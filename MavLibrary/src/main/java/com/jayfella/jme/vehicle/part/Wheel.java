package com.jayfella.jme.vehicle.part;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.VehicleWorld;
import com.jayfella.jme.vehicle.tire.PacejkaTireModel;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * A single wheel of a Vehicle, including its suspension and brakes.
 *
 * Derived from the Wheel class in the Advanced Vehicles project.
 */
public class Wheel {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(Wheel.class.getName());
    // *************************************************************************
    // fields

    /**
     * true if used for steering, otherwise false
     */
    private boolean isSteering;
    /**
     * steer with a rear wheel by flipping the direction
     */
    private boolean isSteeringFlipped;
    /**
     * main brake, which is typically hydraulic
     */
    final private Brake mainBrake;
    /**
     * parking (aka hand or emergency) brake
     */
    final private Brake parkingBrake;
    /**
     * color of tire smoke
     */
    final private ColorRGBA tireSmokeColor
            = new ColorRGBA(0.6f, 0.6f, 0.6f, 0.3f);
    /**
     * additional linear damping applied to the chassis when this wheel has
     * traction
     */
    final private float extraDamping;
    /**
     * grip degradation: 1 = full grip the tire allows, 0 = worn-out tire
     */
    private float grip = 1f;
    /**
     * how far the wheel can turn when steered (in radians)
     */
    private float maxSteerAngle = FastMath.QUARTER_PI;
    /**
     * fraction of the engine's output power transmitted to this wheel (&ge;0,
     * &le;1)
     */
    private float powerFraction = 0f;

    private float rotationDelta;
    /**
     * horizontal rotation (in radians) TODO direction of measurement?
     */
    private float steeringAngle = 0f;
    /**
     * index among the vehicle's wheels (&ge;0)
     */
    final private int wheelIndex;

    private PacejkaTireModel tireModel;
    /**
     * parameters of the associated suspension spring
     */
    final private Suspension suspension;
    /**
     * Vehicle that contains this Wheel
     */
    final private Vehicle vehicle;
    /**
     * vehicle's physics control
     */
    final private VehicleControl vehicleControl;
    /**
     * wheel's physics object
     */
    final private VehicleWheel vehicleWheel;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a wheel with the specified parameters.
     *
     * @param vehicle the vehicle that will contain this Wheel (not null, alias
     * created)
     * @param wheelIndex the index among the vehicle's wheels (&ge;0)
     * @param isSteering true if used for steering, otherwise false
     * @param steeringFlipped true for rear-wheel steering, otherwise false
     * @param suspension the suspension spring (not null, alias created)
     * @param mainBrake the main brake (not null, alias created)
     * @param parkingBrake the parking brake (not null, alias created)
     * @param extraDamping the additional linear damping (&ge;0, &lt;1)
     */
    public Wheel(Vehicle vehicle, int wheelIndex, boolean isSteering,
            boolean steeringFlipped, Suspension suspension,
            Brake mainBrake, Brake parkingBrake, float extraDamping) {
        Validate.nonNegative(wheelIndex, "wheel index");
        Validate.nonNull(suspension, "suspension");
        Validate.nonNull(mainBrake, "main brake");
        Validate.nonNull(parkingBrake, "parking brake");
        Validate.fraction(extraDamping, "extra damping");

        this.vehicle = vehicle;
        this.vehicleControl = vehicle.getVehicleControl();

        this.wheelIndex = wheelIndex;
        vehicleWheel = vehicleControl.getWheel(wheelIndex);
        assert vehicleWheel != null;

        this.isSteering = isSteering;
        this.isSteeringFlipped = steeringFlipped;

        this.suspension = suspension;
        this.mainBrake = mainBrake;
        this.parkingBrake = parkingBrake;
        this.extraDamping = extraDamping;

        setFriction(1f);
    }
    // *************************************************************************
    // new methods exposed

    // Pacejka
    // LATERAL
    // the slip angle is the angle between the direction in which a wheel is pointing
    // and the direction in which the vehicle is traveling.
    public float calculateLateralSlipAngle() {
        Quaternion wheelRot = vehicleControl.getPhysicsRotation().mult(
                new Quaternion().fromAngles(0f, getSteeringAngle(), 0f));

        Vector3f wheelDir = wheelRot.getRotationColumn(2);
        Vector3f vehicleTravel;
        if (vehicleControl.getCurrentVehicleSpeedKmHour() < 5f) {
            vehicleTravel = vehicleControl.getPhysicsRotation().getRotationColumn(2);
        } else {
            vehicleTravel = vehicleControl.getLinearVelocity().normalizeLocal();
            vehicleTravel.setY(0f);
        }

        float minAngle = 0.1f;
        float result = minAngle + wheelDir.angleBetween(vehicleTravel);
        // System.out.println(getVehicleWheel().getWheelSpatial().getName() + ": " + angle * FastMath.RAD_TO_DEG);

        result = FastMath.clamp(result, 0f, FastMath.QUARTER_PI);

        return result;
    }

