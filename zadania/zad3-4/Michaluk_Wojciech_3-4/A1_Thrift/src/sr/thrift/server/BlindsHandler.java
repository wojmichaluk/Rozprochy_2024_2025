package sr.thrift.server;

import thrift.gen.Blinds;
import thrift.gen.BlindsInfo;

public class BlindsHandler implements Blinds.Iface {
    final int id;
    private final BlindsInfo info;

    public BlindsHandler(int id, BlindsInfo info) {
        this.id = id;
        this.info = info;
    }

    @Override
    public int pullUp(int percent) {
        if(info.getRollPercent() == 100) {
            System.out.println("Blinds in the " + info.getRoom() + " already pulled 100% up!");
        } else {
            info.setRollPercent(Math.min(info.getRollPercent() + percent, 100));
            try {
                Thread.sleep(500);
            } catch(java.lang.InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
            System.out.println("Blinds pulled up in the " + info.getRoom() + ", now they're " + info.getRollPercent() + "% up!");
        }
        return info.getRollPercent();
    }

    @Override
    public int pullDown(int percent) {
        if(info.getRollPercent() == 0) {
            System.out.println("Blinds in the " + info.getRoom() + " already pulled down to maximum!");
        } else {
            info.setRollPercent(Math.max(info.getRollPercent() - percent, 0));
            try {
                Thread.sleep(500);
            } catch(java.lang.InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
            System.out.println("Blinds pulled down in the " + info.getRoom() + ", now they're " + info.getRollPercent() + "% up!");
        }
        return info.getRollPercent();
    }
}
