package com.jayfella.jme.vehicle.examples.vehicles;

import com.jayfella.jme.vehicle.Sound;
import com.jayfella.jme.vehicle.Steering;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.WheelModel;
import com.jayfella.jme.vehicle.examples.engines.PeakyEngine;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound2;
import com.jayfella.jme.vehicle.examples.sounds.HornSound1;
import com.jayfella.jme.vehicle.examples.tires.Tire01;
import com.jayfella.jme.vehicle.examples.wheels.DarkAlloyWheel;
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
 * An example Vehicle, built around iSteven's "Nissan GT-R" model.
 */
public class Nismo extends Vehicle {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(Nismo.class.getName());
    // *************************************************************************
    // constructors

    public Nismo() {
        super("Nismo");
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
        float mass = 1_525f; // in kilos
        float linearDamping = 0.002f;
        setChassis(
                "gtr_nismo", "scene.gltf", assetManager, mass, linearDamping);

        float diameter = 0.74f;
        WheelModel lFrontWheel = new DarkAlloyWheel(diameter);
        WheelModel rFrontWheel = new DarkAlloyWheel(diameter);
        WheelModel lRearWheel = new DarkAlloyWheel(diameter);
        WheelModel rRearWheel = new DarkAlloyWheel(diameter);
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
        float wheelX = 0.8f; // half of the axle track
        float axleY = 0.32f; // height of the axles relative to vehicle's CoG
        float frontZ = 1.42f;
        float rearZ = -1.36f;
        float parkingBrake = 25_000f; // in rear only
        float damping = 0.009f; // extra linear damping
        addWheel(lFrontWheel, new Vector3f(+wheelX, axleY, frontZ),
                Steering.DIRECT, 6_750f, 0f, damping);
        addWheel(rFrontWheel, new Vector3f(-wheelX, axleY, frontZ),
                Steering.DIRECT, 6_750f, 0f, damping);
        addWheel(lRearWheel, new Vector3f(+wheelX, axleY, rearZ),
                Steering.UNUSED, 3_000f, parkingBrake, damping);
        addWheel(rRearWheel, new Vector3f(-wheelX, axleY, rearZ),
                Steering.UNUSED, 3_000f, parkingBrake, damping);
        /*
         * Configure the suspension.
         *
         * This vehicle applies the same settings to each wheel,
         * but that isn't required.
         */
        for (Wheel wheel : listWheels()) {
            Suspension suspension = wheel.getSuspension();

            // how much weight the suspension can take before it bottoms out
            // Setting this too low will make the wheels sink into the ground.
            suspension.setMaxForce(7_000f);

            // the stiffness of the suspension
            // Setting this too low can cause odd behavior.
            suspension.setStiffness(12.5f);

            // how fast the suspension will compress
            // 1 = slow, 0 = fast.
            suspension.setCompressDamping(0.3f);

            // how quickly the suspension will rebound back to height
            // 1 = slow, 0 = fast.
            suspension.setRelaxDamping(0.4f);
        }
        /*
         * Give each wheel a tire with friction.
         */
        for (Wheel wheel : listWheels()) {
            wheel.setTireModel(new Tire01());
            wheel.setFriction(1.6f);
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
        GearBox gearBox = new GearBox(6, 1);
        gearBox.getGear(-1).setName("reverse").setMinMaxRedKph(0f, -40f, -40f);
        gearBox.getGear(1).setName("low").setMinMaxRedKph(0f, 30f, 35f);
        gearBox.getGear(2).setName("2nd").setMinMaxRedKph(15f, 70f, 75f);
        gearBox.getGear(3).setName("3rd").setMinMaxRedKph(50f, 130f, 140f);
        gearBox.getGear(4).setName("4th").setMinMaxRedKph(120f, 190f, 200f);
        gearBox.getGear(5).setName("5th").setMinMaxRedKph(180f, 255f, 275f);
        gearBox.getGear(6).setName("high").setMinMaxRedKph(250f, 320f, 320f);
        setGearBox(gearBox);

        float idleRpm = 600f;
        float redlineRpm = 9_000f;
        Engine engine = new PeakyEngine("600-hp gasoline 600-9000 RPM",
                600f * Engine.HP_TO_W, idleRpm, redlineRpm);
        setEngine(engine);

        Sound engineSound = new EngineSound2();
        engineSound.load(assetManager);
        engine.setSound(engineSound);

        Sound hornSound = new HornSound1();
        hornSound.load(assetManager);
        setHornSound(hornSound);

        String assetPath = "/Models/MakeHuman/driver.j3o";
        VehicleControl body = getVehicleControl();
        Vector3f offset = new Vector3f(0.36f, -0.34f, -0.1f);
        String clipName = "driving:gtr_nismo";
        addPassenger(assetManager, assetPath, body, offset, clipName);

        build(); // must be invoked last, to complete the Vehicle
    }

    /**
     * Determine the offset of the Nismo's DashCamera in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    public void locateDashCam(Vector3f storeResult) {
        storeResult.set(0f, 1.5f, 0.5f);
    }

    /**
     * Determine the offset of the Nismo's ChaseCamera target in scaled shape
     * coordinates.
     *
     * @param storeResult storage for the result (not null)
     */
    @Override
    protected void locateTarget(Vector3f storeResult) {
        storeResult.set(0f, 0.6f, -2.31f);
    }
}
