package pt.feup.industrial.mes.dto;

import lombok.Data;
import pt.feup.industrial.mes.model.MachineState;

import java.util.Map;

@Data
public class MesMachineDisplayStatsDto {
    private String machineName;
    private MachineState mesState; // State as known by MES
    private String displayCurrentToolName; // Could be from MES or PLC

    private long displayOperatingSeconds; // Prioritized: PLC, fallback: MES
    private double occupationPercentage;  // Calculated

    private int mesTotalToolChanges;     // From MES Machine entity
    private int mesCompletedSteps;       // From MES OrderStep aggregation
    private Map<Integer, Long> mesCompletedStepsByType; // ProductType (int) -> Count (long)
    private int mesWorkpiecesFromCompletedSteps;

    private Integer plcCycleCount;
    private Integer plcErrorCount;
}