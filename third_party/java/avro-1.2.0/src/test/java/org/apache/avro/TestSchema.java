/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.avro;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.util.Utf8;

public class TestSchema {

  private static final int COUNT =
    Integer.parseInt(System.getProperty("test.count", "10"));

  @Test
  public void testNull() throws Exception {
    check("\"null\"", "null", null);
  }

  @Test
  public void testBoolean() throws Exception {
    check("\"boolean\"", "true", Boolean.TRUE);
  }

  @Test
  public void testString() throws Exception {
    check("\"string\"", "\"foo\"", new Utf8("foo"));
  }

  @Test
  public void testBytes() throws Exception {
    check("\"bytes\"", "\"\\u0000ABC\\u00FF\"",
          ByteBuffer.wrap(new byte[]{0,65,66,67,-1}));
  }

  @Test
  public void testInt() throws Exception {
    check("\"int\"", "9", new Integer(9));
  }

  @Test
  public void testLong() throws Exception {
    check("\"long\"", "11", new Long(11));
  }

  @Test
  public void testFloat() throws Exception {
    check("\"float\"", "1.1", new Float(1.1));
  }

  @Test
  public void testDouble() throws Exception {
    check("\"double\"", "1.2", new Double(1.2));
  }

  @Test
  public void testArray() throws Exception {
    String json = "{\"type\":\"array\", \"items\": \"long\"}";
    Schema schema = Schema.parse(json);
    GenericArray<Long> array = new GenericData.Array<Long>(1, schema);
    array.add(1L);
    check(json, "[1]", array);
  }

  @Test
  public void testMap() throws Exception {
    HashMap<Utf8,Long> map = new HashMap<Utf8,Long>();
    map.put(new Utf8("a"), 1L);
    check("{\"type\":\"map\", \"values\":\"long\"}", "{\"a\":1}", map);
  }

  @Test
  public void testRecord() throws Exception {
    String recordJson = "{\"type\":\"record\", \"name\":\"Test\", \"fields\":"
      +"[{\"name\":\"f\", \"type\":\"long\"}]}";
    Schema schema = Schema.parse(recordJson);
    GenericData.Record record = new GenericData.Record(schema);
    record.put("f", 11L);
    check(recordJson, "{\"f\":11}", record, false);
  }

  @Test
  public void testEnum() throws Exception {
    check("{\"type\":\"enum\", \"name\":\"Test\","
          +"\"symbols\": [\"A\", \"B\"]}", "\"B\"", "B",
          false);
  }

  @Test
  public void testFixed() throws Exception {
    check("{\"type\": \"fixed\", \"name\":\"Test\", \"size\": 1}", "\"a\"",
          new GenericData.Fixed(new byte[]{(byte)'a'}), false);
  }

  @Test
  public void testRecursive() throws Exception {
    check("{\"type\": \"record\", \"name\": \"Node\", \"fields\": ["
          +"{\"name\":\"label\", \"type\":\"string\"},"
          +"{\"name\":\"children\", \"type\":"
          +"{\"type\": \"array\", \"items\": \"Node\" }}]}",
          false);
  }

  @Test
  public void testRecursiveEquals() throws Exception {
    String jsonSchema = "{\"type\":\"record\", \"name\":\"List\", \"fields\": ["
      +"{\"name\":\"next\", \"type\":\"List\"}]}";
    Schema s1 = Schema.parse(jsonSchema);
    Schema s2 = Schema.parse(jsonSchema);
    assertEquals(s1, s2);
    s1.hashCode();                                // test no stackoverflow
  }

  @Test
  public void testLisp() throws Exception {
    check("{\"type\": \"record\", \"name\": \"Lisp\", \"fields\": ["
          +"{\"name\":\"value\", \"type\":[\"null\", \"string\","
          +"{\"type\": \"record\", \"name\": \"Cons\", \"fields\": ["
          +"{\"name\":\"car\", \"type\":\"Lisp\"},"
          +"{\"name\":\"cdr\", \"type\":\"Lisp\"}]}]}]}",
          false);
  }

  @Test
  public void testUnion() throws Exception {
    check("[\"string\", \"long\"]", false);
    checkDefault("[\"double\", \"long\"]", "1.1", new Double(1.1));

    // check union json
    String record = "{\"type\":\"record\",\"name\":\"Foo\",\"fields\":[]}";
    String fixed = "{\"type\":\"fixed\",\"name\":\"Bar\",\"size\": 1}";
    String enu = "{\"type\":\"enum\",\"name\":\"Baz\",\"symbols\": [\"X\"]}";
    Schema union = Schema.parse("[\"null\",\"string\","
                                +record+","+ enu+","+fixed+"]");
    checkJson(union, null, "null");
    checkJson(union, new Utf8("foo"), "{\"string\":\"foo\"}");
    checkJson(union,
              new GenericData.Record(Schema.parse(record)),
              "{\"Foo\":{}}");
    checkJson(union,
              new GenericData.Fixed(new byte[]{(byte)'a'}),
              "{\"Bar\":\"a\"}");
    checkJson(union, "X", "{\"Baz\":\"X\"}");
  }

