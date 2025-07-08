package sr.thrift.server;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import thrift.gen.*;


public class SmartHomeServerBlindsBulbulator {
    public static void main(String [] args) {
        try {
            Runnable serve = SmartHomeServerBlindsBulbulator::serve;
            new Thread(serve).start();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void serve() {
        try {
            // blinds services
            Blinds.Processor<BlindsHandler> processor1 = new Blinds.Processor<>(new BlindsHandler(1, new BlindsInfo(50, "bedroom")));
            Blinds.Processor<BlindsHandler> processor2 = new Blinds.Processor<>(new BlindsHandler(2, new BlindsInfo(50, "kitchen")));

            // bulbulator services
            Bulbulator.Processor<BulbulatorHandler> processor11 = new Bulbulator.Processor<>(new BulbulatorHandler(11, new BulbulatorInfo(BulbulatorType.BUL, "Marian", false)));
            Bulbulator.Processor<BulbulatorHandler> processor12 = new Bulbulator.Processor<>(new BulbulatorHandler(12, new BulbulatorInfo(BulbulatorType.BULBUL, "Eustachy", true)));

            TServerTransport serverTransport = new TServerSocket(9080);
            TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
            TMultiplexedProcessor multiplex = new TMultiplexedProcessor();

            multiplex.registerProcessor("bl1", processor1);
            multiplex.registerProcessor("bl2", processor2);
            multiplex.registerProcessor("bu1", processor11);
            multiplex.registerProcessor("bu2", processor12);

            TServer server = new TSimpleServer(new Args(serverTransport).protocolFactory(protocolFactory).processor(multiplex));
            System.out.println("Starting server for blinds and bulbulators...");
            server.serve();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}

