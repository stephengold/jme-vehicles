package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.part.Brake;
import com.jayfella.jme.vehicle.part.Engine;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jayfella.jme.vehicle.skid.SkidMarksState;
import com.jme3.anim.AnimComposer;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.animation.RagUtils;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.joints.Constraint;
import com.jme3.bullet.joints.New6Dof;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.joints.motors.MotorParam;
import com.jme3.bullet.joints.motors.RotationMotor;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.objects.PhysicsVehicle;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.bullet.objects.infos.RigidBodyMotionState;
import com.jme3.bullet.objects.infos.VehicleController;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.Loadable;
import jme3utilities.MySpatial;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;
import jme3utilities.math.MyQuaternion;
import jme3utilities.math.MyVector3f;

/**
 * A vehicle based on Bullet's btRaycastVehicle, with a single Engine and a
 * single GearBox. In order to simulate articulated vehicles such as motorcycles
 * and semi-trailer trucks, a Vehicle may contain multiple physics bodies.
 *
 * Derived from the Car and Vehicle classes in the Advanced Vehicles project.
 */
abstract public class Vehicle
        implements HasNode, Loadable, PhysicsTickListener, VehicleSpeed,
        VehicleSteering {
    // *************************************************************************
    // constants and loggers

    /**
     * factor to convert km/hr to miles per hour
     */
    final public static float KPH_TO_MPH = 0.62137f;
    /**
     * factor to convert km/hr to wu/sec
     */
    final public static float KPH_TO_WUPS = 0.277778f;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(Vehicle.class.getName());
    // *************************************************************************
    // fields

    private AutomaticGearboxState gearboxState;
    /**
     * for testing TireSmokeEmitter
     */
    private boolean isBurningRubber = false;
    /**
     * true when driver is sounding the horn, otherwise false
     */
    private boolean isHornRequested = false;
    /**
     * source of motive power
     */
    private Engine engine;
    /**
     * control signal for acceleration, ranging from 0 (coasting) to 1 (full
     * throttle)
     */
    private float accelerateSignal;
    /**
     * linear damping due to air resistance on the chassis (&ge;0, &lt;1)
     */
    private float chassisDamping;
    /**
     * the fraction of the total mass in each body (each element &ge;0, &le;1)
     * or null if not determined yet
     */
    private float[] massFractions = null;
    /**
     * ratio of the steeringWheelAngle to the turn angle of any wheels used for
     * steering
     */
    private float steeringRatio = 2f;
    /**
     * rotation of the steering wheel, handlebars, or tiller (in radians,
     * negative&rarr;left, 0&rarr;neutral, positive&rarr;right)
     */
    private float steeringWheelAngle;
    /**
     * convey engine power to the wheels
     */
    private GearBox gearBox;
    /**
     * support the chassis and configure acceleration, steering, and braking
     */
    final private List<Wheel> wheels = new ArrayList<>(4);
    /**
     * temporary storage for the vehicle's orientation
     */
    final private static Matrix3f tmpOrientation = new Matrix3f();
    /**
     * scene-graph subtree that represents this Vehicle
     */
    final private Node node;
    /**
     * manage this vehicle's active skidmarks
     */
    private SkidMarksState skidmarks;
    /**
     * sound produced when the horn is sounding, or null for silence
     */
    private Sound hornSound;
    /**
     * computer-graphics (C-G) model to visualize the whole Vehicle except for
     * its wheels
     */
    private Spatial chassis;
    /**
     * units to use in the speedometer, or null for no speedometer
     */
    private SpeedUnit speedometerUnits = SpeedUnit.MPH;
    /**
     * descriptive name (not null)
     */
    final private String name;
    /**
     * visualize tire smoke
     */
    private TireSmokeEmitter smokeEmitter;
    /**
     * default transform of each body relative to the engine body, or null if
     * transforms have not yet been determined
     */
    private Transform[] relativeTransforms = null;
    /**
     * simulate vehicle sounds
     */
    private VehicleAudioState vehicleAudioState;
    /**
     * physics body associated with the Engine
     */
    private VehicleControl engineBody;
    /**
     * VehicleWorld that contains this Vehicle, or null if none
     */
    private VehicleWorld world;

    private WheelSpinState wheelSpinState;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an unloaded Vehicle with the specified name.
     *
     * @param name the desired name (not null)
     */
    protected Vehicle(String name) {
        Validate.nonNull(name, "name");

        this.name = name;
        this.node = new Node("Vehicle: " + name);
        assert !isLoaded();
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Evaluate the "accelerate" control signal.
     *
     * @return a signal strength, between 0 (coasting) and 1 (full throttle)
     */
    public float accelerateSignal() {
        return accelerateSignal;
    }

    /**
     * Add all bodies and internal joints to the world's PhysicsSpace.
     */
    public void addToPhysicsSpace() {
        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        VehicleControl[] bodies = listBodies();

        int numBodies = bodies.length;
        for (int bodyIndex = 0; bodyIndex < numBodies; ++bodyIndex) {
            VehicleControl body = bodies[bodyIndex];
            body.setPhysicsSpace(physicsSpace);
            /*
             * Align the body's vehicle coordinate system
             * with that of the engine body, which is Y-up and Z-forward.
             */
            VehicleController controller = body.getController();
            Quaternion b2e
                    = relativeTransforms[bodyIndex].getRotation(); // alias
            Quaternion e2b = b2e.inverse();
            Vector3f right = new Vector3f(1f, 0f, 0f);
            MyQuaternion.rotate(e2b, right, right);
            Vector3f up = new Vector3f(0f, 1f, 0f);
            MyQuaternion.rotate(e2b, up, up);
            Vector3f forward = new Vector3f(0f, 0f, 1f);
            MyQuaternion.rotate(e2b, forward, forward);
            controller.setCoordinateSystem(right, up, forward);
        }

        if (isArticulated()) {
            Iterable<PhysicsJoint> joints = RagUtils.listInternalJoints(bodies);
            for (PhysicsJoint joint : joints) {
                physicsSpace.addJoint(joint);
            }
        }
    }

    /**
     * Add this Vehicle to the specified world at the world's default location.
     *
     * @param world where to add (not null, alias created)
     * @param globalAudio the global audio controls (not null, alias created)
     */
    public void addToWorld(VehicleWorld world, GlobalAudio globalAudio) {
        Validate.nonNull(world, "world");
        Validate.nonNull(globalAudio, "global audio");

        Vector3f dropLocation = new Vector3f();
        world.locateDrop(dropLocation);

        float yRotation = world.dropYRotation();
        addToWorld(world, globalAudio, dropLocation, yRotation);
    }

    /**
     * Add this Vehicle to the specified world.
     *
     * @param world where to add (not null, alias created)
     * @param globalAudio the global audio controls (not null, alias created)
     * @param dropLocation the drop location (in physics-space coordinates, not
     * null)
     * @param yRotation the Y rotation angle (in radians, measured
     * counter-clockwise from world +Z as seen from above)
     */
    public void addToWorld(VehicleWorld world, GlobalAudio globalAudio,
            Vector3f dropLocation, float yRotation) {
        Validate.nonNull(world, "world");
        Validate.nonNull(globalAudio, "global audio");
        Validate.finite(dropLocation, "drop location");

        this.world = world;

        if (!isLoaded()) {
            AssetManager assetManager = world.getAssetManager();
            load(assetManager);
        }

        Node parentNode = world.getParentNode();
        parentNode.attachChild(node);

        warp(dropLocation, yRotation);
        getNode().setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        vehicleAudioState.setGlobalAudio(globalAudio);
        enable();

        // Add bodies and physics joints to the PhysicsSpace.
        addToPhysicsSpace();

        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        physicsSpace.addTickListener(this);
    }

    /**
     * Add a single Wheel to the body associated with the Engine. TODO protect
     * method
     *
     * @param wheelModel the desired WheelModel (not null)
     * @param connectionLocation the location where the suspension connects to
     * the chassis (in chassis coordinates, not null, unaffected)
     * @param steering relationship to the steering system (not null)
     * @param mainBrakePeakForce (in Newtons, &ge;0)
     * @param parkingBrakePeakForce (in Newtons, &ge;0)
     * @param extraDamping (&ge;0, &lt;1)
     * @return the new Wheel
     */
    public Wheel addWheel(WheelModel wheelModel, Vector3f connectionLocation,
            Steering steering, float mainBrakePeakForce,
            float parkingBrakePeakForce, float extraDamping) {
        Validate.nonNull(wheelModel, "wheel model");
        Validate.finite(connectionLocation, "connection location");
        Validate.nonNull(steering, "steering");
        Validate.nonNegative(mainBrakePeakForce, "main brake peak force");
        Validate.nonNegative(parkingBrakePeakForce, "parking brake peak force");
        Validate.fraction(extraDamping, "extra damping");

        Wheel result = addWheel(
                wheelModel, engineBody, connectionLocation, steering,
                mainBrakePeakForce, parkingBrakePeakForce, extraDamping);
        return result;
    }

    /**
     * Determine the linear damping due to air resistance.
     *
     * @return a fraction (&ge;0, &lt;1)
     */
    public float chassisDamping() {
        assert chassisDamping >= 0f && chassisDamping < 1f : chassisDamping;
        return chassisDamping;
    }

    /**
     * Perform a yes/no contact test against all physics bodies in this Vehicle.
     *
     * @return true if contact, false if no contact
     */
    public boolean contactTest() {
        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        VehicleControl[] bodies = listBodies();
        for (VehicleControl body : bodies) {
            if (physicsSpace.contactTest(body, null) > 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Count how many wheels this Vehicle has.
     *
     * @return the count (&ge;0)
     */
    public int countWheels() {
        return wheels.size();
    }

    /**
     * Determine the circumference of the first drive wheel. (It's assumed
     * they're all the same size.) Used to convert between axle angular speed
     * and tread speed.
     *
     * @return the circumference (in world units, &gt;0)
     */
    public float driveWheelCircumference() {
        for (Wheel wheel : wheels) {
            if (wheel.isPowered()) {
                float diameter = wheel.getDiameter();
                float circumference = FastMath.PI * diameter;

                assert circumference > 0f : circumference;
                return circumference;
            }
        }

        throw new IllegalStateException("No drive wheel found!");
    }

    /**
     * Determine the forward direction.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a unit vector in physics-space coordinates (either storeResult or
     * a new instance)
     */
    public Vector3f forwardDirection(Vector3f storeResult) {
        Vector3f result = engineBody.getForwardVector(storeResult);
        return result;
    }

    /**
     * Access the computer-graphics (C-G) model for visualization.
     *
     * @return the pre-existing instance
     */
    public Spatial getChassis() {
        return chassis;
    }

    /**
     * Access the Engine.
     *
     * @return the pre-existing instance
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * Access the GearBox.
     *
     * @return the pre-existing instance
     */
    public GearBox getGearBox() {
        return gearBox;
    }

    /**
     * Access the horn sound.
     *
     * @return the pre-existing instance, or null for silence
     */
    public Sound getHornSound() {
        return hornSound;
    }

    /**
     * Determine the mass of the Vehicle.
     *
     * @return the total mass (in kilograms, &gt;0)
     */
    public float getMass() {
        float result = engineBody.getMass();

        assert result > 0f : result;
        return result;
    }

    /**
     * Determine this vehicle's name.
     *
     * @return the descriptive name (not null)
     */
    public String getName() {
        return name;
    }

    /**
     * Determine which units to use in the speedometer.
     *
     * @return an enum value, or null for no speedometer
     */
    public SpeedUnit getSpeedometerUnits() {
        return speedometerUnits;
    }

    /**
     * Access the physics body associated with the Engine.
     *
     * @return the pre-existing instance
     */
    public VehicleControl getVehicleControl() {
        return engineBody;
    }

    /**
     * Access the indexed Wheel.
     *
     * @param index which Wheel to access (&ge;0)
     * @return the pre-existing instance
     */
    public Wheel getWheel(int index) {
        return wheels.get(index);
    }

    /**
     * Access the world that contains this Vehicle.
     *
     * @return the pre-existing instance, or null if none
     */
    public VehicleWorld getWorld() {
        return world;
    }

    /**
     * Test whether the tires are forced to emit smoke.
     *
     * @return true if forced, otherwise false
     */
    public boolean isBurningRubber() {
        return isBurningRubber;
    }

    /**
     * Test whether the driver is sounding the horn.
     *
     * @return true if sounding, otherwise false
     */
    public boolean isHornRequested() {
        return isHornRequested;
    }

    /**
     * Test whether the assets of this Vehicle have been loaded yet.
     *
     * @return true if loaded, otherwise false
     */
    public boolean isLoaded() {
        if (engineBody == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Enumerate all wheels.
     *
     * @return a new array (not null)
     */
    public Wheel[] listWheels() {
        int numWheels = countWheels();
        Wheel[] result = new Wheel[numWheels];
        wheels.toArray(result);

        return result;
    }

    /**
     * Determine the offset of the vehicle's DashCamera in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    abstract public void locateDashCam(Vector3f storeResult);

    /**
     * Determine the location of the ChaseCamera target.
     *
     * @param bias how much to displace the target toward the rear (0=center of
     * mass, 1=back bumper)
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector (in physics-space coordinates, either
     * storeResult or a new instance)
     */
    public Vector3f locateTarget(float bias, Vector3f storeResult) {
        Vector3f result = (storeResult == null) ? new Vector3f() : storeResult;
        RigidBodyMotionState motion = engineBody.getMotionState();

        motion.getOrientation(tmpOrientation);
        Vector3f offset = new Vector3f(); // TODO garbage
        locateTarget(offset);
        offset.z *= bias;
        tmpOrientation.mult(offset, offset);

        motion.getLocation(result);
        result.addLocal(offset);

        return result;
    }

    /**
     * Not yet implemented.
     *
     * @param constraint the Constraint to remove (not null)
     */
    public void removeEquipmentConstraint(Constraint constraint) {
        // TODO
    }

    /**
     * Remove all bodies and internal joints from the world's PhysicsSpace.
     */
    public void removeFromPhysicsSpace() {
        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        VehicleControl[] bodies = listBodies();
        if (isArticulated()) {
            Iterable<PhysicsJoint> joints = RagUtils.listInternalJoints(bodies);
            for (PhysicsJoint joint : joints) {
                physicsSpace.removeJoint(joint);
            }
        }

        for (VehicleControl body : bodies) {
            body.setPhysicsSpace(null);
        }
    }

    /**
     * Remove this Vehicle from the world to which it was added.
     */
    public void removeFromWorld() {
        disable();

        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        physicsSpace.removeTickListener(this);

        removeFromPhysicsSpace();
        node.removeFromParent();
        this.world = null;
    }

    /**
     * Alter the "accelerate" control signal.
     *
     * @param strength the desired strength of the "accelerate" control signal:
     * between 0 (coasting) and 1 (full throttle)
     */
    public void setAccelerateSignal(float strength) {
        Validate.fraction(strength, "strength");

        this.accelerateSignal = strength;

        // Determine unsigned speed in world units per second.
        float speed = forwardSpeed(SpeedUnit.WUPS);
        speed = FastMath.abs(speed);
        if (speed < 0.1f) {
            speed = 0.1f; // avoid division by zero below
        }
        /*
         * Distribute the total engine power across the wheels in accordance
         * with their configured power fractions.
         */
        float signedSignal = gearBox.isInReverse() ? -strength : +strength;
        float maxWatts = getEngine().outputWatts();
        float totalWatts
                = signedSignal * maxWatts; // signed so that <0 means reverse
        for (Wheel wheel : wheels) {
            float powerFraction = wheel.getPowerFraction();
            float wheelPower = powerFraction * totalWatts;
            float wheelForce = wheelPower / speed;
            wheel.updateAccelerate(wheelForce);
        }
    }

    /**
     * Alter the values of the brake control signals.
     *
     * @param mainStrength the desired strength of the main-brake control
     * signal, between 0 (not applied) and 1 (applied as strongly as possible)
     * @param parkingStrength the desired strength of the parking-brake control
     * signal, between 0 (not applied) and 1 (applied as strongly as possible)
     */
    public void setBrakeSignals(float mainStrength, float parkingStrength) {
        for (Wheel wheel : wheels) {
            wheel.updateBrakes(mainStrength, parkingStrength);
        }
    }

    /**
     * Alter whether the tires are forced to emit smoke.
     *
     * @param setting true&rarr;forced, false&rarr;not forced
     */
    public void setBurningRubber(boolean setting) {
        this.isBurningRubber = setting;
    }

    /**
     * Update the status of the horn.
     *
     * @param isRequested true &rarr; requested, false &rarr; not requested
     */
    public void setHornStatus(boolean isRequested) {
        this.isHornRequested = isRequested;
    }

    /**
     * Alter the mass of the Vehicle proportionally.
     *
     * @param mass the desired total mass (in kilograms, &gt;0)
     */
    public void setMass(float mass) {
        Validate.positive(mass, "mass");

        if (isArticulated()) {
            VehicleControl[] bodies = listBodies();
            if (massFractions == null) {
                updateMassFractions(bodies);
            }
            int numBodies = bodies.length;
            assert massFractions.length == numBodies;

            for (int bodyIndex = 0; bodyIndex < numBodies; ++bodyIndex) {
                float massFraction = massFractions[bodyIndex];
                float bodyMass = massFraction * mass;
                engineBody.setMass(bodyMass);
            }
        } else {
            engineBody.setMass(mass);
        }
    }

    /**
     * Alter which units to use in the speedometer.
     *
     * @param units an enum value, or null for no speedometer
     */
    public void setSpeedometerUnits(SpeedUnit units) {
        this.speedometerUnits = units;
    }

    /**
     * Enable or disable skid-mark generation.
     *
     * @param enabled true to enable, false to disable
     */
    public void setTireSkidMarksEnabled(boolean enabled) {
        skidmarks.setSkidmarkEnabled(enabled);
    }

    /**
     * Show or hide any skid marks created by this Vehicle.
     *
     * @param enabled true to show, false to hide
     */
    public void setTireSkidMarksVisible(boolean enabled) {
        skidmarks.setEnabled(enabled);
    }

    /**
     * Enable or disable tire-smoke emission.
     *
     * @param enabled true to enable, false to disable
     */
    public void setTireSmokeEnabled(boolean enabled) {
        smokeEmitter.setEnabled(enabled);
    }

    /**
     * Replace the WheelModel of the indexed wheel with a new instance of the
     * specified class.
     *
     * @param wheelIndex which wheel to replace (&ge;0, &lt;numWheels-1)
     * @param modelClass the desired type of wheel (not null)
     */
    @SuppressWarnings("unchecked")
    public void setWheelModel(
            int wheelIndex, Class<? extends WheelModel> modelClass) {
        Wheel wheel = wheels.get(wheelIndex);
        VehicleWheel vehicleWheel = wheel.getVehicleWheel();

        Node oldNode = (Node) vehicleWheel.getWheelSpatial();
        oldNode.removeFromParent();

        Constructor<? extends WheelModel>[] constructors
                = (Constructor<? extends WheelModel>[]) modelClass
                        .getConstructors();
        assert constructors.length == 1 : constructors.length;
        Constructor<? extends WheelModel> constructor = constructors[0];
        // assuming a single constructor that takes a single float argument

        float radius = vehicleWheel.getRadius();
        float diameter = 2 * radius;
        WheelModel wheelModel;
        try {
            wheelModel = constructor.newInstance(diameter);
        } catch (IllegalAccessException
                | InstantiationException
                | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
        AssetManager assetManager = world.getAssetManager();
        wheelModel.load(assetManager);

        // Copy the local rotation of the old Spatial.
        int numChildren = oldNode.getChildren().size();
        assert numChildren == 1 : numChildren;
        Spatial oldSpatial = oldNode.getChild(0);
        Spatial newSpatial = wheelModel.getSpatial();
        Quaternion localRotation = oldSpatial.getLocalRotation();
        newSpatial.setLocalRotation(localRotation);

        Node newNode = wheelModel.getWheelNode();
        vehicleWheel.setWheelSpatial(newNode);
        getNode().attachChild(newNode);

        AppStateManager stateManager = world.getStateManager();
        stateManager.detach(skidmarks);
        this.skidmarks = new SkidMarksState(this);
        stateManager.attach(skidmarks);
    }

    /**
     * If the Engine is not running, start it.
     */
    public void startEngine() {
        if (!engine.isRunning()) {
            engine.setRunning(true);
        }
    }

    /**
     * Update the "steer" control signal, which controls both the orientation of
     * the steering wheel (or handlebars) and any wheels used for steering.
     *
     * @param wheelAngle the desired angle (in radians)
     */
    public void steer(float wheelAngle) {
        this.steeringWheelAngle = steeringRatio * wheelAngle;

        for (Wheel wheel : wheels) {
            wheel.steer(wheelAngle);
        }
    }

    /**
     * If the Engine is running, stop it.
     */
    public void stopEngine() {
        if (engine.isRunning()) {
            engine.setRunning(false);
        }
    }

    /**
     * Warp this vehicle to the specified drop location.
     *
     * @param dropLocation the drop location (in physics-space coordinates, not
     * null)
     * @param yRotation the desired Y rotation angle (in radians, measured
     * counter-clockwise from world +Z as seen from above)
     */
    public void warp(Vector3f dropLocation, float yRotation) {
        Validate.nonNull(dropLocation, "drop location");

        // Cast a physics ray downward from the drop location.
        Vector3f endLocation = dropLocation.add(0f, -999f, 0f);
        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        List<PhysicsRayTestResult> rayTest
                = physicsSpace.rayTestRaw(dropLocation, endLocation);
        /*
         * Find the closest contact with an external collision object,
         * typically the ground or pavement.
         */
        float closestFraction = 9f;
        for (PhysicsRayTestResult hit : rayTest) {
            PhysicsCollisionObject hitPco = hit.getCollisionObject();
            if (!containsPco(hitPco)) {
                float hitFraction = hit.getHitFraction();
                if (hitFraction < closestFraction) {
                    closestFraction = hitFraction;
                }
            }
        }
        Vector3f contactLocation = MyVector3f.lerp(
                closestFraction, dropLocation, endLocation, null);
        /*
         * Estimate the minimum chassis Y offset to keep
         * the undercarriage off the pavement.
         */
        CollisionShape shape = engineBody.getCollisionShape();
        BoundingBox aabb
                = shape.boundingBox(Vector3f.ZERO, Matrix3f.IDENTITY, null);
        float minYOffset = aabb.getYExtent() - aabb.getCenter().y;
        /*
         * Estimate chassis Y offset based on an unloaded axle.
         * TODO adjust for the vehicle's weight
         */
        VehicleWheel w0 = engineBody.getWheel(0);
        float suspensionLength = w0.getRestLength();
        float yOffset
                = w0.getRadius() + suspensionLength - w0.getLocation(null).y;

        // Calculate and apply an appropriate engine location.
        if (yOffset < minYOffset) {
            yOffset = minYOffset;
        }
        Vector3f engineLocation = contactLocation.add(0f, yOffset, 0f);
        warpAllBodies(engineLocation, yRotation);
    }

    /**
     * Warp this vehicle to the specified position.
     *
     * @param engineLocation the desired location of the Engine (in world
     * coordinates, not null)
     * @param yRotation the desired Y rotation angle (in radians, measured
     * counter-clockwise from world +Z as seen from above)
     */
    public void warpAllBodies(Vector3f engineLocation, float yRotation) {
        Transform engineToWorld = new Transform(engineLocation);
        engineToWorld.getRotation().fromAngles(0f, yRotation, 0f);

        PhysicsRigidBody[] bodies = listBodies();
        int numBodies = bodies.length;
        for (int bodyIndex = 0; bodyIndex < numBodies; ++bodyIndex) {
            Transform bodyToEngine = relativeTransforms[bodyIndex];
            Transform bodyToWorld
                    = MyMath.combine(bodyToEngine, engineToWorld, null);

            PhysicsRigidBody body = bodies[bodyIndex];
            body.setPhysicsTransform(bodyToWorld);
            body.setAngularVelocity(Vector3f.ZERO);
            body.setLinearVelocity(Vector3f.ZERO);
        }

        if (isArticulated()) {
            Iterable<PhysicsJoint> joints = RagUtils.listInternalJoints(bodies);
            for (PhysicsJoint joint : joints) {
                New6Dof sixDof = (New6Dof) joint;
                RotationMotor motor
                        = sixDof.getRotationMotor(PhysicsSpace.AXIS_Y);
                motor.set(MotorParam.ServoTarget, 0f);
            }
        }
    }

    /**
     * Warp this vehicle to the world's preferred start position.
     */
    public void warpToStart() {
        Vector3f dropLocation = new Vector3f();
        world.locateDrop(dropLocation);

        float yRotation = world.dropYRotation();
        warp(dropLocation, yRotation);
    }
    // *************************************************************************
    // new protected methods

    /**
     * Add a passenger with bone animation.
     *
     * @param assetManager to load assets (not null)
     * @param assetPath the path to the C-G model asset (not null, not empty)
     * @param body the physics body which moves the passenger (not null)
     * @param offset the location of the passenger relative to the body (not
     * null, unaffected)
     * @param clipName the name of the animation clip to play (not null, not
     * empty)
     */
    protected static void addPassenger(
            AssetManager assetManager, String assetPath,
            VehicleControl body, Vector3f offset, String clipName) {
        Validate.nonEmpty(assetPath, "asset path");
        Validate.nonNull(offset, "offset");
        Validate.nonEmpty(clipName, "clip name");

        Spatial passengerCgmRoot = assetManager.loadModel(assetPath);

        Node engineSubtree = (Node) body.getSpatial();
        engineSubtree.attachChild(passengerCgmRoot);
        passengerCgmRoot.setLocalTranslation(offset);

        List<AnimComposer> composers = MySpatial
                .listControls(passengerCgmRoot, AnimComposer.class, null);
        assert composers.size() == 1;
        AnimComposer composer = composers.get(0);
        composer.setCurrentAction(clipName);
    }

    /**
     * Add a single Wheel to the specified body.
     *
     * @param wheelModel the desired WheelModel (not null)
     * @param body the physics body to which the Wheel will be added (not null,
     * alias created)
     * @param connectionLocation the location where the suspension connects to
     * the chassis (in chassis coordinates, not null, unaffected)
     * @param steering wheel's relationship to the steering system (not null)
     * @param mainBrakePeakForce (in Newtons, &ge;0)
     * @param parkingBrakePeakForce (in Newtons, &ge;0)
     * @param extraDamping (&ge;0, &lt;1)
     * @return the new Wheel
     */
    protected Wheel addWheel(WheelModel wheelModel, VehicleControl body,
            Vector3f connectionLocation, Steering steering,
            float mainBrakePeakForce, float parkingBrakePeakForce,
            float extraDamping) {
        Node wheelNode = wheelModel.getWheelNode();
        Vector3f suspensionDirection = new Vector3f(0f, -1f, 0f);
        Vector3f axleDirection = new Vector3f(-1f, 0f, 0f);
        float restLength = 0.2f;
        float radius = wheelModel.radius();
        int wheelIndex = body.getNumWheels();
        VehicleWheel vehicleWheel = body.addWheel(
                wheelNode, connectionLocation, suspensionDirection,
                axleDirection, restLength, radius, steering != Steering.UNUSED);

        Suspension suspension = new Suspension(vehicleWheel);
        Brake mainBrake = new Brake(mainBrakePeakForce);
        Brake parkingBrake = new Brake(parkingBrakePeakForce);
        Wheel result = new Wheel(this, body, wheelIndex, steering, suspension,
                mainBrake, parkingBrake, extraDamping);
        wheels.add(result);

        getNode().attachChild(wheelNode);

        return result;
    }

    /**
     * Should be invoked last, after all parts have been configured and added.
     */
    protected void build() {
        updateRelativeTransforms();

        this.gearboxState = new AutomaticGearboxState(this);
        this.skidmarks = new SkidMarksState(this);
        this.smokeEmitter = new TireSmokeEmitter(this);
        this.vehicleAudioState = new VehicleAudioState(this);
        this.wheelSpinState = new WheelSpinState(this);
    }

    /**
     * Test whether this Vehicle contains the specified collision object.
     *
     * @param collisionObject the object to search for (not null)
     * @return true if found, otherwise false
     */
    protected boolean containsPco(PhysicsCollisionObject collisionObject) {
        Validate.nonNull(collisionObject, "collision object");

        if (collisionObject == engineBody) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Test whether this Vehicle contains multiple physics bodies.
     *
     * @return true if multiple bodies, otherwise false
     */
    protected boolean isArticulated() {
        return false;
    }

    /**
     * Enumerate all physics bodies in this Vehicle.
     *
     * @return a new array of pre-existing instances with no duplicates
     */
    protected VehicleControl[] listBodies() {
        VehicleControl[] result = new VehicleControl[1];
        result[0] = engineBody;

        return result;
    }

    /**
     * Determine the offset of the vehicle's ChaseCamera target in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    abstract protected void locateTarget(Vector3f storeResult);

    /**
     * Configure a single-body "chassis" that's loaded from J3O assets in the
     * customary folders. (Bullet refers to everything except the wheels as the
     * "chassis".)
     *
     * @param folderName the name of the folder containing the C-G model asset
     * (not null, not empty)
     * @param cgmBaseFileName the base filename of the C-G model asset (not
     * null, not empty)
     * @param assetManager to load assets (not null)
     * @param mass the mass of the chassis (in kilos, &gt;0)
     * @param damping the drag on the chassis due to air resistance (&ge;0,
     * &lt;1)
     */
    protected void setChassis(String folderName, String cgmBaseFileName,
            AssetManager assetManager, float mass, float damping) {
        Validate.nonEmpty(folderName, "folder name");
        Validate.nonEmpty(cgmBaseFileName, "base filename");
        Validate.nonNull(assetManager, "asset manager");
        Validate.positive(mass, "mass");
        Validate.fraction(damping, "damping");

        String assetPath
                = "/Models/" + folderName + "/" + cgmBaseFileName + ".j3o";
        Spatial cgmRoot = assetManager.loadModel(assetPath);

        assetPath = "/Models/" + folderName + "/shapes/chassis-shape.j3o";
        CollisionShape shape;
        try {
            shape = (CollisionShape) assetManager.loadAsset(assetPath);
            Vector3f scale = cgmRoot.getWorldScale();
            shape.setScale(scale);
        } catch (AssetNotFoundException exception) {
            shape = CollisionShapeFactory.createDynamicMeshShape(cgmRoot);
        }
        setChassis(cgmRoot, shape, mass, damping);
    }

    /**
     * Configure a single-body "chassis". (Bullet refers to everything except
     * the wheels as the "chassis".)
     *
     * @param cgmRoot the root of the C-G model to visualize the chassis (not
     * null, alias created)
     * @param shape the shape for the chassis (not null, alias created)
     * @param mass the mass of the chassis (in kilos, &gt;0)
     * @param damping the drag on the chassis due to air resistance (&ge;0,
     * &lt;1)
     */
    protected void setChassis(Spatial cgmRoot, CollisionShape shape,
            float mass, float damping) {
        Validate.nonNull(cgmRoot, "C-G model root");
        Validate.nonNull(shape, "shape");
        Validate.positive(mass, "mass");
        Validate.fraction(damping, "damping");

        setChassis(cgmRoot, shape, mass, damping, node);
    }

    /**
     * Configure a single-body "chassis". (Bullet refers to everything except
     * the wheels as the "chassis".)
     *
     * @param cgmRoot the root of the C-G model to visualize the chassis (not
     * null, alias created)
     * @param shape the shape for the chassis (not null, alias created)
     * @param mass the mass of the chassis (in kilos, &gt;0)
     * @param damping the drag on the chassis due to air resistance (&ge;0,
     * &lt;1)
     * @param controlledSpatial the Spatial to which the physics control should
     * be added (not null)
     */
    protected void setChassis(Spatial cgmRoot, CollisionShape shape,
            float mass, float damping, Spatial controlledSpatial) {
        Validate.nonNull(cgmRoot, "C-G model root");
        Validate.nonNull(shape, "shape");
        Validate.positive(mass, "mass");
        Validate.fraction(damping, "damping");
        Validate.nonNull(controlledSpatial, "controlled spatial");

        this.chassisDamping = damping;
        this.chassis = cgmRoot;
        node.attachChild(cgmRoot);
        this.massFractions = null;

        // Create the physics body associated with the Engine.
        this.engineBody = new VehicleControl(shape, mass);
        engineBody.setApplicationData(this);
        /*
         * Configure damping for the engine body,
         * to simulate drag due to air resistance.
         */
        engineBody.setLinearDamping(damping);

        // Configure continuous collision detection (CCD) for the chassis.
        float radius = shape.maxRadius();
        engineBody.setCcdMotionThreshold(radius);
        engineBody.setCcdSweptSphereRadius(radius);

        controlledSpatial.addControl(engineBody);
    }

    /**
     * Alter the vehicle's Engine.
     *
     * @param desiredEngine the desired Engine (not null)
     */
    protected void setEngine(Engine desiredEngine) {
        Validate.nonNull(desiredEngine, "desired engine");

        if (engine != null) {
            engine.setVehicle(null);
        }
        this.engine = desiredEngine;
        engine.setVehicle(this);
    }

    /**
     * Alter the vehicle's GearBox.
     *
     * @param gearBox the desired GearBox
     */
    protected void setGearBox(GearBox gearBox) {
        this.gearBox = gearBox;
    }

    /**
     * Alter the horn sound.
     *
     * @param sound the desired Sound (loaded), or null for silence
     */
    protected void setHornSound(Sound sound) {
        if (hornSound != null) {
            hornSound.mute();
            hornSound.detach();
        }
        this.hornSound = sound;
        if (sound != null) {
            sound.attachTo(node);
        }
    }

    /**
     * Alter the ratio of the steeringWheelAngle to the turn angle of any wheels
     * used for steering.
     *
     * @param ratio the desired ratio (default=2)
     */
    protected void setSteeringRatio(float ratio) {
        this.steeringRatio = ratio;
    }

    /**
     * Update the mass fractions.
     *
     * @param bodies the array of bodies (not null, unaffected)
     * @return the total mass (&gt;0)
     */
    protected float updateMassFractions(VehicleControl[] bodies) {
        float totalMass = 0f;
        for (VehicleControl body : bodies) {
            float mass = body.getMass();
            totalMass += mass;
        }
        assert totalMass > 0f : totalMass;

        int numBodies = bodies.length;
        this.massFractions = new float[numBodies];
        for (int bodyIndex = 0; bodyIndex < numBodies; ++bodyIndex) {
            VehicleControl body = bodies[bodyIndex];
            float mass = body.getMass();
            float fraction = mass / totalMass;
            assert fraction >= 0f && fraction <= 1f : fraction;
            this.massFractions[bodyIndex] = fraction;
        }

        return totalMass;
    }
    // *************************************************************************
    // HasNode methods

    /**
     * Access the scene-graph subtree that visualizes this Vehicle.
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
     * Load the assets of this Vehicle.
     *
     * @param assetManager for loading assets (not null)
     */
    @Override
    public void load(AssetManager assetManager) {
        // subclasses should override
    }
    // *************************************************************************
    // PhysicsTickListener methods

    /**
     * Callback from Bullet, invoked just after the physics has been stepped.
     *
     * @param space the space that was just stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void physicsTick(PhysicsSpace space, float timeStep) {
        // do nothing
    }

    /**
     * Callback from Bullet, invoked just before the physics is stepped.
     *
     * @param space the space that is about to be stepped (not null)
     * @param timeStep the time per physics step (in seconds, &ge;0)
     */
    @Override
    public void prePhysicsTick(PhysicsSpace space, float timeStep) {
        // Update the linear damping of the chassis.
        float linearDamping = chassisDamping();
        for (Wheel wheel : wheels) {
            linearDamping += wheel.linearDamping();
        }
        //System.out.println("linearDamping = " + linearDamping);
        PhysicsVehicle physicsVehicle = getVehicleControl();
        physicsVehicle.setLinearDamping(linearDamping);
    }
    // *************************************************************************
    // VehicleSpeed methods

    /**
     * Determine the forward component of the vehicle's inertial velocity. The
     * vehicle must be added to some world.
     *
     * @param speedUnit the unit of measurement to use (not null)
     * @return the speed (in the specified units, may be negative)
     */
    @Override
    public float forwardSpeed(SpeedUnit speedUnit) {
        if (world == null) {
            throw new IllegalStateException("Vehicle not added to any world.");
        }

        float kph = engineBody.getCurrentVehicleSpeedKmHour();

        float result;
        switch (speedUnit) {
            case KPH:
                result = kph;
                break;
            case MPH:
                result = kph * KPH_TO_MPH;
                break;
            case WUPS:
                result = kph * KPH_TO_WUPS;
                break;
            default:
                throw new IllegalArgumentException("speedUnit = " + speedUnit);
        }

        return result;
    }

    /**
     * Estimate the vehicle's maximum forward speed.
     *
     * @param speedUnit the unit of measurement to use (not null)
     * @return the speed (in the specified units, &ge;0)
     */
    @Override
    public float maxForwardSpeed(SpeedUnit speedUnit) {
        float result = gearBox.maxForwardSpeed(speedUnit);
        assert result >= 0f : result;
        return result;
    }

    /**
     * Estimate the vehicle's maximum reverse speed.
     *
     * @param speedUnit the unit of measurement to use (not null)
     * @return the speed (in the specified units, &le;0)
     */
    @Override
    public float maxReverseSpeed(SpeedUnit speedUnit) {
        float result = gearBox.maxForwardSpeed(speedUnit);
        assert result <= 0f : result;
        return result;
    }
    // *************************************************************************
    // VehicleSteering methods

    /**
     * Determine the rotation angle of the steering wheel, handlebars, or
     * tiller.
     *
     * @return the angle (in radians, negative = left, 0 = neutral, positive =
     * right)
     */
    @Override
    public float steeringWheelAngle() {
        return steeringWheelAngle;
    }
    // *************************************************************************
    // Object methods

    /**
     * Represent this instance as a String.
     *
     * @return a descriptive string of text (not null, not empty)
     */
    @Override
    public String toString() {
        String className = getClass().getSimpleName();
        int hashCode = hashCode();
        String result = className + "@" + Integer.toHexString(hashCode);

        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Detach the AppStates associated with this Vehicle.
     */
    private void disable() {
        AppStateManager stateManager = world.getStateManager();
        stateManager.detach(gearboxState);
        stateManager.detach(skidmarks);
        stateManager.detach(smokeEmitter);
        stateManager.detach(vehicleAudioState);
        stateManager.detach(wheelSpinState);
    }

    /**
     * Attach the AppStates associated with this Vehicle.
     */
    private void enable() {
        AppStateManager stateManager = world.getStateManager();
        stateManager.attach(gearboxState);
        stateManager.attach(skidmarks);
        stateManager.attach(smokeEmitter);
        stateManager.attach(vehicleAudioState);
        stateManager.attach(wheelSpinState);
    }

    private void updateRelativeTransforms() {
        VehicleControl[] bodies = listBodies();
        int numBodies = bodies.length;
        this.relativeTransforms = new Transform[numBodies];

        Transform e2r = engineBody.getSpatial().getWorldTransform(); // alias
        Transform rootToEngine = e2r.invert();
        for (int bodyIndex = 0; bodyIndex < numBodies; ++bodyIndex) {
            VehicleControl body = bodies[bodyIndex];
            Transform b2r = body.getSpatial().getWorldTransform(); // alias
            Transform bodyToEngine = MyMath.combine(b2r, rootToEngine, null);
            this.relativeTransforms[bodyIndex] = bodyToEngine;
        }
    }
}
