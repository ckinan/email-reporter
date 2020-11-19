import com.google.api.services.gmail.model.Message;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String... args) throws IOException {
        Long lastInternalDate = ReportService.getLastInternalDate();
        String query = ReportService.calculateQuery(lastInternalDate, ReportService.CONFIG.getQuery());

        // Search operators in Gmail: https://support.google.com/mail/answer/7190?hl=en
        List<Message> messages = GoogleClient.GMAIL_CLIENT.users().messages().list("me").setQ(query).execute().getMessages();

        if (messages.isEmpty()) {
            System.out.println("No messages found.");
            return;
        }

        List<List<Object>> newValues = ReportService.calculateNewValues(messages, lastInternalDate);

        if(newValues.size() > 0) {
            ReportService.appendRowsToSheets(newValues);
        } else {
            System.out.println("No rows to update.");
        }

        System.out.println("Done.");
    }

}