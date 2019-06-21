/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Oracle Implementation of a Statement.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection.drivers;

import com.vrs.sip.FileLog;
import com.vrs.sip.connection.*;
import com.vrs.sip.task.Schedule;
import oracle.jdbc.OraclePreparedStatement;

import java.sql.*;
import java.util.List;

public class OracleStatement implements IStatement {
	FileLog log;
	
	IConnection connection;

	Connection implConnection;

	Schedule schedule;
	
	// Have a big default.
	int batchSize = 65535;
	
	@Override
	public void setLog(FileLog log) {
		this.log = log;
	}
	
	@Override
	public FileLog getLog() {
		if (log == null) {
			log = new FileLog();
		}
		
		return log;
	}
	
	@Override
	public void setConnection(IConnection connection) throws Exception {
		OracleConnection oracleConnection = (OracleConnection)connection;
		Integer connectionBatchSize = null;
		
		this.connection = connection;
		
		setLog(connection.getLog());
		
		implConnection = oracleConnection.getOracleJdbcConnection();
		
		connectionBatchSize = oracleConnection.getConnectionAttributes().getBatchSize();
		
		if (connectionBatchSize != null && connectionBatchSize > 0) {
			batchSize = connectionBatchSize;
		}
	}
	
	@Override
	public IResultSet executeQuery(String sql) throws Exception {
		Statement statement;
		ResultSet resultSet;
		
		OracleResultSet oracleResultSet;
		
		getLog().debug("executeQuery START");
		
		statement = implConnection.createStatement();
		
		resultSet = statement.executeQuery(sql);

		oracleResultSet = new OracleResultSet();
		
		oracleResultSet.setQuery(sql);
		oracleResultSet.setStatement(this);
		oracleResultSet.setJdbcResultSet(resultSet);
		
		return oracleResultSet;
	}

	@Override
	public Integer executeOperation(StatementOperationType operationType, Integer taskStepBatchSize, String entityName, List<String> entityFieldNameList, List<Record> recordList, List<String> keyFieldList) throws Exception {
		PreparedStatement preparedStatement;
		Integer recordCount;
		Integer failRecordCount;
		int operationBatchSize;
		@SuppressWarnings("unused")
		int batchNo;
		int batchOffset;
		
		getLog().debug("executeUpdate START");
		
		if (taskStepBatchSize != null && taskStepBatchSize > 0) {
			this.batchSize = taskStepBatchSize;
		}
		
		recordCount = 0;
		failRecordCount = 0;
		
		if (recordList == null || recordList.isEmpty()) {
			return recordCount;
		}
		
		preparedStatement = implConnection.prepareStatement(
			getOperationStatement(operationType, entityName, entityFieldNameList, keyFieldList)
		);
		
		batchOffset = 0;
		batchNo = 0;
		while (batchOffset < recordList.size()) {
			int operationIndex;
			SQLWarning sqlWarning = null;
			
			batchNo++;
			
			operationBatchSize = recordList.size() - batchOffset;
			
			if (operationBatchSize > batchSize) {
				operationBatchSize = batchSize;
			}
			
			//System.out.println("operationBatchSize=" + operationBatchSize);
			
			((OraclePreparedStatement)preparedStatement).setExecuteBatch(operationBatchSize);
			
			operationIndex = 0;
			for (int recordOffset = batchOffset; operationIndex < operationBatchSize; recordOffset++, operationIndex++) {
				Record record = recordList.get(recordOffset);
				int fieldCount = 1;
				for (String fieldName : entityFieldNameList) {
					Field field = record.getFieldByName(fieldName);
					
					//System.out.println("FieldName=" + fieldName + ", FieldIndex=" + fieldCount + ", FieldValue=" + field.getValue());
					
					setPreparedStatementField(preparedStatement, fieldCount, field);
					
					fieldCount++;
				}
	
				recordCount += preparedStatement.executeUpdate();
				
				//System.out.println("recordOffset=" + recordOffset + ", recordCount=" + recordCount);
				
				sqlWarning = preparedStatement.getWarnings();

				while (sqlWarning != null) {
					int errorCode = sqlWarning.getErrorCode();
					String errorMessage = sqlWarning.getMessage();
					
					getLog().info("SQL Warning Found: errorCode=" + errorCode + ", errorMessage=" + errorMessage);
					
					sqlWarning = sqlWarning.getNextWarning();
				}
			}
			
			batchOffset += operationBatchSize;
		
			if (operationBatchSize > recordCount) {
				failRecordCount += (operationBatchSize - recordCount);
			}
			
			((OraclePreparedStatement)preparedStatement).sendBatch();
			
			sqlWarning = ((OraclePreparedStatement)preparedStatement).getWarnings();
			
			while (sqlWarning != null) {
				int errorCode = sqlWarning.getErrorCode();
				String errorMessage = sqlWarning.getMessage();
				
				getLog().info("SQL Warnings Found: errorCode=" + errorCode + ", errorMessage=" + errorMessage);
				
				sqlWarning = sqlWarning.getNextWarning();
			}

			if (implConnection.getAutoCommit() == false) {
				implConnection.commit();
			}
		}
		
		return failRecordCount;
	}
	
