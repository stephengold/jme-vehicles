package com.jayfella.jme.vehicle.examples.worlds;

import com.jayfella.jme.vehicle.World;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import java.util.logging.Logger;

/**
 * A single-chunk example World, built around Adi Barda's racetrack model.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Racetrack extends World {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Racetrack.class.getName());
    // *************************************************************************
    // World methods

    /**
     * Determine the preferred initial orientation for vehicles.
     *
     * @return the Y rotation angle (in radians, measured counter-clockwise as
     * seen from above)
     */
    @Override
    public float dropYRotation() {
        return FastMath.PI;
    }

    /**
     * Determine the distance to the camera's far plane.
     *
     * @return the distance (in world units, &gt;0)
     */
    @Override
    public float farDistance() {
        return 3_650f;
    }

    /**
     * Load this World from assets.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        assert getCgm() == null : "The model is already loaded.";

        String assetFolder = "/Models/race1/";
        String assetPath = assetFolder + "race1.j3o";
        Node loadedCgm = (Node) assetManager.loadModel(assetPath);
        setCgm(loadedCgm);

        Platform platform = JmeSystem.getPlatform();
        if (platform == Platform.Windows64) {
            assetPath = assetFolder + "shapes/env-shape-Windows64.j3o";
        } else {
            assetPath = assetFolder + "shapes/env-shape.j3o";
        }

        CollisionShape shape;
        try {
            shape = (CollisionShape) assetManager.loadAsset(assetPath);
        } catch (AssetNotFoundException exception) {
            shape = CollisionShapeFactory.createMeshShape(loadedCgm);
        }
        setCollisionShape(shape);
    }

    /**
     * Locate the drop point, which lies directly above the preferred initial
     * location for vehicles.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    public void locateDrop(Vector3f storeResult) {
        storeResult.set(-92f, 6f, 675f);
    }

    /**
     * Reposition the default Camera to the initial location and orientation for
     * this World.
     */
    @Override
    public void resetCameraPosition() {
        Camera camera = getCamera();
        camera.setLocation(new Vector3f(-96.7f, 2.5f, 676.4f));
        camera.lookAt(new Vector3f(-92f, 0f, 675f), Vector3f.UNIT_Y);
    }
}
