// 2009 lab616.com, All Rights Reserved.

package com.lab616.omnibus.event;

import com.espertech.esper.client.Configuration;


/**
 * Event definition for POJOs.
 * 
 * @author david
 *
 */
public class ObjectEventDefinition<E> extends EventDefinition<Class<E>> {

	private String[] imports = new String[0];
	private Class<?>[] importClasses = new Class<?>[0];
	
	public ObjectEventDefinition(String name, Class<E> eventType, String... imp) {
		super(name, eventType);
		this.imports = imp;
	}
	
	public ObjectEventDefinition(String name, Class<E> eventType, Class<?>... imp) {
		super(name, eventType);
		this.importClasses = imp;
	}

	public ObjectEventDefinition(String name, Class<E> eventType) {
		super(name, eventType);
	}

	public ObjectEventDefinition(Class<E> eventType) {
		super(eventType.getSimpleName(), eventType);
	}

	void configure(Configuration configuration) {
		configuration.addEventType(name(), type());
		configuration.addImport(type().getPackage().getName() + ".*");
		for (String imp : imports) {
			configuration.addImport(imp);
		}
		for (Class<?> imp : importClasses) {
			configuration.addImport(imp);
		}	
	}	
}
