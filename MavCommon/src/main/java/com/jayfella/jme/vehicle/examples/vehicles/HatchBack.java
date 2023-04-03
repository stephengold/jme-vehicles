package com.jayfella.jme.vehicle.examples.vehicles;

import com.jayfella.jme.vehicle.Sound;
import com.jayfella.jme.vehicle.Steering;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.WheelModel;
import com.jayfella.jme.vehicle.examples.engines.FlexibleEngine;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound4;
import com.jayfella.jme.vehicle.examples.sounds.HornSound1;
import com.jayfella.jme.vehicle.examples.tires.Tire02;
import com.jayfella.jme.vehicle.examples.wheels.HatchbackWheel;
import com.jayfella.jme.vehicle.part.Engine;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Suspension;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.math.Vector3f;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An example Vehicle, built around Daniel Zhabotinsky's "Modern Hatchback - Low
 * Poly" model.
 */
public class HatchBack extends Vehicle {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(HatchBack.class.getName());
    // *************************************************************************
    // constructors

    public HatchBack() {
        super("HatchBack");
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
        float mass = 1_140f; // in kilos
        float linearDamping = 0.004f;
        setChassis("modern_hatchback", "hatchback", assetManager, mass,
                linearDamping);

        float diameter = 0.65f;
        WheelModel lFrontWheel = new HatchbackWheel(diameter);
        WheelModel rFrontWheel = new HatchbackWheel(diameter);
        WheelModel lRearWheel = new HatchbackWheel(diameter);
        WheelModel rRearWheel = new HatchbackWheel(diameter);
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
        float wheelX = 0.66f; // half of the axle track
        float axleY = 0.14f; // height of the axles relative to vehicle's CoG
        float frontZ = 1.2f;
        float rearZ = -1.19f;
        float mainBrake = 5_000f; // all 4 wheels
        float parkingBrake = 25_000f; // in front only
        float damping = 0.025f; // extra linear damping
        addWheel(lFrontWheel, new Vector3f(+wheelX, axleY, frontZ),
                Steering.DIRECT, mainBrake, parkingBrake, damping);
        addWheel(rFrontWheel, new Vector3f(-wheelX, axleY, frontZ),
                Steering.DIRECT, mainBrake, parkingBrake, damping);
        addWheel(lRearWheel, new Vector3f(+wheelX, axleY, rearZ),
                Steering.UNUSED, mainBrake, 0f, damping);
        addWheel(rRearWheel, new Vector3f(-wheelX, axleY, rearZ),
                Steering.UNUSED, mainBrake, 0f, damping);
        /*
         * Configure the suspension.
         *
         * This vehicle applies the same settings to each wheel,
         * but that isn't required.
         */
        for (Wheel wheel : listWheels()) {
            Suspension suspension = wheel.getSuspension();

            // the stiffness of the suspension
            // Setting this too low can cause odd behavior.
            suspension.setStiffness(20f);

            // how fast the suspension will compress
            // 1 = slow, 0 = fast.
            suspension.setCompressDamping(0.6f);

            // how quickly the suspension will rebound back to height
            // 1 = slow, 0 = fast.
            suspension.setRelaxDamping(0.8f);
        }
        /*
         * Give each wheel a tire with friction.
         */
        for (Wheel wheel : listWheels()) {
            wheel.setTireModel(new Tire02());
            wheel.setFriction(0.9f);
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
        gearBox.getGear(1).setName("low").setMinMaxRedKph(0f, 30f, 40f);
        gearBox.getGear(2).setName("2nd").setMinMaxRedKph(20f, 60f, 70f);
        gearBox.getGear(3).setName("3rd").setMinMaxRedKph(50f, 100f, 120f);
        gearBox.getGear(4).setName("high").setMinMaxRedKph(80f, 140f, 140f);
        setGearBox(gearBox);

        float idleRpm = 700f;
        float redlineRpm = 4_500f;
        Engine engine = new FlexibleEngine("250-hp diesel 700-4500 RPM",
                250f * Engine.HP_TO_W, idleRpm, redlineRpm);
        setEngine(engine);

        Sound engineSound = new EngineSound4();
        engineSound.load(assetManager);
        engine.setSound(engineSound);

        Sound hornSound = new HornSound1();
        hornSound.load(assetManager);
        setHornSound(hornSound);

        String assetPath = "/Models/MakeHuman/driver.j3o";
        VehicleControl body = getVehicleControl();
        Vector3f offset = new Vector3f(0.36f, -0.39f, 0f);
        String clipName = "driving:modern_hatchback";
        addPassenger(assetManager, assetPath, body, offset, clipName);

        build(); // must be invoked last, to complete the Vehicle
    }

    /**
     * Determine the offset of the hatchback's DashCamera in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    public void locateDashCam(Vector3f storeResult) {
        storeResult.set(0f, 1.2f, 0.7f);
    }

    /**
     * Determine the offset of the hatchback's ChaseCamera target in scaled
     * shape coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    protected void locateTarget(Vector3f storeResult) {
        storeResult.set(0f, 0.52f, -1.7f);
    }
}
