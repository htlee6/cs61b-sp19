package bearmaps.hw4;

import bearmaps.proj2ab.DoubleMapPQ;
import com.sun.source.tree.Tree;
import edu.princeton.cs.algs4.DepthFirstSearch;
import edu.princeton.cs.algs4.Stopwatch;

import java.util.*;

public class AStarSolver<Vertex> implements ShortestPathsSolver<Vertex> {

    /* member variables */
    private SolverOutcome outcome = null;
    private List<Vertex> solution = new ArrayList<>();
    private double solutionWeight = 0.0;
    private int numStatesExplored = 0;
    private double explorationTime = 0.0;
    private TreeMap<Vertex, Double> distTo;
    private TreeMap<Vertex, Vertex> edgeTo;
    private DoubleMapPQ<Vertex> pq;

    public AStarSolver(AStarGraph<Vertex> input, Vertex start, Vertex end, double timeout) {
        Stopwatch timer = new Stopwatch();
        pq = new DoubleMapPQ<>();
        pq.add(start, 0.0);

        // Initialization
        distTo = new TreeMap<>();
        distTo.put(start, 0.0);
        edgeTo = new TreeMap<>();

        // Repeat
        while (pq.size() != 0) {
            Vertex current = pq.removeSmallest();
            if (current.equals(end)) break;
            numStatesExplored += 1;
            List<WeightedEdge<Vertex>> outcomingsFromCurrent = input.neighbors(current);
            for (WeightedEdge<Vertex> edge : outcomingsFromCurrent) {
                double estimatedDist = input.estimatedDistanceToGoal(edge.to(), end);
                relax(edge, estimatedDist);
            }
        }

        double time = timer.elapsedTime() / 1000;
        try {
            solution = getSolutionFromEdgeTo(start, end);
        } catch (NullPointerException e) {
            outcome = SolverOutcome.UNSOLVABLE;
            return;
        }

        if (solution.size() == 0) {
            outcome = SolverOutcome.UNSOLVABLE;
        } else if (time <= timeout) {
            outcome = SolverOutcome.SOLVED;
            explorationTime = time;
            solutionWeight = distTo.get(end);
        } else {
            outcome = SolverOutcome.TIMEOUT;
            explorationTime = time;
        }
    }

    @Override
    public SolverOutcome outcome() {
        return outcome;
    }

    @Override
    public List<Vertex> solution() {
        return solution;
    }

    @Override
    public double solutionWeight() {
        if (outcome().equals(SolverOutcome.SOLVED)) {
            return solutionWeight;
        }
        return 0;
    }

    @Override
    public int numStatesExplored() {
        return numStatesExplored;
    }

    @Override
    public double explorationTime() {
        return explorationTime;
    }

    private void relax(WeightedEdge<Vertex> e, double estimatedDistance) {
        Vertex p = e.from(), q = e.to();
        double w = e.weight();
        if (!distTo.containsKey(q)) {
            distTo.put(q, Double.POSITIVE_INFINITY);
        }

        double originPriorityOfQ = distTo.get(q),
                possibleNewPriorityOfQ = distTo.get(p) + w;
        if (possibleNewPriorityOfQ < originPriorityOfQ) {
            distTo.put(q, possibleNewPriorityOfQ); // a closer distance from q to start node
            edgeTo.put(q, p); // q is on the next of p
            if (pq.contains(q)) {
                pq.changePriority(q, distTo.get(q) + estimatedDistance);
            } else {
                pq.add(q, distTo.get(q) + estimatedDistance);
            }
        }
    }

    private List<Vertex> getSolutionFromEdgeTo(Vertex s, Vertex v) throws NullPointerException {
        List<Vertex> res = new ArrayList<>();
        Vertex p = v;
        while (!p.equals(s)) {
            res.add(p);
            p = edgeTo.get(p);
        }
        res.add(s);
        Collections.reverse(res);
        return res;
    }

}
