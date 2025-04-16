package pt.feup.industrial.mes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an unloading dock (U1-U4).
 */
@Entity
@Table(name = "unloading_docks")
@Getter
@Setter
@NoArgsConstructor
public class UnloadingDock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "dock_name", nullable = false, unique = true) // e.g., "U1", "U2"
    private String dockName;

    // Requirement: Max 6 workpieces per dock
    // We might track this via the 'unloadedItems' relationship count or a separate field if needed.

    @OneToMany(mappedBy = "assignedDock", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<MesOrderStep> assignedSteps = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnloadingDock that = (UnloadingDock) o;
        return id != null ? Objects.equals(id, that.id) : Objects.equals(dockName, that.dockName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : dockName);
    }
}
