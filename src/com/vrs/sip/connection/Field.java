/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Generic Field Handing from Connections.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection;

import java.util.Date;

public class Field {
	private String name;
	private Object value;
	private FieldType fieldType;

	public Field(String name, FieldType fieldType, Object value) {
		this.name = name;
		this.fieldType = fieldType;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	public FieldType getFieldType() {
		return fieldType;
	}
	public Object getValue() {
		return value;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	
	public Integer getInteger() {
		Integer result = null;
		
		if (value != null) {
			Double resultDouble = (Double)value;

			result = resultDouble.intValue(); 
		}
		
		return result;
	}
	
	public Double getDouble() {
		Double result = null;
		
		if (value != null) {
			result = (Double)value;
		}
		
		return result;
	}
	
	public String getString() {
		String result = null;
		
		if (value != null) {
			result = (String)value;
		}
		
		return result;
	}
	
	public Boolean getBoolean() {
		Boolean result = null;
		
		if (value != null) {
			result = (Boolean)value;
		}
		
		return result;
	}
	
	public Date getDate() {
		Date result = null;

		if (value != null) {
			result = (Date)value;
		}

		return result;
	}

    public byte[] geContentBytes() {
        byte[] result = null;

        if (value != null) {
            result = (byte[])value;
        }

        return result;
    }
}
