/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: SFDC Task Flow and Task Execution Logging.
 * History: aosantos, 2016-07-11, Initial Release.
 * 
 * 
 */
package com.vrs.sip;

import java.util.Calendar;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.vrs.sip.task.AbstractTaskStep;
import com.vrs.sip.task.Task;
import com.vrs.sip.task.TaskFlow;

public class Logging {
	static PartnerConnection connection = (PartnerConnection)Factory.getMetadataInstance().metadataConnection.getImplConnection();
	
	public enum ExecutionStatus {
		Pending("Pending"),
		Running("Running"),
		Aborted("Aborted"),
		FinishedWithSuccess("Finished with Success"),
		FinishedWithFailure("Finished with Failure"),
		FinishedWithWarnings("Finished with Warnings");
		
		public String statusValue;
		
		private ExecutionStatus(String statusValue) {
			this.statusValue = statusValue;
		}
	};
	
	/**
	 * Audit Server Log Entry
	 * 
	 * @param level
	 * @param auditText
	 */
	public static void serverAudit(String level, String auditText) {
		try {
			Configuration.Log logConfiguration = Configuration.getInstance().getLog();
			
			if (logConfiguration != null && logConfiguration.isDatabaseLog == true) {
				if (connection != null) {
					SObject audit = new SObject();
					
					audit.setType("SIP_Server_Audit__c");
					
					audit.setField("Effective_Date__c", Calendar.getInstance());
					audit.setField("Level__c", level);
					audit.setField("Audit__c", auditText);
					
					try {
						insert(audit);
					} catch (ConnectionException e) {
						System.err.println("Error creating server audit: " + Util.getStackTraceString(e));
					}
				}
			}
		} catch (Exception e) {
			System.err.println("serverAudit: " + Util.getStackTraceString(e));
		}
	}
	
	/**
	 * Log the Task preparation to Execute.
	 * 
	 * @param task
	 * @return
	 * @throws ConnectionException
	 */
	public static String prepareExecution(Task task) throws ConnectionException {
		SObject execution = new SObject();
		String executionId = null;
		
		execution.setType("SIP_Execution__c");
		execution.setField("Status__c", ExecutionStatus.Pending.statusValue);
		execution.setField("Start_Date__c", Calendar.getInstance());
		execution.setField("Task__c", task.taskId);
		
		executionId = insert(execution);
		
		return executionId;
	}
	
	/**
	 * Log the Task Flow preparation to Execute.
	 * 
	 * @param taskFlow
	 * @return
	 * @throws ConnectionException
	 */
	public static String prepareExecution(TaskFlow taskFlow) throws ConnectionException {
		SObject execution = new SObject();
		String executionId = null;
		
		execution.setType("SIP_Execution__c");
		execution.setField("Status__c", ExecutionStatus.Pending.statusValue);
		execution.setField("Start_Date__c", Calendar.getInstance());
		execution.setField("Task_Flow__c", taskFlow.taskFlowId);
		
		executionId = insert(execution);
		
		return executionId;
	}

	/**
	 * Log the Child Task preparation to Execute.
	 * 
	 * @param parentId
	 * @param task
	 * @param taskStep
	 * @return
	 * @throws ConnectionException
	 */
	public static String prepareExecution(String parentId, Task task, AbstractTaskStep taskStep) throws ConnectionException {
		SObject childExecution = new SObject();
		String childExecutionId = null;
		SObject parentExecution;
		SObject parentChildExecution;
		
		parentExecution = select("SIP_Execution__c", parentId);
		parentChildExecution = select("SIP_Child_Execution__c", parentId);
		
		childExecution.setType("SIP_Child_Execution__c");
		childExecution.setField("Status__c", ExecutionStatus.Pending.statusValue);
		childExecution.setField("Start_Date__c", Calendar.getInstance());
		
		if (parentExecution != null) {
			childExecution.setField("Execution__c", parentId);
		}
		
		if (parentChildExecution != null) {
			childExecution.setField("Child_Execution__c", parentId);
		}
		
		if (task != null) {
			childExecution.setField("Task__c", task.taskId);
			childExecution.setField("Order__c", task.order);
		}
		
		if (taskStep != null) {
			childExecution.setField("Task_Step__c", taskStep.taskStepId);
			childExecution.setField("Order__c", taskStep.order);
		}
		
		childExecutionId = insert(childExecution);
		
		return childExecutionId;
	}

	/**
	 * Log the Update of Execution / Child Execution.
	 * 
	 * @param parentId
	 * @param executionStatus
	 * @param successCount
	 * @throws ConnectionException
	 */
	public static void updateExecution(
		String parentId,
		ExecutionStatus executionStatus,
		Integer successCount,
		Integer warningCount
	) throws ConnectionException {
		SObject execution;
		
		execution = select("SIP_Execution__c", parentId);
		
		if (execution == null) {
			execution = select("SIP_Child_Execution__c", parentId);
		}

		if (execution == null) {
			throw new RuntimeException("Could not find Execution/Child Execution with Id " + parentId);
		}
		
		execution.setField("Status__c", executionStatus.statusValue);
		execution.setField("Success_Count__c", successCount);
		execution.setField("Warning_Count__c", warningCount);
		
		update(execution);
	}
	
