/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Email Notification using Salesforce.com.
 * History: aosantos, 2016-07-06, Initial Release.
 * 
 * 
 */
package com.vrs.sip;

import com.sforce.soap.partner.EmailFileAttachment;
import com.sforce.soap.partner.EmailPriority;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SendEmailResult;
import com.sforce.soap.partner.SingleEmailMessage;
import com.sforce.ws.ConnectionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

public class EmailNotification {	
	public void sendMessage(
			String to,
			String cc,
			String bcc,
			String subject,
			String content,
			List<String> attachmentFilenameList
	) throws ConnectionException, IOException {
		Metadata metadata = Factory.getMetadataInstance();
		PartnerConnection partnerConnection;
		
		List<String> toList = getRecipientList(to);
		List<String> ccList = getRecipientList(cc);
		List<String> bccList = getRecipientList(bcc);
		
		SingleEmailMessage message = new SingleEmailMessage();
		SingleEmailMessage[] messages = new SingleEmailMessage[] { message };
		EmailFileAttachment[] attachments;
		SendEmailResult[] results;
		
		message.setEmailPriority(EmailPriority.Normal);
		message.setSaveAsActivity(false);
		message.setSubject(subject);
		message.setUseSignature(false);
		message.setPlainTextBody(content);
		
		attachments = null;
		
		if (attachmentFilenameList != null && attachmentFilenameList.isEmpty() == false) {
			attachments = new EmailFileAttachment[attachmentFilenameList.size()];
			
			for (Integer i = 0; i < attachments.length; i++) {
				EmailFileAttachment efa = new EmailFileAttachment();
				String filename = attachmentFilenameList.get(i);
				String baseFilename = null;
				byte[] fileBody = null;
				
				if (filename != null && filename.contains("/")) {
					baseFilename = filename.substring(filename.lastIndexOf("/") + 1);
				} else {
					baseFilename = filename;
				}
				
				fileBody = loadFileAsByteArray(filename);
				
				efa.setBody(fileBody);
				
				efa.setFileName(baseFilename);
				
				attachments[i] = efa;
			}
			
			message.setFileAttachments(attachments);
		}
		
		if (toList != null && toList.isEmpty() == false) {
			String[] toArray = new String[toList.size()];
			
			for (Integer i = 0; i < toList.size(); i++) {
				toArray[i] = toList.get(i);
			}
			
			message.setToAddresses(toArray);
		}
		
		if (ccList != null && ccList.isEmpty() == false) {
			String[] ccArray = new String[ccList.size()];
			
			for (Integer i = 0; i < ccList.size(); i++) {
				ccArray[i] = ccList.get(i);
			}
			
			message.setCcAddresses(ccArray);
		}
		
		if (bccList != null && bccList.isEmpty() == false) {
			String[] bccArray = new String[bccList.size()];
			
			for (Integer i = 0; i < bccList.size(); i++) {
				bccArray[i] = bccList.get(i);
			}
			
			message.setBccAddresses(bccArray);
		}
		
		partnerConnection = (PartnerConnection)metadata.metadataConnection.getImplConnection();
		
		results = partnerConnection.sendEmail(messages);
		
		if (results != null && results.length > 0 && results[0].isSuccess() == false) {
			System.err.println("The email failed to send: " + results[0].getErrors()[0].getMessage());
		}
	}
	
	private static byte[] loadFileAsByteArray(String filename) throws IOException {
		File file = new File(filename);
		InputStream is = new FileInputStream(file);
		byte[] data = new byte[(int)file.length()];
		
		is.read(data);
		is.close();
		
		return data;
	}
	
	private List<String> getRecipientList(String recipients) {
		List<String> recipientList = new Vector<String>();
		
		if (recipients != null && recipients.trim().isEmpty() == false) {
			for (String recipient : recipients.split(";")) {
				recipientList.add(recipient.trim());
			}
		}
		
		return recipientList;
	}
	
	public static EmailNotification getInstance() throws IOException {
		EmailNotification email;
		
		email = new EmailNotification();
		
		return email;
	}
}
