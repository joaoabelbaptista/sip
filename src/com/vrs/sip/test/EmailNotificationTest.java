package com.vrs.sip.test;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sforce.ws.ConnectionException;
import com.vrs.sip.EmailNotification;

public class EmailNotificationTest {
	private static Log log = LogFactory.getLog(EmailNotificationTest.class);
	
	public static void main(String[] args) throws IOException, ConnectionException {
		EmailNotification emailNotification = EmailNotification.getInstance();
	
		log.info("Starting email notification");
		
		emailNotification.sendMessage(
				"antonio.oliveira.santos@sapo.pt; antonio.manuel.oliveira.santos@gmail.com",
				null,
				null,
				"EmailNotification Test",
				"Test of Class " + EmailNotificationTest.class.getName() + "\n\nAutomated Test, do not reply\n",
				null
		);
		
		log.info("Ending email notification");
	}
}
