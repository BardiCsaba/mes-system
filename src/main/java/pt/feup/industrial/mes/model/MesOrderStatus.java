package pt.feup.industrial.mes.model;

public enum MesOrderStatus {
    RECEIVED,
    SENT_TO_SCHEDULING,
    SCHEDULING_PROCESSING,
    SCHEDULING_FAILED,
    COMPLETED,
    FAILED
}