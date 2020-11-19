import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.github.cdimascio.dotenv.Dotenv;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import us.codecraft.xsoup.Xsoup;

import java.io.*;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String spreadsheetId = dotenv.get("GOOGLE_SHEET_ID");
    private static final String sheetAllRange = "Main!A1:J";
    private static final String sheetInternalDateRange = "Main!A1:A";
    private static final String dateQueryExpression = "<DATE_QUERY>";

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        Long lastInternalDate = null;
        ValueRange internalDates = GoogleClient.SHEETS_CLIENT.spreadsheets().values().get(spreadsheetId, sheetInternalDateRange).execute();
        if(internalDates.getValues() != null) {
            List<Object> lastRow = internalDates.getValues().get(internalDates.getValues().size() - 1);
            lastInternalDate = Long.parseLong(lastRow.get(0).toString());
        }

        // Read configuration file
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream in = Main.class.getResourceAsStream("/config.json");
        Map<String, Object> mapObject = objectMapper.readValue(in, Map.class);
        String query = mapObject.get("query").toString();
        if (lastInternalDate != null) {
            query = query.replaceAll(dateQueryExpression, " AND after:" + Main.dateToString(new Date(lastInternalDate)));
        } else {
            query = query.replaceAll(dateQueryExpression, "");
        }

        List<Map<String, String>> fields = (List<Map<String, String>>) mapObject.get("fields");

        // Search operators in Gmail: https://support.google.com/mail/answer/7190?hl=en
        ListMessagesResponse listMessages = GoogleClient.GMAIL_CLIENT.users().messages().list("me").setQ(query).execute();
        List<Message> messages = listMessages.getMessages();

        if (messages.isEmpty()) {
            System.out.println("No messages found.");
        } else {
            List<List<Object>> newValues = new ArrayList<>();

            for (Message message : messages) {
                Message fullMessage = GoogleClient.GMAIL_CLIENT.users().messages().get("me", message.getId()).setFormat("full").execute();

                if(lastInternalDate != null && fullMessage.getInternalDate() > lastInternalDate){
                    System.out.println("Processing message: " + message.getId());
                    String body = new String(Base64.decodeBase64(fullMessage.getPayload().getBody().getData().getBytes()));

                    Document document = Jsoup.parse(body);

                    List<Object> cellValues = new ArrayList<>();
                    cellValues.add(fullMessage.getInternalDate());

                    for(Map<String, String> field: fields) {
                        String value = Main.readDocument(document, field.get("xpath"));
                        if(field.get("regex") != null) {
                            Pattern compile = Pattern.compile(field.get("regex"));
                            Matcher matcher = compile.matcher(value);
                            if(matcher.find()) {
                                value = matcher.group("matcherGroup");
                            }
                        }
                        cellValues.add(value);
                    }

                    newValues.add(cellValues);
                } else {
                    System.out.println("Skipping message: " + message.getId());
                }
            }

            Collections.sort(newValues, Comparator.comparingLong(value -> (long) value.get(0)));

            ValueRange valueRange = new ValueRange().setValues(newValues);
            GoogleClient.SHEETS_CLIENT.spreadsheets().values().append(spreadsheetId, sheetAllRange, valueRange)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            System.out.println("Done.");
        }
    }

    public static String readDocument(Document doc, String xpath) {
        List<Element> elements = Xsoup.compile(xpath).evaluate(doc).getElements();
        for(Element e: elements) {
            if("img".equals(e.tagName()) && xpath.endsWith("@src")) {
                return e.attributes().get("src");
            } else {
                return e.text();
            }
        }
        return null;
    }

    public static String dateToString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(date);
    }
}