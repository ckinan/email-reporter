import config.ConfigMapper;
import config.ConfigService;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String... args) throws IOException {
        Long lastInternalDate = ReportService.getLastInternalDate();
        String query = ConfigService.calculateQuery(lastInternalDate, ConfigMapper.getConfig().getQuery());
        List<String> messageIds = ReportService.getMessageIds(query);

        if (messageIds.isEmpty()) {
            System.out.println("No messages found.");
            return;
        }

        // List of lists represents Rows and columns
        List<List<Object>> newValues = ReportService.calculateNewValues(messageIds, lastInternalDate);

        if(newValues.size() > 0) {
            ReportService.appendRowsToSheets(newValues);
        } else {
            System.out.println("No rows to update.");
        }

        System.out.println("Done.");
    }

}