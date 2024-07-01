package org.sagemind.multithreading;

public class Main {
    public static void main(String[] args) {
        OddEven oe = new OddEven(20);
        Thread t1 = new Thread(()->{
            try {
                while (true) {
                    oe.printOdd();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Thread t2 = new Thread(()->{
            try {
                while (true) {
                    oe.printEven();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t1.start();
        t2.start();
    }
}

class OddEven {

    int n;
    int currNumber;

    OddEven(int n) {
        this.n = n;
        this.currNumber = 1;
    }

    synchronized void printOdd() throws InterruptedException {
        while(currNumber%2==0){
            wait();
        }
        if(currNumber>n){
            currNumber++;
            notify();
            return;
        }
        System.out.println(Thread.currentThread().getName() + " : " + currNumber);
        currNumber++;
        notifyAll();
    }

    synchronized void printEven() throws InterruptedException {
        while(currNumber%2!=0){
            wait();
        }
        if(currNumber>n){
            currNumber++;
            notify();
            return;
        }
        System.out.println(Thread.currentThread().getName() + " : " + currNumber);
        currNumber++;
        notifyAll();
    }

}
