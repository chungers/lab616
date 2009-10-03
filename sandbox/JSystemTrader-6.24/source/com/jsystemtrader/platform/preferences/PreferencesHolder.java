/**
 * Original author Eugene Kononov <nonlinear5@yahoo.com> 
 * Adapted for JST by Florent Guiliani <florent@guiliani.fr>
 */
package com.jsystemtrader.platform.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

public class PreferencesHolder {
    private static PreferencesHolder instance;
    private final Preferences prefs;


    public static synchronized PreferencesHolder getInstance() {
        if (instance == null) {
            instance = new PreferencesHolder();
        }
        return instance;
    }

    // private constructor for non-instantiability
    private PreferencesHolder() {
        prefs = Preferences.userNodeForPackage(getClass());
    }

	public long getLong(JSTPreferences pref) {
        String value = get(pref);
        return Long.valueOf(value);
	}

    public int getInt(JSTPreferences pref) {
        String value = get(pref);
        return Integer.valueOf(value);
    }

    public boolean getBool(JSTPreferences pref) {
        String value = get(pref);
        return Boolean.valueOf(value);
    }
    
    public String[] getStringArray(JSTPreferences pref) {
        String value = get(pref);
        StringTokenizer st = new StringTokenizer(value, ",");
    	List<String> items = new ArrayList<String>();
    	while (st.hasMoreTokens()) {
    	    items.add(st.nextToken());
    	}

    	return items.toArray(new String[items.size()]);
    }
  

    public String get(JSTPreferences pref) {
        return prefs.get(pref.getName(), pref.getDefault());
    }

    public void set(JSTPreferences pref, String propertyValue) {
        prefs.put(pref.getName(), propertyValue);
    }

    public void set(JSTPreferences pref, int propertyValue) {
        set(pref, String.valueOf(propertyValue));
    }

	public void set(JSTPreferences pref, long propertyValue) {
        set(pref, String.valueOf(propertyValue));		
	}

    public void set(JSTPreferences pref, boolean propertyValue) {
        set(pref, String.valueOf(propertyValue));
    }
}
