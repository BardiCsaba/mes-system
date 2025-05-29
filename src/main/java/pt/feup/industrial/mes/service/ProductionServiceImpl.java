package pt.feup.industrial.mes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.feup.industrial.mes.dto.MesProductionOrderDto;
import pt.feup.industrial.mes.dto.OrderItemCompletionDto;
import pt.feup.industrial.mes.dto.SchedulingStepUpdateDto;
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
    public void updateStepStatusFromScheduling(SchedulingStepUpdateDto updateDto) {
        Optional<MesOrderStep> stepOpt = mesOrderStepRepository.findById(updateDto.getMesOrderStepId());
        if (stepOpt.isEmpty()) {
            log.error("Scheduling callback for unknown MES Step ID: {}. ERP Item ID: {}",
                    updateDto.getMesOrderStepId(), updateDto.getErpOrderItemId());
            return;
        }
        MesOrderStep step = stepOpt.get();

        MesOrderStatus newStatus;
        if ("COMPLETED".equalsIgnoreCase(updateDto.getStatus())) {
            newStatus = MesOrderStatus.COMPLETED;
            step.setCompletionTimestamp(updateDto.getTimestamp());
            log.info("MES Step ID {} (ERP Item ID {}) reported as COMPLETED by Scheduling.",
                    step.getId(), step.getErpOrderItemId());
        } else if ("FAILED".equalsIgnoreCase(updateDto.getStatus())) {
            newStatus = MesOrderStatus.SCHEDULING_FAILED;
            step.setCompletionTimestamp(updateDto.getTimestamp());
            log.warn("MES Step ID {} (ERP Item ID {}) reported as FAILED by Scheduling. Reason: {}",
                    step.getId(), step.getErpOrderItemId(), updateDto.getErrorMessage());
        } else {
            log.warn("Received unknown status '{}' from Scheduling for MES Step ID {}. Ignoring.",
                    updateDto.getStatus(), step.getId());
            return;
        }

        step.setStatus(newStatus);
        mesOrderStepRepository.save(step);

        if (newStatus == MesOrderStatus.COMPLETED) {
            OrderItemCompletionDto completionInfo = new OrderItemCompletionDto(
                    step.getErpOrderItemId(),
                    step.getCompletionTimestamp(), // Use the timestamp from Scheduling/step
                    step.getRequiredQuantity()
            );
            erpClientService.notifyErpItemCompleted(completionInfo);
        } else {
            log.warn("ERP notification for FAILED step {} (ERP Item ID {}) not yet implemented.",
                    step.getId(), step.getErpOrderItemId());
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