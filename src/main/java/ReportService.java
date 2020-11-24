import config.ConfigMapper;
import google.GoogleOperations;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pojo.Field;
import pojo.GmailMessage;
import us.codecraft.xsoup.Xsoup;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportService {

    public static Long getLastInternalDate() throws IOException {
        Long lastInternalDate = null;
        List<Object> lastRow = GoogleOperations.getLastRow();
        if(lastRow != null) {
            lastInternalDate = Long.parseLong(lastRow.get(0).toString());
        }
        return lastInternalDate;
    }

    public static void appendRowsToSheets(List<List<Object>> newValues) throws IOException {
        GoogleOperations.appendRowsToSheets(newValues);
    }

    public static List<String> getMessageIds(String query) throws IOException {
        return GoogleOperations.getMessageIds(query);
    }

    public static List<List<Object>> calculateNewValues(List<String> messageIds, Long lastInternalDate) throws IOException {
        List<List<Object>> newValues = new ArrayList<>();

        for (String messageId : messageIds) {
            GmailMessage message = GoogleOperations.getMessage(messageId);

            if(lastInternalDate != null && message.getInternalDate() > lastInternalDate){
                System.out.println("Processing message: " + messageId);
                newValues.add(ReportService.calculateRowValues(message.getInternalDate(), message.getBody()));
            } else {
                System.out.println("Skipping message: " + messageId);
            }
        }

        Collections.sort(newValues, Comparator.comparingLong(value -> (long) value.get(0)));
        return newValues;
    }

    private static List<Object> calculateRowValues(Long internalDate, String body) {
        Document document = Jsoup.parse(body);

        List<Object> cellValues = new ArrayList<>();
        cellValues.add(internalDate);

        for(Field field: ConfigMapper.CONFIG.getFields()) {
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



}
