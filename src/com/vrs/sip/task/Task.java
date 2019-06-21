/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Task.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.task;

import com.sforce.ws.ConnectionException;
import com.vrs.sip.*;
import com.vrs.sip.Configuration.Scripts;
import com.vrs.sip.metadata.ScheduleMetadata;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Ths class is responsible to initialize a task and corresponding task steps from configuration.
 *
 * Execution sequence is the following:
 * 		 Initialization of Task Steps
 * 		 while (extractTask has batches) {
 * 		 	for each extractTask batch {
 * 				transformTask
 * 				loadTask
 * 			}
 * 		}
 * 		archiveTask
 * 		Clean up
 * 
 * @author aosantos
 *
 */
public class Task extends Schedulable implements Comparable<Task>, Runnable {
	private static FileLog log = FileLog.getNewInstance(Task.class, "task_" + Util.getSimpleUniqueId(), ".log");
	
	private static Integer INFO_KEEPALIVE_SECONDS = 15;
	
	public String taskId;
	public String taskFlowId;
	public String taskName;
	public Integer order;
	public Boolean abortOnFailure;
	public String preProcessingScript;
	public String postProcessingScript;
	public String successEmails;
	public String warningEmails;
	public String failureEmails;
	
	public Boolean retryIfFail;
	public Integer maxRetryCount;
	
	public Schedule taskSchedule;
	public Date lastScheduleDate;
	
	public TaskStatus taskStatus;
	
	public Integer totalSuccess;
	public Integer totalWarning;
	
	public Server serverInstance;
	
	public List<AbstractTaskStep> taskSteps;
		
	protected Boolean completed;

	protected String taskLogFilename;
	
	public volatile String taskExecutionId;
	public volatile String taskExceptionStackTrace;
	
	public String parentExecutionId;

	public void setParentExecutionId(String parentId) {
		this.parentExecutionId = parentId;
	}
	
	public String getParentExecutionId() {
		return parentExecutionId;
	}
	
	public Task() {
		completed = false;
		taskStatus = TaskStatus.NotStarted;
		retryIfFail = false;
		maxRetryCount = 0;
		
		taskSteps = new Vector<AbstractTaskStep>();
	}
	
	public List<AbstractTaskStep> getTaskSteps() {
		return taskSteps;
	}
	
	public void abort() throws Exception {
		if (taskExecutionId != null) {
			Logging.abortExecution(taskExecutionId);
			
			if (taskSteps != null && taskSteps.isEmpty() == false) {
				for (AbstractTaskStep taskStep : taskSteps) {
					taskStep.abort();
				}
			}
		}
	}
	
	public void setTaskExecutionId(String taskExecutionId) {
		this.taskExecutionId = taskExecutionId;
	}
	
	public String getTaskExecutionId() {
		return taskExecutionId;
	}

    public void storeBeginExecution() throws Exception {
        Metadata metadata = Factory.getMetadataInstance();

        metadata.saveBeginExecution(getSchedule(), ScheduleMetadata.Status.IN_PROGRESS);
    }

	public void storeLastScheduleDate() throws Exception {
		Metadata metadata = Factory.getMetadataInstance();
		
		metadata.saveTask(this);
	}
	
	public void setLastScheduleDate(Date lastScheduleDate) throws Exception {
		this.lastScheduleDate = lastScheduleDate;
	}
	
	public Date getLastScheduleDate() throws Exception {
		return lastScheduleDate;
	}
	
	public void setServerInstance(Server serverInstance) {
		this.serverInstance = serverInstance;
	}
	
	public Schedule getSchedule() throws Exception {
		return taskSchedule;
	}
	
	public Integer getTotalSuccess() {
		return totalSuccess;
	}
	
	public Integer getTotalWarning() {
		return totalWarning;
	}
	
