/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Schedule Metadata.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.metadata;

import com.vrs.sip.connection.Record;
import com.vrs.sip.connection.Record.IllegalRecordFieldIndex;
import com.vrs.sip.connection.Record.IllegalRecordFieldName;

import java.util.Date;

public class ScheduleMetadata {

    public static enum Status {
        NOT_STARTED("Not Started"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed")
        ;

        private final String text;

        /**
         * @param text
         */
        Status(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }
    }

	public String id;
	public String name;
	public String scheduleType;
	public String recurrencyType;
	public String recurrence;
	public Date startDate;
	public Date endDate;

	/*Properties for The Sending Email*/
	public String EmailTo;
	public String EmailCc;
	public String EmailBcc;
	public String EmailSubject;
    public String EmailBody;
    public String Var1;
    public String Var2;
    public String Var3;
    public String Var4;
    public String Var5;
    public String status;
    public String taskFlowId;
    public String taskId;




	/** Get a new Schedule from a Record
	 * @throws IllegalRecordFieldName 
	 * @throws IllegalRecordFieldIndex **/
	public ScheduleMetadata(Record record) throws IllegalRecordFieldIndex, IllegalRecordFieldName {
		if (record != null) {
			this.id = (String)record.getFieldByName("Id").getValue();
			this.name = (String)record.getFieldByName("Name").getValue();
			this.scheduleType = (String)record.getFieldByName("Schedule_Type__c").getValue();
			this.recurrencyType = (String)record.getFieldByName("Recurrence_Type__c").getValue();
			this.recurrence = (String)record.getFieldByName("Recurrence__c").getValue();
			this.startDate = (Date)record.getFieldByName("Start_Date__c").getValue();
			this.endDate = (Date)record.getFieldByName("End_Date__c").getValue();

            this.EmailTo = (String)record.getFieldByName("Email_To__c").getValue();
            this.EmailCc = (String)record.getFieldByName("Email_CC__c").getValue();
            this.EmailBcc = (String)record.getFieldByName("Email_BCC__c").getValue();
            this.EmailSubject = (String)record.getFieldByName("Email_Subject__c").getValue();
            this.EmailBody = (String)record.getFieldByName("Email_Body__c").getValue();

            this.Var1 = (String)record.getFieldByName("Var_1__c").getValue();
            this.Var2 = (String)record.getFieldByName("Var_2__c").getValue();
            this.Var3 = (String)record.getFieldByName("Var_3__c").getValue();
            this.Var4 = (String)record.getFieldByName("Var_4__c").getValue();
            this.Var5 = (String)record.getFieldByName("Var_5__c").getValue();

            this.status = (String)record.getFieldByName("Status__c").getValue();

            this.taskFlowId = (String)record.getFieldByName("SIP_Task_Flow_Id__c").getValue();
            this.taskId = (String)record.getFieldByName("SIP_Task_Id__c").getValue();
		}
	}

    public static String getEntityName() {
        return "SIP_Schedule__c";
    }


    /** Get the Enumerator Query **/
	public static String getEnumeratorQuery() {
		return "SELECT Id, Name, Schedule_Type__c, Recurrence_Type__c, Recurrence__c, Start_Date__c, End_Date__c, Email_To__c, Email_CC__c, Email_BCC__c , Email_Subject__c, Email_Body__c, Var_1__c, Var_2__c, Var_3__c, Var_4__c, Var_5__c, Status__c, SIP_Task_Id__c, SIP_Task_Flow_Id__c FROM SIP_Schedule__c  where Status__c !='Completed' order by LastModifiedDate asc ";
	}
}
