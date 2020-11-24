package google;

import com.google.api.services.gmail.Gmail;

import java.io.IOException;

public class GmailClient extends AbstractGoogleClient{

    public static Gmail CLIENT;

    static {
        try {
            CLIENT = new Gmail.Builder(
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