	/**
	 * Log the Finish of Execution / Child Execution.
	 * 
	 * @param parentId
	 * @param executionStatus
	 * @param successCount
	 * @param errorMessage
	 * @param executionLog
	 * @throws ConnectionException
	 */
	public static void finishExecution(
			String parentId,
			ExecutionStatus executionStatus,
			Integer successCount,
			Integer warningCount,
			String errorMessage,
			String executionLog
	) throws ConnectionException {
		SObject execution;
		
		execution = select("SIP_Execution__c", parentId);
		
		if (execution == null) {
			execution = select("SIP_Child_Execution__c", parentId);
		}
		
		if (execution == null) {
			throw new RuntimeException("Could not find Execution/Child Execution with Id " + parentId);
		}
		
		execution.setField("Status__c", executionStatus.statusValue);
		execution.setField("Success_Count__c", successCount);
		execution.setField("Warning_Count__c", warningCount);
		execution.setField("Error_Message__c", errorMessage);
		execution.setField("Execution_Log__c", executionLog);
		execution.setField("End_Date__c", Calendar.getInstance());
		
		update(execution);
	}

	/**
	 * Finish the execution/child execution. Note: successCount and warningCount are not specified in this method
	 * because they could have been set in a previous updateExecution() method.
	 * 
	 * @param parentId
	 * @param executionStatus
	 * @param errorMessage
	 * @param executionLog
	 * @throws ConnectionException
	 */
	public static void finishExecution(
			String parentId,
			ExecutionStatus executionStatus,
			String errorMessage,
			String executionLog
	) throws ConnectionException {
		SObject execution;
		
		execution = select("SIP_Execution__c", parentId);
		
		if (execution == null) {
			execution = select("SIP_Child_Execution__c", parentId);
		}
		
		if (execution == null) {
			throw new RuntimeException("Could not find Execution/Child Execution with Id " + parentId);
		}
		
		execution.setField("Status__c", executionStatus.statusValue);
		execution.setField("Error_Message__c", errorMessage);
		execution.setField("Execution_Log__c", executionLog);
		execution.setField("End_Date__c", Calendar.getInstance());
		
		update(execution);
	}

	
	/**
	 * Abort the execution.
	 * 
	 * @param parentId
	 */
	public static void abortExecution(String parentId) throws ConnectionException {
		SObject execution;
		
		execution = select("SIP_Execution__c", parentId);
		
		if (execution == null) {
			execution = select("SIP_Child_Execution__c", parentId);
		}
		
		if (execution == null) {
			throw new RuntimeException("Could not find Execution/Child Execution with Id " + parentId);
		}
		
		execution.setField("Status__c", ExecutionStatus.Aborted.statusValue);
		execution.setField("End_Date__c", Calendar.getInstance());
		
		update(execution);
	}
	
	/**
	 * Insert a SFDC SObject Record
	 * 
	 * @param record
	 * @return
	 * @throws ConnectionException
	 */
	private static String insert(SObject record) throws ConnectionException {
		SaveResult[] saveResultList = connection.create(new SObject[] { record } );
		String resultId = null;
		
		checkErrors(saveResultList);
		
		if (saveResultList != null && saveResultList.length > 0) {
			resultId = saveResultList[0].getId();
		}
		
		return resultId;
	}
	
	/**
	 * Update a SFDC Object Record
	 * 
	 * @param record
	 * @throws ConnectionException
	 */
	private static void update(SObject record) throws ConnectionException {
		SaveResult[] saveResultList = connection.update(new SObject[] { record } );
		
		checkErrors(saveResultList);
	}
	
	/**
	 * Select a single SFDC Object Record
	 * @param sobjectType
	 * @param id
	 * @return
	 * @throws ConnectionException
	 */
	private static SObject select(String sobjectType, String id) throws ConnectionException {
		String query = "SELECT Status__c FROM " + sobjectType + "\nWHERE Id='" + id + "'";
		
		QueryResult queryResult;
		SObject result = null;
		
		queryResult = connection.query(query);
		if (queryResult != null) {
			SObject[] fetchRecords = queryResult.getRecords();
			
			if (fetchRecords != null && fetchRecords.length > 0) {
				result = fetchRecords[0];
				
				result.setId(id);
			}
		}
		
		return result;
	}
	
	/**
	 * Check for Errors in SFDC API.
	 *  
	 * @param saveResultList
	 */
	private static void checkErrors(SaveResult[] saveResultList) {
		if (saveResultList != null) {
			for (SaveResult saveResult : saveResultList) {
				if (saveResult.isSuccess() == false) {
					String errors = "";
					
					com.sforce.soap.partner.Error[] errorList = saveResult.getErrors();
					
					if (errorList != null) {
						for (com.sforce.soap.partner.Error error : errorList) {
							String[] fieldList = error.getFields();
							String fields = "";

							if (fieldList != null) {
								for (String field : fieldList) {
									if (fields.equals("") == false) {
										fields += ", ";
									}
									
									fields += field;
								}
							}

							if (errors.equals("") == false) {
								errors += "\n";
							}
							
							if (fields.equals("") == false) {
								errors += fields + ": ";
							}
							
							errors += error.getMessage();
						}
					}
					
					throw new RuntimeException(errors);
				}
			}
		}
	}
}