	private String getOperationStatement(StatementOperationType operationType, String entityName, List<String> entityFieldList, List<String> keyFieldList) {
		Integer counter = 0;
		String strStatement = "";
		
		switch (operationType) {
			case Insert:
				strStatement = "INSERT INTO " + entityName + " (" + String.join(", ", entityFieldList) + ") VALUES (";
				
				for (counter = 1; counter <= entityFieldList.size(); counter++) {
					
					if (counter > 1) {
						strStatement += ", ";
					}
					
					strStatement += "?";
				}
				
				strStatement += ")";
				
				break;
				
			case Delete:
				strStatement = "DELETE FROM " + entityName + " WHERE ";
				
				counter = 1;
				for (String keyField : keyFieldList) {
					if (counter > 1) {
						strStatement += " AND ";
					}
					
					strStatement += keyField + " = ?";
					
					counter++;
				}
				
				break;
				
			case Update:
				strStatement = "UPDATE " + entityName + " SET ";
				
				counter = 1;
				for (String entityField : entityFieldList) {
					if (counter > 1) {
						strStatement += ", ";
					}
					
					strStatement += entityField + " = ?";
					
					counter++;
				}
				
				strStatement += " WHERE ";
				
				counter = 1;
				for (String keyField : keyFieldList) {
					if (counter > 1) {
						strStatement += " AND ";
					}
					
					strStatement += keyField + " = ?";
					
					counter++;
				}
				
				break;
				
			case Upsert:
				throw new RuntimeException("The operation Upsert is not available for " + this.getClass().getName());
		}
		
		//System.out.println("entityFieldSet=" + entityFieldSet + " - " + strStatement);
		
		return strStatement;
	}

	@Override
	public void executeCall(String call, List<Object> inputParameterList) throws Exception {
		Integer totalArgs = 0;
		String callArgs = "";
		CallableStatement callableStatement;
		
		getLog().debug("executeCall START");
		
		if (inputParameterList != null) {
			totalArgs = inputParameterList.size();
		}
		
		for (Integer i = 0; i < totalArgs; i++) {
			if (i > 0) {
				callArgs += ", ";
			}
			callArgs += "?";
		}
		
		callableStatement = implConnection.prepareCall("{call " + call + "(" + callArgs + ")}");
		
		if (totalArgs > 0) {
			Integer parameterIndex = 0;
			for (Object parameter : inputParameterList) {
				Object overrideParameter = null;
				
				parameterIndex++;
				
				if (parameter instanceof java.util.Date) {
					// hack to convert to java.sql.Date
					java.util.Date javaDate = (java.util.Date)parameter;
					
					if (parameter != null) {
						overrideParameter = new java.sql.Timestamp(javaDate.getTime());
					}
				}
				
				log.debug(
						"executeCall - parameterIndex=" + parameterIndex +
						" type " + (
								overrideParameter != null
									? overrideParameter.getClass().getName()
									: parameter != null ? parameter.getClass().getName() : "null"
						)
				);
				
				callableStatement.setObject(parameterIndex, overrideParameter != null ? overrideParameter : parameter);
			}
		}
		
		callableStatement.executeQuery();
		
		getLog().debug("executeCall END");
	}

	@Override
	public IResultSet fetchRecords(String entity, String filterClause) throws Exception {
		Statement statement;
		ResultSet resultSet;
		String sql;
		
		OracleResultSet oracleResultSet;
		
		getLog().debug("fetchRecords START");
		
		sql = "SELECT * FROM " + entity;
		
		if (filterClause != null && filterClause.trim().length() > 0) {
			sql += " WHERE " + filterClause;
		}
		
		statement = implConnection.createStatement();
		
		resultSet = statement.executeQuery(sql);

		oracleResultSet = new OracleResultSet();
		
		oracleResultSet.setQuery(sql);
		oracleResultSet.setStatement(this);
		oracleResultSet.setJdbcResultSet(resultSet);
		
		return oracleResultSet;
	}

	@Override
	public Integer executeTruncate(String entity) throws Exception {
		Statement statement = implConnection.createStatement();
		statement.executeUpdate("TRUNCATE TABLE " + entity);
		
		return 0;
	}

	@Override
	public void close() throws Exception {
	}
	
	private void setPreparedStatementField(PreparedStatement preparedStatement, Integer index, Field field) throws SQLException {
		if (field.getValue() == null) {
			preparedStatement.setNull(index, field.getFieldType().getSQLType());
		} else {
			Integer fieldSQLType = field.getFieldType().getSQLType();
			
			if (fieldSQLType == java.sql.Types.INTEGER) {
				preparedStatement.setInt(index, (int)field.getValue());
			} else if (fieldSQLType == java.sql.Types.DECIMAL) {
				preparedStatement.setDouble(index, (double)field.getValue());
			} else if (fieldSQLType == java.sql.Types.VARCHAR) {
				preparedStatement.setString(index, (String)field.getValue());
			} else if (fieldSQLType == java.sql.Types.DATE) {
				java.util.Date valueDate = (java.util.Date)field.getValue();
				java.sql.Date sqlDate = null;
				
				if (valueDate != null) {
					sqlDate = new java.sql.Date(valueDate.getTime());
				}

				preparedStatement.setDate(index, sqlDate);
			} else if (fieldSQLType == java.sql.Types.BOOLEAN) {
				Boolean valueBoolean = (Boolean)field.getValue();
				
				preparedStatement.setBoolean(index, valueBoolean);
			} else {
				throw new RuntimeException("Error setting field with index " + index + " for prepared statement with field type " + fieldSQLType);
			}
		}
	}

	@Override
	public void executeFileUpload(String parentObject, String parentId, Boolean parentIdIsExternal, String externalIdField, String filename, String name, String description, String contentType, Boolean isPrivate) throws Exception {
		throw new RuntimeException("File Upload not implemented by " + this.getClass().getName());
	}

	@Override
	public void setFieldSeparator(char fieldSeparator) throws Exception {
	}

    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
}
