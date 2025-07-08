package sr.grpc.server;

import io.grpc.stub.StreamObserver;

import sr.grpc.gen.PrimeNumbersStreamGrpc.PrimeNumbersStreamImplBase;
import sr.grpc.gen.Number;
import sr.grpc.gen.Report;

public class PrimeNumbersStreamImpl extends PrimeNumbersStreamImplBase {
	@Override
	public void generatePrimeNumbers(Number request, StreamObserver<Number> responseObserver) {
		System.out.println("generatePrimeNumbers is starting (limit=" + request.getNum() + ")");
		for (int i = 0; i < request.getNum(); i++) {
			if(isPrime(i)) {
				Number number = Number.newBuilder().setNum(i).build();
				responseObserver.onNext(number);
			}
		}
		responseObserver.onCompleted();
		System.out.println("generatePrimeNumbers completed");
	}

	private boolean isPrime(int val) {
		if(val < 2) {
			return false;
		} else if(val == 2) {
			return true;
		} else if(val % 2 == 0) {
			return false;
		}

		int div = 3;
		while(div * div <= val) {
			if(val % div == 0) {
				return false;
			}
			div += 2;
		}
		return true;
	}

	@Override
	public StreamObserver<Number> countPrimeNumbers(StreamObserver<Report> responseObserver) {
		return new MyStreamObserver<>(responseObserver);
	}
}

class MyStreamObserver<Number> implements StreamObserver<Number> {
	private int count = 0;
	private final StreamObserver<Report> responseObserver;

	MyStreamObserver(StreamObserver<Report> responseObserver) {
		System.out.println("Let's begin countPrimeNumbers");
		this.responseObserver = responseObserver;
	}

	@Override
	public void onNext(Number number) {
		System.out.println("Received number " + ((sr.grpc.gen.Number)number).getNum());
		count++;
	}

	@Override
	public void onError(Throwable t) {
		System.out.println("Error: " + t.getMessage());
	}

	@Override
	public void onCompleted() {
		responseObserver.onNext(Report.newBuilder().setCount(count).build());
		responseObserver.onCompleted();
		System.out.println("End of countPrimeNumbers");
	}
}
