/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Salesforce Implementation of a Result Set.
 * History: aosantos, 2016-06-26, Initial Release.
 *          aosantos, 2016-07-20, Fixed SFDC datatype Date parsing.
 * 			aosantos, 2016-09-21, Fixed SOQL query field access when
 *                                queried value is not exactly the
 *                                one for the definition of the field
 *                                in SFDC.
 * 
 */
package com.vrs.sip.connection.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;
import com.vrs.sip.Configuration;
import com.vrs.sip.FileLog;
import com.vrs.sip.connection.Field;
import com.vrs.sip.connection.FieldType;
import com.vrs.sip.connection.IResultSet;
import com.vrs.sip.connection.IStatement;
import com.vrs.sip.connection.Record;

public class SalesforceResultSet implements IResultSet {
	FileLog log;
	
	Integer batchSize;
	QueryResult queryResult;
	List<SObject> bufferRecords;
	String soql;
	List<String> soqlFieldList;
	Map<String,com.sforce.soap.partner.FieldType> soqlFieldTypeMap;
	Map<com.sforce.soap.partner.FieldType,FieldType> fieldTypeMap;
	Map<String,String> soqlFieldNameCaseSensitiveMap;

	Boolean fetchDone;
	Boolean firstQuery;
	String sfdcObject;
	
	public Boolean stdoutDebug = false;

	public void outDebug(String output) {
		if (stdoutDebug) {
			System.out.println(output);
		}
	}
	
	SalesforceStatement salesforceStatement;

	/*
	 * for: A__c.B__r.C__r.f__c
	 * indexed by A__c.B__r.C__r
	 * 
	 * or for: B__r.C__r.f__c
	 * indexed by B__r.C__r
	 * 
	 */
	Map<String,String> queryFieldReferenceMap = new HashMap<String,String>();
	
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
	
