package bearmaps;

import java.util.ArrayList;
import java.util.List;

public class NaivePointSet implements PointSet {
    ArrayList<Point> points;
    public NaivePointSet(List<Point> points) {
        // assume points is at least 1 in length
        this.points = (ArrayList<Point>) points;
    }
    @Override
    public Point nearest(double x, double y) {
        Point nearestPoint = null;
        double minDistance = 100000.0;
        Point objective = new Point(x, y);
        for (Point point : points) {
            if (Point.distance(objective, point) < minDistance) {
                nearestPoint = point;
                minDistance = Point.distance(objective, nearestPoint);
            }
        }
        return nearestPoint;
    }

    public static void main(String[] args) {
        Point p1 = new Point(1.1, 2.2); // constructs a Point with x = 1.1, y = 2.2
        Point p2 = new Point(3.3, 4.4);
        Point p3 = new Point(-2.9, 4.2);
        ArrayList<Point> pl = new ArrayList<>();
        pl.add(p1); pl.add(p2); pl.add(p3);

        NaivePointSet nn = new NaivePointSet(pl);
        Point ret = nn.nearest(3.0, 4.0); // returns p2
        System.out.println(ret.getX()); // evaluates to 3.3
        System.out.println(ret.getY()); // evaluates to 4.4
    }
}
