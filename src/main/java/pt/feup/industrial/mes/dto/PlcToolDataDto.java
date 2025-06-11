package pt.feup.industrial.mes.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlcToolDataDto {
    private String toolName; // Key to match with MES Tool
    private long usageSecondsOnPlc; // Total time this tool was active according to PLC
    private int usageCyclesOnPlc;   // Total cycles this tool was used for on PLC
}
