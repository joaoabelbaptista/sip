package com.vrs.sip.exchange;


import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.enumeration.property.MapiPropertyType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.ComparisonMode;
import microsoft.exchange.webservices.data.core.enumeration.search.ContainmentMode;
import microsoft.exchange.webservices.data.core.enumeration.search.FolderTraversal;
import microsoft.exchange.webservices.data.core.enumeration.search.SortDirection;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.enumeration.service.ServiceResult;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.response.MoveCopyItemResponse;
import microsoft.exchange.webservices.data.core.response.ServiceResponseCollection;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.misc.OutParam;
import microsoft.exchange.webservices.data.property.complex.*;
import microsoft.exchange.webservices.data.property.definition.ExtendedPropertyDefinition;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.FolderView;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class EmailGrabberUtil {

    public static void CreateFolder(ExchangeService service, FolderId parentFolderId, String folderName) throws Exception {

        Folder newFolder = new Folder(service);

        newFolder.setDisplayName(folderName);

        newFolder.save(parentFolderId);

    }

    public static void CreateFolder(ExchangeService service, WellKnownFolderName wellKnownFolderName, String folderName) throws Exception {

        Folder newFolder = new Folder(service);

        newFolder.setDisplayName(folderName);

        newFolder.save(wellKnownFolderName);

    }

    public static boolean MoveBulkEmailsByIds(ExchangeService service, List<String> itemsId, String destinationFolderName) throws Exception {


        FolderId   destinationFolderId = FindFolderIdByName(service, destinationFolderName );


        if (itemsId == null || service == null || destinationFolderId == null)
            return false;

        if (itemsId.size() == 0)
            return false;

        List<ItemId> itemIds = new ArrayList<ItemId>();

        for (String id : itemsId) {
            try {
                itemIds.add( ItemId.getItemIdFromString(id));
            } catch (ServiceLocalException e) {
                throw e;
            }
        }


        // You can move items in a batch request. This will result in MoveItem operation call to EWS.
        // Unlike the EmailMessage.Move method, the batch request takes a collection of item identifiers,
        // which identify the items that will be moved.
        ServiceResponseCollection<MoveCopyItemResponse> responses = service.moveItems(itemIds, destinationFolderId);

        if (responses.getOverallResult() == ServiceResult.Success) {
            return true;
        } else {
            throw new Exception("The batch move of the email message items was not successful.");
        }
    }

    public static boolean MoveBulkEmails(ExchangeService service, ArrayList<Item> items, String destinationFolderName) throws Exception {


        FolderId   destinationFolderId = FindFolderIdByName(service, destinationFolderName );


        if (items == null || service == null || destinationFolderId == null)
            return false;

        if (items.size() == 0)
            return false;

        List<ItemId> itemIds = new ArrayList<ItemId>();

        for (Item x : items) {
            try {
                itemIds.add(x.getId());
            } catch (ServiceLocalException e) {
                throw e;
            }
        }


        // You can move items in a batch request. This will result in MoveItem operation call to EWS.
        // Unlike the EmailMessage.Move method, the batch request takes a collection of item identifiers,
        // which identify the items that will be moved.
        ServiceResponseCollection<MoveCopyItemResponse> responses = service.moveItems(itemIds, destinationFolderId);

        if (responses.getOverallResult() == ServiceResult.Success) {
            return true;
        } else {
            throw new Exception("The batch move of the email message items was not successful.");
        }
    }


    public static boolean MoveBulkEmails(ExchangeService service, List<ItemId> itemIds, FolderId destinationFolderId) throws Exception {

        if (itemIds == null || service == null || destinationFolderId == null)
            return false;

        if (itemIds.size() == 0)
            return false;


        // You can move items in a batch request. This will result in MoveItem operation call to EWS.
        // Unlike the EmailMessage.Move method, the batch request takes a collection of item identifiers,
        // which identify the items that will be moved.
        ServiceResponseCollection<MoveCopyItemResponse> responses = service.moveItems(itemIds, destinationFolderId);

        if (responses.getOverallResult() == ServiceResult.Success) {
            return true;
        } else {
            throw new Exception("The batch move of the email message items was not successful.");
        }


    }

    public static boolean MoveBulkEmails(ExchangeService service, List<ItemId> itemIds, String destinationFolderName) throws Exception {


        FolderId   destinationFolderId = FindFolderIdByName(service, destinationFolderName );


        if (itemIds == null || service == null || destinationFolderId == null)
            return false;

        if (itemIds.size() == 0)
            return false;


        // You can move items in a batch request. This will result in MoveItem operation call to EWS.
        // Unlike the EmailMessage.Move method, the batch request takes a collection of item identifiers,
        // which identify the items that will be moved.
        ServiceResponseCollection<MoveCopyItemResponse> responses = service.moveItems(itemIds, destinationFolderId);

        if (responses.getOverallResult() == ServiceResult.Success) {
            return true;
        } else {
            throw new Exception("The batch move of the email message items was not successful.");
        }


    }


    public static FolderId FindFolderIdByName(ExchangeService service, String folderName) throws Exception {


        Folder inboxFolder = Folder.bind(service, WellKnownFolderName.Inbox);

        if(inboxFolder != null && folderName.equalsIgnoreCase("inbox") )
        {

           return inboxFolder.getId();

        }

        FolderView view = new FolderView(1);
        view.setTraversal(FolderTraversal.Deep);
        //SearchFilter filter = new SearchFilter.IsEqualTo(FolderSchema.DisplayName, folderName);

        SearchFilter filter = new SearchFilter.ContainsSubstring(FolderSchema.DisplayName, folderName, ContainmentMode.FullString, ComparisonMode.IgnoreCase);

        FindFoldersResults results = service.findFolders(WellKnownFolderName.Inbox, filter, view);

        if (results.getTotalCount() < 1)
            return null;
        if (results.getTotalCount() > 1)
            throw new Exception(String.format("Multiple Folders %s have been found", folderName));

        return results.getFolders().get(0).getId();

    }

    public static FolderId FindFolderIdByName(ExchangeService service,WellKnownFolderName wellKnownFolderName , String folderName) throws Exception {


        FolderView view = new FolderView(1);
        view.setTraversal(FolderTraversal.Deep);
        //SearchFilter filter = new SearchFilter.IsEqualTo(FolderSchema.DisplayName, folderName);

        SearchFilter filter = new SearchFilter.ContainsSubstring(FolderSchema.DisplayName, folderName, ContainmentMode.FullString, ComparisonMode.IgnoreCase);

        FindFoldersResults results = service.findFolders(wellKnownFolderName, filter, view);

        if (results.getTotalCount() < 1)
            return null;
        if (results.getTotalCount() > 1)
            throw new Exception(String.format("Multiple Folders %s have been found", folderName));

        return results.getFolders().get(0).getId();

    }

    private static ItemView getItemView(int numOfElemsToRetrieve) throws Exception {

        ItemView view = new ItemView(numOfElemsToRetrieve , 0);

        view.getOrderBy().add(ItemSchema.DateTimeReceived, SortDirection.Ascending);
        PropertySet propertySet = new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeReceived, ItemSchema.Categories, EmailMessageSchema.InternetMessageId, EmailMessageSchema.From);

        view.setPropertySet(propertySet);
        return view;
    }

    public static FindItemsResults<Item> FindItemsBySearchCondition(ExchangeService service, WellKnownFolderName parentFolderName, String queryString, int numOfElemsToRetrieve) throws Exception {

        ItemView view = getItemView(numOfElemsToRetrieve);

        // Find the first email message in the Inbox that has attachments. This results in a FindItem operation call to EWS.
        FindItemsResults<Item> results = service.findItems(parentFolderName, queryString, view);

        return results;
    }


    public static FindItemsResults<Item> FindItemsBySearchCondition(ExchangeService service, FolderId folderIdToSearch, String queryString, int numOfElemsToRetrieve) throws Exception {


        ItemView view = getItemView(numOfElemsToRetrieve);

        // Find the first email message in the Inbox that has attachments. This results in a FindItem operation call to EWS.
        FindItemsResults<Item> results = service.findItems(folderIdToSearch, queryString, view);

        return results;
    }

    public static FindItemsResults<Item> FindItems(ExchangeService service, ExchangeQueryCondSet queryCondSet) throws Exception {


        ItemView itemView = queryCondSet.getView();

        //itemView.setOffset(itemView.getOffset() + offSet);

        FindItemsResults<Item> results = service.findItems(queryCondSet.getFolderId(), queryCondSet.queryString, itemView);

        return results;
    }

    public static FindItemsResults<Item> FindItemsBySearchCondition(ExchangeService service, String folderToSearch, String queryString, int numOfElemsToRetrieve) throws Exception {


        FindItemsResults<Item> results = null;

        ItemView view = getItemView(numOfElemsToRetrieve);

        FolderId folderId = FindFolderIdByName(service, folderToSearch);

        if (folderId != null)
            results = service.findItems(folderId, queryString, view);

        return results;
    }

    public static ExchangeQueryCondSet FindItemsByQueryString(ExchangeService service, String folderToSearch, String queryString, int numOfElemsToRetrieve) throws Exception {

        ExchangeQueryCondSet result = null;

        ItemView view = getItemView(numOfElemsToRetrieve);

        FolderId folderId = FindFolderIdByName(service, folderToSearch);

        if (folderId != null)
            result = new ExchangeQueryCondSet(queryString, folderId, view);

        return result;
    }

    public static List<Pair<String, String>> ConvertItemsToIds(FindItemsResults<Item> items) throws Exception {

        ArrayList<Pair<String, String>> itemsList = new ArrayList<Pair<String, String>>();

        items.forEach((Item item) -> {
            try {
                if (item instanceof EmailMessage)
                    itemsList.add(new Pair<String, String>(item.getId().getUniqueId(), ((EmailMessage) item).getInternetMessageId()));

            } catch (ServiceLocalException e) {
                e.printStackTrace();
            }
        });

        return itemsList;

    }


    public static List<String> ConvertItemsToItemsId(FindItemsResults<Item> items) throws Exception {

        ArrayList<String> itemsList = new ArrayList<String>();

        items.forEach((Item item) -> {
            try {
                if (item instanceof EmailMessage)
                    itemsList.add(item.getId().getUniqueId());

            } catch (ServiceLocalException e) {
                e.printStackTrace();
            }
        });

        return itemsList;

    }

    public static List<String> removeDuplicateItems(List<String> sourceList, List<String> listToRemove) throws Exception {

        List<String> sourceAuxList = new ArrayList<String>(sourceList);


        if (sourceAuxList.removeAll(listToRemove))
            return sourceAuxList;
        else
            throw new Exception("It was not possible to remove the items in the source list");

    }


    public static boolean MarkItemsWithStatus(FindItemsResults<Item> items, String status) throws Exception {


        // Create a definition for the extended property.
        ExtendedPropertyDefinition statusCustomField = getStatusCustomField();


        items.forEach((Item item) -> {

            if (item instanceof EmailMessage) {
                // Add the extended property to an e-mail message object named "status".
                EmailMessage message = (EmailMessage) item;
                try {
                    message.setExtendedProperty(statusCustomField, status);


                    StringList x = new StringList();
                    x.add(status);

                    message.setCategories(x);
                    message.update(ConflictResolutionMode.AutoResolve);

                } catch (Exception e) {
                    e.printStackTrace();

                }

            }

        });

        return true;

    }


    public static EmailData ReadItemMessage(ExchangeService service, String itemId) throws Exception {

        // As a best practice, limit the properties returned to only those that are required.

        PropertySet propSet = new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeReceived, ItemSchema.Categories, EmailMessageSchema.InternetMessageId);

        EmailData emailData = new EmailData();

        EmailMessage emailMessage;

        // Bind to the existing item by using the ItemId.
        // This method call results in a GetItem call to EWS.
        try {
            Item item = Item.bind(service, ItemId.getItemIdFromString(itemId), propSet);

            item.load();

            if (item instanceof EmailMessage) {
                emailMessage = ((EmailMessage) item);
                emailData.setSender(emailMessage.getSender().toString());
                emailData.setBody(emailMessage.getBody().toString());
                emailData.setInternetMessageId(emailMessage.getInternetMessageId());
                emailData.setItemId(emailMessage.getRootItemId().getUniqueId());
                emailData.setSubject(emailMessage.getSubject());

            }

            return emailData;

        } catch (Exception e) {
            throw e;
        }

    }

    public static EmailData ReadItemMessage(ExchangeService service, ItemId itemId) throws Exception {

        // As a best practice, limit the properties returned to only those that are required.

         PropertySet propSet = new PropertySet(BasePropertySet.FirstClassProperties);

       // PropertySet propSet = new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, EmailMessageSchema.Body, EmailMessageSchema.From, ItemSchema.DateTimeReceived, ItemSchema.Categories, EmailMessageSchema.InternetMessageId);


        EmailData emailData = null;

        EmailMessage emailMessage;

        // Bind to the existing item by using the ItemId.
        // This method call results in a GetItem call to EWS.
        try {

            Item item = Item.bind(service, itemId, propSet);


            item.load();

            emailData = new EmailData();

            if (item instanceof EmailMessage) {
                emailMessage = ((EmailMessage) item);
                emailData.setFrom(emailMessage.getFrom().getAddress());
                emailData.setSender(emailMessage.getSender().toString());
                emailData.setBody(emailMessage.getBody().toString());
                emailData.setInternetMessageId(emailMessage.getInternetMessageId());
                emailData.setItemId(emailMessage.getId().getUniqueId());
                emailData.setSubject(emailMessage.getSubject());
                emailData.setEmailMessage(emailMessage);
                emailData.setIsBodyHTML(emailMessage.getBody().getBodyType() == BodyType.HTML);
                emailData.setDateTimeReceived(emailMessage.getDateTimeReceived());

                EmailAddressCollection emailAddressCollection = emailMessage.getToRecipients();

                String addressToList = emailAddressCollection.getItems().stream().map(EmailAddress::getAddress).collect(Collectors.joining("; "));

                emailData.setTo(addressToList);

            }

        } catch (Throwable e) {
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n++++++++++++++++++ Exception Message: \n" + e.getMessage()
                                + "\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            throw e;
        }

        return emailData;
    }


    public static Boolean MarkItemAsRead(ExchangeService service, String itemId) throws Exception {

        // As a best practice, limit the properties returned to only those that are required.

        PropertySet propSet = new PropertySet(BasePropertySet.IdOnly, EmailMessageSchema.IsRead);

        EmailMessage emailMessage;

        // Bind to the existing item by using the ItemId.
        // This method call results in a GetItem call to EWS.
        try {
            Item item = Item.bind(service, ItemId.getItemIdFromString(itemId), propSet);


            if (item instanceof EmailMessage) {
                emailMessage = ((EmailMessage) item);

                emailMessage.load();

                boolean auxIsRead = emailMessage.getIsRead();

                if(auxIsRead == false ) {

                    emailMessage.setIsRead(true);
                    emailMessage.update(ConflictResolutionMode.AlwaysOverwrite);

                }

            }

            return true;

        } catch (Exception e) {
            throw e;
        }

    }

    public static Path SaveItemMessageAsEMLFile(ExchangeService service, String itemId, String filePath) throws Exception {

        // As a best practice, limit the properties returned to only those that are required.

        PropertySet mimeContentSet = new PropertySet(ItemSchema.MimeContent);


        EmailMessage emailMessage;

        // Bind to the existing item by using the ItemId.
        // This method call results in a GetItem call to EWS.
        try {
            Item item = Item.bind(service, ItemId.getItemIdFromString(itemId));

            item.load(mimeContentSet);
            MimeContent mc = item.getMimeContent();
            return Files.write(Paths.get(filePath), mc.getContent());
        } catch (Exception e) {
            throw e;
        }

    }

    public static byte[] SaveItemMessageAsEMLFile(ExchangeService service, String itemId) throws Exception {

        // As a best practice, limit the properties returned to only those that are required.

        PropertySet mimeContentSet = new PropertySet(ItemSchema.MimeContent);


        EmailMessage emailMessage;

        // Bind to the existing item by using the ItemId.
        // This method call results in a GetItem call to EWS.
        try {
            Item item = Item.bind(service, ItemId.getItemIdFromString(itemId));

            item.load(mimeContentSet);
            MimeContent mc = item.getMimeContent();
            return mc.getContent();
        } catch (Exception e) {
            throw e;
        }

    }


    public static ByteArrayInputStream ReadItemMessageAsMimeContent(ExchangeService service, String itemId) throws Exception {

        // As a best practice, limit the properties returned to only those that are required.
        PropertySet mimeContentSet = new PropertySet(ItemSchema.MimeContent);

        // Bind to the existing item by using the ItemId.
        // This method call results in a GetItem call to EWS.
        try {
            Item item = Item.bind(service, ItemId.getItemIdFromString(itemId));

            item.load(mimeContentSet);
            MimeContent mc = item.getMimeContent();

            ByteArrayInputStream bis = new ByteArrayInputStream(mc.getContent());

            return bis;

        } catch (Exception e) {
            throw e;
        }

    }

    public static Hashtable<ItemId, String> ReadItemsWithStatus(FindItemsResults<Item> items) throws Exception {


        Hashtable hashtable = new Hashtable<ItemId, String>();


        items.forEach((Item item) -> {

            if (item instanceof EmailMessage) {
                // Add the extended property to an e-mail message object named "status".
                EmailMessage message = (EmailMessage) item;
                try {


                    OutParam statusValueOut = new OutParam();
                    message.getExtendedProperties().tryGetValue(String.class, getStatusCustomField(), statusValueOut);


                    hashtable.put(message.getId(), statusValueOut.getParam());


                } catch (Exception e) {
                    e.printStackTrace();

                }

            }

        });

        return hashtable;

    }


    public static ExtendedPropertyDefinition getStatusCustomField() throws Exception {


        // Get the GUID for the property set.
        UUID MyPropertySetId = UUID.fromString("C11FF724-AA03-4555-9952-8FA248A11C3E");

        // Create a definition for the extended property.
        ExtendedPropertyDefinition statusCustomField = new ExtendedPropertyDefinition(MyPropertySetId, "status", MapiPropertyType.String);


        return statusCustomField;
    }


    public static int SendEmailsWithAttachment(ExchangeService service, Email email) throws Exception {


        EmailMessage msg = new EmailMessage(service);


        msg.setSubject(email.getSubject());
        msg.setBody(MessageBody.getMessageBodyFromText(email.getBody()));


        List<String> emailsTo = getEmails(email.getTo());

        for (String emailTo : emailsTo)
            msg.getToRecipients().add(emailTo);


        if (email.getCc() != null && !email.getCc().equalsIgnoreCase("")){

            List<String> emailsCc = getEmails(email.getCc());

            for (String emailCc : emailsCc)
                msg.getCcRecipients().add(emailCc);

         }


        if (email.getBcc()!=null && !email.getBcc().equalsIgnoreCase("")){

            List<String> emailsBCc = getEmails(email.getBcc());

            for (String emailBCc : emailsBCc)
                msg.getBccRecipients().add(emailBCc);

        }


        for (EmailFileAttachment fileAttach : email.getEmailFileAttachments()) {

            msg.getAttachments().addFileAttachment(fileAttach.name,fileAttach.getFileBytes());
        }


        msg.sendAndSaveCopy();

        return 0;
    }

    private static List<String> getEmails(String str) {
        return Arrays.asList(str.split("\\s*;\\s*"));
    }



}
