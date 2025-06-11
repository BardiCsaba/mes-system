package pt.feup.industrial.mes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pt.feup.industrial.mes.model.MesOrderStep;
import pt.feup.industrial.mes.service.MesDisplayStatisticsService;
import pt.feup.industrial.mes.service.ProductionService;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/ui")
public class MesUiController {

    private final ProductionService productionService; // For dashboard
    private final MesDisplayStatisticsService displayStatisticsService; // For statistics

    @Autowired
    public MesUiController(ProductionService productionService, MesDisplayStatisticsService displayStatisticsService) {
        this.productionService = productionService;
        this.displayStatisticsService = displayStatisticsService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        List<MesOrderStep> orderSteps = productionService.getAllOrderSteps();
        model.addAttribute("orderSteps", orderSteps);
        model.addAttribute("pageTitle", "MES Dashboard");
        return "mes_dashboard";
    }

    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        model.addAttribute("machineStats", displayStatisticsService.getCombinedMachineStatistics());
        model.addAttribute("toolStats", displayStatisticsService.getCombinedToolStatistics());
        model.addAttribute("dockStats", displayStatisticsService.getDockStatistics());
        model.addAttribute("pageTitle", "MES Statistics");

        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        long uptimeMillis = rb.getUptime();
        model.addAttribute("applicationUptimeSeconds", Math.max(1L, TimeUnit.MILLISECONDS.toSeconds(uptimeMillis)));
        model.addAttribute("lastMachineRefresh", displayStatisticsService.getLastMachineStatsRefreshTime());
        model.addAttribute("lastToolRefresh", displayStatisticsService.getLastToolStatsRefreshTime());
        return "mes_statistics";
    }
}