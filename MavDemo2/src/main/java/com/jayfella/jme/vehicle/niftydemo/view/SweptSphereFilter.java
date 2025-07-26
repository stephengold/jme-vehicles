package com.jayfella.jme.vehicle.niftydemo.view;

import com.jayfella.jme.vehicle.Prop;
import com.jayfella.jme.vehicle.Vehicle;
import com.jme3.bullet.CollisionSpace;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.debug.BulletDebugAppState;
import com.jme3.bullet.objects.PhysicsRigidBody;
import java.util.logging.Logger;

/**
 * Options for debug visualization of swept spheres.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class SweptSphereFilter implements BulletDebugAppState.DebugAppStateFilter {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SweptSphereFilter.class.getName());
    // *************************************************************************
    // fields

    /**
     * true&rarr;visualize swept spheres for props
     */
    private boolean enableProps;
    /**
     * true&rarr;visualize swept spheres for vehicles
     */
    private boolean enableVehicles;
    // *************************************************************************
    // constructors

    /**
     * Instantiate with the default settings.
     */
    SweptSphereFilter() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Test whether the specified view flag is set.
     *
     * @param viewFlag which flag to test (not null)
     * @return true if visualization is enabled, otherwise false
     */
    boolean isEnabled(ViewFlags viewFlag) {
        boolean result;

        switch (viewFlag) {
            case PropSpheres:
                result = enableProps;
                break;

            case VehicleSpheres:
                result = enableVehicles;
                break;

            default:
                throw new IllegalArgumentException(viewFlag.toString());
        }
        return result;
    }

    /**
     * Configure the specified view flag.
     *
     * @param viewFlag which flag to set (not null)
     * @param newValue true to enable visualization, false to disable it
     */
    void setEnabled(ViewFlags viewFlag, boolean newValue) {
        switch (viewFlag) {
            case PropSpheres:
                this.enableProps = newValue;
                break;

            case VehicleSpheres:
                this.enableVehicles = newValue;
                break;

            default:
                throw new IllegalArgumentException(viewFlag.toString());
        }
    }
    // *************************************************************************
    // DebugAppStateFilter methods

    /**
     * Test whether the specified physics object should have its swept sphere
     * visualized.
     *
     * @param physicsObject the joint or collision object to test (unaffected)
     * @return return true if the object should be displayed, false if not
     */
    @Override
    public boolean displayObject(Object physicsObject) {
        boolean result = false;

        if (physicsObject instanceof PhysicsCollisionObject) {
            PhysicsCollisionObject pco = (PhysicsCollisionObject) physicsObject;
            Object applicationData = pco.getApplicationData();
            if (applicationData instanceof Prop && enableProps
                    || applicationData instanceof Vehicle && enableVehicles) {
                PhysicsRigidBody body = (PhysicsRigidBody) pco;

                CollisionSpace space = body.getCollisionSpace();
                float timeStep = ((PhysicsSpace) space).getAccuracy();
                float squaredSpeed = body.getSquaredSpeed();
                float squareMotion = squaredSpeed * timeStep * timeStep;
                float threshold = body.getCcdSquareMotionThreshold();
                if (squareMotion > threshold) {
                    result = true;
                }
            }
        }

        return result;
    }
}
