/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Entity.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.task;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Entity {
	public String entityId;
	public String entityName;
	public List<EntityField> fieldList;
	public Map<String,EntityField> fieldMap;
	public List<String> fieldNameList;
	
	public Entity() {
		fieldList = new Vector<EntityField>();
		fieldMap = new HashMap<String,EntityField>();
		fieldNameList = new Vector<String>();
	}
	
	public void addField(EntityField entityField) {
		fieldList.add(entityField);
		fieldMap.put(entityField.entityFieldName, entityField);
		fieldNameList.add(entityField.entityFieldName);
	}
	
	public EntityField getField(String fieldName) {
		return fieldMap.get(fieldName);
	}
	
	public List<String> getFieldNameList() {
		return fieldNameList;
	}
	
	public void sortFields() {
		fieldNameList.clear();
		
		Collections.sort(fieldList);
		
		for (EntityField entityField : fieldList) {
			fieldNameList.add(entityField.entityFieldName);
		}
	}
}
