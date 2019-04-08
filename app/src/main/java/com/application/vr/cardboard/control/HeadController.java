package com.application.vr.cardboard.control;

import com.application.vr.cardboard.motion.AccelerometerListener;

public class HeadController {
    private DownController dController;
    private TopController tController;
    private RightController rController;
    private LeftController lController;

    public HeadController(AccelerometerListener accListener) {
        dController = new DownController(accListener);
        tController = new TopController(accListener);
        rController = new RightController(accListener);
        lController = new LeftController(accListener);

        startListening(dController);
        startListening(tController);
        startListening(rController);
        startListening(lController);
    }

    private void startListening(IController controller) {
        Thread thread = new Thread(controller);
        thread.start();
    }

    private void stopListening(int side) {
        switch (side) {
            case Side.TOP :
                tController.stopListening();
                break;
            case Side.DOWN :
                dController.stopListening();
                break;
            case Side.RIGHT :
                rController.stopListening();
                break;
            case Side.LEFT :
                lController.stopListening();
                break;
        }
    }

    private void startListening(int side) {
        Thread thread = null;
        switch (side) {
            case Side.TOP :
                thread = new Thread(tController);
                break;
            case Side.DOWN :
                thread = new Thread(dController);
                break;
            case Side.RIGHT :
                thread = new Thread(rController);
                break;
            case Side.LEFT :
                thread = new Thread(lController);
                break;
        }
        if (null != thread) thread.start();
    }

    public interface Side {
        int TOP = 1;
        int DOWN = 2;
        int LEFT = 3;
        int RIGHT = 4;
    }
}
