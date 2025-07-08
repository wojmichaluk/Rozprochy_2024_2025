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

import java.util.HashMap;


public class SmartHomeServerDetector {
	public static void main(String [] args) {
		try {
			Runnable serve = SmartHomeServerDetector::serve;
			new Thread(serve).start();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	public static void serve() {
		try {
			// detector services
			HashMap<String, Integer> smokeDetectorParams = new HashMap<>();
			smokeDetectorParams.put("smoke", 5);
			smokeDetectorParams.put("humidity", 23);

			HashMap<String, Integer> carbonMonoxideDetectorParams = new HashMap<>();
			carbonMonoxideDetectorParams.put("CO", 2);
			carbonMonoxideDetectorParams.put("CO2", 30);

			HashMap<String, Integer> temperatureDetectorParams = new HashMap<>();
			temperatureDetectorParams.put("temperature", 21);
			temperatureDetectorParams.put("pressure", 1015);

			Detector.Processor<DetectorHandler> processor21 = new Detector.Processor<>(new DetectorHandler(21, new DetectorInfo(DetectorType.SMOKE, smokeDetectorParams)));
			Detector.Processor<DetectorHandler> processor22 = new Detector.Processor<>(new DetectorHandler(22, new DetectorInfo(DetectorType.CARBON_MONOXIDE, carbonMonoxideDetectorParams)));
			Detector.Processor<DetectorHandler> processor23 = new Detector.Processor<>(new DetectorHandler(23, new DetectorInfo(DetectorType.TEMPERATURE, temperatureDetectorParams)));

			TServerTransport serverTransport = new TServerSocket(9070);
			TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
			TMultiplexedProcessor multiplex = new TMultiplexedProcessor();

			multiplex.registerProcessor("dt1", processor21);
			multiplex.registerProcessor("dt2", processor22);
			multiplex.registerProcessor("dt3", processor23);

			TServer server = new TSimpleServer(new Args(serverTransport).protocolFactory(protocolFactory).processor(multiplex));
			System.out.println("Starting server for detectors...");
			server.serve(); 
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
}
