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
import pt.feup.industrial.mes.model.Machine;
import pt.feup.industrial.mes.model.MachineState;
import pt.feup.industrial.mes.model.MesOrderStatus;
import pt.feup.industrial.mes.model.MesOrderStep;
import pt.feup.industrial.mes.model.Tool;
import pt.feup.industrial.mes.repository.MachineRepository;
import pt.feup.industrial.mes.repository.MesOrderStepRepository;
import pt.feup.industrial.mes.repository.ToolRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class ProductionServiceImpl implements ProductionService {

    private static final Logger log = LoggerFactory.getLogger(ProductionServiceImpl.class);

    private final ErpClientService erpClientService;
    private final MesOrderStepRepository mesOrderStepRepository;
    private final MachineRepository machineRepository;
    private final ToolRepository toolRepository;

    @Autowired
    public ProductionServiceImpl(ErpClientService erpClientService, MesOrderStepRepository mesOrderStepRepository, MachineRepository machineRepository, ToolRepository toolRepository) {
        this.erpClientService = erpClientService;
        this.mesOrderStepRepository = mesOrderStepRepository;
        this.machineRepository = machineRepository;
        this.toolRepository = toolRepository;
    }

    @Async
    @Transactional
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
        MesOrderStep step = stepOpt.get();

        // THIS ASSUMES A MACHINE NAMED 'M1a' EXISTS IN DB
        // For testing purposes, we are hardcoding the machine name.
        Machine assignedMachine = machineRepository.findByMachineName("M1a").orElse(null);
        if (assignedMachine == null) {
            log.error("Cannot simulate: Machine M1a not found for step {}", step.getId());
            step.setStatus(MesOrderStatus.FAILED);
            mesOrderStepRepository.save(step);
            // Notify ERP of failure?
            return;
        }

        if (step.getAssignedMachine() == null) {
            step.setAssignedMachine(assignedMachine);
        }

        // Let's assume T1 is needed for product type 5.
        Tool requiredTool = null;
        if (step.getProductType() == 5) {
            requiredTool = toolRepository.findByToolName("T1").orElse(null);
        }
        // Add logic for other types/tools...

        if (requiredTool != null && (assignedMachine.getCurrentTool() == null || !assignedMachine.getCurrentTool().equals(requiredTool))) {
            changeToolAndUpdateStats(assignedMachine, requiredTool);
        }

        if (step.getStatus() == MesOrderStatus.RECEIVED || step.getStatus() == MesOrderStatus.SCHEDULED) {
            step.setStatus(MesOrderStatus.IN_PROGRESS);
            step.setStartProcessingTimestamp(LocalDateTime.now());
            mesOrderStepRepository.save(step);
            updateMachineStateAndStats(assignedMachine, MachineState.EXECUTING);
            log.info("Machine {} started executing MES Step ID {}", assignedMachine.getMachineName(), step.getId());
        } else {
            log.warn("Step {} not in RECEIVED/SCHEDULED state (state={}). Skipping simulation.", step.getId(), step.getStatus());
            return;
        }

        boolean processingSuccess = true;
        long processingTimeMs = 0;
        try {
            processingTimeMs = ThreadLocalRandom.current().nextLong(5000, 20001);
            log.info("Simulating processing for MES Step ID {} ({} ms)...", step.getId(), processingTimeMs);
            Thread.sleep(processingTimeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Processing simulation interrupted for MES Step ID {}", step.getId());
            processingSuccess = false;
        }

        LocalDateTime completionTime = LocalDateTime.now();
        step.setCompletionTimestamp(completionTime);
        Tool toolUsed = assignedMachine.getCurrentTool();

        if (processingSuccess) {
            step.setStatus(MesOrderStatus.COMPLETED);
            updateMachineStateAndStats(assignedMachine, MachineState.IDLE);

            if (toolUsed != null) {
                updateToolUsageStats(toolUsed, Duration.ofMillis(processingTimeMs).toSeconds());
            }

            assignedMachine.setTotalWorkpiecesProcessed(assignedMachine.getTotalWorkpiecesProcessed() + 1);
            machineRepository.save(assignedMachine);

            mesOrderStepRepository.save(step);
            log.info("Machine {} finished executing MES Step ID {}. Status -> COMPLETED", assignedMachine.getMachineName(), step.getId());

            OrderItemCompletionDto completionInfo = new OrderItemCompletionDto(step.getErpOrderItemId(), completionTime, step.getRequiredQuantity());
            erpClientService.notifyErpItemCompleted(completionInfo);
        } else {
            step.setStatus(MesOrderStatus.FAILED);
            updateMachineStateAndStats(assignedMachine, MachineState.FAILED); // Or IDLE? Depends on failure type

            mesOrderStepRepository.save(step);
            log.warn("Machine {} failed executing MES Step ID {}. Status -> FAILED", assignedMachine.getMachineName(), step.getId());
            // Notify ERP of failure
        }
    }

    @Transactional
    protected void updateMachineStateAndStats(Machine machine, MachineState newState) {
        MachineState oldState = machine.getState();
        if (oldState == newState) return;

        LocalDateTime now = LocalDateTime.now();
        machine.setState(newState);
        machine.setLastStateChangeTimestamp(now);

        if (oldState == MachineState.EXECUTING && machine.getLastExecutionStartTime() != null) {
            Duration executionDuration = Duration.between(machine.getLastExecutionStartTime(), now);
            long secondsToAdd = executionDuration.toSeconds();
            if (secondsToAdd > 0) {
                machine.setTotalOperatingSeconds(machine.getTotalOperatingSeconds() + secondsToAdd);
                log.debug("Added {} seconds to Machine {} total operating time.", secondsToAdd, machine.getMachineName());
            }
            machine.setLastExecutionStartTime(null);
        }

        if (newState == MachineState.EXECUTING) {
            machine.setLastExecutionStartTime(now);
        }

        machineRepository.save(machine);
        log.debug("Updated Machine {} state from {} to {}", machine.getMachineName(), oldState, newState);
    }

    @Transactional
    protected void changeToolAndUpdateStats(Machine machine, Tool newTool) {
        Tool oldTool = machine.getCurrentTool();
        if (newTool != null && !newTool.equals(oldTool)) {

            if (machine.getState() == MachineState.EXECUTING) {
                log.warn("Tool change requested while machine {} is executing. Stopping execution first.", machine.getMachineName());
                updateMachineStateAndStats(machine, MachineState.IDLE);
            }

            machine.setCurrentTool(newTool);
            machine.setTotalToolChanges(machine.getTotalToolChanges() + 1);
            machineRepository.save(machine);
            log.info("Changed tool on Machine {} to {}. Total changes: {}",
                    machine.getMachineName(), newTool.getToolName(), machine.getTotalToolChanges());
        } else if (newTool == null && oldTool != null) {
            machine.setCurrentTool(null);
            machineRepository.save(machine);
            log.info("Removed tool from Machine {}", machine.getMachineName());
        }
    }

    @Transactional
    protected void updateToolUsageStats(Tool tool, long secondsToAdd) {
        if (tool != null && secondsToAdd > 0) {
            tool.setTotalUsageSeconds(tool.getTotalUsageSeconds() + secondsToAdd);
            toolRepository.save(tool);
            log.debug("Added {} seconds to Tool {} total usage time.", secondsToAdd, tool.getToolName());
        }
    }

    public List<MesOrderStep> getAllOrderSteps() {
        log.debug("Fetching all MES Order Steps from the database...");
        return mesOrderStepRepository.findAll(Sort.by(Sort.Direction.DESC, "receivedTimestamp"));
    }
}