/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Data Record as Handled and passed trough Task Steps.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.task;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.vrs.sip.connection.Field;
import com.vrs.sip.connection.Record;
import com.vrs.sip.connection.Record.IllegalRecordFieldIndex;
import com.vrs.sip.connection.Record.IllegalRecordFieldName;

public class DataRecord {
	private HashMap<String,Object> fieldMap;
    UUID idOne = UUID.randomUUID();
    Boolean hasError = false;

    public Boolean getHasError() {
        return hasError;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o instanceof Record)){
            idOne.equals(((Record) o).getIdOne());
        }
        else if (!(o instanceof DataRecord))
            return false;

        DataRecord that = (DataRecord) o;
        return Objects.equals(getIdOne(), that.getIdOne());
    }



    @Override
    public int hashCode() {

        return Objects.hash(getIdOne());
    }

    public UUID getIdOne() {
        return idOne;
    }

    public void setIdOne(UUID idOne) {
        this.idOne = idOne;
    }
	
	/** Constructor **/
	public DataRecord() {
		fieldMap = new HashMap<String,Object>(); 
	}
	
	public DataRecord(Record record) throws IllegalRecordFieldIndex, IllegalRecordFieldName {
		fieldMap = new HashMap<String,Object>();
		
		if (record != null) {
			Set<String> fieldNameSet = record.getFieldNameSet();
			
			for (String fieldName : fieldNameSet) {
				Field field = record.getFieldByName(fieldName);
				Object fieldValue = field.getValue();
				
				setFieldValue(fieldName, fieldValue);
			}
		}
	}
	
	public void copyFrom(DataRecord other) {
		fieldMap.clear();
		
		for (String fieldName : other.getFields()) {
			Object value = other.getFieldValue(fieldName);
			
			setFieldValue(fieldName, value);
		}
	}
	
	/** Public Methods **/
	
	/** Get set of field names that this data record is composed of **/
	public Set<String> getFields() {
		return fieldMap.keySet();
	}
	
	/** Get the field value for the field named fieldName **/
	public Object getFieldValue(String fieldName) {
		if (fieldMap.containsKey(fieldName)) {
			return fieldMap.get(fieldName);
		} else {
			return null;
		}
	}
	
	/** Set the field value for the field named fieldName **/
	public void setFieldValue(String fieldName, Object fieldValue) {
		fieldMap.put(fieldName,  fieldValue);
	}
	
	@Override
	public String toString() {
		Integer counter = 0;
		String result = "";
		
		for (String fieldName : getFields()) {
			counter++;
			
			if (counter > 1) {
				result += ", ";
			}
			
			result += fieldName + "=" + getFieldValue(fieldName);
		}
		
		return result;
	}
}
