/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Statement Interface.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection;

import com.vrs.sip.FileLog;
import com.vrs.sip.task.Schedule;

import java.util.List;

public interface IStatement {
	/** Set the connector associated with the statement **/
	public void setConnection(IConnection connection) throws Exception;
	
	/** Execute a SQL query **/
	public IResultSet executeQuery(String sql) throws Exception;
	
	/**
	 * Execute an insert, update, delete or upsert statement
	 * Returns total number of errors occurred.
	 * 
	 * */
	public Integer executeOperation(StatementOperationType operationType, Integer batchSize, String entityName, List<String> entityFieldNameList, List<Record> rowList, List<String> keyFieldList) throws Exception;

	/** Execute a Stored Procedure **/
	public void executeCall(String call, List<Object> inputParameterList) throws Exception;
	
	/** Execute a File Upload **/
	public void executeFileUpload(String parentObject, String parentId, Boolean parentIdIsExternal, String externalIdField, String filename, String name, String description, String contentType, Boolean isPrivate) throws Exception;
	
	/** Execute a fetch records of the entity **/
	public IResultSet fetchRecords(String entity, String filterClause) throws Exception;
	
	/** Execute a TRUNCATE of the entity **/
	public Integer executeTruncate(String entity) throws Exception;
	
	/** Close the Statement **/
	public void close() throws Exception;
	
	/** Set the Logger */
	public void setLog(FileLog log);
	
	/** Get the Logger **/
	public FileLog getLog();
	
	/** Set a specific attribute only used by source/target CSV files **/
	public void setFieldSeparator(char fieldSeparator) throws Exception;

    public Schedule getSchedule();

    public void setSchedule(Schedule schedule);
}
