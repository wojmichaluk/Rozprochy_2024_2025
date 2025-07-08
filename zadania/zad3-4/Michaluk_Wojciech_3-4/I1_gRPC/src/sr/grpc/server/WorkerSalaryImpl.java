package sr.grpc.server;

import io.grpc.stub.StreamObserver;

import sr.grpc.gen.Date;
import sr.grpc.gen.QuarterSalaries;
import sr.grpc.gen.Empty;
import sr.grpc.gen.SalariesList;
import sr.grpc.gen.WorkerSalaryGrpc.WorkerSalaryImplBase;

import java.util.HashMap;

public class WorkerSalaryImpl extends WorkerSalaryImplBase {
    final HashMap<Date, SalariesList> salaries;

    public WorkerSalaryImpl(HashMap<Date, SalariesList> salaries) {
        this.salaries = salaries;
    }

    @Override
    public void insertQuarterSalaries(QuarterSalaries request, StreamObserver<Empty> responseObserver) {
        Date date = request.getDate();
        if(salaries.containsKey(date)) {
            System.out.println("Updating salary for year " + date.getYear() + ", quarter " + date.getQuarter());
        } else {
            System.out.println("Adding salary for year " + date.getYear() + ", quarter " + date.getQuarter());
        }
        salaries.put(date, SalariesList.newBuilder().addAllSalaries(request.getSalariesList()).build());
        responseObserver.onNext(new Empty());
        responseObserver.onCompleted();
    }

    @Override
    public void getQuarterSalaries(Date request, StreamObserver<SalariesList> responseObserver) {
        if(salaries.containsKey(request)) {
            System.out.println("Retrieving salaries data for year " + request.getYear() + ", quarter " + request.getQuarter());
            SalariesList quarterSalaries = salaries.get(request);
            responseObserver.onNext(quarterSalaries);
            responseObserver.onCompleted();
        } else {
            System.out.println("Incorrect date (year: " + request.getYear() + ", quarter: " + request.getQuarter() + ") - found no info about salaries");
            responseObserver.onError(new Exception("Invalid date"));
        }
    }
}
