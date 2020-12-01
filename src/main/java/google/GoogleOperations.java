package google;

import com.google.api.services.sheets.v4.model.ValueRange;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.util.List;

public class GoogleOperations {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String spreadsheetId = dotenv.get("GOOGLE_SHEET_ID");
    private static final String sheetAllRange = "Main!A1:J";
    private static final String sheetInternalDateRange = "Main!A1:A";

    public static List<Object> getLastRow() throws IOException {
        ValueRange internalDates = SheetsClient.CLIENT.spreadsheets().values().get(spreadsheetId, sheetInternalDateRange).execute();
        if(internalDates.getValues() != null) {
            return internalDates.getValues().get(internalDates.getValues().size() - 1);
        }
        return null;
    }

    public static void appendRowsToSheets(List<List<Object>> newValues) throws IOException {
        ValueRange valueRange = new ValueRange().setValues(newValues);
        SheetsClient.CLIENT.spreadsheets().values().append(spreadsheetId, sheetAllRange, valueRange)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

}
