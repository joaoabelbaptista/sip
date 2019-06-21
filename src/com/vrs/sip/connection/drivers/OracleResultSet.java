/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Oracle Implementation of a Result Set.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection.drivers;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.vrs.sip.FileLog;
import com.vrs.sip.connection.Field;
import com.vrs.sip.connection.FieldType;
import com.vrs.sip.connection.IResultSet;
import com.vrs.sip.connection.IStatement;
import com.vrs.sip.connection.Record;

public class OracleResultSet implements IResultSet {
	FileLog log;
	
	String query;
	OracleStatement oracleStatement;
	Integer batchSize;
	ResultSet resultSet;
	
	Map<Integer,FieldType> fieldTypeMap;
	
	public OracleResultSet() {
		// Set the default batch size
		batchSize = 200;
		
		fieldTypeMap = new HashMap<Integer,FieldType>();
		
		fieldTypeMap.put(java.sql.Types.BIGINT, FieldType.T_INTEGER);
		fieldTypeMap.put(java.sql.Types.BOOLEAN, FieldType.T_INTEGER);
		fieldTypeMap.put(java.sql.Types.CHAR, FieldType.T_STRING);
		fieldTypeMap.put(java.sql.Types.DATE, FieldType.T_DATE);
		fieldTypeMap.put(java.sql.Types.TIMESTAMP, FieldType.T_DATE);
		fieldTypeMap.put(java.sql.Types.DECIMAL, FieldType.T_DECIMAL);
		fieldTypeMap.put(java.sql.Types.DOUBLE, FieldType.T_DECIMAL);
		fieldTypeMap.put(java.sql.Types.FLOAT, FieldType.T_DECIMAL);
		fieldTypeMap.put(java.sql.Types.INTEGER, FieldType.T_INTEGER);
		fieldTypeMap.put(java.sql.Types.LONGVARCHAR, FieldType.T_STRING);
		fieldTypeMap.put(java.sql.Types.NUMERIC, FieldType.T_DECIMAL);
		fieldTypeMap.put(java.sql.Types.SMALLINT, FieldType.T_INTEGER);
		fieldTypeMap.put(java.sql.Types.VARCHAR, FieldType.T_STRING);
		fieldTypeMap.put(java.sql.Types.NVARCHAR, FieldType.T_STRING);
		fieldTypeMap.put(java.sql.Types.NCLOB, FieldType.T_STRING);
		fieldTypeMap.put(java.sql.Types.CLOB, FieldType.T_STRING);
	}
	
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
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getQuery() {
		return query;
	}
	
	@Override
	public void setStatement(IStatement statement) throws Exception {
		Integer connectionBatchSize;
		
		oracleStatement = (OracleStatement)statement;
		
		setLog(statement.getLog());
		
		connectionBatchSize = oracleStatement.connection.getConnectionAttributes().getBatchSize();
		
		if (connectionBatchSize != null && connectionBatchSize > 0) {
			setBatchSize(connectionBatchSize);
		}
	}
	
	@Override
	public List<Record> fetchRows() throws Exception {
		List<Record> rowList = new Vector<Record>();
		Integer counter = 0;
		ResultSetMetaData rsMetadata;
		
		getLog().debug("fetchRows START");
		
		rsMetadata = null;
		while (counter < batchSize && resultSet.next() == true) {
			List<Field> fieldList = new Vector<Field>();
			Record record = new Record(fieldList);
			
			if (rsMetadata == null) {
				rsMetadata = resultSet.getMetaData();
			}
			
			for (Integer i = 1; i <= rsMetadata.getColumnCount(); i++) {
				String fieldName;
				FieldType fieldType;
				Object fieldValue;
				
				fieldName = rsMetadata.getColumnName(i);
				
				//getLog().debug("FieldName=" + fieldName);
				
				fieldType = getFieldType(resultSet, rsMetadata, i);
				
				//getLog().debug("FieldType=" + fieldType);
				
				fieldValue = getFieldValue(resultSet, rsMetadata, i);
				
				//getLog().debug("FieldValue=" + fieldValue);
				
				Field field = new Field(fieldName, fieldType, fieldValue);
				
				fieldList.add(field);
				
				record.setFieldList(fieldList);
			}
			
			rowList.add(record);
			
			counter++;
		}
		
		return rowList;
	}
	
	@Override
	public List<Record> fetchAllRows() throws Exception {
		List<Record> rowList = new Vector<Record>();
		List<Record> batchRowList;
		
		do {
			batchRowList = fetchRows();
			
			if (batchRowList != null && batchRowList.isEmpty() == false) {
				rowList.addAll(batchRowList);
			}
		} while (batchRowList != null && batchRowList.isEmpty() == false);
		
		return rowList;
	}
	
	private Object getFieldValue(ResultSet resultSet, ResultSetMetaData rsMetadata, Integer i) throws SQLException {
		Object result = null;
		FieldType fieldType = getFieldType(resultSet, rsMetadata, i);
		
		if (fieldType == FieldType.T_DATE) {
			result = resultSet.getDate(i);
		} else if (fieldType == FieldType.T_DECIMAL) {
			result = resultSet.getDouble(i);
		} else if (fieldType == FieldType.T_INTEGER) {
			result = resultSet.getLong(i);
		} else if (fieldType == FieldType.T_STRING) {
			result = resultSet.getString(i);
		} else {
			String fieldName = rsMetadata.getColumnName(i);
			String fieldTypeAtIndex = rsMetadata.getColumnTypeName(i);
			int fieldTypeValueAtIndex = rsMetadata.getColumnType(i);

			throw new RuntimeException("Query " + query + ", Database Column " + fieldName + " of SQL Type " + fieldTypeAtIndex + " (" + fieldTypeValueAtIndex + ") not mapped (" + fieldType + ")");
		}
		
		return result;
	}

	private FieldType getFieldType(ResultSet resultSet, ResultSetMetaData rsMetadata, Integer i) throws SQLException {
		Integer sqlFieldType;
		FieldType fieldType;
		
		sqlFieldType = rsMetadata.getColumnType(i);
		
		//getLog().debug("SQLFieldType=" + sqlFieldType);
		
		//System.out.println("SQLFieldType=" + sqlFieldType);
		
		fieldType = fieldTypeMap.get(sqlFieldType);
		
		return fieldType;
	}

	@Override
	public void setBatchSize(Integer batchSize) throws Exception {
		getLog().debug("setBatchSize START");
		
		this.batchSize = batchSize;
	}
	
	public void setJdbcResultSet(ResultSet resultSet) {
		getLog().debug("setJdbcResultSet START");
		
		this.resultSet = resultSet;
	}
}
