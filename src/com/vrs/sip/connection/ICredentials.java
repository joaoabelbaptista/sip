/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Credentials Interface.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection;

import java.util.Properties;

public interface ICredentials {
	public Boolean hasUsername();
	public Boolean hasPassword();
	public Boolean hasSecurityToken();
	public Boolean hasHostname();
	public Boolean hasPort();
	public Boolean hasService();
	public Boolean hasLoginServer();
	
	public String getUsername();
	public String getPassword();
	public String getSecurityToken();
	public String getHostname();
	public Integer getPort();
	public String getService();
	public String getLoginServer();

	public void setUsername(String username);
	public void setPassword(String password);
	public void setSecurityToken(String securityToken);
	public void setHostname(String hostname);
	public void setPort(Integer port);
	public void setService(String service);
	public void setLoginServer(String loginServer);
	
	public void set(Properties props);

	public String getImpersonatedUserAccount() throws Exception;
	public void setImpersonatedUserAccount(String impersonatedEmailAccount) throws Exception;

    public String getProxyHostname();
    public Integer getProxyPort();

    public void setProxyHostname(String proxyHostname);
    public void setProxyPort(Integer proxyPort);

}
