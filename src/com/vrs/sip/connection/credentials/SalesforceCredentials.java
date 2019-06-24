/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Salesforce Credentials Implementation.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection.credentials;

import com.vrs.sip.connection.ICredentials;

import java.util.Properties;

public class SalesforceCredentials implements ICredentials {
	public String username;
	public String password;
	public String securityToken;
	public String loginServer;
	
	/** Getters **/
	
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public String getLoginServer() {
		return loginServer;
	}
	
	@Override
	public String getHostname() {
		return null;
	}

	@Override
	public Integer getPort() {
		return null;
	}

	@Override
	public String getService() {
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
		this.securityToken = securityToken;
	}

	@Override
	public void setHostname(String hostname) {
	}

	@Override
	public void setPort(Integer port) {
	}

	@Override
	public void setService(String service) {
	}

	@Override
	public void setLoginServer(String loginServer) {
		this.loginServer = loginServer;
	}
	
	/** -- **/
	
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
		return false;
	}

	@Override
	public Boolean hasPort() {
		return false;
	}

	@Override
	public Boolean hasService() {
		return false;
	}
	
	@Override
	public Boolean hasLoginServer() {
		return true;
	}

	@Override
	public void set(Properties props) {
		this.username = props.getProperty("username");
		this.password = props.getProperty("password");
		this.securityToken = props.getProperty("securityToken");
		this.loginServer = props.getProperty("loginServer");
	}

	@Override
	public String getImpersonatedUserAccount() throws Exception {
	    throw new Exception("This method is not implemented");

	}

	@Override
	public void setImpersonatedUserAccount(String impersonatedEmailAccount) throws Exception {
        throw new Exception("This method is not implemented");
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
		return String.format("{ username=%1s, password=%2s, securityToken=%3s, loginServer=%4s }", username, "********", "************************", loginServer);
	}
}
