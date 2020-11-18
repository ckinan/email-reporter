import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
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
    private static final String APPLICATION_NAME = "Email Reporter";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final Dotenv dotenv = Dotenv.load();
    private static final String spreadsheetId = dotenv.get("GOOGLE_SHEET_ID");
    private static final String sheetAllRange = "Main!A1:J";
    private static final String sheetInternalDateRange = "Main!A1:A";
    private static final String dateQueryExpression = "<DATE_QUERY>";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     * See:
     * - https://developers.google.com/gmail/api/auth/scopes
     * - https://developers.google.com/sheets/api/guides/authorizing
     */
    private static final List<String> SCOPES = Arrays.asList(
            GmailScopes.GMAIL_READONLY,
            SheetsScopes.SPREADSHEETS
    );
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = Main.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail serviceGmail = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        Sheets serviceSheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        Long lastInternalDate = null;
        ValueRange internalDates = serviceSheets.spreadsheets().values().get(spreadsheetId, sheetInternalDateRange).execute();
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
        ListMessagesResponse listMessages = serviceGmail.users().messages().list("me").setQ(query).execute();
        List<Message> messages = listMessages.getMessages();

        if (messages.isEmpty()) {
            System.out.println("No messages found.");
        } else {
            List<List<Object>> newValues = new ArrayList<>();

            for (Message message : messages) {
                Message fullMessage = serviceGmail.users().messages().get("me", message.getId()).setFormat("full").execute();

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
            serviceSheets.spreadsheets().values().append(spreadsheetId, sheetAllRange, valueRange)
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