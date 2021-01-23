package com.ckinan.emailreporter.core.providers.google;

import com.ckinan.emailreporter.core.IDataSource;
import com.ckinan.emailreporter.core.providers.google.clients.SheetsClient;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class GoogleSheetsDataSource implements IDataSource {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String spreadsheetId = dotenv.get("GOOGLE_SHEET_ID");
    private static final String sheetAllRange = "Main!A1:J";
    private static final String sheetInternalDateRange = "Main!A1:A";
    public SheetsClient sheetsClient;

    public GoogleSheetsDataSource() throws GeneralSecurityException, IOException {
        sheetsClient = new SheetsClient();
    }

    @Override
    public List<Object> findLast() throws IOException {
        ValueRange internalDates =
                sheetsClient.getClient().spreadsheets().values().get(spreadsheetId, sheetInternalDateRange).execute();
        if(internalDates.getValues() != null) {
            return internalDates.getValues().get(internalDates.getValues().size() - 1);
        }
        return null;
    }

    @Override
    public void save(List<List<Object>> newValues) throws IOException {
        ValueRange valueRange = new ValueRange().setValues(newValues);
        sheetsClient.getClient().spreadsheets().values().append(spreadsheetId, sheetAllRange, valueRange)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }
}
