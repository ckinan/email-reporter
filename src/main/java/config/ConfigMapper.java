package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import pojo.Config;

import java.io.IOException;
import java.io.InputStream;

public class ConfigMapper {

    private static final String dateQueryExpression = "<DATE_QUERY>";
    private static final String configFile = "/config.json";
    public static Config CONFIG;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream in = ConfigService.class.getResourceAsStream(configFile);
        try {
            CONFIG = objectMapper.readValue(in, Config.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
