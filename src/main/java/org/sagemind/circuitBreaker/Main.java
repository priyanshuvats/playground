package org.sagemind.circuitBreaker;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        ProductServiceWrapper w = new ProductServiceWrapper();
        for(int i=0; i<400; i++){
            w.cbRpcCall();
        }
    }
}

class ProductServiceWrapper {

    CB cb = new CustomCB(5,0.5,5,10);

    int currCall = 0;

    public void cbRpcCall(){
        if(cb.isRequestAllowed()){
            try{
                System.out.println("Allowed");
                String val = rpcCall();
                cb.handleSuccess();
            } catch(Exception e){
                // System.out.println("Failure");
                cb.handleFailure();
            }
        } else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("CB Open");
        }
    }

    public String rpcCall() throws Exception{
        if(currCall>=100 && currCall<=210){
            currCall++;
            throw new Exception("Service Failure");
        }
        try{
            Thread.sleep(100);
        } catch (Exception e){
            System.out.println("Interrupted");
        }
        currCall++;
        return "Success";
    }

}


interface CB {
    public boolean isRequestAllowed();
    public void handleSuccess();
    public void handleFailure();
}

class CustomCB implements CB {

    private int timeWindowSec;
    private double failureRatioThreshhold;
    private int circuitCloseTimeSec;
    private int minRequests;
    private CBStatus cbStatus;
    private long lastRequestSec;
    private List<RequestBucket> requestBuckets;

    CustomCB(int timeWindowSec, double failureRatioThreshhold, int circuitCloseTimeSec, int minRequests){
        this.timeWindowSec = timeWindowSec;
        this.failureRatioThreshhold=failureRatioThreshhold;
        this.circuitCloseTimeSec=circuitCloseTimeSec;
        this.minRequests=minRequests;
        requestBuckets = new ArrayList<>();
        for (int i = 0; i < timeWindowSec; i++) {
            requestBuckets.add(null);
        }
        cbStatus = CBStatus.CLOSED;
    }

    public boolean isRequestAllowed(){
        long currTimeInSec = System.currentTimeMillis()/1000;
        if(cbStatus == CBStatus.OPEN){
            if(currTimeInSec-lastRequestSec > circuitCloseTimeSec){
                cbStatus = CBStatus.CLOSED;
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public void handleSuccess(){
        RequestBucket curr = registerRequestInBucket();
        curr.incSuccess();
        lastRequestSec = curr.epochSec;
    }

    public void handleFailure(){
        RequestBucket curr = registerRequestInBucket();
        curr.incFailure();
        lastRequestSec = curr.epochSec;
        RC rc = getFailureRatio();
        if(rc.ratio>=failureRatioThreshhold && rc.reqCount >= minRequests){
            cbStatus = CBStatus.OPEN;
        }
    }

    private RequestBucket registerRequestInBucket(){
        long currTimeInSec = System.currentTimeMillis()/1000;
        int idx = (int)(currTimeInSec%(long)timeWindowSec);
        RequestBucket currBucket = requestBuckets.get(idx);
        if(currBucket!=null && currBucket.epochSec==currTimeInSec){
            return currBucket;
        } else {
            requestBuckets.set(idx, new RequestBucket(currTimeInSec));
            return requestBuckets.get(idx);
        }
    }

    private RC getFailureRatio(){
        long currTimeInSec = System.currentTimeMillis()/1000;
        int successCount=0;
        int failureCount=0;
        for(RequestBucket bucket: requestBuckets){
            if(currTimeInSec - bucket.epochSec <= timeWindowSec){
                successCount+=bucket.successCount;
                failureCount+=bucket.failureCount;
            }
        }
        if(failureCount==0){return new RC(0.0,0);}

        int totalReq = failureCount + successCount;
        return new RC((double)failureCount/(double)totalReq, totalReq);
    }

}

class RC {
    double ratio;
    int reqCount;
    RC(double ratio, int reqCount){
        this.ratio = ratio;
        this.reqCount=reqCount;
    }
}

class RequestBucket {
    long epochSec;
    int successCount;
    int failureCount;
    RequestBucket(long epochSec){
        this.epochSec = epochSec;
        successCount=0;
        failureCount=0;
    }
    void incSuccess(){
        successCount++;
    }
    void incFailure(){
        failureCount++;
    }
}

enum CBStatus {
    OPEN,
    CLOSED
}

class CircuitOpenException extends Exception {
    CircuitOpenException(){
        super("CIRCUIT IS OPEN");
    }
}

