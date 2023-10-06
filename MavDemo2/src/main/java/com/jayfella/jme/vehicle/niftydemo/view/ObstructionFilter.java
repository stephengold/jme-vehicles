package com.jayfella.jme.vehicle.niftydemo.view;

import com.jayfella.jme.vehicle.World;
import com.jme3.bullet.debug.BulletDebugAppState;
import com.jme3.bullet.objects.PhysicsRigidBody;
import java.util.logging.Logger;

/**
 * A DebugAppStateFilter that selects only those physics objects that
 * OrbitCamera should treat as obstructions.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ObstructionFilter
        implements BulletDebugAppState.DebugAppStateFilter {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ObstructionFilter.class.getName());
    // *************************************************************************
    // DebugAppStateFilter methods

    /**
     * Test whether the specified physics object should be treated as an
     * obstruction.
     *
     * @param physicsObject the joint or collision object to test (unaffected)
     * @return return true if the object is an obstruction, false if it isn't
     */
    @Override
    public boolean displayObject(Object physicsObject) {
        if (physicsObject instanceof PhysicsRigidBody) {
            Object applicationObject
                    = ((PhysicsRigidBody) physicsObject).getApplicationData();
            if (applicationObject instanceof World) {
                return true;
            } // props are never treated as obstructions
        }

        return false;
    }
}
