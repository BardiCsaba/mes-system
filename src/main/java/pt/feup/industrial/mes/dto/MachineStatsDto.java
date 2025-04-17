package pt.feup.industrial.mes.dto;

import lombok.Data;
import pt.feup.industrial.mes.model.MachineState;
import java.util.Map;

@Data
public class MachineStatsDto {
    private Long machineId;
    private String machineName;
    private MachineState currentState;
    private String currentToolName;
    private long totalOperatingSeconds;
    private int totalToolChanges;
    private int totalWorkpiecesProcessed;
    private int totalCompletedSteps;
    private Map<Integer, Long> completedStepsByType;
}