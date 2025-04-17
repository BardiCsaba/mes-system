package pt.feup.industrial.mes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Represents a processing tool (T1-T6).
 */
@Entity
@Table(name = "tools")
@Getter
@Setter
@NoArgsConstructor
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "tool_name", nullable = false, unique = true) // e.g., "T1", "T2"
    private String toolName;

    @Column(name = "total_usage_seconds", nullable = false, columnDefinition = "bigint default 0")
    private long totalUsageSeconds = 0L;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tool tool = (Tool) o;
        return id != null ? Objects.equals(id, tool.id) : Objects.equals(toolName, tool.toolName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : toolName);
    }
}
