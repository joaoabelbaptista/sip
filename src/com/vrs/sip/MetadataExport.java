/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: SIP Metadata Export Module.
 * History: aosantos, 2016-07-17, Initial Release.
 * 
 * 
 */
package com.vrs.sip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.opencsv.CSVWriter;
import com.vrs.sip.connection.Field;
import com.vrs.sip.connection.FieldType;
import com.vrs.sip.connection.IResultSet;
import com.vrs.sip.connection.IStatement;
import com.vrs.sip.connection.Record;
import com.vrs.sip.connection.drivers.SalesforceStatement;

public class MetadataExport {
	FileLog log = FileLog.getNewInstance(MetadataExport.class, "metadata_export_" + Util.getSimpleUniqueId(), ".log");
	String baseDirectory;
	String orgId;
	Metadata metadata;
	String exportPathname;
	String archivePathname;
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat filenameSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	
	static Map<String, List<String>> objectFieldMap = initializeObjectFieldMap();
	
	public MetadataExport(String baseDirectory) throws Exception {
		this.baseDirectory = baseDirectory;
		
		metadata = Factory.getMetadataInstance();
		
		metadata.metadataConnection.setLog(log);
		
		orgId = metadata.getOrgId();
		
		exportPathname = baseDirectory + "/" + orgId;
		archivePathname = baseDirectory + "/" + orgId + getArchiveSuffix();
		
		Util.createDirectory(baseDirectory);
		Util.createDirectory(exportPathname);
	}
	
	public static Map<String,List<String>> initializeObjectFieldMap() {
		Map<String,List<String>> resultMap = new HashMap<String,List<String>>();
		
		resultMap.put("connection", new Vector<String>());
		resultMap.put("schedule", new Vector<String>());
		resultMap.put("task_flow", new Vector<String>());
		resultMap.put("task", new Vector<String>());
		resultMap.put("task_step", new Vector<String>());
		resultMap.put("transformation", new Vector<String>());
		resultMap.put("entity", new Vector<String>());
		resultMap.put("entity_field", new Vector<String>());
		
		resultMap.get("connection").add("Name");
		resultMap.get("connection").add("Unique_ID__c");
		resultMap.get("connection").add("Connection_Name__c");
		resultMap.get("connection").add("Connection_Type__c");
		resultMap.get("connection").add("Description__c");
		resultMap.get("connection").add("All_Or_None__c");
		resultMap.get("connection").add("Auto_Commit__c");
		resultMap.get("connection").add("Batch_Size__c");
		resultMap.get("connection").add("Charset__c");
		resultMap.get("connection").add("Date_Format__c");
		resultMap.get("connection").add("Custom_Date_Format__c");
		resultMap.get("connection").add("Directory__c");
		resultMap.get("connection").add("Hostname__c");
		resultMap.get("connection").add("Login_Server__c");
		resultMap.get("connection").add("Login_Timeout__c");
		resultMap.get("connection").add("Password__c");
		resultMap.get("connection").add("Port__c");
		resultMap.get("connection").add("Security_Token__c");
		resultMap.get("connection").add("Service__c");
		resultMap.get("connection").add("Username__c");
		
		resultMap.get("schedule").add("Name");
		resultMap.get("schedule").add("Unique_ID__c");		
		resultMap.get("schedule").add("Schedule_Name__c");
		resultMap.get("schedule").add("Schedule_Type__c");
		resultMap.get("schedule").add("Start_Date__c");
		resultMap.get("schedule").add("End_Date__c");
		resultMap.get("schedule").add("Recurrence__c");
		resultMap.get("schedule").add("Recurrence_Type__c");
		
		resultMap.get("task_flow").add("Name");
		resultMap.get("task_flow").add("Unique_ID__c");
		resultMap.get("task_flow").add("Flow_Name__c");
		resultMap.get("task_flow").add("Schedule__r.Unique_ID__c");
		resultMap.get("task_flow").add("Description__c");
		resultMap.get("task_flow").add("Failure_Emails__c");
		resultMap.get("task_flow").add("Last_Scheduled_Date__c");
		resultMap.get("task_flow").add("Max_Retry_Count__c");
		resultMap.get("task_flow").add("Retry_If_Fail__c");
		resultMap.get("task_flow").add("Success_Emails__c");
		resultMap.get("task_flow").add("Warning_Emails__c");
		
		resultMap.get("task").add("Name");
		resultMap.get("task").add("Unique_ID__c");
		resultMap.get("task").add("Task_Name__c");
		resultMap.get("task").add("Task_Flow__r.Unique_ID__c");
		resultMap.get("task").add("Schedule__r.Unique_ID__c");
		resultMap.get("task").add("Abort_On_Failure__c");
		resultMap.get("task").add("Description__c");
		resultMap.get("task").add("Failure_Emails__c");
		resultMap.get("task").add("Last_Scheduled_Date__c");
		resultMap.get("task").add("Max_Retry_Count__C");
		resultMap.get("task").add("Order__c");
		resultMap.get("task").add("PostProcessing_Script__c");
		resultMap.get("task").add("PreProcessing_Script__c");
		resultMap.get("task").add("Retry_If_Fail__c");
		resultMap.get("task").add("Success_Emails__c");
		resultMap.get("task").add("Warning_Emails__c");
		
		resultMap.get("task_step").add("Name");
		resultMap.get("task_step").add("Unique_ID__c");
		resultMap.get("task_step").add("Step_Type__c");
		resultMap.get("task_step").add("Order__c");
		resultMap.get("task_step").add("Task__r.Unique_ID__c");
		resultMap.get("task_step").add("Source_Connection__r.Unique_ID__c");
		resultMap.get("task_step").add("Target_Connection__r.Unique_ID__c");
		resultMap.get("task_step").add("Operation__c");
		resultMap.get("task_step").add("Operation_Key_Field_List__c");
		resultMap.get("task_step").add("Description__c");
		resultMap.get("task_step").add("Truncate_Target__c");
		resultMap.get("task_step").add("Batch_Size__c");
		resultMap.get("task_step").add("Field_Separator__c");
		
		resultMap.get("entity").add("Name");
		resultMap.get("entity").add("Unique_ID__c");
		resultMap.get("entity").add("Entity_Name__c");
		resultMap.get("entity").add("Entity_Type__c");
		resultMap.get("entity").add("Task_Step__r.Unique_ID__c");
		
		resultMap.get("entity_field").add("Name");
		resultMap.get("entity_field").add("Unique_ID__c");
		resultMap.get("entity_field").add("Entity__r.Unique_ID__c");
		resultMap.get("entity_field").add("Field_Name__c");
		resultMap.get("entity_field").add("Order__c");
		resultMap.get("entity_field").add("Type__c");
		
		resultMap.get("transformation").add("Name");
		resultMap.get("transformation").add("Unique_ID__c");
		resultMap.get("transformation").add("Order__c");
		resultMap.get("transformation").add("Target_Field__c");
		resultMap.get("transformation").add("Task_Step__r.Unique_ID__c");
		resultMap.get("transformation").add("Transformation__c");
		
		return resultMap;
	}
	
