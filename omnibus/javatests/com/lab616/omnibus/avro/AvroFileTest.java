// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.avro;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.file.SeekableFileInput;
import org.apache.avro.generic.GenericData;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.util.Utf8;

import com.lab616.omnibus.avro.test.Event;
import com.lab616.omnibus.avro.test.Field;
import com.lab616.util.Time;

/**
 * @author david
 *
 */
public class AvroFileTest extends TestCase {

	private static int ITEMS = 1000;
	
	/**
	 * Tests creation of avro objects.  These are defined in the test avpr schema.
	 * 
	 * @throws Exception
	 */
	public void testCreateEvents() throws Exception {
		Event event = buildEvent(5);
		
		// Now check value
		assertEquals(10, event.fields.size());
		assertTrue(event.timestamp > 0);
		
		int i = 0;
		for (Field f : event.fields) {
			if (i < 5) {
				assertEquals("i" + i, f.stringValue.toString());
			} else {
				assertTrue(i-5 == f.intValue);
			}
			i++;
		}
	}

	/**
	 * Tests creating a file.
	 * @throws Exception
	 */
	public void testWriteAndReadFile() throws Exception {
		writeFile(getFile(), false, ITEMS);
		readFile(getFile(), ITEMS);
	}

	/**
	 * Tests appending to an existing file.
	 * @throws Exception
	 */
	public void testAppendAndReadFile() throws Exception {
		int total = 0;
		if (getFile().exists()) {
			getFile().delete();
		}
		total += writeFile(getFile(), false, 1);
		total += writeFile(getFile(), true, 1);
		
		readFile(getFile(), total);
	}
	


	private Event buildEvent(int fields) {
		Event event = new Event();
		event.method = new Utf8("test");
		event.timestamp = Time.now();
		
		// Create the field array.
		Field field = new Field();
		event.fields = new GenericData.Array<Field>(100, field.getSchema());
		
		for (int i=0; i<fields; i++) {
			field = new Field();
			field.stringValue = new Utf8("i" + i);
			event.fields.add(field);
		}
		for (int i=0; i<fields; i++) {
			field = new Field();
			field.intValue = i;
			event.fields.add(field);
		}
		return event;
	}
	
	private File getFile() {
		return new File("/tmp/" + 
				getClass().getSimpleName() + ".test");
	}

  private int writeFile(File file, boolean append, int items) 
		throws Exception {
		FileOutputStream fs = new FileOutputStream(file, append);
		BufferedOutputStream os = new BufferedOutputStream(fs);
		
		Schema schema = new Event().getSchema();
		DatumWriter<Object> sdw = new SpecificDatumWriter(schema);
		DataFileWriter<Object> dfw = new DataFileWriter<Object>(schema, os, sdw);
		
		int written = 0;
		for (int i = 0; i < items; i++) {
			dfw.append(buildEvent(5));
			written++;
		}
		dfw.flush();
		dfw.close();
		return written;
	}
	
	private void readFile(File file, int items) throws Exception {
		SeekableFileInput sfi = new SeekableFileInput(file);
		
		Schema schema = new Event().getSchema();
		DatumReader<Object> sdr = new SpecificDatumReader(schema);

		DataFileReader<Object> dfr = new DataFileReader<Object>(sfi, sdr);
		
		int count = 0;
		try {
			Object event = null;
			while ((event = dfr.next(new Event())) != null) {
				assertTrue(event instanceof Event);
				count++;
			}
		} catch (IOException e) {
			System.out.println("count = " + count);
			throw e;
		} finally {
			dfr.close();
		}
		assertEquals(items, count);
	}
}
