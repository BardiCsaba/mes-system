package pt.feup.industrial.mes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.feup.industrial.mes.dto.MesProductionOrderDto;
import pt.feup.industrial.mes.dto.OrderItemCompletionDto;
import pt.feup.industrial.mes.erp.ErpClientService;
import pt.feup.industrial.mes.model.MesOrderStatus;
import pt.feup.industrial.mes.model.MesOrderStep;
import pt.feup.industrial.mes.repository.MesOrderStepRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class ProductionServiceImpl implements ProductionService {

    private static final Logger log = LoggerFactory.getLogger(ProductionServiceImpl.class);

    private final ErpClientService erpClientService;
    private final MesOrderStepRepository mesOrderStepRepository;

    @Autowired
    public ProductionServiceImpl(ErpClientService erpClientService, MesOrderStepRepository mesOrderStepRepository) {
        this.erpClientService = erpClientService;
        this.mesOrderStepRepository = mesOrderStepRepository;
    }

    @Async
    public void queueAndProcessProductionOrder(MesProductionOrderDto orderDto) {
        log.info("MES received and queued production request for ERP Order Item ID: {}, Product Type: {}, Quantity: {}",
                orderDto.getErpOrderItemId(), orderDto.getProductType(), orderDto.getQuantity());

        try {
            long processingTimeMs = ThreadLocalRandom.current().nextLong(5000, 15001);
            log.info("Simulating processing for item {} ({} ms)...", orderDto.getErpOrderItemId(), processingTimeMs);
            Thread.sleep(processingTimeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Processing simulation interrupted for item {}", orderDto.getErpOrderItemId());
            return;
        }
        log.info("Simulated processing COMPLETE for item {}", orderDto.getErpOrderItemId());

        OrderItemCompletionDto completionInfo = new OrderItemCompletionDto(
                orderDto.getErpOrderItemId(),
                LocalDateTime.now(),
                orderDto.getQuantity()
        );
        erpClientService.notifyErpItemCompleted(completionInfo);
    }

    @Transactional
    public void receiveAndStoreErpOrder(MesProductionOrderDto orderDto) {
        log.info("Received production order from ERP for ERP Order Item ID: {}", orderDto.getErpOrderItemId());

        Optional<MesOrderStep> existingStep = mesOrderStepRepository.findByErpOrderItemId(orderDto.getErpOrderItemId());
        if (existingStep.isPresent()) {
            String warningMessage = String.format("MES Order Step for ERP Order Item ID %d already exists (current status: %s). Ignoring duplicate request.",
                    orderDto.getErpOrderItemId(), existingStep.get().getStatus());
            log.warn(warningMessage);
            return;
        }

        MesOrderStep newStep = new MesOrderStep();
        newStep.setErpClientOrderId(orderDto.getErpOrderId());
        newStep.setErpOrderItemId(orderDto.getErpOrderItemId());
        newStep.setProductType(orderDto.getProductType());
        newStep.setRequiredQuantity(orderDto.getQuantity());
        newStep.setDueDate(orderDto.getDueDate());
        newStep.setStatus(MesOrderStatus.RECEIVED);

        try {
            MesOrderStep savedStep = mesOrderStepRepository.save(newStep);
            log.info("Successfully saved new MES Order Step with ID: {} (Status: {}) for ERP Order Item ID: {}",
                    savedStep.getId(), savedStep.getStatus(), savedStep.getErpOrderItemId());

            // ---- FOR TESTING ----
            //processAndNotifyCompletion(savedStep.getId());
            // -----------------------

        } catch (Exception e) {
            log.error("Failed to save MES Order Step for ERP Order Item ID {}: {}",
                    orderDto.getErpOrderItemId(), e.getMessage(), e);
            throw new RuntimeException("Database error saving MES order step.", e);
        }
    }

    @Async
    @Transactional
    public void processAndNotifyCompletion(Long mesOrderStepId) {
        Optional<MesOrderStep> stepOpt = mesOrderStepRepository.findById(mesOrderStepId);

        if (stepOpt.isEmpty()) {
            log.error("Processing Error: MesOrderStep with ID {} not found.", mesOrderStepId);
            return;
        }
        MesOrderStep step = stepOpt.get();

        if (step.getStatus() != MesOrderStatus.RECEIVED && step.getStatus() != MesOrderStatus.SCHEDULED) {
            log.warn("Processing Warning: Step {} is not in a processable state (current state: {}). Skipping simulation.",
                    mesOrderStepId, step.getStatus());
            return;
        }

        log.info("Starting processing for MES Step ID: {} (ERP Item ID: {})", step.getId(), step.getErpOrderItemId());

        step.setStatus(MesOrderStatus.IN_PROGRESS);
        step.setStartProcessingTimestamp(LocalDateTime.now());
        mesOrderStepRepository.save(step);
        log.debug("Updated MES Step ID {} status to IN_PROGRESS", step.getId());

        boolean processingSuccess = true;
        try {
            long processingTimeMs = ThreadLocalRandom.current().nextLong(5000, 20001);
            log.info("Simulating processing for MES Step ID {} ({} ms)...", step.getId(), processingTimeMs);
            Thread.sleep(processingTimeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Processing interrupted for MES Step ID {}", step.getId());

            step.setStatus(MesOrderStatus.FAILED);
            step.setCompletionTimestamp(LocalDateTime.now());
            mesOrderStepRepository.save(step);

            return;
        }

        LocalDateTime completionTime = LocalDateTime.now();
        step.setCompletionTimestamp(completionTime);

        if (processingSuccess) {
            log.info("Processing COMPLETE for MES Step ID {}", step.getId());
            step.setStatus(MesOrderStatus.COMPLETED);
            mesOrderStepRepository.save(step);

            OrderItemCompletionDto completionInfo = new OrderItemCompletionDto(
                    step.getErpOrderItemId(),
                    completionTime,
                    step.getRequiredQuantity()
            );
            erpClientService.notifyErpItemCompleted(completionInfo);

        } else {
            log.warn("Processing FAILED for MES Step ID {}", step.getId());
            step.setStatus(MesOrderStatus.FAILED);
            mesOrderStepRepository.save(step);
        }
    }

    public List<MesOrderStep> getAllOrderSteps() {
        log.debug("Fetching all MES Order Steps from the database...");
        return mesOrderStepRepository.findAll(Sort.by(Sort.Direction.DESC, "receivedTimestamp"));
    }
}