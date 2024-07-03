package dev.zzcheah.interview.trainscheduling;

import dev.zzcheah.interview.trainscheduling.Main.Load.LoadState;
import dev.zzcheah.interview.trainscheduling.Main.Schedule.ScheduleType;
import dev.zzcheah.interview.trainscheduling.Main.Train.TrainState;
import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

public class Main {

    // input
    static List<Node> nodes;
    static List<Edge> edges;
    static List<Load> loads;
    static List<Train> trains;

    // derived
    static Map<String, Node> nodeMap;
    static Map<Node, LinkedList<Node>> adjListMap;
    static Map<Load, LinkedList<Edge>> shortestPath;

    static final int INTERVAL = 60;

    static {
        nodes = Stream.of("A", "B", "C").map(Node::new).toList();
        nodeMap = nodes.stream().collect(Collectors.toMap(n -> n.name, Function.identity()));
        edges = constructEdges(nodeMap, List.of("E1,A,B,30", "E2,B,C,10"));
        loads = prepareLoads(nodeMap, List.of("K1,5,A,C"));
        trains = prepareTrains(nodeMap, List.of("K1,5,A,C"));
    }

    public static void main(String[] args) {

        initializeNetwork();

        // for optimization, we can actually define multiple strategy based on different factors
        // these factors can be number of trains, number of loads, average capacity of train etc

        // for this example, we are assuming that the number of trains will be high
        List<Schedule> result = calculateSchedule();
        result.forEach(System.out::println);
    }

    static void initializeNetwork() {
        adjListMap = new HashMap<>();
        nodeMap.values().forEach(node -> adjListMap.put(node, new LinkedList<>()));
        edges.forEach(e ->
            nodes.forEach(n ->
                e.getPair(n).ifPresent(x ->
                    adjListMap.get(n).add(x)
                )
            )
        );
        shortestPath = new HashMap<>(); // fixme, BFS for shortest path from load to destination
    }

    /**
     * Greedy algorithm <br/> Attempted Optimization / Strategy: <br/> - maximize concurrency by
     * having each load to be picked up train with the shortest distance <br/>
     */
    static List<Schedule> calculateSchedule() {
        List<Schedule> allSchedules = new ArrayList<>();

        int time = 0;
        var doneProcessingLoadsMap = loads.stream()
            .collect(Collectors.partitioningBy(load -> load.state == LoadState.DELIVERED));

        while (!doneProcessingLoadsMap.get(false).isEmpty()) {

            List<Schedule> currentSchedules = new ArrayList<>();

            Map<LoadState, List<Load>> loadsByState = doneProcessingLoadsMap.get(false).stream()
                .collect(Collectors.groupingBy(load -> load.state));

            handleNewLoads(loadsByState.getOrDefault(LoadState.NEW, List.of()), currentSchedules,
                time);

            time += INTERVAL;
            allSchedules.addAll(currentSchedules);
            updateState(time, allSchedules);

        }

        return allSchedules;
    }

    static void updateState(Integer time, List<Schedule> allSchedules) {
        allSchedules.stream().filter(s -> s.type != ScheduleType.COMPLETED).forEach(s -> {
            if (time >= s.time + s.duration) {
                if (s.type == ScheduleType.DELIVERY) {
                    s.train.state = TrainState.READY;
                    s.p1.get(0).state = LoadState.DELIVERED;
                } else if (s.type == ScheduleType.PICKUP) {
                    s.train.state = TrainState.DELIVERING;
                    s.p1.get(0).state = LoadState.DELIVERING;
                    //
                }

                s.type = ScheduleType.COMPLETED;
            }

        });

    }

    static void handleNewLoads(List<Load> loads, List<Schedule> currentSchedules, int time) {
        // find trains for new loads
        for (Load load : loads) {
            var trainsAvailable = trains.stream().filter(t -> t.state == TrainState.READY)
                .toList();

            var trainAtSameLocation = trainsAvailable.stream()
                .filter(t -> t.currentNode == load.startingNode)
                .findFirst();

            if (trainAtSameLocation.isPresent()) {
                var train = trainAtSameLocation.get();
                LinkedList<Edge> path = shortestPath.get(load);
                var scheduledTime = time;
                var currentNode = load.startingNode;
                train.state = TrainState.DELIVERING;
                load.state = LoadState.DELIVERING;

                for (Edge edge : path) {
                    currentSchedules.add(new Schedule(
                        scheduledTime, train, currentNode,
                        edge.getPair(currentNode).orElseThrow(), List.of(load),
                        List.of(), edge.duration, ScheduleType.DELIVERY
                    ));
                    scheduledTime += edge.duration;
                }

            } else {
                Node startNode = load.startingNode;

                Queue<Pair<Node, LinkedList<Node>>> queue = new LinkedList<>();
                Map<Node, Boolean> visited = nodes.stream()
                    .collect(Collectors.toMap(Function.identity(), n -> Boolean.FALSE));

                visited.put(startNode, true);
                queue.add(Pair.of(startNode, new LinkedList<>()));

                Pair<Train, LinkedList<Node>> trainPath = null;
                while (!queue.isEmpty()) {
                    var currentPair = queue.poll();
                    Node currentNode = currentPair.getLeft();

                    if (currentNode == load.startingNode) {
                        var trainFound = trainsAvailable.stream()
                            .filter(t -> t.canPickupLoad(load) && t.currentNode == currentNode)
                            .findFirst();
                        if (trainFound.isPresent()) {
                            trainPath = Pair.of(trainFound.get(), currentPair.getRight());
                            break;
                        }
                    }

                    var neighbours = adjListMap.get(currentNode).stream()
                        .filter(n -> !visited.get(n));

                    neighbours.forEach(n -> {
                        LinkedList<Node> cloned = new LinkedList<>(currentPair.getRight());
                        cloned.add(n);
                        visited.put(n, true);
                        queue.add(Pair.of(n, cloned));
                    });

                }

                if (trainPath != null) {
                    Train train = trainPath.getLeft();
                    var path = trainPath.getRight();
                    var scheduledTime = time;
                    train.state = TrainState.PICKING_LOAD;
                    load.state = LoadState.WAITING;

                    for (int i = path.size() - 1; i > 0; i--) {
                        int duration = 30; // fixme: remodel so duration of edge can be captured
                        currentSchedules.add(new Schedule(
                            scheduledTime, train, path.get(i - 1),
                            path.get(i - 1), List.of(load),
                            List.of(), duration, ScheduleType.PICKUP
                        ));
                        scheduledTime += duration;
                    }
                }

            }
        }
    }


