/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Archive Task Step Implementation.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.task.step;

import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;

import com.vrs.sip.FileLog;
import com.vrs.sip.Logging;
import com.vrs.sip.Util;
import com.vrs.sip.connection.IConnection;
import com.vrs.sip.task.AbstractTaskStep;
import com.vrs.sip.task.DataRecord;
import com.vrs.sip.task.Entity;
import com.vrs.sip.task.EntityField;
import com.vrs.sip.task.Transformation;
import com.vrs.sip.task.TransformationEngine;

public class File extends AbstractTaskStep {
	FileLog log;
	String taskStepType;
	String childExecutionId;
	String parentExecutionId;
	String parentId;
	
	static Set<String> validFileOperationSet = initializeValidFileOperationSet();
	
	static Set<String> initializeValidFileOperationSet() {
		Set<String> resultSet = new HashSet<String>();
		
		resultSet.add("copy");
		resultSet.add("move");
		resultSet.add("delete");
		
		return resultSet;
	}
	
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
	public void setLog(FileLog fileLog) {
		this.log = fileLog;
	}
	
	@Override
	public FileLog getLog() {
		return this.log;
	}
	
	@Override
	public void produceOutputBatch() throws Exception {
		List<DataRecord> sourceRecords;
		IConnection outConnection = getTargetConnection();
		Entity outEntity = getTargetEntity();
		Integer totalRecords;
		Integer totalFailedRecords;
		
		info("File Batch Execute START");
		
		parentId = getParentExecutionId();
		
		if (childExecutionId == null) {
			if (parentId != null) {
				childExecutionId = Logging.prepareExecution(parentId, null, this);
			}
		}
		
		sourceRecords = getInputBatch();
		
		info("Total Source Records: " + (sourceRecords != null ? sourceRecords.size() : 0));
		
		if (sourceRecords != null && sourceRecords.isEmpty() == false) {
			totalFailedRecords = 0;
			
			setTotalInRecords(getTotalInRecords() + sourceRecords.size());
			
			for (DataRecord record : sourceRecords) {
				JexlEngine jexl = TransformationEngine.getJexl();
				JexlContext jexlContext = TransformationEngine.getNewContext();
				
				for (String inputVariable : record.getFields()) {
					jexlContext.set(inputVariable.toUpperCase(), record.getFieldValue(inputVariable));
				}
				
				for (Transformation transformation : transformationList) {
					JexlExpression expression = jexl.createExpression(transformation.transformation);
					EntityField outField = outEntity.getField(transformation.targetFieldName);
					Object value = null;
					
					if (outField == null) {
						throw new RuntimeException("Cannot find field " + transformation.targetFieldName + " on Target Entity");
					}
					
					switch (outField.entityFieldType) {
						case Boolean:
							value = TransformationEngine.evaluateBoolean(expression, jexlContext);
							break;
							
						case Date:
							value = TransformationEngine.evaluateDate(expression, jexlContext);
							break;
							
						case Decimal:
							value = TransformationEngine.evaluateDouble(expression, jexlContext);
							break;
							
						case Integer:
							value = TransformationEngine.evaluateInteger(expression, jexlContext);
							break;
							
						case String:
							value = TransformationEngine.evaluateString(expression, jexlContext);
							break;
					}
					
					record.setFieldValue(transformation.targetFieldName, value);
					
					// feed to input context
					jexlContext.set(transformation.targetFieldName, value);
				}
			}
			
			for (DataRecord record : sourceRecords) {
				String fileOperation;
				String outputDirectory;
				String inputFilename;
				String outputFilename;
				Set<String> inputFields = record.getFields();
				
				outputDirectory = outConnection.getConnectionAttributes().getDirectory();
				outputFilename = null;
				
				if (outputDirectory == null) {
					throw new RuntimeException("File Task Step: directory is not set on Target Connection");
				}
				
				if (inputFields.contains("fileOperation")) {
					fileOperation = (String)record.getFieldValue("fileOperation");
					
					if (validFileOperationSet.contains(fileOperation) == false) {
						throw new RuntimeException("File Task Step: fileOperation must be one of " + String.join(", ", validFileOperationSet));
					}
				} else {
					throw new RuntimeException("File Task Step: fileOperation is mandatory");
				}
				
				if (inputFields.contains("inputFilename")) {
					inputFilename = (String)record.getFieldValue("inputFilename");
				} else {
					throw new RuntimeException("File Task Step: inputFilename is mandatory");
				}
				
				if (inputFields.contains("outputFilename")) {
					outputFilename = (String)record.getFieldValue("outputFilename");
				}
				
				if (outputFilename == null) {
					outputFilename = inputFilename;
				}
				
				debug("File Task Step: inputFilename=" + inputFilename + ", outputFilename=" + outputFilename + ", fileOperation=" + fileOperation + ", outputDirectory=" + outputDirectory);
				
				try {
					java.io.File sourceFile = new java.io.File(inputFilename);
					java.io.File targetFile = new java.io.File(outputDirectory + "/" + outputFilename);
				
					if (fileOperation.equals("copy")) {
						info("Copying " + sourceFile.toString() + " to " + targetFile.toString());
						
						Files.copy(sourceFile.toPath(),  targetFile.toPath());						
					} else if (fileOperation.equals("move")) {
						info("Moving " + sourceFile.toString() + " to " + targetFile.toString());
						
						Files.move(sourceFile.toPath(), targetFile.toPath());						
					} else if (fileOperation.equals("delete")) {
						info("Deleting " + sourceFile.toString());
						
						Files.delete(sourceFile.toPath());
					}
				} catch (Exception e) {
					fatal(Util.getStackTraceString(e));
					
					totalFailedRecords++;
				}
			}
			
			totalRecords = sourceRecords.size() - totalFailedRecords;
			
			if (totalFailedRecords > 0) {
				setTotalWarnRecords(getTotalWarnRecords() + totalFailedRecords);
			}
			
			info("Copy " + totalRecords + " into target, " + totalFailedRecords + " failed.");
			
			setTotalOutRecords(getTotalOutRecords() + totalRecords);
		}
		
		if (sourceRecords == null || sourceRecords.isEmpty()) {
			info("Source Records is Empty, setting Completed to TRUE");
			
			setCompleted(true);
		}
		
		if (getNextStep() != null) {
			// Copy input records to next step
			
			getNextStep().setInputBatch(sourceRecords);
			
			getNextStep().setCompleted(false);
		}
		
		if (childExecutionId != null) {
			Logging.updateExecution(childExecutionId, Logging.ExecutionStatus.Running, getTotalOutRecords(), getTotalWarnRecords());
		}
		
		info("File Batch Execute END");
	}

	@Override
	public String getTaskStepType() {
		return taskStepType;
	}

	@Override
	public void setTaskStepType(String taskStepType) {
		this.taskStepType = taskStepType;
	}
}
