/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Extract Task Step Implementation.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.task.step;

import com.vrs.sip.Configuration;
import com.vrs.sip.FileLog;
import com.vrs.sip.Logging;
import com.vrs.sip.connection.IConnection;
import com.vrs.sip.connection.IResultSet;
import com.vrs.sip.connection.IStatement;
import com.vrs.sip.connection.Record;
import com.vrs.sip.task.AbstractTaskStep;
import com.vrs.sip.task.DataRecord;
import com.vrs.sip.task.Entity;

import java.util.List;
import java.util.Vector;

/**
 * Extract Task Step. Usually this is the Initial Task Step from a Task.
 * 
 * Using Source Connector extracts data and stores in output batch.
 * 
 * @author aosantos
 *
 */
public class Extract extends AbstractTaskStep {
	FileLog log;
	
	IStatement statement;
	IResultSet resultSet;
	String taskStepType;
	
	String childExecutionId;
	
	String parentId;
	
	@Override
	public void abort() throws Exception {
		if (childExecutionId != null) {
			Logging.abortExecution(childExecutionId);
		}
	}
	
	@Override
	public void finish() throws Exception {
		if (childExecutionId != null) {
			Boolean hasWarnings = false;
			Logging.ExecutionStatus executionStatus;
			
			hasWarnings = getTotalWarnRecords() > 0;
			
			if (hasWarnings) {
				executionStatus = Logging.ExecutionStatus.FinishedWithWarnings;
			} else {
				executionStatus = Logging.ExecutionStatus.FinishedWithSuccess;
			}
			
			Logging.finishExecution(childExecutionId, executionStatus, null, null);
		}
	}
	
	@Override
	public String getTaskStepType() {
		return taskStepType;
	}

	@Override
	public void setTaskStepType(String taskStepType) {
		this.taskStepType = taskStepType;
	}
	
	@Override
	public void setLog(FileLog fileLog) {
		this.log = fileLog;
	}
	
	@Override
	public FileLog getLog() {
		return this.log;
	}
	
	@Override
	public void produceOutputBatch() throws Exception {
		List<Record> sourceRecords;
		IConnection inConnection = getSourceConnection();
		Entity inEntity = getSourceEntity();
		
		inConnection.setLog(log);
		
		info("Extract Batch Execute START");
		
		debug("Extract Step Source Connection = " + inConnection);
		debug("getCredentials =  " + inConnection.getCredentials().getUsername() + "   AND :: " + inConnection.getCredentials().getPassword());
		
		parentId = getParentExecutionId();
		
		if (inEntity == null || inEntity.entityName == null) {
			throw new RuntimeException("Task Source Entity not defined. Please define the file matching pattern as the Source Entity Name.");
		}
		
		if (childExecutionId == null) {
			if (parentId != null) {
				childExecutionId = Logging.prepareExecution(parentId, null, this);
			}
		}
		
		if (statement == null) {

			inConnection.openConnection();
			
			statement = inConnection.createStatement();

			statement.setSchedule(this.getSchedule());
			
			statement.setFieldSeparator(getFieldSeparator());
		}
		
		if (resultSet == null) {

		    //TODO: Transformation of The Entity Name
            log.debug("inEntity.entityName=" + inEntity.entityName);

			resultSet = statement.executeQuery(inEntity.entityName);
		}
		
		sourceRecords = resultSet.fetchRows();

		info("Extracted " + sourceRecords.size() + " records from source");
		
		if (sourceRecords.size() > 0) {

			debug(sourceRecords); //TODO: PUT COMMENTS

			if (Configuration.getInstance().getLog().pipeline == true) {
				for (Record sourceRecord : sourceRecords) {
					info("Extracted Record: " + sourceRecord);
				}
			}
			
			setTotalInRecords(getTotalInRecords() + sourceRecords.size());
		}
		
		if (sourceRecords.isEmpty()) {
			setCompleted(true);
		}
		
		debug("Have nextStep = " + (getNextStep() != null));
		
		if (getNextStep() != null) {
			List<DataRecord> dataRecordList = new Vector<DataRecord>();
			
			for (Record record : sourceRecords) {
				DataRecord dataRecord = new DataRecord(record);
				
				dataRecordList.add(dataRecord);
			}
			
			debug("Passing " + dataRecordList.size() + " records to next Task Step");
			
			setTotalOutRecords(getTotalOutRecords() + dataRecordList.size());
			
			getNextStep().setInputBatch(dataRecordList);
			getNextStep().setCompleted(false);
		}
		
		if (childExecutionId != null) {
			Logging.updateExecution(childExecutionId, Logging.ExecutionStatus.Running, getTotalOutRecords(), getTotalWarnRecords());
		}
		
		info("Extract Batch Execute END");
	}
}
