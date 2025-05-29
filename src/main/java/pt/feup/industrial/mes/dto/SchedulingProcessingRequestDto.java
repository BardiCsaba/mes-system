package pt.feup.industrial.mes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingProcessingRequestDto {
    private Long mesOrderStepId;
    private Long erpOrderItemId;
    private Integer targetProductType; // e.g., 5 for P5
    private Integer quantity;
    private LocalDate dueDate;
}
