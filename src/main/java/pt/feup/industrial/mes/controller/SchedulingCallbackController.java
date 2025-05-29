package pt.feup.industrial.mes.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.feup.industrial.mes.dto.SchedulingStepUpdateDto;
import pt.feup.industrial.mes.service.ProductionService;

@RestController
@RequestMapping("/api/mes/scheduling-callback")
public class SchedulingCallbackController {
    private static final Logger log = LoggerFactory.getLogger(SchedulingCallbackController.class);

    private final ProductionService productionService;

    @Autowired
    public SchedulingCallbackController(ProductionService productionService) {
        this.productionService = productionService;
    }

    @PostMapping("/step-update")
    public ResponseEntity<Void> handleStepUpdate(@Valid @RequestBody SchedulingStepUpdateDto updateDto) {
        log.info("Received update from Scheduling service for MES Step ID {}: Status - {}",
                updateDto.getMesOrderStepId(), updateDto.getStatus());
        try {
            productionService.updateStepStatusFromScheduling(updateDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing Scheduling service step update for MES Step ID {}: {}",
                    updateDto.getMesOrderStepId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
