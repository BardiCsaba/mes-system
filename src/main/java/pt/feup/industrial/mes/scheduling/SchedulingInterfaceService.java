package pt.feup.industrial.mes.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pt.feup.industrial.mes.dto.SchedulingProcessingRequestDto;

import java.time.Duration;

@Service
public class SchedulingInterfaceService {
    private static final Logger log = LoggerFactory.getLogger(SchedulingInterfaceService.class);

    private final WebClient schedulerWebClient;

    @Value("${scheduler.processEndpoint:/process-step}")
    private String processEndpoint;

    @Autowired
    public SchedulingInterfaceService(@Qualifier("schedulerWebClient") WebClient schedulerWebClient) {
        this.schedulerWebClient = schedulerWebClient;
    }

    public boolean requestSchedulingProcessing(SchedulingProcessingRequestDto requestDto) {
        log.info("Sending request to Scheduling service for MES Step ID {}: {}", requestDto.getMesOrderStepId(), requestDto);
        try {
            schedulerWebClient.post()
                    .uri(processEndpoint)
                    .bodyValue(requestDto)
                    .retrieve()
                    .toBodilessEntity() // We just care if it was accepted
                    .block(Duration.ofSeconds(5)); // Timeout for Schedule's initial ack
            log.info("Scheduling service acknowledged request for MES Step ID {}", requestDto.getMesOrderStepId());
            return true;
        } catch (Exception e) {
            log.error("Failed to send request to Scheduling service or get acknowledgment for MES Step ID {}: {}",
                    requestDto.getMesOrderStepId(), e.getMessage());
            return false;
        }
    }
}