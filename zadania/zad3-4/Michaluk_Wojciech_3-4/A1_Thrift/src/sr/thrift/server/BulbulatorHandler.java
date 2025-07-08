package sr.thrift.server;

import thrift.gen.Bulbulator;
import thrift.gen.BulbulatorInfo;

public class BulbulatorHandler implements Bulbulator.Iface {
    final int id;
    private final BulbulatorInfo info;

    public BulbulatorHandler(int id, BulbulatorInfo info) {
        this.id = id;
        this.info = info;
    }

    @Override
    public void turnOn() {
        if(info.isIsOn()) {
            System.out.println("Bulbulator '" + info.getName() + "', type '" + info.getType() + "' already turned on!");
            return;
        }
        System.out.println("Turning on...");
        try {
            Thread.sleep(5000);
        } catch(java.lang.InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        info.setIsOn(true);
        System.out.println("Bulbulator '" + info.getName() + "', type '" + info.getType() + "' turned on");
    }

    @Override
    public void turnOff() {
        if(!info.isIsOn()) {
            System.out.println("Bulbulator '" + info.getName() + "', type '" + info.getType() + "' already turned off!");
            return;
        }
        System.out.println("Turning off...");
        try {
            Thread.sleep(2000);
        } catch(java.lang.InterruptedException ex) {
            System.out.println(ex.getMessage());
        }
        info.setIsOn(false);
        System.out.println("Bulbulator '" + info.getName() + "', type '" + info.getType() + "' turned off");

    }
}
