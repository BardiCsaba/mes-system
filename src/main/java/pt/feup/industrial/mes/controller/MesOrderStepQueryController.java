package pt.feup.industrial.mes.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.feup.industrial.mes.model.MesOrderStatus;
import pt.feup.industrial.mes.model.MesOrderStep;
import pt.feup.industrial.mes.repository.MesOrderStepRepository;

import java.util.List;

@RestController
@RequestMapping("/api/mes/order-steps")
public class MesOrderStepQueryController {
    @Autowired
    private MesOrderStepRepository repository;

    @GetMapping
    public List<MesOrderStep> getAllSteps(
            @RequestParam(required = false) MesOrderStatus status,
            @RequestParam(required = false) Long erpClientOrderId,
            @RequestParam(required = false) String machineName
    ) {
        if (status != null) return repository.findByStatus(status);
        if (erpClientOrderId != null) return repository.findByErpClientOrderId(erpClientOrderId);
        if (machineName != null) return repository.findByAssignedMachine_MachineName(machineName);
        return repository.findAll();
    }

    @GetMapping("/{mesStepId}")
    public ResponseEntity<MesOrderStep> getStepById(@PathVariable Long mesStepId) {
        return repository.findById(mesStepId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-erp-item/{erpOrderItemId}")
    public ResponseEntity<MesOrderStep> getStepByErpItemId(@PathVariable Long erpOrderItemId) {
        return repository.findByErpOrderItemId(erpOrderItemId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}