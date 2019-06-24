package com.vrs.sip.connection.drivers;


import com.vrs.sip.FileLog;
import com.vrs.sip.connection.*;
import com.vrs.sip.exchange.Email;
import com.vrs.sip.exchange.EmailFileAttachment;
import com.vrs.sip.exchange.EmailGrabberUtil;
import com.vrs.sip.exchange.ExchangeQueryCondSet;
import com.vrs.sip.task.Schedule;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.search.FindItemsResults;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vrs.sip.exchange.EmailGrabberUtil.FindItemsByQueryString;
import static com.vrs.sip.exchange.EmailGrabberUtil.SendEmailsWithAttachment;

public class ExchangeStatement implements IStatement {

    FileLog log;
    Schedule schedule;

    IConnection connection;


    /**
     * Set the connector associated with the statement
     *
     * @param connection
     **/
    @Override
    public void setConnection(IConnection connection) throws Exception {

        this.connection = connection;

        setLog(connection.getLog());

    }


    public ExchangeService getExchangeService() {

       return  (ExchangeService) connection.getImplConnection();

    }


    /**
     * Execute a query
     *
     * @param query
     **/
    @Override
    public IResultSet executeQuery(String query) throws Exception {

        ExchangeResultSet resultSet;

        if (query == null )
            query = "Kind:email";

        String directory = connection.getConnectionAttributes().getDirectory() == null ? "Inbox" : connection.getConnectionAttributes().getDirectory();

        int batchSize = connection.getConnectionAttributes().getBatchSize() == null ? 10 : connection.getConnectionAttributes().getBatchSize().intValue();

        ExchangeQueryCondSet result = FindItemsByQueryString(getExchangeService(), directory, query, batchSize);

        resultSet = new ExchangeResultSet(result,this);

        return resultSet;
    }

    /**
     * Execute an insert, update, delete or upsert statement
     * Returns total number of errors occurred.
     *
     * @param operationType
     * @param batchSize
     * @param entityName
     * @param entityFieldNameList
     * @param recordList
     * @param keyFieldList
     */
    @Override
    public Integer executeOperation(StatementOperationType operationType, Integer batchSize, String entityName, List<String> entityFieldNameList, List<Record> recordList, List<String> keyFieldList) throws Exception {

        Integer result = 0;

        switch (operationType) {

            case SendEmailWithAttachment:

               result = executeSendEmailWithAttachment(operationType, batchSize, entityName,entityFieldNameList, recordList, keyFieldList);
               break;

        }

         return result;
    }



     Integer executeSendEmailWithAttachment(StatementOperationType operationType, Integer batchSize, String entityName, List<String> entityFieldNameList, List<Record> recordList, List<String> keyFieldList) throws Exception {

        String[] idArray;
        String filename;
        ArrayList<EmailFileAttachment> filesToSend = new ArrayList<EmailFileAttachment>();
        String upsertKeyField;

        Integer counter;
        Integer result = 0;

        if (recordList == null || recordList.isEmpty()) {
            return result;
        }

        if (entityFieldNameList!= null  && entityFieldNameList.size() < 5){
            log.info("************************************************");
            log.info("Warning: The Sip Entity Fields for the File Virtual Entity has not been created.");
            log.info("The File Entity had to have the following Fields:");

            log.info("***** Name:directory         Type:String *******");
            log.info("***** Name:filename          Type:String *******");
            log.info("***** Name:createdDate       Type:String *******");
            log.info("***** Name:lastModifiedDate  Type:String *******");
            log.info("***** Name:contentBytes      Type:Byte *********");
            log.info("************************************************");

            return -1 * recordList.size();

        }



        counter = 0;
        for (Record record : recordList) {

            EmailFileAttachment emailFileAttachment = new EmailFileAttachment();
            byte[] fileData;

            for (String fieldName : entityFieldNameList) {



                if (fieldName.equalsIgnoreCase("filename")) {
                    filename = record.getFieldByName(fieldName).getString();

                    log.debug("filename = " + filename);

                    emailFileAttachment.setName(filename);
                }
                else if(fieldName.equalsIgnoreCase("contentBytes")){

                    fileData = record.getFieldByName(fieldName).geContentBytes();

                    log.debug("filedata = " + fileData.length);

                    emailFileAttachment.setFileBytes(fileData);

                }

            }

            filesToSend.add(emailFileAttachment);


            counter++;
        }


        upsertKeyField = null;
        if (keyFieldList != null && keyFieldList.isEmpty() == false) {
            upsertKeyField = keyFieldList.get(0);
        }


        switch (operationType) {

            case SendEmailWithAttachment:

                if (getSchedule() == null)
                    return 0;

                if (counter > 0)
                    result = exchangeSendEmailWithAttachments(batchSize, filesToSend);
                else
                    log.info("Not files hava been found to be sent");

                break;


        }

        return result;
    }





