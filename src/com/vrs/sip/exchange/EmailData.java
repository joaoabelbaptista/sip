package com.vrs.sip.exchange;

import microsoft.exchange.webservices.data.core.service.item.EmailMessage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

public class EmailData {

    String  From;
    String Subject;
    String Body;
    String ItemId;
    String Sender;
    String InternetMessageId;
    Date DateTimeReceived;
    String To;
    EmailMessage EmailMessage;
    boolean isBodyHTML;



    public EmailData() {
    }


    public EmailData(String from, String subject, String body, String itemId, String sender, String internetMessageId, Date dateTimeReceived, String to) {
        From = from;
        Subject = subject;
        Body = body;
        ItemId = itemId;
        Sender = sender;
        InternetMessageId = internetMessageId;
        DateTimeReceived = dateTimeReceived;
        To = to;
    }


    public String getTo() {
        return To;
    }

    public void setTo(String to) {
        To = to;
    }

    public Date getDateTimeReceived() {
        return DateTimeReceived;
    }

    public void setDateTimeReceived(Date dateTimeReceived) {
        DateTimeReceived = dateTimeReceived;
    }

    public boolean isBodyHTML() {
        return isBodyHTML;
    }

    public void setIsBodyHTML(boolean bodyHTML) {
        isBodyHTML = bodyHTML;
    }

    public microsoft.exchange.webservices.data.core.service.item.EmailMessage getEmailMessage() {
        return EmailMessage;
    }

    public void setEmailMessage(microsoft.exchange.webservices.data.core.service.item.EmailMessage emailMessage) {
        EmailMessage = emailMessage;
    }


    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getBody() {

        if (Body != null)
         return Body.substring(0,Math.min(Body.length(), 131071)); //INFO: Limitation for the size of the field in Salesforce.
        else
         return null;

    }

    public void setBody(String body) {
        Body = body;
    }

    public String getItemId() {
        return ItemId;
    }

    public void setItemId(String itemId) {
        ItemId = itemId;
    }

    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        Sender = sender;
    }

    public String getInternetMessageId() {
        return InternetMessageId;
    }

    public void setInternetMessageId(String internetMessageId) {
        InternetMessageId = internetMessageId;
    }

    public String getDateTimeReceivedToUnixFormat() {
        return String.valueOf(DateTimeReceived.getTime());
    }


    private static Date cvtToGmt( Date date )
    {
        TimeZone tz = TimeZone.getDefault();
        Date ret = new Date( date.getTime() - tz.getRawOffset() );
        // if we are now in DST, back off by the delta.  Note that we are checking the GMT date, this is the KEY.
        if ( tz.inDaylightTime( ret ))
        {
            Date dstDate = new Date( ret.getTime() - tz.getDSTSavings() );
            // check to make sure we have not crossed back into standard time
            // this happens when we are on the cusp of DST (7pm the day before the change for PDT)
            if ( tz.inDaylightTime( dstDate ))
            {
                ret = dstDate;
            }
        }
        return ret;
    }

    private static LocalDateTime convertDateToGMT(Date dateToConvert){

            return new java.sql.Timestamp(
                    dateToConvert.getTime()).toLocalDateTime();
    }

    public static String toISO8601UTC(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(tz);
        return df.format(date);
    }

    @Override
    public String toString() {

        /*
        Subject = subject;
        Body = body;
        ItemId = itemId;
        Sender = sender;
        InternetMessageId = internetMessageId;
*/

        return  String.format("Sender = %s, Subject = %s\n ItemId =%s \n InternetMessageId = %s \n Body = %s",getSender(),getSubject(),getItemId(),getInternetMessageId(),"");
    }
}
