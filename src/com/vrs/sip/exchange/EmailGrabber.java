package com.vrs.sip.exchange;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.search.FindItemsResults;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class EmailGrabber {

    public static void main_old(String[] args) {

        System.out.println("Hello1");
        ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);
        ExchangeCredentials credentials = new WebCredentials("orlando.agostinho@stellaxius.onmicrosoft.com", "#gajas#201075#");
        service.setCredentials(credentials);
        try
        {
           // service.setImpersonatedUserId(new ImpersonatedUserId(ConnectingIdType.SmtpAddress, "daniel.marujo@stellaxius.onmicrosoft.com"));
            service.setUrl(new URI("https://outlook.office365.com/EWS/Exchange.asmx"));

            //Export Attachments
            EmailAttachments.GetAttachments(service);

        /*    EmailMessage msg = new EmailMessage(service);
            msg.setSubject("Hello world!");
            msg.setBody(MessageBody.getMessageBodyFromText("Sent using the EWS Java API....CENAS1"));
            msg.getToRecipients().add("orlando.agostinho@gmail.com");
            msg.send();*/
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


//    public static void main2(String[] args) {
//
//        System.out.println("Hello1");
//
//        try
//        {
//            UserEmailData userEmailData = new  UserEmailData(ExchangeVersion.Exchange2010_SP2,"orlando.agostinho@stellaxius.onmicrosoft.com", "#gajas#201075#", new URI("https://outlook.office365.com/EWS/Exchange.asmx"));
//
//            //UserEmailData userEmailData1 = new  UserEmailData(ExchangeVersion.Exchange2010_SP2,"miguel.fernandes4@vodafone.com", "Welcome11", new URI("https://outlook.office365.com/EWS/Exchange.asmx"));
//
//            ExchangeService service = EmailGrabberService.ConnectToServiceWithImpersonationURLExchangeServer(userEmailData,"daniel.marujo@stellaxius.onmicrosoft.com", null);
//
//            ExchangeService service = EmailGrabberService.ConnectToServiceWithURLExchangeServer(userEmailData,null);
//
//            // service.setImpersonatedUserId(new ImpersonatedUserId(ConnectingIdType.SmtpAddress, "daniel.marujo@stellaxius.onmicrosoft.com"));
//            //service.setUrl(new URI("https://outlook.office365.com/EWS/Exchange.asmx"));
//
//            //Export Attachments
//           // EmailAttachments.GetAttachments(service);
//
//            /*
//          EmailMessage msg = new EmailMessage(service);
//            msg.setSubject("Hello world!");
//            msg.setBody(MessageBody.getMessageBodyFromText("Sent using the EWS Java API....CENAS1--Barabara2"));
//            msg.getToRecipients().add("orlando.agostinho@gmail.com");
//            msg.send();
//            */
//
//           FolderId folderSource = EmailGrabberUtil.FindFolderIdByName(service, "Inbox");
//
//
//           FolderId folderTarget = EmailGrabberUtil.FindFolderIdByName(service, "Processed");
//
//
//
//
//
//            //String querystring = "HasAttachments:true Subject:'Message with Attachments' Kind:email";
//
//            String querystring = "Kind:email";
//
//            FindItemsResults<Item>  result = EmailGrabberUtil.FindItemsBySearchCondition(service,folderSource,querystring,300);
//
//            List<ItemId> itemsToMove = EmailGrabberUtil.ConvertToListItemsId(result);
//
//           // boolean hasbeenremoved = EmailGrabberUtil.MoveBulkEmails(service,itemsToMove,folderTarget);
//
//           boolean hasBeenMarked = EmailGrabberUtil.MarkItemsWithStatus(result, DateTime.now().toString("dd-MM-yyyy HH:mm:ss"));
//
//
//            result = EmailGrabberUtil.FindItemsBySearchCondition(service,folderSource,querystring,300);
//
//            Hashtable<ItemId,String> hashTable = EmailGrabberUtil.ReadItemsWithStatus(result);
//
//            hashTable.forEach( (k,v)-> System.out.println(String.format("itemId: %s,\n Status: %s",k,v)));
//
//            //System.out.println(folderProcessedWithError);
//
//        }
//        catch (Exception e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//

//    private static final Logger log = LogManager.getLogger(EmailGrabber.class);

    public static void main5(String[] args) {


      //  log.info("ORLANDO");
        //log.info("ORLANDO");




        System.out.println("Hello1");

        try
        {

            UserEmailData userEmailData = new  UserEmailData(ExchangeVersion.Exchange2010_SP2,"orlando.agostinho@stellaxius.onmicrosoft.com", "#gajas#201075#", new URI("https://outlook.office365.com/EWS/Exchange.asmx"));

            //UserEmailData userEmailData1 = new  UserEmailData(ExchangeVersion.Exchange2010_SP2,"miguel.fernandes4@vodafone.com", "Welcome11", new URI("https://outlook.office365.com/EWS/Exchange.asmx"));

           // ExchangeService service = EmailGrabberService.ConnectToServiceWithImpersonationURLExchangeServer(userEmailData,"daniel.marujo@stellaxius.onmicrosoft.com", null);

            ExchangeService service = EmailGrabberService.ConnectToServiceWithURLExchangeServer(userEmailData,null);


            FolderId folderSource = EmailGrabberUtil.FindFolderIdByName(service, "Inbox");


            //String querystring = "HasAttachments:true Subject:'Message with Attachments' Kind:email";

            String querystring1 = "Kind:email AND (Received:today OR Received:yesterday)";

            String querystring = "Kind:email";

            FindItemsResults<Item>  result = EmailGrabberUtil.FindItemsBySearchCondition(service,WellKnownFolderName.Inbox,querystring,300);

            List<Pair<String,String>> emailIdsList = EmailGrabberUtil.ConvertItemsToIds(result);

            List<String> itemsId= EmailGrabberUtil.ConvertItemsToItemsId(result);

            List<String> newList = new ArrayList<String>(itemsId);

            newList.remove(0);

            List<String> xpto = EmailGrabberUtil.removeDuplicateItems(itemsId, newList);

            //Boolean xpto1 = EmailGrabberUtil.SaveItemMessageAsEML(service,itemsId.get(1), "/Users/Orlando/message.eml");

            Boolean yo = EmailGrabberUtil.MarkItemAsRead(service,itemsId.get(1));
             yo = EmailGrabberUtil.MarkItemAsRead(service,itemsId.get(0));

            EmailData emailData = EmailGrabberUtil.ReadItemMessage(service,itemsId.get(0));

            System.out.println(emailData);



            itemsId.forEach(x ->

                    {
                        //System.out.println(String.format("itemId:%s", x));
                        System.out.println(String.format("itemId:%s", x));
                    }


            );

            System.out.println("--------------------------");


            xpto.forEach(x ->

                    System.out.println(String.format("itemId:%s", x))

            );




            //Hashtable<ItemId,String> hashTable = EmailGrabberUtil.ReadItemsWithStatus(result);

           // hashTable.forEach( (k,v)-> System.out.println(String.format("itemId: %s,\n Status: %s",k,v)));

            //System.out.println(folderProcessedWithError);

        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    public static void mainorlando(String[] args) {




        try
        {

            //https://webmail-north.vodafone.com/ews/Services.wsdl
            UserEmailData userEmailData = new  UserEmailData(ExchangeVersion.Exchange2010_SP2,"oagostinho@corp.contoso.com", "#cabras#201075#", new URI("https://emailgrabber.westeurope.cloudapp.azure.com:444/EWS/exchange.asmx"));


            ExchangeService service = EmailGrabberService.ConnectToServiceWithURLExchangeServerTeste(userEmailData,null);


            String querystring = "Kind:email";

            FindItemsResults<Item>  result = EmailGrabberUtil.FindItemsBySearchCondition(service,WellKnownFolderName.Inbox,querystring,300);



            List<String> itemsId= EmailGrabberUtil.ConvertItemsToItemsId(result);

            System.out.println("itemsId.size = " + itemsId.size());


            itemsId.forEach(x ->

                    {
                        //System.out.println(String.format("itemId:%s", x));
                        try {
                            System.out.println(EmailGrabberUtil.ReadItemMessage(service,x).getSubject());
                        } catch (Exception e) {
                            e.printStackTrace();

                        }

                        //log.info(String.format("itemId:%s", x));
                    }


            );

            System.out.println("--------------------------");





        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {




        try
        {

            //https://webmail-north.vodafone.com/ews/Services.wsdl
            UserEmailData userEmailData = new  UserEmailData(ExchangeVersion.Exchange2010_SP2,"miguel.fernandes4@vodafone.com", "Welcome11", new URI("https://webmail-north.vodafone.com/ews/exchange.asmx"));


            ExchangeService service = EmailGrabberService.ConnectToServiceWithURLExchangeServer(userEmailData,null);


            String querystring = "Kind:email";

            FindItemsResults<Item>  result = EmailGrabberUtil.FindItemsBySearchCondition(service,WellKnownFolderName.Inbox,querystring,10);



            List<String> itemsId= EmailGrabberUtil.ConvertItemsToItemsId(result);

            System.out.println("itemsId.size = " + itemsId.size());


            itemsId.forEach(x ->

                    {
                        //System.out.println(String.format("itemId:%s", x));
                        try {
                            System.out.println("-----------------------BEGIN-------------------------------");
                            System.out.println(EmailGrabberUtil.ReadItemMessage(service,x).getSubject());
                            System.out.println(EmailGrabberUtil.ReadItemMessage(service,x).getBody());
                            System.out.println("-----------------------END-------------------------------");
                        } catch (Exception e) {
                            System.out.println(e);
                            e.printStackTrace();
                        }

                        //log.info(String.format("itemId:%s", x));
                    }


            );

            System.out.println("--------------------------");





        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }
    }

}
