package org.sagemind.pubSubMessagingQueue;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        PubSubQueue psq = new PubSubQueue();
        Producer p = new Producer(psq);
        Subscriber s1 = new GeneralSubscriber("firstSubscriber");
        Subscriber s2 = new GeneralSubscriber("secondSubscriber");
        psq.addSubscriber(s1);
        psq.addSubscriber(s2);
        Thread producerThread = new Thread(p);
        producerThread.start();
    }
}

class Message {
    String text;
    Message(String text){
        this.text = text;
    }
}

interface Subscriber {
    public void onMessaage(Message m);
}

class PubSubQueue {

    List<Subscriber> subscribers;
    Queue<Message> q;
    ExecutorService es;

    int retryAttempts;

    PubSubQueue(){
        subscribers = new ArrayList<>();
        q = new LinkedList<>();
        es = Executors.newSingleThreadExecutor();
        es.submit(this::sendMessageToConsumers);
        retryAttempts = 3;
    }

    void addSubscriber(Subscriber s){
        subscribers.add(s);
    }

    void publishMessage(Message m){
        q.offer(m);
    }

    void sendMessageToConsumers(){
        while(true){
            if(!q.isEmpty()){
                Message m = q.poll();
                for (Subscriber s: subscribers){
                    sendMessageWithRetries(s, m);
                }
            }
        }
    }

    void sendMessageWithRetries(Subscriber s, Message m){
        int remRetryCount = retryAttempts;
        while (remRetryCount > 0) {
            try{
                s.onMessaage(m);
            } catch (Exception e){
                remRetryCount--;
            }
        }
    }
}


class Producer implements Runnable {

    PubSubQueue psq;

    Producer(PubSubQueue psq){
        this.psq = psq;
    }

    @Override
    public void run() {
        for(int i=0; i<100; i++) {
            psq.publishMessage(new Message(String.valueOf(i)));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class GeneralSubscriber implements Subscriber {

    String name;

    GeneralSubscriber(String name){
        this.name = name;
    }

    @Override
    public void onMessaage(Message m) {
        System.out.println("message received in consumer " + name + " : " + m.text);
    }
}
