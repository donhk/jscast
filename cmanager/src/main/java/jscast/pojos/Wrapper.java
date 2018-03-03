package jscast.pojos;


import org.opencv.core.Rect;

public class Wrapper {
    private Rect reference;
    private org.opencv.core.Rect[] targets;

    public Wrapper(Rect reference, Rect[] targets) {
        this.reference = reference;
        this.targets = targets;
    }

    public Rect getReference() {
        return reference;
    }

    public Rect[] getTargets() {
        return targets;
    }
}
