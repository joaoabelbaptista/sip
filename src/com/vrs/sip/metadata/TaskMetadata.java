/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Task Metadata.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.metadata;

import java.text.ParseException;
import java.util.Date;

import com.vrs.sip.connection.Record;
import com.vrs.sip.connection.Record.IllegalRecordFieldIndex;
import com.vrs.sip.connection.Record.IllegalRecordFieldName;

public class TaskMetadata {
	public String id;
	public String uniqueId;
	public String name;
	public Boolean abortOnFailure;
	public Integer order;
	public String scheduleId;
	public Date lastScheduledDate;
	public String taskFlowId;
	public String taskName;
	public String preProcessingScript;
	public String postProcessingScript;
	public String successEmails;
	public String warningEmails;
	public String failureEmails;
	public Boolean retryIfFail;
	public Integer maxRetryCount;
	
	public TaskMetadata(Record record) throws IllegalRecordFieldIndex, IllegalRecordFieldName, ParseException {
		this.id = record.getFieldByName("Id").getString();
		this.uniqueId = record.getFieldByName("Unique_ID__c").getString();
		this.name = record.getFieldByName("Name").getString();
		this.abortOnFailure = record.getFieldByName("Abort_On_Failure__c").getBoolean();
		this.order = record.getFieldByName("Order__c").getInteger();
		this.scheduleId = record.getFieldByName("Schedule__c").getString();
		this.lastScheduledDate = record.getFieldByName("Last_Scheduled_Date__c").getDate();
		this.taskFlowId = record.getFieldByName("Task_Flow__c").getString();
		this.taskName = record.getFieldByName("Task_Name__c").getString();
		this.preProcessingScript = record.getFieldByName("PreProcessing_Script__c").getString();
		this.postProcessingScript = record.getFieldByName("PostProcessing_Script__c").getString();
		this.successEmails = record.getFieldByName("Success_Emails__c").getString();
		this.warningEmails = record.getFieldByName("Warning_Emails__c").getString();
		this.failureEmails = record.getFieldByName("Failure_Emails__c").getString();
		this.retryIfFail = record.getFieldByName("Retry_If_Fail__c").getBoolean();
		this.maxRetryCount = record.getFieldByName("Max_Retry_Count__c").getInteger();
	}
	
	public static String getEnumeratorQuery() {
		return "SELECT Id, Unique_ID__c, Name, Abort_On_Failure__c, Order__c, Schedule__c, Last_Scheduled_Date__c, Task_Flow__c, Task_Name__c, PreProcessing_Script__c, PostProcessing_Script__c, Success_Emails__c, Warning_Emails__c, Failure_Emails__c, Retry_If_Fail__c, Max_Retry_Count__c FROM SIP_Task__c";
	}
	
	public static String getEntityName() {
		return "SIP_Task__c";
	}
	
	public static String getKeyFieldName() {
		return "Id";
	}
	
	public String toString() {
		return String.format(
				"{ id=%1s, name=%2s, abortOnFailure=%3s, order=%4s, scheduleId=%5s, taskFlowId=%6s, taskName=%7s }",
				id, name, abortOnFailure, order, scheduleId, taskFlowId, taskName
		);
	}
}
