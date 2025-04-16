package pt.feup.industrial.mes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.feup.industrial.mes.model.Machine;
import pt.feup.industrial.mes.model.MachineState;


import java.util.List;
import java.util.Optional;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    Optional<Machine> findByMachineName(String machineName);
    List<Machine> findByState(MachineState state);
}
