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
package org.apache.avro.io.parsing;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;

/**
 * The class that generates a resolving grammar to resolve between two
 * schemas.
 */
public class ResolvingGrammarGenerator extends ValidatingGrammarGenerator {
  /**
   * Resolves the writer schema <tt>writer</tt> and the reader schema
   * <tt>reader</tt> and returns the start symbol for the grammar generated. 
   * @param writer    The schema used by the writer
   * @param reader    The schema used by the reader
   * @return          The start symbol for the resolving grammar
   * @throws IOException 
   */
  public final Symbol generate(Schema writer, Schema reader)
    throws IOException {
    return Symbol.root(generate(writer, reader, new HashMap<LitS, Symbol>()));
  }
  
  /**
   * Resolves the writer schema <tt>writer</tt> and the reader schema
   * <tt>reader</tt> and returns the start symbol for the grammar generated.
   * If there is already a symbol in the map <tt>seen</tt> for resolving the
   * two schemas, then that symbol is returned. Otherwise a new symbol is
   * generated and returnd. 
   * @param writer    The schema used by the writer
   * @param reader    The schema used by the reader
   * @param seen      The &lt;reader-schema, writer-schema&gt; to symbol
   * map of start symbols of resolving grammars so far.
   * @return          The start symbol for the resolving grammar
   * @throws IOException 
   */
  public Symbol generate(Schema writer, Schema reader,
                                Map<LitS, Symbol> seen) throws IOException
  {
    final Schema.Type writerType = writer.getType();
    final Schema.Type readerType = reader.getType();

    if (writerType == readerType) {
      switch (writerType) {
      case NULL:
        return Symbol.NULL;
      case BOOLEAN:
        return Symbol.BOOLEAN;
      case INT:
        return Symbol.INT;
      case LONG:
        return Symbol.LONG;
      case FLOAT:
        return Symbol.FLOAT;
      case DOUBLE:
        return Symbol.DOUBLE;
      case STRING:
        return Symbol.STRING;
      case BYTES:
        return Symbol.BYTES;
      case FIXED:
        if (writer.getName().equals(reader.getName())
            && writer.getFixedSize() == reader.getFixedSize()) {
          return Symbol.seq(new Symbol.IntCheckAction(writer.getFixedSize()),
              Symbol.FIXED);
        }
        break;

      case ENUM:
        if (writer.getName() == null
                || writer.getName().equals(reader.getName())) {
          return Symbol.seq(mkEnumAdjust(writer.getEnumSymbols(),
                  reader.getEnumSymbols()), Symbol.ENUM);
        }
        break;

      case ARRAY:
        return Symbol.seq(Symbol.ARRAY_END,
            Symbol.repeat(Symbol.ARRAY_END,
                generate(writer.getElementType(),
                reader.getElementType(), seen)),
            Symbol.ARRAY_START);
      
      case MAP:
        return Symbol.seq(Symbol.MAP_END,
            Symbol.repeat(Symbol.MAP_END,
                generate(writer.getValueType(),
                reader.getValueType(), seen), Symbol.STRING),
            Symbol.MAP_START);
      case RECORD:
        return resolveRecords(writer, reader, seen);
      case UNION:
        return resolveUnion(writer, reader, seen);
      default:
        throw new AvroTypeException("Unkown type for schema: " + writerType);
      }
    } else {  // writer and reader are of different types
      if (writerType == Schema.Type.UNION) {
        return resolveUnion(writer, reader, seen);
      }
  
      switch (readerType) {
      case LONG:
        switch (writerType) {
        case INT:
        case DOUBLE:
        case FLOAT:
          return Symbol.resolve(super.generate(writer, seen), Symbol.LONG);
        }
        break;
  
      case DOUBLE:
        switch (writerType) {
        case INT:
        case LONG:
        case FLOAT:
          return Symbol.resolve(super.generate(writer, seen), Symbol.DOUBLE);
        }
        break;
  
      case UNION:
        int j = bestBranch(reader, writer);
        if (j >= 0) {
          Symbol s = generate(writer, reader.getTypes().get(j), seen);
          return Symbol.seq(new Symbol.UnionAdjustAction(j, s), Symbol.UNION);
        }
        break;
      case NULL:
      case BOOLEAN:
      case INT:
      case STRING:
      case FLOAT:
      case BYTES:
      case ENUM:
      case ARRAY:
      case MAP:
      case RECORD:
        break;
      default:
        throw new RuntimeException("Unexpected schema type: " + readerType);
      }
    }
    return Symbol.error("Found " + writer + ", expecting " + reader);
  }

