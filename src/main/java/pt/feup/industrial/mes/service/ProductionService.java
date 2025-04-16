package pt.feup.industrial.mes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pt.feup.industrial.mes.dto.MesProductionOrderDto;

@Service
public class ProductionService {

    private static final Logger log = LoggerFactory.getLogger(ProductionService.class);

    public void queueProductionOrder(MesProductionOrderDto orderDto) {
        // TODO: Implement actual MES logic here:
        // - Validate the request further (e.g., check if productType is valid)
        // - Store the production request in the MES database
        // - Add it to a production queue or schedule
        // - Interact with PLCs/SCADA eventually

        log.info("MES received and queued production request for ERP Order Item ID: {}, Product Type: {}, Quantity: {}",
                orderDto.getErpOrderItemId(), orderDto.getProductType(), orderDto.getQuantity());

        // Simulate some processing time if needed
        // try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}