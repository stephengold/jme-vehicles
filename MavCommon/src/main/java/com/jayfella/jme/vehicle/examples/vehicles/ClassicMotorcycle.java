package com.jayfella.jme.vehicle.examples.vehicles;

import com.jayfella.jme.vehicle.Bike;
import com.jayfella.jme.vehicle.Sound;
import com.jayfella.jme.vehicle.Steering;
import com.jayfella.jme.vehicle.WheelModel;
import com.jayfella.jme.vehicle.examples.engines.PeakyEngine;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound5;
import com.jayfella.jme.vehicle.examples.sounds.HornSound1;
import com.jayfella.jme.vehicle.examples.tires.Tire02;
import com.jayfella.jme.vehicle.examples.wheels.MotorcycleFrontWheel;
import com.jayfella.jme.vehicle.examples.wheels.MotorcycleRearWheel;
import com.jayfella.jme.vehicle.part.Engine;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.joints.New6Dof;
import com.jme3.bullet.joints.motors.MotorParam;
import com.jme3.bullet.joints.motors.RotationMotor;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyVector3f;

/**
 * An example Bike, built around Mora's "Classic Motorcycle" model.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class ClassicMotorcycle extends Bike {
    // *************************************************************************
    // constants and loggers

    /**
     * lag for estimating acceleration
     */
    final private static float accelerationLag = 25f;
    /**
     * delta error gain factor (for stabilization)
     */
    final private static float deltaGainFactor = 2f;
    /**
     * error gain (for stabilization)
     */
    final private static float errorGainFactor = 4f;
    /**
     * message logger for this class
     */
    final public static Logger logger3
            = Logger.getLogger(ClassicMotorcycle.class.getName());
    /**
     * estimated local offset of the point of support (to calculate
     * acceleration)
     */
    final private static Vector3f supportOffset = new Vector3f(0f, -0.55f, 0f);
    // *************************************************************************
    // fields

    /**
     * true if stabilized, otherwise false
     */
    private boolean isStabilized = true;
    /**
     * sine of the roll error from the previous timestep (for stabilization)
     */
    private float previousSre;
    /**
     * rotational inertia (for stabilization)
     */
    final private static Matrix3f tmpInertia = new Matrix3f();
    /**
     * physics joint connecting the steering body to the engine body
     */
    private New6Dof steeringJoint;
    /**
     * local-to-world transform of the engine body
     */
    final private static Transform tmpTransform = new Transform();
    /**
     * acceleration from the previous timestep (for stabilization)
     */
    final private Vector3f previousAcceleration = new Vector3f();
    /**
     * location of the engine body's support point from the previous timestep
     * (in world coordinates)
     */
    final private Vector3f previousLocation = new Vector3f();
    /**
     * linear velocity from the previous timestep (to calculate acceleration)
     */
    final private Vector3f previousVelocity = new Vector3f();
    /**
     * estimated linear acceleration (in world coordinates)
     */
    final private static Vector3f tmpAcceleration = new Vector3f();
    /**
     * actual "up" direction (in world coordinates)
     */
    final private static Vector3f tmpActualUp = new Vector3f();
    /**
     * desired "up" direction (for stabilization)
     */
    final private static Vector3f tmpDesiredUp = new Vector3f();
    /**
     * actual orientation error
     */
    final private static Vector3f tmpError = new Vector3f();
    /**
     * actual "forward" direction (in world coordinates)
     */
    final private static Vector3f tmpForward = new Vector3f();
    /**
     * torque impulse to apply (in world coordinates)
     */
    final private static Vector3f tmpImpulse = new Vector3f();
    /**
     * actual "left" direction (in world coordinates)
     */
    final private static Vector3f tmpLeft = new Vector3f();
    /**
     * estimated support location (in world coordinates)
     */
    final private static Vector3f tmpLocation = new Vector3f();
    /**
     * estimated linear velocity (in world coordinates)
     */
    final private static Vector3f tmpVelocity = new Vector3f();
    // *************************************************************************
    // constructors

    public ClassicMotorcycle() {
        super("Classic Motorcycle");
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Disable stabilization.
     */
    public void destabilize() {
        this.isStabilized = false;
    }
    // *************************************************************************
    // Bike methods

    /**
     * Load this Vehicle from assets.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        if (isLoaded()) {
            logger.log(Level.SEVERE, "The model is already loaded.");
            return;
        }

        // Load the C-G model with everything except passengers and wheels.
        String assetPath = "Models/classic_motorcycle/chassis.j3o";
        Node cgmRoot = (Node) assetManager.loadModel(assetPath);

        Spatial engineSubtree = cgmRoot.getChild("engine subtree");
        CollisionShape engineShape;
        try {
            assetPath = "Models/classic_motorcycle/shapes/engine.j3o";
            engineShape = (CollisionShape) assetManager.loadAsset(assetPath);
        } catch (AssetNotFoundException exception) {
            engineShape = CollisionShapeFactory.createDynamicMeshShape(
                    engineSubtree);
        }

        Spatial steeringSubtree = cgmRoot.getChild("steering subtree");
        CollisionShape steeringShape;
        try {
            assetPath = "Models/classic_motorcycle/shapes/steering.j3o";
            steeringShape = (CollisionShape) assetManager.loadAsset(assetPath);
        } catch (AssetNotFoundException exception) {
            steeringShape = CollisionShapeFactory.createDynamicMeshShape(
                    steeringSubtree);
        }

        float engineMass = 180f; // in kilograms
        float steeringMass = 40f; // in kilograms
        float linearDamping = 0.001f;
        this.steeringJoint = setBikeChassis(cgmRoot, engineSubtree,
                steeringSubtree, engineShape, steeringShape, engineMass,
                steeringMass, linearDamping);

        // Introduce some angular damping to mitigate the tendency to spin.
        VehicleControl engineBody = getVehicleControl();
        engineBody.setAngularDamping(0.4f);

        float wheelDiameter = 0.6f;
        WheelModel frontWheel = new MotorcycleFrontWheel(wheelDiameter);
        frontWheel.load(assetManager);
        wheelDiameter = 0.65f;
        WheelModel rearWheel = new MotorcycleRearWheel(wheelDiameter);
        rearWheel.load(assetManager);

        // Add 2 wheels to the Vehicle.
        Vector3f connectionLocation = new Vector3f(0f, -0.53f, 0.075f);
        float mainBrake = 1_000f; // both wheels
        float parkingBrake = 3_000f; // in front only
        float damping = 0.005f; // extra linear damping
        addSteeringWheel(frontWheel, connectionLocation, mainBrake,
                parkingBrake, damping);

        float rearAxleY = -0.1f; // height of rear axle relative to body's CoG
        float rearZ = -0.703f;
        addWheel(rearWheel, new Vector3f(0f, rearAxleY, rearZ), Steering.UNUSED,
                mainBrake, 0f, damping);
        /*
         * Configure the suspension.
         *
         * This vehicle applies the same settings to each wheel,
         * but that isn't required.
         */
        for (Wheel wheel : listWheels()) {
            Suspension suspension = wheel.getSuspension();

            // how fast the suspension will compress
            // 1 = slow, 0 = fast.
            suspension.setCompressDamping(0.6f);

            // how quickly the suspension will rebound back to height
            // 1 = slow, 0 = fast.
            suspension.setRelaxDamping(0.8f);

            // the stiffness of the suspension
            // Setting this too low can cause odd behavior.
            suspension.setStiffness(150f);
        }

        // Give each wheel a tire with plenty of friction.
        for (Wheel wheel : listWheels()) {
            wheel.setFriction(4f);
            wheel.setTireModel(new Tire02());
        }
        /*
         * Distribute drive power across the wheels:
         *  0 = no power, 1 = all the power
         */
        getWheel(0).setPowerFraction(0f); // front wheel
        getWheel(1).setPowerFraction(1f); // rear wheel
        /*
         * Specify the name and speed range for each gear.
         * The min-max speeds of successive gears should overlap.
         * The "min" speed of low gear should be zero.
         * The "max" speed of high gear determines the top speed.
         * The "red" speed of each gear is used to calculate its ratio.
         */
        GearBox gearBox = new GearBox(4, 1);
        gearBox.getGear(-1).setName("reverse").setMinMaxRedKph(0f, -40f, -40f);
        gearBox.getGear(1).setName("low").setMinMaxRedKph(0f, 30f, 40f);
        gearBox.getGear(2).setName("2nd").setMinMaxRedKph(20f, 60f, 70f);
        gearBox.getGear(3).setName("3rd").setMinMaxRedKph(50f, 100f, 120f);
        gearBox.getGear(4).setName("high").setMinMaxRedKph(80f, 140f, 140f);
        setGearBox(gearBox);

        float idleRpm = 1_000f;
        float redlineRpm = 7_500f;
        Engine engine = new PeakyEngine("60-hp gasoline 1000-7500 RPM",
                60f * Engine.HP_TO_W, idleRpm, redlineRpm);
        setEngine(engine);

        Sound engineSound = new EngineSound5();
        engineSound.load(assetManager);
        engine.setSound(engineSound);

        Sound hornSound = new HornSound1();
        hornSound.load(assetManager);
        setHornSound(hornSound);

        // Add a visible rider.
        assetPath = "Models/MakeHuman/driver.j3o";
        Vector3f offset = new Vector3f(0f, -0.5f, -0.3f);
        String clipName = "driving:classic_motorcycle";
        addPassenger(assetManager, assetPath, engineBody, offset, clipName);

        build(); // must be invoked last, to complete the Vehicle
    }

    /**
     * Determine the offset of the motorcycle's DashCamera in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    public void locateDashCam(Vector3f storeResult) {
        storeResult.set(0f, 0.97f, -0.1f);
    }

    /**
     * Determine the offset of the motorcycle's ChaseCamera target in scaled
     * shape coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    protected void locateTarget(Vector3f storeResult) {
        storeResult.set(0f, 0.135f, -0.96f);
    }

    /**
     * Callback from Bullet, invoked just before the physics is stepped.
     *
     * @param space the space that is about to be stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        super.prePhysicsTick(space, timeStep);

        RotationMotor motor
                = steeringJoint.getRotationMotor(PhysicsSpace.AXIS_Y);
        float angle = steeringWheelAngle();
        motor.set(MotorParam.ServoTarget, -angle);

        if (isStabilized) {
            stabilize(timeStep);
        }
    }

    /**
     * Warp this Vehicle to the specified position.
     *
     * @param engineLocation the desired location of the engine body (in world
     * coordinates, not null)
     * @param yRotation the desired Y rotation angle (in radians, measured
     * counter-clockwise from world +Z as seen from above)
     */
    @Override
    public void warpAllBodies(Vector3f engineLocation, float yRotation) {
        super.warpAllBodies(engineLocation, yRotation);

        this.isStabilized = true;
        this.previousSre = 0f;
        this.previousAcceleration.zero();
        this.previousLocation.set(engineLocation);
        this.previousVelocity.zero();
    }
    // *************************************************************************
    // private methods

    private void stabilize(float timeStep) {
        assert timeStep > 0f : timeStep;
        /*
         * Determine the engine body's current "left", "forward",
         * and "up" directions in world coordinates.
         */
        VehicleControl engineBody = getVehicleControl();
        engineBody.getTransform(tmpTransform);
        Quaternion localToWorld = tmpTransform.getRotation(); // alias
        localToWorld.getRotationColumn(0, tmpLeft);
        localToWorld.getRotationColumn(1, tmpActualUp);
        localToWorld.getRotationColumn(2, tmpForward);
        /*
         * Estimate the acceleration of the engine body's support point
         * in world coordinates.
         */
        MyMath.transform(tmpTransform, supportOffset, tmpLocation);
        tmpLocation.subtract(previousLocation, tmpVelocity);
        tmpVelocity.divideLocal(timeStep);
        previousLocation.set(tmpLocation);

        tmpVelocity.subtract(previousVelocity, tmpAcceleration);
        tmpAcceleration.divideLocal(timeStep);
        previousVelocity.set(tmpVelocity);

        // Apply smoothing to the acceleration estimate.
        MyVector3f.lerp(1f / accelerationLag, previousAcceleration,
                tmpAcceleration, tmpAcceleration);
        previousAcceleration.set(tmpAcceleration);
        /*
         * Calculate an "up" direction that would balance
         * lateral acceleration against gravity.
         */
        tmpLeft.y = 0f;
        if (MyVector3f.isZero(tmpLeft)) {
            logger3.warning("unable to determine lateral axis");
            return;
        }
        tmpAcceleration.projectLocal(tmpLeft);
        engineBody.getGravity(tmpDesiredUp);
        tmpDesiredUp.negateLocal();
        tmpDesiredUp.addLocal(tmpAcceleration);
        tmpDesiredUp.normalizeLocal();

        // error = actual X desired
        tmpActualUp.cross(tmpDesiredUp, tmpError);
        /*
         * Project the error onto the forward direction
         * in order to ignore wheelies and hill climbing.
         */
        float sinRollError = MyVector3f.scalarProjection(tmpError, tmpForward);

        // Calculate delta, the change in the sine of the roll error.
        float deltaSre = sinRollError - previousSre;
        this.previousSre = sinRollError;

        // Calculate a corrective torque impulse about the forward axis.
        float impulseMagnitude = deltaSre * deltaGainFactor
                + sinRollError * errorGainFactor;
        tmpForward.mult(impulseMagnitude, tmpImpulse);
        engineBody.getInverseInertiaWorld(tmpInertia);
        tmpInertia.invertLocal();
        tmpInertia.mult(tmpImpulse, tmpImpulse);

        // Apply the torque impulse to the engine body.
        if (Vector3f.isValidVector(tmpImpulse)) {
            engineBody.applyTorqueImpulse(tmpImpulse);
        } else {
            logger3.warning("overflow in stabilize()");
        }
    }
}
