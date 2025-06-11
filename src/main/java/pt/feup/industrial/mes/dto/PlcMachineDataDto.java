package pt.feup.industrial.mes.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlcMachineDataDto {
    private String machineName; // Key to match with MES Machine
    private long activeTimeSeconds;
    private int cycleCount;
    private int errorCount;
    private String currentToolId; // e.g., "T1", "T2"
    private Map<String, Integer> workpiecesProducedInLastOperation; // e.g., {"P5": 1, "P7": 0}
}