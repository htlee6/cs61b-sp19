package bearmaps.proj2ab;

import edu.princeton.cs.algs4.Stopwatch;
import java.util.ArrayList;
import java.util.List;

public class KDTree implements PointSet{
    KDTreeNode root;
    public KDTree(List<Point> points) {
        for (Point aPoint : points) {
            put(aPoint);
        }
    }
    /** Put the given point p in right place */
    public void put(Point p) {
        root = putHelper(root, p, 0);
    }

    private KDTreeNode putHelper(KDTreeNode node, Point p, int depth) {
        if (p == null) {
            throw new IllegalArgumentException("Point to put couldn't be null");
        }
        if (node == null) {
            return new KDTreeNode(p, depth);
        }
        int cmp;
        if (lookAtX(depth)) { // at level which split vertically
            cmp = Double.compare(p.getX(), node.point.getX());
        } else { // at level which split horizontally
            cmp = Double.compare(p.getY(), node.point.getY());
        }
        if (cmp > 0) {
            node.childBig = putHelper(node.childBig, p, depth + 1);
        } else if (cmp < 0) {
            node.childSmall = putHelper(node.childSmall, p, depth + 1);
        } else { // tie breaker, treat it as greater than
            node.childBig = putHelper(node.childBig, p, depth + 1);
        }
        return node;
    }

    @Override
    public Point nearest(double x, double y) {
        Point goal = new Point(x, y);
        return nearestHelper(root, goal, 0, root.point);
    }

    private Point nearestHelper(KDTreeNode node, Point goal, int depth, Point best) {
        if (node == null) {
            return best;
        }
        if (Point.distance(node.point, goal) < Point.distance(best, goal)) {
            best = node.point;
        }
        int goodSide = chooseGoodSide(node.point, goal, depth);
        // use the good side
        if (goodSide == 1) {
            best = nearestHelper(node.childBig, goal, depth + 1, best);
        }
        if (goodSide == 0) {
            best = nearestHelper(node.childSmall, goal, depth + 1, best);
        }
        // only if the bad sad has some useful info, then we look at it
        // this operation is so called 'pruning', which means cutting off useless branches of a tree
        // ignore the useless to speed up
        if (badSideIsUseful(best, goal, node)) {
            if (goodSide == 1) {
                best = nearestHelper(node.childSmall, goal, depth + 1, best);
            }
            if (goodSide == 0) {
                best = nearestHelper(node.childBig, goal, depth + 1, best);
            }
        }
        return best;
    }

    /** Tell if the bad side will possibly has useful information
     * AKA if theoretically possible distance from goal to the splitting border < current best-goal distance
     */
    private static boolean badSideIsUseful(Point best, Point goal, KDTreeNode border) {
        double possibleDistanceMin;
        if (lookAtX(border.depth)) {
            possibleDistanceMin = Math.pow(border.point.getX() - goal.getX(), 2);
        } else {
            possibleDistanceMin = Math.pow(border.point.getY() - goal.getY(), 2);
        }
        double bestGoalDistance = Point.distance(best, goal);
        return Double.compare(possibleDistanceMin, bestGoalDistance) < 0;
    }

    public Point nearestInefficient(double x, double y) {
        Point goal = new Point(x, y);
        return nearestInefficientHelper(root, goal, root.point);
    }

    private Point nearestInefficientHelper(KDTreeNode node, Point goal, Point best) {
        if (node == null) {
            return best;
        }
        if (Point.distance(node.point, goal) < Point.distance(best, goal)) {
            best = node.point;
        }
        best = nearestInefficientHelper(node.childSmall, goal, best);
        best = nearestInefficientHelper(node.childBig, goal, best);
        return best;
    }

    private class KDTreeNode {
        private int depth;
        private Point point;
        private KDTreeNode childSmall;
        private KDTreeNode childBig;

        public KDTreeNode(Point p, int d) {
            depth = d;
            point = p;
            childBig = null;
            childSmall = null;
        }
    }
    private static boolean lookAtX(int d) {
        return d % 2 == 0;
    }

    private static int compareX(Point p1, Point p2) {
        return Double.compare(p1.getX(), p2.getX());
    }

    private static int compareY(Point p1, Point p2) {
        return Double.compare(p1.getY(), p2.getY());
    }

    /** Return if p1 is on the left of p2 */
    private static boolean isLeft(Point p1, Point p2) {
        return compareX(p1, p2) < 0;
    }

    /** Return if p1 is on the downside of p2 */
    private static boolean isDown(Point p1, Point p2) {
        return compareY(p1, p2) < 0;
    }

    /** Return 1 for big child & 0 for small child */
    private static int chooseGoodSide(Point p, Point goal, int depth) {
        if (lookAtX(depth)) {
            if (isLeft(goal, p)) {
                // when looking at X and goal is on the left of p, then left(small child branch) is the good side
                return 0;
            }
        } else { // looking at Y
            if (isDown(goal, p)) {
                // goal is on the downside of p, then down(small child branch) is the good side
                return 0;
            }
        }
        return 1;
    }

    public static void main(String[] args) {
        Point A = new Point(2, 3),
                B = new Point(4, 2),
                C = new Point(4, 5),
                D = new Point(3, 3),
                E = new Point(1, 5),
                F = new Point(4, 4);
        ArrayList<Point> points = new ArrayList<>();
        points.add(A);
        points.add(B);
        points.add(C);
        points.add(D);
        points.add(E);
        points.add(F);
        KDTree kdt = new KDTree(points);
        System.out.println("Fine");
        Point nrst = new Point(0, 0), nrst_effi = new Point(0, 0);
        int count = 100000;

        Stopwatch inefficientWatch = new Stopwatch();
        for (int i = 0; i < count; i += 1) {
            nrst = kdt.nearestInefficient(0, 7);
        }
        System.out.println(inefficientWatch.elapsedTime() + "\t" + nrst.toString());

        Stopwatch efficientWatch = new Stopwatch();
        for (int i = 0; i < count; i += 1) {
            nrst_effi = kdt.nearest(0, 7);
        }
        System.out.println(efficientWatch.elapsedTime()+ "\t" + nrst_effi.toString());
    }
}
