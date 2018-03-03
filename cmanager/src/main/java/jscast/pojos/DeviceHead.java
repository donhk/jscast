package jscast.pojos;

import java.util.ArrayList;

public class DeviceHead {
    public String urn;
    public String name;
    public String hardware;
    public String location;
    public ArrayList<String> types;
    public ArrayList<String> xaddrs;
    public ArrayList<String> scopes;

    @Override
    public String toString() {
        return "urn[" + urn
                + "] name[" + name
                + "] hardware[" + hardware
                + "] location[" + location
                + "] types[" + types.toString()
                + "] xaddrs[" + xaddrs.toString()
                + "] scopes [" + scopes.toString() + "]";
    }
}
