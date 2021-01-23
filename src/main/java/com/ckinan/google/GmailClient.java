package com.ckinan.google;

import com.google.api.services.gmail.Gmail;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GmailClient extends AbstractGoogleClient{

    public Gmail client;

    public GmailClient() throws GeneralSecurityException, IOException {
        client = new Gmail.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                AbstractGoogleClient.getCredentials(HTTP_TRANSPORT)
        ).setApplicationName(
                AbstractGoogleClient.APPLICATION_NAME
        ).build();
    }

    public Gmail getClient() {
        return this.client;
    }
}
