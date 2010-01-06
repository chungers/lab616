/**
 * 
 */
package com.lab616.ib.scripting;

/**
 * @author david
 *
 */
public class ScriptingModule extends com.lab616.common.scripting.ScriptingModule {

  @Override
	public void configure() {
    bind(PlaybackData.class);
    bind(ConnectionManagement.class);
    bind(EventLogManagement.class);
    bind(MarketData.class);
	}
}
