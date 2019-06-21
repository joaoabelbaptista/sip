package com.vrs.sip.connection.drivers;

import com.vrs.sip.connection.IStatement;

public class SaleforceExchangeConnection  extends SalesforceConnection{

    @Override
    public IStatement createStatement() throws Exception {
        SalesforceStatement statement = null;

        getLog().debug("createStatement");

        if (partnerConnection != null) {
            statement = new SalesforceExchangeStatement();
            statement.setConnection(this);
        }

        return statement;
    }

}
