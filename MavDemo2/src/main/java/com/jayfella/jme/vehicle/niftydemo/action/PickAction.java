package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.Prop;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.view.View;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.Vector3f;
import java.util.List;
import java.util.logging.Logger;

/**
 * Process actions that start with the word "pick".
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class PickAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(PickAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private PickAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "pick".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;

        switch (actionString) {
            case Action.pickProp:
                Prop picked = pickProp();
                MavDemo2.getDemoState().selectProp(picked);
                break;

            default:
                handled = false;
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Pick the nearest Prop under the mouse cursor using a physics ray.
     *
     * @return the pre-existing instance, or null of none found
     */
    private static Prop pickProp() {
        Vector3f near = new Vector3f();
        Vector3f far = new Vector3f();
        MavDemo2.findAppState(View.class).mouseRay(near, far);

        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        PhysicsSpace physicsSpace = bas.getPhysicsSpace();
        List<PhysicsRayTestResult> results = physicsSpace.rayTest(near, far);

        // Calculate the offset from near end to the far end.
        Vector3f offset = far.subtract(near);
        /*
         * Collision results are sorted by increasing distance from the camera,
         * so the first result is also the nearest one.
         */
        for (PhysicsRayTestResult result : results) {
            /*
             * If the dot product of the normal with the offset is negative,
             * then the triangle is facing the camera.
             */
            Vector3f worldNormal = result.getHitNormalLocal(null);
            PhysicsCollisionObject pco = result.getCollisionObject();
            float dotProduct = offset.dot(worldNormal);
            if (dotProduct < 0f) {
                Object appData = pco.getApplicationData();
                if (appData instanceof Prop) {
                    return (Prop) appData;
                }
            }
        }

        return null;
    }
}