    public int exchangeSendEmailWithAttachments(Integer batchSize, ArrayList<EmailFileAttachment> emailFileAttachments) throws Exception {

       int result;
       Email email = new Email();

       if (!getSchedule().getEmailTo().equalsIgnoreCase("") || !getSchedule().getEmailCc().equalsIgnoreCase("") || !getSchedule().getEmailBcc().equalsIgnoreCase("") ) {

           email.setTo(getSchedule().getEmailTo());
           email.setCc(getSchedule().getEmailCc());
           email.setBcc(getSchedule().getEmailBcc());
           email.setSubject(getSchedule().EmailSubject);
           email.setBody(getSchedule().EmailBody);
           email.setEmailFileAttachments(emailFileAttachments);

           try {

               result = SendEmailsWithAttachment(getExchangeService(), email);

           }
           catch(Exception ex) {

               getLog().info("Erro inside of The Operation ExchangeSendEmailWithAttachments", ex);

               return 1;
           }

           return result;
       }
       else
           getLog().info("The Operation ExchangeSendEmailWithAttachments was cancelled because the fields To;CC,Bcc on Schedule don't have any values.");

       return 0;
    }

    /**
     * Execute a Stored Procedure//Function
     *
     * @param call
     * @param inputParameterList
     **/
    @Override
    public void executeCall(String call, List<Object> inputParameterList) throws Exception {
        //throw new RuntimeException("The method executeCall is not supported by " + this.getClass().getName());

       if (inputParameterList.size() == 0)
           return ;

        int batchSize = connection.getConnectionAttributes().getBatchSize() == null ? 300 : connection.getConnectionAttributes().getBatchSize().intValue();

        log.info("batch size =" + batchSize );

        switch (call.toLowerCase()) {

            case "func::moveemailstofolderbytagid": //TODO: In the future ...Load The Class dynamically.


                List<Record> outputRecords = (List<Record>) inputParameterList.get(0);

                //Move the Email to the Folder Processed//

                for( Record record: outputRecords){

                    String internetEmailId = record.getFieldByName("InternetEmailId").getString();

                    String emailId = record.getFieldByName("EmailId").getString();

                    String subject = record.getFieldByName("Subject").getString();

                    log.info(String.format("The Email with the Subject {%s}  \n  and Internet Mail Id %s have been processed %s", subject, internetEmailId, record.getHasError()? "with error (Check the folder EMAIL_GRABBER_ERROR in the Exchange Server)." : "with success"  ));

                    if (record.getHasError()) {

                        String folderDest = "01.EMAIL_GRABBER_ERROR";

                        if ( EmailGrabberUtil.FindFolderIdByName(getExchangeService(),  WellKnownFolderName.Inbox, folderDest) == null)
                             EmailGrabberUtil.CreateFolder(getExchangeService(), WellKnownFolderName.Inbox, folderDest);

                        EmailGrabberUtil.MoveBulkEmailsByIds(getExchangeService(), Arrays.<String>asList(emailId), folderDest); //TODO: Search the FolderName

                    }
                    else
                    {

                        String folderDest = calculateFolderDestinationToMove(subject);

                        FolderId folderDestId =  EmailGrabberUtil.FindFolderIdByName(getExchangeService(),  WellKnownFolderName.Inbox, folderDest);

                        if ( folderDestId == null )
                            EmailGrabberUtil.CreateFolder(getExchangeService(), WellKnownFolderName.Inbox, folderDest);

                        EmailGrabberUtil.MarkItemAsRead(getExchangeService(),emailId);
                        EmailGrabberUtil.MoveBulkEmailsByIds(getExchangeService(), Arrays.<String>asList(emailId), folderDest); //TODO: Search the FolderName

                        log.info(" The email has been moved to the folder = " + folderDest);

                    }


                }

                break;

        }
    }


