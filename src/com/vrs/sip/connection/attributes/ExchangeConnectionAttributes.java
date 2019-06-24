package com.vrs.sip.connection.attributes;

import com.vrs.sip.connection.IConnectionAttributes;

import java.util.Properties;

public class ExchangeConnectionAttributes  implements IConnectionAttributes {

    Integer batchSize;
    String directory;
    String ImpersonatedEmailAccount;



    @Override
    public Boolean hasAutoCommit() {
        return false ;
    }

    @Override
    public Boolean hasLoginTimeout() {
        return false;
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
    public Boolean hasCustomDateFormat() {
        return false;
    }

    @Override
    public Boolean hasCharset() {
        return false;
    }

    @Override
    public Boolean hasAllOrNone() {
        return false;
    }

    @Override
    public Boolean getAutoCommit() {
        return null;
    }

    @Override
    public Integer getLoginTimeout() {
        return null;
    }

    @Override
    public Integer getBatchSize() {
        return batchSize;
    }

    @Override
    public String getDirectory() {
        return directory;
    }

    @Override
    public String getDateFormat() {
        return null;
    }

    @Override
    public String getCustomDateFormat() {
        return null;
    }

    @Override
    public String getCharset() {
        return null;
    }

    @Override
    public Boolean getAllOrNone() {
        return null;
    }

    @Override
    public void setAutoCommit(Boolean autoCommit) {

    }

    @Override
    public void setLoginTimeout(Integer loginTimeout) {

    }

    @Override
    public void setBatchSize(Integer batchSize)  {

        this.batchSize = batchSize;
    }

    @Override
    public void setDirectory(String directory) {

        this.directory = directory;

    }

    @Override
    public void setDateFormat(String dateFormat) {

    }

    @Override
    public void setCustomDateFormat(String customDateFormat) {

    }

    @Override
    public void setCharset(String charset) {

    }

    @Override
    public void setAllOrNone(Boolean allOrNone) {

    }

    @Override
    public void set(Properties props) {
        batchSize = Integer.valueOf(props.getProperty("batchSize"));
        directory = props.getProperty("directory");
        ImpersonatedEmailAccount = props.getProperty("impersonatedemailaccount");
    }

    @Override
    public void set(Boolean autoCommit, Integer loginTimeout, Integer batchSize, String directory, String dateFormat, String customDateFormat, String charset, Boolean allOrNone) {

        this.batchSize = batchSize;
        this.directory = directory;

    }

    public String getImpersonatedEmailAccount() {
        return ImpersonatedEmailAccount;
    }

    public void setImpersonatedEmailAccount(String impersonatedEmailAccount) {
        ImpersonatedEmailAccount = impersonatedEmailAccount;
    }

}
