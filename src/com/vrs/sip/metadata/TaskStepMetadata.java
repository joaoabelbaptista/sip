/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Task Step Metadata.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.metadata;

import com.vrs.sip.connection.Record;
import com.vrs.sip.connection.Record.IllegalRecordFieldIndex;
import com.vrs.sip.connection.Record.IllegalRecordFieldName;

public class TaskStepMetadata {
	public String id;
	public String name;
	public String sourceConnectionId;
	public String stepType;
	public String targetConnectionId;
	public String taskId;
	public String operation;
	public String operationKeyFieldList;
	public Integer order;
	public Boolean truncateTarget;
	public Integer batchSize;
	public String fieldSeparator;
	
	public TaskStepMetadata(Record record) throws IllegalRecordFieldIndex, IllegalRecordFieldName {
		if (record != null) {
			this.id = (String)record.getFieldByName("Id").getValue();
			this.name = (String)record.getFieldByName("Name").getValue();
			this.sourceConnectionId = (String)record.getFieldByName("Source_Connection__c").getValue();
			this.stepType = (String)record.getFieldByName("Step_Type__c").getValue();
			this.targetConnectionId = (String)record.getFieldByName("Target_Connection__c").getValue();
			this.taskId = (String)record.getFieldByName("Task__c").getValue();
			this.operation = record.getFieldByName("Operation__c").getString();
			this.operationKeyFieldList = record.getFieldByName("Operation_Key_Field_List__c").getString();
			this.order = record.getFieldByName("Order__c").getInteger();
			this.truncateTarget = record.getFieldByName("Truncate_Target__c").getBoolean();
			this.batchSize = record.getFieldByName("Batch_Size__c").getInteger();
			this.fieldSeparator = record.getFieldByName("Field_Separator__c").getString();
		}
	}
	
	public static String getEnumeratorQuery() {
		return "SELECT Id, Name, Source_Connection__c, Step_Type__c, Target_Connection__c, Task__c, Operation__c, Operation_Key_Field_List__c, Order__c, Truncate_Target__c, Batch_Size__c, Field_Separator__c FROM SIP_Task_Step__c";
	}
}
