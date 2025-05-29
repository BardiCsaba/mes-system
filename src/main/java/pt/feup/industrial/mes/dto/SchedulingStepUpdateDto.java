package pt.feup.industrial.mes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingStepUpdateDto {
    private Long mesOrderStepId;
    private Long erpOrderItemId;
    private String status;
    private LocalDateTime timestamp;
    private String errorMessage;
}
