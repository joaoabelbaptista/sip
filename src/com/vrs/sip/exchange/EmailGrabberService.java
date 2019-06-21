package com.vrs.sip.exchange;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ConnectingIdType;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.misc.ImpersonatedUserId;

public class EmailGrabberService {



    public static ExchangeService ConnectToService(ExchangeVersion exchangeVersion)
    {

        ExchangeService service = new ExchangeService(exchangeVersion);

        return service;
    }

    public static ExchangeService ConnectToService(UserEmailData userData)
    {
        // return ConnectToService(userData, null);

        return null;
    }


    public static ExchangeService ConnectToServiceWithAutoDiscover(UserEmailData userData, Object listener) throws Exception {


        ExchangeService service = new ExchangeService(userData.getVersion());

        /*
        if (listener != null)
        {
            service.TraceListener = listener;
            service.TraceFlags = TraceFlags.All;
            service.TraceEnabled = true;
        }
        */

        ExchangeCredentials credentials = new WebCredentials (userData.getEmailAddress() , userData.getPassword());

        service.setCredentials( credentials );


        service = setAutoDiscover(userData, service);

        return service;
    }

    public static ExchangeService ConnectToServiceWithURLExchangeServer(UserEmailData userData, Object listener) throws Exception {


        ExchangeService service = new ExchangeService(userData.getVersion());


/*
        service.setTraceEnabled(true);
        service.setTraceFlags(EnumSet.allOf(TraceFlags.class)); // can also be restricted
        service.setTraceListener(new ITraceListener() {

            public void trace(String traceType, String traceMessage) {
                // do some logging-mechanism here
                System.out.println("Type:" + traceType + " Message:" + traceMessage);
            }
        });
*/
        /*
        if (listener != null)
        {
            service.TraceListener = listener;
            service.TraceFlags = TraceFlags.All;
            service.TraceEnabled = true;
        }
        */

        ExchangeCredentials credentials = new WebCredentials (userData.getEmailAddress() , userData.getPassword());

        service.setCredentials( credentials );

        service.setUrl(userData.getUrlExchangeServer());

        return service;
    }

    public static ExchangeService ConnectToServiceWithURLExchangeServerTeste(UserEmailData userData, Object listener) throws Exception {


        ExchangeService service = new CustomExchangeService();

        /*
        if (listener != null)
        {
            service.TraceListener = listener;
            service.TraceFlags = TraceFlags.All;
            service.TraceEnabled = true;
        }
        */

        ExchangeCredentials credentials = new WebCredentials (userData.getEmailAddress() , userData.getPassword());

        service.setCredentials( credentials );

        service.setUrl(userData.getUrlExchangeServer());

        return service;
    }

    public static ExchangeService ConnectToServiceWithImpersonationURLExchangeServer(UserEmailData userData,
                                                                                     String impersonatedUserSMTPAddress,
                                                                                     Object listener) throws Exception {


        ExchangeService service = new ExchangeService(userData.getVersion());

        /*
        if (listener != null)
        {
            service.TraceListener = listener;
            service.TraceFlags = TraceFlags.All;
            service.TraceEnabled = true;
        }
        */

        ExchangeCredentials credentials = new WebCredentials (userData.getEmailAddress() , userData.getPassword());

        ImpersonatedUserId impersonatedUserId =
                new ImpersonatedUserId(ConnectingIdType.SmtpAddress, impersonatedUserSMTPAddress);

        service.setImpersonatedUserId(impersonatedUserId);

        service.setCredentials( credentials );

        service.setUrl(userData.getUrlExchangeServer());

        return service;
    }

    public static ExchangeService ConnectToServiceWithImpersonationAutodiscover(
            UserEmailData userData,
            String impersonatedUserSMTPAddress,
            Object listener) throws Exception {
        ExchangeService service = new ExchangeService(userData.getVersion());

        /*
        if (listener != null)
        {
            service.TraceListener = listener;
            service.TraceFlags = TraceFlags.All;
            service.TraceEnabled = true;
        }
        */

        ExchangeCredentials credentials = new WebCredentials (userData.getEmailAddress() , userData.getPassword());

        service.setCredentials(credentials);

        ImpersonatedUserId impersonatedUserId =
                new ImpersonatedUserId(ConnectingIdType.SmtpAddress, impersonatedUserSMTPAddress);

        service.setImpersonatedUserId(impersonatedUserId);

        service = setAutoDiscover(userData, service);

        return service;
    }

    private static ExchangeService setAutoDiscover(UserEmailData userData, ExchangeService service) throws Exception {

        if (userData.getAutodiscoverUrl() == null)
        {
            //Console.Write(string.Format("Using Autodiscover to find EWS URL for {0}. Please wait... ", userData.EmailAddress));

            service.autodiscoverUrl(userData.getEmailAddress(), new RedirectionUrlValidationCallback());

            userData.setAutodiscoverUrl(service.getUrl());

            //Console.WriteLine("Autodiscover Complete");
        }
        else
        {
            service.setUrl(userData.getAutodiscoverUrl());
        }

        return service;
    }




}
