package com.vrs.sip.connection.drivers;

import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.vrs.sip.connection.FieldType;
import com.vrs.sip.connection.Record;
import com.vrs.sip.connection.StatementOperationType;
import com.vrs.sip.exchange.EmailFileAttachment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SalesforceExchangeStatement extends SalesforceStatement {


    @Override
    public Integer executeOperation(StatementOperationType operationType, Integer batchSize, String entityName, List<String> entityFieldNameList, List<Record> recordList, List<String> keyFieldList) throws Exception {
        SObject[] sobjectArray;
        String[] idArray;
        SaveResult[] saveResult;
        DeleteResult[] deleteResult;
        UpsertResult[] upsertResult;
        String upsertKeyField;

        Integer counter;
        Integer result = 0;

        if (recordList == null || recordList.isEmpty()) {
            return result;
        }

       List<String>  entityFieldNameListFiltered = entityFieldNameList.stream().filter(p -> !p.toLowerCase().contains("fileattachments")).collect(Collectors.toList());



        sobjectArray = new SObject[recordList.size()];
        idArray = new String[recordList.size()];

        counter = 0;
        for (Record record : recordList) {
            String id;
            SObject sobjectRecord = new SObject(entityName);
            List<String> sobjectFieldsNull = new ArrayList<String>();

            id = null;
            for (String fieldName : entityFieldNameListFiltered) {
                if (fieldName.equalsIgnoreCase("id")) {
                    id = record.getFieldByName(fieldName).getString();
                }

                if (record.getFieldByName(fieldName).getValue() == null) {
                    sobjectFieldsNull.add(fieldName);
                }

                if (record.getFieldByName(fieldName).getFieldType() == FieldType.T_DATE) {
                    Date dt = record.getFieldByName(fieldName).getDate();
                    java.util.GregorianCalendar dtValue = null;

                    if (dt != null) {
                        dtValue = getGregorianCalendarDate(dt);
                    }
                    sobjectRecord.setField(fieldName, dtValue);
                } else {
                    sobjectRecord.setSObjectField(fieldName, record.getFieldByName(fieldName).getValue());
                }

                if (sobjectFieldsNull.isEmpty() == false) {
                    String[] nullFields = new String[sobjectFieldsNull.size()];

                    for (int i = 0; i < sobjectFieldsNull.size(); i++) {
                        nullFields[i] = sobjectFieldsNull.get(i);
                    }

                    sobjectRecord.setFieldsToNull(nullFields);
                }
            }

            sobjectArray[counter] = sobjectRecord;
            idArray[counter] = id;

            counter++;
        }

        upsertKeyField = null;
        if (keyFieldList != null && keyFieldList.isEmpty() == false) {
            upsertKeyField = keyFieldList.get(0);
        }

        switch (operationType) {
            case Insert:
                saveResult = salesforceCreate(batchSize, sobjectArray);

                result = checkOperationResultErrors(operationType, saveResult);

                break;

            case Delete:
                deleteResult = salesforceDelete(batchSize, idArray);

                result = checkOperationResultErrors(operationType, deleteResult);

                break;

            case Update:
                saveResult = salesforceUpdate(batchSize, sobjectArray);

                result = checkOperationResultErrors(operationType, saveResult);

                break;

            case Upsert:
                upsertResult = salesforceUpsert(batchSize, upsertKeyField, sobjectArray);

                result = checkOperationResultErrors(operationType, upsertResult);

                break;

            case InsertWithAttachments:

                saveResult = uploadRegistersWithAttachments(operationType, batchSize, recordList, sobjectArray);

                result = checkOperationResultErrors(operationType, saveResult);

                break;
        }

        return result;
    }

    private SaveResult[] uploadRegistersWithAttachments(StatementOperationType operationType, Integer batchSize, List<Record> recordList, SObject[] sobjectArray) throws ConnectionException, Record.IllegalRecordFieldIndex, Record.IllegalRecordFieldName {

        int index = -1;


        SaveResult[] saveResult = salesforceCreate(batchSize, sobjectArray );

       // Integer result = checkOperationResultErrors(operationType, saveResult);

        //log.info("uploadRegistersWithAttachments - Errors = " + result);

        for (SaveResult salesforceResult: saveResult ){

            log.info("Email has been processed by Salesforce with success =" + salesforceResult.isSuccess());

            index++;

            if (salesforceResult.isSuccess()) {

               if (isHavingFileattachments(recordList, index)) {

                   List<EmailFileAttachment> fileAttachments = (List<EmailFileAttachment>) recordList.get(index).getFieldByName("FileAttachments").getValue();

                   InsertFilesAttachment(salesforceResult, fileAttachments);
               }

            }
            else
                recordList.get(index).setHasError(true);


        }

        SaveResult[] saveResultUpdated = UpdateSalesforceObjectToActivateTriggerUpdate(batchSize, sobjectArray, saveResult);


        System.arraycopy( saveResultUpdated, 0, saveResult, 0, saveResultUpdated.length );

        for(int i=0; i < saveResultUpdated.length; i++)
            recordList.get(i).setHasError(!saveResultUpdated[i].isSuccess());

        return saveResult;
    }

    private SaveResult[] UpdateSalesforceObjectToActivateTriggerUpdate(Integer batchSize, SObject[] sobjectArray, SaveResult[] saveResult) throws ConnectionException {
        /*

        SObject[] sobjectToBeUpdatedArray = new SObject[sobjectArray.length];

        for(int i=0; i < sobjectToBeUpdatedArray.length; i++){

            SObject sobjectRecord = new SObject(sobjectArray[i].getType());

            sobjectRecord.setId(saveResult[i].getId());
            sobjectToBeUpdatedArray[i] = sobjectRecord;
        }

        saveResult = salesforceUpdate(batchSize, sobjectToBeUpdatedArray );
        return saveResult;
        */

        SaveResult[] saveResultUpdated;

        ArrayList<SObject> sObjectsToUpdate = new ArrayList<SObject>();

        for(int i=0; i < saveResult.length; i++){

            SObject sobjectRecord = new SObject(sobjectArray[i].getType());

            if (saveResult[i].isSuccess()) {
                sobjectRecord.setId(saveResult[i].getId());
                sObjectsToUpdate.add(sobjectRecord);
            }

        }

        saveResultUpdated = salesforceUpdate(batchSize,  sObjectsToUpdate.toArray(new SObject[0]));

        return saveResultUpdated;

    }

    private boolean isHavingFileattachments(List<Record> recordList, int index) {
        return recordList.get(index).getFieldList().stream().anyMatch( x -> x.getName().toLowerCase().contains("fileattachments"));
    }

    private void InsertFilesAttachment(SaveResult salesforceResult, List<EmailFileAttachment> fileAttachments) throws ConnectionException {

        SObject attachment = new SObject();

        for ( EmailFileAttachment emailFileAttachment:fileAttachments){

            attachment.setType("Attachment");
            attachment.setSObjectField("parentId", salesforceResult.getId());
            attachment.setSObjectField("name", emailFileAttachment.getName());


            attachment.setSObjectField("body", emailFileAttachment.toByteArray());

            SaveResult[] filesAttachmentResultList = implConnection.create(new SObject[] { attachment });

            if (filesAttachmentResultList != null) {
                for (SaveResult fileAttachmentResult : filesAttachmentResultList) {
                    if (fileAttachmentResult.isSuccess() == false) {
                        Error[] errors = fileAttachmentResult.getErrors();

                        if (errors != null) {
                            String errorMessage = getErrorMessage(errors);

                            getLog().debug("Doc Id: " + salesforceResult.getId());

                            getLog().debug("FileAttach: " + errorMessage);

                            throw new RuntimeException("FileAttach: " + errorMessage);
                        }
                    }
                }
            }

        }
    }


}
