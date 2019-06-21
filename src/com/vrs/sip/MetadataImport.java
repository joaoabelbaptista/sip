/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: SIP Metadata Import Module.
 * History: aosantos, 2016-07-18, Initial Release.
 * 
 * 
 */
package com.vrs.sip;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.opencsv.CSVReader;
import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;
import com.vrs.sip.connection.Field;
import com.vrs.sip.connection.FieldType;
import com.vrs.sip.connection.Record;
import com.vrs.sip.connection.Record.IllegalRecordFieldIndex;
import com.vrs.sip.connection.Record.IllegalRecordFieldName;

public class MetadataImport {
	FileLog log = FileLog.getNewInstance(MetadataExport.class, "metadata_import_" + Util.getSimpleUniqueId(), ".log");
	Metadata metadata;
	String baseDirectory;
	String importPathname;
	PartnerConnection connection = (PartnerConnection)Factory.getMetadataInstance().metadataConnection.getImplConnection();
	
	static Map<String,Map<String,FieldType>> objectFieldTypeMap = initializeObjectFieldTypeMap();
	static Map<String, List<String>> objectFieldMap = MetadataExport.initializeObjectFieldMap();

	public static class ObjectReferenceLookupDefinition {
		public String objectName;
		public String upsertField;
		public String sourceUpsertField;
		public String idField;
		
		public ObjectReferenceLookupDefinition(
				String objectName,
				String upsertField,
				String sourceUpsertField,
				String idField
		) {
			this.objectName = objectName;
			this.upsertField = upsertField;
			this.sourceUpsertField = sourceUpsertField;
			this.idField = idField;
		}
	}
	
	public static class ObjectUpsertDefinition {
		public String objectName;
		public String upsertField;
		public List<ObjectReferenceLookupDefinition> referenceDefinitionList;
		
		public ObjectUpsertDefinition(String objectName, String upsertField) {
			this.objectName = objectName;
			this.upsertField = upsertField;
			
			referenceDefinitionList = new Vector<ObjectReferenceLookupDefinition>();
		}
	}

	static Map<String,ObjectUpsertDefinition> upsertMap = initializeUpsertMap();
	
	public MetadataImport(String baseDirectory, String orgId) {
		this.baseDirectory = baseDirectory;
		
		metadata = Factory.getMetadataInstance();
		
		metadata.metadataConnection.setLog(log);
		
		importPathname = baseDirectory + "/" + orgId;
	}
	
