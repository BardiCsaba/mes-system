package pt.feup.industrial.mes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.feup.industrial.mes.statistics.PlcStatisticsCache;
import pt.feup.industrial.mes.dto.MesDockDisplayStatsDto;
import pt.feup.industrial.mes.dto.MesMachineDisplayStatsDto;
import pt.feup.industrial.mes.dto.MesToolDisplayStatsDto;
import pt.feup.industrial.mes.dto.PlcMachineDataDto;
import pt.feup.industrial.mes.dto.PlcToolDataDto;
import pt.feup.industrial.mes.model.Machine;
import pt.feup.industrial.mes.model.MesOrderStep;
import pt.feup.industrial.mes.model.Tool;
import pt.feup.industrial.mes.model.UnloadingDock;
import pt.feup.industrial.mes.model.MesOrderStatus;
import pt.feup.industrial.mes.repository.MachineRepository;
import pt.feup.industrial.mes.repository.MesOrderStepRepository;
import pt.feup.industrial.mes.repository.ToolRepository;
import pt.feup.industrial.mes.repository.UnloadingDockRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MesDisplayStatisticsService {
    private static final Logger log = LoggerFactory.getLogger(MesDisplayStatisticsService.class);

    private final MachineRepository machineRepository;
    private final ToolRepository toolRepository;
    private final UnloadingDockRepository unloadingDockRepository;
    private final MesOrderStepRepository mesOrderStepRepository;
    private final PlcStatisticsCache plcStatisticsCache;

    @Autowired
    public MesDisplayStatisticsService(MachineRepository machineRepository, ToolRepository toolRepository,
                                       UnloadingDockRepository unloadingDockRepository, MesOrderStepRepository mesOrderStepRepository,
                                       PlcStatisticsCache plcStatisticsCache) {
        this.machineRepository = machineRepository;
        this.toolRepository = toolRepository;
        this.unloadingDockRepository = unloadingDockRepository;
        this.mesOrderStepRepository = mesOrderStepRepository;
        this.plcStatisticsCache = plcStatisticsCache;
    }

    @Transactional(readOnly = true)
    public List<MesMachineDisplayStatsDto> getCombinedMachineStatistics() {
        List<Machine> mesMachines = machineRepository.findAll();
        Map<String, PlcMachineDataDto> plcDataMap = plcStatisticsCache.getCachedMachineStats();

        return mesMachines.stream().map(mesMachine -> {
            MesMachineDisplayStatsDto dto = new MesMachineDisplayStatsDto();
            dto.setMachineName(mesMachine.getMachineName());
            dto.setMesState(mesMachine.getState());
            dto.setDisplayCurrentToolName(mesMachine.getCurrentTool() != null ? mesMachine.getCurrentTool().getToolName() : "-");
            dto.setMesTotalToolChanges(mesMachine.getTotalToolChanges());

            List<MesOrderStep> completedSteps = mesOrderStepRepository.findByAssignedMachineAndStatus(mesMachine, MesOrderStatus.COMPLETED);
            dto.setMesCompletedSteps(completedSteps.size());
            dto.setMesCompletedStepsByType(
                    completedSteps.stream().collect(Collectors.groupingBy(MesOrderStep::getProductType, Collectors.counting()))
            );
            dto.setMesWorkpiecesFromCompletedSteps(
                    completedSteps.stream().mapToInt(MesOrderStep::getRequiredQuantity).sum()
            );

            PlcMachineDataDto plcMachineData = plcDataMap.get(mesMachine.getMachineName());
            if (plcMachineData != null) {
                dto.setDisplayOperatingSeconds(plcMachineData.getActiveTimeSeconds());
                dto.setDisplayCurrentToolName(plcMachineData.getCurrentToolId() != null ? plcMachineData.getCurrentToolId() : dto.getDisplayCurrentToolName());
                dto.setPlcCycleCount(plcMachineData.getCycleCount());
                dto.setPlcErrorCount(plcMachineData.getErrorCount());
            } else {
                dto.setDisplayOperatingSeconds(mesMachine.getTotalOperatingSeconds());
                dto.setDisplayCurrentToolName(dto.getDisplayCurrentToolName());
                log.trace("No PLC data found in cache for machine {}", mesMachine.getMachineName());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MesToolDisplayStatsDto> getCombinedToolStatistics() {
        List<Tool> mesTools = toolRepository.findAll();
        Map<String, PlcToolDataDto> plcDataMap = plcStatisticsCache.getCachedToolStats();

        return mesTools.stream().map(mesTool -> {
            MesToolDisplayStatsDto dto = new MesToolDisplayStatsDto();
            dto.setToolName(mesTool.getToolName());

            PlcToolDataDto plcToolData = plcDataMap.get(mesTool.getToolName());
            if (plcToolData != null) {
                dto.setDisplayTotalUsageSeconds(plcToolData.getUsageSecondsOnPlc());
                dto.setPlcUsageCycles(plcToolData.getUsageCyclesOnPlc());
            } else {
                dto.setDisplayTotalUsageSeconds(mesTool.getTotalUsageSeconds());
                log.trace("No PLC data found in cache for tool {}", mesTool.getToolName());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MesDockDisplayStatsDto> getDockStatistics() {
        List<UnloadingDock> docks = unloadingDockRepository.findAll();
        return docks.stream().map(dock -> {
            MesDockDisplayStatsDto dto = new MesDockDisplayStatsDto();
            dto.setDockName(dock.getDockName());
            List<MesOrderStep> unloadedSteps = mesOrderStepRepository.findByAssignedDockAndStatus(dock, MesOrderStatus.COMPLETED);
            dto.setTotalUnloadedWorkpieces(
                    unloadedSteps.stream().mapToLong(MesOrderStep::getRequiredQuantity).sum()
            );
            dto.setUnloadedWorkpiecesByType(
                    unloadedSteps.stream()
                            .collect(Collectors.groupingBy(MesOrderStep::getProductType, Collectors.summingLong(MesOrderStep::getRequiredQuantity))) // Sum quantities
            );
            return dto;
        }).collect(Collectors.toList());
    }

    public LocalDateTime getLastMachineStatsRefreshTime() { return plcStatisticsCache.getLastMachineStatsRefreshTime(); }
    public LocalDateTime getLastToolStatsRefreshTime() { return plcStatisticsCache.getLastToolStatsRefreshTime(); }
}