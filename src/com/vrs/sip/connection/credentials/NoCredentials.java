/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Credentials Implementation where no credentials are required.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection.credentials;

import com.vrs.sip.connection.ICredentials;

import java.util.Properties;

public class NoCredentials implements ICredentials {

	@Override
	public Boolean hasUsername() {
		return false;
	}

	@Override
	public Boolean hasPassword() {
		return false;
	}

	@Override
	public Boolean hasSecurityToken() {
		return false;
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
		return false;
	}
	
	@Override
	public void set(Properties prop) {
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

	@Override
	public String getUsername() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getSecurityToken() {
		return null;
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

	@Override
	public String getLoginServer() {
		return null;
	}

	@Override
	public void setUsername(String username) {
	}

	@Override
	public void setPassword(String password) {
	}

	@Override
	public void setSecurityToken(String securityToken) {
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
	}

}
