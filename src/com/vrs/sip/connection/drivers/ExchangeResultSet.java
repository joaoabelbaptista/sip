package com.vrs.sip.connection.drivers;

import com.vrs.sip.FileLog;
import com.vrs.sip.connection.*;
import com.vrs.sip.exchange.EmailData;
import com.vrs.sip.exchange.EmailFileAttachment;
import com.vrs.sip.exchange.EmailGrabberUtil;
import com.vrs.sip.exchange.ExchangeQueryCondSet;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.search.FindItemsResults;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Vector;

public class ExchangeResultSet implements IResultSet {

    FileLog log;
    Integer batchSize;
    ExchangeStatement statement;
    ExchangeQueryCondSet dataQueryCond;
    private Integer itemProcessed = 0;
    private Integer counter = 0;
    boolean findItemResultsHasMoreItems = true;
    boolean itemsHasBeenProcessed = false;
    int totalItemsToBeProcessed = 0;
    ItemId anchorId = null;


    public ExchangeResultSet(ExchangeQueryCondSet dataQuery, IStatement statement) throws Exception {
        this.dataQueryCond = dataQuery;
        setStatement(statement);

        FindItemsResults<Item> resultEmails = EmailGrabberUtil.FindItems(getExchangeConnectionService(), dataQueryCond);

        totalItemsToBeProcessed = resultEmails.getTotalCount();

    }

    /**
     * Set the Statement that generated this result set
     *
     * @param statement
     **/
    @Override
    public void setStatement(IStatement statement) throws Exception {

        Integer connectionBatchSize;

        this.statement = (ExchangeStatement) statement;


        connectionBatchSize = ((ExchangeStatement) statement).connection.getConnectionAttributes().getBatchSize();

        if (connectionBatchSize != null && connectionBatchSize > 0) {
            setBatchSize(connectionBatchSize);
        } else {
            setBatchSize(300);
        }

        setLog(statement.getLog());
    }

    public ExchangeService getExchangeConnectionService() {

        ExchangeConnection result = (ExchangeConnection) statement.connection;

        return result.service;
    }

    public void setExchangeDataResults(ExchangeQueryCondSet dataQuery) {

        this.dataQueryCond = dataQuery;
    }

    /**
     * Fetch more rows from the statement
     **/
    @Override
    public List<Record> fetchRows() throws Exception {

        List<Record> result = new Vector<Record>();

        int fetchCount = 0;

        ExchangeQueryCondSet dataQueryCondAux = dataQueryCond.clone();

        dataQueryCondAux.getView().setOffset(0);

        log.info(dataQueryCondAux.getQueryString());

        FindItemsResults<Item> resultEmails = EmailGrabberUtil.FindItems(getExchangeConnectionService(), dataQueryCondAux);

        log.info("resultEmails.getTotalCount() = " + resultEmails.getTotalCount());

        if (resultEmails.getTotalCount() <= 0)
            return result;

        if (hasTheSearchNewModifications(anchorId, resultEmails))
            setViewOffSet(0);

        resultEmails = EmailGrabberUtil.FindItems(getExchangeConnectionService(), dataQueryCond);

        log.debug("view offer = " + dataQueryCond.getView().getOffset());
        log.debug("findItemResultsHasMoreItems depois= " + findItemResultsHasMoreItems);
        log.debug("itemProcessed = " + itemProcessed);
        log.debug(" view offset  = " + dataQueryCond.getView().getOffset());
        log.debug("findItemResultsHasMoreItems = " + findItemResultsHasMoreItems);
        log.debug("dataResults.getTotalCount() = " + resultEmails.getTotalCount());
        log.debug(" dataResults.getNextPageOffset() = " + resultEmails.getNextPageOffset());
        log.debug(" results.getItems().size() = " + resultEmails.getItems().size());

        if (IsMoreEmailsAvailable(resultEmails))
            setViewOffSet(resultEmails.getNextPageOffset());

        if (resultEmails.getItems().size() > 0)
            anchorId = resultEmails.getItems().get(resultEmails.getItems().size() - 1).getId();

        int displayCount = Math.min( Math.min(batchSize, resultEmails.getItems().size()) , resultEmails.getTotalCount());

        for (int i = 0; i < displayCount; i++) {
            List<Field> fieldList = new Vector<Field>();
            Record record = new Record(fieldList);

            itemProcessed++;

            fetchCount++;


            getLog().debug("-------BEGIN--------");

            EmailMessage item = (EmailMessage) resultEmails.getItems().get(i);

            // item.load();

            EmailData emailData = EmailGrabberUtil.ReadItemMessage(getExchangeConnectionService(), item.getId());


            if (emailData != null) {

                fieldList.add(
                        new Field(
                                "EmailId", FieldType.T_STRING, emailData.getItemId()
                        )
                );

                getLog().debug("Email Id = " + emailData.getItemId());


                fieldList.add(
                        new Field(
                                "EmailIDHashSHA256", FieldType.T_STRING, TransformIdEmailToSHA256(emailData.getItemId())
                        )
                );

                String encoded = TransformIdEmailToSHA256(emailData.getItemId());
                getLog().debug("Email Id SHA256 = " + encoded);

                fieldList.add(
                        new Field(
                                "From", FieldType.T_STRING, emailData.getFrom()
                        )
                );

                getLog().debug("Email From = " + item.getFrom());

                fieldList.add(
                        new Field(
                                "Subject", FieldType.T_STRING, emailData.getSubject()
                        )
                );
                log.info("## SUBJECT ##:" + emailData.getSubject());
                getLog().debug("Subject = " + emailData.getSubject());

                fieldList.add(
                        new Field(
                                "Body", FieldType.T_STRING, emailData.getBody()
                        )
                );

                getLog().debug("Body = " + emailData.getBody());

                fieldList.add(
                        new Field(
                                "InternetEmailId", FieldType.T_STRING, emailData.getInternetMessageId()
                        )
                );

                fieldList.add(
                        new Field(
                                "IsTheBodyHTML", FieldType.T_BOOLEAN, emailData.isBodyHTML()
                        )
                );

                //TODO: Validate If would be necessary to add one copy of the email into salesforce.?
                fieldList.add(
                        new Field(
                                "FileAttachments", FieldType.T_FILEATTACHMENT_LIST, ReadFilesAttachmentsFromEmail(emailData.getEmailMessage())
                        )
                );

                log.info("emailData.getDateTimeReceived - Unix Format() =" + emailData.getDateTimeReceivedToUnixFormat());
               // log.info("emailData.getDateTimeReceived() GMT =" + emailData.getDateTimeReceivedGMT());


                fieldList.add(
                        new Field(
                                "DateTimeReceived", FieldType.T_STRING, emailData.getDateTimeReceivedToUnixFormat()
                        )
                );

                fieldList.add(
                        new Field(
                                "To", FieldType.T_STRING, emailData.getTo()
                        )
                );

                getLog().debug("-------END--------");

                record.setFieldList(fieldList);
                result.add(record);
            }


        }


        return result;
    }




