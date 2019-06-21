/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Filesystem Result Set implementation.
 * History: aosantos, 2016-07-03, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection.drivers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vrs.sip.FileLog;
import com.vrs.sip.Util;
import com.vrs.sip.connection.Field;
import com.vrs.sip.connection.FieldType;
import com.vrs.sip.connection.IResultSet;
import com.vrs.sip.connection.IStatement;
import com.vrs.sip.connection.Record;

public class FilesystemResultSet implements IResultSet {
	FileLog log;
	FilesystemStatement statement;
	String filePattern;
	
	String directory;
	List<String> fileList;
	
	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}
	
	public void initListFiles() {
		log.debug("initListFiles START");
		
		directory = statement.connection.getConnectionAttributes().getDirectory();

		log.debug("initListFiles directory = " + directory);
		
		if (directory == null) {
			throw new RuntimeException("Directory attribute is undefined");
		}
		
		fileList = listFiles(new File(directory));
		
		log.debug("initListFiles got " + fileList.size() + " entries");
		
		log.debug("initListFiles END");
	}
	
	@Override
	public void setStatement(IStatement statement) throws Exception {
		this.statement = (FilesystemStatement)statement;
		setLog(statement.getLog());

	}

	@Override
	public List<Record> fetchRows() throws Exception {
		List<Record> result = new Vector<Record>();
		
		log.debug("fetchRows START");
		
		log.debug("Pattern = " + filePattern);
	
		Pattern pattern = Pattern.compile(filePattern);
		
		if (fileList != null && fileList.isEmpty() == false) {
			for (String filename : fileList) {
				Matcher matcher = pattern.matcher(filename);
				BasicFileAttributes fileAttributes = null;
				
				if (matcher.matches()) {
					FileTime createdFileTime = null;
					FileTime lastModifiedFileTime = null;
					Date createdDate = null;
					Date lastModifiedDate = null;
					
					List<Field> recordFieldList = new Vector<Field>();
					
					try {
						File file = new File(directory + "/" + filename);
						LinkOption options = LinkOption.NOFOLLOW_LINKS;

						fileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class, options);
						
						if (fileAttributes != null) {
							createdFileTime = fileAttributes.creationTime();
							lastModifiedFileTime = fileAttributes.lastModifiedTime();
							
							if (createdFileTime != null) {
								createdDate = new Date(createdFileTime.toMillis());
								lastModifiedDate = new Date(lastModifiedFileTime.toMillis());
							}
						}
					} catch (IOException e) {
						log.info("directory=" + directory + ", filename=" + filename + ": " + Util.getStackTraceString(e));
					}
					
					recordFieldList.add(new Field("directory", FieldType.T_STRING, directory));
					recordFieldList.add(new Field("filename", FieldType.T_STRING, filename));
					recordFieldList.add(new Field("createdDate", FieldType.T_DATE, createdDate));
					recordFieldList.add(new Field("lastModifiedDate", FieldType.T_DATE, lastModifiedDate));
					
					Record record = new Record(recordFieldList);
					
					result.add(record);
				}
				
			}
		}
		
		fileList = null;
		
		log.debug("Returning " + (result != null ? result.size() : 0) + " files");
		
		log.debug("fetchRows END");
		
		return result;
	}

	@Override
	public List<Record> fetchAllRows() throws Exception {
		return fetchRows(); // Assumed for this implementation that fetchRows() return all the file entries
	}

	@Override
	public void setBatchSize(Integer batchSize) throws Exception {
	}

	@Override
	public void setLog(FileLog log) {
		this.log = log;
	}

	@Override
	public FileLog getLog() {
		return log;
	}

	private List<String> listFiles(File directory) {
		List<String> resultFileList = new Vector<String>();
		
		if (directory != null) {
			File[] fileList = directory.listFiles();
			
			if (fileList != null && fileList.length > 0) {
				for (File fileEntry : fileList) {
					if (fileEntry.isDirectory() == false) {
						resultFileList.add(fileEntry.getName());
					}
				}
			}
		}
		
		return resultFileList;
	}
}
