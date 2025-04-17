package pt.feup.industrial.mes.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DockStatsDto {
    private String dockName;
    private long totalUnloadedWorkpieces;
    private Map<Integer, Long> unloadedWorkpiecesByType; // Key: ProductType, Value: Count
}