    private List<EmailFileAttachment> ReadFilesAttachmentsFromEmail(EmailMessage emailMessage) throws Exception {


        // Request all the attachments on the email message. This results in a GetItem operation call to EWS.
        emailMessage.load(new PropertySet(EmailMessageSchema.Attachments, ItemSchema.MimeContent, ItemSchema.Subject));

        List<EmailFileAttachment> attachments = new ArrayList<EmailFileAttachment>();

        for (Attachment attachment : emailMessage.getAttachments()) {
            if (attachment instanceof FileAttachment) {
                FileAttachment fileAttachment = (FileAttachment) attachment;

                //System.out.println(fileAttachment);

                // Load the file attachment into memory. This gives you access to the attachment content, which
                // is a byte array that you can use to attach this file to another item. This results in a GetAttachment operation
                // call to EWS.

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                fileAttachment.load(outputStream);

                attachments.add(new EmailFileAttachment(fileAttachment.getName(), outputStream));

            }

        }


       // ByteArrayOutputStream emailMessageAsFileEML = new ReadEmailMimeContentAsByteStream(emailMessage).invoke();

       // attachments.add(new EmailFileAttachment(emailMessage.getSubject() + ".eml", emailMessageAsFileEML));


        return attachments;

    }

    private String TransformIdEmailToSHA256(String emailId) throws NoSuchAlgorithmException, ServiceLocalException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(emailId.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(hash);
    }

    private void validateIfAllItemsHaveBeenProcessed(FindItemsResults<Item> results) {
        itemsHasBeenProcessed = itemProcessed == totalItemsToBeProcessed || itemProcessed == results.getTotalCount();
    }

    private boolean hasTheSearchNewModifications(ItemId anchorId, FindItemsResults<Item> results) throws ServiceLocalException {

        if (anchorId != null) {
            // Check the first result to make sure it matches
            // the last result (anchor) from the previous page.
            // If it doesn't, that means that something was added
            // or deleted since you started the search.
            if (results.getItems().get(0).getId() != anchorId) {
                return true;
            }
        }

        return false;
    }

    private void setViewOffSet(int offset) {
        dataQueryCond.getView().setOffset(offset);
        // dataQueryCond.getView().setOffset(dataQueryCond.getView().getOffset() + batchSize);
    }

    private boolean IsMoreEmailsAvailable(FindItemsResults<Item> results) {
        return results.getNextPageOffset() != null;
        // return results.isMoreAvailable() || results.getNextPageOffset() != null || itemProcessed < results.getTotalCount() - 1 ;
    }

    /**
     * Fetch all rows from the statement
     **/
    @Override
    public List<Record> fetchAllRows() throws Exception {
        List<Record> allRecords = new Vector<Record>();
        List<Record> fetchRecords = null;

        do {
            fetchRecords = fetchRows();

            if (fetchRecords != null && fetchRecords.isEmpty() == false) {
                allRecords.addAll(fetchRecords);
            }
        } while (fetchRecords != null && fetchRecords.isEmpty() == false);

        return allRecords;
    }

    /**
     * Set the batch size to be used by fetchRows()
     *
     * @param batchSize
     **/
    @Override
    public void setBatchSize(Integer batchSize) throws Exception {

        this.batchSize = batchSize;

    }

    /**
     * Set the Logger
     *
     * @param log
     **/
    @Override
    public void setLog(FileLog log) {
        this.log = log;
    }

    /**
     * Get the Logger
     **/
    @Override
    public FileLog getLog() {
        return log;
    }

    private class ReadEmailMimeContentAsByteStream {
        private EmailMessage emailMessage;

        public ReadEmailMimeContentAsByteStream(EmailMessage emailMessage) {
            this.emailMessage = emailMessage;
        }

        public ByteArrayOutputStream invoke() throws ServiceLocalException {
            ByteArrayOutputStream emailMessageAsFileEML = new ByteArrayOutputStream(emailMessage.getMimeContent().getContent().length);
            emailMessageAsFileEML.write(emailMessage.getMimeContent().getContent(), 0, emailMessage.getMimeContent().getContent().length);
            return emailMessageAsFileEML;
        }
    }
}
