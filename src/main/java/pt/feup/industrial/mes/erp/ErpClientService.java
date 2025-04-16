package pt.feup.industrial.mes.erp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pt.feup.industrial.mes.dto.OrderItemCompletionDto; //TODO create common DTO for both systems
import reactor.core.publisher.Mono;

@Service
public class ErpClientService {
    private static final Logger log = LoggerFactory.getLogger(ErpClientService.class);

    private final WebClient erpWebClient;

    @Value("${erp.api.completionEndpoint:/api/erp/order-items/{erpOrderItemId}/complete}")
    private String completionEndpointUriTemplate;

    @Autowired
    public ErpClientService(WebClient erpWebClient) {
        this.erpWebClient = erpWebClient;
    }

    public void notifyErpItemCompleted(OrderItemCompletionDto completionInfo) {
        log.info("Notifying ERP of completion for Order Item ID: {}", completionInfo.getErpOrderItemId());

        String uri = completionEndpointUriTemplate.replace("{erpOrderItemId}", String.valueOf(completionInfo.getErpOrderItemId()));

        erpWebClient.put()
                .uri(uri)
                .bodyValue(completionInfo)
                .retrieve()
                .onStatus(status -> status.isError(), response -> {
                    log.error("ERP returned error status {} for completion notification of item {}", response.statusCode(), completionInfo.getErpOrderItemId());
                    response.bodyToMono(String.class).doOnNext(body -> log.error("ERP Error Body: {}", body));
                    return Mono.error(new RuntimeException("ERP notification failed with status: " + response.statusCode()));
                })
                .toBodilessEntity()
                .doOnError(error -> log.error("Error notifying ERP for item {}: {}", completionInfo.getErpOrderItemId(), error.getMessage()))
                .doOnSuccess(response -> log.info("Successfully notified ERP for item {}. Status: {}", completionInfo.getErpOrderItemId(), response.getStatusCode()))
                .subscribe();
    }
}