package com.jayfella.jme.vehicle.shape;

import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.asset.plugins.ClasspathLocator;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.bullet.util.NativeLibrary;
import com.jme3.export.binary.BinaryLoader;
import com.jme3.material.plugins.J3MLoader;
import com.jme3.math.Transform;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.JmeSystem;
import com.jme3.system.NativeLibraryLoader;
import com.jme3.system.Platform;
import com.jme3.texture.plugins.AWTLoader;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.math.MyMath;
import jme3utilities.minie.PhysicsDumper;

/**
 * A console application to pre-compute collision shapes for assets.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class CreateShapes {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(CreateShapes.class.getName());
    // *************************************************************************
    // fields

    /**
     * AssetManager for loading C-G models
     */
    final private static AssetManager assetManager = new DesktopAssetManager();
    /**
     * dump debugging information to System.out
     */
    final private static PhysicsDumper dumper = new PhysicsDumper();
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private CreateShapes() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Main entry point for the CreateShapes application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        NativeLibraryLoader.loadNativeLibrary("bulletjme", true);
        NativeLibrary.setStartupMessageEnabled(false);

        // Configure the AssetManager (from scratch).
        assetManager.registerLoader(AWTLoader.class, "jpeg", "png");
        assetManager.registerLoader(BinaryLoader.class, "j3o");
        assetManager.registerLoader(J3MLoader.class, "j3m", "j3md");
        assetManager.registerLocator(null, ClasspathLocator.class);

        // Create collision shapes for vehicle chassis.
        createBikeChassisShapes("classic_motorcycle", "chassis");
        createChassisShape("GT", "scene.gltf");
        createChassisShape("Tank", "chassis");
        createChassisShape("ford_ranger", "pickup");
        createChassisShape("gtr_nismo", "scene.gltf");
        createChassisShape("hcr2_buggy", "dune-buggy");
        createChassisShape("hcr2_rotator", "chassis");
        createChassisShape("modern_hatchback", "hatchback");

        // Create a convex hull for each Prop model.
        createPropHull("barrier_pack", "barrel1");
        createPropHull("barrier_pack", "barrel2");
        createPropHull("barrier_pack", "barrier");
        createPropHull("barrier_pack", "barrier_painted");
        createPropHull("barrier_pack", "cone1");
        createPropHull("barrier_pack", "cone2");
        createPropHull("barrier_pack", "fenced_barrier");
        createPropHull("barrier_pack", "marker");
        createPropHull("barrier_pack", "short_barrier");
        createPropHull("barrier_pack", "short_barrier_painted");
        createPropHull("barrier_pack", "short_barrier_signed");
        createPropHull("barrier_pack", "wall_barrier");
        createPropHull("barrier_pack", "wall_barrier_painted");
        createPropHull("barrier_pack", "warning_sign");
        createPropHull("barrier_pack", "weight");

        // Create a CollisionShape for each World.
        createWorldShape("race1", "race1");
        createWorldShape("vehicle-playground", "vehicle-playground");
    }
    // *************************************************************************
    // private methods

    /**
     * Create collision shapes for an articulated vehicle where the engine and
     * the steering are in separate physics bodies.
     *
     * @param folderName the name of the folder containing the C-G model
     * @param cgmBaseFileName the base filename of the C-G model
     */
    private static void createBikeChassisShapes(String folderName,
            String cgmBaseFileName) {
        assetManager.clearCache(); // to reclaim direct buffer memory

        String cgmAssetPath = String.format("/Models/%s/%s.j3o", folderName,
                cgmBaseFileName);
        Node cgmRoot = (Node) assetManager.loadModel(cgmAssetPath);

        String description = folderName + " engine body";
        Spatial subtree = cgmRoot.getChild("engine subtree");
        createDynamicShape(description, subtree, folderName, "engine.j3o");

        description = folderName + " steering body";
        subtree = cgmRoot.getChild("steering subtree");
        createDynamicShape(description, subtree, folderName, "steering.j3o");
    }

    /**
     * Create a CollisionShape for a single-body vehicle chassis.
     *
     * @param folderName the name of the folder containing the C-G model
     * @param cgmBaseFileName the base filename of the C-G model
     */
    private static void createChassisShape(String folderName,
            String cgmBaseFileName) {
        assetManager.clearCache(); // to reclaim direct buffer memory

        String cgmAssetPath = String.format("/Models/%s/%s.j3o", folderName,
                cgmBaseFileName);
        Spatial cgmRoot = assetManager.loadModel(cgmAssetPath);

        String description = folderName + " chassis";
        String fileName = "chassis-shape.j3o";
        createDynamicShape(description, cgmRoot, folderName, fileName);
    }

    /**
     * Create a CollisionShape for a single body in a vehicle chassis.
     *
     * @param description a textual description of the body being created (not
     * null, not empty)
     * @param subtree the scene-graph subtree on which to base the shape (not
     * null, unaffected)
     * @param folderName the name of the folder containing the C-G model (not
     * null, not empty)
     * @param fileName the final component of the output filepath (not null,
     * ending in ".j3o")
     */
    private static void createDynamicShape(String description, Spatial subtree,
            String folderName, String fileName) {
        assert description != null;
        assert !description.isEmpty();
        assert subtree != null;
        assert folderName != null;
        assert !folderName.isEmpty();
        assert fileName != null;
        assert fileName.endsWith(".j3o");

        System.out.printf("%nCreate shape for %s ... ", description);
        System.out.flush();

        CollisionShape collisionShape
                = CollisionShapeFactory.createDynamicMeshShape(subtree);

        System.out.printf("done!%n");
        dumper.dump(collisionShape, "    ");
        System.out.flush();

        // Save the collision shape in J3O format.
        String writeFilePath = "src/main/resources/Models/" + folderName
                + "/shapes/" + fileName;
        Heart.writeJ3O(writeFilePath, collisionShape);
    }

    /**
     * Create a convex hull for a Prop.
     *
     * @param folderName the name of the folder containing the C-G model
     * @param cgmBaseFileName the base filename of the C-G model
     */
    private static void createPropHull(String folderName,
            String cgmBaseFileName) {
        assetManager.clearCache(); // to reclaim direct buffer memory

        String cgmAssetPath = String.format("/Models/Props/%s/%s.j3o",
                folderName, cgmBaseFileName);
        Spatial cgmRoot = assetManager.loadModel(cgmAssetPath);
        Transform rootTransform = cgmRoot.getLocalTransform();
        if (!MyMath.isIdentity(rootTransform)) {
            throw new IllegalStateException(
                    cgmBaseFileName + " rootTransform = " + rootTransform);
        }

        System.out.printf("%nCreate convex hull for %s prop ... ",
                cgmBaseFileName);
        System.out.flush();

        CollisionShape collisionShape
                = CollisionShapeFactory.createMergedHullShape(cgmRoot);

        System.out.printf("done!%n");
        dumper.dump(collisionShape, "    ");
        System.out.flush();

        // Write the convex hull in J3O format.
        String writeFilePath = String.format(
                "src/main/resources/Models/Props/%s/%s_hull.j3o",
                folderName, cgmBaseFileName);
        Heart.writeJ3O(writeFilePath, collisionShape);
    }

    /**
     * Create a CollisionShape for a World.
     *
     * @param folderName the name of the folder containing the C-G model
     * @param cgmBaseFileName the base filename of the C-G model
     */
    private static void createWorldShape(String folderName,
            String cgmBaseFileName) {
        assetManager.clearCache(); // to reclaim direct buffer memory

        String cgmAssetPath = String.format("/Models/%s/%s.j3o", folderName,
                cgmBaseFileName);
        Spatial cgmRoot = assetManager.loadModel(cgmAssetPath);

        System.out.printf("%nCreate shape for the %s world ... ",
                cgmBaseFileName);
        System.out.flush();

        CollisionShape collisionShape
                = CollisionShapeFactory.createMergedMeshShape(cgmRoot);

        System.out.printf("done!%n");
        dumper.dump(collisionShape, "    ");
        System.out.flush();

        // Save the shape in J3O format.
        String assetName;
        Platform platform = JmeSystem.getPlatform();
        if (platform == Platform.Windows64) {
            assetName = "env-shape-Windows64.j3o";
        } else {
            assetName = "env-shape.j3o";
        }

        String writeFilePath = "src/main/resources/Models/" + folderName
                + "/shapes/" + assetName;
        Heart.writeJ3O(writeFilePath, collisionShape);
    }
}
