package com.intel.dronekit;

import android.os.Handler;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HandlerTest {


    public static void main(String[] args) {
        HandlerTest test = new HandlerTest();
        test.testHandler();
    }

    final Semaphore semaphore = new Semaphore(1);

    private void testHandler() {

        System.out.println(Thread.currentThread() + "starting");
        semaphore.acquireUninterruptibly();

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread() + "doing work");
                semaphore.release();
            }
        });

        Runnable delayed = new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread() + "delayed work");

            }
        };
        handler.postDelayed(delayed, 400);

        Runnable delayed2 = new Runnable() {
            @Override
            public void run() {
                System.out.println(Thread.currentThread() + "delayed work");

            }
        };
        handler.postDelayed(delayed2, 100);

        System.out.println("waiting...");
        handler.postDelayed(() -> {
            System.out.println(Thread.currentThread() + "delayed 2");
        }, 500);

        semaphore.acquireUninterruptibly();
        handler.removeCallbacks(delayed);
        System.out.println("...done");


        handler.getLooper().awaitShutdown();
        System.out.println("--- fin ---");


    }
}