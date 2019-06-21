/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Task Flow.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.task;

import com.sforce.ws.ConnectionException;
import com.vrs.sip.*;
import com.vrs.sip.metadata.ScheduleMetadata;

import java.io.IOException;
import java.util.*;

public class TaskFlow extends Schedulable implements Runnable {
	private static FileLog log = FileLog.getNewInstance(TaskFlow.class, "taskflow_" + Util.getSimpleUniqueId(), ".log");
	
	public String 		taskFlowId;
	public String 		taskFlowName;
	public List<Task>	taskList;
	public String		successEmails;
	public String		warningEmails;
	public String		failureEmails;
	
	public Schedule		taskFlowSchedule;
	public Date			lastScheduleDate;
	
	public Boolean		retryIfFail;
	public Integer		maxRetryCount;
	
	public Server serverInstance;
	
	protected Boolean completed;
	
	public TaskFlowStatus	taskFlowStatus;
	
	String taskFlowExecutionId;
	String taskFlowExceptionStackTrace;
	
	// Schedule
	
	public TaskFlow() {
		taskList = new Vector<Task>();
		completed = false;
		taskFlowStatus = TaskFlowStatus.NotStarted;
		retryIfFail = false;
		maxRetryCount = 0;
	}
	
	public void abort() throws Exception {
		if (taskFlowExecutionId != null) {
			Logging.abortExecution(taskFlowExecutionId);
		}
	}
	
	public void setServerInstance(Server serverInstance) {
		this.serverInstance = serverInstance;
	}

    public void storeBeginExecution() throws Exception {
        Metadata metadata = Factory.getMetadataInstance();

        metadata.saveBeginExecution(getSchedule(), ScheduleMetadata.Status.IN_PROGRESS);
    }

	public void storeLastScheduleDate() throws Exception {

		Metadata metadata = Factory.getMetadataInstance();
		
		metadata.saveTaskFlow(this);
	}
	
	public void setLastScheduleDate(Date lastScheduleDate) throws Exception {
		this.lastScheduleDate = lastScheduleDate;
	}
	
	public Date getLastScheduleDate() throws Exception {
		return lastScheduleDate;
	}
	
	public Schedule getSchedule() throws Exception {
		return taskFlowSchedule;
	}
	
	public String getSchedulableName() throws Exception {
		return "TaskFlow " + taskFlowId + " [" + taskFlowName + "]";
	}
	
	public String getSchedulableId() throws Exception {
		return taskFlowId;
	}
	
	public void run() {
		try {
			storeBeginExecution();
			executeTaskFlow();
		} catch (Exception e) {
			System.err.println(Util.getStackTraceString(e));
		}
		
		if (serverInstance != null) {
			serverInstance.finishSchedulable(taskFlowId);
		}
	}
	
	public Boolean isCompleted() {
		return completed;
	}
	
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
	
