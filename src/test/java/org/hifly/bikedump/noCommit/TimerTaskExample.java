package org.hifly.bikedump.noCommit;


import java.util.Timer;
import java.util.TimerTask;

class SayHello extends TimerTask {


    public void run() {
        System.out.println("Hello");
    }
}

public class TimerTaskExample {

    public static void main (String[] args) {
        Timer timer = new Timer();
        timer.schedule(new SayHello(), 0, 5000);

    }
}
