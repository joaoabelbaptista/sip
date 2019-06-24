package com.vrs.sip.exchange;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.property.complex.Attachment;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.property.complex.ItemAttachment;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;

public class EmailAttachments {

    /// <summary>
    /// Demonstrates three ways to get file attachments and how to get an item attachment.
    /// </summary>
    /// <param name="service">An ExchangeService object with credentials and the EWS URL.</param>
    public static void GetAttachments(ExchangeService service) throws Exception
    {
        // Return a single item.
        ItemView view = new ItemView(1);

        //String querystring = "HasAttachments:true Subject:'Message with Attachments' Kind:email";

        String querystring = "HasAttachments:true Kind:email";

        // Find the first email message in the Inbox that has attachments. This results in a FindItem operation call to EWS.
        FindItemsResults<Item> results = service.findItems(WellKnownFolderName.Inbox, querystring, view);



        if (results.getTotalCount() > 0)
        {
            EmailMessage email = (EmailMessage) results.getItems().get(0);

            // Request all the attachments on the email message. This results in a GetItem operation call to EWS.
            email.load(new PropertySet(EmailMessageSchema.Attachments));

            for (Attachment attachment:email.getAttachments())
            {
                if (attachment instanceof FileAttachment)
                {
                    FileAttachment fileAttachment = (FileAttachment) attachment  ;

                    System.out.println(fileAttachment);

                    // Load the file attachment into memory. This gives you access to the attachment content, which
                    // is a byte array that you can use to attach this file to another item. This results in a GetAttachment operation
                    // call to EWS.
                    fileAttachment.load();
                    System.out.println ("Load a file attachment with a name = " + fileAttachment.getName());

                    // Load attachment contents into a file. This results in a GetAttachment operation call to EWS.
                    fileAttachment.load("/Users/Orlando/" + fileAttachment.getName());


                }
                    else // Attachment is an item attachment.
                {
                    ItemAttachment itemAttachment = (ItemAttachment) attachment ;

                    // Load the item attachment properties. This results in a GetAttachment operation call to EWS.
                    itemAttachment.load();
                    System.out.println ("Loaded an item attachment with Subject = " + itemAttachment.getItem().getSubject());
                }
            }
        }
    }




}
