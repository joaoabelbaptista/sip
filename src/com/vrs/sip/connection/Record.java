/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Generic Record as handled by Connection.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection;

import com.vrs.sip.task.DataRecord;

import java.util.*;

public class Record extends Object {
	private List<Field> fieldList;
	private Map<String,Integer> fieldIndexMap;
	UUID idOne;
    Boolean hasError = false;

    public Boolean getHasError() {
        return hasError;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }

	public UUID getIdOne() {
		return idOne;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if ((o instanceof DataRecord))
            return idOne.equals(((DataRecord) o).getIdOne());

        if (!(o instanceof Record)) return false;

        Record record = (Record) o;
        return Objects.equals(getIdOne(), record.getIdOne());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getIdOne());
    }

    public void setIdOne(UUID idOne) {
		this.idOne = idOne;
	}

	public class IllegalRecordFieldIndex extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public IllegalRecordFieldIndex(Integer fieldIndex) {
			super("Invalid field index " + fieldIndex);
		}
	}

	public class IllegalRecordFieldName extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public IllegalRecordFieldName(String fieldName) {
			super("Invalid field " + fieldName);
		}
	}
	
	public Record(List<Field> fieldList) {
		Integer i = 0;
		
		this.fieldList = new Vector<Field>();
		
		this.fieldList.addAll(fieldList);
		
		fieldIndexMap = new HashMap<String,Integer>();
		
		for (Field field : fieldList) {			
			fieldIndexMap.put(field.getName(), i);
			i++;
		}
	}
	
	public void setField(String fieldName, Object fieldValue, FieldType fieldType) {
		if (fieldIndexMap.containsKey(fieldName)) {
			// Update existing field
			Integer fieldIndex = fieldIndexMap.get(fieldName);
			Field existingField = fieldList.get(fieldIndex);
			
			existingField.setValue(fieldValue);
			existingField.setFieldType(fieldType);
		} else {
			// Add new field
			
			fieldList.add(new Field(fieldName,fieldType, fieldValue));
			
			fieldIndexMap.put(fieldName, fieldList.size() - 1);
		}
	}
	
	public List<Field> getFieldList() {
		return fieldList;
	}

	public void setFieldList(List<Field> fieldList) {
		Integer i = 0;
		
		this.fieldList = fieldList;
		
		this.fieldList = new Vector<Field>();
		
		this.fieldList.addAll(fieldList);
		
		fieldIndexMap = new HashMap<String,Integer>();
		
		for (Field field : fieldList) {
			//System.err.println("Adding Field named " + field.getName() + " with Index " + i);
			
			fieldIndexMap.put(field.getName(), i);
			i++;
		}
	}
	
	public Field getField(Integer index) {
		return fieldList.get(index);
	}
	
	public Field getFieldByName(String fieldName) throws IllegalRecordFieldIndex, IllegalRecordFieldName {
		if (fieldIndexMap.containsKey(fieldName)) {
			Integer fieldIndex = fieldIndexMap.get(fieldName);
			
			if (fieldIndex < fieldList.size()) {
				return fieldList.get(fieldIndex);
			} else {
				throw new IllegalRecordFieldIndex(fieldIndex);
			}
		} else {
			throw new IllegalRecordFieldName(fieldName);
		}
	}
	
	public Object getFieldValue(Integer index) {
		return fieldList.get(index).getValue();
	}
	
	public FieldType getFieldType(Integer index) {
		return fieldList.get(index).getFieldType();
	}
	
	public Set<String> getFieldNameSet() {
		HashSet<String> fieldNameSet = new HashSet<String>();
		
		fieldNameSet.addAll(fieldIndexMap.keySet());
		
		return fieldNameSet;
	}
	
	@Override
	public String toString() {
		String result = "{ ";
		Integer counter = 0;
		
		if (fieldList != null && fieldList.isEmpty() == false) {
			for (Field field : fieldList) {
				counter++;
				
				if (counter > 1) {
					result += ", ";
				}
				
				result += field.getName() + '=' + field.getValue();
			}
		}
		
		result += " }";
		
		return result;
	}
}
