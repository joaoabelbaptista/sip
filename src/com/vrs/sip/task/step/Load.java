/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Load Task Step Implementation.
 * History: aosantos, 2016-06-26, Initial Release.
 *          aosantos, 2016-07-19, Updated Call behavior to the following:
 *                                  - If Operation is Call and no input records
 *                                    then run once the Call operation.
 *          aosantos, 2016-07-20, Fixed Call behavior, was not working as expected. 
 * 
 */
package com.vrs.sip.task.step;

import com.vrs.sip.Configuration;
import com.vrs.sip.FileLog;
import com.vrs.sip.Logging;
import com.vrs.sip.Util;
import com.vrs.sip.connection.*;
import com.vrs.sip.task.*;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Load extends AbstractTaskStep {
	FileLog log;
	
	IStatement statement;
	IResultSet resultSet;
	String taskStepType;
	String parentId;

	
	String childExecutionId;






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
		List<DataRecord> sourceRecords;
		IConnection outConnection = getTargetConnection();
		Entity outEntity = getTargetEntity();
		Integer totalRecords;
		Integer totalFailedRecords;
		JexlEngine jexl = TransformationEngine.getJexl();
		
		List<Object> callParameterList = new Vector<Object>();
		Map<String,Object> callParameterMap = new HashMap<String,Object>();
		List<JexlContext> inputContexes = new Vector<JexlContext>();
		List<Map<String,Object>> contextList = new Vector<Map<String,Object>>();
		List<Record> outputRecords = new Vector<Record>();
		
		outConnection.setLog(log);
		
		info("Load Batch Execute START");
		
		parentId = getParentExecutionId();
		
		if (childExecutionId == null) {
			if (parentId != null) {
				childExecutionId = Logging.prepareExecution(parentId, null, this);
			}
		}
		
		sourceRecords = getInputBatch();
		
		info("Total Source Records: " + (sourceRecords != null ? sourceRecords.size() : 0));


		/* TODO: Validate this code ...Dont Forget!!!!
        if (outEntity == null) {
            info("Source Records is Empty, setting Completed to TRUE");

            setCompleted(true);

            return;
        }
        */
		
		setTotalInRecords(getTotalInRecords() + sourceRecords.size());
		
		if (statement == null) {
			outConnection.openConnection();
			
			statement = outConnection.createStatement();


			statement.setSchedule(this.getSchedule());

			
			statement.setFieldSeparator(getFieldSeparator());
			
			if (getOperation() == AbstractTaskStep.OperationType.Insert) {
				if (getTruncateTarget()) {
					statement.executeTruncate(outEntity.entityName);
				}
			}
		}
			
		if (sourceRecords == null || sourceRecords.isEmpty()) {
			// Generate Header (in case of CSV output)
			totalFailedRecords = statement.executeOperation(
					getStatementOperationType(getOperation()),
					getBatchSize(),
					outEntity.entityName,
					outEntity.fieldNameList,
					null,
					getOperationKeyFieldList()
			);			
		}
		
		totalFailedRecords = 0;
		
		for (DataRecord rec : sourceRecords) {
			Record record;
			List<Field> fieldList = new Vector<Field>();
			
			for (String fieldName : rec.getFields()) {
				Object value = rec.getFieldValue(fieldName);
				Field outputField = new Field(fieldName, FieldType.T_STRING, value);
				
				//System.out.println("Source Record Field " + fieldName + " with " + value);
				
				fieldList.add(outputField);
			}
			
			record = new Record(fieldList);
            record.setIdOne(rec.getIdOne());
            record.setHasError(rec.getHasError());
			
			outputRecords.add(record);
		}
		
		if (Configuration.getInstance().getLog().pipeline == true) {
			for (Record sourceRecord : outputRecords) {
				info("Load Record: " + sourceRecord);
			}
		}


		
		if (
				getOperation() == AbstractTaskStep.OperationType.Insert
				||
				getOperation() == AbstractTaskStep.OperationType.Update
				||
				getOperation() == AbstractTaskStep.OperationType.Upsert
				||
				getOperation() == AbstractTaskStep.OperationType.Delete
                ||
                getOperation() == AbstractTaskStep.OperationType.InsertWithFileAttachment
                ||
                getOperation() == AbstractTaskStep.OperationType.SendEmailWithAttachment

		) {
			Integer recordIndex;
			
			for (Record record : outputRecords) {
				JexlContext context = new MapContext();
				Map<String,Object> contextMap = new HashMap<String,Object>();
				
				for (String fieldName : record.getFieldNameSet()) {
					Object fieldValue = record.getFieldByName(fieldName).getValue();
					
					context.set(fieldName.toUpperCase(), fieldValue);
					
					contextMap.put(fieldName.toUpperCase(), fieldValue);
				}
				
				inputContexes.add(context);
				
				contextList.add(contextMap);
			}
			
			recordIndex = -1;
			for (JexlContext context : inputContexes) {
				Record record = null;
				Map<String,Object> contextMap = null;
				
				recordIndex++;
				
				if (recordIndex < outputRecords.size()) {
					record = outputRecords.get(recordIndex);
					
					contextMap = contextList.get(recordIndex);
				}
				
				for (Transformation transformation : transformationList) {
					getLog().debug("Creating JEXL expression " + transformation.transformation);
					
					JexlExpression expression = jexl.createExpression(transformation.transformation);

					EntityField outField = outEntity.getField(transformation.targetFieldName);
					Object value = null;
					FieldType fieldType = null;
					
					if (outField == null) {
						throw new RuntimeException("Cannot find field " + transformation.targetFieldName + " on Target Entity");
					}
					
					try {
						switch (outField.entityFieldType) {
							case Boolean:
								value = TransformationEngine.evaluateBoolean(expression, context);
								fieldType = FieldType.T_BOOLEAN;
								break;
								
								case Date:
								value = TransformationEngine.evaluateDate(expression, context);
								fieldType = FieldType.T_DATE;
								break;
								
							case Decimal:
								value = TransformationEngine.evaluateDouble(expression, context);
								fieldType = FieldType.T_DECIMAL;
								break;
								
							case Integer:
								value = TransformationEngine.evaluateInteger(expression, context);
								fieldType = FieldType.T_INTEGER;
								break;
								
							case String:
								value = TransformationEngine.evaluateString(expression, context);
								fieldType = FieldType.T_STRING;
								break;
						}
					} catch (Exception e) {
						throw new RuntimeException("Error in Transformation \'" + transformation.transformation + "\' for Target Field '" + transformation.targetFieldName + "': " + Util.getStackTraceString(e) + "\nContext:\n" + Util.getMapAsString(contextMap));
					}
				
					if (record != null) {
						record.setField(outField.entityFieldName, value, fieldType);
					}
				}
			}


			if (outputRecords != null && outputRecords.isEmpty() == false) {
				totalFailedRecords = statement.executeOperation(
						getStatementOperationType(getOperation()),
						getBatchSize(),
						outEntity.entityName,
						outEntity.fieldNameList,
						outputRecords,
						getOperationKeyFieldList()
				);
			}
		} else if (getOperation() == AbstractTaskStep.OperationType.ExecuteCall) {
			for (Record record : outputRecords) {
				JexlContext context = new MapContext();
				
				for (String fieldName : record.getFieldNameSet()) {
					Object fieldValue = record.getFieldByName(fieldName).getValue();
					
					context.set(fieldName.toUpperCase(), fieldValue);
				}
				
				inputContexes.add(context);
			}

			if (inputContexes.isEmpty() == true) {
				// Run once
				inputContexes.add(new MapContext());
			}
			
			for (JexlContext context : inputContexes) {
				// Transformation List is ordered by Callable Statement input variables
				
				callParameterList.clear();
				for (Transformation transformation : transformationList) {
					JexlExpression expression = jexl.createExpression(transformation.transformation);
					EntityField outField = outEntity.getField(transformation.targetFieldName);
					Object value = null;
					
					if (outField == null) {
						throw new RuntimeException("Cannot find field " + transformation.targetFieldName + " on Target Entity");
					}
					
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
					
					callParameterMap.put(outField.entityFieldName, value);
				}
				
				for (EntityField parameterField : outEntity.fieldList) {
					String fieldName = parameterField.entityFieldName;
					Object fieldValue = callParameterMap.get(fieldName);
					
					callParameterList.add(fieldValue);
				}
				
				log.info("Executing Call: " + outEntity.entityName);
				
				statement.executeCall(outEntity.entityName, callParameterList);
			}
		} else if (getOperation() == OperationType.MoveEmailsToFolder) {

            statement.executeCall(outEntity.entityName, Collections.singletonList(outputRecords));

        }
		else if (getOperation() == AbstractTaskStep.OperationType.FileAttach) {
			Integer recordIndex;
			
			for (Record record : outputRecords) {
				JexlContext context = new MapContext();
				
				for (String fieldName : record.getFieldNameSet()) {
					Object fieldValue = record.getFieldByName(fieldName).getValue();
					
					context.set(fieldName.toUpperCase(), fieldValue);
				}
				
				inputContexes.add(context);
			}
			
			recordIndex = -1;
			for (JexlContext context : inputContexes) {
				Record record;
				
				recordIndex++;
				
				record = outputRecords.get(recordIndex);
				
				for (Transformation transformation : transformationList) {
					JexlExpression expression = jexl.createExpression(transformation.transformation);
					EntityField outField = outEntity.getField(transformation.targetFieldName);
					Object value = null;
					FieldType fieldType = null;
					
					if (outField == null) {
						throw new RuntimeException("Cannot find field " + transformation.targetFieldName + " on Target Entity");
					}
					
					switch (outField.entityFieldType) {
						case Boolean:
							value = (Boolean)expression.evaluate(context);
							fieldType = FieldType.T_BOOLEAN;
							break;
							
						case Date:
							value = (Date)expression.evaluate(context);
							fieldType = FieldType.T_DATE;
							break;
							
						case Decimal:
							value = (Double)expression.evaluate(context);
							fieldType = FieldType.T_DECIMAL;
							break;
							
						case Integer:
							value = (Integer)expression.evaluate(context);
							fieldType = FieldType.T_INTEGER;
							break;
							
						case String:
							value = (String)expression.evaluate(context);
							fieldType = FieldType.T_STRING;
							break;
					}
					
					record.setField(outField.entityFieldName, value, fieldType);
				}
			}
			
			for (Record record : outputRecords) {
				debug("File Attach - Input: " + record);

				try {
					// Check if parameters are defined
					String directory = record.getFieldNameSet().contains("directory") ? record.getFieldByName("directory").getString() : null;
					String filename = record.getFieldNameSet().contains("filename") ? record.getFieldByName("filename").getString() : null;
					String extractIdPattern = record.getFieldNameSet().contains("extractIdPattern") ? record.getFieldByName("extractIdPattern").getString() : null;
					Boolean isExternalId = record.getFieldNameSet().contains("isExternalId") ? record.getFieldByName("isExternalId").getBoolean() : null;
					Boolean isPrivate = record.getFieldNameSet().contains("isPrivate") ? record.getFieldByName("isPrivate").getBoolean() : null;
					String contentType = record.getFieldNameSet().contains("contentType") ? record.getFieldByName("contentType").getString() : null;
					String name = record.getFieldNameSet().contains("name") ? record.getFieldByName("name").getString() : null;
					String description = record.getFieldNameSet().contains("description") ? record.getFieldByName("description").getString() : null;
					String externalIdField = record.getFieldNameSet().contains("externalIdField") ? record.getFieldByName("externalIdField").getString() : null;
					String attachmentObject = record.getFieldNameSet().contains("attachmentObject") ? record.getFieldByName("attachmentObject").getString() : null;
					
					String parentId;
					String externalId;
					
					Integer errorFieldCount = 0;
					String errorMessage = "";
					
					if (filename == null) {
						errorFieldCount++;
						
						if (errorFieldCount > 1) {
							errorMessage += ", ";
						}
						
						errorMessage += "filename";
					}
					
					if (extractIdPattern == null) {
						errorFieldCount++;
						
						if (errorFieldCount > 1) {
							errorMessage += ", ";
						}
						
						errorMessage += "extractIdPattern";
					}
					
					if (isExternalId == null) {
						errorFieldCount++;
						
						if (errorFieldCount > 1) {
							errorMessage += ", ";
						}
						
						errorMessage += "isExternalId";
					}
					
					if (isExternalId != null && isExternalId && externalIdField == null) {
						errorFieldCount++;
						
						if (errorFieldCount > 1) {
							errorMessage += ", ";
						}
						
						errorMessage += "externalIdField";							
					}
					
					if (isExternalId != null && isExternalId && attachmentObject == null) {
						errorFieldCount++;
						
						if (errorFieldCount > 1) {
							errorMessage += ", ";
						}
						
						errorMessage += "attachmentObject";
					}
					
					if (errorFieldCount > 0) {
						errorMessage = "File Attach Task Step :: The following field" + (errorFieldCount > 1 ? "s":"") + " are mandatory: " + errorMessage;
						
						throw new RuntimeException(errorMessage);
					} else {
						Pattern pattern = Pattern.compile(extractIdPattern);
						Matcher matcher = pattern.matcher(filename);

						parentId = null;
						if (matcher.matches()) {
							externalId = matcher.group(1);
							
							debug("externalId=" + externalId);
							
							parentId = externalId;
						}
						
						if (parentId != null) {
							statement.executeFileUpload(
									attachmentObject,
									parentId,
									isExternalId,
									externalIdField,
									(directory != null && directory.equals("") == false ? directory + "/" : "") + filename,
									name,
									description,
									contentType,
									isPrivate
							);
						} else {
							throw new RuntimeException("Error extracting extractId field using pattern " + extractIdPattern + " from filename " + filename);
						}
					}
				} catch (Exception e) {
					fatal(Util.getStackTraceString(e));
					
					totalFailedRecords++;
				}
			}
		}




		if (outConnection.getConnectionAttributes().hasAutoCommit()) {
			if (outConnection.getConnectionAttributes().getAutoCommit() == false) {
				info("Connection is not AutoCommit - invoking commit");
				outConnection.commit();
				info("Commit finished");
			}
		}
		
		totalRecords = outputRecords.size() - totalFailedRecords;
		
		if (totalFailedRecords > 0) {
			setTotalWarnRecords(getTotalWarnRecords() + totalFailedRecords);
		}
		
		info("Loaded " + totalRecords + " into target, " + totalFailedRecords + " failed.");
		
		setTotalOutRecords(getTotalOutRecords() + totalRecords);
		
		if (sourceRecords.isEmpty()) {
			info("Source Records is Empty, setting Completed to TRUE");
			
			setCompleted(true);
		}
		
		if (getNextStep() != null) {
			// Copy input records to next step
            List<DataRecord> newSourceRecords = new ArrayList<DataRecord>();

            for(Record outputRecord : outputRecords){

                if (outputRecord.getHasError()){

                    int indexOf = sourceRecords.indexOf(outputRecord);

                    log.info("indexOf = "+ indexOf);

                    if (indexOf >=0)
                        sourceRecords.get(indexOf).setHasError(true);
                }

            }

			getNextStep().setInputBatch(sourceRecords);
			getNextStep().setInputFilename(outEntity.entityName);
			getNextStep().setCompleted(false);
		}
		
		if (childExecutionId != null) {
			Logging.updateExecution(childExecutionId, Logging.ExecutionStatus.Running, getTotalOutRecords(), getTotalWarnRecords());
		}
		
		info("Load Batch Execute END");
	}
	
	private StatementOperationType getStatementOperationType(OperationType operationType) {

	    switch (operationType) {
			case Insert:
				return StatementOperationType.Insert;
				
			case Update:
				return StatementOperationType.Update;
				
			case Upsert:
				return StatementOperationType.Upsert;
				
			case Delete:
				return StatementOperationType.Delete;

            case InsertWithFileAttachment:
                return StatementOperationType.InsertWithAttachments;

			case SendEmailWithAttachment:
                return StatementOperationType.SendEmailWithAttachment;

            case MoveEmailsToFolder:
                return StatementOperationType.MoveEmailsToFolder;

			default:
				return null;
		}
	}
}
