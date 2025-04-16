package pt.feup.industrial.mes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pt.feup.industrial.mes.model.MesOrderStatus;
import pt.feup.industrial.mes.model.MesOrderStep;
import pt.feup.industrial.mes.repository.MesOrderStepRepository;

import java.util.List;

@Service
public class MesProcessingScheduler {

    private static final Logger log = LoggerFactory.getLogger(MesProcessingScheduler.class);

    private final MesOrderStepRepository mesOrderStepRepository;
    private final ProductionService productionService;

    @Autowired
    public MesProcessingScheduler(MesOrderStepRepository mesOrderStepRepository,
                                  ProductionService productionService) {
        this.mesOrderStepRepository = mesOrderStepRepository;
        this.productionService = productionService;
    }

    /**
     * Periodically checks for MES Order Steps in the RECEIVED state
     * and triggers the asynchronous processing simulation for each one found.
     */
    @Scheduled(fixedRateString = "${mes.scheduling.process-rate-ms:15000}")
    public void schedulePendingMesSteps() {
        log.debug("MES Scheduler: Checking for RECEIVED order steps...");

        List<MesOrderStep> stepsToProcess = mesOrderStepRepository.findByStatus(MesOrderStatus.RECEIVED);

        if (stepsToProcess.isEmpty()) {
            log.debug("MES Scheduler: No RECEIVED steps found.");
            return;
        }

        log.info("MES Scheduler: Found {} RECEIVED step(s). Triggering processing...", stepsToProcess.size());

        for (MesOrderStep step : stepsToProcess) {
            try {
                log.debug("MES Scheduler: Triggering processing for MES Step ID: {}", step.getId());
                productionService.processAndNotifyCompletion(step.getId());
            } catch (Exception e) {
                log.error("MES Scheduler: Failed to trigger processing for MES Step ID {}: {}", step.getId(), e.getMessage());
            }
        }
        log.info("MES Scheduler: Finished triggering {} step(s).", stepsToProcess.size());
    }
}
