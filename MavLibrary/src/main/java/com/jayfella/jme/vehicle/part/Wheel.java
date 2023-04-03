package com.jayfella.jme.vehicle.part;

import com.jayfella.jme.vehicle.Steering;
import static com.jayfella.jme.vehicle.Steering.DIRECT;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.VehicleWorld;
import com.jayfella.jme.vehicle.tire.PacejkaTireModel;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.objects.PhysicsVehicle;
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
     * index among the physics body's wheels (&ge;0)
     */
    final private int wheelIndex;

    private PacejkaTireModel tireModel;
    /**
     * physics body to which this Wheel is added
     */
    final private PhysicsVehicle body;
    /**
     * relationship to the steering system (not null)
     */
    private Steering steering;
    /**
     * parameters of the associated suspension spring
     */
    final private Suspension suspension;
    /**
     * Vehicle that contains this Wheel
     */
    final private Vehicle vehicle;
    /**
     * wheel's physics object
     */
    final private VehicleWheel vehicleWheel;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a wheel (added to the engine body) with the specified
     * parameters.
     *
     * @param vehicle the Vehicle to which this Wheel will be added (not null,
     * alias created)
     * @param wheelIndex the index among the engine body's wheels (&ge;0)
     * @param steering relationship to the steering system (not null)
     * @param suspension the suspension spring (not null, alias created)
     * @param mainBrake the main brake (not null, alias created)
     * @param parkingBrake the parking brake (not null, alias created)
     * @param extraDamping the additional linear damping (&ge;0, &lt;1)
     */
    public Wheel(Vehicle vehicle, int wheelIndex, Steering steering,
            Suspension suspension, Brake mainBrake, Brake parkingBrake,
            float extraDamping) {
        this(vehicle, vehicle.getVehicleControl(), wheelIndex,
                steering, suspension, mainBrake, parkingBrake, extraDamping);
    }

    /**
     * Instantiate a wheel with the specified parameters.
     *
     * @param vehicle the Vehicle to which this Wheel will be added (not null,
     * alias created)
     * @param body the physics body to which this Wheel will be added (not null,
     * alias created)
     * @param wheelIndex the index among the body's wheels (&ge;0)
     * @param steering relationship to the steering system (not null)
     * @param suspension the suspension spring (not null, alias created)
     * @param mainBrake the main brake (not null, alias created)
     * @param parkingBrake the parking brake (not null, alias created)
     * @param extraDamping the additional linear damping (&ge;0, &lt;1)
     */
    public Wheel(Vehicle vehicle, PhysicsVehicle body, int wheelIndex,
            Steering steering, Suspension suspension,
            Brake mainBrake, Brake parkingBrake, float extraDamping) {
        Validate.nonNull(vehicle, "vehicle");
        Validate.nonNegative(wheelIndex, "wheel index");
        Validate.nonNull(steering, "steering");
        Validate.nonNull(suspension, "suspension");
        Validate.nonNull(mainBrake, "main brake");
        Validate.nonNull(parkingBrake, "parking brake");
        Validate.fraction(extraDamping, "extra damping");

        this.vehicle = vehicle;
        this.body = body;
        this.wheelIndex = wheelIndex;
        vehicleWheel = body.getWheel(wheelIndex);
        assert vehicleWheel != null;

        this.steering = steering;
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
    /**
     * Determine the angle between the forward direction of the wheel and that
     * of the chassis. At speeds above 5 kilometers per hour, the direction of
     * motion of the chassis is used in place of its orientation.
     *
     * @return the angle (in radians, &ge;0.1, &lt;Pi/4)
     */
    public float calculateLateralSlipAngle() {
        Quaternion wheelRot = body.getPhysicsRotation().mult(
                new Quaternion().fromAngles(0f, getSteeringAngle(), 0f));

        Vector3f wheelDir = wheelRot.getRotationColumn(2);
        Vector3f vehicleTravel;
        if (body.getCurrentVehicleSpeedKmHour() < 5f) {
            vehicleTravel = body.getPhysicsRotation().getRotationColumn(2);
        } else {
            vehicleTravel = body.getLinearVelocity().normalizeLocal();
            vehicleTravel.setY(0f);
        }

        float minAngle = 0.1f;
        float result = minAngle + wheelDir.angleBetween(vehicleTravel);
        // System.out.println(getVehicleWheel().getWheelSpatial().getName()
        //         + ": " + angle * FastMath.RAD_TO_DEG);

        result = FastMath.clamp(result, 0f, FastMath.QUARTER_PI);

        return result;
    }

    // the slip angle for this is how much force is being applied to the tire (acceleration force).
    // how much rotation has been applied as a result of acceleration.
    /**
     * Estimate the amount of tire rotation caused by acceleration slippage.
     *
     * @return the angle (in radians, &ge;0, &lt;2*Pi)
     */
    public float calculateLongitudinalSlipAngle() {
        // the rotation of the wheel as if it were just following a moving vehicle.
        // that is to say a wheel that is rolling without slip.
        float normalRot = vehicleWheel.getDeltaRotation();

        // the rotation applied via wheelspin
        float wheelSpinRot = getRotationDelta();

        // combined rotation of normal roll + wheelspin
        float rot = wheelSpinRot + normalRot;

        // System.out.println(getVehicleWheel().getWheelSpatial().getName() + ": " + rot);
        float vel = body.getLinearVelocity().length();

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

    /**
     * Determine the wheel's diameter.
     *
     * @return the diameter (in meters)
     */
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

    /**
     * Determine the maximum steering angle for this wheel, relative to the
     * chassis.
     *
     * Wheels are assumed able to turn the same amount in both directions.
     *
     * @return the angular limit (in radians, &ge;0)
     */
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

    /**
     * Determine the fraction of engine power lost to this wheel's longitudinal
     * slippage.
     *
     * @return the power fraction (&ge;0, &le;1)
     */
    public float getRotationDelta() {
        return rotationDelta;
    }

    /**
     * Determine the steering angle between the wheel and the chassis.
     *
     * @return the angle (in radians)
     */
    public float getSteeringAngle() {
        return steeringAngle;
    }

    /**
     * Access the wheel's Suspension.
     *
     * @return the pre-existing instance (not null)
     */
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
        boolean result = (steering != Steering.UNUSED);
        return result;
    }

    /**
     * Test whether this wheel turns in the opposite direction relative to the
     * Vehicle.
     *
     * @return true if opposite, otherwise false
     */
    public boolean isSteeringFlipped() {
        boolean result = (steering == Steering.FLIPPED);
        return result;
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

    /**
     * Alter the wheel's diameter.
     *
     * @param diameter the desired diameter (in meters, &gt;0)
     */
    public void setDiameter(float diameter) {
        Validate.positive(diameter, "diameter");

        vehicleWheel.getWheelSpatial().setLocalScale(diameter);
        vehicleWheel.setRadius(diameter / 2);
    }

    /**
     * Alter the friction between this wheel's tire and the ground.
     *
     * @param friction the desired coefficient of friction
     */
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

    /**
     * Alter the maximum steering angle for this wheel, relative to the chassis.
     *
     * Wheels are assumed able to turn the same amount in both directions.
     *
     * @param maxSteerAngle the desired limit (in radians, &ge;0)
     */
    public void setMaxSteerAngle(float maxSteerAngle) {
        Validate.nonNegative(maxSteerAngle, "max steer angle");
        this.maxSteerAngle = maxSteerAngle;
    }

    /**
     * Alter the fraction of the engine's output power that gets transmitted
     * this wheel.
     *
     * @param fraction the desired power fraction (&ge;0, &le;1)
     */
    public void setPowerFraction(float fraction) {
        Validate.fraction(fraction, "fraction");
        powerFraction = fraction;
    }

    /**
     * Update the fraction of engine power lost to this wheel's longitudinal
     * slippage.
     *
     * @param rotationDelta the power fraction (&ge;0, &le;1)
     */
    public void setRotationDelta(float rotationDelta) {
        this.rotationDelta = rotationDelta;
    }

    /**
     * Alter how the wheel is steered, if at all.
     *
     * @param steering (not null)
     */
    public void setSteering(Steering steering) {
        Validate.nonNull(steering, "steering");

        this.steering = steering;
        vehicleWheel.setFrontWheel(steering != Steering.UNUSED);
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
        float depth = body.castRay(wheelIndex);
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

    /**
     * Update the steering angle based on the specified control signal, as
     * follows:
     *
     * If the wheel isn't used for steering, this method has no effect.
     *
     * If it's used for steering and flipped, a positive signal turns it to the
     * right.
     *
     * If it's used for steering and not flipped, a positive signal turns it to
     * the left.
     *
     * @param strength the control signal (in radians)
     */
    public void steer(float strength) {
        switch (steering) {
            case DIRECT:
                this.steeringAngle = getMaxSteerAngle() * strength;
                body.steer(wheelIndex, steeringAngle);
                break;

            case FLIPPED:
                this.steeringAngle = getMaxSteerAngle() * -strength;
                body.steer(wheelIndex, steeringAngle);
                break;

            case UNUSED:
                break;

            default:
                throw new IllegalStateException("steering = " + steering);
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
        float depth = body.castRay(wheelIndex);
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
        body.accelerate(wheelIndex, force);
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
        body.brake(wheelIndex, impulse);

        assert vehicleWheel.getBrake() == impulse : vehicleWheel.getBrake();
    }
}