	public void executeTaskFlow() throws Exception {
		TaskStatus lastTaskStatus;
		Boolean isSomeTaskStatusWarning;
		Boolean retry = true;
		Integer retryCount = 0;
		Logging.ExecutionStatus flowExecutionStatus;
		Integer totalFlowSuccessCount;
		Integer totalFlowWarningCount;
		
		log.info("executeTaskFlow START");
		
		taskFlowStatus = TaskFlowStatus.Processing;
		
		taskFlowExecutionId = Logging.prepareExecution(this);
		
		while (retry == true) {
			isSomeTaskStatusWarning = false;
		
			taskFlowExceptionStackTrace = null;
			totalFlowSuccessCount = 0;
			totalFlowWarningCount = 0;
			
			try {
				
				Logging.updateExecution(taskFlowExecutionId, Logging.ExecutionStatus.Running, totalFlowSuccessCount, totalFlowWarningCount);
				
				for (Task task : taskList) {
					String taskLogContent;
					String taskExecutionId = null;
					String taskExceptionStackTrace;

					log.info("Task Name = " + task.taskName);
                    log.info("Task Order = " + task.order);

					taskExceptionStackTrace = null;
					
					try {
						taskExecutionId = Logging.prepareExecution(taskFlowExecutionId, task, null);
						
						task.setParentExecutionId(taskExecutionId);
						
						lastTaskStatus = task.getTaskStatus();
						
						Logging.updateExecution(taskExecutionId, Logging.ExecutionStatus.Running, task.getTotalSuccess(), task.getTotalWarning());

						if (task.taskSchedule == null) // TODO: VALIDATE THIS CHANGES //
						    task.taskSchedule = getSchedule();


						task.executeTask();

						log.info("task.getTaskStatus()" + task.getTaskStatus());
						
						lastTaskStatus = task.getTaskStatus();
                        taskExceptionStackTrace = task.taskExceptionStackTrace;

						totalFlowSuccessCount += task.getTotalSuccess();
						totalFlowWarningCount += task.getTotalWarning();
						
					} catch (Exception e) {

						lastTaskStatus = TaskStatus.Fail;
						
						taskExceptionStackTrace = Util.getStackTraceString(e);
					}


					if (taskExecutionId != null) {
						Logging.finishExecution(taskExecutionId, getTaskLoggingExecutionStatus(lastTaskStatus), task.getTotalSuccess(), task.getTotalWarning(), taskExceptionStackTrace, null);
					}
					

					taskLogContent = task.getLogContent();
						
					if (taskLogContent != null) {
						log.info("=================================> Task " + task.taskName + " Id " + task.taskId + " <=================================");
						log.append(taskLogContent);
						log.info("======================================================================================================================");
						log.info("");
						log.info("");
					}

					
					if (task.taskStatus == TaskStatus.Warning) {
						isSomeTaskStatusWarning = true;
					}

					log.info(" task.abortOnFailure = " + task.abortOnFailure + " lastTaskStatus=" + lastTaskStatus  );


					
					if (task.abortOnFailure == true && lastTaskStatus == TaskStatus.Fail) {
						log.info("Task has abortOnFailure=" + task.abortOnFailure + ", aborting Task Flow Execution");
						taskFlowStatus = TaskFlowStatus.Fail;
						break;
					}
				}
				
				if (taskFlowStatus == TaskFlowStatus.Processing) {
					taskFlowStatus = TaskFlowStatus.Success;
				}
				
				if (isSomeTaskStatusWarning && taskFlowStatus == TaskFlowStatus.Success) {
					taskFlowStatus = TaskFlowStatus.Warning;
				}
			} catch (Exception e) {
				log.fatal(e);
			
				taskFlowExceptionStackTrace = Util.getStackTraceString(e);
				
				taskFlowStatus = TaskFlowStatus.Fail;
			}
			
			flowExecutionStatus = getTaskFlowLoggingExecutionStatus(taskFlowStatus);
			
			Logging.finishExecution(taskFlowExecutionId, flowExecutionStatus, totalFlowSuccessCount, totalFlowWarningCount, taskFlowExceptionStackTrace, null);
			
			if (taskFlowStatus == TaskFlowStatus.Fail) {
				log.debug("retryIfFail=" + retryIfFail + ", maxRetryCount=" + maxRetryCount + ", retryCount=" + retryCount);
				
				if (retryIfFail == true) {
					retryCount++;
					
					if (retryCount > maxRetryCount) {
						retry = false;
					} else {
						log.info("Executing Task Flow Retry Tentative number " + retryCount + " out of max " + maxRetryCount);
					}
				} else {
					retry = false;
				}
			} else {
				retry = false;
			}
		}
		
		log.info("executeTaskFlow END");
		
		setCompleted(true);
		
		emailNotification();
	}
	
	private String getTaskFlowExecutionOverview() {
		String result = "";
		
		result = "\t" + taskFlowName + " (" + taskFlowStatus  + ")\n";
		
		for (Task task : taskList) {
			result += "\t\t" + task.taskName + " (" + task.taskStatus + ")\n";
		}
		
		return result;
	}
	
