/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: CSV Implementation of a Connection.
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
import com.vrs.sip.connection.attributes.CSVConnectionAttributes;
import com.vrs.sip.connection.credentials.NoCredentials;

public class CSVConnection extends AbstractConnection {
	FileLog log;
	
	private ICredentials credentials;
	private IConnectionAttributes connectionAttributes;
	
	@Override
	public void setLog(FileLog log) {
		this.log = log;
	}
	
	@Override
	public FileLog getLog() {
		return log;
	}
	
	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.CSV;
	}

	@Override
	public ICredentials getCredentials() {
		if (credentials == null) {
			credentials = new NoCredentials();
		}
		
		return credentials;
	}

	@Override
	public void setCredentials(String credentialsFilename) {
	}

	@Override
	public void setCredentials(ICredentials credentials) {
	}
	
	@Override
	public IConnectionAttributes getConnectionAttributes() {
		if (connectionAttributes == null) {
			connectionAttributes = new CSVConnectionAttributes();
		}
		
		return connectionAttributes;
	}

	@Override
	public void openConnection() throws Exception {
	}

	@Override
	public void closeConnection() throws Exception {
	}

	@Override
	public IStatement createStatement() throws Exception {
		CSVStatement statement = null;
		
		getLog().debug("createStatement");
		
		statement = new CSVStatement();
		
		statement.setConnection(this);
		
		return statement;
	}

	@Override
	public void commit() throws Exception {
	}

	@Override
	public Object getImplConnection() {
		return null;
	}
}
