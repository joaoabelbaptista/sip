/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Salesforce Implementation of a Statement.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection.drivers;

import com.sforce.soap.partner.*;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.vrs.sip.Configuration;
import com.vrs.sip.FileLog;
import com.vrs.sip.connection.FieldType;
import com.vrs.sip.connection.*;
import com.vrs.sip.task.Schedule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SalesforceStatement implements IStatement {
	FileLog log;
	
	IConnection connection;

	Schedule schedule;
	
	PartnerConnection implConnection;
	
	public Boolean stdoutDebug = getStdoutDebugConfiguration();

	public final Integer DEFAULT_SALESFORCE_BATCH_SIZE = 200;
	
	// implConnection

	@Override
	public void setLog(FileLog log) {
		this.log = log;
	}

	private Boolean getStdoutDebugConfiguration() {
		try {
			return Configuration.getInstance().getServer().stdoutDebug;
		} catch (IOException e) {
			e.printStackTrace();
			
			return false;
		}
	}

	@Override
	public FileLog getLog() {
		if (log == null) {
			log = new FileLog();
		}

		return log;
	}

	@Override
	public void setConnection(IConnection connection) throws Exception {
		SalesforceConnection salesforceConnection = (SalesforceConnection)connection;

		this.connection = connection;

		setLog(connection.getLog());

		implConnection = salesforceConnection.getSalesforcePartnerConnection();
	}

	@Override
	public IResultSet executeQuery(String sql) throws Exception {
		if (isValidQuery(sql) == false) {
			throw new RuntimeException("The following query is not-valid: " + sql);
		}

		sql = fixQuery(sql);

        getLog().debug("executeQuery: " + sql);

		QueryResult queryResult = implConnection.query(sql);

		SalesforceResultSet salesforceResultSet = new SalesforceResultSet();

		salesforceResultSet.stdoutDebug = this.stdoutDebug;
		salesforceResultSet.setStatement(this);
		salesforceResultSet.setSalesforceQueryResult(queryResult);
		salesforceResultSet.setSalesforceQuery(sql);

		return salesforceResultSet;
	}

	@Override
	public Integer executeOperation(StatementOperationType operationType, Integer batchSize, String entityName, List<String> entityFieldNameList, List<Record> recordList, List<String> keyFieldList) throws Exception {
		SObject[] sobjectArray;
		String[] idArray;
		SaveResult[] saveResult;
		DeleteResult[] deleteResult;
		UpsertResult[] upsertResult;
		String upsertKeyField;

		Integer counter;
		Integer result = 0;

		if (recordList == null || recordList.isEmpty()) {
			return result;
		}

		sobjectArray = new SObject[recordList.size()];
		idArray = new String[recordList.size()];

		counter = 0;
		for (Record record : recordList) {
			String id;
			SObject sobjectRecord = new SObject(entityName);
			List<String> sobjectFieldsNull = new ArrayList<String>();

			id = null;
			for (String fieldName : entityFieldNameList) {
				if (fieldName.equalsIgnoreCase("id")) {
					id = record.getFieldByName(fieldName).getString();

				}

				if (record.getFieldByName(fieldName).getValue() == null) {
					sobjectFieldsNull.add(fieldName);
				}

				if (record.getFieldByName(fieldName).getFieldType() == FieldType.T_DATE) {
					Date dt = record.getFieldByName(fieldName).getDate();
					java.util.GregorianCalendar dtValue = null;

					if (dt != null) {
						dtValue = getGregorianCalendarDate(dt);
					}
					sobjectRecord.setField(fieldName, dtValue);
				} else {

                    log.debug("record.getFieldByName(fieldName).getValue()" + record.getFieldByName(fieldName).getValue());
					sobjectRecord.setSObjectField(fieldName, record.getFieldByName(fieldName).getValue());
				}

				if (sobjectFieldsNull.isEmpty() == false) {
					String[] nullFields = new String[sobjectFieldsNull.size()];

					for (int i = 0; i < sobjectFieldsNull.size(); i++) {
						nullFields[i] = sobjectFieldsNull.get(i);
					}

					sobjectRecord.setFieldsToNull(nullFields);
				}
			}

			sobjectArray[counter] = sobjectRecord;
			idArray[counter] = id;

			counter++;
		}

		upsertKeyField = null;
		if (keyFieldList != null && keyFieldList.isEmpty() == false) {
			upsertKeyField = keyFieldList.get(0);
		}

		switch (operationType) {
			case Insert:
				saveResult = salesforceCreate(batchSize, sobjectArray);

				result = checkOperationResultErrors(operationType, saveResult);


				break;

			case Delete:
				deleteResult = salesforceDelete(batchSize, idArray);

				result = checkOperationResultErrors(operationType, deleteResult);

				break;

			case Update:

				saveResult = salesforceUpdate(batchSize, sobjectArray);

				result = checkOperationResultErrors(operationType, saveResult);

				break;

			case Upsert:
				upsertResult = salesforceUpsert(batchSize, upsertKeyField, sobjectArray);

				result = checkOperationResultErrors(operationType, upsertResult);

				break;
		}

		return result;
	}

	SaveResult[] salesforceCreate(Integer batchSize, SObject[] sobjectArray) throws ConnectionException {
		SaveResult[] result;
		SObject[] createArray = null;
		
		if (batchSize == null) {
			batchSize = DEFAULT_SALESFORCE_BATCH_SIZE;
		}
		
		if (batchSize > sobjectArray.length) {
			createArray = sobjectArray;
			
			result = implConnection.create(createArray);
		} else {
			int batchOffset = 0;
			int remainingEntries;
			
			result = new SaveResult[sobjectArray.length];
			
			while (batchOffset < sobjectArray.length) {
				SaveResult[] batchSaveResult;
				
				remainingEntries = sobjectArray.length - batchOffset;
				
				if (remainingEntries > batchSize) {
					remainingEntries = batchSize;
				}
				
				createArray = new SObject[remainingEntries];
				
				for (int i = 0; i < remainingEntries; i++) {
					createArray[i] = sobjectArray[batchOffset + i];
				}
				
				batchSaveResult = implConnection.create(createArray);
				
				for (int i = 0; i < batchSaveResult.length; i++) {
					result[batchOffset + i] = batchSaveResult[i];
				}
				
				batchOffset += batchSize;
			}
		}
		
		return result;
	}
	
	DeleteResult[] salesforceDelete(Integer batchSize, String[] idArray) throws ConnectionException {
		DeleteResult[] result;
		String[] deleteArray = null;
		
		if (batchSize == null) {
			batchSize = DEFAULT_SALESFORCE_BATCH_SIZE;
		}
		
		if (batchSize > idArray.length) {
			deleteArray = idArray;
			
			result = implConnection.delete(deleteArray);
		} else {
			int batchOffset = 0;
			int remainingEntries;
			
			result = new DeleteResult[idArray.length];
			
			while (batchOffset < idArray.length) {
				DeleteResult[] batchDeleteResult;
				
				remainingEntries = idArray.length - batchOffset;
				
				if (remainingEntries > batchSize) {
					remainingEntries = batchSize;
				}
				
				deleteArray = new String[remainingEntries];
				
				for (int i = 0; i < remainingEntries; i++) {
					deleteArray[i] = idArray[batchOffset + i];
				}
				
				batchDeleteResult = implConnection.delete(deleteArray);
				
				for (int i = 0; i < batchDeleteResult.length; i++) {
					result[batchOffset + i] = batchDeleteResult[i];
				}
				
				batchOffset += batchSize;
			}
		}
		
		return result;		
	}
	
	SaveResult[] salesforceUpdate(Integer batchSize, SObject[] sobjectArray) throws ConnectionException {
		SaveResult[] result;
		SObject[] updateArray = null;
		
		if (batchSize == null) {
			batchSize = DEFAULT_SALESFORCE_BATCH_SIZE;
		}
		
		if (batchSize > sobjectArray.length) {
			updateArray = sobjectArray;
			
			result = implConnection.update(updateArray);
		} else {
			int batchOffset = 0;
			int remainingEntries;
			
			result = new SaveResult[sobjectArray.length];
			
			while (batchOffset < sobjectArray.length) {
				SaveResult[] batchSaveResult;
				
				remainingEntries = sobjectArray.length - batchOffset;
				
				if (remainingEntries > batchSize) {
					remainingEntries = batchSize;
				}
				
				updateArray = new SObject[remainingEntries];
				
				for (int i = 0; i < remainingEntries; i++) {
					updateArray[i] = sobjectArray[batchOffset + i];
				}
				
				batchSaveResult = implConnection.update(updateArray);
				
				for (int i = 0; i < batchSaveResult.length; i++) {
					result[batchOffset + i] = batchSaveResult[i];
				}
				
				batchOffset += batchSize;
			}
		}
		
		return result;
	}
	
	UpsertResult[] salesforceUpsert(Integer batchSize, String upsertKeyField, SObject[] sobjectArray) throws ConnectionException {
		UpsertResult[] result;
		SObject[] createArray = null;
		
		if (batchSize == null) {
			batchSize = DEFAULT_SALESFORCE_BATCH_SIZE;
		}
		
		if (batchSize > sobjectArray.length) {
			createArray = sobjectArray;
			
			result = implConnection.upsert(upsertKeyField, createArray);
		} else {
			int batchOffset = 0;
			int remainingEntries;
			
			result = new UpsertResult[sobjectArray.length];
			
			while (batchOffset < sobjectArray.length) {
				UpsertResult[] batchSaveResult;
				
				remainingEntries = sobjectArray.length - batchOffset;
				
				if (remainingEntries > batchSize) {
					remainingEntries = batchSize;
				}
				
				createArray = new SObject[remainingEntries];
				
				for (int i = 0; i < remainingEntries; i++) {
					createArray[i] = sobjectArray[batchOffset + i];
				}
				
				batchSaveResult = implConnection.upsert(upsertKeyField, createArray);
				
				for (int i = 0; i < batchSaveResult.length; i++) {
					result[batchOffset + i] = batchSaveResult[i];
				}
				
				batchOffset += batchSize;
			}
		}
		
		return result;
	}
	
	protected String getErrorMessage(Error[] inputErrors) {
		Integer counter = 0;
		String errorMessage = "";
		
		if (inputErrors != null && inputErrors.length > 0) {
			for (Integer i = 0; i < inputErrors.length; i++) {
				Error reportedError = inputErrors[i];
				
				counter++;
				
				if (counter > 1) {
					errorMessage += ", ";
				}
				errorMessage += reportedError.getMessage(); 
			}
		}
		
		return errorMessage;
	}
	
	protected Integer checkOperationResultErrors(StatementOperationType operationType, Object[] resultArray) {
		SaveResult[] saveResultArray;
		DeleteResult[] deleteResultArray;
		UpsertResult[] upsertResultArray;
		Integer result = 0;
		
		if (operationType == StatementOperationType.Insert || operationType == StatementOperationType.Update || operationType == StatementOperationType.InsertWithAttachments) {
			saveResultArray = (SaveResult[])resultArray;
			
			for (SaveResult saveResult : saveResultArray) {
				if (saveResult.isSuccess() == false) {
					getLog().info(operationType.name() + ": " + getErrorMessage(saveResult.getErrors()));
					
					result++;
				}
			}
		} else if (operationType == StatementOperationType.Delete) {
			deleteResultArray = (DeleteResult[])resultArray;
			
			for (DeleteResult deleteResult : deleteResultArray) {
				if (deleteResult.isSuccess() == false) {
					getLog().info(operationType.name() + ": " + getErrorMessage(deleteResult.getErrors()));
					
					result++;
				}
			}
			
		} else if (operationType == StatementOperationType.Upsert) {
			upsertResultArray = (UpsertResult[])resultArray;
			
			for (UpsertResult upsertResult : upsertResultArray) {
				if (upsertResult.isSuccess() == false) {
					getLog().info(operationType.name() + ": " + getErrorMessage(upsertResult.getErrors()));
					
					result++;
				}
			}
		}
		
		return result;
	}

	@Override
	public IResultSet fetchRecords(String entity, String filterClause) throws Exception {
		throw new RuntimeException("TODO: not implemented");
	}

	@Override
	public Integer executeTruncate(String entity) throws Exception {
		throw new RuntimeException("TODO: not implemented");
	}

	@Override
	public void close() throws Exception {
		throw new RuntimeException("TODO: not implemented");
	}

	@Override
	public void executeCall(String call, List<Object> inputParameterList) throws Exception {
		throw new RuntimeException("The method executeCall is not supported by " + this.getClass().getName());
	}

	@Override
	public void executeFileUpload(String parentObject, String parentId, Boolean parentIdIsExternal, String externalIdField, String filename, String name, String description, String contentType, Boolean isPrivate) throws Exception {
		SObject attachment;
		byte[] bodyBytes;

		if (parentId == null || parentId.equals("")) {
			throw new RuntimeException("parentId is mandatory");
		}

		if (filename == null || filename.equals("")) {
			throw new RuntimeException("filename is mandatory");
		}

		if (name == null || name.equals("")) {
			throw new RuntimeException("name is mandatory");
		}

		if (parentIdIsExternal) {
			// Using "externalIdField", search for the appropriate SFDC "Id" from the corresponding SObject "parentObject"
			IResultSet idLookupResultSet;

			// Check for missing parentObject name
			if (parentObject == null || parentObject.equals("")) {
				throw new RuntimeException("parentObject is mandatory when parentIdIsExternal is set to true");
			}

			// Check for missing externalIdField
			if (externalIdField == null || externalIdField.equals("")) {
				throw new RuntimeException("externalIdField is mandatory when parentIdIsExternal is set to true");
			}

			idLookupResultSet = executeQuery("SELECT Id FROM " + parentObject + " WHERE " + externalIdField + " = '" + parentId + "'");

			if (idLookupResultSet != null) {
				List<Record> allRecords = idLookupResultSet.fetchAllRows();
				String id;

				id = null;

				if (allRecords == null || allRecords.isEmpty()) {
					throw new RuntimeException("Error looking up Id from " + parentObject + " where " + externalIdField + " is " + parentId);
				}

				for (Record record : allRecords) {
					id = record.getFieldByName("Id").getString();
				}

				parentId = id;
			}
		}

		attachment = new SObject();

		attachment.setType("Attachment");
		attachment.setSObjectField("parentId", parentId);
		attachment.setSObjectField("name", name);

		if (description != null && description.equals("") == false) {
			attachment.setSObjectField("description", description);
		}

		if (contentType != null && contentType.equals("") == false) {
			attachment.setSObjectField("contentType", contentType);
		}

		if (isPrivate != null) {
			attachment.setSObjectField("isPrivate", isPrivate);
		}

		bodyBytes = getFileBytes(filename);

		attachment.setSObjectField("body", bodyBytes);

		SaveResult[] saveResultList = implConnection.create(new SObject[] { attachment });

		if (saveResultList != null) {
			for (SaveResult saveResult : saveResultList) {
				if (saveResult.isSuccess() == false) {
					Error[] errors = saveResult.getErrors();

					if (errors != null) {
						String errorMessage = getErrorMessage(errors);

						getLog().debug("FileAttach: " + errorMessage);

						throw new RuntimeException("FileAttach: " + errorMessage);
					}
				}
			}
		}
	}

	private byte[] getFileBytes(String filename) throws IOException {
		byte[] result = null;
		FileInputStream fis = new FileInputStream(new File(filename));
		int readBytes;
		int MAX_LENGTH = 16384;
		byte[] buffer = new byte[MAX_LENGTH];	// 16K buffer for file reading
		
		while ((readBytes = fis.read(buffer)) != -1) {
			if (readBytes == MAX_LENGTH) {
				//full block loaded
				
				if (result == null) {
					result = new byte[MAX_LENGTH];
					
					for (int i = 0; i < readBytes; i++) {
						result[i] = buffer[i];
					}
				} else {
					int i = 0;
					int cnt;
					byte[] tmpResult = new byte[result.length + MAX_LENGTH];
					
					while (i < result.length) {
						tmpResult[i] = result[i];
						i++;
					}
					
					cnt = 0;
					while (cnt < MAX_LENGTH) {
						tmpResult[i] = buffer[cnt];
						
						i++;
						cnt++;
					}
					
					result = tmpResult;
				} 
			} else {
				if (result == null) {
					result = new byte[readBytes];
					
					for (int i = 0; i < readBytes; i++) {
						result[i] = buffer[i];
					}
				} else {
					// non-full block loaded
					byte[] tmpResult = new byte[result.length + readBytes];
					int i = 0;
					int cnt;
					
					while (i < result.length) {
						tmpResult[i] = result[i];
						i++;
					}
					
					cnt = 0;
					while (cnt < readBytes) {
						tmpResult[i] = buffer[cnt];
						
						i++;
						cnt++;
					}
					
					result = tmpResult;
				}
			}
		}
		
		fis.close();
		
		return result;
	}
	
	/** This method is to fix a bug in the WSC SOQL query parser, the where clause must be defined after a newline ! **/
	public static String fixQuery(String query) {
		String patternSelectQueryString = "^\\s*(SELECT.*FROM\\s+[^\\s]+)\\s*(WHERE.*)\\s*$";
		Pattern patternSelectQuery = Pattern.compile(patternSelectQueryString, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher matcherSelectQuery = patternSelectQuery.matcher(query);
		String fixQuery = query;
		
		if (matcherSelectQuery.find()) {
			String selectObject = matcherSelectQuery.group(1);
			String optionalWhere = matcherSelectQuery.group(2);
			
			if (optionalWhere != null && optionalWhere.equals("") == false) {
				fixQuery = selectObject + "\n" + optionalWhere;
			}
		}
		
		return fixQuery;
	}
	
	/** Check if this is a valid SFDC query **/
	public static Boolean isValidQuery(String query) {
		Boolean result = false;
		
		if (query != null) {
			String queryUpper = query.trim().toUpperCase();
			
			if (queryUpper.startsWith("SELECT")) {
				return true;
			}
			
			/*
			String patternSelectQueryString = "^\\s*SELECT\\s+.*$";
			Pattern patternSelectQuery = Pattern.compile(patternSelectQueryString, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
			Matcher matcherSelectQuery = patternSelectQuery.matcher(query);
			
			if (matcherSelectQuery.matches()) {
				result = true;
			}
			*/
		}
		
		return result;
	}
	
	/**
	 * Utility to return a Gregorian Calendar Date that is used to store date + time for SFDC.
	 * 
	 * @param dt
	 * @return
	 */
	public static java.util.GregorianCalendar getGregorianCalendarDate(java.util.Date dt) {
		java.util.GregorianCalendar result = new GregorianCalendar();
		TimeZone tz;

		result.setTime(dt);
		
		tz = result.getTimeZone();
		
		result.add(java.util.GregorianCalendar.MILLISECOND, tz.getDSTSavings());	
		
		return result;
	}

	@Override
	public void setFieldSeparator(char fieldSeparator) throws Exception {
	}

	@Override
	public Schedule getSchedule() {
		return schedule;
	}

	@Override
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}
}
