package at.jku.softengws20.group1.maintenance.impl;

import java.util.Date;

public abstract class Repair { // TODO: Milestone 3.2
    public static int nextRepairId;
    private final RepairType repairType;
    private final String repairId;
    private final String location;
    private Date from = new Date();
    private Date to = new  Date();
    // duration in milliseconds
    private long duration;
    private int nrVehiclesNeeded;
    private int nrWorkersNeeded;

    public Repair(String repairId, RepairType repairType, String location, long duration,
                  int nrVehiclesNeeded, int nrWorkersNeeded) {
        this.repairId = repairId;
        this.repairType = repairType;
        this.location = location;
        this.duration = duration;
        this.nrVehiclesNeeded = nrVehiclesNeeded;
        this.nrWorkersNeeded = nrWorkersNeeded;
    }

    public int getNrVehiclesNeeded() {
        return nrVehiclesNeeded;
    }

    public void setNrVehiclesNeeded(int nrVehiclesNeeded) {
        this.nrVehiclesNeeded = nrVehiclesNeeded;
    }

    public int getNrWorkersNeeded() {
        return nrWorkersNeeded;
    }

    public void setNrWorkersNeeded(int nrWorkersNeeded) {
        this.nrWorkersNeeded = nrWorkersNeeded;
    }

    public String getRepairId() {
        return repairId;
    }

    public Enum getRepairType() {
        return repairType;
    }

    public String getLocation() {
        return location;
    }

    /**
     * @return estimated duration in milliseconds
     */
    public long getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setTime (Date from, Date to){
        this.from = from;
        this.to = to;
    }

    public Date getFrom() {
        return from;
    }

    public Date getTo() {
        return to;
    }

}