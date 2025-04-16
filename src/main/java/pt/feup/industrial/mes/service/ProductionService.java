package pt.feup.industrial.mes.service;

import pt.feup.industrial.mes.dto.MesProductionOrderDto;

public interface ProductionService {

    void queueAndProcessProductionOrder(MesProductionOrderDto orderDto);
}