    static class Node {

        String name;

        public Node(String name) {
            this.name = name;
        }
    }

    static class Edge {

        String name;
        Node x;
        Node y;
        int duration; // in minutes

        public Edge(String name, @Nonnull Node x, @Nonnull Node y, int duration) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.duration = duration;
        }

        /**
         * return empty if current is not part of edge <br/> else return the other pair
         */
        Optional<Node> getPair(Node current) {
            Set<Node> set = new HashSet<>(Arrays.asList(x, y));
            if (set.remove(current)) { // if contains current
                return Optional.of(set.iterator().next()); // return the other pair
            } else {
                return Optional.empty();
            }
        }

    }

    static class Train {

        String name;
        int capacity;
        Node startingNode;

        int currentCapacity;
        Node currentNode;
        List<Load> currentLoads;
        TrainState state;


        public Train(String name, int capacity, Node startingNode) {
            this.name = name;
            this.capacity = capacity;
            this.startingNode = startingNode;
            this.currentNode = startingNode;
            this.currentCapacity = 0;
            this.currentLoads = new ArrayList<>();
            this.state = TrainState.READY;
        }

        boolean canPickupLoad(Load load) {
            return capacity >= currentCapacity + load.weight;
        }

        enum TrainState {
            READY,
            PICKING_LOAD,
            DELIVERING,
        }
    }

    // this class represent Package to avoid keyword package
    static class Load {

        String name;
        int weight;
        Node startingNode;
        Node destinationNode;

        LoadState state;

        public Load(String name, int weight, Node startingNode, Node destinationNode) {
            this.name = name;
            this.weight = weight;
            this.startingNode = startingNode;
            this.destinationNode = destinationNode;
            this.state = LoadState.NEW;
        }

        enum LoadState {
            NEW,
            WAITING,
            DELIVERING,
            DELIVERED
        }
    }

    static class Schedule {

        int time; // in seconds
        Train train;
        Node n1; // start
        Node n2; // destination
        List<Load> p1;
        List<Load> p2;

        int duration;
        ScheduleType type;


        public Schedule(int time, Train train, Node n1, Node n2, List<Load> p1, List<Load> p2,
            int duration, ScheduleType type) {
            this.time = time;
            this.train = train;
            this.n1 = n1;
            this.n2 = n2;
            this.p1 = p1;
            this.p2 = p2;
            this.duration = duration;
            this.type = type;
        }

        @Override
        public String toString() {
            return "W=%s, T=%s, N1=%s, P1=[%s], N2=%s, P2=[%s]"
                .formatted(time, train.name, n1.name, p1, n2.name, p2);
        }

        enum ScheduleType {
            PICKUP,
            DELIVERY,
            COMPLETED
        }
    }


    // preparations
    static List<Edge> constructEdges(Map<String, Node> nodeMap, List<String> edgeStrings) {
        return edgeStrings.stream()
            .map(s -> s.split(","))
            .map(arr -> new Edge(
                arr[0], nodeMap.get(arr[1]),
                nodeMap.get(arr[2]), Integer.parseInt(arr[3])
            ))
            .toList();
    }

    static List<Load> prepareLoads(Map<String, Node> nodeMap, List<String> loadStrings) {
        return loadStrings.stream()
            .map(s -> s.split(","))
            .map(arr -> new Load(
                arr[0], Integer.parseInt(arr[1]),
                nodeMap.get(arr[2]), nodeMap.get(arr[3])
            ))
            .toList();
    }

    static List<Train> prepareTrains(Map<String, Node> nodeMap, List<String> trainStrings) {
        return trainStrings.stream()
            .map(s -> s.split(","))
            .map(arr -> new Train(arr[0], Integer.parseInt(arr[1]), nodeMap.get(arr[2])))
            .toList();
    }
}
