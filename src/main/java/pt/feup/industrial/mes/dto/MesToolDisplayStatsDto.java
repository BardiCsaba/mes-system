package pt.feup.industrial.mes.dto;

import lombok.Data;

@Data
public class MesToolDisplayStatsDto {
    private String toolName;
    private long displayTotalUsageSeconds; // Prioritized: PLC, fallback: MES
    private int plcUsageCycles;
}