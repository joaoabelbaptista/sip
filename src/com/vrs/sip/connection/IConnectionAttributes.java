/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Connection Attributes Interface.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection;

import java.util.Properties;

public interface IConnectionAttributes {
	public Boolean hasAutoCommit();
	public Boolean hasLoginTimeout();
	public Boolean hasDirectory();
	public Boolean hasDateFormat();
	public Boolean hasCustomDateFormat();
	public Boolean hasCharset();
	public Boolean hasAllOrNone();

	public Boolean getAutoCommit();
	public Integer getLoginTimeout();
	public Integer getBatchSize();
	public String getDirectory();
	public String getDateFormat();
	public String getCustomDateFormat();
	public String getCharset();
	public Boolean getAllOrNone();
	
	public void setAutoCommit(Boolean autoCommit);
	public void setLoginTimeout(Integer loginTimeout);
	public void setBatchSize(Integer batchSize);
	public void setDirectory(String directory);
	public void setDateFormat(String dateFormat);
	public void setCustomDateFormat(String customDateFormat);
	public void setCharset(String charset);
	public void setAllOrNone(Boolean allOrNone);
	
	public void set(Properties props);
	
	public void set(Boolean autoCommit, Integer loginTimeout, Integer batchSize, String directory, String dateFormat, String customDateFormat, String charset, Boolean allOrNone);
}
