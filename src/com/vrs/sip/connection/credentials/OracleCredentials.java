/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Oracle Credentials Implementation.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection.credentials;

import com.vrs.sip.connection.ICredentials;

import java.util.Properties;

public class OracleCredentials implements ICredentials {
	private String username;
	private String password;
	private String hostname;
	private Integer port;
	private String service;
	
	/** Getters **/
	@Override
	public String getUsername() {
		return username;
	}
	
	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getHostname() {
		return hostname;
	}

	@Override
	public Integer getPort() {
		return port;
	}

	@Override
	public String getService() {
		return service;
	}

	@Override
	public String getSecurityToken() {
		return null;
	}

	@Override
	public String getLoginServer() {
		return null;
	}
	
	/** Setters **/
	
	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void setSecurityToken(String securityToken) {
	}

	@Override
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@Override
	public void setPort(Integer port) {
		this.port = port;
	}

	@Override
	public void setService(String service) {
		this.service = service;
	}

	@Override
	public void setLoginServer(String loginServer) {
	}
	
	/** -- **/
	
	public OracleCredentials() {
		
	}
	
	public OracleCredentials(Properties props) {
		if (props != null) {
			set(props);
		}
	}

	@Override
	public Boolean hasUsername() {
		return true;
	}

	@Override
	public Boolean hasPassword() {
		return true;
	}

	@Override
	public Boolean hasSecurityToken() {
		return true;
	}

	@Override
	public Boolean hasHostname() {
		return true;
	}

	@Override
	public Boolean hasPort() {
		return true;
	}

	@Override
	public Boolean hasService() {
		return true;
	}

	@Override
	public Boolean hasLoginServer() {
		return false;
	}
	
	@Override
	public void set(Properties prop) {
		this.username = prop.getProperty("username");
		this.password = prop.getProperty("password");
		this.hostname = prop.getProperty("hostname");
		this.port = prop.getProperty("port") == null ? 1521 : Integer.valueOf(prop.getProperty("port"));
		this.service = prop.getProperty("service");
	}

	@Override
	public String getImpersonatedUserAccount() throws Exception {
		return null;
	}

	@Override
	public void setImpersonatedUserAccount(String impersonatedEmailAccount) throws Exception {

	}

	@Override
	public String getProxyHostname() {
		return null;
	}

	@Override
	public Integer getProxyPort() {
		return null;
	}

	@Override
	public void setProxyHostname(String proxyHostname) {

	}

	@Override
	public void setProxyPort(Integer proxyPort) {

	}

	public String toString() {
		return String.format("{ username=%1s, password=%2s, hostname=%3s, port=%4s, service=%5s }", username, "********", hostname, port, service);
	}
}
