package jscast.pojos;

public class Coord {
    public String target;
    public String x;
    public String y;
    public String z;

    public Coord(String target, String x, String y, String z) {
        this.target = target;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Coord(String target, double x, double y, double z) {
        this(target, String.valueOf(x), String.valueOf(y), String.valueOf(z));
    }
}
