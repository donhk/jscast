package jscast.pojos.onvif;

public class Camera {
    public String name;
    public OnvifDevice attr;

    @Override
    public String toString() {
        return "name " + name + " attr " + attr.toString();
    }
}
