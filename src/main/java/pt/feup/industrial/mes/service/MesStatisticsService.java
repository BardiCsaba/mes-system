package pt.feup.industrial.mes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.feup.industrial.mes.dto.DockStatsDto;
import pt.feup.industrial.mes.dto.MachineStatsDto;
import pt.feup.industrial.mes.dto.ToolStatsDto;
import pt.feup.industrial.mes.model.Machine;
import pt.feup.industrial.mes.model.MesOrderStatus;
import pt.feup.industrial.mes.model.MesOrderStep;
import pt.feup.industrial.mes.model.Tool;
import pt.feup.industrial.mes.model.UnloadingDock;
import pt.feup.industrial.mes.repository.MachineRepository;
import pt.feup.industrial.mes.repository.MesOrderStepRepository;
import pt.feup.industrial.mes.repository.ToolRepository;
import pt.feup.industrial.mes.repository.UnloadingDockRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MesStatisticsService {

    private final MachineRepository machineRepository;
    private final ToolRepository toolRepository;
    private final UnloadingDockRepository unloadingDockRepository;
    private final MesOrderStepRepository mesOrderStepRepository;

    @Autowired
    public MesStatisticsService(MachineRepository machineRepository, ToolRepository toolRepository, UnloadingDockRepository unloadingDockRepository, MesOrderStepRepository mesOrderStepRepository) {
        this.machineRepository = machineRepository;
        this.toolRepository = toolRepository;
        this.unloadingDockRepository = unloadingDockRepository;
        this.mesOrderStepRepository = mesOrderStepRepository;
    }

    @Transactional(readOnly = true)
    public List<MachineStatsDto> getMachineStatistics() {
        List<Machine> machines = machineRepository.findAll();
        return machines.stream().map(this::mapToMachineStatsDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ToolStatsDto> getToolStatistics() {
        List<Tool> tools = toolRepository.findAll();
        return tools.stream().map(this::mapToToolStatsDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DockStatsDto> getDockStatistics() {
        List<UnloadingDock> docks = unloadingDockRepository.findAll();
        return docks.stream().map(this::mapToDockStatsDto).collect(Collectors.toList());
    }

    private MachineStatsDto mapToMachineStatsDto(Machine machine) {
        MachineStatsDto dto = new MachineStatsDto();
        dto.setMachineId(machine.getId());
        dto.setMachineName(machine.getMachineName());
        dto.setCurrentState(machine.getState());
        dto.setCurrentToolName(machine.getCurrentTool() != null ? machine.getCurrentTool().getToolName() : "-");
        dto.setTotalOperatingSeconds(machine.getTotalOperatingSeconds());
        dto.setTotalToolChanges(machine.getTotalToolChanges());
        dto.setTotalWorkpiecesProcessed(machine.getTotalWorkpiecesProcessed());

        List<MesOrderStep> completedSteps = mesOrderStepRepository.findByAssignedMachineAndStatus(machine, MesOrderStatus.COMPLETED);
        dto.setTotalCompletedSteps(completedSteps.size());
        dto.setCompletedStepsByType(
                completedSteps.stream()
                        .collect(Collectors.groupingBy(MesOrderStep::getProductType, Collectors.counting()))
        );

        return dto;
    }

    private ToolStatsDto mapToToolStatsDto(Tool tool) {
        ToolStatsDto dto = new ToolStatsDto();
        dto.setToolName(tool.getToolName());
        dto.setTotalUsageSeconds(tool.getTotalUsageSeconds());
        return dto;
    }

    private DockStatsDto mapToDockStatsDto(UnloadingDock dock) {
        DockStatsDto dto = new DockStatsDto();
        dto.setDockName(dock.getDockName());

        List<MesOrderStep> unloadedSteps = mesOrderStepRepository.findByAssignedDockAndStatus(dock, MesOrderStatus.COMPLETED);
        dto.setTotalUnloadedWorkpieces(unloadedSteps.size());
        dto.setUnloadedWorkpiecesByType(
                unloadedSteps.stream()
                        .collect(Collectors.groupingBy(MesOrderStep::getProductType, Collectors.counting()))
        );

        return dto;
    }
}