package jscast.pojos;

public class Point {
    public double x;
    public double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(String x, String y) {
        this.x = Double.parseDouble(x);
        this.y = Double.parseDouble(y);
    }

    public String toString() {
        return "x[" + x + "] y[" + y + "]";
    }

    public double distance(Point b) {
        double vx = Math.pow((b.x - x), 2);
        double vy = Math.pow((b.y - y), 2);
        return Math.sqrt(vx + vy);
    }
}
