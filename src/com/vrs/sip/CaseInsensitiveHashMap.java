package com.vrs.sip;

import java.util.HashMap;

/**
 * Case Insensitive HashMap.
 * 
 * @author aosantos
 *
 */
public class CaseInsensitiveHashMap extends HashMap<String, Object> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String put(String key, Object value) {
		String upperCaseKey = key;
		
		if (upperCaseKey != null) {
			upperCaseKey = upperCaseKey.toUpperCase();
		}
		
		return (String) super.put(upperCaseKey, value);
	}
	
	@Override
	public Object get(Object key) {
		String upperCaseKey = (String)key;
		
		if (upperCaseKey != null) {
			upperCaseKey = upperCaseKey.toUpperCase();
		}
		
		return super.get(upperCaseKey);
	}
}