	public void run() {
		try {
            storeBeginExecution();
			executeTask();
		} catch (Exception e) {
			System.err.println(Util.getStackTraceString(e));
		}
		
		if (serverInstance != null) {
			serverInstance.finishSchedulable(taskId);
		}
	}

    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && object instanceof Task)
        {
            sameSame = this.taskId.equals(((Task) object).taskId);
        }

        return sameSame;
    }
	
	@Override
	public int compareTo(Task o) {
		return order.compareTo(o.order);
	}
		
	public Boolean isCompleted() {
		return completed;
	}
	
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}
	
	public FileLog getLog() {
		return log;
	}
	
	public String getSchedulableName() {
		return "Task " + taskId + " [" + taskName + "]";
	}
	
	public String getSchedulableId() throws Exception {
		return taskId;
	}
	
	private void initializeTask() throws Exception {
		List<ITaskStep> steps = new Vector<ITaskStep>();
	
		getLog().reopenLogFile();
		
		taskStatus = TaskStatus.Processing;
		
		for (ITaskStep taskStep : taskSteps) {
			steps.add(taskStep);
		}
		
		getLog().info("Task " + getSchedulableName() + " is composed of the following steps");
		for (ITaskStep step : steps) {
			getLog().info("\t" + step.getTaskStepType() + " [" + step.getOrder() + "]");
		}
		
		for (Integer i = 0; i < steps.size(); i++) {
			ITaskStep prevStep = null;
			ITaskStep nextStep = null;
			ITaskStep currentStep = steps.get(i);
			String parentId;
			
			if (i > 0) {
				prevStep = steps.get(i - 1);
			}
			
			if (i < steps.size() - 1) {
				nextStep = steps.get(i + 1);
			}
			
			currentStep.setPreviousStep(prevStep);
			currentStep.setNextStep(nextStep);
			
			currentStep.setLog(log);

			if (taskFlowId == null) {
				parentId = getTaskExecutionId();
			} else {
				parentId = getParentExecutionId();
			}
			
			if (parentId != null) {
				currentStep.setParentExecutionId(parentId);
			}
		}
	}
	
	@SuppressWarnings("unused")
	public void executeTask() throws Exception {
		Boolean[] stepCompleted;
		
		Integer taskTotalSuccess;
		Integer taskTotalWarn;
		Integer taskTotalOut;

		Boolean retry = true;
		Integer retryCount = 0;
		Calendar lastKeepaliveInfo = Calendar.getInstance();
		long totalSecondsRunning = 0;
		
		Integer inputSuccess;
		Integer outputSuccess;
		Integer outputWarning;
		
		stepCompleted = new Boolean[taskSteps.size()];

		taskExceptionStackTrace = null;
		
		while (retry == true) {
			Integer cnt = 0;
			while (cnt < taskSteps.size()) {
				stepCompleted[cnt] = false;
				cnt++;
			}
						
			taskTotalSuccess = 0;
			taskTotalWarn = 0;
			taskTotalOut =0;
			
			inputSuccess = 0;
			outputSuccess = 0;
			outputWarning = 0;
			
			totalSuccess = 0;
			totalWarning = 0;
			
			taskExecutionId = null;

			
			if (taskFlowId == null) {
				setTaskExecutionId(Logging.prepareExecution(this));
			}
			
			try {
				initializeTask();
				
				if (getTaskExecutionId() != null) {
					Logging.updateExecution(getTaskExecutionId(), Logging.ExecutionStatus.Running, totalSuccess, totalWarning);
				}
				
				// NOTE - Don't log anything until this line code because logging is not initialized before this point
				if (preProcessingScript != null) {
					Integer exitCode = runProcessingScript(preProcessingScript);
					
					if (exitCode != 0) {
						throw new RuntimeException("Task PreProcessing Script ended with exitValue=" + exitCode);
					}
				}

				while (isCompleted() == false) {
					Calendar keepaliveInfo = Calendar.getInstance();
					Calendar keepaliveInfoReport = (Calendar)lastKeepaliveInfo.clone();
					
					keepaliveInfoReport.add(Calendar.SECOND, INFO_KEEPALIVE_SECONDS);
					
					if (keepaliveInfoReport.before(keepaliveInfo)) {
						totalSecondsRunning += (keepaliveInfo.getTimeInMillis() - lastKeepaliveInfo.getTimeInMillis()) / 1000;
						
						lastKeepaliveInfo = (Calendar)keepaliveInfo.clone();
						
						log.info("Task is not completed, executing for " + totalSecondsRunning + " seconds [" + keepaliveInfo.getTime() + "]");
						
						for (Integer i = 0; i < taskSteps.size(); i++) {
							ITaskStep step = taskSteps.get(i);
							Boolean isCompleted = stepCompleted[i];
							
							log.info("\tTask Step [" + i + "] - " + step.getStepName() + " isCompleted=" + isCompleted + ", totalIn=" + step.getTotalInRecords() + ", totalOut=" + step.getTotalOutRecords() + ", totalWarn=" + step.getTotalWarnRecords());
							
							if (isCompleted) {
								step.finish();
							}
						}
						log.info("");
					}
										
					for (Integer i = 0; i < taskSteps.size(); i++) {
						Boolean isCompleted = stepCompleted[i];
						ITaskStep taskStep = taskSteps.get(i);
						
						if (isCompleted == false) {
							if (taskStep.isCompleted() == true) {
								stepCompleted[i] = true;
							}
							
							if (taskStep.isCompleted() == false) {
								log.info("Task Step " + taskStep.getTaskStepType() + " " + taskStep.getStepName() + " run produceOutputBatch()");
								
								taskStep.produceOutputBatch();
								
								log.info("Task Step " + taskStep.getTaskStepType() + " " + taskStep.getStepName() + " isCompleted=" + taskStep.isCompleted());
								
								if (taskStep.isCompleted() == true) {
									stepCompleted[i] = true;
									
									// Signal task step as completed
									taskStep.finish();
								}
							}
						}
					}
					
					Boolean allCompleted = true;
					
					for (Integer i = 0; i < taskSteps.size(); i++) {
						Boolean isCompleted = stepCompleted[i];
						
						if (isCompleted == false) {
							allCompleted = false;
							break;
						}
					}
					
					if (allCompleted) {
						log.info("Setting Task to Completed Status - all TaskSteps are completed");
						
						setCompleted(true);
					}			
				} // while task is not completed
				
				for (Integer i = 0; i < taskSteps.size(); i++) {
					ITaskStep taskStep = taskSteps.get(i);

					Integer stepIn = taskStep.getTotalInRecords();
					Integer stepWarn = taskStep.getTotalWarnRecords();
					Integer stepOut = taskStep.getTotalOutRecords();
					
					log.info("Task Step " + taskStep.getStepName() + " [" + i + "] In=" + stepIn + ", Warn=" + stepWarn + ", Out=" + stepOut);
					
					if (i == 0) {
						inputSuccess = stepIn;
					}
					
					if (i == taskSteps.size() - 1) {
						outputSuccess = stepOut;
						
						outputWarning = inputSuccess - outputSuccess;
					}

					if (!(taskStep.getTaskStepType().equalsIgnoreCase("Filter")))
                    {
                        taskTotalSuccess += stepIn;
                        taskTotalWarn += stepWarn;
                        taskTotalOut += stepOut;
                    }

				}
				
				if (postProcessingScript != null) {
					Integer exitCode = runProcessingScript(postProcessingScript);
					
					if (exitCode != 0) {
						throw new RuntimeException("Task PostProcessing Script ended with exitValue=" + exitCode);
					}
				}

				log.info("taskTotalSuccess = " + taskTotalSuccess + "," + "taskTotalOut = " + taskTotalOut);

				taskStatus = taskTotalWarn > 0 ? TaskStatus.Warning : ( taskTotalSuccess.equals(taskTotalOut) ? TaskStatus.Success : TaskStatus.Fail  ) ;

			} catch (Exception e) {

				log.fatal(getStackTrace(e));
				
				taskStatus = TaskStatus.Fail;

				//Setting the Task with the error Factor//
				this.abortOnFailure = true;

				setCompleted(false);

				taskExceptionStackTrace = Util.getStackTraceString(e);

				log.info("taskExceptionStackTrace = " + taskExceptionStackTrace);
			}
			
			totalSuccess += taskTotalSuccess;
			totalWarning += taskTotalWarn;
			
			log.info("Task totalSuccess=" + totalSuccess + ", totalWarning=" + totalWarning);
			
			if (getTaskExecutionId() != null) {

				log.info("getTaskExecutionId() = " + getTaskExecutionId());

				// outputSuccess is total number of records output
				// inputSuccess is total number of records input
				// outputWarning is total number of records that didnt get from input to output
				// totalSuccess is sum of total success records from all steps
				// totalWarning is sum of total warning records from all steps


				Logging.finishExecution(getTaskExecutionId(), getTaskLoggingExecutionStatus(taskStatus), totalSuccess, totalWarning, taskExceptionStackTrace, log.getLogFilenameFullPath());
			}
			
			if (taskStatus == TaskStatus.Fail) {
				if (retryIfFail == true) {
					retryCount++;
					
					if (retryCount > maxRetryCount) {
						retry = false;
					} else {
						log.info("Executing Task Retry Tentative number " + retryCount + " out of max " + maxRetryCount);
					}
				} else {
					retry = false;
				}
			} else {
				retry = false;
			}
		}
		
		log.info("executeTask END");
	
		setTaskLogFilename(log.getLogFilenameFullPath());
		
		log.closeLogFile();
		
		emailNotification();
	}
	
	private void setTaskLogFilename(String taskLogFilename) {
		this.taskLogFilename = taskLogFilename;
	}
	
	public String getTaskLogFilename() {
		return taskLogFilename;
	}
	
	public String getLogContent() {
		String content = "";
		BufferedReader br = null;
		
		try {
			String line;
			
			br = new BufferedReader(new FileReader(getTaskLogFilename()));
			
			do {
				line = br.readLine();
				
				if (line != null) {
					content += line + "\n";
				}
			} while (line != null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return content;
	}
	
	private void emailNotification() throws IOException, ConnectionException {
		String logFilename = getTaskLogFilename();
		EmailNotification email = EmailNotification.getInstance();
		String subject = "Task " + taskName + " with Id " + taskId + " finished with status of ";
		String content = "Dear SIP User,\n\nAttached with this email is the Log of the Task Session.\n\nRegards,\nThe Salesforce Integration Platform\n\n";
		List<String> attachmentList = new Vector<String>();
		
		log.closeLogFile();
		
		attachmentList.add(logFilename);
		
		if (taskStatus == TaskStatus.Success && successEmails != null && successEmails.isEmpty() == false) {
			subject += "Success";
			
			email.sendMessage(successEmails, null, null, subject, content, attachmentList);
		}
		
		if (taskStatus == TaskStatus.Fail && failureEmails != null && failureEmails.isEmpty() == false) {
			subject += "Failure";
			
			email.sendMessage(failureEmails, null, null, subject, content, attachmentList);
		}
		
		if (taskStatus == TaskStatus.Warning && warningEmails != null && warningEmails.isEmpty() == false) {
			subject += "Warning";
			
			email.sendMessage(warningEmails, null, null, subject, content, attachmentList);
		}
	}
	
	public TaskStatus getTaskStatus() {
		return taskStatus;
	}
	
	public String toString() {
		return String.format(
				"{ taskId=%1s, taskName=%2s, order=%3s, abortOnFailure=%4s }",
				taskId, taskName, order, abortOnFailure
		);
	}
	
	public String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		if (t != null) {
			t.printStackTrace(pw);
		}
		
		return sw.toString();
	}
	
	private Integer runProcessingScript(String script) throws Exception {
		Scripts scriptsConfig = Configuration.getInstance().getScripts();
		ProcessBuilder processBuilder;
		Process process;
		Integer exitValue = 0;
		InputStream inputStream;
		List<String> inputParameterList = new Vector<String>();
		
		getLog().debug("ProcessBuilder input parameters:");
		
		for (String execParameter : scriptsConfig.exec.split("\\s+")) {
			getLog().debug("\t" + execParameter)
			;
			inputParameterList.add(execParameter);
		}
		
		getLog().debug("\t" + script);

        //TODO: Script PostProcessing

        if (getSchedule() == null )
            script = script.replaceAll("@var\\d@","");
        else
            script = script.replace("@var1@",getSchedule().getVar1()).replace("@var2@",getSchedule().getVar2()).replace("@var3@",getSchedule().getVar3()).replace("@var4@",getSchedule().getVar4()).replace("@var5@",getSchedule().getVar5());

        inputParameterList.add(script);
		
		getLog().debug("-------------------------------");
		
		processBuilder = new ProcessBuilder(inputParameterList);
		
		processBuilder.directory(new File (scriptsConfig.home));
		processBuilder.redirectErrorStream(true);
		
		getLog().debug("Executing the following Script:\n" + script + "\n<<<<<<<<< EOF >>>>>>>>>>");
		
		getLog().info("Executing Command Line Script:\n" + script + "\n");
		
		process = processBuilder.start();
		
		while (process.isAlive()) {
			try {
				exitValue = process.waitFor();
			} catch (InterruptedException e) {
				getLog().info("Wait for Script Thread Completion ...");
			}
		}
		
		exitValue = process.exitValue();
		
		inputStream = process.getInputStream();
		
		if (inputStream != null) {
			BufferedReader bufferedReader = null;
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			String capturedOutput;
			
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				
				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			capturedOutput = stringBuilder.toString();
			
			if (capturedOutput != null) {
				getLog().debug("Captured Output/Error from Script:\n" + capturedOutput + "\n<<<<<<<<< EOF >>>>>>>>>>");
				
				getLog().info("Output from Script:\n" + capturedOutput + "\n");
			}
		}
		
		return exitValue;
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
}

