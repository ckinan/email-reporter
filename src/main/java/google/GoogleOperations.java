package google;

import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.github.cdimascio.dotenv.Dotenv;
import pojo.GmailMessage;

import java.io.IOException;
import java.util.ArrayList;
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

    public static List<String> getMessageIds(String query) throws IOException {
        List<String> messageIds = new ArrayList<>();
        // Search operators in Gmail: https://support.google.com/mail/answer/7190?hl=en
        List<Message> messages = GmailClient.CLIENT.users().messages().list("me").setQ(query).execute().getMessages();

        for(Message message: messages) {
            messageIds.add(message.getId());
        }

        return messageIds;
    }

    public static GmailMessage getMessage(String messageId) throws IOException {
        Message fullMessage = GmailClient.CLIENT.users().messages().get("me", messageId).setFormat("full").execute();
        GmailMessage gmailMessage = new GmailMessage();
        gmailMessage.setInternalDate(fullMessage.getInternalDate());
        gmailMessage.setBody(new String(Base64.decodeBase64(fullMessage.getPayload().getBody().getData().getBytes())));
        return gmailMessage;
    }

}
