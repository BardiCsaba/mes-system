package pt.feup.industrial.mes.service;

import pt.feup.industrial.mes.dto.MesProductionOrderDto;
import pt.feup.industrial.mes.dto.SchedulingStepUpdateDto;
import pt.feup.industrial.mes.model.MesOrderStep;

import java.util.List;

public interface ProductionService {

    public void receiveAndStoreErpOrder(MesProductionOrderDto orderDto);
    List<MesOrderStep> getAllOrderSteps();
    void updateStepStatusFromScheduling(SchedulingStepUpdateDto updateDto);
}
