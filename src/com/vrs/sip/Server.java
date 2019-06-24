/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Main Server Process for SIP.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip;

import com.vrs.sip.concurrency.JustOneLock;
import com.vrs.sip.metadata.TaskFlowMetadata;
import com.vrs.sip.metadata.TaskMetadata;
import com.vrs.sip.task.Schedulable;
import com.vrs.sip.task.Task;
import com.vrs.sip.task.TaskFlow;

import java.util.*;

/**
 * Imports
 **/
/*
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
*/

/** --------------------------------------------------------------------- **/

public class Server {
	private static FileLog log = FileLog.getNewInstance(Server.class, "server_" + Util.getSimpleUniqueId(), ".log");
	
	private static final String SIP_VERSION_STRING = "Salesforce Integration Platform v2.00 (2018-03-21)";


	final Thread serverThread = Thread.currentThread();
	
	static volatile Boolean signalShutdown = false; 
	
	private static String[] usage = {
			"Command Line Parameter Arguments:",
			"\t-listTasks",
			"\t-listTaskFlows",
			"\t-task <Task Name>",
			"\t-taskId <Task Id>",
			"\t-taskFlow <Task Flow Name>",
			"\t-taskFlowId <Task Flow Id>",
			"\t-saveMetadata",
			"\t-loadMetadata <source OrgId> [-force]",
			"\t-saveModel",
			"\t-loadModel <source OrgId> [-force]",
			"\t-server  (running at default port 12345)",
            "\t-server  <port> (for running at port = <port>)",
            "\t-server single <port> (for running only one instance at port = <port>)",
			"\t-version"
	};
	
	volatile Set<String> runningSchedulableSet = new HashSet<String>();
	volatile Map<String,Schedulable> runningThreadMap = new HashMap<String,Schedulable>();
	
