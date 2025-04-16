package pt.feup.industrial.mes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.feup.industrial.mes.model.UnloadingDock;

import java.util.Optional;

public interface UnloadingDockRepository extends JpaRepository<UnloadingDock, Long> {
    Optional<UnloadingDock> findByDockName(String dockName);
}