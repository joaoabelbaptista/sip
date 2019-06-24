/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Attributes defined by a CSV Connection.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection.attributes;

import java.util.Properties;

import com.vrs.sip.connection.IConnectionAttributes;

public class CSVConnectionAttributes implements IConnectionAttributes {
	Integer batchSize;
	String directory;
	String dateFormat;
	String customDateFormat;
	String charset;

	@Override
	public Boolean hasAutoCommit() {
		return false;
	}

	@Override
	public Boolean hasLoginTimeout() {
		return false;
	}

	@Override
	public Boolean hasDirectory() {
		return true;
	}

	@Override
	public Boolean hasDateFormat() {
		return true;
	}

	@Override
	public Boolean hasCharset() {
		return true;
	}

	@Override
	public Boolean hasAllOrNone() {
		return false;
	}

	@Override
	public Boolean getAutoCommit() {
		return null;
	}

	@Override
	public Boolean getAllOrNone() {
		return null;
	}

	@Override
	public Integer getLoginTimeout() {
		return null;
	}

	@Override
	public Integer getBatchSize() {
		return batchSize;
	}

	@Override
	public String getDirectory() {
		return directory;
	}

	@Override
	public String getDateFormat() {
		return dateFormat;
	}

	@Override
	public String getCharset() {
		return charset;
	}

	@Override
	public void set(Properties props) {
		batchSize = Integer.valueOf(props.getProperty("batchSize"));
		directory = props.getProperty("directory");
		dateFormat = props.getProperty("dateFormat");
		charset = props.getProperty("charset");
	}

	@Override
	public void set(Boolean autoCommit, Integer loginTimeout, Integer batchSize, String directory, String dateFormat,
			String customDateFormat, String charset, Boolean allOrNone) {
		this.batchSize = batchSize;
		this.directory = directory;
		this.dateFormat = dateFormat;
		this.charset = charset;
	}

	@Override
	public void setAutoCommit(Boolean autoCommit) {
	}

	@Override
	public void setLoginTimeout(Integer loginTimeout) {
	}

	@Override
	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

	@Override
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	@Override
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	@Override
	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Override
	public void setAllOrNone(Boolean allOrNone) {
	}

	@Override
	public Boolean hasCustomDateFormat() {
		return true;
	}

	@Override
	public String getCustomDateFormat() {
		return customDateFormat;
	}

	@Override
	public void setCustomDateFormat(String customDateFormat) {
		this.customDateFormat = customDateFormat;
	}

}
