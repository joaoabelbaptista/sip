package com.vrs.sip.metadata;

import com.vrs.sip.connection.Record;

import java.text.ParseException;

public class TaskFlowsTasksMetadata {

   public String id;
   public String taskId;
   public String taskFlowId;
   public String name;
   public int order;

    public TaskFlowsTasksMetadata(Record record) throws Record.IllegalRecordFieldIndex, Record.IllegalRecordFieldName, ParseException {
        if (record != null) {
            this.id = record.getFieldByName("Id").getString();
            this.taskId = record.getFieldByName("SIP_Task__c").getString();
            this.name = record.getFieldByName("Name").getString();
            this.taskFlowId = record.getFieldByName("SIP_Task_Flow__c").getString();
            this.order =  record.getFieldByName("Task_Order__c").getInteger() == null ? 0 : record.getFieldByName("Task_Order__c").getInteger();

        }
    }


    public static String getEnumeratorQuery() {
        return "SELECT Id, Name, SIP_Task__c, SIP_Task_Flow__c, Task_Order__c FROM SIP_Task_Flows_Tasks__c order by Task_Order__c asc";
    }

    public static String getEntityName() {
        return "SIP_Task_Flows_Tasks__c";
    }

    public static String getKeyFieldName() {
        return "Id";
    }

    public String toString() {
        return String.format(
                "{ id=%1s, name=%2s, taskId=%3s, taskFlowId=%4s, order=%5s}",
                id, name, taskId, taskFlowId, order
        );
    }


}
