package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.RotationOrder;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.joints.New6Dof;
import com.jme3.bullet.joints.motors.MotorParam;
import com.jme3.bullet.joints.motors.RotationMotor;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * A Vehicle that has a steering body distinct from the engine body.
 */
abstract public class Bike extends Vehicle {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(Bike.class.getName());
    // *************************************************************************
    // fields

    /**
     * physics body associated with steering
     */
    private VehicleControl steeringBody;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an unloaded Bike with the specified name.
     *
     * @param name the desired name (not null)
     */
    protected Bike(String name) {
        super(name);
    }
    // *************************************************************************
    // new protected methods

    /**
     * Add a single Wheel to the physics body associated with steering.
     *
     * @param wheelModel the desired WheelModel (not null)
     * @param connectionLocation the location where the suspension connects to
     * the chassis (in chassis coordinates, not null, unaffected)
     * @param mainBrakePeakForce (in Newtons, &ge;0)
     * @param parkingBrakePeakForce (in Newtons, &ge;0)
     * @param extraDamping (&ge;0, &lt;1)
     * @return the new Wheel
     */
    protected Wheel addSteeringWheel(
            WheelModel wheelModel, Vector3f connectionLocation,
            float mainBrakePeakForce, float parkingBrakePeakForce,
            float extraDamping) {
        Validate.nonNull(wheelModel, "wheel model");
        Validate.finite(connectionLocation, "connection location");
        Validate.nonNegative(mainBrakePeakForce, "main brake peak force");
        Validate.nonNegative(parkingBrakePeakForce, "parking brake peak force");
        Validate.fraction(extraDamping, "extra damping");

        Wheel result = addWheel(wheelModel, steeringBody, connectionLocation,
                false, false, mainBrakePeakForce, parkingBrakePeakForce,
                extraDamping);
        return result;
    }

    /**
     * Configure the "chassis" for an articulated vehicle where the engine and
     * the steering are in distinct physics bodies. (Bullet refers to everything
     * except the wheels as the "chassis".)
     *
     * @param cgmRoot the root of the C-G model to visualize the chassis (not
     * null, alias created)
     * @param engineSubtree the subtree of cgmRoot which visualizes the engine
     * body (not null, alias created)
     * @param steeringSubtree the subtree of cgmRoot which visualizes the
     * steering body (not null, alias created)
     * @param engineShape the shape for the engine body (not null, alias
     * created)
     * @param steeringShape the shape for the steering body (not null, alias
     * created)
     * @param engineMass the desired mass of the engine body (in kilos, &gt;0)
     * @param steeringMass the desired mass of the steering body (in kilos,
     * &gt;0)
     * @param damping the drag on the chassis due to air resistance (&ge;0,
     * &lt;1)
     * @return the constraint that joins the steering body and the engine body
     */
    protected New6Dof setBikeChassis(Node cgmRoot, Spatial engineSubtree,
            Spatial steeringSubtree, CollisionShape engineShape,
            CollisionShape steeringShape, float engineMass, float steeringMass,
            float damping) {
        Validate.nonNull(cgmRoot, "model root");
        Validate.nonNull(engineSubtree, "engine subtree");
        Validate.nonNull(steeringSubtree, "steering subtree");
        Validate.nonNull(engineShape, "engine shape");
        Validate.nonNull(steeringShape, "steering shape");
        Validate.positive(engineMass, "engine mass");
        Validate.positive(steeringMass, "steering mass");
        Validate.fraction(damping, "damping");

        // Configure the physics body associated with the Engine.
        setChassis(cgmRoot, engineShape, engineMass, damping, engineSubtree);

        // Create the physics body associated with steering.
        steeringShape.setScale(1f);
        steeringBody = new VehicleControl(steeringShape, steeringMass);
        steeringBody.setApplicationData(this);

        // Configure continuous collision detection (CCD) for the steering body.
        float radius = steeringShape.maxRadius();
        steeringBody.setCcdMotionThreshold(radius);
        steeringBody.setCcdSweptSphereRadius(radius);
        steeringSubtree.addControl(steeringBody);
        /*
         * Determine the default location and orientation of the steering body
         * and engine body.
         */
        VehicleControl engineBody = getVehicleControl();
        Vector3f location = engineSubtree.getWorldTranslation();
        Quaternion orientation = engineSubtree.getWorldRotation();
        engineBody.setPhysicsLocation(location);
        engineBody.setPhysicsRotation(orientation);
        location = steeringSubtree.getWorldTranslation();
        orientation = steeringSubtree.getWorldRotation();
        steeringBody.setPhysicsLocation(location);
        steeringBody.setPhysicsRotation(orientation);

        // Join the 2 bodies using a physics constraint.
        New6Dof result = New6Dof.newInstance(engineBody, steeringBody, location,
                orientation, RotationOrder.YXZ);
        result.setCollisionBetweenLinkedBodies(false);

        // Lock the rotational DOFs for the X and Z axes.
        int xRotationDof = 3 + PhysicsSpace.AXIS_X;
        result.set(MotorParam.LowerLimit, xRotationDof, 0f);
        result.set(MotorParam.UpperLimit, xRotationDof, 0f);
        int zRotationDof = 3 + PhysicsSpace.AXIS_Z;
        result.set(MotorParam.LowerLimit, zRotationDof, 0f);
        result.set(MotorParam.UpperLimit, zRotationDof, 0f);

        // Enable a servomotor to control the Y rotation.
        RotationMotor motor = result.getRotationMotor(PhysicsSpace.AXIS_Y);
        motor.setMotorEnabled(true);
        motor.setServoEnabled(true);
        motor.set(MotorParam.MaxMotorForce, 9e9f);
        motor.set(MotorParam.TargetVelocity, 99f);

        setSteeringRatio(1f);

        assert isArticulated();
        return result;
    }
    // *************************************************************************
    // Vehicle methods

    /**
     * Test whether this Vehicle contains the specified collision object.
     *
     * @param collisionObject the object to search for (not null)
     * @return true if found, otherwise false
     */
    @Override
    protected boolean containsPco(PhysicsCollisionObject collisionObject) {
        Validate.nonNull(collisionObject, "collision object");

        if (collisionObject == steeringBody) {
            return true;
        } else {
            boolean result = super.containsPco(collisionObject);
            return result;
        }
    }

    /**
     * Determine the mass of the Vehicle.
     *
     * @return the total mass (in kilograms, &gt;0)
     */
    @Override
    public float getMass() {
        float result = super.getMass();
        result += steeringBody.getMass();

        assert result > 0f : result;
        return result;
    }

    /**
     * Test whether this Vehicle contains multiple physics bodies.
     *
     * @return true if multiple bodies, otherwise false
     */
    @Override
    public boolean isArticulated() {
        return true;
    }

    /**
     * Enumerate all physics bodies in this Vehicle.
     *
     * @return a new array of pre-existing instances with no duplicates
     */
    @Override
    protected VehicleControl[] listBodies() {
        VehicleControl[] array = super.listBodies();
        int last = array.length;
        int numBodies = last + 1;
        VehicleControl[] result = new VehicleControl[numBodies];
        System.arraycopy(array, 0, result, 0, last);
        result[last] = steeringBody;

        return result;
    }
}
