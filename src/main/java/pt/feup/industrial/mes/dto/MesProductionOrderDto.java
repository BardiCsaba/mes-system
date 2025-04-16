package pt.feup.industrial.mes.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesProductionOrderDto {

    @NotNull
    private Long erpOrderId;

    @NotNull
    private Long erpOrderItemId;

    @NotNull
    private Integer productType;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private LocalDate dueDate;
}