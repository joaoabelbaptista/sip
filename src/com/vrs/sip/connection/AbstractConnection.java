/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Abstraction Connection.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class AbstractConnection implements IConnection {
	protected ICredentials credentials;
	protected IConnectionAttributes connectionAttributes;
	protected Integer operationId;
	
	public AbstractConnection() {
		credentials = null;
		operationId = 0;
		
		//System.out.println("New of " + this.getClass().getName());
	}
	
	public abstract ConnectionType getConnectionType();
	
	public abstract ICredentials getCredentials();
	
	public abstract IConnectionAttributes getConnectionAttributes();
	
	/** Set connector credentials based on properties filename 
	 * @throws IOException **/
	public void setCredentials(String credentialsFilename) throws IOException {
		ICredentials credentials = getCredentials(); // Force initialization of a default credentials instance if non-existing
		
		if (credentialsFilename != null) {
			InputStream is;
			Properties props;
			
			is = ClassLoader.getSystemResourceAsStream(credentialsFilename);
			
			if (is == null) {
				throw new RuntimeException("Error Loading Credentials File: " + credentialsFilename);
			}
			
			props = new Properties();
			
			props.load(is);
			
			credentials.set(props);
		}
	}
	
	/** Set connection attributes based on properties filename
	 * @return
	 */
	public void setConnectionAttributes(String connectionAttributesFilename) throws IOException {
		IConnectionAttributes connectionAttributes = getConnectionAttributes(); // Force initialization of a default connection attributes instance if non-existing
		
		if (connectionAttributesFilename != null) {
			InputStream is;
			Properties props;
			
			is = ClassLoader.getSystemResourceAsStream(connectionAttributesFilename);
			
			props = new Properties();
			
			props.load(is);
			
			connectionAttributes.set(props);
		}
	}
	
	public void setConnectionAttributes(IConnectionAttributes attributes) {
		IConnectionAttributes connectionAttributes = getConnectionAttributes(); // Force initialization of a default connection attributes instance if non-existing
		
		if (attributes != null) {
			connectionAttributes.set(
				attributes.getAutoCommit(),
				attributes.getLoginTimeout(),
				attributes.getBatchSize(),
				attributes.getDirectory(),
				attributes.getDateFormat(),
				attributes.getCustomDateFormat(),
				attributes.getCharset(),
				attributes.getAllOrNone()
			);
		}
	}
	
	/** Open a connection to endpoint **/
	public abstract void openConnection() throws Exception;
	
	/** Close connection from endpoint **/
	public abstract void closeConnection() throws Exception;

	/** Create a Statement **/
	public abstract IStatement createStatement() throws Exception;
	
	/** Commit **/
	public abstract void commit() throws Exception;
	
	public String toString() {
		return String.format("{ credentials=%1s, connectionAttributes=%2s }", getCredentials(), getConnectionAttributes());
	}
}
