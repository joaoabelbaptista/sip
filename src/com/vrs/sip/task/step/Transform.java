/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Transform Task Step Implementation.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.task.step;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import com.vrs.sip.FileLog;
import com.vrs.sip.Logging;
import com.vrs.sip.Util;
import com.vrs.sip.task.AbstractTaskStep;
import com.vrs.sip.task.DataRecord;
import com.vrs.sip.task.Entity;
import com.vrs.sip.task.EntityField;
import com.vrs.sip.task.Transformation;
import com.vrs.sip.task.TransformationEngine;

public class Transform extends AbstractTaskStep {
	FileLog log;
	
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
		info("Transform Batch Execute START");
		
		parentId = getParentExecutionId();
		
		if (childExecutionId == null) {
			if (parentId != null) {
				childExecutionId = Logging.prepareExecution(parentId, null, this);
			}
		}
		
		List<DataRecord> inputRecords = getInputBatch();
		List<DataRecord> outputRecords = new Vector<DataRecord>();
		Entity outEntity = getTargetEntity();
		
		if (inputRecords != null && inputRecords.isEmpty() == false) {
			JexlEngine jexl = TransformationEngine.getJexl();
			
			setTotalInRecords(getTotalInRecords() + inputRecords.size());
			
			for (DataRecord inRecord : inputRecords) {
				try {
					DataRecord outRecord = new DataRecord();
					List<Transformation> transformationList = getTransformationList();
					JexlContext context = new MapContext();
					
					// Set context with Input Record Variables
					for (String fieldName : inRecord.getFields()) {
						Object fieldValue = inRecord.getFieldValue(fieldName);
						
						context.set(fieldName.toUpperCase(), fieldValue);
					}
					
					for (Transformation transformation : transformationList) {
						JexlExpression expression = jexl.createExpression(transformation.transformation);
						EntityField outField = outEntity.getField(transformation.targetFieldName);
						Object value = null;
						
						switch (outField.entityFieldType) {
							case Boolean:
								value = (Boolean)expression.evaluate(context);
								break;
								
							case Date:
								value = (Date)expression.evaluate(context);
								break;
								
							case Decimal:
								value = (Double)expression.evaluate(context);
								break;
								
							case Integer:
								value = (Integer)expression.evaluate(context);
								break;
								
							case String:
								value = (String)expression.evaluate(context);
								break;
						}
						
						outRecord.setFieldValue(outField.entityFieldName, value);
						
						debug("expression " + transformation.transformation + " set " + outField.entityFieldName + " to " + value);
					}
					
					outputRecords.add(outRecord);
				} catch (Exception e) {
					setTotalWarnRecords(getTotalWarnRecords() + 1);
					
					info("Input Record: " + inRecord + " failed with " + e);
					
					fatal(Util.getStackTraceString(e));
				}
			}
			
			info("Transform processed " + inputRecords.size() + " records");
			
			// aosantos, 2016-10-20, incorrectly setting completed as true on first batch finish
			//setCompleted(true);
		}
		
		if (inputRecords == null || inputRecords != null && inputRecords.size() == 0) {
			setCompleted(true);
		}
		
		if (getNextStep() != null) {
			setTotalOutRecords(getTotalOutRecords() + outputRecords.size());
			
			getNextStep().setInputBatch(outputRecords);
			getNextStep().setCompleted(false);
		}
		
		if (childExecutionId != null) {
			Logging.updateExecution(childExecutionId, Logging.ExecutionStatus.Running, getTotalOutRecords(), getTotalWarnRecords());
		}
		
		info("Transform Batch Execute END");
	}
}
