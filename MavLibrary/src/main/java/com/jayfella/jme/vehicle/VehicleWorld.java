package com.jayfella.jme.vehicle;

import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import jme3utilities.DecalManager;

/**
 * A 3-D world for vehicles.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public interface VehicleWorld {
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the preferred initial orientation for vehicles.
     *
     * @return the Y rotation angle (in radians, measured counter-clockwise as
     * seen from above)
     */
    float dropYRotation();

    /**
     * Access the AssetManager.
     *
     * @return the pre-existing instance (not null)
     */
    AssetManager getAssetManager();

    /**
     * Access the DecalManager.
     *
     * @return the pre-existing instance (not null)
     */
    DecalManager getDecalManager();

    /**
     * Access the scene-graph node for adding probes and attaching spatials.
     *
     * @return the pre-existing instance (not null)
     */
    Node getParentNode();

    /**
     * Access the PhysicsSpace.
     *
     * @return the pre-existing instance (not null)
     */
    PhysicsSpace getPhysicsSpace();

    /**
     * Access the AppStateManager.
     *
     * @return the pre-existing instance (not null)
     */
    AppStateManager getStateManager();

    /**
     * Locate the drop point, which lies directly above the preferred initial
     * location for vehicles.
     *
     * @param storeResult storage for the result (not null)
     */
    void locateDrop(Vector3f storeResult);
}