	public static Map<String,ObjectUpsertDefinition> initializeUpsertMap() {
		Map<String,ObjectUpsertDefinition> resultMap = new HashMap<String,ObjectUpsertDefinition>();
		
		resultMap.put("connection", new MetadataImport.ObjectUpsertDefinition("SIP_Connection__c", "Unique_ID__c"));
		resultMap.put("schedule", new MetadataImport.ObjectUpsertDefinition("SIP_Schedule__c", "Unique_ID__c"));
		resultMap.put("task_flow", new MetadataImport.ObjectUpsertDefinition("SIP_Task_Flow__c", "Unique_ID__c"));
		resultMap.put("task", new MetadataImport.ObjectUpsertDefinition("SIP_Task__c", "Unique_ID__c"));
		resultMap.put("task_step", new MetadataImport.ObjectUpsertDefinition("SIP_Task_Step__c", "Unique_ID__c"));
		resultMap.put("transformation", new MetadataImport.ObjectUpsertDefinition("SIP_Transformation__c", "Unique_ID__c"));
		resultMap.put("entity", new MetadataImport.ObjectUpsertDefinition("SIP_Entity__c", "Unique_ID__c"));
		resultMap.put("entity_field", new MetadataImport.ObjectUpsertDefinition("SIP_Entity_Field__c", "Unique_ID__c"));

		resultMap.get("task_flow").referenceDefinitionList.add(
				new ObjectReferenceLookupDefinition(
						"SIP_Schedule__c",
						"Unique_ID__c",
						"Schedule__r.Unique_ID__c",
						"Schedule__c"
				)
		);
		
		resultMap.get("task").referenceDefinitionList.add(
				new ObjectReferenceLookupDefinition(
						"SIP_Task_Flow__c",
						"Unique_ID__c",
						"Task_Flow__r.Unique_ID__c",
						"Task_Flow__c"
				)
		);
		
		resultMap.get("task").referenceDefinitionList.add(
				new ObjectReferenceLookupDefinition(
						"SIP_Schedule__c",
						"Unique_ID__c",
						"Schedule__r.Unique_ID__c",
						"Schedule__c"
				)
		);
		
		resultMap.get("task_step").referenceDefinitionList.add(
				new ObjectReferenceLookupDefinition(
						"SIP_Task__c",
						"Unique_ID__c",
						"Task__r.Unique_ID__c",
						"Task__c"
				)
		);
		
		resultMap.get("task_step").referenceDefinitionList.add(
				new ObjectReferenceLookupDefinition(
						"SIP_Connection__c",
						"Unique_ID__c",
						"Source_Connection__r.Unique_ID__c",
						"Source_Connection__c"
				)
		);

		resultMap.get("task_step").referenceDefinitionList.add(
				new ObjectReferenceLookupDefinition(
						"SIP_Connection__c",
						"Unique_ID__c",
						"Target_Connection__r.Unique_ID__c",
						"Target_Connection__c"
				)
		);
		
		resultMap.get("transformation").referenceDefinitionList.add(
				new ObjectReferenceLookupDefinition(
						"SIP_Task_Step__c",
						"Unique_ID__c",
						"Task_Step__r.Unique_ID__c",
						"Task_Step__c"
				)
		);
		
		resultMap.get("entity").referenceDefinitionList.add(
				new ObjectReferenceLookupDefinition(
						"SIP_Task_Step__c",
						"Unique_ID__c",
						"Task_Step__r.Unique_ID__c",
						"Task_Step__c"
				)
		);
		
		resultMap.get("entity_field").referenceDefinitionList.add(
				new ObjectReferenceLookupDefinition(
						"SIP_Entity__c",
						"Unique_ID__c",
						"Entity__r.Unique_ID__c",
						"Entity__c"
				)
		);		
		
		return resultMap;
	}
	
	// Only define the non-String fields
	public static Map<String,Map<String,FieldType>> initializeObjectFieldTypeMap() {
		Map<String,Map<String,FieldType>> resultMap = new HashMap<String,Map<String,FieldType>>();
		
		resultMap.put("connection", new HashMap<String,FieldType>());
		resultMap.put("schedule", new HashMap<String,FieldType>());
		resultMap.put("task_flow", new HashMap<String,FieldType>());
		resultMap.put("task", new HashMap<String,FieldType>());
		resultMap.put("task_step", new HashMap<String,FieldType>());
		resultMap.put("transformation", new HashMap<String,FieldType>());
		resultMap.put("entity", new HashMap<String,FieldType>());
		resultMap.put("entity_field", new HashMap<String,FieldType>());
		
		resultMap.get("connection").put("All_Or_None__c", FieldType.T_BOOLEAN);
		resultMap.get("connection").put("Auto_Commit__c", FieldType.T_BOOLEAN);
		
		resultMap.get("schedule").put("End_Date__c", FieldType.T_DATE);
		resultMap.get("schedule").put("Start_Date__c", FieldType.T_DATE);
		
		resultMap.get("task_flow").put("Last_Scheduled_Date__c", FieldType.T_DATE);
		resultMap.get("task_flow").put("Retry_If_Fail__c", FieldType.T_BOOLEAN);
		
		resultMap.get("task").put("Abort_On_Failure__c", FieldType.T_BOOLEAN);
		resultMap.get("task").put("Last_Scheduled_Date__c", FieldType.T_DATE);
		resultMap.get("task").put("Retry_If_Fail__c", FieldType.T_BOOLEAN);
		
		resultMap.get("task_step").put("Truncate_Target__c", FieldType.T_BOOLEAN);

		return resultMap;
	}
	
	public FieldType getFieldType(String objectType, String fieldName) {
		Map<String,FieldType> fieldTypeMap = objectFieldTypeMap.get(objectType);
		FieldType fieldType = FieldType.T_STRING;
		
		if (fieldTypeMap != null) {
			fieldType = fieldTypeMap.get(fieldName);
		}
		
		return fieldType;
	}
	
