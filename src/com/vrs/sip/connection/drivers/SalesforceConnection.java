/**itialioConnzeiti
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Salesforce Implementation of a Connection.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection.drivers;

import com.vrs.sip.FileLog;
import com.vrs.sip.connection.AbstractConnection;
import com.vrs.sip.connection.ConnectionType;
import com.vrs.sip.connection.IConnectionAttributes;
import com.vrs.sip.connection.ICredentials;
import com.vrs.sip.connection.IStatement;
import com.vrs.sip.connection.attributes.SalesforceConnectionAttributes;
import com.vrs.sip.connection.credentials.SalesforceCredentials;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class SalesforceConnection extends AbstractConnection {
	FileLog log;
	
	private ConnectorConfig connectorConfig;
	protected PartnerConnection partnerConnection;
	private ConnectorConfig metadataConnectorConfig;
	private MetadataConnection metadataConnection;
	private LoginResult loginResult;
	
	@Override
	public void setLog(FileLog log) {
		this.log = log;
	}
	
	@Override
	public FileLog getLog() {
		if (log == null) {
			log = new FileLog();
		}
		
		return log;
	}
	
	/** Private Methods **/
	private boolean salesforceLogin() throws ConnectionException {
		boolean isLoginSuccess = false;

		getLog().debug("salesforceLogin START");
		
		initializeConnectorConfig();
		initializePartnerConnection();
		initializeMetadataConnection();
		
		doConnectivityTest();
		
		isLoginSuccess = true;
		
		getLog().debug("isLoginSuccess = " + isLoginSuccess);
		
		getLog().debug("salesforceLogin END");
		
		return isLoginSuccess;
	}
	
	private void initializeConnectorConfig() {
		connectorConfig = new ConnectorConfig();
		
		connectorConfig.setManualLogin(true);
		
		connectorConfig.setServiceEndpoint(getCredentials().getLoginServer());
		connectorConfig.setAuthEndpoint(getCredentials().getLoginServer());
		connectorConfig.setValidateSchema(false);
	}
	
	private void initializePartnerConnection() throws ConnectionException {
		partnerConnection = Connector.newConnection(connectorConfig);
		
		if (partnerConnection != null) {
			ICredentials credentials = getCredentials();
			
			loginResult = partnerConnection.login(credentials.getUsername(), credentials.getPassword() + credentials.getSecurityToken());

			if (loginResult.isPasswordExpired()) {
				getLog().error("Salesforce Login: Password Expired");
			} else {
				IConnectionAttributes conAttributes;
				Boolean allOrNone = false;
				
				conAttributes = getConnectionAttributes();
				
				if (conAttributes != null && conAttributes.getAllOrNone() != null) {
					allOrNone = conAttributes.getAllOrNone();
				}
				
				getLog().debug("SessionID=" + loginResult.getSessionId() + ", ServiceEndpoint=" + loginResult.getServerUrl() + ", AllOrNone=" + allOrNone);
				
				partnerConnection.setSessionHeader(loginResult.getSessionId());
				
				partnerConnection.setAllOrNoneHeader(allOrNone);
				connectorConfig.setServiceEndpoint(loginResult.getServerUrl());
			}
		}
	}
	
	private void initializeMetadataConnection() throws ConnectionException {
		metadataConnectorConfig = new ConnectorConfig();
		
		metadataConnectorConfig.setSessionId(loginResult.getSessionId());
		metadataConnectorConfig.setServiceEndpoint(loginResult.getMetadataServerUrl());
		
		metadataConnection = new MetadataConnection(metadataConnectorConfig);
	}
	
	private void doConnectivityTest() throws ConnectionException {
		getLog().debug("doConnectivityTest START");
		
		QueryResult queryResult = partnerConnection.query("SELECT Id, Name FROM Organization");
		
		if (queryResult.getSize() > 0) {
			SObject[] records = queryResult.getRecords();
			
			for (int i = 0; i < records.length; i++) {
				SObject record = records[i];
				String orgId;
				String orgName;
				
				orgId = (String)record.getSObjectField("Id");
				orgName = (String)record.getSObjectField("Name");
				
				getLog().debug("Salesforce Connectivity Test: OrgId=" + orgId + ", OrgName=" + orgName);
			}
		}
		
		getLog().debug("doConnectivityTest END");
	}

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.SALESFORCE;
	}

	@Override
	public ICredentials getCredentials() {
		if (credentials == null) {
			credentials = new SalesforceCredentials();
		}
		
		return credentials;
	}
	
	@Override
	public void setCredentials(ICredentials credentials) {
		this.credentials = credentials;
	}

	@Override
	public IConnectionAttributes getConnectionAttributes() {
		if (connectionAttributes == null) {
			connectionAttributes = new SalesforceConnectionAttributes();
		}
		
		return connectionAttributes;
	}

	@Override
	public void openConnection() throws Exception {
		getLog().debug("openConnection START");
		
		if (salesforceLogin() == true) {
			doConnectivityTest();
		}
		
		getLog().debug("openConnection END");
	}

	@Override
	public void closeConnection() throws Exception {
	}

	@Override
	public IStatement createStatement() throws Exception {
		SalesforceStatement statement = null;
		
		getLog().debug("createStatement");
		
		if (partnerConnection != null) {
			statement = new SalesforceStatement();
			statement.setConnection(this);	
		}
		
		return statement;
	}

	@Override
	public void commit() throws Exception {
	}

	public PartnerConnection getSalesforcePartnerConnection() {
		return partnerConnection;
	}
	
	public MetadataConnection getSalesforceMetadataConnection() {
		return metadataConnection;
	}

	@Override
	public Object getImplConnection() {
		return partnerConnection;
	}
}
