package com.vrs.sip.connection.drivers;

import java.util.List;
import java.util.Vector;

import com.opencsv.CSVReader;
import com.vrs.sip.FileLog;
import com.vrs.sip.connection.Field;
import com.vrs.sip.connection.FieldType;
import com.vrs.sip.connection.IResultSet;
import com.vrs.sip.connection.IStatement;
import com.vrs.sip.connection.Record;

public class CSVResultSet implements IResultSet {
	FileLog log;
	
	private CSVStatement statement;
	
	private CSVReader reader;

	private Integer linesRead = 0;
	private Integer counter = 0;
	private List<String> csvHeader = new Vector<String>();

	// Have a big default (load in-memory if connection has no batchSize attribute).
	private int batchSize = 65535;
	
	@Override
	public void setLog(FileLog log) {
		this.log = log;
	}
	
	@Override
	public FileLog getLog() {
		return log;
	}
	
	public void setCSVReader(CSVReader csvReader) {
		this.reader = csvReader;
	}
	
	@Override
	public void setStatement(IStatement statement) throws Exception {
		Integer connectionBatchSize;
		
		this.statement = (CSVStatement)statement;

		setLog(statement.getLog());
		
		connectionBatchSize = this.statement.connection.getConnectionAttributes().getBatchSize();
		
		if (connectionBatchSize != null && connectionBatchSize > 0) {
			setBatchSize(connectionBatchSize);
		}
	}

	@Override
	public List<Record> fetchRows() throws Exception {
		List<Record> result = new Vector<Record>();
		String[] csvLine;
		int fetchCount = 0;
		
		while (fetchCount < batchSize && (csvLine = reader.readNext()) != null) {
			counter++;
			
			if (counter == 1) {
				// Header Line
				for (String column : csvLine) {
					csvHeader.add(column);
				}
			} else {
				Record record;
				Integer columnIndex = -1;
				List<Field> fieldList = new Vector<Field>();
				Boolean ignoreLine = false;
				
				linesRead++;
				
				fetchCount++;
				
				for (String value : csvLine) {
					String column;
					
					columnIndex++;
					
					column = csvHeader.get(columnIndex);
					
					fieldList.add(
							new Field(
								column, FieldType.T_STRING, value
							)
					);
				}
				
				if (fieldList.size() == 1 && csvHeader.size() > 1) {
					Field field = fieldList.get(0);
					String fieldValue = (String)field.getValue();
					
					if (fieldValue != null && fieldValue.isEmpty() == false) {
						throw new RuntimeException("Invalid CSV Line: " + String.join(",", csvLine) + " for header: " + String.join(",", csvHeader));
					} else {
						ignoreLine = true;
					}
				}
				
				if (fieldList.size() > 1 && csvHeader.size() != fieldList.size()) {
					throw new RuntimeException("Invalid CSV Line: " + String.join(",", csvLine) + " for header: " + String.join(",", csvHeader));
				}
				
				if (ignoreLine == false) {
					record = new Record(fieldList);
					result.add(record);
				}
			}
		}

		return result;
	}

	@Override
	public List<Record> fetchAllRows() throws Exception {
		List<Record> allRecords = new Vector<Record>();
		List<Record> fetchRecords = null;
		
		do {
			fetchRecords = fetchRows();
			
			if (fetchRecords != null && fetchRecords.isEmpty() == false) {
				allRecords.addAll(fetchRecords);
			}
		} while (fetchRecords != null && fetchRecords.isEmpty() == false);
		
		return allRecords;
	}

	@Override
	public void setBatchSize(Integer batchSize) throws Exception {
		this.batchSize = batchSize;
	}
}
