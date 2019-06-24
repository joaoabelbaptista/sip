package com.vrs.sip.connection.drivers;

import com.vrs.sip.FileLog;
import com.vrs.sip.connection.*;
import com.vrs.sip.task.Schedule;

import java.util.List;

public class FilesystemStatement implements IStatement {
	FileLog log;
	
	IConnection connection;

	Schedule schedule;
	
	@Override
	public void setConnection(IConnection connection) throws Exception {
		this.connection = connection;
		
		setLog(connection.getLog());
	}

	@Override
	public IResultSet executeQuery(String filePattern) throws Exception {
		IConnectionAttributes connectionAttributes = connection.getConnectionAttributes();
		String directory = connectionAttributes.getDirectory();
		FilesystemResultSet frs;
		
		if (directory == null || directory.trim().equals("") == true) {
			directory = ".";
		}
		
		frs = new FilesystemResultSet();
		
		frs.setStatement(this);
		frs.setFilePattern(filePattern);
		frs.initListFiles();
		
		return frs;
	}

	@Override
	public Integer executeOperation(StatementOperationType operationType, Integer batchSize, String entityName, List<String> entityFieldNameList, List<Record> rowList, List<String> keyFieldList) throws Exception {
			throw new RuntimeException("executeOperation() not supported by " + this.getClass().getName());
	}

	@Override
	public void executeCall(String call, List<Object> inputParameterList) throws Exception {
		throw new RuntimeException("executeCall() not supported by " + this.getClass().getName());
	}

	@Override
	public IResultSet fetchRecords(String entity, String filterClause) throws Exception {
		throw new RuntimeException("fetchRecords() not supported by " + this.getClass().getName());
	}

	@Override
	public Integer executeTruncate(String entity) throws Exception {
		throw new RuntimeException("executeTruncate() not supported by " + this.getClass().getName());
	}

	@Override
	public void close() throws Exception {
	}

	@Override
	public void setLog(FileLog log) {
		this.log = log;
	}

	@Override
	public FileLog getLog() {
		return log;
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
