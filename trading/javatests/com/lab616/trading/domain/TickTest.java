// 2009 lab616.com, All Rights Reserved.

package com.lab616.trading.domain;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Random;

import com.lab616.trading.domain.DomainProtos.Tick;

import junit.framework.TestCase;

/**
 * @author david
 *
 */
public class TickTest extends TestCase {

	public void testTick() throws Exception {
		Tick tick = Tick.newBuilder().setTimestamp(System.currentTimeMillis())
			.setType(Tick.Type.ASK)
			.setTickerId(1000)
			.setValue(20.0).build();
		
		System.out.println(tick.toByteArray().length);
	}
	
	public void testWriteTicks() throws Exception {
		Random rng = new Random(System.currentTimeMillis());
		
		RandomAccessFile f = new RandomAccessFile(new File("/tmp/ticks"), "rw"); 
		for (int i=0; i < 100; i++) {
			Tick tick = Tick.newBuilder().setTimestamp(System.currentTimeMillis())
			.setType(Tick.Type.ASK)
			.setTickerId(1000)
			.setValue(rng.nextDouble() * .5 + 20.).build();
			
			byte[] bytes = tick.toByteArray();
			f.writeInt(bytes.length);
			f.write(bytes);
		}
		f.close();
	}
	
	public void testReadTicks() throws Exception {
		RandomAccessFile f = new RandomAccessFile(new File("/tmp/ticks"), "rw"); 
		int size = f.readInt();
		while (size > 0) {
			byte[] buf = new byte[size];
			f.read(buf);
			Tick tick = Tick.parseFrom(buf);
		}
	}
}
