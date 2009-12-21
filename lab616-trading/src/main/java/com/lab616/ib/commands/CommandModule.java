/**
 * 
 */
package com.lab616.ib.commands;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

/**
 * @author david
 *
 */
public class CommandModule implements Module {

	public void configure(Binder binder) {
		binder.bind(PlaybackData.class).in(Scopes.SINGLETON);
	}
}
