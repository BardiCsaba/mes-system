package pt.feup.industrial.mes.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pt.feup.industrial.mes.dto.PlcMachineDataDto;
import pt.feup.industrial.mes.dto.PlcToolDataDto;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

@Service
public class PlcStatsClientService {
    private static final Logger log = LoggerFactory.getLogger(PlcStatsClientService.class);

    private final WebClient pythonApiWebClient;

    @Value("${plc.stats.api.machinesEndpoint:/plc-stats/machines}")
    private String machinesEndpoint;

    @Value("${plc.stats.api.toolsEndpoint:/plc-stats/tools}")
    private String toolsEndpoint;

    @Autowired
    public PlcStatsClientService(WebClient.Builder webClientBuilder,
                                       @Value("${plc.stats.api.baseUrl}") String pythonApiBaseUrl) {
        this.pythonApiWebClient = webClientBuilder.baseUrl(pythonApiBaseUrl).build();
    }

    public Mono<Map<String, PlcMachineDataDto>> fetchMachineStatsFromPython() {
        log.debug("Fetching machine stats from Python API: {}", machinesEndpoint);
        return pythonApiWebClient.get()
                .uri(machinesEndpoint)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, PlcMachineDataDto>>() {})
                .doOnError(e -> log.error("Error fetching machine stats from Python: {}", e.getMessage()))
                .onErrorReturn(Collections.emptyMap());
    }

    public Mono<Map<String, PlcToolDataDto>> fetchToolStatsFromPython() {
        log.debug("Fetching tool stats from Python API: {}", toolsEndpoint);
        return pythonApiWebClient.get()
                .uri(toolsEndpoint)
                .retrieve()
                // Assumes Python returns a JSON object where keys are tool names
                .bodyToMono(new ParameterizedTypeReference<Map<String, PlcToolDataDto>>() {})
                .doOnError(e -> log.error("Error fetching tool stats from Python: {}", e.getMessage()))
                .onErrorReturn(Collections.emptyMap());
    }
}
