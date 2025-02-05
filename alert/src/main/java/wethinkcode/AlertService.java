package wethinkcode;


import wethinkcode.loadshed.alert.AlertServiceListener;

public class AlertService {
    public static void main(String[] args) {
        AlertServiceListener alertListener = new AlertServiceListener("loadshed_alerts");
        alertListener.run();
    }
}