    // the slip angle for this is how much force is being applied to the tire (acceleration force).
    // how much rotation has been applied as a result of acceleration.
    public float calculateLongitudinalSlipAngle() {
        // the rotation of the wheel as if it were just following a moving vehicle.
        // that is to say a wheel that is rolling without slip.
        float normalRot = vehicleWheel.getDeltaRotation();

        // the rotation applied via wheelspin
        float wheelSpinRot = getRotationDelta();

        // combined rotation of normal roll + wheelspin
        float rot = wheelSpinRot + normalRot;

        // System.out.println(getVehicleWheel().getWheelSpatial().getName() + ": " + rot);
        float vel = vehicleControl.getLinearVelocity().length();

        float minAngle = 0.1f;

        float result = rot / vel;
        result *= 10f;
        //angle += minAngle;

        //result = FastMath.QUARTER_PI - result;
        // return result + 0.1f;
        // float slip = 1.0f - vehicleWheel.getSkidInfo();
        // slip *= FastMath.QUARTER_PI;
        //return slip;
        result = FastMath.clamp(result, 0f, FastMath.TWO_PI);
        return result;
    }

    public float getDiameter() {
        return vehicleWheel.getWheelSpatial().getLocalScale().y; // they should all be the same.
    }

    /**
     * Determine the friction between this wheel's tire and the ground.
     *
     * @return the coefficient of friction
     */
    public float getFriction() {
        return vehicleWheel.getFrictionSlip();
    }

    /**
     * Determine the tire's grip.
     *
     * @return the fraction of the original grip remaining (&ge;0, &le;1)
     */
    public float getGrip() {
        return grip;
    }

    /**
     * Access the main brake for this wheel. (The main brakes are typically
     * hydraulic.)
     *
     * @return the pre-existing instance
     */
    public Brake getMainBrake() {
        return mainBrake;
    }

    public float getMaxSteerAngle() {
        assert maxSteerAngle >= 0f : maxSteerAngle;
        return maxSteerAngle;
    }

    /**
     * Determine the fraction of the engine's output power transmitted to this
     * wheel.
     *
     * @return the power fraction (&ge;0, &le;1)
     */
    public float getPowerFraction() {
        assert powerFraction >= 0f && powerFraction <= 1f : powerFraction;
        return powerFraction;
    }

    public float getRotationDelta() {
        return rotationDelta;
    }

    public float getSteeringAngle() {
        return steeringAngle;
    }

    public Suspension getSuspension() {
        assert suspension != null;
        return suspension;
    }

    /**
     * Access the tire model.
     *
     * @return the pre-existing instance
     */
    public PacejkaTireModel getTireModel() {
        return tireModel;
    }

    /**
     * Access the Vehicle that contains this Wheel.
     *
     * @return the pre-existing instance (not null)
     */
    public Vehicle getVehicle() {
        assert vehicle != null;
        return vehicle;
    }

    /**
     * Access the wheel's physics object.
     *
     * @return the pre-existing instance (not null)
     */
    public VehicleWheel getVehicleWheel() {
        assert vehicleWheel != null;
        return vehicleWheel;
    }

