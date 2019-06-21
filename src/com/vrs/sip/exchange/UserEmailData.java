package com.vrs.sip.exchange;

import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;

import java.net.URI;

public class UserEmailData implements IUserData{


    private ExchangeVersion Version = null;
    private String EmailAddress = null;
    private String Password = null;
    private URI AutodiscoverUrl = null;
    private URI UrlExchangeServer = null;

    public UserEmailData() {
    }


    public UserEmailData(ExchangeVersion Version, String EmailAddress, String Password, URI UrlExchangeServer, URI AutodiscoverUrl) {

        this.setVersion(Version);
        this.setEmailAddress(EmailAddress);
        this.setPassword(Password);
        this.setUrlExchangeServer(UrlExchangeServer);
        this.setAutodiscoverUrl(AutodiscoverUrl);

    }

    public UserEmailData(ExchangeVersion Version, String EmailAddress, String Password, URI UrlExchangeServer) {

        this.setVersion(Version);
        this.setEmailAddress(EmailAddress);
        this.setPassword(Password);
        this.setUrlExchangeServer(UrlExchangeServer);

    }

    public ExchangeVersion getVersion() {
        return Version;
    }

    public void setVersion(ExchangeVersion version) {
        Version = version;
    }

    public String getEmailAddress() {
        return EmailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        EmailAddress = emailAddress;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public URI getAutodiscoverUrl() {
        return AutodiscoverUrl;
    }

    public void setAutodiscoverUrl(URI autodiscoverUrl) {
        AutodiscoverUrl = autodiscoverUrl;
    }

    public URI getUrlExchangeServer() {
        return UrlExchangeServer;
    }

    public void setUrlExchangeServer(URI urlExchangeServer) {
        UrlExchangeServer = urlExchangeServer;
    }
}