  private static void check(String schemaJson, String defaultJson,
                            Object defaultValue) throws Exception {
    check(schemaJson, defaultJson, defaultValue, true);
  }
  private static void check(String schemaJson, String defaultJson,
                            Object defaultValue, boolean induce)
    throws Exception {
    check(schemaJson, induce);
    checkDefault(schemaJson, defaultJson, defaultValue);
  }

  private static void check(String jsonSchema) throws Exception {
    check(jsonSchema, true);
  }
  private static void check(String jsonSchema, boolean induce)
    throws Exception {
    Schema schema = Schema.parse(jsonSchema);
    //System.out.println(schema);
    for (Object datum : new RandomData(schema, COUNT)) {
      //System.out.println(GenericData.get().toString(datum));

      if (induce) {
        Schema induced = GenericData.get().induce(datum);
        assertEquals("Induced schema does not match.", schema, induced);
      }
        
      assertTrue("Datum does not validate against schema "+datum,
                 GenericData.get().validate(schema, datum));

      checkBinary(schema, datum,
                  new GenericDatumWriter<Object>(),
                  new GenericDatumReader<Object>());
      checkJson(schema, datum,
                  new GenericDatumWriter<Object>(),
                  new GenericDatumReader<Object>());
    }
  }

  private static void checkBinary(Schema schema, Object datum,
                                  DatumWriter<Object> writer,
                                  DatumReader<Object> reader)
    throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    writer.setSchema(schema);
    writer.write(datum, new BinaryEncoder(out));
    byte[] data = out.toByteArray();

    reader.setSchema(schema);
        
    Object decoded =
      reader.read(null, new BinaryDecoder(new ByteArrayInputStream(data)));
      
    assertEquals("Decoded data does not match.", datum, decoded);
  }

  private static void checkJson(Schema schema, Object datum,
                                DatumWriter<Object> writer,
                                DatumReader<Object> reader)
    throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Encoder encoder = new JsonEncoder(schema, out);
    writer.setSchema(schema);
    writer.write(datum, encoder);
    writer.write(datum, encoder);
    encoder.flush();
    byte[] data = out.toByteArray();

    reader.setSchema(schema);
    Decoder decoder = new JsonDecoder(schema, new ByteArrayInputStream(data));
    Object decoded = reader.read(null, decoder);
    assertEquals("Decoded data does not match.", datum, decoded);

    decoded = reader.read(decoded, decoder);
    assertEquals("Decoded data does not match.", datum, decoded);
  }

  private static void checkJson(Schema schema, Object datum,
                                String json) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Encoder encoder = new JsonEncoder(schema, out);
    DatumWriter<Object> writer = new GenericDatumWriter<Object>();
    writer.setSchema(schema);
    writer.write(datum, encoder);
    encoder.flush();
    byte[] data = out.toByteArray();

    String encoded = new String(data, "UTF-8");
    assertEquals("Encoded data does not match.", json, encoded);

    DatumReader<Object> reader = new GenericDatumReader<Object>();
    reader.setSchema(schema);
    Object decoded =
      reader.read(null, new JsonDecoder(schema,new ByteArrayInputStream(data)));
      
    assertEquals("Decoded data does not match.", datum, decoded);
  }

  private static final Schema ACTUAL =            // an empty record schema
    Schema.parse("{\"type\":\"record\", \"name\":\"Foo\", \"fields\":[]}");

  @SuppressWarnings(value="unchecked")
  private static void checkDefault(String schemaJson, String defaultJson,
                                   Object defaultValue) throws Exception {
    String recordJson =
      "{\"type\":\"record\", \"name\":\"Foo\", \"fields\":[{\"name\":\"f\", "
    +"\"type\":"+schemaJson+", "
    +"\"default\":"+defaultJson+"}]}";
    Schema expected = Schema.parse(recordJson);
    DatumReader in = new GenericDatumReader(ACTUAL, expected);
    GenericData.Record record = (GenericData.Record)
      in.read(null, new BinaryDecoder(new ByteArrayInputStream(new byte[0])));
    assertEquals("Wrong default.", defaultValue, record.get("f"));
    assertEquals("Wrong toString", expected, Schema.parse(expected.toString()));
  }

}