	public void importCSVFiles() throws Exception {
		if (Util.hasFiles(importPathname)) {
			deleteAllMetadata();
			
			importObjectCSV("SIP_Connection__c", "connection");
			importObjectCSV("SIP_Schedule__c", "schedule");
			importObjectCSV("SIP_Task_Flow__c", "task_flow");
			importObjectCSV("SIP_Task__c", "task");
			importObjectCSV("SIP_Task_Step__c", "task_step");
			importObjectCSV("SIP_Entity__c", "entity");
			importObjectCSV("SIP_Entity_Field__c", "entity_field");
			importObjectCSV("SIP_Transformation__c", "transformation");
		}
	}
	
	private void importObjectCSV(String objectName, String filenamePrefix) throws IOException, ConnectionException, IllegalRecordFieldIndex, IllegalRecordFieldName, ParseException {
		String inputFilename = importPathname + "/" + filenamePrefix + ".csv";
		int counter = 0;
		String[] recordFields;
		InputStreamReader inputStreamReader;
		CSVReader reader;
		List<String> header = new Vector<String>();
		List<Record> recordList = new Vector<Record>();
		ObjectUpsertDefinition upsertDefinition = upsertMap.get(filenamePrefix);
		
		log.info("Importing Object " + objectName + " from file " + inputFilename);
		
		inputStreamReader = new InputStreamReader(new FileInputStream(inputFilename));
		
		reader = new CSVReader(inputStreamReader);
		
		while ((recordFields = reader.readNext()) != null) {
			counter++;
			
			if (counter == 1) {
				// Header Record
				for (String columnName : recordFields) {
					header.add(columnName);
				}
			} else {
				// Detail Record
				Record record;
				int columnIndex = -1;
				List<Field> fieldList = new Vector<Field>();
				Boolean hasUniqueId = false;
				//String fields = null;
				
				for (String columnValue : recordFields) {
					String columnName;
					
					columnIndex++;
					
					columnName = header.get(columnIndex);
	
					if (columnName.equals("Unique_ID__c")) {
						hasUniqueId = true;
					}
					
					fieldList.add(getField(filenamePrefix, columnName, columnValue));
					
					/*
					if (fields == null) {
						fields = "";
					} else {
						fields += ",";
					}
					
					fields += columnName;
					*/
				}
				
				//log.info("Inserting Record " + objectName + " with fields " + fields);
				
				if (hasUniqueId) {
					record = new Record(fieldList);
					
					recordList.add(record);
				}
			}
		}
	
		reader.close();
		
		upsertRecords(
				recordList,
				upsertDefinition.objectName,
				upsertDefinition.upsertField,
				getLookupObjectTypeList(filenamePrefix),
				getLookupObjectUpsertKeyList(filenamePrefix),
				getLookupUpsertKeyFieldList(filenamePrefix),
				getLookupIdFieldList(filenamePrefix)
		);
		
		log.info("Imported " + recordList.size() + " records");
	}
	
	private Field getField(String filenamePrefix, String fieldName, String fieldValue) throws ParseException {
		FieldType fieldType = FieldType.T_STRING;
		Object fieldObjectValue = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		fieldType = getFieldType(filenamePrefix, fieldName);
		
		if (fieldValue != null && fieldValue.isEmpty() == false) {
			if (fieldType == FieldType.T_BOOLEAN) {
				fieldObjectValue = Boolean.valueOf(fieldValue);
			} else if (fieldType == FieldType.T_DATE) {
				fieldObjectValue = simpleDateFormat.parse(fieldValue);
			} else {
				fieldObjectValue = fieldValue;
			}
		}
		
		return new Field(fieldName, fieldType, fieldObjectValue);
	}
	
	private List<String> getLookupObjectTypeList(String object) {
		List<String> result = new Vector<String>();

		for (ObjectReferenceLookupDefinition lookupDefinition : upsertMap.get(object).referenceDefinitionList) {
			result.add(lookupDefinition.objectName);
		}
		
		return result;
	}
	
