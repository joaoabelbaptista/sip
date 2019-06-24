/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Task Flow Metadata.
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

public class TaskFlowMetadata {
	public String id;
	public String uniqueId;
	public String name;
	public String flowName;
	public String scheduleId;
	public Date lastScheduledDate;
	public String successEmails;
	public String warningEmails;
	public String failureEmails;
	public Boolean retryIfFail;
	public Integer maxRetryCount;
	
	public TaskFlowMetadata(Record record) throws IllegalRecordFieldIndex, IllegalRecordFieldName, ParseException {
		if (record != null) {
			this.id = record.getFieldByName("Id").getString();
			this.uniqueId = record.getFieldByName("Unique_ID__c").getString();
			this.name = record.getFieldByName("Name").getString();
			this.flowName = record.getFieldByName("Flow_Name__c").getString();
			this.scheduleId = record.getFieldByName("Schedule__c").getString();
			this.lastScheduledDate = record.getFieldByName("Last_Scheduled_Date__c").getDate();
			this.successEmails = record.getFieldByName("Success_Emails__c").getString();
			this.warningEmails = record.getFieldByName("Warning_Emails__c").getString();
			this.failureEmails = record.getFieldByName("Failure_Emails__c").getString();
			this.retryIfFail = record.getFieldByName("Retry_If_Fail__c").getBoolean();
			this.maxRetryCount = record.getFieldByName("Max_Retry_Count__c").getInteger();
		}
	}
	
	public static String getEntityName() {
		return "SIP_Task_Flow__c";
	}
	
	public static String getKeyFieldName() {
		return "Id";
	}
	
	public static String getEnumeratorQuery() {
		return "SELECT Id, Unique_ID__c, Name, Flow_Name__c, Schedule__c, Last_Scheduled_Date__c, Success_Emails__c, Warning_Emails__c, Failure_Emails__c, Retry_If_Fail__c, Max_Retry_Count__c FROM SIP_Task_Flow__c";
	}
}