  private Symbol resolveUnion(Schema writer, Schema reader,
      Map<LitS, Symbol> seen) throws IOException {
    List<Schema> alts = writer.getTypes();
    final int size = alts.size();
    Symbol[] symbols = new Symbol[size];
    String[] labels = new String[size];

    /**
     * We construct a symbol without filling the arrays. Please see
     * {@link Symbol#production} for the reason.
     */
    int i = 0;
    for (Schema w : alts) {
      symbols[i] = generate(w, reader, seen);
      labels[i] = w.getName();
      i++;
    }
    return Symbol.seq(Symbol.alt(symbols, labels),
        new Symbol.WriterUnionAction());
  }

  private Symbol resolveRecords(Schema writer, Schema reader,
      Map<LitS, Symbol> seen) throws IOException {
    LitS wsc = new LitS2(writer, reader);
    Symbol result = seen.get(wsc);
    if (result == null) {
      // first count the number of entries in the production;
      int count = 0;

      Map<String, Field> wfields = writer.getFields();
      Map<String, Field> rfields = reader.getFields();
      for (String fn : wfields.keySet()) {
        count += (rfields.get(fn) == null) ? 1 : 2;
      }

      for (Map.Entry<String, Field> rfe : rfields.entrySet()) {
        String fname = rfe.getKey();
        if (wfields.get(fname) == null) {
          Field rf = rfe.getValue();
          if (rf.defaultValue() == null) {
            result = Symbol.error("Found " + writer + ", expecting " + reader);
            seen.put(wsc, result);
            return result;
          } else {
            count += 4;
          }
        }
      }

      Symbol[] production = new Symbol[count];

      /**
       * We construct a symbol without filling the array. Please see
       * {@link Symbol#production} for the reason.
       */
      result = Symbol.seq(production);
      seen.put(wsc, result);

      /*
       * For now every field in read-record with no default value
       * must be in write-record.
       * Write record may have additional fields, which will be
       * skipped during read.
       */

      // Handle all the writer's fields
      for (Map.Entry<String, Field> wfe : wfields.entrySet()) {
        String fname = wfe.getKey();
        Field rf = rfields.get(fname);
        if (rf == null) {
          production[--count] =
            new Symbol.SkipAction(super.generate(wfe.getValue().schema(),
              seen));
        } else {
          production[--count] =
            new Symbol.FieldAdjustAction(rf.pos(), fname);
          production[--count] =
            generate(wfe.getValue().schema(), rf.schema(), seen);
        }
      }

      // Add default values for fields missing from Writer
      for (Map.Entry<String, Field> rfe : rfields.entrySet()) {
        String fname = rfe.getKey();
        Field wf = wfields.get(fname);
        if (wf == null) {
          Field rf = rfe.getValue();
          production[--count] = new Symbol.FieldAdjustAction(rf.pos(), fname);
          production[--count] = new Symbol.DefaultStartAction(
              new JsonGrammarGenerator().generate(rf.schema()),
              rf.defaultValue());
          production[--count] = super.generate(rf.schema(), seen);
          production[--count] = Symbol.DEFAULT_END_ACTION;
        }
      }
    }
    return result;
  }

  private static Symbol mkEnumAdjust(List<String> rsymbols,
      List<String> wsymbols){
    Object[] adjustments = new Object[wsymbols.size()];
    for (int i = 0; i < adjustments.length; i++) {
      int j = rsymbols.indexOf(wsymbols.get(i));
      adjustments[i] = (j == -1 ? "No match for " + wsymbols.get(i)
                                : new Integer(j));
    }
    return new Symbol.EnumAdjustAction(rsymbols.size(), adjustments);
  }

  private static int bestBranch(Schema r, Schema w) {
    Schema.Type vt = w.getType();
      // first scan for exact match
      int j = 0;
      for (Schema b : r.getTypes()) {
        if (vt == b.getType())
          if (vt == Schema.Type.RECORD) {
            String vname = w.getName();
            if (vname == null || vname.equals(b.getName()))
              return j;
          } else
            return j;
        j++;
      }

      // then scan match via numeric promotion
      j = 0;
      for (Schema b : r.getTypes()) {
        switch (vt) {
        case INT:
          switch (b.getType()) {
          case LONG: case DOUBLE:
            return j;
          }
          break;
        case LONG:
        case FLOAT:
          switch (b.getType()) {
          case DOUBLE:
            return j;
          }
          break;
        }
        j++;
      }
      return -1;
  }

  /**
   * Clever trick which differentiates items put into
   * <code>seen</code> by {@link ValidatingGrammarGenerator#validating validating()}
   * from those put in by {@link ValidatingGrammarGenerator#resolving resolving()}.
   */
   static class LitS2 extends ValidatingGrammarGenerator.LitS {
     public Schema expected;
     public LitS2(Schema actual, Schema expected) {
       super(actual);
       this.expected = expected;
     }
     public boolean equals(Object o) {
       if (! (o instanceof LitS2)) return false;
       LitS2 other = (LitS2) o;
       return actual == other.actual && expected == other.expected;
     }
     public int hashCode() {
       return super.hashCode() + expected.hashCode();
     }
   }
}

