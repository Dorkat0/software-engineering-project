package at.jku.softengws20.group1.shared.impl.model;

public class CarArrivedNotification implements at.jku.softengws20.group1.shared.maintenance.CarArrivedNotification {

    private String roadSegmentId;
    private String maintenanceCarId;

    public CarArrivedNotification() {}

    public CarArrivedNotification(String roadSegmentId, String maintenanceCarId) {
        this.roadSegmentId = roadSegmentId;
        this.maintenanceCarId = maintenanceCarId;
    }

    @Override
    public String getRoadSegmentId() {
        return roadSegmentId;
    }

    @Override
    public String getMaintenanceCarId() {
        return maintenanceCarId;
    }
}
