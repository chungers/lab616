package com.jsystemtrader.platform.util;

import com.jsystemtrader.platform.model.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;


public class ClassFinder {

    /**
     * Searches the classpath (including inside the JAR files) to find classes
     * that extend the specified superclass. The intent is to be able to implement
     * new strategy classes as "plug-and-play" units of JSystemTrader. That is,
     * JSystemTrader will know how to run the trading strategy as long as this
     * strategy is implemented in a class that extends the base Strategy class.
     */
    public List<Class<?>> getClasses(String packageName, String superClassName) throws URISyntaxException, IOException, ClassNotFoundException {
    	return getClasses(packageName, superClassName, false);
    }
    
    public List<Class<?>> getInterfaces(String packageName, String interfaceName) throws URISyntaxException, IOException, ClassNotFoundException {
        return getClasses(packageName, interfaceName, true);
    }    
    
    public List<Class<?>> getClasses(String packageName, String parentName, boolean parentIsInterface) throws URISyntaxException, IOException, ClassNotFoundException {

        String packagePath = packageName.replace('.', '/');
        URL[] classpath = ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs();
        List<Class<?>> classes = new ArrayList<Class<?>>();

        for (URL url : classpath) {
            List<String> classNames = new ArrayList<String>();

            ClassLoader classLoader = new URLClassLoader(new URL[]{url});
            URI uri = url.toURI();
            File file = new File(uri);

            if (file.getPath().endsWith(".jar")) {
                if (file.exists()) {
                    JarFile jarFile = new JarFile(file);
                    for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                        String entryName = (entries.nextElement()).getName();
                        if (entryName.matches(packagePath + "/\\w*\\.class")) {// get only class files in package dir
                            String className = entryName.replace('/', '.').substring(0, entryName.lastIndexOf('.'));
                            classNames.add(className);
                        }
                    }
                }
            } else {// directory
                File packageDirectory = new File(file.getPath() + "/" + packagePath);
                if (packageDirectory.exists()) {
                    for (File f : packageDirectory.listFiles()) {
                        if (f.getPath().endsWith(".class")) {
                            String className = packageName + "." + f.getName()
                                    .substring(0, f.getName().lastIndexOf('.'));
                            classNames.add(className);
                        }
                    }
                }
            }

            // make sure the strategy extends the base parentName class
            for (String className : classNames) {
                Class<?> clazz = classLoader.loadClass(className);                
                if ( parentIsInterface ) {
                	boolean interfaceFound = false;
                	for(Class<?> implementedInterface: clazz.getInterfaces()) {
                		if(implementedInterface.getName().equals(parentName)) {
                			interfaceFound = true;
                			break;
                		}
                	}
                	if(interfaceFound) {
                		classes.add(clazz);
                	}
                }
                else if(clazz.getSuperclass()!=null && clazz.getSuperclass().getName().equals(parentName)) {
                    classes.add(clazz);
                }
            }
        }

        // Java Web Start support
        ClassLoader cl = getClass().getClassLoader();
        if (cl.getClass().getSimpleName().equals("JNLPClassLoader")) {
        	BufferedReader strategies = new BufferedReader(new InputStreamReader(cl.getResourceAsStream("strategies.txt")));
        	for (String strategy; (strategy = strategies.readLine()) != null;) {
        		String className = packageName + "." + strategy.substring(0, strategy.lastIndexOf("."));
        		Class<?> clazz = Class.forName(className);
        		classes.add(clazz);
        	}
        	strategies.close();
        }
        // End of Java Web Start support

        Collections.sort(classes, new Comparator<Class<?>>() {
            public int compare(Class<?> o1, Class<?> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return classes;
    }

    public String[] getClassNames() throws JSystemTraderException {
        String[] names;
        try {
            List<Class<?>> classes = getClasses("com.jsystemtrader.strategy", "com.jsystemtrader.platform.strategy.Strategy");
            List<String> classNames = new ArrayList<String>();

            for (Class<?> strategyClass : classes) {
                classNames.add(strategyClass.getSimpleName());
            }
            names = classNames.toArray(new String[classNames.size()]);
        } catch (Exception e) {
            throw new JSystemTraderException(e);
        }

        Arrays.sort(names);
        return names;
    }
}
