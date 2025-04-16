package pt.feup.industrial.mes.model;

public enum MachineState {
    IDLE,          // Available, doing nothing
    EXECUTING,     // Actively processing a workpiece
    CHANGING_TOOL, // Performing a tool change
    MAINTENANCE,   // Unavailable for production (manual state)
    OFFLINE,       // Communication lost or powered off
    FAILED         // Machine reported an internal error
}
