package com.jayfella.jme.vehicle.niftydemo.view;

import com.jayfella.jme.vehicle.Prop;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.World;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.debug.BulletDebugAppState;
import com.jme3.bullet.joints.PhysicsJoint;
import java.util.logging.Logger;

/**
 * Options for debug visualization of physics joints and collision shapes.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class ViewPhysics implements BulletDebugAppState.DebugAppStateFilter {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ViewPhysics.class.getName());
    // *************************************************************************
    // fields

    /**
     * true&rarr;visualize physics joints, false&rarr;hide physics joints
     */
    private boolean joints = false;
    /**
     * true&rarr;visualize prop physics, false&rarr;hide prop collision shapes
     */
    private boolean props = false;
    /**
     * true&rarr;visualize vehicle physics, false&rarr;hide vehicle collision
     * shapes
     */
    private boolean vehicles = false;
    /**
     * true&rarr;visualize world physics, false&rarr;hide world collision shapes
     */
    private boolean world = false;
    // *************************************************************************
    // constructors

    /**
     * Instantiate with the default settings.
     */
    ViewPhysics() {
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
            case PhysicsJoints:
                result = joints;
                break;

            case PropShapes:
                result = props;
                break;

            case VehicleShapes:
                result = vehicles;
                break;

            case WorldShapes:
                result = world;
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
            case PhysicsJoints:
                this.joints = newValue;
                break;

            case PropShapes:
                this.props = newValue;
                break;

            case VehicleShapes:
                this.vehicles = newValue;
                break;

            case WorldShapes:
                this.world = newValue;
                break;

            default:
                throw new IllegalArgumentException(viewFlag.toString());
        }
    }
    // *************************************************************************
    // DebugAppStateFilter methods

    /**
     * Test whether the specified physics object should be displayed.
     *
     * @param physicsObject the joint or collision object to test (unaffected)
     * @return return true if the object should be displayed, false if not
     */
    @Override
    public boolean displayObject(Object physicsObject) {
        boolean result = false;

        if (physicsObject instanceof PhysicsCollisionObject) {
            PhysicsCollisionObject pco = (PhysicsCollisionObject) physicsObject;
            Object appData = pco.getApplicationData();
            if (appData instanceof Prop) {
                result = props;
            } else if (appData instanceof Vehicle) {
                result = vehicles;
            } else if (appData instanceof World) {
                result = world;
            }
        } else if (physicsObject instanceof PhysicsJoint) {
            result = joints;
        }

        return result;
    }
}
