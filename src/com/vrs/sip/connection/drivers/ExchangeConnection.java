package com.vrs.sip.connection.drivers;


import com.vrs.sip.FileLog;
import com.vrs.sip.connection.*;
import com.vrs.sip.connection.attributes.ExchangeConnectionAttributes;
import com.vrs.sip.connection.credentials.NoCredentials;
import com.vrs.sip.exchange.EmailGrabberService;
import com.vrs.sip.exchange.EmailGrabberUtil;
import com.vrs.sip.exchange.UserEmailData;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.WebProxy;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.search.FindItemsResults;

import java.net.URI;

public class ExchangeConnection extends AbstractConnection {

    FileLog log;

    ExchangeService service;

    public ExchangeService getService() {
        return service;
    }

    public void setService(ExchangeService service) {
        this.service = service;
    }


    @Override
    public void setLog(FileLog log) {
        this.log = log;
    }

    @Override
    public FileLog getLog() {
        return log;
    }

    @Override
    public ConnectionType getConnectionType()  {

        return ConnectionType.EXCHANGE;
    }

    @Override
    public ICredentials getCredentials() {
        if (credentials == null) {
            credentials = new NoCredentials();
        }

        return credentials;
    }

    @Override
    public IConnectionAttributes getConnectionAttributes() {
        if (connectionAttributes == null) {
            connectionAttributes = new ExchangeConnectionAttributes();
        }

        return connectionAttributes;
    }

    @Override
    public void setCredentials(ICredentials credentials) throws Exception {

        this.credentials = credentials;

    }

    @Override
    public void openConnection() throws Exception {

        getLog().debug("openConnection START");

        if (exchangeLogin())
            doConnectivityTest();

        getLog().debug("openConnection END");

    }

    private void doConnectivityTest() throws Exception {

        getLog().debug("doConnectivityTest START");

        String querystring = "Kind:email";

        FindItemsResults<Item> result = EmailGrabberUtil.FindItemsBySearchCondition(service, WellKnownFolderName.Inbox, querystring,1);

        getLog().debug("Exchange Connectivity Test - number of mailbox " + result.getTotalCount());

        getLog().debug("doConnectivityTest END");
    }


    private boolean exchangeLogin() throws Exception{
        boolean isLoginSuccess = false;

        getLog().debug("exchangelogin START");

        setConnectorConfig();

        isLoginSuccess = getService() != null;

        getLog().debug("isLoginSuccess = " + isLoginSuccess);

        getLog().debug("exchangeLogin END");

        return isLoginSuccess;
    }

    private void setConnectorConfig() throws Exception {

        // UserEmailData userEmailData = new  UserEmailData(ExchangeVersion.Exchange2010_SP2,"miguel.fernandes4@vodafone.com", "Welcome11", new URI("https://webmail-north.vodafone.com/ews/exchange.asmx"));

        UserEmailData userEmailData = new UserEmailData(ExchangeVersion.Exchange2010_SP2, getCredentials().getUsername(), getCredentials().getPassword(), new URI(getCredentials().getLoginServer()));


        if (getCredentials().getImpersonatedUserAccount() != null && !getCredentials().getImpersonatedUserAccount().isEmpty()) {



            service = EmailGrabberService.ConnectToServiceWithImpersonationURLExchangeServer(userEmailData, getCredentials().getImpersonatedUserAccount(), null);

        } else
        {

            service = EmailGrabberService.ConnectToServiceWithURLExchangeServer(userEmailData, null);
        }



        if (service == null)
            service = EmailGrabberService.ConnectToServiceWithAutoDiscover(userEmailData, null);

        if (service == null)
            throw new Exception(String.format("The connection to the Exchange Server with the user %s was not possible", userEmailData));


        log.info("Exchange Connection Credentials => "+ getCredentials().toString());

        if (getCredentials().getProxyHostname()!=null && getCredentials().getProxyPort()!=null && getCredentials().getProxyPort() > 1)
             service.setWebProxy(new WebProxy(getCredentials().getProxyHostname(), getCredentials().getProxyPort()));

        //service1 = EmailGrabberService.ConnectToServiceWithURLExchangeServerTeste(userEmailData,null);
    }

    @Override
    public void closeConnection() throws Exception {

    }

    @Override
    public IStatement createStatement() throws Exception {

        ExchangeStatement statement = null;

        getLog().debug("createStatement");

        statement = new ExchangeStatement();

        statement.setConnection(this);

        return statement;

    }

    @Override
    public void commit() throws Exception {

    }

    @Override
    public Object getImplConnection() {
        return getService();
    }
}
