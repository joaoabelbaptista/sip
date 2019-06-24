package com.vrs.sip.connection.attributes;

import java.util.Properties;

import com.vrs.sip.connection.IConnectionAttributes;

public class FilesystemConnectionAttributes implements IConnectionAttributes {
	private String directory;

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
		return false;
	}

	@Override
	public Boolean hasCharset() {
		return false;
	}

	@Override
	public Boolean hasAllOrNone() {
		return false;
	}

	@Override
	public Boolean getAutoCommit() {
		return false;
	}

	@Override
	public Integer getLoginTimeout() {
		return null;
	}

	@Override
	public Integer getBatchSize() {
		return null;
	}

	@Override
	public String getDirectory() {
		return directory;
	}

	@Override
	public String getDateFormat() {
		return null;
	}

	@Override
	public String getCharset() {
		return null;
	}

	@Override
	public Boolean getAllOrNone() {
		return null;
	}

	@Override
	public void set(Properties props) {
		directory = props.getProperty("directory");
	}

	@Override
	public void set(Boolean autoCommit, Integer loginTimeout, Integer batchSize, String directory, String dateFormat,
			String customDateFormat, String charset, Boolean allOrNone) {
		this.directory = directory;
	}

	@Override
	public void setAutoCommit(Boolean autoCommit) {
	}

	@Override
	public void setLoginTimeout(Integer loginTimeout) {
	}

	@Override
	public void setBatchSize(Integer batchSize) {
	}

	@Override
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	@Override
	public void setDateFormat(String dateFormat) {
	}

	@Override
	public void setCharset(String charset) {
	}

	@Override
	public void setAllOrNone(Boolean allOrNone) {
	}

	@Override
	public Boolean hasCustomDateFormat() {
		return false;
	}

	@Override
	public String getCustomDateFormat() {
		return null;
	}

	@Override
	public void setCustomDateFormat(String customDateFormat) {
	}
}
