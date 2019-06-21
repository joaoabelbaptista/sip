/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Transformation Metadata.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.metadata;

import com.vrs.sip.connection.Record;
import com.vrs.sip.connection.Record.IllegalRecordFieldIndex;
import com.vrs.sip.connection.Record.IllegalRecordFieldName;

public class TransformationMetadata {
	public String id;
	public String name;
	public String taskStepId;
	public String targetField;
	public String transformation;
	public Integer order;
	
	public TransformationMetadata(Record record) throws IllegalRecordFieldIndex, IllegalRecordFieldName {
		if (record != null) {
			this.id = (String)record.getFieldByName("Id").getValue();
			this.name = (String)record.getFieldByName("Name").getValue();
			this.taskStepId = (String)record.getFieldByName("Task_Step__c").getValue();
			this.targetField = (String)record.getFieldByName("Target_Field__c").getValue();
			this.transformation = (String)record.getFieldByName("Transformation__c").getValue();
			this.order = Integer.valueOf(record.getFieldByName("Order__c").getDouble().intValue());
		}
	}
	
	public static String getEnumeratorQuery() {
		return "SELECT Id, Name, Task_Step__c, Target_Field__c, Transformation__c, Order__c FROM SIP_Transformation__c";
	}
}
