package pt.feup.industrial.mes.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pt.feup.industrial.mes.dto.PlcMachineDataDto;
import pt.feup.industrial.mes.dto.PlcToolDataDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class PlcStatisticsCache {
    private static final Logger log = LoggerFactory.getLogger(PlcStatisticsCache.class);
    private final PlcStatsClientService pythonPlcStatsClient;

    private final AtomicReference<Map<String, PlcMachineDataDto>> cachedMachineStats =
            new AtomicReference<>(Collections.emptyMap());
    private final AtomicReference<Map<String, PlcToolDataDto>> cachedToolStats =
            new AtomicReference<>(Collections.emptyMap());

    private volatile LocalDateTime lastMachineStatsRefresh = null;
    private volatile LocalDateTime lastToolStatsRefresh = null;

    @Autowired
    public PlcStatisticsCache(PlcStatsClientService pythonPlcStatsClient) {
        this.pythonPlcStatsClient = pythonPlcStatsClient;
    }

    @Scheduled(fixedRateString = "${mes.cache.plc-stats-refresh-rate-ms:15000}")
    public void refreshPlcStatisticsCache() {
        log.info("Refreshing PLC statistics cache from Python API...");
        pythonPlcStatsClient.fetchMachineStatsFromPython().subscribe(
                statsMap -> {
                    if (statsMap != null && !statsMap.isEmpty()) {
                        cachedMachineStats.set(new ConcurrentHashMap<>(statsMap));
                        lastMachineStatsRefresh = LocalDateTime.now();
                        log.info("Refreshed PLC machine stats cache. {} machines.", statsMap.size());
                    } else { log.warn("Empty/null machine stats from Python. Cache not updated."); }
                },
                error -> log.error("Error refreshing PLC machine stats cache: {}", error.getMessage())
        );
        pythonPlcStatsClient.fetchToolStatsFromPython().subscribe(
                statsMap -> {
                    if (statsMap != null && !statsMap.isEmpty()) {
                        cachedToolStats.set(new ConcurrentHashMap<>(statsMap));
                        lastToolStatsRefresh = LocalDateTime.now();
                        log.info("Refreshed PLC tool stats cache. {} tools.", statsMap.size());
                    } else { log.warn("Empty/null tool stats from Python. Cache not updated."); }
                },
                error -> log.error("Error refreshing PLC tool stats cache: {}", error.getMessage())
        );
    }
    public Map<String, PlcMachineDataDto> getCachedMachineStats() { return cachedMachineStats.get(); }
    public Map<String, PlcToolDataDto> getCachedToolStats() { return cachedToolStats.get(); }
    public LocalDateTime getLastMachineStatsRefreshTime() { return lastMachineStatsRefresh; }
    public LocalDateTime getLastToolStatsRefreshTime() { return lastToolStatsRefresh; }
}