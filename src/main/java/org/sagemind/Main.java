package org.sagemind;
import java.util.*;
import java.util.concurrent.*;
import org.sagemind.game.commands.RegisterUserCommand;

public class Main {
    public static void main(String[] args) {

        System.out.println("Hello world!");
        IExecutorService es = new FixedPoolSizeExecutorService(3);
        for(int i=0; i<10; i++){
            es.submit(() -> {
                try{
                    Thread.sleep(5000);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            });
        }
        es.shutDown();
        System.out.println("Done");
    }
}


interface IExecutorService {
    public void submit(Runnable r);
    public void shutDown();
}

class FixedPoolSizeExecutorService implements IExecutorService {

    int capacity;
    BlockingQueue<Runnable> q = new LinkedBlockingQueue<>();
    List<Thread> taskRunners = new ArrayList<>();

    FixedPoolSizeExecutorService(int c) {
        this.capacity = c;
        for(int i=0; i<c; i++){
            Thread t = new Thread(() -> {
                runTasks();
            });
            taskRunners.add(t);
            t.start();
        }
    }

    public void submit(Runnable r) {
        q.add(r);
    }

    public void shutDown() {
        while(!q.isEmpty());
        for(Thread t: taskRunners) {
            t.interrupt();
        }
    }

    private void runTasks() {
        while(!Thread.currentThread().isInterrupted()) {
            if(!q.isEmpty()) {
                System.out.println("Thread: " + Thread.currentThread().getName());
                q.poll().run();
            }
        }
    }

}