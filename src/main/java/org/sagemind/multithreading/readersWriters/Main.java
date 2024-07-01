package org.sagemind.multithreading.readersWriters;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {
}

class DB {

    String data;
//    ReadWriteLock rwl = new ReentrantReadWriteLock();
    Semaphore rs = new Semaphore(1);
    Semaphore s = new Semaphore(1);

    int readersCount = 0;

    void write(String newData) throws InterruptedException {
//        rwl.writeLock().lock();
        s.acquire();
        this.data = newData;
        s.release();
//        rwl.writeLock().unlock();
//        System.out.println(data);
    }

    String read() throws InterruptedException {

        rs.acquire();
        try{
            if(readersCount == 0) {
                s.acquire();
            }
            readersCount++;
        } finally {
            rs.release();
        }
        rs.release();
        String d = this.data;
        rs.acquire();
        readersCount--;
        if(readersCount == 0){
            s.release();
        }
        rs.release();
        return d;
    }

}
