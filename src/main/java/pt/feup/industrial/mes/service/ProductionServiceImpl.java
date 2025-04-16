package pt.feup.industrial.mes.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pt.feup.industrial.mes.dto.MesProductionOrderDto;
import pt.feup.industrial.mes.dto.OrderItemCompletionDto;
import pt.feup.industrial.mes.erp.ErpClientService;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class ProductionServiceImpl implements ProductionService {

    private static final Logger log = LoggerFactory.getLogger(ProductionServiceImpl.class);

    private final ErpClientService erpClientService;

    @Autowired
    public ProductionServiceImpl(ErpClientService erpClientService) {
        this.erpClientService = erpClientService;
    }

    @Async
    public void queueAndProcessProductionOrder(MesProductionOrderDto orderDto) {
        log.info("MES received and queued production request for ERP Order Item ID: {}, Product Type: {}, Quantity: {}",
                orderDto.getErpOrderItemId(), orderDto.getProductType(), orderDto.getQuantity());

        try {
            long processingTimeMs = ThreadLocalRandom.current().nextLong(5000, 15001);
            log.info("Simulating processing for item {} ({} ms)...", orderDto.getErpOrderItemId(), processingTimeMs);
            Thread.sleep(processingTimeMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Processing simulation interrupted for item {}", orderDto.getErpOrderItemId());
            return;
        }
        log.info("Simulated processing COMPLETE for item {}", orderDto.getErpOrderItemId());

        OrderItemCompletionDto completionInfo = new OrderItemCompletionDto(
                orderDto.getErpOrderItemId(),
                LocalDateTime.now(),
                orderDto.getQuantity()
        );
        erpClientService.notifyErpItemCompleted(completionInfo);
    }
}