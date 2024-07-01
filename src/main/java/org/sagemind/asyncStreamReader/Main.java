package org.sagemind.asyncStreamReader;


public class Main {
}

interface Callable<T> {
    void execute(T data);
}

interface IStringReader {
    void onData(byte[] bytes, Callable<String> c );

}

class StreamReader implements IStringReader {

    State state;
    byte[] headerBuffer;
    byte[] bodyBuffer;
    int headerPtr;
    int bodyPtr;

    @Override
    public void onData(byte[] bytes, Callable<String> c) {
        for(byte b: bytes){
            if (state == State.HEADER){
                if(headerPtr < headerBuffer.length){
                    headerBuffer[headerPtr] = b;
                    headerPtr++;
                }
                if(headerPtr == headerBuffer.length){
                    state = State.BODY;
                    int size = convertToInt(headerBuffer);
                    bodyBuffer = new byte[size];
                    headerPtr=0;
                }
            }
            if (state == State.BODY){
                if(bodyPtr < bodyBuffer.length){
                    bodyBuffer[bodyPtr] = b;
                    bodyPtr++;
                }
                if(bodyPtr == bodyBuffer.length){
                    state = State.HEADER;
                    String s = convertToString(bodyBuffer);
                    c.execute(s);
                    bodyPtr = 0;
                }
            }
        }
    }
}

enum State {
    HEADER,
    BODY
}