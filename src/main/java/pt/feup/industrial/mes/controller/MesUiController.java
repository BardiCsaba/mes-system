package pt.feup.industrial.mes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pt.feup.industrial.mes.model.MesOrderStep;
import pt.feup.industrial.mes.service.ProductionService;

import java.util.List;

@Controller
@RequestMapping("/ui")
public class MesUiController {

    private final ProductionService productionService;

    @Autowired
    public MesUiController(ProductionService productionService) {
        this.productionService = productionService;
    }

    @GetMapping("/dashboard") // Maps requests to http://localhost:8081/ui/dashboard
    public String showDashboard(Model model) {
        List<MesOrderStep> orderSteps = productionService.getAllOrderSteps();

        model.addAttribute("orderSteps", orderSteps);

        return "mes_dashboard"; // Spring Boot + Thymeleaf will look for src/main/resources/templates/mes_dashboard.html
    }
}