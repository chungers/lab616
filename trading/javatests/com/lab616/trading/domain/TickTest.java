// 2009 lab616.com, All Rights Reserved.

package com.lab616.trading.domain;

import java.io.EOFException;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.Random;

import junit.framework.TestCase;

import com.lab616.trading.domain.DomainProtos.Tick;

/**
 * @author david
 *
 */
public class TickTest extends TestCase {

	static int TICKS = 10000;
	
	public void testTick() throws Exception {
		Tick tick = Tick.newBuilder().setTimestamp(System.currentTimeMillis())
			.setType(Tick.Type.ASK)
			.setTickerId(1000)
			.setValue(20.0).build();
		
		System.out.println(tick.toByteArray().length + "," + tick.getSerializedSize());
	}
	
	public void testWriteTicks() throws Exception {
		Random rng = new Random(System.currentTimeMillis());
		
		RandomAccessFile f = new RandomAccessFile(new File("/tmp/ticks"), "rwd"); 
		try {
			for (int i=0; i < TICKS; i++) {
				Tick tick = Tick.newBuilder().setTimestamp(System.currentTimeMillis())
				.setType(Tick.Type.ASK)
				.setTickerId(1000)
				.setValue(rng.nextDouble() * .5 + 20.).build();
				
				byte[] bytes = tick.toByteArray();
				assertTrue(0xff > bytes.length);
				f.writeByte(bytes.length);
				f.write(bytes);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			f.close();
		}
	}
	
	public void testReadTicks() throws Exception {
		RandomAccessFile f = new RandomAccessFile(new File("/tmp/ticks"), "rwd"); 
		int size;
		int count = 0;
		try {
			for (; (size = f.readByte()) > 0; count++) {
				byte[] buf = new byte[size];
				f.read(buf);
				Tick tick = Tick.parseFrom(buf);
			}
		} catch (EOFException e) {
		} finally {
			f.close();
		}
		assertEquals(TICKS, count);
	}

	public void testWriteToSharedMemory() throws Exception {
		
	}
}
