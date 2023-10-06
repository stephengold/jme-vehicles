package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import com.jayfella.jme.vehicle.niftydemo.view.Cameras;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.util.logging.Logger;
import jme3utilities.math.noise.Generator;

/**
 * Process actions that start with the word "reset".
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class ResetAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ResetAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private ResetAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "reset".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;
        DemoState demoState = MavDemo2.getDemoState();

        switch (actionString) {
            case Action.resetCameraFov:
                Cameras.resetFov();
                break;

            case Action.resetCameraOffset:
                Cameras.resetOffset();
                break;

            case Action.resetElapsedTime:
                demoState.resetElapsedTime();
                break;

            case Action.resetPropProposal:
                demoState.getPropProposal().reset();
                break;

            case Action.resetVehicle:
                resetVehicle();
                break;

            default:
                handled = false;
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Re-orient the Vehicle so that it is upright, reset its motion to zero,
     * and attempt to locate it someplace it won't immediately collide.
     */
    private static void resetVehicle() {
        DemoState demoState = MavDemo2.getDemoState();
        Vehicle vehicle = demoState.getVehicles().getSelected();
        VehicleControl control = vehicle.getVehicleControl();
        float[] angles = new float[3];
        control.getPhysicsRotation().toAngles(angles);

        Quaternion newRotation = new Quaternion();
        newRotation.fromAngles(0f, angles[1], 0f);
        control.setPhysicsRotation(newRotation);

        control.setAngularVelocity(Vector3f.ZERO);
        control.setLinearVelocity(Vector3f.ZERO);

        Vector3f location = control.getPhysicsLocation();
        PhysicsSpace space = (PhysicsSpace) control.getCollisionSpace();
        control.setPhysicsSpace(null);
        Generator generator = demoState.getPrGenerator();
        if (space.contactTest(control, null) > 0) {
            Vector3f newLocation = location.add(0f, 1f, 0f);
            for (int iteration = 0; iteration < 9; ++iteration) {
                control.setPhysicsLocation(newLocation);
                if (space.contactTest(control, null) == 0) {
                    break;
                }
                Vector3f offset = generator.nextVector3f();
                offset.multLocal(0.1f * (iteration + 1));
                newLocation.addLocal(offset);
            }
        }
        control.setPhysicsSpace(space);
    }
}
