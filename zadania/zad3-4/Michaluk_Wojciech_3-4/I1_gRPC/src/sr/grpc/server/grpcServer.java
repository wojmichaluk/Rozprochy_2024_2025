package sr.grpc.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;


public class grpcServer {
	private static final Logger logger = Logger.getLogger(grpcServer.class.getName());

	private Server server;

	private void start() throws IOException {
		int port = 50051;
		server = ServerBuilder.forPort(port).executor((Executors.newFixedThreadPool(16)))
				.addService(new WorkerSalaryImpl(new HashMap<>()))
				.addService(new PrimeNumbersStreamImpl())
				.addService(ProtoReflectionService.newInstance())
				.build()
				.start();
		logger.info("Server started, listening on " + port);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server...");
            grpcServer.this.stop();
            System.err.println("Server shut down.");
        }));
	}

	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		final grpcServer server = new grpcServer();
		server.start();
		server.blockUntilShutdown();
	}
}
