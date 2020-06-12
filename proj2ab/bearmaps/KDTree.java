package bearmaps;

import java.util.ArrayList;
import java.util.List;

public class KDTree implements PointSet{
    QuadTreeNode root;
    public KDTree(List<Point> points) {
        for (Point point : points) {
            put(point);
        }
    }

    public void put(Point p) {
        root = put(root, p);
    }

    /* Helper Method */
    private QuadTreeNode put(QuadTreeNode node, Point p) {
        if (p == null) {
            throw new IllegalArgumentException("Point can't be null");
        }
        if (node == null) {
            return new QuadTreeNode(p);
        }
        int direction = node.goTo(p);
        node.children[direction] = put(node.children[direction], p);
        return node;
    }

    @Override
    public Point nearest(double x, double y) {
        return null;
    }

    // specifically, implement quadtree here
    private class QuadTreeNode {
        private Point p;
        private QuadTreeNode[] children;
        // NE - 0, SE - 1, SW - 2, NW - 3

        public QuadTreeNode(Point point) {
            p = point;
            children = new QuadTreeNode[4];
        }

        public Point getPoint() {
            return p;
        }

        public QuadTreeNode getChild(int i) {
            if (i >= 4) {
                throw new IllegalArgumentException("Child index overflow");
            }
            return children[i];
        }

        public void setChild(int i, QuadTreeNode node) {
            if (i >= 4) {
                throw new IllegalArgumentException("Child index overflow");
            }
            children[i] = node;
        }

        public int goTo(Point o) {
            if (getPoint().getX() < o.getX()) {
                // Always east
                if (getPoint().getY() < o.getY()) {
                    // Northeast
                    return 0;
                } else {
                    // Southeast
                    return 1;
                }
            } else {
                // Always west
                if (getPoint().getY() < o.getY()) {
                    // Northwest
                    return 3;
                } else {
                    // Southwest
                    return 2;
                }
            }
        }
    }

    public static void main(String[] args) {
        Point A = new Point(-1, -1),
                B = new Point(2, 2),
                C = new Point(0, 1),
                D = new Point(1, 0),
                E = new Point(-2, -2);
        ArrayList<Point> points = new ArrayList<>();
        points.add(A);
        points.add(B);
        points.add(C);
        points.add(D);
        points.add(E);

        KDTree t = new KDTree(points);
        System.out.println("Fine");
    }
}
