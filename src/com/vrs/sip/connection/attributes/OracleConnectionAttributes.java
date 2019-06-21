/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Attributes defined as a Oracle Connection.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection.attributes;

import java.util.Properties;

import com.vrs.sip.connection.IConnectionAttributes;

public class OracleConnectionAttributes implements IConnectionAttributes {
	Boolean autoCommit;
	Integer loginTimeout;
	Integer batchSize;

	@Override
	public Boolean hasAllOrNone() {
		return false;
	}

	@Override
	public Boolean getAllOrNone() {
		return null;
	}

	@Override
	public void setAllOrNone(Boolean allOrNone) {
	}

	@Override
	public Boolean hasAutoCommit() {
		return true;
	}

	@Override
	public Boolean hasLoginTimeout() {
		return true;
	}

	@Override
	public Boolean getAutoCommit() {
		return autoCommit;
	}

	@Override
	public Integer getLoginTimeout() {
		return loginTimeout;
	}

	@Override
	public Integer getBatchSize() {
		return batchSize;
	}

	@Override
	public void set(Properties props) {
		if (props != null) {
			autoCommit = props.getProperty("autoCommit", "true").equalsIgnoreCase("true") ? true : false;
			loginTimeout = Integer.valueOf(props.getProperty("loginTimeout", "5"));
			batchSize = Integer.valueOf(props.getProperty("batchSize"));
		}
	}

	@Override
	public Boolean hasDirectory() {
		return false;
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
	public String getDirectory() {
		return null;
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
	public void set(Boolean autoCommit, Integer loginTimeout, Integer batchSize, String directory, String dateFormat,
			String customDateFormat, String charset, Boolean allOrNone) {
		this.autoCommit = autoCommit;
		this.loginTimeout = loginTimeout;
		this.batchSize = batchSize;
	}

	@Override
	public void setAutoCommit(Boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	@Override
	public void setLoginTimeout(Integer loginTimeout) {
		this.loginTimeout = loginTimeout;
	}

	@Override
	public void setBatchSize(Integer batchSize) {
		this.batchSize = batchSize;
	}

	@Override
	public void setDirectory(String directory) {
	}

	@Override
	public void setDateFormat(String dateFormat) {
	}

	@Override
	public void setCharset(String charset) {
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
