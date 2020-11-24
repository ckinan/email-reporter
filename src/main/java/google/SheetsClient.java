package google;

import com.google.api.services.sheets.v4.Sheets;

import java.io.IOException;

public class SheetsClient extends AbstractGoogleClient {

    public static Sheets CLIENT;

    static {
        try {
            CLIENT = new Sheets.Builder(
                    AbstractGoogleClient.HTTP_TRANSPORT,
                    AbstractGoogleClient.JSON_FACTORY,
                    AbstractGoogleClient.getCredentials(AbstractGoogleClient.HTTP_TRANSPORT)
            ).setApplicationName(
                    AbstractGoogleClient.APPLICATION_NAME
            ).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
