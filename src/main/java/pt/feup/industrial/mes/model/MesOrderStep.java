package pt.feup.industrial.mes.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "mes_order_steps")
@Getter
@Setter
@NoArgsConstructor
public class MesOrderStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "erp_client_order_id", nullable = false)
    private Long erpClientOrderId;

    @NotNull
    @Column(name = "erp_order_item_id", nullable = false, unique = true)
    private Long erpOrderItemId;

    @NotNull
    @Column(name = "product_type", nullable = false)
    private Integer productType;

    @NotNull
    @Positive
    @Column(name = "required_quantity", nullable = false)
    private Integer requiredQuantity;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MesOrderStatus status = MesOrderStatus.RECEIVED;

    @Column(name = "received_timestamp", nullable = false, updatable = false)
    private LocalDateTime receivedTimestamp;

    @Column(name = "scheduled_timestamp")
    private LocalDateTime scheduledTimestamp;

    @Column(name = "dispatch_timestamp")
    private LocalDateTime dispatchTimestamp;

    @Column(name = "start_processing_timestamp")
    private LocalDateTime startProcessingTimestamp;

    @Column(name = "completion_timestamp")
    private LocalDateTime completionTimestamp;

    @Column(name = "mes_priority")
    private Integer mesPriority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_machine_id", foreignKey = @ForeignKey(name = "fk_step_machine"))
    private Machine assignedMachine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_dock_id", foreignKey = @ForeignKey(name = "fk_step_dock"))
    private UnloadingDock assignedDock; // Which dock is designated for this item's output

    @PrePersist
    protected void onCreate() {
        receivedTimestamp = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MesOrderStep that = (MesOrderStep) o;
        return id != null ? Objects.equals(id, that.id) : Objects.equals(erpOrderItemId, that.erpOrderItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : erpOrderItemId);
    }
}