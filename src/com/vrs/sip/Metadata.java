/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: SIP Metadata.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip;

import com.vrs.sip.connection.*;
import com.vrs.sip.metadata.*;
import com.vrs.sip.task.Schedule;
import com.vrs.sip.task.Task;
import com.vrs.sip.task.TaskFlow;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class Metadata {
	private Log log = LogFactory.getLog(Metadata.class);
	
	IConnection metadataConnection;

	private Boolean isSandbox = false;
	private Record organization;
	
	public Map<String,ScheduleMetadata> scheduleMap;
	public Map<String,ConnectionMetadata> connectionMap;
	public Map<String,EntityMetadata> entityMap;
	public Map<String,EntityFieldMetadata> entityFieldMap;
	public Map<String,TaskMetadata> taskMap;
	public Map<String,TaskFlowMetadata> taskFlowMap;
	public Map<String,TaskStepMetadata> taskStepMap;
	public Map<String,TransformationMetadata> transformationMap;
    public Map<String,TaskFlowsTasksMetadata> taskFlowsTasksMap;
	
	/**
	 * 
	 * @param connection
	 * @param install
	 * @throws Exception
	 */
	public Metadata(IConnection connection, Boolean install) throws Exception {
		initialize(connection, install);
	}
	
	/**
	 * Initialize Metadata.
	 * 
	 * @param isInstallMode - set to true if performing application install.
	 * @throws Exception
	 */
	public void initialize(IConnection connection, Boolean isInstallMode) throws Exception {
		System.out.println("Metadata Initialize, Running in Install Mode = " + isInstallMode);
		
		//log.debug("Metadata Constructor START");
		
		this.metadataConnection = connection;
		
		if (! isInstallMode) {
			reload();
		}
		
		//log.debug("Metadata Constructor END");
	}
	
	public void reload() throws Exception {
		log.debug("Metadata reload START");
		
		scheduleMap = getScheduleMap();
		connectionMap = getConnectionMap();
		entityMap = getEntityMap();
		entityFieldMap = getEntityFieldMap();
		taskMap = getTaskMap();
		taskFlowMap = getTaskFlowMap();
		taskStepMap = getTaskStepMap();
		transformationMap = getTransformationMap();
        taskFlowsTasksMap = getTaskFlowsTasksMap();

		
		log.debug("Metadata reload END");
	}


    public Boolean getIsSandbox() throws Exception {
		Boolean isSandbox = false;
		Record organization = getOrganization();
		
		if (organization != null) {
			if (organization.getFieldNameSet().contains("IsSandbox")) {
				isSandbox = organization.getFieldByName("IsSandbox").getBoolean();
			} else {
				isSandbox = this.isSandbox;
			}
		}
		
		return isSandbox;
	}
	
	public String getOrgId() throws Exception {
		String orgId = null;
		Record organization = getOrganization();
		
		if (organization != null) {
			orgId = organization.getFieldByName("Id").getString();
		}
		
		return orgId;
	}
	
	public Record getOrganization() throws Exception {
		if (organization == null) {
			IStatement statement = metadataConnection.createStatement();
			IResultSet resultSet;
			
			try {
				resultSet = statement.executeQuery("SELECT Id, Name, IsSandbox FROM Organization");
			} catch (Exception e) {
				String loginServer = metadataConnection.getCredentials().getLoginServer();
				
				resultSet = statement.executeQuery("SELECT Id, Name FROM Organization");
				
				if (loginServer.startsWith("https://test.")) {
					isSandbox = true;
				} else if (loginServer.startsWith("https://login.")) {
					isSandbox = false;
				} else if (loginServer.startsWith("https://www.")) {
					isSandbox = false;
				}
			}
			
			List<Record> records = resultSet.fetchAllRows();
			
			if (records != null && records.isEmpty() == false) {
				organization = records.get(0);
			}
		}
		
		return organization;
	}
	
	/** Assuming Connection is open 
	 * @throws Exception **/
	private Map<String,ScheduleMetadata> getScheduleMap() throws Exception {
		HashMap<String,ScheduleMetadata> resultMap = new HashMap<String,ScheduleMetadata>();
		String query = ScheduleMetadata.getEnumeratorQuery();
		IStatement statement = metadataConnection.createStatement();
		IResultSet resultSet = statement.executeQuery(query);
		
		List<Record> allRecords = resultSet.fetchAllRows();

		if (allRecords != null)
		    log.info("Total Number of Schedule Records found = " + allRecords.size());
		
		for (Record record : allRecords) {
			ScheduleMetadata schedule = new ScheduleMetadata(record);
			
			resultMap.put(schedule.id, schedule);
		}
		
		return resultMap;
	}
	
	private Map<String,ConnectionMetadata> getConnectionMap() throws Exception {
		HashMap<String,ConnectionMetadata> resultMap = new HashMap<String,ConnectionMetadata>();
		String query = ConnectionMetadata.getEnumeratorQuery();
		IStatement statement = metadataConnection.createStatement();
		IResultSet resultSet = statement.executeQuery(query);
		
		List<Record> allRecords = resultSet.fetchAllRows();
		
		for (Record record : allRecords) {
			ConnectionMetadata connection = new ConnectionMetadata(record);
			
			resultMap.put(connection.id, connection);
		}
		
		return resultMap;
	}
	
	private Map<String,EntityMetadata> getEntityMap() throws Exception {
		HashMap<String,EntityMetadata> resultMap = new HashMap<String,EntityMetadata>();
		String query = EntityMetadata.getEnumeratorQuery();
		IStatement statement = metadataConnection.createStatement();
		IResultSet resultSet = statement.executeQuery(query);
		
		List<Record> allRecords = resultSet.fetchAllRows();
		
		for (Record record : allRecords) {
			EntityMetadata entity = new EntityMetadata(record);
			
			resultMap.put(entity.id, entity);
		}
		
		return resultMap;
	}
	
	private Map<String,EntityFieldMetadata> getEntityFieldMap() throws Exception {
		HashMap<String,EntityFieldMetadata> resultMap = new HashMap<String,EntityFieldMetadata>();
		String query = EntityFieldMetadata.getEnumeratorQuery();
		IStatement statement = metadataConnection.createStatement();
		IResultSet resultSet = statement.executeQuery(query);
		
		List<Record> allRecords = resultSet.fetchAllRows();
		
		for (Record record : allRecords) {
			EntityFieldMetadata entityField = new EntityFieldMetadata(record);
			
			resultMap.put(entityField.id, entityField);
		}
		
		return resultMap;
	}
	
	private Map<String,TaskMetadata> getTaskMap() throws Exception {
		HashMap<String,TaskMetadata> resultMap = new HashMap<String,TaskMetadata>();
		String query = TaskMetadata.getEnumeratorQuery();
		IStatement statement = metadataConnection.createStatement();
		IResultSet resultSet = statement.executeQuery(query);
		
		List<Record> allRecords = resultSet.fetchAllRows();
		
		for (Record record : allRecords) {
			TaskMetadata task = new TaskMetadata(record);
			
			resultMap.put(task.id, task);
		}
		
		return resultMap;
	}
	
	private Map<String,TaskFlowMetadata> getTaskFlowMap() throws Exception {
		HashMap<String,TaskFlowMetadata> resultMap = new HashMap<String,TaskFlowMetadata>();
		String query = TaskFlowMetadata.getEnumeratorQuery();
		IStatement statement = metadataConnection.createStatement();
		IResultSet resultSet = statement.executeQuery(query);
		
		List<Record> allRecords = resultSet.fetchAllRows();
		
		for (Record record : allRecords) {
			TaskFlowMetadata taskFlow = new TaskFlowMetadata(record);
			
			resultMap.put(taskFlow.id, taskFlow);
		}
		
		return resultMap;
	}
	
	private Map<String,TaskStepMetadata> getTaskStepMap() throws Exception {
		HashMap<String,TaskStepMetadata> resultMap = new HashMap<String,TaskStepMetadata>();
		String query = TaskStepMetadata.getEnumeratorQuery();
		IStatement statement = metadataConnection.createStatement();
		IResultSet resultSet = statement.executeQuery(query);
		
		List<Record> allRecords = resultSet.fetchAllRows();
		
		for (Record record : allRecords) {
			TaskStepMetadata taskStep = new TaskStepMetadata(record);
			
			resultMap.put(taskStep.id, taskStep);
		}
		
		return resultMap;
	}
	
	private Map<String,TransformationMetadata> getTransformationMap() throws Exception {
		HashMap<String,TransformationMetadata> resultMap = new HashMap<String,TransformationMetadata>();
		String query = TransformationMetadata.getEnumeratorQuery();
		IStatement statement = metadataConnection.createStatement();
		IResultSet resultSet = statement.executeQuery(query);
		
		List<Record> allRecords = resultSet.fetchAllRows();
		
		for (Record record : allRecords) {
			TransformationMetadata transformation = new TransformationMetadata(record);
			
			resultMap.put(transformation.id, transformation);
		}
		
		return resultMap;
	}

    private Map<String,TaskFlowsTasksMetadata> getTaskFlowsTasksMap() throws Exception  {

        HashMap<String,TaskFlowsTasksMetadata> resultMap = new HashMap<String,TaskFlowsTasksMetadata>();
        String query = TaskFlowsTasksMetadata.getEnumeratorQuery();
        IStatement statement = metadataConnection.createStatement();
        IResultSet resultSet = statement.executeQuery(query);

        List<Record> allRecords = resultSet.fetchAllRows();

        for (Record record : allRecords) {
            TaskFlowsTasksMetadata taskFlowsTasksMetadata = new TaskFlowsTasksMetadata(record);

            resultMap.put(taskFlowsTasksMetadata.id, taskFlowsTasksMetadata);
        }

        return resultMap;


    }
	
	public void saveTaskFlow(TaskFlow taskFlow) throws Exception {
		List<Record> taskFlowRecordList = new Vector<Record>();
		List<String> updateKeyList = new Vector<String>();
		IStatement saveStatement = metadataConnection.createStatement();
		List<Field> fieldList = new Vector<Field>();
		List<String> saveFieldList = new Vector<String>();
		
		updateKeyList.add(TaskFlowMetadata.getKeyFieldName());


		fieldList.add(new Field("Id", FieldType.T_STRING, taskFlow.taskFlowId));
		fieldList.add(new Field("Last_Scheduled_Date__c", FieldType.T_DATE, taskFlow.lastScheduleDate));
		
		saveFieldList.add("Id");
		saveFieldList.add("Last_Scheduled_Date__c");
		
		taskFlowRecordList.add(new Record(fieldList));
		
		log.debug("Saving Task Flow " + taskFlow.taskFlowId);
		
		saveStatement.executeOperation(StatementOperationType.Update, 1, TaskFlowMetadata.getEntityName(), saveFieldList, taskFlowRecordList, updateKeyList);

        calculateAndSaveLastStatusSchedule(taskFlow);
	}
	
	public void saveTask(Task task) throws Exception {
		List<Record> taskRecordList = new Vector<Record>();
		List<String> updateKeyList = new Vector<String>();
		IStatement saveStatement = metadataConnection.createStatement();
		List<Field> fieldList = new Vector<Field>();
		List<String> saveFieldList = new Vector<String>();
		
		updateKeyList.add(TaskMetadata.getKeyFieldName());
		
		fieldList.add(new Field("Id", FieldType.T_STRING, task.taskId));
		fieldList.add(new Field("Last_Scheduled_Date__c", FieldType.T_DATE, task.lastScheduleDate));
		
		saveFieldList.add("Id");
		saveFieldList.add("Last_Scheduled_Date__c");
		
		taskRecordList.add(new Record(fieldList));
		
		log.debug("Saving Task " + task.taskId);
		
		log.debug("Task Details = " + taskRecordList.get(0));
		
		saveStatement.executeOperation(StatementOperationType.Update, 1, TaskMetadata.getEntityName(), saveFieldList, taskRecordList, updateKeyList);

        calculateAndSaveLastStatusSchedule(task);

	}

    private void calculateAndSaveLastStatusSchedule(Task task) throws Exception {
        Schedule schedule = task.getSchedule();

        if ( schedule != null ){

            log.debug("schedule.endDate = " + schedule.endDate);
            log.debug("new Date() = " + new Date());
            log.debug("schedule.endDate.after(new Date()) = " + schedule.endDate);



            if ( (schedule.scheduleType  == Schedule.ScheduleType.OneTime) || ( schedule.scheduleType  == Schedule.ScheduleType.Recurrent && schedule.endDate!=null && new Date().after(schedule.endDate) ))
                saveLastStatusSchedule(schedule, task.lastScheduleDate, ScheduleMetadata.Status.COMPLETED);
            else
                saveLastStatusSchedule(schedule, task.lastScheduleDate, ScheduleMetadata.Status.IN_PROGRESS);
        }
    }

    private void calculateAndSaveLastStatusSchedule(TaskFlow taskFlow) throws Exception {
        Schedule schedule = taskFlow.getSchedule();

        if ( schedule != null ){

            if ( (schedule.scheduleType  == Schedule.ScheduleType.OneTime) || ( schedule.scheduleType  == Schedule.ScheduleType.Recurrent && schedule.endDate!=null && new Date().after(schedule.endDate) ))
                saveLastStatusSchedule(schedule, taskFlow.lastScheduleDate, ScheduleMetadata.Status.COMPLETED);
            else
                saveLastStatusSchedule(schedule, taskFlow.lastScheduleDate, ScheduleMetadata.Status.IN_PROGRESS);


        }
    }


    public void saveLastStatusSchedule(Schedule schedule, Date lastScheduleDate, ScheduleMetadata.Status status) throws Exception {

        if (schedule == null)
            return;

        List<Record> scheduleRecordList = new Vector<Record>();
        List<String> updateKeyList = new Vector<String>();
        IStatement saveStatement = metadataConnection.createStatement();
        List<Field> fieldList = new Vector<Field>();
        List<String> saveFieldList = new Vector<String>();

        updateKeyList.add(TaskMetadata.getKeyFieldName());

        fieldList.add(new Field("Id", FieldType.T_STRING, schedule.scheduleId));
        fieldList.add(new Field("Last_Scheduled_Date__c", FieldType.T_DATE, lastScheduleDate));
        fieldList.add(new Field("Last_Finished_Running_Date__c", FieldType.T_DATE, new Date()));
        fieldList.add(new Field("Status__c", FieldType.T_STRING, status.toString()));

        saveFieldList.add("Id");
        saveFieldList.add("Last_Scheduled_Date__c");
        saveFieldList.add("Last_Finished_Running_Date__c");
        saveFieldList.add("Status__c");


        scheduleRecordList.add(new Record(fieldList));

        log.debug("Saving Schedule " + schedule.scheduleId);



        saveStatement.executeOperation(StatementOperationType.Update, 1, ScheduleMetadata.getEntityName(), saveFieldList, scheduleRecordList, updateKeyList);
    }


    public void saveBeginExecution(Schedule schedule, ScheduleMetadata.Status status) throws Exception {

	    log.info("TASKFLOW SCHEDULE = " + schedule );
	    if (schedule == null)
	        return;

        List<Record> scheduleRecordList = new Vector<Record>();
        List<String> updateKeyList = new Vector<String>();
        IStatement saveStatement = metadataConnection.createStatement();
        List<Field> fieldList = new Vector<Field>();
        List<String> saveFieldList = new Vector<String>();

        updateKeyList.add(TaskMetadata.getKeyFieldName());



        fieldList.add(new Field("Id", FieldType.T_STRING, schedule.scheduleId));
        fieldList.add(new Field("Status__c", FieldType.T_STRING, status.toString()));
        fieldList.add(new Field("Last_Scheduled_Date__c", FieldType.T_DATE,new Date(0, 0, 0, 0, 0)));
        fieldList.add(new Field("Last_Finished_Running_Date__c", FieldType.T_DATE, new Date(0, 0, 0, 0, 0)));

        saveFieldList.add("Id");
        saveFieldList.add("Status__c");

        scheduleRecordList.add(new Record(fieldList));

        log.info("Saving Schedule Status = " + status.toString());
        log.info("Saving Schedule Id ..... " + schedule.scheduleId);



        saveStatement.executeOperation(StatementOperationType.Update, 1, ScheduleMetadata.getEntityName(), saveFieldList, scheduleRecordList, updateKeyList);


    }
}
