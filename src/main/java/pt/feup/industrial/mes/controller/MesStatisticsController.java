package pt.feup.industrial.mes.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.feup.industrial.mes.dto.DockStatsDto;
import pt.feup.industrial.mes.dto.MachineStatsDto;
import pt.feup.industrial.mes.dto.ToolStatsDto;
import pt.feup.industrial.mes.service.MesStatisticsService;

import java.util.List;

@RestController
@RequestMapping("/api/mes/stats")
public class MesStatisticsController {

    private static final Logger log = LoggerFactory.getLogger(MesStatisticsController.class);

    private final MesStatisticsService statisticsService;

    @Autowired
    public MesStatisticsController(MesStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/machines")
    public List<MachineStatsDto> getMachineStatistics() {
        log.debug("Request received for machine statistics");
        return statisticsService.getMachineStatistics();
    }

    @GetMapping("/tools")
    public List<ToolStatsDto> getToolStatistics() {
        log.debug("Request received for tool statistics");
        return statisticsService.getToolStatistics();
    }

    @GetMapping("/docks")
    public List<DockStatsDto> getDockStatistics() {
        log.debug("Request received for dock statistics");
        return statisticsService.getDockStatistics();
    }
}
