package com.vrs.sip.connection.drivers;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.vrs.sip.FileLog;
import com.vrs.sip.connection.*;
import com.vrs.sip.task.Schedule;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class CSVStatement implements IStatement {
	private final char DEFAULT_CSV_FIELD_SEPARATOR = ',';
	
	FileLog log;
	
	IConnection connection;

	Schedule schedule;
	
	char fieldSeparator = 0;
	
	@Override
	public void setConnection(IConnection connection) throws Exception {
		this.connection = connection;
		
		setLog(connection.getLog());
	}

	@Override
	public void setLog(FileLog log) {
		this.log = log;
	}
	
	@Override
	public FileLog getLog() {
		return log;
	}
	
	/**
	 * Open a CSV file for Reading Records.
	 * 
	 */
	@Override
	public IResultSet executeQuery(String csvFilename) throws Exception {
		IConnectionAttributes connectionAttributes = connection.getConnectionAttributes();
		String charset = connectionAttributes.getCharset();
		String directory = connectionAttributes.getDirectory();
		InputStreamReader isr;
		CSVReader reader;
		String filename;
		
		
		if (directory != null && directory.trim().equals("") == false) {
			filename = directory + "/" + csvFilename;
		} else {
			filename = csvFilename;
		}
		
		if (charset != null) {
			isr = new InputStreamReader(new FileInputStream(filename), charset);
		} else {
			isr = new InputStreamReader(new FileInputStream(filename));
		}
		
		reader = new CSVReader(isr);
		
		CSVResultSet resultSet = new CSVResultSet();
		
		resultSet.setStatement(this);
		resultSet.setCSVReader(reader);
		
		return resultSet;
	}

	@Override
	public Integer executeOperation(StatementOperationType operationType, Integer batchSize, String csvFilename, List<String> csvColumns, List<Record> rowList, List<String> keyFieldList) throws Exception {
		IConnectionAttributes connectionAttributes = connection.getConnectionAttributes();
		String charset = connectionAttributes.getCharset();
		String directory = connectionAttributes.getDirectory();
		String dateFormat = connectionAttributes.getDateFormat();
		String customDateFormat = connectionAttributes.getCustomDateFormat();
		String filename;
		
		Integer totalRecordsInError = 0;
		Integer totalRecords = 0;
		File f;
		Boolean isAppend = false;
		
		if (directory != null && directory.trim().equals("") == false) {
			filename = directory + "/" + csvFilename;
		} else {
			filename = csvFilename;
		}
		
		f = new File(filename);
		
		if (f.exists() && f.isDirectory() == false) {
			isAppend = true;
		}
		
		OutputStreamWriter osw;
		CSVWriter writer;
		List<String> header = new Vector<String>();
		
		if (charset != null) {
			osw = new OutputStreamWriter(new FileOutputStream(filename, isAppend), charset);
		} else {
			osw = new OutputStreamWriter(new FileOutputStream(filename, isAppend)); 
		}
		
		writer = new CSVWriter(osw, getFieldSeparator());
		
		List<String> entries = new Vector<String>();
		String[] entriesArray;
		SimpleDateFormat sdf = null;
		
		if (dateFormat != null) {
			sdf = new SimpleDateFormat(dateFormat);
		} else if (customDateFormat != null) {
			sdf = new SimpleDateFormat(customDateFormat);
		}
		
		for (String fieldName : csvColumns) {
			entries.add(fieldName);
			header.add(fieldName);
		}
		
		// Write Header
		if (isAppend == false) {
			entriesArray = new String[entries.size()];
			
			for (Integer i = 0; i < entries.size(); i++) {
				entriesArray[i] = entries.get(i);
			}
		
			writer.writeNext(entriesArray);
		}
		
		if (rowList != null) {
			for (Record row : rowList) {
				entries.clear();
				
				for (String fieldName : header) {
					Field field = row.getFieldByName(fieldName);
					FieldType fieldType = field.getFieldType();
					String value;
					
					if (fieldType == FieldType.T_DATE) {
						Date dtValue = (Date)field.getValue();
						
						if (dtValue != null) {
							if (sdf != null) {
								value = sdf.format(dtValue);
							} else {
								value = dtValue.toString();
							}
						} else {
							value = null;
						}
					} else if (fieldType == FieldType.T_BOOLEAN) {
						Boolean boolValue = (Boolean)field.getValue();
						
						if (boolValue != null) {
							value = boolValue ? "true" : "false";
						} else {
							value = null;
						}
					} else if (fieldType == FieldType.T_DECIMAL){
						value =  String.valueOf(field.getValue() ==null ?"": field.getValue());
					}else {
						value = (String)field.getValue();
					}
					
					entries.add(value);
				}
				
				entriesArray = new String[entries.size()];
				
				for (Integer i = 0; i < entries.size(); i++) {
					entriesArray[i] = entries.get(i);
				}
				
				writer.writeNext(entriesArray);
				
				totalRecords++;
			}
		}
		
		writer.close();
		
		return totalRecordsInError;
	}

	@Override
	public IResultSet fetchRecords(String entity, String filterClause) throws Exception {
		throw new RuntimeException("The method fetchRecords is not supported by " + this.getClass().getName());
	}

	@Override
	public Integer executeTruncate(String entity) throws Exception {
		IConnectionAttributes connectionAttributes = connection.getConnectionAttributes();
		String directory = connectionAttributes.getDirectory();
		Integer result = 0;
		String filename;
		
		if (directory != null && directory.trim().equals("") == false) {
			filename = directory + "/" + entity;
		} else {
			filename = entity;
		}
		
		File file = new File(filename);
		
		if (file.delete()) {
			result = 1;
		}
		
		return result;
	}

	@Override
	public void close() throws Exception {
	}

	@Override
	public void executeCall(String call, List<Object> inputParameterList) throws Exception {
		throw new RuntimeException("The method executeCall is not supported by " + this.getClass().getName());
	}

	@Override
	public void executeFileUpload(String parentObject, String parentId, Boolean parentIdIsExternal, String externalIdField, String filename, String name, String description, String contentType, Boolean isPrivate) throws Exception {
		throw new RuntimeException("File Upload not implemented by " + this.getClass().getName());
	}

	@Override
	public void setFieldSeparator(char fieldSeparator) {
		this.fieldSeparator = fieldSeparator;
		
		if (this.fieldSeparator == 0) {
			this.fieldSeparator = DEFAULT_CSV_FIELD_SEPARATOR;
		}
	}
	
	public char getFieldSeparator() {
		if (fieldSeparator == 0) {
			return DEFAULT_CSV_FIELD_SEPARATOR;
		} else {
			return fieldSeparator;
		}
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
