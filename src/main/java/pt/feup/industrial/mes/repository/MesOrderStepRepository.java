package pt.feup.industrial.mes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.feup.industrial.mes.model.Machine;
import pt.feup.industrial.mes.model.MesOrderStatus;
import pt.feup.industrial.mes.model.MesOrderStep;
import pt.feup.industrial.mes.model.UnloadingDock;

import java.util.List;
import java.util.Optional;

public interface MesOrderStepRepository extends JpaRepository<MesOrderStep, Long> {
    Optional<MesOrderStep> findByErpOrderItemId(Long erpOrderItemId);
    List<MesOrderStep> findByErpClientOrderId(Long erpClientOrderId);
    List<MesOrderStep> findByStatus(MesOrderStatus status);
    List<MesOrderStep> findByAssignedMachine_MachineName(String machineName);
    List<MesOrderStep> findByAssignedDockAndStatus(UnloadingDock dock, MesOrderStatus status);
    List<MesOrderStep> findByAssignedMachineAndStatus(Machine assignedMachine, MesOrderStatus status);
    long countByAssignedDockAndStatus(UnloadingDock dock, MesOrderStatus status); // For capacity checks?

}