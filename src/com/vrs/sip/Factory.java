/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Factory.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip;

import com.vrs.sip.connection.ConnectionType;
import com.vrs.sip.connection.IConnection;
import com.vrs.sip.connection.IConnectionAttributes;
import com.vrs.sip.connection.ICredentials;
import com.vrs.sip.connection.attributes.*;
import com.vrs.sip.connection.credentials.ExchangeCredentials;
import com.vrs.sip.connection.credentials.NoCredentials;
import com.vrs.sip.connection.credentials.OracleCredentials;
import com.vrs.sip.connection.credentials.SalesforceCredentials;
import com.vrs.sip.connection.drivers.*;
import com.vrs.sip.metadata.*;
import com.vrs.sip.task.*;
import com.vrs.sip.task.AbstractTaskStep.TaskStepType;
import com.vrs.sip.task.Schedule.RecurrenceType;
import com.vrs.sip.task.Schedule.ScheduleType;
import com.vrs.sip.task.step.*;
import com.vrs.sip.util.Helper;

import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("rawtypes")
public class Factory {
	private static FileLog log = FileLog.getNewInstance(Factory.class, "factory_" + Util.getSimpleUniqueId(), ".log");

	private final static char DEFAULT_CSV_FIELD_SEPARATOR = ',';
	
	private static final Map<ConnectionType, Class> connectorImplMap = initializeConnectorImplementations();
	private static Metadata metadata;

	private static Map<ConnectionType, Class> initializeConnectorImplementations() {
		HashMap<ConnectionType, Class> resultMap = new HashMap<ConnectionType, Class>();

		resultMap.put(ConnectionType.CSV, CSVConnection.class);
		resultMap.put(ConnectionType.ORACLE, OracleConnection.class);
		resultMap.put(ConnectionType.SALESFORCE, SalesforceConnection.class);
		resultMap.put(ConnectionType.FILESYSTEM, FilesystemConnection.class);
		resultMap.put(ConnectionType.EXCHANGE, ExchangeConnection.class);
        resultMap.put(ConnectionType.SALESFORCE_WITH_EXCHANGE, SaleforceExchangeConnection.class);
        resultMap.put(ConnectionType.FILESYSTEM_IN_MEMORY, FilesSystemInMemoryConnection.class);




		return resultMap;
	}

