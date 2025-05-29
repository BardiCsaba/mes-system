package pt.feup.industrial.mes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.feup.industrial.mes.dto.SchedulingProcessingRequestDto;
import pt.feup.industrial.mes.model.MesOrderStatus;
import pt.feup.industrial.mes.model.MesOrderStep;
import pt.feup.industrial.mes.repository.MesOrderStepRepository;
import pt.feup.industrial.mes.scheduling.SchedulingInterfaceService;

import java.util.List;

@Service
public class MesProcessingScheduler {

    private static final Logger log = LoggerFactory.getLogger(MesProcessingScheduler.class);

    private final MesOrderStepRepository mesOrderStepRepository;
    private final ProductionService productionService;
    private final SchedulingInterfaceService schedulingInterfaceService;

    @Autowired
    public MesProcessingScheduler(MesOrderStepRepository mesOrderStepRepository,
                                  ProductionService productionService, SchedulingInterfaceService schedulingInterfaceService) {
        this.mesOrderStepRepository = mesOrderStepRepository;
        this.productionService = productionService;
        this.schedulingInterfaceService = schedulingInterfaceService;
    }

    @Scheduled(fixedRateString = "${mes.scheduling.process-rate-ms:10000}")
    @Transactional
    public void delegateReceivedStepsToScheduling() {
        log.debug("MES Scheduler: Checking for RECEIVED order steps to send to Scheduling...");
        List<MesOrderStep> stepsToProcess = mesOrderStepRepository.findByStatus(MesOrderStatus.RECEIVED);

        if (stepsToProcess.isEmpty()) {
            log.debug("MES Scheduler: No RECEIVED steps for Scheduling.");
            return;
        }

        log.info("MES Scheduler: Found {} RECEIVED step(s) for Scheduling. Delegating...", stepsToProcess.size());

        for (MesOrderStep step : stepsToProcess) {
            SchedulingProcessingRequestDto requestDto = new SchedulingProcessingRequestDto(
                    step.getId(),
                    step.getErpOrderItemId(),
                    step.getProductType(),
                    step.getRequiredQuantity(),
                    step.getDueDate()
            );

            boolean requestSent = schedulingInterfaceService.requestSchedulingProcessing(requestDto);

            if (requestSent) {
                step.setStatus(MesOrderStatus.SENT_TO_SCHEDULING);
                mesOrderStepRepository.save(step);
                log.info("MES Step ID {} status updated to SENT_TO_SCHEDULING.", step.getId());
            } else {
                log.warn("Failed to send MES Step ID {} to Scheduling. It remains RECEIVED for next attempt.", step.getId());
            }
        }
        log.info("MES Scheduler: Finished delegating {} step(s) to Scheduling.", stepsToProcess.size());
    }
}
