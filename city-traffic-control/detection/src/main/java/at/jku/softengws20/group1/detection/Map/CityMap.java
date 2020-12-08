package at.jku.softengws20.group1.detection.Map;

import at.jku.softengws20.group1.shared.impl.model.RoadNetwork;
import java.util.HashMap;
import java.util.Map;

public class CityMap {
    private final Map<String, Street> streets = new HashMap<>();
    private final Map<String, Crossroad> crossroads = new HashMap<>();

    public CityMap() {
    }

    public void createCityMap(RoadNetwork roadNetwork) {
        for (int i = 0; i < roadNetwork.getRoadSegments().length ; i++) {
            putStreet(roadNetwork.getRoadSegments()[i].getId());
        }

        for (int i = 0; i < roadNetwork.getCrossings().length; i++) {
            putCrossroad(roadNetwork.getCrossings()[i].getId());
            for (int j = 0; j < roadNetwork.getCrossings()[i].getRoadSegmentIds().length; j++) {
                putStreetToCrossroad(roadNetwork.getCrossings()[i].getId() ,roadNetwork.getCrossings()[i].getRoadSegmentIds()[j]);
            }
        }

        start();
    }

    public void start() {                           //Start Map
        crossroads.forEach((x,y) -> y.start());
    }

    //Streets and Crossroads
    public void putStreet(String id) {
        streets.put(id, new Street(id));
    }

    public Street getStreet(String id) {
        return streets.get(id);
    }

    public void putCrossroad(String id) {
        crossroads.put(id, new Crossroad(id));
    }

    public Crossroad getCrossroad(String id) {
        return crossroads.get(id);
    }

    public Map<String, Crossroad> getCrossroads() {
        return crossroads;
    }

    public void putStreetToCrossroad(String crossroadId, String streetId) {     //Connection
        crossroads.get(crossroadId).putStreet(getStreet(streetId));
    }

    /*For further Implementation if more time:
        - accessibility for information signs
        - block roads possible
     */
}