	private List<String> getLookupObjectUpsertKeyList(String object) {
		List<String> result = new Vector<String>();

		for (ObjectReferenceLookupDefinition lookupDefinition : upsertMap.get(object).referenceDefinitionList) {
			result.add(lookupDefinition.upsertField);
		}
		
		return result;
	}
	
	private List<String> getLookupUpsertKeyFieldList(String object) {
		List<String> result = new Vector<String>();

		for (ObjectReferenceLookupDefinition lookupDefinition : upsertMap.get(object).referenceDefinitionList) {
			result.add(lookupDefinition.sourceUpsertField);
		}
		
		return result;
	}
	
	private List<String> getLookupIdFieldList(String object) {
		List<String> result = new Vector<String>();
		
		for (ObjectReferenceLookupDefinition lookupDefinition : upsertMap.get(object).referenceDefinitionList) {
			result.add(lookupDefinition.idField);
		}
		
		return result;
	}
	
	private void deleteAllMetadata() throws ConnectionException {
		List<String> deleteObjects = new Vector<String>();
		
		deleteObjects.add("SIP_Child_Execution__c");
		deleteObjects.add("SIP_Execution__c");
		deleteObjects.add("SIP_Transformation__c");
		deleteObjects.add("SIP_Entity_Field__c");
		deleteObjects.add("SIP_Entity__c");
		deleteObjects.add("SIP_Task_Step__c");
		deleteObjects.add("SIP_Task__c");
		deleteObjects.add("SIP_Task_Flow__c");
		deleteObjects.add("SIP_Schedule__c");
		deleteObjects.add("SIP_Connection__c");
		
		for (String objectName : deleteObjects) {
			List<String> objectRecords = getAllRecordIdList(objectName);
			
			if (objectRecords != null && objectRecords.isEmpty() == false) {
				String[] idList = new String[objectRecords.size()];
				
				for (int i = 0; i < objectRecords.size(); i++) {
					idList[i] = objectRecords.get(i);
				}
				
				log.info("Deleting the records from " + objectName);
				
				deleteInBatches(idList);
				
				log.info("Deleted " + idList.length + " records");
			}
		}
	}
	
	private void deleteInBatches(String[] idList) throws ConnectionException {
		int maxBatchSize = 200;
		int batchNo;
		int batchOffset;
		int batchSize;
		String[] batchIdList = null;
		
		if (idList != null && idList.length > 0) {
			batchNo = 1;
			batchOffset = 0;
			
			while (batchOffset < idList.length) {
				if (batchOffset + maxBatchSize < idList.length) {
					batchSize = maxBatchSize;
				} else {
					batchSize = idList.length - batchOffset;
				}
				
				if (batchIdList == null) {
					batchIdList = new String[batchSize];
				} else {
					if (batchIdList.length != batchSize) {
						batchIdList = new String[batchSize];
					}
				}
				
				for (int i = 0; i < batchSize; i++) {
					batchIdList[i] = idList[batchOffset + i];
				}
				
				log.info("Deleting batch " + batchNo + " (" + batchSize + " records)");
				
				checkDeleteErrors(connection.delete(batchIdList));
				
				batchOffset += maxBatchSize;
				batchNo++;
			}
		}
	}
	
	private void checkDeleteErrors(DeleteResult[] deleteResultArray) {
		String errorMessage = null;
		
		if (deleteResultArray != null && deleteResultArray.length > 0) {
			for (DeleteResult deleteResult : deleteResultArray) {
				if (deleteResult != null) {
					if (deleteResult.isSuccess() == false) {
						Error[] errorArray = deleteResult.getErrors();
						
						if (errorArray != null && errorArray.length > 0) {
							for (Error error : errorArray) {
								String[] fields = error.getFields();
								String reportFields = null;
								
								if (fields != null) {
									for (int i = 0; i < fields.length; i++) {
										if (i == 0) {
											reportFields = "";
										}
										
										if (i > 0) {
											reportFields += ",";
										}
										
										reportFields += fields[i];
									}
								}
								
								if (errorMessage == null) {
									errorMessage = "";
								}
								
								if (errorMessage.isEmpty() == false) {
									errorMessage += ", ";
								}
								
								errorMessage += error.getStatusCode() + ": " + error.getMessage();
								
								if (reportFields != null) {
									errorMessage += ": " + reportFields;
								}
							}
						}
					}
				}
			}
		}
	}
	
