package com.jayfella.jme.vehicle;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsSweepTestResult;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.ConvexShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.Constraint;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import jme3utilities.Loadable;
import jme3utilities.MySpatial;
import jme3utilities.NameGenerator;
import jme3utilities.Validate;
import jme3utilities.math.MyVector3f;

/**
 * A connected group of dynamic rigid bodies.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract public class Prop
        implements HasNode, Loadable {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Prop.class.getName());
    // *************************************************************************
    // fields

    /**
     * double-ended constraint to the Vehicle equipping this Prop, or
     * single-ended constraint, or null
     */
    private Constraint equipmentConstraint;
    /**
     * CollisionShape used in sweep tests
     */
    private ConvexShape sweepShape;
    /**
     * scale factor (world units per model unit, &gt;0)
     */
    final private float scaleFactor;
    /**
     * total mass (in kilograms, &gt;0)
     */
    final private float totalMass;
    /**
     * constraints internal to this Prop TODO use RagUtils.listInternalJoints()
     */
    final private List<Constraint> internalConstraints = new ArrayList<>(4);
    /**
     * map the name of a part (including both the main and peripherals) to its
     * physics control
     */
    final private Map<String, RigidBodyControl> partNameToBody
            = new HashMap<>(8);
    /**
     * generate unique names for prop instances
     */
    final private static NameGenerator nameGenerator = new NameGenerator();
    /**
     * scene-graph subtree that visualizes this Prop
     */
    final private Node node;
    /**
     * world that contains this Prop, or null if none
     */
    private PropWorld world;
    /**
     * main physics control
     */
    private RigidBodyControl mainRbc;
    /**
     * permanent, unique name for this Prop
     */
    final private String name;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Prop with the specified name prefix, scale, and mass.
     *
     * @param namePrefix the desired name prefix (not null)
     * @param scaleFactor the desired scale factor (world units per model unit,
     * &gt;0)
     * @param totalMass the desired total mass (in kilograms, &gt;0)
     */
    protected Prop(String namePrefix, float scaleFactor, float totalMass) {
        Validate.nonNull(namePrefix, "name prefix");
        Validate.positive(scaleFactor, "scale factor");
        Validate.positive(totalMass, "total mass");

        this.name = nameGenerator.unique(namePrefix);
        this.node = new Node("Prop: " + name);

        this.scaleFactor = scaleFactor;
        this.totalMass = totalMass;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Activate all rigid bodies in this Prop.
     */
    public void activateAll() {
        for (RigidBodyControl rbc : partNameToBody.values()) {
            rbc.activate();
        }
    }

    /**
     * Add this Prop to the specified world.
     *
     * @param world where to add (not null, alias created)
     * @param dropLocation (not null, unaffected)
     * @param dropOrientation (not null, unaffected)
     */
    public void addToWorld(PropWorld world, Vector3f dropLocation,
            Quaternion dropOrientation) {
        this.world = world;
        world.addProp(this);

        if (!isLoaded()) {
            AssetManager assetManager = world.getAssetManager();
            load(assetManager);
            assert isLoaded();
        }

        Vector3f startLocation
                = findStartLocation(world, dropLocation, dropOrientation);
        mainRbc.setPhysicsLocation(startLocation);
        mainRbc.setPhysicsRotation(dropOrientation);
        mainRbc.setAngularVelocity(Vector3f.ZERO);
        mainRbc.setLinearVelocity(Vector3f.ZERO);

        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        for (RigidBodyControl body : partNameToBody.values()) {
            physicsSpace.addCollisionObject(body);
        }
        for (Constraint constraint : internalConstraints) {
            physicsSpace.addJoint(constraint);
        }
        if (equipmentConstraint != null) {
            physicsSpace.addJoint(equipmentConstraint);
        }

        Node parentNode = world.getParentNode();
        parentNode.attachChild(node);
    }

    /**
     * If this Prop has an equipment constraint, destroy it and remove it from
     * the PhysicsSpace.
     */
    public void cancelEquipment() {
        if (equipmentConstraint != null) {
            PhysicsRigidBody bodyA = equipmentConstraint.getBodyA();

            PhysicsRigidBody foreignBody;
            if (bodyA.getApplicationData() == this) {
                foreignBody = equipmentConstraint.getBodyB();
            } else {
                foreignBody = bodyA;
            }
            Vehicle vehicle = (Vehicle) foreignBody.getApplicationData();
            vehicle.removeEquipmentConstraint(equipmentConstraint);

            equipmentConstraint.destroy();
            PhysicsSpace physicsSpace = world.getPhysicsSpace();
            physicsSpace.removeJoint(equipmentConstraint);

            this.equipmentConstraint = null;
        }
    }

    /**
     * Copy the local-to-world Transform of the named part.
     *
     * @param partName the name of the part
     * @param storeResult storage for the result (modified if not null)
     * @return the extrapolated Transform (either storeResult or a new
     * Transform)
     */
    public Transform copyPartTransform(String partName, Transform storeResult) {
        Transform result;
        if (storeResult == null) {
            result = new Transform();
        } else {
            result = storeResult;
        }

        RigidBodyControl body = partNameToBody.get(partName);
        body.getTransform(result);

        return result;
    }

    /**
     * Count how many constraints this Prop has, including internal ones.
     *
     * @return the count (&ge;0)
     */
    public int countConstraints() {
        int result = internalConstraints.size();
        if (equipmentConstraint != null) {
            ++result;
        }

        return result;
    }

    /**
     * Determine the default total mass for scale=1, for this type of Prop.
     *
     * @return the mass (in kilograms, &gt;0)
     */
    abstract public float defaultDescaledMass();

    /**
     * Find a good start location for this Prop in the specified world.
     *
     * @param world where to add (not null, unaffected)
     * @param dropLocation (not null, unaffected)
     * @param dropOrientation (not null, unaffected)
     * @return the suggested location (a new instance, in world coordinates)
     */
    public Vector3f findStartLocation(PropWorld world, Vector3f dropLocation,
            Quaternion dropOrientation) {
        Vector3f endLocation = dropLocation.add(0f, -999f, 0f);
        Transform startTransform = new Transform(dropLocation, dropOrientation);
        Transform endTransform = new Transform(endLocation, dropOrientation);
        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        List<PhysicsSweepTestResult> sweepTest = physicsSpace.sweepTest(
                sweepShape, startTransform, endTransform);
        /*
         * Find the closest contact with another collision object,
         * typically the pavement.
         */
        float closestFraction = 9f;
        for (PhysicsSweepTestResult hit : sweepTest) {
            if (hit.getCollisionObject() != mainRbc) {
                float hitFraction = hit.getHitFraction();
                if (hitFraction < closestFraction) {
                    closestFraction = hitFraction;
                }
            }
        }
        Vector3f startLocation = MyVector3f
                .lerp(closestFraction, dropLocation, endLocation, null);

        return startLocation;
    }

    /**
     * Access the main body.
     *
     * @return the pre-existing instance, or null if none
     */
    public RigidBodyControl getMainBody() {
        return mainRbc;
    }

    /**
     * Determine this prop's name.
     *
     * @return the descriptive name (not null)
     */
    public String getName() {
        return name;
    }

    /**
     * Access the world that contains this Prop.
     *
     * @return the pre-existing instance, or null if none
     */
    public PropWorld getWorld() {
        return world;
    }

    /**
     * Test whether this Prop has been loaded.
     *
     * @return true if loaded, otherwise false
     */
    public boolean isLoaded() {
        if (mainRbc == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Enumerate all constraints.
     *
     * @return a new array (not null)
     */
    public Constraint[] listConstraints() {
        int count = countConstraints();
        Constraint[] result = new Constraint[count];
        internalConstraints.toArray(result);

        if (equipmentConstraint != null) {
            result[count - 1] = equipmentConstraint;
        }

        return result;
    }

    /**
     * Enumerate all parts in this Prop.
     *
     * @return a map-backed set of names (not null)
     */
    public Set<String> listPartNames() {
        Set<String> result = partNameToBody.keySet();
        return result;
    }

    /**
     * Remove this Prop from the world to which it was added.
     */
    public void removeFromWorld() {
        node.removeFromParent();

        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        for (Constraint constraint : internalConstraints) {
            physicsSpace.removeJoint(constraint);
        }
        if (equipmentConstraint != null) {
            physicsSpace.removeJoint(equipmentConstraint);
        }
        for (RigidBodyControl body : partNameToBody.values()) {
            physicsSpace.removeCollisionObject(body);
        }
        world.removeProp(this);
        this.world = null;
        /*
         * This Prop might have been supporting unrelated bodies
         * that had been deactivated.
         */
        physicsSpace.activateAll(true);
    }

    /**
     * Determine the height of this loaded Prop in the specified orientation.
     *
     * @param orientation the desired orientation relative to the world axes
     * (not null, unaffected)
     * @return the height (in world units, &ge;0)
     */
    public float scaledHeight(Quaternion orientation) {
        assert isLoaded() : "Prop isn't loaded yet!";

        Quaternion savedRotation = node.getLocalRotation().clone();
        MySpatial.setWorldOrientation(node, orientation);
        Vector3f[] minMax = MySpatial.findMinMaxCoords(node);
        node.setLocalRotation(savedRotation);

        float result = minMax[1].y - minMax[0].y;
        return result;
    }

    /**
     * Determine the scale.
     *
     * @return the factor to convert model distances to world distances (&gt;0)
     */
    public float scaleFactor() {
        assert scaleFactor > 0f : scaleFactor;
        return scaleFactor;
    }

    /**
     * Equip this Prop using the specified constraint.
     *
     * @param constraint (not null, alias created)
     */
    public void setEquipmentConstraint(Constraint constraint) {
        assert constraint != null;
        assert equipmentConstraint == null;

        this.equipmentConstraint = constraint;
    }

    /**
     * Determine the total mass.
     *
     * @return the mass (in kilograms, &gt;0)
     */
    public float totalMass() {
        assert totalMass > 0f : totalMass;
        return totalMass;
    }
    // *************************************************************************
    // protected methods

    /**
     * Configure this Prop with a single RigidBodyControl.
     *
     * @param cgmRoot the root of the unscaled C-G model to visualize the Prop
     * (not null)
     * @param sweepShape unscaled collision shape for sweep tests (not null, may
     * be the same as unscaledBody)
     * @param bodyShape unscaled collision shape for the rigid body (not null,
     * may be same as unscaledSweep)
     */
    protected void configureSingle(
            Spatial cgmRoot, ConvexShape sweepShape, CollisionShape bodyShape) {
        Validate.nonNull(cgmRoot, "C-G model root");
        Validate.nonNull(sweepShape, "sweep shape");
        Validate.nonNull(bodyShape, "body shape");

        cgmRoot.setLocalScale(scaleFactor);
        bodyShape.setScale(scaleFactor);
        sweepShape.setScale(scaleFactor);
        this.sweepShape = sweepShape;

        float massKg = totalMass();
        mainRbc = new RigidBodyControl(bodyShape, massKg);
        mainRbc.setApplicationData(this);
        partNameToBody.put("main", mainRbc);

        // Configure continuous collision detection (CCD).
        float radius = bodyShape.maxRadius();
        mainRbc.setCcdMotionThreshold(radius);
        mainRbc.setCcdSweptSphereRadius(radius);

        node.addControl(mainRbc);
        node.attachChild(cgmRoot);
    }

    /**
     * Enumerate all internal constraints.
     *
     * @return a new array (not null)
     */
    protected Constraint[] listInternalConstraints() {
        int count = internalConstraints.size();
        Constraint[] result = new Constraint[count];
        internalConstraints.toArray(result);

        return result;
    }

    /**
     * Enumerate all physics controls.
     *
     * @return a new array
     */
    protected RigidBodyControl[] listBodies() {
        int count = partNameToBody.size();
        RigidBodyControl[] result = new RigidBodyControl[count];
        partNameToBody.values().toArray(result);

        return result;
    }
    // *************************************************************************
    // HasNode methods

    /**
     * Access the scene-graph subtree that visualizes this Prop.
     *
     * @return the pre-existing instance (not null)
     */
    @Override
    public Node getNode() {
        return node;
    }
    // *************************************************************************
    // Loadable methods

    /**
     * Load the assets of this Prop.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        // subclasses should override
    }
}
