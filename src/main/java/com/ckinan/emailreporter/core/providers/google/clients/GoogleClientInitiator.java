package com.ckinan.emailreporter.core.providers.google.clients;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GoogleClientInitiator extends AbstractGoogleClient {

    public GoogleClientInitiator() throws GeneralSecurityException, IOException {
        AbstractGoogleClient.getCredentials(HTTP_TRANSPORT);
    }

}
