package pt.feup.industrial.mes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a physical machine on the plant floor.
 */
@Entity
@Table(name = "machines")
@Getter
@Setter
@NoArgsConstructor
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "machine_name", nullable = false, unique = true) // e.g., "M1a", "M6b"
    private String machineName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, columnDefinition = "varchar(255) default 'OFFLINE'")
    private MachineState state = MachineState.OFFLINE;

    @Column(name = "last_state_change_timestamp")
    private LocalDateTime lastStateChangeTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_tool_id", foreignKey = @ForeignKey(name = "fk_machine_tool"))
    private Tool currentTool;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_step_id", unique = true, foreignKey = @ForeignKey(name = "fk_machine_step"))
    private MesOrderStep currentStep;

    @Column(name = "total_operating_seconds", nullable = false, columnDefinition = "bigint default 0")
    private long totalOperatingSeconds = 0L;

    @Column(name = "total_tool_changes", nullable = false, columnDefinition = "integer default 0")
    private int totalToolChanges = 0;

    @Column(name = "total_workpieces_processed", nullable = false, columnDefinition = "integer default 0")
    private int totalWorkpiecesProcessed = 0;

    @Column(name = "last_execution_start_time")
    private LocalDateTime lastExecutionStartTime;

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        lastStateChangeTimestamp = LocalDateTime.now();
    }

    @Transient
    public Duration getCurrentExecutionDuration() {
        if (state == MachineState.EXECUTING && lastExecutionStartTime != null) {
            return Duration.between(lastExecutionStartTime, LocalDateTime.now());
        }
        return Duration.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Machine machine = (Machine) o;
        return id != null ? Objects.equals(id, machine.id) : Objects.equals(machineName, machine.machineName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : machineName);
    }
}
