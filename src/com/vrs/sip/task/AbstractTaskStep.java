/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Abstract Task Step.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.task;

import com.vrs.sip.connection.IConnection;

import java.util.List;
import java.util.Vector;

public abstract class AbstractTaskStep implements ITaskStep, Comparable<AbstractTaskStep> {
	
	// Operation Types - only valid for Task Step Type of Load
	public enum OperationType {
		Insert, Update, Upsert, Delete, FileAttach, ExecuteCall, InsertWithFileAttachment, SendEmailWithAttachment, MoveEmailsToFolder
	};
	
	public enum TaskStepType {
		Extract, Transform, Load, File, Filter
	};

	public String taskId;
	public String taskStepId;
	public String taskStepName;
	public TaskStepType taskStepType;
	public IConnection sourceConnection;
	public Entity sourceEntity;
	public IConnection targetConnection;
	public Entity targetEntity;
	public Integer order;
	public char fieldSeparator = 0;

    Schedule schedule;

	protected ITaskStep previousStep;
	protected ITaskStep nextStep;
	
	protected Boolean completed;
	
	protected Integer totalInRecords;
	protected Integer totalOutRecords;
	protected Integer totalWarnRecords;
	
	public OperationType operation;
	public List<String> operationKeyFieldList;
	public Boolean truncateTarget;
	public Integer batchSize;
	
	protected List<Transformation> transformationList;
	
	protected String inputFilename;
	
	public String parentExecutionId;

	public void setParentExecutionId(String parentId) {
		this.parentExecutionId = parentId;
	}
	
	public String getParentExecutionId() {
		return parentExecutionId;
	}

	public void setTruncateTarget(Boolean truncateTarget) {
		this.truncateTarget = truncateTarget;
	}
	
