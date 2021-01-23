package com.ckinan.google;

import com.google.api.services.sheets.v4.Sheets;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SheetsClient extends AbstractGoogleClient {

    public Sheets client;

    public SheetsClient() throws GeneralSecurityException, IOException {
        client = new Sheets.Builder(
                HTTP_TRANSPORT,
                AbstractGoogleClient.JSON_FACTORY,
                AbstractGoogleClient.getCredentials(HTTP_TRANSPORT)
        ).setApplicationName(
                AbstractGoogleClient.APPLICATION_NAME
        ).build();
    }

    public Sheets getClient() {
        return this.client;
    }
}
