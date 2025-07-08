package sr.thrift.server;

import org.apache.thrift.TException;
import thrift.gen.DangerousParamValue;
import thrift.gen.Detector;
import thrift.gen.DetectorInfo;
import thrift.gen.InvalidParamName;

import java.util.List;
import java.util.Map;

public class DetectorHandler implements Detector.Iface {
    final int id;
    private final DetectorInfo info;

    public DetectorHandler(int id, DetectorInfo info) {
        this.id = id;
        this.info = info;
    }

    @Override
    public Map<String, Integer> getParams() {
        System.out.println("Retrieving " + info.getType() + " detector parameters...");
        try {
            Thread.sleep(3000);
        } catch(java.lang.InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        return info.getParams();
    }

    @Override
    public int getParamValue(String param) throws TException {
        if(!info.getParams().containsKey(param)) {
            throw new InvalidParamName(param);
        }
        System.out.println("Retrieving parameter '" + param + "' from " + info.getType() + " detector...");
        try {
            Thread.sleep(500);
        } catch(java.lang.InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        return info.getParams().get(param);
    }

    @Override
    public void checkParamSafety(String param, List<Integer> bounds) throws TException {
        int paramValue = getParamValue(param);
        int minimum = bounds.get(0);
        int maximum = bounds.get(1);
        if(paramValue < minimum || paramValue > maximum) {
            System.out.println("Alert! Param '" + param + "' value = " + paramValue + " in " + info.getType() + " detector outside safe bounds: " + bounds);
            throw new DangerousParamValue(param, paramValue, bounds);
        }
        System.out.println("Param '" + param + "' value = " + paramValue + " in " + info.getType() + " detector within bounds: " + bounds);
    }
}
