package com.lab616.ib.commands;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.ib.client.EWrapper;
import com.lab616.common.scripting.ScriptObject;
import com.lab616.ib.api.TWSClient;
import com.lab616.ib.api.TWSClientManager;
import com.lab616.ib.api.simulator.DataSource;
import com.lab616.ib.api.simulator.EClientSocketSimulator;
import com.lab616.ib.api.simulator.EWrapperDataSource;
import com.lab616.ib.api.simulator.ProtoFileDataSource;

/**
 * Command that plays back either CSV for Proto data files.
 * 
 * @author david
 *
 */
@ScriptObject(name = "tws.PlaybackData", doc = "")
public class PlaybackData {

	private final TWSClientManager clientManager;
	
	@Inject
	public PlaybackData(TWSClientManager clientManager) {
		this.clientManager = clientManager;
	}

	public interface EWrapperSession extends PlaybackSession {
		public EWrapper getEWrapper();
	}

	public interface PlaybackSession {
		public PlaybackSession withProtoFile(String filename, long... latency)
			throws Exception;
		public EWrapperSession withEWrapper(String name)
			throws Exception;
		public void start();
	}
	
	
	public PlaybackSession newSession(String profile) throws Exception {
		final EClientSocketSimulator sim = startSimulator(profile);
		return new PlaybackSession() {
			private List<DataSource> dataSources = Lists.newArrayList();
			private void runAll() {
				for (DataSource d : dataSources) {
					d.start();
				}
			}
			@Override
			public PlaybackSession withProtoFile(String fname, long... latency)
				throws Exception {
				ProtoFileDataSource pfd = new ProtoFileDataSource(fname);
				if (latency.length > 0 && latency[0] > 0) {
					pfd.setEventLatency(latency[0]);
				}
				sim.addDataSource(pfd);
				dataSources.add(pfd);
				return this;
			}
			@Override
			public void start() {
				runAll();
			}
			@Override
			public EWrapperSession withEWrapper(String name)
				throws Exception {
				final EWrapperDataSource eds = new EWrapperDataSource(name);
				sim.addDataSource(eds);
				dataSources.add(eds);
				return new EWrapperSession() {
					@Override
					public PlaybackSession withProtoFile(String fname, long... latency)
						throws Exception {
						return withProtoFile(fname, latency);
					}
					@Override
					public EWrapperSession withEWrapper(String name) {
						return this;
					}
					@Override
					public EWrapper getEWrapper() {
						return eds.getEWrapper();
					}
					@Override
					public void start() {
						runAll();
					}
				};
			}
 		};
	}

	/**
	 * Starts the simulator with the given profile name.  A new client is
	 * started.  The profile should be distinct from the actual production / live
	 * accounts.
	 * @param profile The profile name.
	 * @return A simulator instance.
	 * @throws Exception
	 */
	EClientSocketSimulator startSimulator(String profile) throws Exception {
		int clientId = clientManager.newConnection(profile, true);
		
		TWSClient client = clientManager.getClient(profile, clientId);
		client.connect();
		while (!client.isReady()) {
			Thread.sleep(10L);
		}
		return EClientSocketSimulator.getSimulator(profile, clientId);
	}
	
}
