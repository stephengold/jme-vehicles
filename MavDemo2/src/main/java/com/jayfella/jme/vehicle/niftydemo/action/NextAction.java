package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.view.Cameras;
import com.jayfella.jme.vehicle.niftydemo.view.View;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import java.util.logging.Logger;

/**
 * Process actions that start with the word "next".
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class NextAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(NextAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private NextAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "next".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;
        switch (actionString) {
            case Action.nextCamera:
                Cameras.next();
                break;

            case Action.nextPerformanceMode:
                nextPerformanceMode();
                break;

            case Action.nextTimeStep:
                nextTimeStep();
                break;

            default:
                handled = false;
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Process a "next performanceMode" action.
     */
    private static void nextPerformanceMode() {
        View view = MavDemo2.findAppState(View.class);
        view.selectNextPerformanceMode();
    }

    /**
     * Process a "next timeStep" action.
     */
    private static void nextTimeStep() {
        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        float speed = bas.getSpeed();
        if (speed == 0f) { // paused
            PhysicsSpace physicsSpace = bas.getPhysicsSpace();
            float timeStep = physicsSpace.getAccuracy();
            physicsSpace.update(timeStep);
        }
    }
}