    /**
     * Test whether this wheel's brakes are applied.
     *
     * @return false if the braking impulse is negligible, otherwise true
     */
    public boolean isBraking() {
        float impulse = vehicleWheel.getBrake();
        if (impulse > +1f || impulse < -1f) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Test whether this wheel receives power from the Engine.
     *
     * @return true if receives power, otherwise false
     */
    public boolean isPowered() {
        if (powerFraction > 0f) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Test whether this wheel is used for steering.
     *
     * @return true if used for steering, otherwise false
     */
    public boolean isSteering() {
        return isSteering;
    }

    /**
     * Test whether this wheel turns in the opposite direction relative to the
     * Vehicle.
     *
     * @return true if opposite, otherwise false
     */
    public boolean isSteeringFlipped() {
        return isSteeringFlipped;
    }

    /**
     * Determine how much linear damping this Wheel contributes to its Vehicle.
     *
     * @return (&ge;0, &lt;1)
     */
    public float linearDamping() {
        float result = traction() * extraDamping;
        return result;
    }

    public void setDiameter(float diameter) {
        Validate.positive(diameter, "diameter");

        vehicleWheel.getWheelSpatial().setLocalScale(diameter);
        vehicleWheel.setRadius(diameter / 2);
    }

    public void setFriction(float friction) {
        vehicleWheel.setFrictionSlip(friction);
    }

    /**
     * Alter the tire's grip.
     *
     * @param grip the fraction of the original grip remaining (&ge;0, &le;1,
     * default=1)
     */
    public void setGrip(float grip) {
        this.grip = grip;
    }

    public void setMaxSteerAngle(float maxSteerAngle) {
        Validate.nonNegative(maxSteerAngle, "max steer angle");
        this.maxSteerAngle = maxSteerAngle;
    }

    /**
     * Alter the fraction of the of the engine's output power that gets
     * transmitted this wheel.
     *
     * @param fraction the desired power fraction (&ge;0, &le;1)
     */
    public void setPowerFraction(float fraction) {
        Validate.fraction(fraction, "fraction");
        powerFraction = fraction;
    }

    public void setRotationDelta(float rotationDelta) {
        this.rotationDelta = rotationDelta;
    }

    public void setSteering(boolean steering) {
        this.isSteering = steering;
        vehicleWheel.setFrontWheel(steering);
    }

    public void setSteering(boolean steering, boolean flipped) {
        setSteering(steering);
        setSteeringFlipped(flipped);
    }

    public void setSteeringFlipped(boolean steeringFlipped) {
        this.isSteeringFlipped = steeringFlipped;
    }

    /**
     * Alter the tire model.
     *
     * @param tireModel the desired model (alias created)
     */
    public void setTireModel(PacejkaTireModel tireModel) {
        this.tireModel = tireModel;
    }

    /**
     * Alter the color of smoke produced by the tire.
     *
     * @param color the desired color (not null, unaffected)
     */
    public void setTireSmokeColor(ColorRGBA color) {
        tireSmokeColor.set(color);
    }

    /**
     * Determine how much the wheel is skidding.
     *
     * @return the relative amount of skidding (&ge;0, &le;1, 0&rarr;
     * unsupported or full traction, 1&rarr;complete slippage)
     */
    public float skidFraction() {
        float result;
        float depth = vehicleControl.castRay(wheelIndex);
        if (depth == -1f) {
            result = 0f; // no supporting surface
        } else {
            result = 1f - vehicleWheel.getSkidInfo();
            if (result > 1f) {
                result = 1f;
            }
        }

        assert result >= 0f && result <= 1f : result;
        return result;
    }

    public void steer(float strength) {
        if (isSteering()) {
            if (isSteeringFlipped) {
                steeringAngle = getMaxSteerAngle() * -strength;
            } else {
                steeringAngle = getMaxSteerAngle() * strength;
            }

            vehicleControl.steer(wheelIndex, steeringAngle);
        }
    }

    /**
     * Determine the color of smoke produced by the tire.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return the color (either storeResult or a new instance)
     */
    public ColorRGBA tireSmokeColor(ColorRGBA storeResult) {
        if (storeResult == null) {
            return tireSmokeColor.clone();
        } else {
            return storeResult.set(tireSmokeColor);
        }
    }

    /**
     * Determine how much traction this wheel has.
     *
     * @return the relative amount of traction (&ge;0, &le;1, 0&rarr;unsupported
     * or complete slippage, 1&rarr;full traction)
     */
    public float traction() {
        float result;
        float depth = vehicleControl.castRay(wheelIndex);
        if (depth == -1f) {
            result = 0f; // no supporting surface
        } else {
            result = vehicleWheel.getSkidInfo();
            if (result < 0f) {
                result = 0f;
            }
        }

        assert result >= 0f && result <= 1f : result;
        return result;
    }

    /**
     * Update the drive force applied via this wheel.
     *
     * @param force the desired drive force (negative if reversing)
     */
    public void updateAccelerate(float force) {
        vehicleControl.accelerate(wheelIndex, force);
        assert vehicleWheel.getEngineForce() == force :
                vehicleWheel.getEngineForce();
    }

    /**
     * Update the braking impulse applied to this wheel.
     *
     * @param mainStrength the strength of the main-brake control signal (&ge;0,
     * &le;1)
     * @param parkingStrength the strength of the parking-brake control signal
     * (&ge;0, &le;1)
     */
    public void updateBrakes(float mainStrength, float parkingStrength) {
        Validate.fraction(mainStrength, "main strength");
        Validate.fraction(parkingStrength, "parking strength");

        VehicleWorld world = vehicle.getWorld();
        PhysicsSpace physicsSpace = world.getPhysicsSpace();
        float timeStep = physicsSpace.getAccuracy();

        float force = mainStrength * mainBrake.getPeakForce()
                + parkingStrength * parkingBrake.getPeakForce();
        float impulse = force * timeStep;
        if (impulse != vehicleWheel.getBrake()) {
            //System.out.printf("brake[%d] = %f%n", wheelIndex, impulse);
        }
        vehicleControl.brake(wheelIndex, impulse);

        assert vehicleWheel.getBrake() == impulse : vehicleWheel.getBrake();
    }
}