	private List<SObject> select(String query) throws ConnectionException {
		List<SObject> resultList = new Vector<SObject>();
		QueryResult queryResult = connection.query(query);
		
		if (queryResult != null) {
			SObject[] fetchedRecords;
			
			do {
				fetchedRecords = queryResult.getRecords();
				
				if (fetchedRecords != null && fetchedRecords.length > 0) {
					for (int i = 0; i < fetchedRecords.length; i++) {
						resultList.add(fetchedRecords[i]);
					}
				}
				
				if (queryResult.isDone() == false) {
					queryResult = connection.queryMore(queryResult.getQueryLocator());
				}
			} while (queryResult.isDone() == false);
		}
		
		return resultList;
	}
	
	private List<String> getAllRecordIdList(String sobjectType) throws ConnectionException {
		List<String> resultList = new Vector<String>();
		String query = "SELECT Id FROM " + sobjectType;
		QueryResult queryResult = connection.query(query);
		
		if (queryResult != null) {
			SObject[] fetchedRecords;
			
			do {
				fetchedRecords = queryResult.getRecords();
				
				if (fetchedRecords != null && fetchedRecords.length > 0) {
					for (int i = 0; i < fetchedRecords.length; i++) {
						resultList.add((String)fetchedRecords[i].getSObjectField("Id"));
					}
				}
				
				if (queryResult.isDone() == false) {
					queryResult = connection.queryMore(queryResult.getQueryLocator());
				}
			} while (queryResult.isDone() == false);
		}
	
		return resultList;
	}
	
	/**
	 * Get the element string used in a IN clause on a SOQL query as bind variables are only allowed in APEX.
	 * 
	 * @param elementList
	 * @return
	 */
	private String getInElements(List<String> elementList) {
		String result = "(";
		
		if (elementList != null && elementList.isEmpty() == false) {
			for (int i = 0; i < elementList.size(); i++) {
				if (i > 0) {
					result += ",";
				}
				
				result += "'" + elementList.get(i) + "'";
			}
		} else {
			result += "null";
		}
		
		result += ")";
		
		return result;
	}
	
