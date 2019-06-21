package com.vrs.sip.exchange;

import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;

public class RedirectionUrlValidationCallback implements IAutodiscoverRedirectionUrl {
    public boolean autodiscoverRedirectionUrlValidationCallback(
            String redirectionUrl) {
        return redirectionUrl.toLowerCase().startsWith("https://");
    }
}