	/**
	 * get task flow logging execution status.
	 * 
	 * @param taskFlowStatus
	 * @return
	 */
	private Logging.ExecutionStatus getTaskFlowLoggingExecutionStatus(TaskFlowStatus taskFlowStatus) {
		Map<TaskFlowStatus,Logging.ExecutionStatus> statusMap = new HashMap<TaskFlowStatus,Logging.ExecutionStatus>();
		
		Logging.ExecutionStatus resultStatus = Logging.ExecutionStatus.Aborted;
		
		statusMap.put(TaskFlowStatus.Fail, Logging.ExecutionStatus.FinishedWithFailure);
		statusMap.put(TaskFlowStatus.NotStarted, Logging.ExecutionStatus.Pending);
		statusMap.put(TaskFlowStatus.Processing, Logging.ExecutionStatus.Running);
		statusMap.put(TaskFlowStatus.Success, Logging.ExecutionStatus.FinishedWithSuccess);
		statusMap.put(TaskFlowStatus.Warning, Logging.ExecutionStatus.FinishedWithWarnings);
		
		resultStatus = statusMap.get(taskFlowStatus);
		
		return resultStatus;
	}

	/**
	 * get task flow logging execution status.
	 * 
	 * @param taskStatus
	 * @return
	 */
	private Logging.ExecutionStatus getTaskLoggingExecutionStatus(TaskStatus taskStatus) {
		Map<TaskStatus,Logging.ExecutionStatus> statusMap = new HashMap<TaskStatus,Logging.ExecutionStatus>();
		
		Logging.ExecutionStatus resultStatus = Logging.ExecutionStatus.Aborted;
		
		statusMap.put(TaskStatus.Fail, Logging.ExecutionStatus.FinishedWithFailure);
		statusMap.put(TaskStatus.NotStarted, Logging.ExecutionStatus.Pending);
		statusMap.put(TaskStatus.Processing, Logging.ExecutionStatus.Running);
		statusMap.put(TaskStatus.Success, Logging.ExecutionStatus.FinishedWithSuccess);
		statusMap.put(TaskStatus.Warning, Logging.ExecutionStatus.FinishedWithWarnings);
		
		resultStatus = statusMap.get(taskStatus);
		
		return resultStatus;
	}
	
	private void emailNotification() throws IOException, ConnectionException {
		String logFilename = log.getLogFilenameFullPath();
		EmailNotification email = EmailNotification.getInstance();
		String subject = "Task Flow " + taskFlowName + " with Id " + taskFlowId + " finished with status of ";
		String content;
		List<String> attachmentList = new Vector<String>();
		String taskFlowExecutionOverview = getTaskFlowExecutionOverview();
		
		log.closeLogFile();
		
		content = "Dear SIP User,\n\nAttached with this email is the Log of the Task Flow Session.\n\n";
		content += "Overview of the Task FLow Execution:\n";
		content += taskFlowExecutionOverview + "\n\n\n";
		content += "Regards,\nThe Salesforce Integration Platform\n\n";
		
		attachmentList.add(logFilename);
		
		if (taskFlowStatus == TaskFlowStatus.Success && successEmails != null && successEmails.isEmpty() == false) {
			subject += "Success";
			
			email.sendMessage(successEmails, null, null, subject, content, attachmentList);
		}
		
		if (taskFlowStatus == TaskFlowStatus.Fail && failureEmails != null && failureEmails.isEmpty() == false) {
			subject += "Failure";
			
			email.sendMessage(failureEmails, null, null, subject, content, attachmentList);
		}
		
		if (taskFlowStatus == TaskFlowStatus.Warning && warningEmails != null && warningEmails.isEmpty() == false) {
			subject += "Warning";
			
			email.sendMessage(warningEmails, null, null, subject, content, attachmentList);
		}
	}
}
