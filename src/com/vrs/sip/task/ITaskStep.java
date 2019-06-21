/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Task Step Interface.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.task;

import java.util.List;

import com.vrs.sip.FileLog;
import com.vrs.sip.connection.IConnection;
import com.vrs.sip.task.AbstractTaskStep.OperationType;

public interface ITaskStep {
	public void abort() throws Exception;
	public void finish() throws Exception;
	
	public void setParentExecutionId(String parentId) throws Exception;
	public String getParentExecutionId() throws Exception;
	
	public ITaskStep getPreviousStep();
	public void setPreviousStep(ITaskStep step);
	
	public char getFieldSeparator();
	public void setFieldSeparator(char fieldSeparator);
	
	public Integer getOrder();
	public void setOrder(Integer order);
	
	public String getStepName();
	public void setStepName(String name);
	
	public ITaskStep getNextStep();
	public void setNextStep(ITaskStep step);
	
	public void setSourceConnection(IConnection connection);
	public IConnection getSourceConnection();
	public void setSourceEntity(Entity entity);
	public Entity getSourceEntity();
	
	public void setTargetConnection(IConnection connection);
	public IConnection getTargetConnection();
	public void setTargetEntity(Entity entity);
	public Entity getTargetEntity();
	
	public Integer getTotalInRecords();
	public Integer getTotalOutRecords();
	public Integer getTotalWarnRecords();
	
	public void setTotalInRecords(Integer total);
	public void setTotalOutRecords(Integer total);
	public void setTotalWarnRecords(Integer total);
	
	public void setTransformationList(List<Transformation> transformationList);
	public List<Transformation> getTransformationList();
	
	public String getInputFilename();
	public void setInputFilename(String filename);
	
	public void setOperation(OperationType operationType);
	public OperationType getOperation();
	
	public void setOperationKeyFieldList(List<String> operationKeyFieldList);
	public List<String> getOperationKeyFieldList();
	
	public String getTaskStepType();
	public void setTaskStepType(String taskStepType);
	
	/**
	 * Called from the current Task Step. Gets the previous task output batch.
	 * 
	 * @return
	 */
	public List<DataRecord> getOutputBatch();
	
	/**
	 * Called from this Task Step when this Task is connected in chain with a
	 * previous Task.
	 * 
	 * Sets the input batch for next task in chain
	 * 
	 * @param inputBatch
	 * @throws Exception
	 */
	public void setInputBatch(List<DataRecord> inputBatch) throws Exception;
	
	/**
	 * Get the Input Data records for the Task Step. Note: this method return the current batch and clears the internal input batch storage.
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<DataRecord> getInputBatch() throws Exception;
	
	/**
	 * The method that the concrete Task Step class must implement.
	 *    
	 * @throws Exception
	 */
	public void produceOutputBatch() throws Exception;
		
	/**
	 * Has this Task Step completed processing?
	 * @return
	 */
	public Boolean isCompleted();
	
	/**
	 * Set this Task Step as completed.
	 * 
	 * @param completed
	 */
	public void setCompleted(Boolean completed);
	
	/**
	 * Set the File log to append.
	 * 
	 * @param fileLog
	 */
	public void setLog(FileLog fileLog);
	
	public FileLog getLog();
	
	/** Comparable Interface **/
	int compareTo(AbstractTaskStep o);
	
	public Boolean getTruncateTarget();
	
	public void setTruncateTarget(Boolean truncateTarget);
	
	public Integer getBatchSize();
	
	public void setBatchSize(Integer batchSize);
}
