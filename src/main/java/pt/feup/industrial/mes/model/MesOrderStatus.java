package pt.feup.industrial.mes.model;

public enum MesOrderStatus {
    RECEIVED,      // Received from ERP, pending scheduling/assignment
    SCHEDULED,     // Assigned a place in the production sequence
    DISPATCHED,    // Sent to a specific machine/PLC for execution
    IN_PROGRESS,   // Machine has started processing
    COMPLETED,     // Successfully processed
    FAILED,        // Processing failed
    ABORTED        // Processing cancelled/aborted
}