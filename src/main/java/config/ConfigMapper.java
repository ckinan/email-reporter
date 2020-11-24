package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import pojo.Config;

import java.io.IOException;
import java.io.InputStream;

public enum ConfigMapper {

    INSTANCE;

    private final String dateQueryExpression = "<DATE_QUERY>";
    private final String configFile = "/config.json";
    private Config CONFIG;

    ConfigMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream in = ConfigService.class.getResourceAsStream(configFile);
        try {
            CONFIG = objectMapper.readValue(in, Config.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Config getConfig() {
        return INSTANCE.CONFIG;
    }

}
