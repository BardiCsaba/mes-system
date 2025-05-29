package pt.feup.industrial.mes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${erp.api.baseUrl}")
    private String erpBaseUrl;

    @Value("${scheduler.baseUrl}")
    private String schedulerBaseUrl;

    @Bean
    public WebClient erpWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(erpBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient schedulerWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(schedulerBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}