/**
 * 
 */
package com.lab616.ib.commands;

import com.google.inject.Inject;
import com.ib.client.EClientSocket;
import com.lab616.ib.api.EClientSocketFactory;
import com.lab616.omnibus.Kernel;

/**
 * Command that plays back either CSV for Proto data files.
 * 
 * @author david
 *
 */
public class PlaybackData {

	private final EClientSocketFactory clientSocketFactory;
	
	@Inject
	public PlaybackData(Kernel kernel) {
		clientSocketFactory = kernel.getInstance(EClientSocketFactory.class);
	}
	
	public void start(String name) throws Exception {
		EClientSocket client = clientSocketFactory.create(
				name, null, true);
	}
}
