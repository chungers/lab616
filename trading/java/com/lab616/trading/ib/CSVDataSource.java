// 2009 lab616.com, All Rights Reserved.

package com.lab616.trading.ib;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ib.client.EWrapper;

/**
 * Loads data from CSV file.
 * 
 * @author david
 */
public class CSVDataSource {

	static Logger logger = Logger.getLogger(CSVDataSource.class);
	
	static Map<String, Method> methods = Maps.newHashMap();
	
	
	
	public CSVDataSource(String fname, Set<String> methodToLoad) {
		
	}
}
