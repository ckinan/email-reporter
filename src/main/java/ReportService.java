import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.sheets.v4.model.ValueRange;
import io.github.cdimascio.dotenv.Dotenv;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import us.codecraft.xsoup.Xsoup;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportService {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String spreadsheetId = dotenv.get("GOOGLE_SHEET_ID");
    private static final String sheetAllRange = "Main!A1:J";
    private static final String sheetInternalDateRange = "Main!A1:A";
    private static final String dateQueryExpression = "<DATE_QUERY>";
    private static final String configFile = "/config.json";
    public static Config CONFIG;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream in = Main.class.getResourceAsStream(configFile);
        try {
            CONFIG = objectMapper.readValue(in, Config.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Long getLastInternalDate() throws IOException {
        Long lastInternalDate = null;
        ValueRange internalDates = GoogleClient.SHEETS_CLIENT.spreadsheets().values().get(spreadsheetId, sheetInternalDateRange).execute();
        if(internalDates.getValues() != null) {
            List<Object> lastRow = internalDates.getValues().get(internalDates.getValues().size() - 1);
            lastInternalDate = Long.parseLong(lastRow.get(0).toString());
        }
        return lastInternalDate;
    }

    public static String calculateQuery(Long lastInternalDate, String query) {
        if (lastInternalDate != null) {
            query = query.replaceAll(dateQueryExpression, " AND after:" + ReportService.dateToString(new Date(lastInternalDate)));
        } else {
            query = query.replaceAll(dateQueryExpression, "");
        }
        return query;
    }

    public static void appendRowsToSheets(List<List<Object>> newValues) throws IOException {
        ValueRange valueRange = new ValueRange().setValues(newValues);
        GoogleClient.SHEETS_CLIENT.spreadsheets().values().append(spreadsheetId, sheetAllRange, valueRange)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    public static List<List<Object>> calculateNewValues(List<Message> messages, Long lastInternalDate) throws IOException {
        List<List<Object>> newValues = new ArrayList<>();

        for (Message message : messages) {
            Message fullMessage = GoogleClient.GMAIL_CLIENT.users().messages().get("me", message.getId()).setFormat("full").execute();

            if(lastInternalDate != null && fullMessage.getInternalDate() > lastInternalDate){
                System.out.println("Processing message: " + message.getId());
                newValues.add(ReportService.calculateRowValues(fullMessage));
            } else {
                System.out.println("Skipping message: " + message.getId());
            }
        }

        Collections.sort(newValues, Comparator.comparingLong(value -> (long) value.get(0)));
        return newValues;
    }

    private static List<Object> calculateRowValues(Message fullMessage) {
        String body = new String(Base64.decodeBase64(fullMessage.getPayload().getBody().getData().getBytes()));
        Document document = Jsoup.parse(body);

        List<Object> cellValues = new ArrayList<>();
        cellValues.add(fullMessage.getInternalDate());

        for(Field field: ReportService.CONFIG.getFields()) {
            String value = ReportService.readDocument(document, field.getXpath());
            if(field.getRegex() != null) {
                Pattern compile = Pattern.compile(field.getRegex());
                Matcher matcher = compile.matcher(value);
                if(matcher.find()) {
                    value = matcher.group("matcherGroup");
                }
            }
            cellValues.add(value);
        }

        return cellValues;
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
