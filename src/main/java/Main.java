import config.ConfigService;
import google.GoogleOperations;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String... args) throws IOException {
        IEmailProvider emailProvider = new GmailProvider();

        Long watermark = emailProvider.getWatermark();
        String query = ConfigService.calculateQuery(watermark);
        List<String> messageIds = emailProvider.getPendingMessageIds(query);

        if (messageIds.isEmpty()) {
            System.out.println("No messages found.");
            return;
        }

        // List of lists represents Rows and columns
        List<List<Object>> newValues = emailProvider.calculateNewValues(messageIds, watermark);

        if(newValues.size() > 0) {
            GoogleOperations.appendRowsToSheets(newValues);
        } else {
            System.out.println("No rows to update.");
        }

        System.out.println("Done.");
    }

}