package com.roadmap.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("app")
public class AppProperties {

    private String host;
    private String kakaoJsKey;
    private String kakaoRestKey;
    private String jusoConfirmKey;
    private String roadmapApiKey;

}

