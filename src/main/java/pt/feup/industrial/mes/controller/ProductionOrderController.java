package pt.feup.industrial.mes.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.feup.industrial.mes.dto.MesProductionOrderDto;
import pt.feup.industrial.mes.service.ProductionService;

@RestController
@RequestMapping("/api/production-orders")
public class ProductionOrderController {

    private static final Logger log = LoggerFactory.getLogger(ProductionOrderController.class);
    private final ProductionService productionService;

    @Autowired
    public ProductionOrderController(ProductionService productionService) {
        this.productionService = productionService;
    }

    @PostMapping
    public ResponseEntity<Void> receiveProductionOrder(@Valid @RequestBody MesProductionOrderDto orderRequest) {
        try {
            log.info("Received production order request via REST: {}", orderRequest);
            productionService.queueProductionOrder(orderRequest);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error processing production order request: {}", orderRequest, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}