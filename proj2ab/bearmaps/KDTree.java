package bearmaps;

import java.util.ArrayList;
import java.util.Comparator;
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
        if (compareX(depth)) { // at level which split vertically
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
        return null;
    }
    private class KDTreeNode {
        private int depth;
        private boolean vertical;
        private Point point;
        private KDTreeNode childSmall;
        private KDTreeNode childBig;

        public KDTreeNode(Point p) {
            depth = 0;
            vertical = isVertical(depth);
            point = p;
            childBig = null;
            childSmall = null;
        }

        public KDTreeNode(Point p, int d) {
            depth = d;
            vertical = isVertical(depth);
            point = p;
            childBig = null;
            childSmall = null;
        }

        public boolean splitVertically() {
            return vertical;
        }

        public int getDepth() {
            return depth;
        }

        private boolean isVertical(int depth) {
            return depth % 2 == 0;
        }
    }
    private static boolean compareX(int d) {
        return d % 2 == 0;
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
    }
}
