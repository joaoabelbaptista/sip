package com.vrs.sip.connection.credentials;

import com.vrs.sip.connection.ICredentials;

import java.util.Properties;

public class ExchangeCredentials implements ICredentials {

    public String username;
    public String password;
    public String urlExchangeServer;
    public String impersonatedUserAccount;
    public String proxyHostName;
    public int proxyPort;


    @Override
    public String getImpersonatedUserAccount() {
        return impersonatedUserAccount;
    }
    @Override
    public void setImpersonatedUserAccount(String impersonatedUserAccount) {
        this.impersonatedUserAccount = impersonatedUserAccount;
    }

    @Override
    public String getProxyHostname() {
        return proxyHostName;
    }

    @Override
    public Integer getProxyPort() {
        return proxyPort;
    }

    @Override
    public void setProxyHostname(String proxyHostname) {
      this.proxyHostName = proxyHostname;
    }

    @Override
    public void setProxyPort(Integer proxyPort) {

        this.proxyPort = proxyPort;
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
        return true;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
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
        return this.urlExchangeServer;
    }

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

    }

    @Override
    public void setPort(Integer port) {

    }

    @Override
    public void setService(String service) {

    }

    @Override
    public void setLoginServer(String loginServer) {

        this.urlExchangeServer = loginServer;

    }

    @Override
    public void set(Properties props) {

        this.username = props.getProperty("username");
        this.password = props.getProperty("password");
        this.urlExchangeServer = props.getProperty("urlexchangeserver");
        this.proxyHostName = props.getProperty("proxyhost","");
        this.proxyPort = Integer.valueOf(props.getProperty("proxyport","-1"));



    }

    public String toString() {
        return String.format("{ username=%1s, password=%2s, urlexchangeserver=%3s, impersonateuser=%s, proxyHostname=%s, proxyPort=%s }", username, "********", urlExchangeServer, impersonatedUserAccount,proxyHostName,proxyPort);
    }
}