	/**
	 * 
	 * @param upsertRecordList - list of Records to UPSERT.
	 * @param objectName - name of object target of UPSERT operation.
	 * @param upsertKey - field used as UPSERT key.
	 * @param lookupObjectTypeList - if lookups to be processed, this is the list of ObjectTypes of each lookup.
	 * @param lookupObjectUpsertKeyList - if lookups to be processed, this is the field name on lookup object where the related upsert key is found.
	 * @param lookupUpsertKeyFieldList - if lookups to be processed, this is the field name from source Record where the UPSERT key value is found.
	 * @param lookupIdFieldList - if lookups to be processed, this is the Id field name of the lookup.
	 * @throws ConnectionException 
	 * @throws IllegalRecordFieldName 
	 * @throws IllegalRecordFieldIndex 
	 */
	private void upsertRecords(
			List<Record> upsertRecordList,
			String objectName,
			String upsertKey,
			List<String> lookupObjectTypeList,
			List<String> lookupObjectUpsertKeyList,
			List<String> lookupUpsertKeyFieldList,
			List<String> lookupIdFieldList
	) throws ConnectionException, IllegalRecordFieldIndex, IllegalRecordFieldName {
		List<SObject> upsertSObjectList = new Vector<SObject>();
		
		if (upsertRecordList.isEmpty() == false) {
			for (int i = 0; i < upsertRecordList.size(); i++) {
				SObject sobjectInstance = new SObject();
				
				sobjectInstance.setType(objectName);
				
				upsertSObjectList.add(sobjectInstance);
			}
			
			if (lookupObjectTypeList.isEmpty() == false) {
				for (int i = 0; i < lookupObjectTypeList.size(); i++) {
					Set<String> uniqueLookupValueSet = new HashSet<String>();
					List<String> lookupValueList = new Vector<String>();
					String lookupObjectType = lookupObjectTypeList.get(i);
					String lookupObjectUpsertKeyField = lookupObjectUpsertKeyList.get(i);
					String lookupUpsertKeyField = lookupUpsertKeyFieldList.get(i);
					String lookupIdField = lookupIdFieldList.get(i);
					Map<String,String> idMap = new HashMap<String,String>();
					
					for (int j = 0; j < upsertRecordList.size(); j++) {
						Record upsertRecord = upsertRecordList.get(j);
						Field relatedUpsertKeyField = upsertRecord.getFieldByName(lookupUpsertKeyField);
						String relatedUpsertKey = null;
						
						if (relatedUpsertKeyField != null) {
							relatedUpsertKey = relatedUpsertKeyField.getString();
							
							if (relatedUpsertKey != null && relatedUpsertKey.isEmpty() == false) {
								uniqueLookupValueSet.add(relatedUpsertKey);
							}
						}
					} // for each upsert record
					
					if (uniqueLookupValueSet.isEmpty() == false) {
						lookupValueList.addAll(uniqueLookupValueSet);
						String lookupQuery = "SELECT Id, " + lookupObjectUpsertKeyField + " FROM " + lookupObjectType + "\nWHERE " + lookupObjectUpsertKeyField + " IN " + getInElements(lookupValueList);
						List<SObject> lookupList;
						
						log.info("Performing the SOQL '" + lookupQuery + "' to determine the Lookup IDs");
						
						lookupList = select(lookupQuery);
						
						log.info("Got " + lookupList.size() + " records");
						
						if (lookupList != null && lookupList.isEmpty() == false) {
							for (SObject lookupRecord : lookupList) {
								String id = (String)lookupRecord.getSObjectField("Id");
								String key = (String)lookupRecord.getSObjectField(lookupObjectUpsertKeyField);
								
								idMap.put(key, id);
							}
						}
					} // if has lookup values
					
					for (int k = 0; k < upsertSObjectList.size(); k++) {
						SObject sobject = upsertSObjectList.get(k);
						Record record = upsertRecordList.get(k);
						Field referenceUpsertKeyField = record.getFieldByName(lookupUpsertKeyField);
						
						if (referenceUpsertKeyField != null) {
							Object referenceUpsertKey = referenceUpsertKeyField.getValue();
							
							if (referenceUpsertKey != null) {
								String keyValue = (String)referenceUpsertKey;
								String referenceId = idMap.get(keyValue);
								
								if (! keyValue.isEmpty()) {
									if (referenceId == null) {
										throw new RuntimeException("Error at UPSERT of " + objectName + " Record Index " + k + ", could not find Id of Related Upsert Key " + keyValue + " from input CSV file " + lookupUpsertKeyField);
									}
									
									sobject.setSObjectField(lookupIdField, referenceId);
								}
							}
						}
					} // for each upsert record
				} // for each lookup
			} // if lookups
		} // if have upsert records
		
		if (upsertRecordList != null && upsertRecordList.isEmpty() == false) {
			for (int k = 0; k < upsertRecordList.size(); k++) {
				Record record = upsertRecordList.get(k);
				SObject sobject = upsertSObjectList.get(k);
				List<Field> recordFieldList = record.getFieldList();
				Set<String> lookupUpsertKeyFieldSet = new HashSet<String>();
				
				if (lookupUpsertKeyFieldList != null && lookupUpsertKeyFieldList.isEmpty() == false) {
					lookupUpsertKeyFieldSet.addAll(lookupUpsertKeyFieldList);
				}
				
				// for each one of the fields that are not lookups copy them to SObject
				for (Field field : recordFieldList) {
					String fieldName = field.getName();
					Object fieldValue = field.getValue();
					
					if (lookupUpsertKeyFieldSet.contains(fieldName) == false) {
						if (fieldName.equalsIgnoreCase("Name") == false) {
							sobject.setSObjectField(fieldName, fieldValue);
						}
					}
				} // for each one of the fields that are not lookups
			}
		}
		
		if (upsertSObjectList.isEmpty() == false) {
			SObject[] upsertArray;
			
			upsertArray = new SObject[upsertSObjectList.size()];
			
			for (int i = 0; i < upsertSObjectList.size(); i++) {
				upsertArray[i] = upsertSObjectList.get(i);
			}
			
			log.info("Upserting " + upsertArray.length + " Records in SFDC");
			
			upsertInBatches(upsertKey, upsertArray);
		}
	}
	
