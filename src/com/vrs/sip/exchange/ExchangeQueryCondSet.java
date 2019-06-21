package com.vrs.sip.exchange;

import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.search.ItemView;

public class ExchangeQueryCondSet implements Cloneable {

    public ExchangeQueryCondSet(String queryString, FolderId folderId, ItemView view) {
        this.view = view;
        this.folderId = folderId;
        this.queryString = queryString;
    }

    ItemView view;
    FolderId folderId;
    String queryString;

    public ItemView getView() {
        return view;
    }

    public void setView(ItemView view) {
        this.view = view;
    }

    public FolderId getFolderId() {
        return folderId;
    }

    public void setFolderId(FolderId folderId) {
        this.folderId = folderId;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }


    @Override public ExchangeQueryCondSet clone() throws CloneNotSupportedException {


            return (ExchangeQueryCondSet) super.clone();

    }
}
