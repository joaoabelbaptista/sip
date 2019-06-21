/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Result Set Interface.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection;

import java.util.List;

import com.vrs.sip.FileLog;

public interface IResultSet {
	/** Set the Statement that generated this result set **/
	public void setStatement(IStatement statement) throws Exception;
	
	/** Fetch more rows from the statement **/
	public List<Record> fetchRows() throws Exception;
	
	/** Fetch all rows from the statement **/
	public List<Record> fetchAllRows() throws Exception;
	
	/** Set the batch size to be used by fetchRows() **/
	public void setBatchSize(Integer batchSize) throws Exception;
	
	/** Set the Logger **/
	public void setLog(FileLog log);
	
	/** Get the Logger **/
	public FileLog getLog();
}
