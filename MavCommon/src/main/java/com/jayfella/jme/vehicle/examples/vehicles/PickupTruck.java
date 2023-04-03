package com.jayfella.jme.vehicle.examples.vehicles;

import com.jayfella.jme.vehicle.Sound;
import com.jayfella.jme.vehicle.Steering;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.WheelModel;
import com.jayfella.jme.vehicle.examples.engines.FlexibleEngine;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound1;
import com.jayfella.jme.vehicle.examples.sounds.HornSound1;
import com.jayfella.jme.vehicle.examples.tires.Tire01;
import com.jayfella.jme.vehicle.examples.wheels.RangerWheel;
import com.jayfella.jme.vehicle.part.Engine;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An example Vehicle, built around mauro.zampaoli's "Ford Ranger" model.
 */
public class PickupTruck extends Vehicle {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(PickupTruck.class.getName());
    // *************************************************************************
    // constructors

    public PickupTruck() {
        super("Pickup Truck");
    }
    // *************************************************************************
    // Vehicle methods

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
        /*
         * Load the C-G model with everything except the wheels.
         * Bullet refers to this as the "chassis".
         */
        float mass = 1_550f; // in kilos
        float linearDamping = 0.01f;
        setChassis("ford_ranger", "pickup", assetManager, mass, linearDamping);

        float diameter = 0.8f;
        WheelModel lFrontWheel = new RangerWheel(diameter);
        WheelModel rFrontWheel = new RangerWheel(diameter);
        WheelModel lRearWheel = new RangerWheel(diameter);
        WheelModel rRearWheel = new RangerWheel(diameter);
        lFrontWheel.load(assetManager);
        rFrontWheel.load(assetManager);
        lRearWheel.load(assetManager);
        rRearWheel.load(assetManager);
        /*
         * By convention, wheels are modeled for the left side, so
         * wheel models for the right side require a 180-degree rotation.
         */
        rFrontWheel.flip();
        rRearWheel.flip();
        /*
         * Add the wheels to the vehicle.
         * For rear-wheel steering, it will be necessary to "flip" the steering.
         */
        float wheelX = 0.75f; // half of the axle track
        float axleY = 0.45f; // height of the axles relative to vehicle's CoG
        float frontZ = 1.76f;
        float rearZ = -1.42f;
        float mainBrake = 4_000f; // all 4 wheels
        float parkingBrake = 25_000f; // in rear only
        float damping = 0.04f; // extra linear damping
        addWheel(lFrontWheel, new Vector3f(+wheelX, axleY, frontZ),
                Steering.DIRECT, mainBrake, 0f, damping);
        addWheel(rFrontWheel, new Vector3f(-wheelX, axleY, frontZ),
                Steering.DIRECT, mainBrake, 0f, damping);
        addWheel(lRearWheel, new Vector3f(+wheelX, axleY, rearZ),
                Steering.UNUSED, mainBrake, parkingBrake, damping);
        addWheel(rRearWheel, new Vector3f(-wheelX, axleY, rearZ),
                Steering.UNUSED, mainBrake, parkingBrake, damping);
        /*
         * Configure the suspension.
         *
         * This vehicle applies the same settings to each wheel,
         * but that isn't required.
         */
        for (Wheel wheel : listWheels()) {
            Suspension suspension = wheel.getSuspension();

            suspension.setMaxTravelCm(1_000f);

            // how much weight the suspension can take before it bottoms out
            // Setting this too low will make the wheels sink into the ground.
            suspension.setMaxForce(20_000f);

            // the stiffness of the suspension
            // Setting this too low can cause odd behavior.
            suspension.setStiffness(20f);
        }

        // Give each wheel a tire with friction.
        for (Wheel wheel : listWheels()) {
            wheel.setTireModel(new Tire01());
            wheel.setFriction(1f);
        }
        /*
         * Distribute drive power across the wheels:
         *  0 = no power, 1 = all the power
         *
         * This vehicle has 4-wheel drive.
         */
        getWheel(0).setPowerFraction(0.2f);
        getWheel(1).setPowerFraction(0.2f);
        getWheel(2).setPowerFraction(0.2f);
        getWheel(3).setPowerFraction(0.2f);
        /*
         * Specify the name and speed range for each gear.
         * The min-max speeds of successive gears should overlap.
         * The "min" speed of low gear should be zero.
         * The "max" speed of high gear determines the top speed.
         * The "red" speed of each gear is used to calculate its ratio.
         */
        GearBox gearBox = new GearBox(4, 1);
        gearBox.getGear(-1).setName("reverse").setMinMaxRedKph(0f, -40f, -40f);
        gearBox.getGear(1).setName("low").setMinMaxRedKph(0f, 19f, 25f);
        gearBox.getGear(2).setName("2nd").setMinMaxRedKph(12f, 50f, 60f);
        gearBox.getGear(3).setName("3rd").setMinMaxRedKph(40f, 80f, 90f);
        gearBox.getGear(4).setName("high").setMinMaxRedKph(70f, 110f, 110f);
        setGearBox(gearBox);

        float idleRpm = 800f;
        float redlineRpm = 6_000f;
        Engine engine = new FlexibleEngine("450-hp diesel 800-6000 RPM",
                450f * Engine.HP_TO_W, idleRpm, redlineRpm);
        setEngine(engine);

        Sound engineSound = new EngineSound1();
        engineSound.load(assetManager);
        engine.setSound(engineSound);

        Sound hornSound = new HornSound1();
        hornSound.load(assetManager);
        setHornSound(hornSound);

        build(); // must be invoked last, to complete the Vehicle
    }

    /**
     * Determine the offset of the truck's DashCamera in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    public void locateDashCam(Vector3f storeResult) {
        storeResult.set(0f, 1.5f, 1.1f);
    }

    /**
     * Determine the offset of the truck's ChaseCamera target in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    protected void locateTarget(Vector3f storeResult) {
        storeResult.set(0f, 0.91f, -2.75f);
    }
}
