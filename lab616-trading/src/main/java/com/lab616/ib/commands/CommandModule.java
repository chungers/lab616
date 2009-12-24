/**
 * 
 */
package com.lab616.ib.commands;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.lab616.common.scripting.ScriptObjects;

/**
 * @author david
 *
 */
public class CommandModule implements Module {

	public void configure(Binder binder) {
		// Use the CommandObjects utility to bind the command consistently.
		ScriptObjects.with(binder)
			.bind(PlaybackData.class);
	}
}
