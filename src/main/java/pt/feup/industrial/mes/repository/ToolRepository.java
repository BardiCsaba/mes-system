package pt.feup.industrial.mes.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import pt.feup.industrial.mes.model.Tool;

import java.util.Optional;

public interface ToolRepository extends JpaRepository<Tool, Long> {
    Optional<Tool> findByToolName(String toolName);
}