	private static synchronized Metadata initializeMetadata(Boolean isInstallation) {
		String metadataPropertiesFilename = "metadata.properties";
		Metadata metadataInstance = null;
		IConnection metadataConnection;

		if (metadata == null) {
			try {
				metadataConnection = getConnection(ConnectionType.SALESFORCE);
	
				metadataConnection.setCredentials(metadataPropertiesFilename);
				metadataConnection.setConnectionAttributes(metadataPropertiesFilename);
	
				metadataConnection.setLog(log);
			
				// Warning: The following println statements should not be changed to log.info or log.debug due to the way the log 
				// initialization is dependent on the attributes of Metadata class.
				
				/*
				System.out.println(Util.getCurrentThreadStackTraceString());
				*/
				System.out.println("Metadata Initialization: Connecting with username \'" + metadataConnection.getCredentials().getUsername() + "\' using Endpoint \'" + metadataConnection.getCredentials().getLoginServer() + "\'");
	
				metadataConnection.openConnection();
	
				metadataInstance = new Metadata(metadataConnection, isInstallation);
	
				metadataConnection.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
	
			metadata = metadataInstance;
		}
		
		return metadata;
	}

	public static Metadata getMetadataInstance(Boolean isInstallation) {
		return initializeMetadata(isInstallation);
	}
	
	public static Metadata getMetadataInstance() {
		return initializeMetadata(false);
	}

	/** Get a new Connection Instance **/
	public static IConnection getConnection(ConnectionType conType)
			throws InstantiationException, IllegalAccessException {
		IConnection connector = null;
		Class implClass = connectorImplMap.get(conType);

		if (implClass != null) {
			connector = (IConnection) implClass.newInstance();
		}

		return connector;
	}

	/** Get a Task Flow by Id **/
	public static TaskFlow getTaskFlow1(String taskFlowId) throws Exception {
		TaskFlow taskFlowInstance = null;

		for (TaskFlowMetadata taskFlowMetadata : getMetadataInstance().taskFlowMap.values()) {
			if (taskFlowMetadata.id.equals(taskFlowId)) {
				taskFlowInstance = new TaskFlow();

				taskFlowInstance.taskFlowId = taskFlowMetadata.id;
				taskFlowInstance.taskFlowName = taskFlowMetadata.flowName;
				taskFlowInstance.successEmails = taskFlowMetadata.successEmails;
				taskFlowInstance.warningEmails = taskFlowMetadata.warningEmails;
				taskFlowInstance.failureEmails = taskFlowMetadata.failureEmails;
				taskFlowInstance.lastScheduleDate = taskFlowMetadata.lastScheduledDate;
				taskFlowInstance.retryIfFail = taskFlowMetadata.retryIfFail;
				taskFlowInstance.maxRetryCount = taskFlowMetadata.maxRetryCount != null ? taskFlowMetadata.maxRetryCount : 0;

				if (taskFlowMetadata.scheduleId != null) {
					taskFlowInstance.taskFlowSchedule = getSchedule(taskFlowMetadata.scheduleId);
				}
				/*
				else {

				    //TODO: Procurar ScheduleId in Schedule by taskFlowId
                    ScheduleMetadata scheduleMetadata =  searchTaskFlowInScheduleMetadataByTaskFlowId(taskFlowInstance.taskFlowId);

                    if (scheduleMetadata != null){

                        taskFlowInstance.taskFlowSchedule = getSchedule(scheduleMetadata.id);
                    }

                }*/
				break;
			}
		}

		if (taskFlowInstance != null) {

          Boolean hasTasksFromTaskFlowTasks = false;

            //TODO: Adding the New JUNCTION OBJECT - RELATE TASKS WITH TASKSFLOW.
            for (TaskFlowsTasksMetadata taskFlowsTasksMetadata : getMetadataInstance().taskFlowsTasksMap.values()) {
                if (taskFlowsTasksMetadata.taskFlowId != null) {
                    if (taskFlowsTasksMetadata.taskFlowId.equals(taskFlowId)) {

                        log.debug("taskFlowsTasksMetadata.taskFlowId = " + taskFlowsTasksMetadata.taskFlowId);

                        Task task = getTask(taskFlowsTasksMetadata.taskId, taskFlowInstance.taskFlowSchedule );

                        log.debug("task= " + task.taskName);

                        if (!taskFlowInstance.taskList.contains(task)){

                            hasTasksFromTaskFlowTasks = true;
                            task.order =  taskFlowsTasksMetadata.order;
                            taskFlowInstance.taskList.add(task);
                        }
                    }
                }
            }


           if (!hasTasksFromTaskFlowTasks)
           {
               for (TaskMetadata taskMetadata : getMetadataInstance().taskMap.values()) {
                   if (taskMetadata.taskFlowId != null) {
                       if (taskMetadata.taskFlowId.equals(taskFlowId)) {

                           taskFlowInstance.taskList.add(getTask(taskMetadata.id, taskFlowInstance.taskFlowSchedule));

                       }
                   }
               }
           }




			Collections.sort(taskFlowInstance.taskList);
		}

		return taskFlowInstance;
	}






    /** Get a Task Flow by Id **/
    public static TaskFlow getTaskFlow(String taskFlowId, Schedule schedule) throws Exception {
        TaskFlow taskFlowInstance = null;

        for (TaskFlowMetadata taskFlowMetadata : getMetadataInstance().taskFlowMap.values()) {
            if (taskFlowMetadata.id.equals(taskFlowId)) {
                taskFlowInstance = new TaskFlow();

                taskFlowInstance.taskFlowId = taskFlowMetadata.id;
                taskFlowInstance.taskFlowName = taskFlowMetadata.flowName;
                taskFlowInstance.successEmails = taskFlowMetadata.successEmails;
                taskFlowInstance.warningEmails = taskFlowMetadata.warningEmails;
                taskFlowInstance.failureEmails = taskFlowMetadata.failureEmails;
                taskFlowInstance.lastScheduleDate = taskFlowMetadata.lastScheduledDate;
                taskFlowInstance.retryIfFail = taskFlowMetadata.retryIfFail;
                taskFlowInstance.maxRetryCount = taskFlowMetadata.maxRetryCount != null ? taskFlowMetadata.maxRetryCount : 0;
                taskFlowInstance.taskFlowSchedule = schedule;

                if (schedule == null && taskFlowMetadata.scheduleId != null) {
                    taskFlowInstance.taskFlowSchedule = getSchedule(taskFlowMetadata.scheduleId);
                }
                else if (schedule != null)
                    taskFlowInstance.taskFlowSchedule = schedule;

                break;
            }
        }

        if (taskFlowInstance != null) {

            Boolean hasTasksFromTaskFlowTasks = false;

            //TODO: Adding the New JUNCTION OBJECT - RELATE TASKS WITH TASKSFLOW.
            for (TaskFlowsTasksMetadata taskFlowsTasksMetadata : getMetadataInstance().taskFlowsTasksMap.values()) {
                if (taskFlowsTasksMetadata.taskFlowId != null) {
                    if (taskFlowsTasksMetadata.taskFlowId.equals(taskFlowId)) {

                        log.debug("taskFlowsTasksMetadata.taskFlowId = " + taskFlowsTasksMetadata.taskFlowId);

                        Task task = getTask(taskFlowsTasksMetadata.taskId, schedule);

                        log.debug("task= " + task.taskName);

                        if (!taskFlowInstance.taskList.contains(task)){

                            hasTasksFromTaskFlowTasks = true;
                            task.taskFlowId = taskFlowInstance.taskFlowId;
                            task.order =  taskFlowsTasksMetadata.order;
                            taskFlowInstance.taskList.add(task);

                        }
                    }
                }
            }


            if (!hasTasksFromTaskFlowTasks)
            {
                for (TaskMetadata taskMetadata : getMetadataInstance().taskMap.values()) {
                    if (taskMetadata.taskFlowId != null) {
                        if (taskMetadata.taskFlowId.equals(taskFlowId)) {

                            taskFlowInstance.taskList.add(getTask(taskMetadata.id, schedule));

                        }
                    }
                }
            }




            Collections.sort(taskFlowInstance.taskList);
        }

        return taskFlowInstance;
    }


    /**
     * Get a Task by Task Id and Set The Schedule defined by Parameter
     *
     * @param taskId
     * @return
     * @throws Exception
     */
    public static Task getTask1(String taskId, Schedule schedule) throws Exception {

         Task task = getTask(taskId,schedule);

         if (task != null)
             task.taskSchedule = schedule;


         return task;

    }


    /**
	 * Get a Task by Task Id.
	 * 
	 * @param taskId
	 * @return
	 * @throws Exception
	 */
	public static Task getTask(String taskId, Schedule schedule) throws Exception {

		Task taskInstance = null;
		TaskMetadata taskMetadata = getMetadataInstance().taskMap.get(taskId);
		List<TaskStepMetadata> taskStepMetadataList = new Vector<TaskStepMetadata>();

		if (getMetadataInstance().taskMap.containsKey(taskId)) {
			log.debug("Task with Id=" + taskId + " is found on TaskMetadata");
		} else {
			log.debug("***** Task Id = " + taskId + " not found");
		}

		log.debug("TaskMetadata: " + taskMetadata);


		if (taskMetadata != null) {
            taskInstance = new Task();

            taskInstance.abortOnFailure = taskMetadata.abortOnFailure;
            taskInstance.order = taskMetadata.order;
            taskInstance.taskId = taskMetadata.id;
            taskInstance.taskFlowId = taskMetadata.taskFlowId;
            taskInstance.taskName = taskMetadata.taskName;
            taskInstance.preProcessingScript = taskMetadata.preProcessingScript;
            taskInstance.postProcessingScript = taskMetadata.postProcessingScript;
            taskInstance.successEmails = taskMetadata.successEmails;
            taskInstance.warningEmails = taskMetadata.warningEmails;
            taskInstance.failureEmails = taskMetadata.failureEmails;
            taskInstance.lastScheduleDate = taskMetadata.lastScheduledDate;
            taskInstance.retryIfFail = taskMetadata.retryIfFail;
            taskInstance.maxRetryCount = taskMetadata.maxRetryCount != null ? taskMetadata.maxRetryCount : 0;

            if (schedule == null && taskMetadata.scheduleId != null) {
                taskInstance.taskSchedule = getSchedule(taskMetadata.scheduleId);
            }
            else if ( schedule != null )
                taskInstance.taskSchedule = schedule;

            /******
            else {

                if (taskInstance.taskFlowId == null ) {

                    log.info("taskMetadata.taskFlowId........  = " + taskInstance.taskFlowId);

                    TaskFlowsTasksMetadata taskFlowsTasksMetadata = searchTaskFlowsTasksInMetadataByTaskId(taskInstance.taskId);

                    taskInstance.taskSchedule = searchScheduleInTaskFlow(taskInstance, taskFlowsTasksMetadata, getMetadataInstance().taskFlowMap);

                    if(taskInstance.taskSchedule == null)
                        taskInstance.taskSchedule = searchTaskInScheduleByTaskId(taskInstance.taskId);


                }
                else {

                    TaskFlowMetadata taskFlowMetadata = getMetadataInstance().taskFlowMap.get(taskInstance.taskFlowId);

                    if (taskFlowMetadata.scheduleId == null){

                        if (taskInstance.taskFlowId != null)
                            taskInstance.taskSchedule =  searchTaskFlowInScheduleByTaskFlowId(taskInstance.taskFlowId);
                        else if (taskInstance.taskId != null) {
                            taskInstance.taskSchedule =  searchTaskInScheduleByTaskId(taskInstance.taskId);
                        }
                        else
                        {

                            log.info("Warning: The task id =" + taskInstance.taskId + "has not bee found in the Schedule Records" );
                        }

                    }
                    else
                         taskInstance.taskSchedule = getSchedule(taskFlowMetadata.scheduleId);

                }




            }
            **********/
		}

		// Load Task Steps
		if (taskInstance != null && taskInstance.taskId != null) {
			for (TaskStepMetadata taskStepMetadata : getMetadataInstance().taskStepMap.values()) {
				if (taskStepMetadata.taskId.equals(taskInstance.taskId)) {
					taskStepMetadataList.add(taskStepMetadata);
				}
			}
		}

		if (taskStepMetadataList.isEmpty() == false) {
			for (TaskStepMetadata taskStepMetadata : taskStepMetadataList) {
				AbstractTaskStep taskStep;
				TaskStepType taskStepType = null;

				taskStep = null;
				switch (taskStepMetadata.stepType) {
				case "Filter":
					taskStep = new Filter();
					taskStepType = TaskStepType.Filter;

					taskStep.setTaskStepType(taskStepType.name());

					taskStep.setCompleted(false);

					break;

				case "Extract":
					taskStep = new Extract();
					taskStepType = TaskStepType.Extract;

					taskStep.setTaskStepType(taskStepType.name());

					taskStep.setCompleted(false);

					break;

				case "Transform":
					taskStep = new Transform();
					taskStepType = TaskStepType.Transform;

					taskStep.setTaskStepType(taskStepType.name());

					taskStep.setCompleted(false);

					break;

				case "Load":
					taskStep = new Load();
					taskStepType = TaskStepType.Load;

					taskStep.setTaskStepType(taskStepType.name());

					taskStep.setCompleted(false);


					break;

				case "File":
					taskStep = new File();
					taskStep.setCompleted(false);

					taskStepType = TaskStepType.File;

					taskStep.setTaskStepType(taskStepType.name());

					break;
				}

				if (taskStep != null) {
					String fieldSeparatorString = taskStepMetadata.fieldSeparator;
					char fieldSeparator = 0;
					
					taskStep.setOrder(taskStepMetadata.order);
					taskStep.setTruncateTarget(taskStepMetadata.truncateTarget);
					taskStep.setBatchSize(taskStepMetadata.batchSize);
					
					if (fieldSeparatorString != null) {
						if (fieldSeparatorString.length() != 1) {
							throw new RuntimeException("Invalid configuration for field separator, must be a string of exactly 1 character length.");
						}
						
						fieldSeparator = fieldSeparatorString.charAt(0);
						
						taskStep.setFieldSeparator(fieldSeparator);
					} else {
						taskStep.setFieldSeparator(DEFAULT_CSV_FIELD_SEPARATOR);
					}
				}

				if (taskStep != null) {


				    taskStep.setSchedule(taskInstance.taskSchedule);

					ConnectionMetadata sourceConnectionMetadata = null;
					ConnectionMetadata targetConnectionMetadata = null;
					ConnectionType sourceConnectionType = null;
					ConnectionType targetConnectionType = null;
					IConnectionAttributes sourceConnectionAttributes = null;
					IConnectionAttributes targetConnectionAttributes = null;
					ICredentials sourceConnectionCredentials = null;
					ICredentials targetConnectionCredentials = null;

					taskStep.taskStepId = taskStepMetadata.id;
					taskStep.taskStepName = taskStepMetadata.name;
					taskStep.taskStepType = taskStepType;

					if (taskStepMetadata.sourceConnectionId != null) {
						sourceConnectionMetadata = getMetadataInstance().connectionMap
								.get(taskStepMetadata.sourceConnectionId);

						if (sourceConnectionMetadata != null) {
							switch (sourceConnectionMetadata.connectionType) {
							case "CSV":
								sourceConnectionType = ConnectionType.CSV;
								sourceConnectionAttributes = new CSVConnectionAttributes();
								sourceConnectionCredentials = new NoCredentials();

								break;

							case "Oracle":
								sourceConnectionType = ConnectionType.ORACLE;
								sourceConnectionAttributes = new OracleConnectionAttributes();
								sourceConnectionCredentials = new OracleCredentials();

								sourceConnectionCredentials.setUsername(sourceConnectionMetadata.username);
								sourceConnectionCredentials.setPassword(sourceConnectionMetadata.password);
								sourceConnectionCredentials.setHostname(sourceConnectionMetadata.hostname);
								sourceConnectionCredentials.setPort(sourceConnectionMetadata.port);
								sourceConnectionCredentials.setService(sourceConnectionMetadata.service);

								break;

							case "Salesforce":
								sourceConnectionType = ConnectionType.SALESFORCE;
								sourceConnectionAttributes = new SalesforceConnectionAttributes();
								sourceConnectionCredentials = new SalesforceCredentials();

								sourceConnectionCredentials.setUsername(sourceConnectionMetadata.username);
								sourceConnectionCredentials.setPassword(sourceConnectionMetadata.password);
								sourceConnectionCredentials.setSecurityToken(sourceConnectionMetadata.securityToken);
								sourceConnectionCredentials.setLoginServer(sourceConnectionMetadata.loginServer);

								break;

							case "Filesystem":
								sourceConnectionType = ConnectionType.FILESYSTEM;
								sourceConnectionAttributes = new FilesystemConnectionAttributes();
								sourceConnectionCredentials = new NoCredentials();

								break;

							case "Filesystem_In_Memory":

                                    sourceConnectionType = ConnectionType.FILESYSTEM_IN_MEMORY;
                                    sourceConnectionAttributes = new FilesystemConnectionAttributes();
                                    sourceConnectionCredentials = new NoCredentials();

                             break;



                                case "Exchange":
                                    sourceConnectionType = ConnectionType.EXCHANGE;
                                    sourceConnectionAttributes = new ExchangeConnectionAttributes();
                                    sourceConnectionCredentials = new ExchangeCredentials();

                                    sourceConnectionCredentials.setUsername(sourceConnectionMetadata.username);
                                    sourceConnectionCredentials.setPassword(sourceConnectionMetadata.password);
                                    sourceConnectionCredentials.setLoginServer(sourceConnectionMetadata.loginServer);
                                    sourceConnectionCredentials.setImpersonatedUserAccount(sourceConnectionMetadata.impersonatedEmailAccount);
                                    sourceConnectionCredentials.setProxyHostname(sourceConnectionMetadata.proxyHostName);
                                    sourceConnectionCredentials.setProxyPort(sourceConnectionMetadata.proxyPort);


                                    break;

                             case "Salesforce_With_Exchange":
                                 sourceConnectionType = ConnectionType.SALESFORCE_WITH_EXCHANGE;
                                 sourceConnectionAttributes = new SalesforceConnectionAttributes();
                                 sourceConnectionCredentials = new SalesforceCredentials();

                                 sourceConnectionCredentials.setUsername(sourceConnectionMetadata.username);
                                 sourceConnectionCredentials.setPassword(sourceConnectionMetadata.password);
                                 sourceConnectionCredentials.setSecurityToken(sourceConnectionMetadata.securityToken);
                                 sourceConnectionCredentials.setLoginServer(sourceConnectionMetadata.loginServer);


                                 break;
							}
						}

						if (sourceConnectionAttributes != null) {
							sourceConnectionAttributes.set(sourceConnectionMetadata.autoCommit,
									sourceConnectionMetadata.loginTimeout, sourceConnectionMetadata.batchSize,
									sourceConnectionMetadata.directory, sourceConnectionMetadata.dateFormat,
									sourceConnectionMetadata.customDateFormat, sourceConnectionMetadata.charset,
									sourceConnectionMetadata.allOrNone);
						}
					}

					if (taskStepMetadata.targetConnectionId != null) {
						targetConnectionMetadata = getMetadataInstance().connectionMap
								.get(taskStepMetadata.targetConnectionId);

						if (targetConnectionMetadata != null) {


							switch (targetConnectionMetadata.connectionType) {
							case "CSV":
								targetConnectionType = ConnectionType.CSV;
								targetConnectionAttributes = new CSVConnectionAttributes();
								targetConnectionCredentials = new NoCredentials();

								break;

							case "Oracle":
								targetConnectionType = ConnectionType.ORACLE;
								targetConnectionAttributes = new OracleConnectionAttributes();
								targetConnectionCredentials = new OracleCredentials();

								targetConnectionCredentials.setUsername(targetConnectionMetadata.username);
								targetConnectionCredentials.setPassword(targetConnectionMetadata.password);
								targetConnectionCredentials.setHostname(targetConnectionMetadata.hostname);
								targetConnectionCredentials.setPort(targetConnectionMetadata.port);
								targetConnectionCredentials.setService(targetConnectionMetadata.service);

								break;

							case "Salesforce":
								targetConnectionType = ConnectionType.SALESFORCE;
								targetConnectionAttributes = new SalesforceConnectionAttributes();
								targetConnectionCredentials = new SalesforceCredentials();

								targetConnectionCredentials.setUsername(targetConnectionMetadata.username);
								targetConnectionCredentials.setPassword(targetConnectionMetadata.password);
								targetConnectionCredentials.setSecurityToken(targetConnectionMetadata.securityToken);
								targetConnectionCredentials.setLoginServer(targetConnectionMetadata.loginServer);

								break;

							case "Filesystem":
								targetConnectionType = ConnectionType.FILESYSTEM;
								targetConnectionAttributes = new FilesystemConnectionAttributes();
								targetConnectionCredentials = new NoCredentials();

								break;

                            case "Filesystem_In_Memory":
                                    targetConnectionType = ConnectionType.FILESYSTEM_IN_MEMORY;
                                    targetConnectionAttributes = new FilesystemConnectionAttributes();
                                    targetConnectionCredentials = new NoCredentials();

                            break;

					    	case "Exchange":
                                    targetConnectionType = ConnectionType.EXCHANGE;
                                    targetConnectionAttributes = new ExchangeConnectionAttributes();
                                    targetConnectionCredentials = new ExchangeCredentials();

                                    targetConnectionCredentials.setUsername(targetConnectionMetadata.username);
                                    targetConnectionCredentials.setPassword(targetConnectionMetadata.password);
                                    targetConnectionCredentials.setLoginServer(targetConnectionMetadata.loginServer);
                                    targetConnectionCredentials.setImpersonatedUserAccount(targetConnectionMetadata.impersonatedEmailAccount);
                                    targetConnectionCredentials.setProxyHostname(targetConnectionMetadata.proxyHostName);
                                    targetConnectionCredentials.setProxyPort(targetConnectionMetadata.proxyPort);

                                    break;

                            case "Salesforce_With_Exchange":

                                    targetConnectionType = ConnectionType.SALESFORCE_WITH_EXCHANGE;
                                    targetConnectionAttributes = new SalesforceConnectionAttributes();
                                    targetConnectionCredentials = new SalesforceCredentials();

                                    targetConnectionCredentials.setUsername(targetConnectionMetadata.username);
                                    targetConnectionCredentials.setPassword(targetConnectionMetadata.password);
                                    targetConnectionCredentials.setSecurityToken(targetConnectionMetadata.securityToken);
                                    targetConnectionCredentials.setLoginServer(targetConnectionMetadata.loginServer);

                                    break;


							}
						}

						if (targetConnectionAttributes != null) {
							targetConnectionAttributes.set(targetConnectionMetadata.autoCommit,
									targetConnectionMetadata.loginTimeout, targetConnectionMetadata.batchSize,
									targetConnectionMetadata.directory, targetConnectionMetadata.dateFormat,
									targetConnectionMetadata.customDateFormat, targetConnectionMetadata.charset,
									targetConnectionMetadata.allOrNone);
						}
					}

					// Load the Source Connection
					if (sourceConnectionMetadata != null) {
						log.debug("Setting Task Step source connection");

						taskStep.setSourceConnection(getConnection(sourceConnectionType));

						if (sourceConnectionCredentials != null) {
							taskStep.getSourceConnection().setCredentials(sourceConnectionCredentials);
						}

						if (sourceConnectionAttributes != null) {
							taskStep.getSourceConnection().setConnectionAttributes(sourceConnectionAttributes);
						}

						log.debug("SourceConnection = " + taskStep.sourceConnection);
					}

					// Load the Target Connection
					if (targetConnectionMetadata != null) {
						taskStep.setTargetConnection(getConnection(targetConnectionType));

						if (targetConnectionCredentials != null) {
							taskStep.getTargetConnection().setCredentials(targetConnectionCredentials);
						}

						if (targetConnectionAttributes != null) {
							taskStep.getTargetConnection().setConnectionAttributes(targetConnectionAttributes);
						}
					}
				}

				// Load the Source Entity
				if (taskStep != null) {
					EntityMetadata entityMetadata = null;
					List<EntityFieldMetadata> entityFieldMetadataList = new Vector<EntityFieldMetadata>();
					Entity entity = null;

					for (EntityMetadata em : getMetadataInstance().entityMap.values()) {
						if (em.taskStepId.equals(taskStep.taskStepId)) {
							if (em.entityType.equals("Source")) {
								entityMetadata = em;
								break;
							}
						}
					}

					if (entityMetadata != null) {
						for (EntityFieldMetadata ef : getMetadataInstance().entityFieldMap.values()) {
							if (ef.entityId.equals(entityMetadata.id)) {
								entityFieldMetadataList.add(ef);
							}
						}
					}

					if (entityMetadata != null) {
						entity = new Entity();

						entity.entityId = entityMetadata.id;

						entity.entityName = Helper.parsingEntityName(entityMetadata.entityName, taskInstance.taskSchedule); //TODO: New Transformation - PROJ-1613760_BREQ-019

                        log.debug("entity.entityName = "+entity.entityName );

						for (EntityFieldMetadata ef : entityFieldMetadataList) {
							EntityField entityField = new EntityField();

							entityField.entityFieldId = ef.id;
							entityField.entityFieldName = ef.fieldName;
							entityField.order = ef.order;

							switch (ef.type) {
							case "Boolean":
								entityField.entityFieldType = EntityField.EntityFieldType.Boolean;
								break;

							case "Date":
								entityField.entityFieldType = EntityField.EntityFieldType.Date;

								break;

							case "Decimal":
								entityField.entityFieldType = EntityField.EntityFieldType.Decimal;

								break;

							case "Integer":
								entityField.entityFieldType = EntityField.EntityFieldType.Integer;

								break;

							case "String":
								entityField.entityFieldType = EntityField.EntityFieldType.String;

								break;
							}

							entity.addField(entityField);
						}
					}

					if (entity != null) {
						entity.sortFields();
					}


					taskStep.sourceEntity = entity;
				}

				// Load the Target Entity
				if (taskStep != null) {
					EntityMetadata entityMetadata = null;
					List<EntityFieldMetadata> entityFieldMetadataList = new Vector<EntityFieldMetadata>();
					Entity entity = null;

					for (EntityMetadata em : getMetadataInstance().entityMap.values()) {
						if (em.taskStepId.equals(taskStep.taskStepId)) {
							if (em.entityType.equals("Target")) {
								entityMetadata = em;
								break;
							}
						}
					}

					if (entityMetadata != null) {
						for (EntityFieldMetadata ef : getMetadataInstance().entityFieldMap.values()) {
							if (ef.entityId.equals(entityMetadata.id)) {
								entityFieldMetadataList.add(ef);
							}
						}
					}

					if (entityMetadata != null) {
						entity = new Entity();

						entity.entityId = entityMetadata.id;
						entity.entityName = Helper.parsingEntityName(entityMetadata.entityName, taskInstance.taskSchedule); //TODO: New Transformation - PROJ-1613760_BREQ-019

                        log.debug(entity.entityName);

                        for (EntityFieldMetadata ef : entityFieldMetadataList) {
							EntityField entityField = new EntityField();

							entityField.entityFieldId = ef.id;
							entityField.entityFieldName = ef.fieldName;
							entityField.order = ef.order;

							switch (ef.type) {
							case "Boolean":
								entityField.entityFieldType = EntityField.EntityFieldType.Boolean;
								break;

							case "Date":
								entityField.entityFieldType = EntityField.EntityFieldType.Date;

								break;

							case "Decimal":
								entityField.entityFieldType = EntityField.EntityFieldType.Decimal;

								break;

							case "Integer":
								entityField.entityFieldType = EntityField.EntityFieldType.Integer;

								break;

							case "String":
								entityField.entityFieldType = EntityField.EntityFieldType.String;

								break;
							}

							entity.addField(entityField);
						}
					}

					if (entity != null) {
						entity.sortFields();
					}

					taskStep.targetEntity = entity;
				}

				// Load the Transformation
				if (taskStep != null) {
					List<TransformationMetadata> transformationMetadataList = new Vector<TransformationMetadata>();

					// Load the Transformation List
					for (TransformationMetadata transformationMetadata : metadata.transformationMap.values()) {
						if (transformationMetadata.taskStepId.equals(taskStep.taskStepId)) {
							transformationMetadataList.add(transformationMetadata);
						}
					}

					for (TransformationMetadata transformationMetadata : transformationMetadataList) {
						Transformation transformation = new Transformation();

						transformation.targetFieldName = transformationMetadata.targetField;
						transformation.transformation = transformationMetadata.transformation;
						transformation.transformationId = transformationMetadata.id;
						transformation.transformationName = transformationMetadata.name;
						transformation.order = transformationMetadata.order;

						taskStep.getTransformationList().add(transformation);
					}

					Collections.sort(taskStep.getTransformationList());
				}

				taskInstance.getTaskSteps().add(taskStep);

				// Final mappings for Task Step (copy some values from Task)
				if (taskStep != null) {
					if (taskStepMetadata.operation != null) {
						switch (taskStepMetadata.operation) {
						case "Insert":
							taskStep.operation = AbstractTaskStep.OperationType.Insert;
							break;

						case "Update":
							taskStep.operation = AbstractTaskStep.OperationType.Update;
							break;

						case "Upsert":
							taskStep.operation = AbstractTaskStep.OperationType.Upsert;
							break;

						case "Delete":
							taskStep.operation = AbstractTaskStep.OperationType.Delete;
							break;

						case "File Attach":
							taskStep.operation = AbstractTaskStep.OperationType.FileAttach;
							break;

						case "Execute Call":
							taskStep.operation = AbstractTaskStep.OperationType.ExecuteCall;
							break;

						case "Insert_with_Attachments":
                                taskStep.operation = AbstractTaskStep.OperationType.InsertWithFileAttachment;
                                break;

                        case "Send_Email_With_Attachments":
                                taskStep.operation = AbstractTaskStep.OperationType.SendEmailWithAttachment;
                                break;

                         case "Move_Emails_to_Folder":
								taskStep.operation = AbstractTaskStep.OperationType.MoveEmailsToFolder;
								break;

						}
					}

					List<String> keyFieldList = new Vector<String>();

					if (taskStepMetadata.operationKeyFieldList != null
							&& taskStepMetadata.operationKeyFieldList.trim().isEmpty() == false) {
						for (String operationKeyField : taskStepMetadata.operationKeyFieldList.split(",")) {
							keyFieldList.add(operationKeyField);
						}
					}

					taskStep.setOperationKeyFieldList(keyFieldList);
				}
			}
		}

		if (taskInstance.taskSteps != null) {
			Collections.sort(taskInstance.taskSteps);
		}

		return taskInstance;
	}

    private static Schedule searchTaskInScheduleByTaskId(String taskId) {

            ScheduleMetadata scheduleMetadata = searchTaskInScheduleMetadataByTaskId(taskId);

            if (scheduleMetadata != null) {

                return getSchedule(scheduleMetadata.id);

            }

            return null;

    }

    private static Schedule searchTaskFlowInScheduleByTaskFlowId(String taskFlowId) {

        ScheduleMetadata scheduleMetadata = searchTaskFlowInScheduleMetadataByTaskFlowId(taskFlowId);

        log.info("ORANDO = "+ scheduleMetadata);

        if (scheduleMetadata != null) {

            log.info("ORANDO id = "+ scheduleMetadata.id);

            return getSchedule(scheduleMetadata.id);

        }

        return null;

    }

    private static Schedule searchScheduleInTaskFlow(Task taskInstance, TaskFlowsTasksMetadata taskFlowsTasksMetadata, Map<String,TaskFlowMetadata> taskFlowMap) {

        Schedule taskSchedule =  null;

        if (taskInstance.taskFlowId == null)
            taskInstance.taskFlowId = taskFlowsTasksMetadata == null ? null : taskFlowsTasksMetadata.taskFlowId;


        if (taskInstance.taskFlowId != null) {

            TaskFlowMetadata taskFlowMetadata = taskFlowMap.get(taskInstance.taskFlowId);

            log.debug("taskFlowMetadata  = " + taskFlowMetadata);

            if (taskFlowMetadata != null) {

                if (taskFlowMetadata.scheduleId != null)
                    taskSchedule = getSchedule(taskFlowMetadata.scheduleId);
                else
                    taskSchedule = searchTaskFlowInScheduleByTaskFlowId(taskFlowMetadata.id);

            }
        }

        return taskSchedule;

    }

    private static ScheduleMetadata searchTaskInScheduleMetadataByTaskId(String taskIdAux) {

        if (getMetadataInstance().scheduleMap != null && getMetadataInstance().scheduleMap.values() != null)
         return getMetadataInstance().scheduleMap.values().stream().filter(x -> x.taskId == null ? false : x.taskId .equals(taskIdAux)).findFirst().orElse(null);
        else
         return  null;
    }

    private static ScheduleMetadata searchTaskFlowInScheduleMetadataByTaskFlowId(String taskFlowId) {

        log.info("BARBARBA fdfdfdf + " + taskFlowId);

        getMetadataInstance().scheduleMap.values().forEach(

                x-> {

                    log.info("BARBARBA + " + x.taskFlowId);

                    log.info("BARBARBA 111+ " + x.taskFlowId.equals(taskFlowId));

                    log.info("BARBARBA222 + " + x.taskId);

                }



        );


        if (getMetadataInstance().scheduleMap != null && getMetadataInstance().scheduleMap.values() != null)
            return getMetadataInstance().scheduleMap.values().stream().filter(x -> x.taskFlowId == null ? false :  x.taskFlowId.equals(taskFlowId)).findFirst().orElse(null);
        else
            return null;

    }

    private static TaskFlowsTasksMetadata searchTaskFlowsTasksInMetadataByTaskId(String taskIdAux) {

        if (getMetadataInstance().taskFlowsTasksMap != null && getMetadataInstance().taskFlowsTasksMap.values() != null)
	     return getMetadataInstance().taskFlowsTasksMap.values().stream().filter(x -> x.taskId == null ? false : x.taskId.equals(taskIdAux)).findFirst().orElse(null);
        else
	     return null;
	}

    private static TaskFlowsTasksMetadata searchTaskFlowsTasksInMetadataByTaskFlowId(String taskFlowId) {

        if (getMetadataInstance().taskFlowsTasksMap != null && getMetadataInstance().taskFlowsTasksMap.values() != null)
            return getMetadataInstance().taskFlowsTasksMap.values().stream().filter(x -> x.taskFlowId == null ? false : x.taskId.equals(taskFlowId)).findFirst().orElse(null);
        else
            return null;
    }


    //TODO: OPA
	public static Schedule getSchedule(String scheduleId) {
		ScheduleMetadata scheduleMetadata = metadata.scheduleMap.get(scheduleId);
		Schedule scheduleInstance = null;

        log.info("scheduleMetadata is null? value = " + scheduleMetadata );


		if (scheduleMetadata != null) {

            scheduleInstance = new Schedule();
			scheduleInstance.scheduleId = scheduleMetadata.id;
			scheduleInstance.scheduleName = scheduleMetadata.name;
			scheduleInstance.startDate = scheduleMetadata.startDate;
			log.info("scheduleInstance.startDate22121 =" + scheduleInstance.startDate  );
			scheduleInstance.endDate = scheduleMetadata.endDate;

            scheduleInstance.EmailTo = scheduleMetadata.EmailTo;
            scheduleInstance.EmailCc = scheduleMetadata.EmailCc;
            scheduleInstance.EmailBcc = scheduleMetadata.EmailBcc;
            scheduleInstance.EmailSubject = scheduleMetadata.EmailSubject;
            scheduleInstance.EmailBody = scheduleMetadata.EmailBody;

            scheduleInstance.Var1 =  scheduleMetadata.Var1;
            scheduleInstance.Var2 =  scheduleMetadata.Var2;
            scheduleInstance.Var3 =  scheduleMetadata.Var3;
            scheduleInstance.Var4 =  scheduleMetadata.Var4;
            scheduleInstance.Var5 =  scheduleMetadata.Var5;



			scheduleInstance.recurrence = scheduleMetadata.recurrence;

			switch (scheduleMetadata.scheduleType) {
			case "One Time":
				scheduleInstance.scheduleType = ScheduleType.OneTime;
				break;

			case "Recurrent":
				scheduleInstance.scheduleType = ScheduleType.Recurrent;
				break;
			}

			if(scheduleMetadata.recurrencyType != null) {

                switch (scheduleMetadata.recurrencyType) {
                    case "Year":
                        scheduleInstance.recurrenceType = RecurrenceType.Year;
                        break;
                    case "Month":
                        scheduleInstance.recurrenceType = RecurrenceType.Month;
                        break;
                    case "Weekday":
                        scheduleInstance.recurrenceType = RecurrenceType.Weekday;
                        break;
                    case "Day":
                        scheduleInstance.recurrenceType = RecurrenceType.Day;
                        break;
                    case "Hour":
                        scheduleInstance.recurrenceType = RecurrenceType.Hour;
                        break;
                    case "Minute":
                        scheduleInstance.recurrenceType = RecurrenceType.Minute;
                        break;
                }

            }
            else{
                scheduleInstance.recurrenceType = RecurrenceType.Minute;
            }

		}

		return scheduleInstance;
	}

	/**
	 * Get list of next schedulable Task(s) / Task Flow(s)
	 * 
	 * @throws Exception
	 **/
	public static List<Schedulable> getSchedulableQueue() throws Exception {

	    List<Schedulable> schedulableQueue = new Vector<Schedulable>();
		List<Schedulable> resultSchedulableQueue = new Vector<Schedulable>();
		Metadata metadata = getMetadataInstance();


        Date currentDate;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");


       for(ScheduleMetadata   scheduleMetadata : getMetadataInstance().scheduleMap.values())
       {

           Schedulable schedulable = null;

           if (scheduleMetadata.taskFlowId != null)
           {
               schedulable = getTaskFlow(scheduleMetadata.taskFlowId, getSchedule(scheduleMetadata.id));
               log.info("scheduleMetadata.taskFlowId =" + scheduleMetadata.taskFlowId);

           }
           else if (scheduleMetadata.taskId != null){

               schedulable = getTask(scheduleMetadata.taskId ,  getSchedule(scheduleMetadata.id)  );
               log.info("scheduleMetadata.taskId  =" + scheduleMetadata.taskId );


           }

           if (schedulable != null)
               schedulableQueue.add(schedulable);

       }



        for (Schedulable schedulable : schedulableQueue) {

            if (schedulable.getSchedule() != null) {

                Date lastScheduledDate = schedulable.getLastScheduleDate();
                Calendar lastCalendarScheduledDate = null;
                Calendar currentCalendarDate = Calendar.getInstance();
                Calendar scheduleCalendarDate = Calendar.getInstance();


                Date scheduleDate = schedulable.getSchedule().getNextScheduleDate(lastScheduledDate);

                if (lastScheduledDate != null) {
                    lastCalendarScheduledDate = Calendar.getInstance();

                    lastCalendarScheduledDate.setTime(lastScheduledDate);
                }


                if (scheduleDate != null)
                    scheduleCalendarDate.setTime(scheduleDate);
                else
                    schedulable.storeLastScheduleDate();


                currentCalendarDate.setTime(new Date());

                log.info(schedulable.getSchedulableName() + "  Curr=" + formatDate(sdf, currentCalendarDate.getTime()) + ", Last="
                        + formatDate(sdf, lastScheduledDate) + ",  Next=" + formatDate(sdf, scheduleDate));


                if (scheduleDate != null) {
                    if (currentCalendarDate.equals(scheduleCalendarDate)
                            || currentCalendarDate.after(scheduleCalendarDate)) {
                        log.debug(schedulable.getSchedulableName() + " will be executed");

                        resultSchedulableQueue.add(schedulable);
                    } else {
                        log.debug(schedulable.getSchedulableName() + " will not be executed - next execution scheduled to "
                                + formatDate(sdf, scheduleDate));
                    }
                }
            }
        }


		return resultSchedulableQueue;
	}





    /**
     * //TODO: If work to delete...!!!! VERY IMPORTANT
     * Get list of next schedulable Task(s) / Task Flow(s)
     *
     * @throws Exception

    public static List<Schedulable> getSchedulableQueue1() throws Exception {
        List<Schedulable> schedulableQueue = new Vector<Schedulable>();
        List<Schedulable> resultSchedulableQueue = new Vector<Schedulable>();
        Metadata metadata = getMetadataInstance();



        Date currentDate;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");

        for (TaskMetadata taskMeta : metadata.taskMap.values()) {

            if (taskMeta.scheduleId != null || searchTaskInScheduleMetadataByTaskId(taskMeta.id) !=null) {

                Schedulable taskSchedulable = getTask(taskMeta.id, null);

                log.info("taskMeta.id=" + taskMeta.id);

                schedulableQueue.add(taskSchedulable);
            }

        }

        for (TaskFlowMetadata taskFlowMeta : metadata.taskFlowMap.values()) {

            if (taskFlowMeta.scheduleId != null || searchTaskFlowInScheduleMetadataByTaskFlowId(taskFlowMeta.id) != null ) {

                Schedulable taskFlowSchedulable = getTaskFlow(taskFlowMeta.id);

                log.info("taskFlowMeta.id=" + taskFlowMeta.id);
                schedulableQueue.add(taskFlowSchedulable);
            }
        }


        for (Schedulable schedulable : schedulableQueue) {

            if (schedulable.getSchedule() != null) {

                Date lastScheduledDate = schedulable.getLastScheduleDate();
                Calendar lastCalendarScheduledDate = null;
                Calendar currentCalendarDate = Calendar.getInstance();
                Calendar scheduleCalendarDate = Calendar.getInstance();


                Date scheduleDate = schedulable.getSchedule().getNextScheduleDate(lastScheduledDate);

                if (lastScheduledDate != null) {
                    lastCalendarScheduledDate = Calendar.getInstance();

                    lastCalendarScheduledDate.setTime(lastScheduledDate);
                }


                if (scheduleDate != null)
                    scheduleCalendarDate.setTime(scheduleDate);
                else
                    schedulable.storeLastScheduleDate();


                currentCalendarDate.setTime(new Date());

                log.info(schedulable.getSchedulableName() + "  Curr=" + formatDate(sdf, currentCalendarDate.getTime()) + ", Last="
                        + formatDate(sdf, lastScheduledDate) + ",  Next=" + formatDate(sdf, scheduleDate));


                if (scheduleDate != null) {
                    if (currentCalendarDate.equals(scheduleCalendarDate)
                            || currentCalendarDate.after(scheduleCalendarDate)) {
                        log.debug(schedulable.getSchedulableName() + " will be executed");

                        resultSchedulableQueue.add(schedulable);
                    } else {
                        log.debug(schedulable.getSchedulableName() + " will not be executed - next execution scheduled to "
                                + formatDate(sdf, scheduleDate));
                    }
                }
            }
        }

        return resultSchedulableQueue;
    }


     **/



	private static String formatDate(SimpleDateFormat sdf, Date dt) {
		if (dt == null) {
			return "n/a";
		} else {
			return sdf.format(dt);
		}
	}

	public static void saveSchedulableQueue(List<Schedulable> schedulableQueue) throws Exception {
		for (Schedulable schedulable : schedulableQueue) {
			schedulable.storeLastScheduleDate();
		}
	}

	public static String findTaskId(String taskId, String taskName) throws Exception {
		String resultTaskId = null;

		for (TaskMetadata taskMetadata : getMetadataInstance().taskMap.values()) {
			if (taskId != null) {
				if (taskId.length() == 15 && taskMetadata.id.startsWith(taskId)) {
					resultTaskId = taskMetadata.id;
					break;
				}

				if (taskId.length() == 18 && taskMetadata.id.equals(taskId)) {
					resultTaskId = taskMetadata.id;
					break;
				}
			}

			if (taskName != null) {
				if (taskMetadata.taskName.equalsIgnoreCase(taskName)) {
					resultTaskId = taskMetadata.id;
					break;
				}
			}
		}

		return resultTaskId;
	}

	public static String findTaskFlowId(String taskFlowId, String taskFlowName) {
		String resultTaskFlowId = null;

		for (TaskFlowMetadata taskFlowMetadata : getMetadataInstance().taskFlowMap.values()) {
			if (taskFlowId != null) {
				if (taskFlowId.length() == 15 && taskFlowMetadata.id.startsWith(taskFlowId)) {
					resultTaskFlowId = taskFlowMetadata.id;
					break;
				}

				if (taskFlowId.length() == 18 && taskFlowMetadata.id.equals(taskFlowId)) {
					resultTaskFlowId = taskFlowMetadata.id;
					break;
				}
			}

			if (taskFlowName != null) {
				if (taskFlowMetadata.flowName.equalsIgnoreCase(taskFlowName)) {
					resultTaskFlowId = taskFlowMetadata.id;
					break;
				}
			}
		}

		return resultTaskFlowId;
	}
}
