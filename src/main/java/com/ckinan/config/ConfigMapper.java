package com.ckinan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ckinan.pojo.Config;

import java.io.IOException;
import java.io.InputStream;

public class ConfigMapper {

    private Config config;

    public ConfigMapper(String configFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream in = ConfigMapper.class.getResourceAsStream(configFile);
        this.config = objectMapper.readValue(in, Config.class);
    }

    public Config getConfig() {
        return this.config;
    }

}