	public SalesforceResultSet() {
		fetchDone = false;
		firstQuery = true;
		
		// TOOD: Set the default batch size
		
		batchSize= 200;
		
		bufferRecords = new Vector<SObject>();
		soqlFieldList = new Vector<String>();
		soqlFieldTypeMap = new HashMap<String,com.sforce.soap.partner.FieldType>();
		
		soqlFieldNameCaseSensitiveMap = new HashMap<String,String>();

		fieldTypeMap = new HashMap<com.sforce.soap.partner.FieldType,FieldType>();
		
		fieldTypeMap.put(com.sforce.soap.partner.FieldType._boolean, FieldType.T_BOOLEAN);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType._double, FieldType.T_DECIMAL);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType._int, FieldType.T_INTEGER);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.combobox, FieldType.T_STRING);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.currency, FieldType.T_DECIMAL);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.date, FieldType.T_DATE_WITHOUT_TIME);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.datetime, FieldType.T_DATE);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.email, FieldType.T_STRING);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.id, FieldType.T_STRING);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.multipicklist, FieldType.T_STRING);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.percent, FieldType.T_DECIMAL);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.phone, FieldType.T_STRING);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.picklist, FieldType.T_STRING);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.reference, FieldType.T_STRING);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.string, FieldType.T_STRING);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.textarea, FieldType.T_STRING);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.url, FieldType.T_STRING);
		fieldTypeMap.put(com.sforce.soap.partner.FieldType.encryptedstring, FieldType.T_STRING);
	}

	@Override
	public void setStatement(IStatement statement) throws Exception {
		Integer connectionBatchSize;
		
		salesforceStatement = (SalesforceStatement)statement;
		
		setLog(salesforceStatement.getLog());
		
		connectionBatchSize = salesforceStatement.connection.getConnectionAttributes().getBatchSize();
		
		if (connectionBatchSize != null && connectionBatchSize > 0) {
			setBatchSize(connectionBatchSize);
		}
	}

	@Override
	public List<Record> fetchRows() throws Exception {
		getLog().debug("fetchRows START");
		
		List<Record> rowList = new Vector<Record>();
		Integer counter = 0;
		
		getLog().debug("batchSize=" + batchSize);
		getLog().debug("bufferRecords.isEmpty=" + bufferRecords.isEmpty());
		
		outDebug("batchSize = " + batchSize + ", bufferRecords.isEmpty = " + bufferRecords.isEmpty());
		
		// Push the buffered records
		if (bufferRecords.isEmpty() == false) {
			while (counter < batchSize && bufferRecords.isEmpty() == false) {
				SObject sobjectRecord = bufferRecords.remove(0);
				
				List<Field> fieldList = new Vector<Field>();
				Record record = new Record(fieldList);
				
				for (Integer i = 0; i < soqlFieldList.size(); i++) {
					String fieldName;
					FieldType fieldType;
					Object fieldValue;
					Field field;
					
					fieldName = soqlFieldList.get(i);
					fieldType = fieldTypeMap.get(soqlFieldTypeMap.get(fieldName.toUpperCase()));
					fieldValue = getFieldValue(sfdcObject, sobjectRecord, i);
					
					outDebug("#1 fetchRows::process the buffered record from first query: fieldName = " + fieldName + ", fieldType = " + fieldType + ", fieldValue = " + fieldValue);
					
					field = new Field(fieldName, fieldType, fieldValue);
					
					fieldList.add(field);
					
					record.setFieldList(fieldList);
				}
				
				//getLog().debug("fetchRows:: buffered record push: " + record);
				
				rowList.add(record);
				
				counter++;
			}
		}
		
		getLog().debug("fetchRows::counter=" + counter + ", batchSize=" + batchSize + ", firstQuery=" + firstQuery + ", fetchDone=" + fetchDone);
		
		outDebug("fetchRows:: counter = " + counter + ", batchSize = " + batchSize + ", firstQuery = " + firstQuery + ", fetchDone = " + fetchDone);
		
		if (counter < batchSize) {
			if (firstQuery) {
				SObject[] fetchRecords;
				
				fetchRecords = queryResult.getRecords();
				
				firstQuery = false;
				
				for (Integer i = 0; i < fetchRecords.length; i++) {
					//getLog().debug("fetchRows:: got the following record for first query ==> " + fetchRecords[i]);
					
					/*
					if (Configuration.getInstance().getLog().connection == true) {
						getLog().info("fetched the record: " + fetchRecords[i]);
					}
					*/
					
					//System.out.println("fetched the record: " + fetchRecords[i]);
					
					outDebug("fetchRows:: bufferRecords add of record " + fetchRecords[i]);
					
					bufferRecords.add(fetchRecords[i]);
				}
				
				if (queryResult.isDone()) {
					fetchDone = true;
				}
				
				while (counter < batchSize && bufferRecords.isEmpty() == false) {
					SObject sobjectRecord = bufferRecords.remove(0);
					
					List<Field> fieldList = new Vector<Field>();
					Record record = new Record(fieldList);
					
					//getLog().debug("fetchRows:: got the SObject Record " + sobjectRecord);
					
					for (Integer i = 0; i < soqlFieldList.size(); i++) {
						String fieldName;
						FieldType fieldType;
						Object fieldValue;
						Field field;
						
						fieldName = soqlFieldList.get(i);
						fieldType = fieldTypeMap.get(soqlFieldTypeMap.get(fieldName.toUpperCase()));
						fieldValue = getFieldValue(sfdcObject, sobjectRecord, i);
						
						if (Configuration.getInstance().getLog().connection == true) {
							getLog().info("#2 fetchRows::process the buffered record from first query: fieldName = " + fieldName + ", fieldType = " + fieldType + ", fieldValue = " + fieldValue);
						}
						
						outDebug("#2 fetchRows::process the buffered record from first query: fieldName = " + fieldName + ", fieldType = " + fieldType + ", fieldValue = " + fieldValue);
						
						field = new Field(fieldName, fieldType, fieldValue);
						
						fieldList.add(field);
						
						record.setFieldList(fieldList);
					}
					
					rowList.add(record);
					
					counter++;
				}
			}
			
			// If not all loaded and still some to fetch
			while (fetchDone == false) {
				SObject[] fetchRecords;
				
				if (firstQuery) {
					fetchRecords = queryResult.getRecords();
					
					firstQuery = false;
				} else {
					if (queryResult.isDone() == false) {
						queryResult = salesforceStatement.implConnection.queryMore(queryResult.getQueryLocator());
					}
					
					fetchRecords = queryResult.getRecords();
				}
				
				// Process the Records
				for (Integer i = 0; i < fetchRecords.length; i++) {
					bufferRecords.add(fetchRecords[i]);
				}
				
				if (queryResult.isDone()) {
					fetchDone = true;
				}
				
				// Process the buffered records
				while (counter < batchSize && bufferRecords.isEmpty() == false) {
					SObject sobjectRecord = bufferRecords.remove(0);
					
					List<Field> fieldList = new Vector<Field>();
					Record record = new Record(fieldList);
					
					for (Integer i = 0; i < soqlFieldList.size(); i++) {
						String fieldName;
						FieldType fieldType;
						Object fieldValue;
						Field field;
						
						fieldName = soqlFieldList.get(i);
						fieldType = fieldTypeMap.get(soqlFieldTypeMap.get(fieldName.toUpperCase()));
						fieldValue = getFieldValue(sfdcObject, sobjectRecord, i);
						
						//getLog().debug("fetchRows::process the buffered record: fieldName = " + fieldName + ", fieldType = " + fieldType + ", fieldValue = " + fieldValue);
						
						if (Configuration.getInstance().getLog().connection == true) {
							getLog().info("#3 fetchRows::process the buffered record from first query: fieldName = " + fieldName + ", fieldType = " + fieldType + ", fieldValue = " + fieldValue);
						}
						
						outDebug("#3 fetchRows::process the buffered record from first query: fieldName = " + fieldName + ", fieldType = " + fieldType + ", fieldValue = " + fieldValue);
						
						field = new Field(fieldName, fieldType, fieldValue);
						
						fieldList.add(field);
						
						record.setFieldList(fieldList);
					}
					
					rowList.add(record);
					
					counter++;
				}
				
				if (counter >= batchSize) {
					break;
				}
			}
		}
				
		getLog().debug("fetchRows END");
		
		return rowList;
	}

	@Override
	public List<Record> fetchAllRows() throws Exception {
		getLog().debug("fetchAllRows START");
		
		List<Record> rowList = new Vector<Record>();
		List<Record> batchRowList;
		
		do {
			batchRowList = fetchRows();
			
			if (batchRowList != null && batchRowList.isEmpty() == false) {
				/*
				for (Record record : batchRowList) {
					getLog().debug("fetchAllRows::" + record);
				}
				*/
				
				if (stdoutDebug) {
					for (Record record : batchRowList) {
						outDebug("fetchAllRows:: got the record " + record);
					}
				}
				
				for (Record record : batchRowList) {
					log.debug("fetchAllRows:: got the record " + record);
				}
				
				rowList.addAll(batchRowList);
			}
		} while (batchRowList != null && batchRowList.isEmpty() == false);
		
		getLog().debug("Got " + rowList.size() + " Rows");
		
		getLog().debug("fetchAllRows END");
		
		return rowList;
	}

	@Override
	public void setBatchSize(Integer batchSize) throws Exception {
		this.batchSize = batchSize;
	}
	
	public void setSalesforceQueryResult(QueryResult queryResult) {
		this.queryResult = queryResult;
	}
	
	public void setSalesforceQuery(String soql) throws ConnectionException, FileNotFoundException, IOException {
		this.soql = soql;
		
		extractSoqlMetadata(soql);
	}
	
	private String normalizeSoqlQuery(String query) {
		String result = "";
		
		result = query.replace("\r", " ").replace("\n", " ").replace("\t", " ");
		
		return result;
	}
	
	private void extractSoqlMetadata(String query) throws ConnectionException, FileNotFoundException, IOException {
		String soql = normalizeSoqlQuery(query);
		//String patternSelectFieldsString = "^SELECT\\s+(.*)\\s+FROM\\s+([a-zA-Z0-9_]+)(\\s+WHERE.*(\\s+ORDER BY.*)?)?\\s*$";
		String patternSelectFieldsString = "^SELECT\\s+(.+)\\s+FROM\\s+([^\\s]+)";
		Pattern patternSelectFields = Pattern.compile(patternSelectFieldsString, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher matchSelectFields = patternSelectFields.matcher(soql);
		
		outDebug("extractSoqlMetadata::soql=" + soql);
		
		if (Configuration.getInstance().getLog().connection == true) {
			getLog().info("soql=" + soql);
		}
		
		if (matchSelectFields.find()) {
			String selectFields = matchSelectFields.group(1);
			String salesforceEntity = matchSelectFields.group(2);			
			String[] tmpFields = selectFields.split(",", -1);
			String[] fields = new String[tmpFields.length];

			outDebug("salesforceEntity=" + salesforceEntity);
			outDebug("selectFields=" + selectFields);
			
			for (int i = 0; i < tmpFields.length; i++) {
				fields[i] = tmpFields[i].trim();
			}
			
			if (Configuration.getInstance().getLog().connection == true) {
				getLog().info("extractSoqlMetadata for Salesforce Entity " + salesforceEntity);
			}
			
			sfdcObject = salesforceEntity;
			
			describeSoqlFields(salesforceEntity, fields);
			describeSoqlEntity(salesforceEntity);

			if (Configuration.getInstance().getLog().connection == true) {
				getLog().info("Fields from SOQL query:");
			}
			
			for (String field : fields) {
				if (Configuration.getInstance().getLog().connection == true) {
					getLog().info("\t" + field + " [type=" + soqlFieldTypeMap.get(field.toUpperCase()) + "]");
				}
				
				outDebug("soqlFieldList add field " + field);
				
				soqlFieldList.add(field);
			}
		}
	}

	private void describeSoqlFields(String entity, String[] fields) throws ConnectionException, FileNotFoundException, IOException {
		DescribeSObjectResult objDescribe;
		com.sforce.soap.partner.Field[] fieldDescribe;
		Map<String,Map<String,com.sforce.soap.partner.FieldType>> typeMap = new HashMap<String,Map<String,com.sforce.soap.partner.FieldType>>(); 
		
		queryFieldReferenceMap.put("", entity);
		queryFieldReferenceMap.put(entity,  entity);
		
		for (String field : fields) {
			String address = extractAddress(field);
			String fieldName = extractFieldName(field);
			String baseEntity;
			
			if (Configuration.getInstance().getLog().connection == true) {
				getLog().info("Address=" + address);
			}
			
			outDebug("Address=" + address);
			
			if (queryFieldReferenceMap.containsKey(address) == false) {
				if (Configuration.getInstance().getLog().connection == true) {
					getLog().info("Address=" + address + " not yet described, building it");
				}
				
				outDebug("Address=" + address + " not yet described, building it");
				
				buildAddressReference(entity, address);
			}
			
			baseEntity = queryFieldReferenceMap.get(address);
			
			if (Configuration.getInstance().getLog().connection == true) {
				getLog().info("Address=" + address + ", FieldName=" + fieldName + ": BaseEntity=" + baseEntity);
			}
			
			outDebug("Address=" + address + ", FieldName=" + fieldName + ": BaseEntity=" + baseEntity);
			
			if (typeMap.containsKey(baseEntity)) {
				Map<String,com.sforce.soap.partner.FieldType> fieldTypeByName = typeMap.get(baseEntity);
				
				if (fieldTypeByName.containsKey(fieldName.toUpperCase())) {
					com.sforce.soap.partner.FieldType fieldType = fieldTypeByName.get(fieldName.toUpperCase());
					
					if (Configuration.getInstance().getLog().connection == true) {
						getLog().info("queryField=" + field + " type=" + fieldType.name());
					}
					
					outDebug("queryField=" + field + ", type=" + fieldType.name());
					
					soqlFieldTypeMap.put(field.toUpperCase(), fieldType);
				}
			} else {
				Map<String,com.sforce.soap.partner.FieldType> fieldTypeByName = new HashMap<String,com.sforce.soap.partner.FieldType>();
				
				objDescribe = salesforceStatement.implConnection.describeSObject(baseEntity);
				fieldDescribe = objDescribe.getFields();
				
				typeMap.put(baseEntity, fieldTypeByName);
				
				for (int i = 0; i < fieldDescribe.length; i++) {
					com.sforce.soap.partner.Field fieldDefinition = fieldDescribe[i];
					String fname = fieldDefinition.getName();
					String sensitiveNameKey = (baseEntity + "." + fname).toUpperCase();
					
					com.sforce.soap.partner.FieldType fieldType = fieldDefinition.getType();
					
					fieldTypeByName.put(fname.toUpperCase(), fieldType);

					soqlFieldNameCaseSensitiveMap.put(sensitiveNameKey, fname);
				}
				
				if (fieldTypeByName.containsKey(fieldName.toUpperCase())) {
					com.sforce.soap.partner.FieldType fieldType = fieldTypeByName.get(fieldName.toUpperCase());
					
					if (Configuration.getInstance().getLog().connection == true) {
						getLog().info("queryField=" + field + " type=" + fieldType.name());
					}
					
					outDebug("queryField=" + field + ", type=" + fieldType.name());
					
					soqlFieldTypeMap.put(field.toUpperCase(), fieldType);
				}
			}
		}
	}
	
	private void buildAddressReference(String baseEntity, String address) throws ConnectionException, FileNotFoundException, IOException {
		for (String prefixAddress : walkAddress(baseEntity, address, false)) {
			String baddr = extractAddress(prefixAddress);
			String faddr = extractFieldName(prefixAddress);
			String describeEntity = null;
			String describeField = null;
			String fieldEntity = null;
			
			if (Configuration.getInstance().getLog().connection == true) {
				getLog().info("BuildAddressReference: baseEntity=" + baseEntity + ", address=" + address + ", baddr=" + baddr + ", faddr=" + faddr);
			}
			
			outDebug("BuildAddressReference: baseEntity=" + baseEntity + ", address=" + address + ", baddr=" + baddr + ", faddr=" + faddr);
			
			if (queryFieldReferenceMap.containsKey(baddr)) {
				describeEntity = queryFieldReferenceMap.get(baddr);
				describeField = faddr;
				
				if (describeField.endsWith("__r")) {
					describeField = describeField.replaceAll("__r$", "__c");
				}
				
				if (Configuration.getInstance().getLog().connection == true) {
					getLog().info("BuildAddressReference: describeEntity=" + describeEntity + ", describeField=" + describeField);
				}
				
				outDebug("BuildAddressReference: describeEntity=" + describeEntity + ", describeField=" + describeField);
				
				fieldEntity = getEntityReferenceType(describeEntity, describeField);
				
				queryFieldReferenceMap.put(prefixAddress, fieldEntity);
			}
		}
	}
	
	private String getEntityReferenceType(String entityName, String fieldName) throws ConnectionException {
		DescribeSObjectResult objDescribe = salesforceStatement.implConnection.describeSObject(entityName);
		com.sforce.soap.partner.Field[] fields = objDescribe.getFields();
		
		for (int i = 0; i < fields.length; i++) {
			com.sforce.soap.partner.Field field = fields[i];
			com.sforce.soap.partner.FieldType fieldType = field.getType();
			 
			if (fieldName.equalsIgnoreCase(field.getName()) || (fieldName + "Id").equalsIgnoreCase(field.getName())) {
				if (fieldType == com.sforce.soap.partner.FieldType.reference) {
					String[] referenceToList = field.getReferenceTo();
					
					if (referenceToList != null && referenceToList.length > 0) {
						return referenceToList[0];
					}
				}
			}			
		} 
		
		return null;
	}
	
	private List<String> walkAddress(String baseEntity, String address, Boolean dontAppend) {
		String[] items = address.split("\\.");
		List<String> walk = new Vector<String>();
		int startIndex = 0;
		
		if (address.startsWith(baseEntity)) {
			startIndex = 1;
		}
		
		for (int i = startIndex; i < items.length; i++) {
			String item = "";
			
			for (int j = 0; j <= i; j++) {
				if (dontAppend == false) {
					if (j > 0) {
						item += ".";
					}
					
					item += items[j];
				} else {
					item = items[j];
				}
			}
			
			walk.add(item);
		}
		
		return walk;
	}
	
	private String extractAddress(String field) {
		String address = null;
		int lastIndexOfDot = field.lastIndexOf(".");
		
		if (lastIndexOfDot > 0) {
			address = field.substring(0, lastIndexOfDot);
		} else {
			address = "";
		}
		
		return address;
	}
	
	private String extractFieldName(String field) {
		String fieldName = null;
		int lastIndexOfDot = field.lastIndexOf(".");
		
		if (lastIndexOfDot > 0) {
			fieldName = field.substring(lastIndexOfDot + 1);
		} else {
			fieldName = field;
		}
		
		return fieldName;
	}
	
	private void describeSoqlEntity(String entity) throws ConnectionException {
		DescribeSObjectResult objDescribe = salesforceStatement.implConnection.describeSObject(entity);
		com.sforce.soap.partner.Field[] fields = objDescribe.getFields();
		
		for (int i = 0; i < fields.length; i++) {
			com.sforce.soap.partner.Field field = fields[i];
			String fieldName = field.getName();
			com.sforce.soap.partner.FieldType fieldType = field.getType();
			
			soqlFieldTypeMap.put(fieldName.toUpperCase(), fieldType);
			
			soqlFieldTypeMap.put((entity + "." + fieldName).toUpperCase(), fieldType);
		}
	}

	private Object getSObjectFieldReference(String sobjectType, SObject sobject, String field) {
		List<String> path;
		Object result = null;
		SObject currentRecord;
		String currentPathItem = "";
		
		outDebug("GetSObjectFieldReference:: sobjectType=" + sobjectType + ", sObject=" + sobject + ", field=" + field);
		
		path = walkAddress(sobjectType, field, true);
		 
		outDebug("GetSObjectFieldReference: path=" + path);

		int pathIndex = 0;
		currentRecord = sobject;
		for (String pathItem : path) {
			if (pathIndex > 0) {
				currentPathItem += ".";
			}
			currentPathItem += pathItem;

			outDebug("GetSObjectFieldReference: pathItem=" + pathItem);
			
			if (path.size() > 1 && pathIndex < path.size() - 1 && pathItem.endsWith("__c") == false) {
				if (currentRecord != null) {
					currentRecord = (SObject)currentRecord.getSObjectField(pathItem);
				}
				
				outDebug("GetSObjectFieldReference: field=" + field + ", pathItem=" + pathItem + ", currentRecord=" + currentRecord);
			} else {
				if (currentRecord != null) {
					XmlObject childField;
					
					outDebug("GetSObjectFieldReference: currentRecord=" + currentRecord);
					
					childField = currentRecord.getChild(pathItem);
					
					outDebug("GetSObjectFieldReference: childField=" + childField);
					
					if (childField == null) {
						throw new RuntimeException("Field " + pathItem + " not found on query.");
					}

					result = currentRecord.getSObjectField(pathItem);
				} else {
					result = null;
				}
				
				outDebug("GetSObjectFieldReference: field=" + field + ", result=" + result);
				
				return result; 
			}
			
			pathIndex++;
		}
		
		return result;
	}
	
	private Object getFieldValue(String sobjectType, SObject sobject, Integer i) throws ParseException {
		Object result = null;
		String fullFieldName = soqlFieldList.get(i);
		com.sforce.soap.partner.FieldType fieldType = soqlFieldTypeMap.get(fullFieldName.toUpperCase());
		FieldType localFieldType = fieldTypeMap.get(fieldType);
		Object fieldValue; 
		String fieldName;
				
		fieldName = extractFieldName(fullFieldName);

		if (fullFieldName.contains(".")) {
			//log.info("fullFieldName = "+ fullFieldName);
			fieldValue = getSObjectFieldReference(sobjectType, sobject, fullFieldName);
		} else {
			String sensitiveNameKey = (sobjectType + "." + fieldName).toUpperCase();
			String sensitiveFieldName = soqlFieldNameCaseSensitiveMap.get(sensitiveNameKey);

			if (sensitiveFieldName == null) {
				sensitiveFieldName = fieldName;
			}

			fieldValue = sobject.getSObjectField(sensitiveFieldName);
		}
		
		if (localFieldType == FieldType.T_BOOLEAN) {
			String resultFieldValue = null;
			
			if (fieldValue != null) {
				resultFieldValue = String.valueOf(fieldValue);
			}
			
			result = (resultFieldValue == null ? false : (resultFieldValue.equalsIgnoreCase("true") ? true : false));
		} else if (localFieldType == FieldType.T_DATE_WITHOUT_TIME) {
			String resultFieldValue = null;
			
			if (fieldValue != null) {
				resultFieldValue = String.valueOf(fieldValue);
			}
			
			if (resultFieldValue != null) {
				Calendar currentCalendarDate = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat sdf2 = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
				String sdfInput = resultFieldValue;
				Calendar fieldCalendarDate = Calendar.getInstance();
				
				try {
					fieldCalendarDate.setTime(sdf.parse(sdfInput));
				} catch (ParseException e) {
					fieldCalendarDate.setTime(sdf2.parse(sdfInput));					
				}
				
				fieldCalendarDate.add(Calendar.MILLISECOND, currentCalendarDate.getTimeZone().getDSTSavings());
				
				result = fieldCalendarDate.getTime();
			}
			
		} else if (localFieldType == FieldType.T_DATE) {
			String resultFieldValue = null;
			
			if (fieldValue != null) {
				resultFieldValue = (String)fieldValue;
			}
			
			if (resultFieldValue != null) {
				Calendar currentCalendarDate = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				String sdfInput = resultFieldValue.replace("T", " ").replace("Z", "+000");
				Calendar fieldCalendarDate = Calendar.getInstance();
				
				fieldCalendarDate.setTime(sdf.parse(sdfInput));
				fieldCalendarDate.add(Calendar.MILLISECOND, currentCalendarDate.getTimeZone().getDSTSavings());
				
				result = fieldCalendarDate.getTime();
			}
		} else if (localFieldType == FieldType.T_DECIMAL) {
			String resultFieldValue = null;
			
			if (fieldValue != null) {
				resultFieldValue = (String)fieldValue;
			}
			
			if (resultFieldValue != null) {
				result = Double.valueOf(resultFieldValue);
			}
		} else if (localFieldType == FieldType.T_INTEGER) {
			String resultFieldValue = null;
			
			if (fieldValue != null) {
				resultFieldValue = (String)fieldValue;
			}
			
			if (resultFieldValue != null) {
				result = Integer.valueOf(resultFieldValue);
			}
		} else if (localFieldType == FieldType.T_STRING) {
			String resultFieldValue = null;
			
			if (fieldValue != null) {
				resultFieldValue = (String)fieldValue;
			}
			
			if (resultFieldValue != null) {
				result = resultFieldValue;
			}
		} else {
			if (fieldType == null) {
				throw new RuntimeException("Salesforce Field " + fullFieldName + " cannot determine the type");
			}
			
			throw new RuntimeException("Salesforce Field Type " + fieldType.name() + " not mapped");
		}
		
		return result;
	}
}
