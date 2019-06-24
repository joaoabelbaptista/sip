/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Entity Metadata.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.metadata;

import com.vrs.sip.connection.Record;
import com.vrs.sip.connection.Record.IllegalRecordFieldIndex;
import com.vrs.sip.connection.Record.IllegalRecordFieldName;

public class EntityMetadata {
	public String id;
	public String name;
	public String entityName;
	public String entityType;
	public String taskStepId;
	
	public EntityMetadata(Record record) throws IllegalRecordFieldIndex, IllegalRecordFieldName {
		if (record != null) {
			this.id = (String)record.getFieldByName("Id").getValue();
			this.name = (String)record.getFieldByName("Name").getValue();
			this.entityName = (String)record.getFieldByName("Entity_Name__c").getValue();
			this.entityType = (String)record.getFieldByName("Entity_Type__c").getValue();
			this.taskStepId = (String)record.getFieldByName("Task_Step__c").getValue();
		}
	}
	
	public static String getEnumeratorQuery() {
		return "SELECT Id, Name, Entity_Name__c, Entity_Type__c, Task_Step__c FROM SIP_Entity__c";
	}
}
