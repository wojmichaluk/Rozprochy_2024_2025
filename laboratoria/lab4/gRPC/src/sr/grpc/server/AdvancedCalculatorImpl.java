package sr.grpc.server;

import sr.grpc.gen.ComplexArithmeticOpResult;
import sr.grpc.gen.AdvancedCalculatorGrpc.AdvancedCalculatorImplBase;

public class AdvancedCalculatorImpl extends AdvancedCalculatorImplBase 
{
	@Override
	public void complexOperation(sr.grpc.gen.ComplexArithmeticOpArguments request,
			io.grpc.stub.StreamObserver<sr.grpc.gen.ComplexArithmeticOpResult> responseObserver) 
	{
		System.out.println("multipleArgumentsRequest (" + request.getOptypeValue() + ", #" + request.getArgsCount() +")");

		// przeniesione przed if w ramach punktu 2.4.11
		double res = 0;

		if(request.getArgsCount() == 0) {
			System.out.println("No arguments");
		} else { // switch wrzucony do else w ramach punktu 2.4.11
			switch (request.getOptype()) {
				case SUM:
					for (Double d : request.getArgsList()) res += d;
					break;
				case AVG:
					for (Double d : request.getArgsList()) res += d;
					res /= request.getArgsCount();
					break;
				case MIN:
					break;
				case MAX:
					break;
				case MUL: // dodane w 2.4.11
					res = 1;
					for (Double d : request.getArgsList()) res *= d;
					break;
				case UNRECOGNIZED:
					break;
			}
		}

		ComplexArithmeticOpResult result = ComplexArithmeticOpResult.newBuilder().setRes(res).build();
		responseObserver.onNext(result);
		responseObserver.onCompleted();
	}
}
