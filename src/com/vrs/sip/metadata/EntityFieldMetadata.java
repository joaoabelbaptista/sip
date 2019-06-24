/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Entity Field Metadata.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.metadata;

import com.vrs.sip.connection.Record;
import com.vrs.sip.connection.Record.IllegalRecordFieldIndex;
import com.vrs.sip.connection.Record.IllegalRecordFieldName;

public class EntityFieldMetadata {
	public String id;
	public String name;
	public String entityId;
	public String fieldName;
	public Integer order;
	public String type;
	
	public EntityFieldMetadata(Record record) throws IllegalRecordFieldIndex, IllegalRecordFieldName {
		id = record.getFieldByName("Id").getString();
		name = record.getFieldByName("Name").getString();
		entityId = record.getFieldByName("Entity__c").getString();
		fieldName = record.getFieldByName("Field_Name__c").getString();
		order = record.getFieldByName("Order__c").getInteger();
		type = record.getFieldByName("Type__c").getString();
	}
	
	public static String getEnumeratorQuery() {
		return "SELECT Id, Name, Entity__c, Field_Name__c, Order__c, Type__c FROM SIP_Entity_Field__c";
	}
}
