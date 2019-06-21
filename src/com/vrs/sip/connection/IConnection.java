/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Connection Interface.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection;

import java.io.IOException;

import com.vrs.sip.FileLog;

public interface IConnection {
	public void setLog(FileLog log);
	public FileLog getLog();
	
	public ConnectionType getConnectionType();
	
	public ICredentials getCredentials();
	
	public IConnectionAttributes getConnectionAttributes();
	
	/** Set connector credentials based on properties filename 
	 * @throws IOException **/
	public void setCredentials(String credentialsFilename) throws IOException;
	
	/** Set connector credentials based on ICredentials interface **/
	public void setCredentials(ICredentials credentials) throws Exception;
	
	/** Set connection attributes based on properties filename
	 * @return
	 */
	public void setConnectionAttributes(String connectionAttributesFilename) throws IOException;
	
	public void setConnectionAttributes(IConnectionAttributes attributes);
	
	/** Open a connection to endpoint **/
	public void openConnection() throws Exception;
	
	/** Close connection from endpoint **/
	public void closeConnection() throws Exception;

	/** Create a Statement **/
	public IStatement createStatement() throws Exception;
	
	/** Commit **/
	public void commit() throws Exception;
	
	public Object getImplConnection();
}
