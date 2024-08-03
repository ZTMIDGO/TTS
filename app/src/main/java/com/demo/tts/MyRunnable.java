package com.demo.tts;

public abstract class MyRunnable implements Runnable {
    private boolean interrupt;
    private boolean pause;

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public boolean isPause() {
        return pause;
    }

    public void interrupt(){
        interrupt = true;
    }

    public boolean isInterrupt() {
        return interrupt;
    }
}