     public String calculateFolderDestinationToMove(String subject){

         String folderDest = "01.EMAIL_GRABBER_NO_MATCH";

         Pattern patternLaunchLetter = Pattern.compile("(?i)Launch Letter_[\\w]+");
         Matcher launchLetterMatcher = patternLaunchLetter.matcher(subject);
         Boolean hasLaunchLetterMatched = launchLetterMatcher.find();

         Pattern patternTADIGLetter = Pattern.compile("_[\\w]{5}_");
         Matcher hasLetterTADIGMatched = patternTADIGLetter.matcher(subject);

         Pattern patternCLLNameCase = Pattern.compile("CLL-[\\w]{7}");
         Matcher cLLNameMatcher = patternCLLNameCase.matcher(subject);
         Boolean hasCLLNameMatched = cLLNameMatcher.find();

         Pattern patternCaseRef= Pattern.compile("ref:.+:ref");
         Matcher caseRefMatcher = patternCaseRef.matcher(subject);
         Boolean hasCaseRefMatched = caseRefMatcher.find();

         Boolean emailHasMatchedForCLLNameOrCaseRefOrLaunchLetter = hasCLLNameMatched || hasCaseRefMatched || hasLaunchLetterMatched;

         if( emailHasMatchedForCLLNameOrCaseRefOrLaunchLetter && hasLetterTADIGMatched.find() )
           folderDest = hasLetterTADIGMatched.group(0).replaceAll("_","");

         log.info("Folder Dest = " + folderDest);

         return folderDest;

     }


    /**
     * Execute a File Upload
     *
     * @param parentObject
     * @param parentId
     * @param parentIdIsExternal
     * @param externalIdField
     * @param filename
     * @param name
     * @param description
     * @param contentType
     * @param isPrivate
     **/
    @Override
    public void executeFileUpload(String parentObject, String parentId, Boolean parentIdIsExternal, String externalIdField, String filename, String name, String description, String contentType, Boolean isPrivate) throws Exception {
        throw new RuntimeException("File Upload not implemented by " + this.getClass().getName());
    }

    /**
     * Execute a fetch records of the entity
     *
     * @param entity
     * @param filterClause
     **/
    @Override
    public IResultSet fetchRecords(String entity, String filterClause) throws Exception {
        throw new RuntimeException("The method fetchRecords is not supported by " + this.getClass().getName());
    }

    /**
     * Execute a TRUNCATE of the entity
     *
     * @param entity
     **/
    @Override
    public Integer executeTruncate(String entity) throws Exception {
        throw new RuntimeException("The method executeTruncate is not supported by " + this.getClass().getName());
    }

    /**
     * Close the Statement
     **/
    @Override
    public void close() throws Exception {

    }

    /**
     * Set the Logger
     *
     * @param log
     */
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

    /**
     * Set a specific attribute only used by source/target CSV files
     *
     * @param fieldSeparator
     **/
    @Override
    public void setFieldSeparator(char fieldSeparator) throws Exception {

    }

    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
}
