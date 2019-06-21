/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Connection Metadata.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.metadata;

import com.vrs.sip.connection.Record;
import com.vrs.sip.connection.Record.IllegalRecordFieldIndex;
import com.vrs.sip.connection.Record.IllegalRecordFieldName;

public class ConnectionMetadata {
	public String id;
	public String name;
	public Boolean autoCommit;
	public Integer batchSize;
	public String connectionName;
	public String connectionType;
	public String hostname;
	public String loginServer;
	public Integer loginTimeout;
	public String password;
	public Integer port;
	public String securityToken;
	public String service;
	public String username;
	public String directory;
	public String dateFormat;
	public String customDateFormat;
	public String charset;
	public Boolean allOrNone;
	public String impersonatedEmailAccount;
	public String proxyHostName;
	public int proxyPort;
	
	/** Get a new Connection from a Record 
	 * @throws IllegalRecordFieldName 
	 * @throws IllegalRecordFieldIndex **/
	public ConnectionMetadata(Record record) throws IllegalRecordFieldIndex, IllegalRecordFieldName {
		if (record != null) {
			this.id = record.getFieldByName("Id").getString();
			this.name = record.getFieldByName("Name").getString();
			this.autoCommit = record.getFieldByName("Auto_Commit__c").getBoolean();
			this.batchSize = record.getFieldByName("Batch_Size__c").getInteger();
			this.connectionName = record.getFieldByName("Connection_Name__c").getString();
			this.connectionType = record.getFieldByName("Connection_Type__c").getString();
			this.hostname = record.getFieldByName("Hostname__c").getString();
			this.loginServer = record.getFieldByName("Login_Server__c").getString();
			this.loginTimeout = record.getFieldByName("Login_Timeout__c").getInteger();
			this.password = record.getFieldByName("Password__c").getString();
			this.port = record.getFieldByName("Port__c").getInteger();
			this.securityToken = record.getFieldByName("Security_Token__c").getString();
			this.service = record.getFieldByName("Service__c").getString();
			this.username = record.getFieldByName("Username__c").getString();
			this.directory = record.getFieldByName("Directory__c").getString();
			this.dateFormat = record.getFieldByName("Date_Format__c").getString();
			this.customDateFormat = record.getFieldByName("Custom_Date_Format__c").getString();
			this.charset = record.getFieldByName("Charset__c").getString();
			this.allOrNone = record.getFieldByName("All_Or_None__c").getBoolean();
            this.impersonatedEmailAccount = record.getFieldByName("Impersonated_User_Email__c").getString();
            this.proxyHostName = record.getFieldByName("Proxy_Host__c").getString();
            this.proxyPort = record.getFieldByName("Proxy_Port__c").getInteger() == null ? -1 : record.getFieldByName("Proxy_Port__c").getInteger() ;
		}
	}
	
	/** Get the Enumerator Query **/
	public static String getEnumeratorQuery() {
		return "SELECT Id, Name, Auto_Commit__c, Batch_Size__c, Connection_Name__c, Connection_Type__c, Hostname__c, Login_Server__c, Login_Timeout__c, Password__c, Port__c, Security_Token__c, Service__c, Username__c, Directory__c, Date_Format__c, Custom_Date_Format__c, Charset__c, All_Or_None__c, Impersonated_User_Email__c, Proxy_Port__c, Proxy_Host__c FROM SIP_Connection__c";
	}
}