	public Boolean getTruncateTarget() {
		return truncateTarget;
	}
	
	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}
	
	public Integer getBatchSize() {
		return batchSize;
	}
	
	public void setStepName(String name) {
		this.taskStepName = name;
	}
	
	public String getStepName() {
		return taskStepName;
	}

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
	
	public AbstractTaskStep() {
		transformationList = new Vector<Transformation>();
		operationKeyFieldList = new Vector<String>();
		
		completed = false;
		
		totalInRecords = 0;
		totalOutRecords = 0;
		totalWarnRecords = 0;
	}
	
	public void info(Object obj) {
		String prefix = "[" + taskStepType.name().toUpperCase() + "] ";
		
		getLog().info(prefix + obj);
	}
	
	public void debug(Object obj) {
		String prefix = "[" + taskStepType.name().toUpperCase() + "] ";
		
		getLog().debug(prefix + obj);
	}

	public void fatal(Object obj) {
		String prefix = "[" + taskStepType.name().toUpperCase() + "] ";
		
		getLog().fatal(prefix + obj);
	}
	
	@Override
	public int compareTo(AbstractTaskStep o) {
		if (order == null) {
			return 1;
		}
		
		return order.compareTo(o.order);
	}
	
	public void setOrder(Integer order) {
		this.order = order;
	}
	
	public Integer getOrder() {
		return order;
	}
	
	public String toString() {
		return String.format(
				"taskStepId=%1s, taskStepName=%2s, taskStepType=%3s, sourceConnection=%4s, sourceEntity=%5s, targetConnection=%6s, targetEntity=%7s, transformationList=%8s",
				taskStepId, taskStepName, taskStepType, sourceConnection, sourceEntity, targetConnection, targetEntity, transformationList
		);
	}
	
	public String getInputFilename() {
		return inputFilename;
	}
	
	public void setInputFilename(String inputFilename) {
		this.inputFilename = inputFilename;
	}

	public Integer getTotalInRecords() {
		return totalInRecords;
	}
	
	public Integer getTotalOutRecords() {
		return totalOutRecords;
	}
	
	public Integer getTotalWarnRecords() {
		return totalWarnRecords;
	}
	
	public void setTotalInRecords(Integer total) {
		totalInRecords = total;
	}
	
	public void setTotalOutRecords(Integer total) {
		totalOutRecords = total;
	}
	
	public void setTotalWarnRecords(Integer total) {
		totalWarnRecords = total;
	}
	
	public ITaskStep getPreviousStep() {
		return previousStep;
	}
	
	public void setPreviousStep(ITaskStep step) {
		previousStep = step;
	}
	
	public ITaskStep getNextStep() {
		return nextStep;
	}
	
	public void setNextStep(ITaskStep step) {
		nextStep = step;
	}
	public Boolean isCompleted() {
		return completed;
	}
	public void setCompleted(Boolean isCompleted) {
		completed = isCompleted;
	}
	public IConnection getSourceConnection() {
		return sourceConnection;
	}
	
	public Entity getSourceEntity() {
		return sourceEntity;
	}
	
	public IConnection getTargetConnection() {
		return targetConnection;
	}
	
	public Entity getTargetEntity() {
		return targetEntity;
	}
	
	public void setSourceConnection(IConnection connection) {
		sourceConnection = connection;
	}
	
	public void setSourceEntity(Entity entity) {
		sourceEntity = entity;
	}
	
	public void setTargetConnection(IConnection connection) {
		targetConnection = connection;
	}
	
	public void setTargetEntity(Entity entity) {
		targetEntity = entity;
	}
	
	public void setOperation(OperationType operationType) {
		this.operation = operationType;
	}
	
	public OperationType getOperation() {
		return operation;
	}
	
	public void setOperationKeyFieldList(List<String> operationKeyFieldList) {
		this.operationKeyFieldList = operationKeyFieldList;
	}
	
	public List<String> getOperationKeyFieldList() {
		return this.operationKeyFieldList;
	}
	
	
	public void setTransformationList(List<Transformation> transformationList) {
		this.transformationList.clear();
		this.transformationList.addAll(transformationList);
	}
	
	public List<Transformation> getTransformationList() {
		return transformationList;
	}
	
	public void setFieldSeparator(char fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
	}
	
	public char getFieldSeparator() {
		return fieldSeparator;
	}
	
	/* ----------------------------------------------------------------------- */
	
	protected List<DataRecord> inputBatch;
	protected List<DataRecord> outputBatch;
		
	/**
	 * Called from the current Task Step and the next Task Step.
	 * This is a synchronized method so that the Task Steps can run in parallel.
	 * 
	 * @return
	 */
	public synchronized List<DataRecord> getOutputBatch() {
		if (outputBatch == null) {
			outputBatch = new Vector<DataRecord>();
		}
		
		return outputBatch;
	}
	
	/**
	 * Called from this Task Step when this Task is connected in chain with a
	 * previous Task.
	 * 
	 * @param inBatchRecords
	 * @throws Exception
	 */
	public void setInputBatch(List<DataRecord> inBatchRecords) throws Exception {
		if (inputBatch == null) {
			inputBatch = new Vector<DataRecord>();
		}
		
		if (inputBatch.isEmpty() == false) {
			inputBatch.clear();
		}
		
		if (inBatchRecords != null) {
			inputBatch.addAll(inBatchRecords);
		}
	}
	
	/**
	 * Get the Input Data Records for the Task Step.
	 * 
	 */
	public List<DataRecord> getInputBatch() throws Exception {
		List<DataRecord> resultList;
		
		if (inputBatch == null) {
			inputBatch = new Vector<DataRecord>();
		}
		
		resultList = inputBatch;
		
		inputBatch = new Vector<DataRecord>();
		
		return resultList;
	}
	
	/**
	 * The method that the concrete Task Step class must implement.
	 * 
	 * The implementation should:
	 * 	- If this task step is the initial task step then it is assumed that outputBatch will be populated
	 *    with data retrieved from the connector.
	 *    
	 *  - If this task step is the final task step then it is assumed that inputBatch will be stored using
	 *    the connector.
	 *    
	 *  - If this task step is a connected task step (with previous and next task steps connected) then
	 *    the implementation should read records using the previous task getOutputBatch() and store them
	 *    using the setInputBatch(), manipulate them and store them in the outputBatch.
	 *    
	 *    
	 * @throws Exception
	 */
	public abstract void produceOutputBatch() throws Exception;
}
