package at.jku.softengws20.group1.controlsystem.gui.osm_import;

import at.jku.softengws20.group1.shared.impl.model.Position;
import at.jku.softengws20.group1.shared.impl.model.RoadType;

import java.util.*;
import java.util.stream.Collectors;

class ImportedRoadNetwork {

    private final double LANE_DIST = 0.007;

    private Map<String, ImportedRoad> roads = new HashMap<>();
    private Map<String, ImportedCrossing> crossings = new HashMap<>();

    static ImportedRoadNetwork fromOSMModel(OSMStreetNetwork osm, LatLonProjector proj) {
        osm.getNodes().values().forEach(n -> proj.addPos(n.getLat(), n.getLon()));

        ImportedRoadNetwork roadNetwork = new ImportedRoadNetwork();
        Map<String, ImportedRoad> roadsByName = new HashMap<>();
        for (OSMWay way : osm.getWays().values()) {
            String name = way.getTags().get("name");
            ImportedRoad r = roadsByName.getOrDefault(name, null);
            if (r == null) {
                r = new ImportedRoad();
                r.setId(way.getId());
                r.setName(name);
                r.setNumber(way.getRoadRef());

                roadNetwork.getRoads().put(r.getId(), r);
                if (name != null) {
                    roadsByName.put(name, r);
                }
            }
            roadNetwork.createRoadSegments(r, way, proj);
        }
        roadNetwork.removeIslands();
        return roadNetwork;
    }

    Map<String, ImportedRoad> getRoads() {
        return roads;
    }

    Map<String, ImportedCrossing> getCrossings() {
        return crossings;
    }

    private void removeIslands() {
        Map<ImportedCrossing, Collection<ImportedRoadSegment>> edges = new HashMap<>();
        for (var r : roads.values()) {
            for (var rs : r.getRoadSegments()) {
                var l = edges.getOrDefault(rs.getCrossingA(), new ArrayList<>());
                l.add(rs);
                edges.put(rs.getCrossingA(), l);
            }
        }

        Set<ImportedCrossing> toDelete = new HashSet<>();
        for (var c : crossings.values()) {
            if (toDelete.contains(c)) {
                continue;
            }
            Set<ImportedCrossing> visited = new HashSet<>();
            if (isIsland(c, visited, edges, 20)) {
                toDelete.addAll(visited);
            }
        }

        for (var c : toDelete) {
            for (var rs : edges.getOrDefault(c, new ArrayList<>())) {
                rs.getRoad().getRoadSegments().remove(rs);
            }
            crossings.remove(c.getId());
        }

        for (var rs : roads.values().stream().flatMap(r -> r.getRoadSegments().stream()).collect(Collectors.toList())) {
            if (!crossings.containsKey(rs.getCrossingA().getId()) || !crossings.containsKey(rs.getCrossingB().getId())) {
                rs.getRoad().getRoadSegments().remove(rs);
            }
        }

        for (var r : new ArrayList<>(roads.values())) {
            if (r.getRoadSegments().isEmpty()) {
                roads.remove(r.getId());
            }
        }
    }

    private boolean isIsland(ImportedCrossing crossing, Set<ImportedCrossing> visited, Map<ImportedCrossing, Collection<ImportedRoadSegment>> edges, int maxDepth) {
        visited.add(crossing);
        if (maxDepth == 0) {
            return false;
        }

        for (var rs : edges.getOrDefault(crossing, new ArrayList<>())) {
            if (!visited.contains(rs.getCrossingB())) {
                if (!isIsland(rs.getCrossingB(), visited, edges, maxDepth - 1)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void createRoadSegments(ImportedRoad r, OSMWay way, LatLonProjector proj) {
        ImportedRoadSegment currentSegment = null;
        for (OSMNode node : way.getNodes()) {
            if (currentSegment == null) {
                currentSegment = createRoadData(r, way);
                if (node.isCrossing()) {
                    currentSegment.setCrossingA(getOrCreateCrossing(node, proj));
                }
                currentSegment.getPath().add(proj.project(node.getLat(), node.getLon()));
                continue;
            }
            currentSegment.getPath().add(proj.project(node.getLat(), node.getLon()));
            if (!node.isCrossing()) {
                continue;
            }
            currentSegment.setCrossingB(getOrCreateCrossing(node, proj));
            if (currentSegment.getCrossingA() != null && currentSegment.getCrossingB() != null) {
                int fLanes = way.getForwardLaneCount();
                if (way.isOneWay()) {
                    r.getRoadSegments().add(createDirectedLane(currentSegment, way, false, 0, fLanes));
                } else {
                    r.getRoadSegments().add(createDirectedLane(currentSegment, way, false, LANE_DIST / 2.0, fLanes));
                    int bLanes = way.getBackwardLaneCount();
                    r.getRoadSegments().add(createDirectedLane(currentSegment, way, true,  LANE_DIST / 2.0, bLanes));
                }
            }
            currentSegment = createRoadData(r, way);
            currentSegment.setCrossingA(getOrCreateCrossing(node, proj));
            currentSegment.getPath().add(proj.project(node.getLat(), node.getLon()));
        }
    }

    private ImportedRoadSegment createDirectedLane(ImportedRoadSegment original, OSMWay way, boolean reverse, double displace, int lanes) {
        ImportedRoadSegment s = createRoadData(original.getRoad(), way);
        s.setLaneCount(lanes);

        List<Position> path = new ArrayList<>(original.getPath());
        if (reverse) {
            s.setCrossingA(original.getCrossingB());
            s.setCrossingB(original.getCrossingA());
            Collections.reverse(path);
        } else {
            s.setCrossingA(original.getCrossingA());
            s.setCrossingB(original.getCrossingB());
        }

        s.setPath(movePathToRight(path, displace));
        return s;
    }

    private List<Position> movePathToRight(Collection<Position> path, double displace) {
        var newPath = new ArrayList<Position>();
        Position last = null;
        double x = 0;
        double y = 0;
        for (var p : path) {
            if (last == null) {
                last = p;
                continue;
            }
            double dx = p.getX() - last.getX();
            double dy = p.getY() - last.getY();
            y = dx;
            x = -dy;
            double l = Math.sqrt(x * x + y * y);
            if (l > 0.000001) {
                y *= displace / l;
                x *= displace / l;
                newPath.add(new Position(last.getX() + x, last.getY() + y));
            }
            last = p;
        }
        assert last != null;
        newPath.add(new Position(last.getX() + x, last.getY() + y));
        return newPath;
    }

    private ImportedRoadSegment createRoadData(ImportedRoad road, OSMWay way) {
        ImportedRoadSegment segment = new ImportedRoadSegment();
        segment.setId(road.getId() + "_" + road.getRoadSegments().size());
        segment.setRoadType(RoadType.fromOSMName(way.getRoadType()));
        segment.setSpeedLimit(way.getSpeedLimit() > 0 ? way.getSpeedLimit() : segment.getRoadType().getMaxSpeed());
        segment.setRoad(road);
        return segment;
    }

    private ImportedCrossing getOrCreateCrossing(OSMNode node, LatLonProjector proj) {
        ImportedCrossing crossing = crossings.getOrDefault(node.getId(), null);
        if (crossing == null) {
            crossing = new ImportedCrossing();
            crossing.setId(node.getId());
            crossing.setPosition(proj.project(node.getLat(), node.getLon()));
            crossings.put(crossing.getId(), crossing);
        }
        return crossing;
    }
}