	/**
	 * Constructor.
	 * 
	 */
	public Server() {
		Runtime.getRuntime().addShutdownHook(
			new Thread() {
				public void run() {
					signalShutdown = true;
					
					try {
						// Kill running Threads
						for (Schedulable runningThread : runningThreadMap.values()) {
							try {
								System.err.println("Aborting Schedulable " + runningThread.getSchedulableId());
								
								runningThread.abort();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						
						serverThread.interrupt();
						
						serverThread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		);
	}
	
	/**
	 * Show Application Version.
	 * 
	 */
	public static void showVersion() {
		System.out.println(SIP_VERSION_STRING);
	}
	
	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		int portServer =  12345;
        Boolean isrunningOneInstance = false;
		Boolean isScheduleLoop = false;
		Boolean isTaskRun = false;
		Boolean isTaskFlowRun = false;
		Boolean isShowUsage = false;
		Boolean isSaveMetadata = false;
		Boolean isLoadMetadata = false;
		Boolean isForce = false;
		Boolean isSaveModel = false;
		Boolean isLoadModel = false;
		Boolean isListTasks = false;
		Boolean isListTaskFlows = false;
		Boolean isShowVersion = false;
		
		String taskName = null;
		String taskId = null;
		String taskFlowName = null;
		String taskFlowId = null;
		String orgId = null;
		
		isShowUsage = true;
		if (args != null && args.length > 0) {
			isShowUsage = false;
			
			if (args.length == 3) {
				if (args[0].equalsIgnoreCase("-loadMetadata")) {
					orgId = args[1];
					isLoadMetadata = true;
					
					if (args[2].equalsIgnoreCase("-force")) {
						isForce = true;
					}
				} else if (args[0].equalsIgnoreCase("-loadModel")) {
					orgId = args[1];
					isLoadModel = true;
					
					if (args[2].equalsIgnoreCase("-force")) {
						isForce = true;
					}
				}
				else if (args[0].equalsIgnoreCase("-server")) {
					isrunningOneInstance = args[1].equalsIgnoreCase("single");
					portServer = Integer.parseInt(args[2]);
					isScheduleLoop = true;
				}
				else {
					isShowUsage = true;
				}
			} else if (args.length == 2) {
				if (args[0].equalsIgnoreCase("-task")) {
					taskName = args[1];
					isTaskRun = true;
				} else if (args[0].equalsIgnoreCase("-taskId")) {
					taskId = args[1];
					isTaskRun = true;
				} else if (args[0].equalsIgnoreCase("-taskflow")) {
					taskFlowName = args[1];
					isTaskFlowRun = true;
				} else if (args[0].equalsIgnoreCase("-taskflowId")) {
					taskFlowId = args[1];
					isTaskFlowRun = true;
				} else if (args[0].equalsIgnoreCase("-loadMetadata")) {
					orgId = args[1];
					isLoadMetadata = true;
				}
                else if (args[0].equalsIgnoreCase("-server")) {
                    portServer = Integer.parseInt(args[1]);
                    isrunningOneInstance = false;
                    isScheduleLoop = true;
                }
				else {
					isShowUsage = true;
				}
			} else if (args.length == 1) {
				if (args[0].equalsIgnoreCase("-saveMetadata")) {
					isSaveMetadata = true;
				} else if (args[0].equalsIgnoreCase("-saveModel")) {
					isSaveModel = true;
				} else if (args[0].equalsIgnoreCase("-server")) {
                    isrunningOneInstance = false;
					isScheduleLoop = true;
				} else if (args[0].equalsIgnoreCase("-listTasks")) {
					isListTasks = true;
				} else if (args[0].equalsIgnoreCase("-listTaskFlows")) {
					isListTaskFlows = true;
				} else if (args[0].equalsIgnoreCase("-version")) {
					isShowVersion = true;
				} else {
					isShowUsage = true;
				}
			}
		}
		
		Server sipServer = new Server();
		
		try {
			if (isShowUsage) {
				for (String usageLine : usage) {
					System.out.println(usageLine);
				}
				
				System.exit(1);
			}
			
			if (isTaskRun) {
				log.info("Running SIP Task with Name " + taskName);
				
				try {
					sipServer.taskRun(taskId, taskName);
				} catch (Exception e) {
					log.fatal(Util.getStackTraceString(e));
				}
				
				log.info("Finished Running SIP Task");
			}
			
			if (isTaskFlowRun) {
				log.info("Running SIP Task Flow with Name " + taskFlowName);
				
				try {
					sipServer.taskFlowRun(taskFlowId, taskFlowName);
				} catch (Exception e) {
					log.fatal(Util.getStackTraceString(e));
				}
				
				log.info("Finished Running SIP Task Flow");
			}
			
			if (isScheduleLoop) {
				try {
					sipServer.scheduleLoop(portServer, isrunningOneInstance);
				} catch (Exception e) {
					log.fatal(Util.getStackTraceString(e));
				}
			}
			
			if (isSaveMetadata) {
				String metadataOrgId = Factory.getMetadataInstance().getOrgId();
				
				log.info("Saving Metadata of Org " + metadataOrgId);
				
				try {
					sipServer.saveMetadata();
				} catch (Exception e) {
					log.fatal(Util.getStackTraceString(e));
				}
				
				log.info("Save of Metadata finished");
			}
			
			if (isLoadMetadata) {
				String metadataOrgId = Factory.getMetadataInstance().getOrgId();
				
				log.info("Loading Metadata from Org " + metadataOrgId);
				
				try {
					sipServer.loadMetadata(orgId, isForce);
				} catch (Exception e) {
					log.fatal(Util.getStackTraceString(e));
				}
				
				log.info("Load of Metadata finished");
			}
			
			if (isSaveModel) {			
				System.out.println("Saving Model");
				
				try {
					sipServer.saveModel();
				} catch (Exception e) {
					System.err.println(Util.getStackTraceString(e));
				}
				
				System.out.println("Saving Model - Finished");
			}
			
			if (isLoadModel) {
				System.out.println("Loading Model");
				
				try {
					sipServer.loadModel(orgId, isForce);
				} catch (Exception e) {
					System.err.println(Util.getStackTraceString(e));
				}
				
				System.out.println("Loading Model - Finished");
			}
			
			if (isListTasks) {
				System.out.println("List Tasks");
				
				try {
					sipServer.listTasks();
				} catch (Exception e) {
					System.err.println(Util.getStackTraceString(e));
				}
				
				System.out.println("List Tasks - Finished");
			}
			
			if (isListTaskFlows) {
				System.out.println("List Task Flows");

				try {
					sipServer.listTaskFlows();
				} catch (Exception e) {
					System.err.println(Util.getStackTraceString(e));
				}
				
				System.out.println("List Task Flows - Finished");				
			}
			
			if (isShowVersion) {
				showVersion();
			}
		} catch (Exception e) {
			log.fatal(Util.getStackTraceString(e));
		}
	}
	
	public void taskRun(String taskId, String taskName) throws Exception {
		String tId = Factory.findTaskId(taskId, taskName);
		
		if (tId != null) {

			Task task = Factory.getTask(tId, null);
			
			// Because we are running the task standalone, say it's not attached to task flow, in order for the Logging to be created with success
			task.taskFlowId = null;
			
			task.run();
		} else {
			throw new RuntimeException("Task not found");
		}
	}
	
	public void taskFlowRun(String taskFlowId, String taskFlowName) throws Exception {
		String fId = Factory.findTaskFlowId(taskFlowId, taskFlowName);
		
		if (fId != null) {
			TaskFlow flow = Factory.getTaskFlow(fId, null);
			
			flow.run();
		} else {
			throw new RuntimeException("Task Flow not found");
		}
	}
	
	public void finishSchedulable(String schedulableId) {
		if (runningSchedulableSet.contains(schedulableId)) {
			runningSchedulableSet.remove(schedulableId);
		}
		
		if (runningThreadMap.containsKey(schedulableId)) {
			runningThreadMap.remove(schedulableId);
		}
	}
	
	public void scheduleLoop(int port, Boolean isRunningOneInstace) throws Exception {

	    System.out.println("isRunningOneInstace = " + isRunningOneInstace);

        JustOneLock lock = new JustOneLock();
		int sleepMiliseconds = 1000 * Configuration.getInstance().getServer().poolingSeconds;

        if (lock.isAppActive(port)) {
            log.info("Sip already active, Stopping now...!!!");
            System.exit(1);
        }

        log.info("Starting SIP Server");

		do {
			List<Schedulable> schedulableQueue = Factory.getSchedulableQueue();


			if (schedulableQueue != null && schedulableQueue.isEmpty() == false) {

				for (Schedulable schedulable : schedulableQueue) {

					Thread schedulableThread;
					
					// Execute
					Date startDate = new Date();
					
					schedulable.setServerInstance(this);
					
					if (runningSchedulableSet.contains(schedulable.getSchedulableId()) == false) {
						try {
							schedulableThread = new Thread(schedulable);
							
							runningSchedulableSet.add(schedulable.getSchedulableId());
							runningThreadMap.put(schedulable.getSchedulableId(), schedulable);
							
							log.info("Launched Thread " + schedulableThread.getId() + " for " + schedulable.getSchedulableName());
							
							schedulableThread.start();

							//Wait to finish the Thread//
                            schedulableThread.join();

						} catch (Exception e) {
							log.fatal(Util.getStackTraceString(e));
						}



						schedulable.setLastScheduleDate(startDate);

						schedulable.storeLastScheduleDate();



					} else {
						log.info("Schedulable " + schedulable.getSchedulableId() + " already running, not running twice");
					}




				}
			}
			
			log.info("Sleeping for " + (sleepMiliseconds / 1000) + " seconds");
			
			try {
				Thread.sleep(sleepMiliseconds);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (signalShutdown == true) {
				break;
			}
			
			log.info("Wakeup");
            log.info("2. isRunningOneInstace = " + isRunningOneInstace);
			Factory.getMetadataInstance().reload();
		} while (!isRunningOneInstace && signalShutdown == false);
		
		log.info("Stoping SIP Server");
	}
	
	private void saveMetadata() throws Exception {
		MetadataExport metadataExport = new MetadataExport(Configuration.getInstance().getServer().metadataBaseDirectory);
		
		metadataExport.exportCSVFiles();
		
	}
	
	private void saveModel() throws Exception {
		Setup setup = new Setup();
		Metadata metadata = Factory.getMetadataInstance(true);
		String orgId = metadata.getOrgId();
		
		System.out.println("Saving Model");
		
		setup.getApplicationSetup(orgId);
	}
	
	private void loadMetadata(String sourceOrgId, Boolean isForce) throws Exception {
		Metadata metadata = Factory.getMetadataInstance();
		String targetOrgId = metadata.getOrgId();
		Boolean isSandbox = metadata.getIsSandbox();
		MetadataImport metadataImport = new MetadataImport(Configuration.getInstance().getServer().metadataBaseDirectory, sourceOrgId);
		
		if (! isSandbox) {
			if (! isForce) {
				throw new RuntimeException("Trying to perform a Load Metadata in Production. Please use the extra command line argument -force");
			}
		}
		
		if (sourceOrgId.equals(targetOrgId)) {
			if (! isForce) {
				throw new RuntimeException("Trying to perform a Load Metadata using as target the same Organization that produced the Export. Please use the extra command line argument -force");
			}
		}
		
		metadataImport.importCSVFiles();
	}
	
	private void loadModel(String sourceOrgId, Boolean isForce) throws Exception {
		Setup setup = new Setup();
		
		if (! isForce) {
			throw new RuntimeException("Trying to perform a Load Model. Please use the extra command line argument -force");
		}
		
		setup.runApplicationSetup(sourceOrgId);
	}
	
	private void listTasks() throws Exception {
		Metadata metadata = Factory.getMetadataInstance();
		
		for (TaskMetadata taskMetadata : metadata.taskMap.values()) {
			System.out.println("TaskID=" + taskMetadata.id + ", UniqueID=" + taskMetadata.uniqueId + ", TaskName=" + taskMetadata.taskName);
		}
	}
	
	private void listTaskFlows() throws Exception {
		Metadata metadata = Factory.getMetadataInstance();
		
		for (TaskFlowMetadata taskFlowMetadata : metadata.taskFlowMap.values()) {
			System.out.println("TaskFlowID=" + taskFlowMetadata.id + ", UniqueID=" + taskFlowMetadata.uniqueId + ", TaskFlowName=" + taskFlowMetadata.flowName);
		}		
	}
}
