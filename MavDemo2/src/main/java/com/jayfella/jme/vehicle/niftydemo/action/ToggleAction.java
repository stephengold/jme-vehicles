package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import com.jayfella.jme.vehicle.niftydemo.state.Vehicles;
import com.jayfella.jme.vehicle.niftydemo.view.View;
import com.jayfella.jme.vehicle.niftydemo.view.ViewFlags;
import com.jayfella.jme.vehicle.part.Engine;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jme3.bullet.BulletAppState;
import java.util.logging.Logger;

/**
 * Process actions that start with the word "toggle".
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class ToggleAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ToggleAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private ToggleAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "toggle".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;

        DemoState demoState = MavDemo2.getDemoState();
        Vehicles vehicles = demoState.getVehicles();
        Vehicle vehicle = vehicles.getSelected();

        switch (actionString) {
            case Action.toggleEngine:
                Engine engine = vehicle.getEngine();
                boolean wasRunning = engine.isRunning();
                engine.setRunning(!wasRunning);
                break;

            case Action.togglePause:
                togglePause();
                break;

            case Action.togglePhysicsDebug:
                togglePhysicsDebug();
                break;

            case Action.toggleReverse:
                GearBox box = vehicle.getGearBox();
                boolean wasReversing = box.isInReverse();
                box.setReversing(!wasReversing);
                break;

            default:
                handled = false;
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Process a "toggle pause" action.
     */
    private static void togglePause() {
        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);

        float speed = bas.getSpeed();
        if (speed > 0f) { // was running
            bas.setSpeed(0f);
        } else {
            bas.setSpeed(1f);
        }
    }

    /**
     * Process a "toggle physics" action.
     */
    private static void togglePhysicsDebug() {
        View view = MavDemo2.findAppState(View.class);

        boolean wasEnabled = view.isEnabled(ViewFlags.PhysicsJoints)
                || view.isEnabled(ViewFlags.PropShapes)
                || view.isEnabled(ViewFlags.VehicleShapes)
                || view.isEnabled(ViewFlags.WorldShapes);
        boolean enable = !wasEnabled;

        view.setEnabled(ViewFlags.PhysicsJoints, enable);
        view.setEnabled(ViewFlags.PropShapes, enable);
        view.setEnabled(ViewFlags.VehicleShapes, enable);
        view.setEnabled(ViewFlags.WorldShapes, enable);
    }
}