	/** Insert records in batches of 200 ... max limit of SFDC per batch **/
	private void upsertInBatches(String upsertKey, SObject[] upsertArray) throws ConnectionException {
		final int maxBatchRecords = 200;
		
		SObject[] batchUpsertArray = null;
		
		log.debug("upsertKey=" + upsertKey + ", upsertArray=" + debugSObjectArray(upsertArray));
		
		if (upsertArray != null) {
			if (upsertArray.length <= maxBatchRecords) {
				checkUpsertErrors(connection.upsert(upsertKey, upsertArray));
			} else {
				int batchOffset = 0;
				int batchLength;
				int batchNo = 1;
				
				while (batchOffset < upsertArray.length) {					
					if (batchOffset + maxBatchRecords > upsertArray.length) {
						batchLength = upsertArray.length - batchOffset;
					} else {
						batchLength = maxBatchRecords;
					}
					
					if (batchUpsertArray == null) {
						batchUpsertArray = new SObject[batchLength];
					} else {
						if (batchLength != batchUpsertArray.length) {
							batchUpsertArray = new SObject[batchLength];
						}
					}
					
					for (int i = 0; i < batchLength; i++) {
						batchUpsertArray[i] = upsertArray[batchOffset + i];
					}
					
					log.info("Upserting batchNo " + batchNo + " (" + batchLength + " records)");
					checkUpsertErrors(connection.upsert(upsertKey, batchUpsertArray));
					
					batchNo++;
					batchOffset += maxBatchRecords;
				}				
			}
		}
	}
	
	private void checkUpsertErrors(UpsertResult[] upsertResultArray) {
		String errorMessage = null;
		
		if (upsertResultArray != null && upsertResultArray.length > 0) {
			for (UpsertResult upsertResult : upsertResultArray) {
				if (upsertResult != null) {
					if (upsertResult.isSuccess() == false) {
						Error[] errorArray = upsertResult.getErrors();
						
						if (errorArray != null && errorArray.length > 0) {
							for (Error error : errorArray) {
								String[] fields = error.getFields();
								String reportFields = null;
								
								if (fields != null) {
									for (int i = 0; i < fields.length; i++) {
										if (i == 0) {
											reportFields = "";
										}
										
										if (i > 0) {
											reportFields += ",";
										}
										
										reportFields += fields[i];
									}
								}
								
								if (errorMessage == null) {
									errorMessage = "";
								}
								
								if (errorMessage.isEmpty() == false) {
									errorMessage += ", ";
								}
								
								errorMessage += error.getStatusCode() + ": " + error.getMessage();
								
								if (reportFields != null) {
									errorMessage += ": " + reportFields;
								}
							}
						}
					}
				}
			}
		}

		if (errorMessage != null) {
			throw new RuntimeException(errorMessage);
		}
	}
	
	/**
	 * Debug a SObject Array.
	 * 
	 * @param sobjectArray
	 * @return
	 */
	private static String debugSObjectArray(SObject[] sobjectArray) {
		String result = "";
		
		if (sobjectArray != null) {
			result = "\t[\n";
			
			for (int arrayIndex = 0; arrayIndex < sobjectArray.length; arrayIndex++) {
				result += debugSObject(arrayIndex, sobjectArray[arrayIndex]);
				result += "\n";
			}
			
			result += "\n\t]\n";
		} else {
			result = " *null array* ";
		}
		
		return result;
	}
	
	/**
	 * Debug a SObject.
	 * 
	 * @param sobject
	 * @return
	 */
	private static String debugSObject(int index, SObject sobject) {
		String result = "";
		Iterator<XmlObject> xmlObjectIterator = sobject.getChildren();
		int counter = 0;
		
		while (xmlObjectIterator.hasNext()) {
			XmlObject xmlObject = xmlObjectIterator.next();
			Object xmlObjectValue = xmlObject.getValue();
			
			counter++;
			
			result += "\t\t" + index + ": '" + xmlObject.getName().getLocalPart() + "': '" + xmlObjectValue + "'\n";
		}
		
		return result;
	}
}
 