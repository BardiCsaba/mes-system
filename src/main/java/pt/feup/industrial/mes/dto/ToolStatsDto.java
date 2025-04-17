package pt.feup.industrial.mes.dto;

import lombok.Data;

@Data
public class ToolStatsDto {
    private String toolName;
    private long totalUsageSeconds;
}
