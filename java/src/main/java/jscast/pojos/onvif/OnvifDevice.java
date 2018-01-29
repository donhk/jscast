package jscast.pojos.onvif;

import java.util.ArrayList;

public class OnvifDevice {
    public String address;
    public String xaddr;
    public String user;
    public String pass;
    public Oxaddr oxaddr;
    public String time_diff;
    public Information information;
    public Service services;
    public ArrayList<Profile> profile_list;
    public Profile current_profile;
    public String ptz_moving;
    public String domain;

    @Override
    public String toString() {
        return "address " + address + " xaddr " + xaddr;
    }
}