	public void exportCSVFiles() throws Exception {
		if (Util.hasFiles(exportPathname)) {
			archiveLastVersion();
		} else {
			log.info("No previous export found for orgId " + orgId + ": skip archive");
		}
		
		exportObjectCSV("SIP_Connection__c", "connection");
		exportObjectCSV("SIP_Schedule__c", "schedule");
		exportObjectCSV("SIP_Task_Flow__c", "task_flow");
		exportObjectCSV("SIP_Task__c", "task");
		exportObjectCSV("SIP_Task_Step__c", "task_step");
		exportObjectCSV("SIP_Entity__c", "entity");
		exportObjectCSV("SIP_Entity_Field__c", "entity_field");
		exportObjectCSV("SIP_Transformation__c", "transformation");
	}
	
	private void archiveLastVersion() {
		log.info("Archiving last export to " + archivePathname);
		
		Util.createDirectory(archivePathname);
		
		// rename files/directories to archive directory
		for (String filename : (new File(exportPathname)).list()) {
			log.info("\tArchiving file: " + filename);
			
			Util.rename(exportPathname + "/" + filename, archivePathname + "/" + filename);
		}
	}
	
	private String getArchiveSuffix() {
		Calendar cal = Calendar.getInstance();
		
		return "." + filenameSimpleDateFormat.format(cal.getTime());
	}
	
	private void exportObjectCSV(String objectName, String filenamePrefix) throws Exception {
		List<String> objectFields = objectFieldMap.get(filenamePrefix);
		IStatement statement;
		IResultSet resultSet;
		String outputFilename = exportPathname + "/" + filenamePrefix + ".csv";
		List<Record> recordList;
		OutputStreamWriter osw;
		CSVWriter writer;
		String[] entries;
		
		log.info("Exporting Object " + objectName + " to file " + outputFilename);
		
		statement = metadata.metadataConnection.createStatement();
		
		((SalesforceStatement)statement).stdoutDebug = Configuration.getInstance().getServer().stdoutDebug;
		
		resultSet = statement.executeQuery(getSelectQuery(objectName, filenamePrefix));
		
		recordList = resultSet.fetchAllRows();
		
		osw = new OutputStreamWriter(new FileOutputStream(outputFilename, false));
		writer = new CSVWriter(osw, ',');
		
		// Header
		entries = new String[objectFields.size()];
		
		for (Integer i = 0; i < entries.length; i++) {
			entries[i] = objectFields.get(i);
		}
		
		writer.writeNext(entries);
		
		// Records
		if (recordList != null && recordList.isEmpty() == false) {
			for (Record row : recordList) {
				for (Integer i = 0; i < entries.length; i++) {
					String fieldName = objectFields.get(i);
					Field field;
					FieldType fieldType;
					String value = null;
					
					entries[i] = null;
					
					field = row.getFieldByName(fieldName);
					fieldType = field.getFieldType();
					
					if (fieldType == FieldType.T_DATE) {
						Date dtValue = field.getDate();
						
						if (dtValue != null) {
							if (simpleDateFormat != null) {
								value = simpleDateFormat.format(dtValue);
							} else {
								value = dtValue.toString();
							}
						}
					} else {
						Object objectValue = field.getValue();
						
						if (objectValue != null) {
							value = String.valueOf(objectValue);
						}
					}

					if (value != null) {
						entries[i] = value;
					}
				}
				
				writer.writeNext(entries);
			}
		}
		
		writer.close();
	}
	
	private String getSelectQuery(String objectName, String filenamePrefix) {
		List<String> objectFields = objectFieldMap.get(filenamePrefix);
		String query = "SELECT " + String.join(",", objectFields) + " FROM " + objectName + " ORDER BY Name";
		
		//System.out.println("query>> " + query);
		
		return query;
	}
}
