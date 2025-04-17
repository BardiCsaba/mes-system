package pt.feup.industrial.mes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pt.feup.industrial.mes.model.MesOrderStep;
import pt.feup.industrial.mes.service.MesStatisticsService;
import pt.feup.industrial.mes.service.ProductionService;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/ui")
public class MesUiController {

    private final ProductionService productionService;
    private final MesStatisticsService statisticsService;

    @Autowired
    public MesUiController(ProductionService productionService, MesStatisticsService statisticsService) {
        this.productionService = productionService;
        this.statisticsService = statisticsService;
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
        model.addAttribute("machineStats", statisticsService.getMachineStatistics());
        model.addAttribute("toolStats", statisticsService.getToolStatistics());
        model.addAttribute("dockStats", statisticsService.getDockStatistics());
        model.addAttribute("pageTitle", "MES Statistics");

        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        long uptimeMillis = rb.getUptime();
        long uptimeSeconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMillis);

        model.addAttribute("applicationUptimeSeconds", Math.max(1L, uptimeSeconds));

        return "mes_statistics";
    }
}