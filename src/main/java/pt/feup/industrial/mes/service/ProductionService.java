package pt.feup.industrial.mes.service;

import pt.feup.industrial.mes.dto.MesProductionOrderDto;

public interface ProductionService {

    void queueAndProcessProductionOrder(MesProductionOrderDto orderDto);
    public void receiveAndStoreErpOrder(MesProductionOrderDto orderDto);
    public void processAndNotifyCompletion(Long mesOrderStepId);
}
