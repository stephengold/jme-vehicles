package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.part.Engine;
import com.jayfella.jme.vehicle.part.Gear;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.FastMath;
import java.util.logging.Logger;

/**
 * The automatic transmission of a Vehicle.
 *
 * Derived from the AutomaticGearBoxState class in the Advanced Vehicles
 * project.
 */
public class AutomaticGearboxState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(AutomaticGearboxState.class.getName());
    // *************************************************************************
    // fields

    private GearBox gearBox;
    private int wheelCount;
    final private Vehicle vehicle;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled AppState for the specified Vehicle.
     *
     * @param vehicle (alias created)
     */
    public AutomaticGearboxState(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void cleanup(Application application) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        this.wheelCount = vehicle.countWheels();
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        // do nothing
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        this.gearBox = vehicle.getGearBox();
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);

        Engine engine = vehicle.getEngine();
        boolean isEngineRunning = engine.isRunning();
        if (!isEngineRunning) {
            engine.setRpmFraction(0f);
            return;
        }

        GearBox gearbox = vehicle.getGearBox();
        float signedKph = vehicle.forwardSpeed(SpeedUnit.KPH);

        int gearNum = gearbox.getEngagedGearNum();
        Gear gear = gearBox.getGear(gearNum);
        float minKph = gear.getMinKph();
        float maxKph = gear.getMaxKph();
        if (gear.isReverse()) {
            int numGears = gearbox.countReverseGears();
            if (signedKph > minKph && gearNum < -1) {
                ++gearNum;
                //System.out.println("Downshifting to " + gearNum);
            } else if (signedKph < maxKph && gearNum > -numGears) {
                --gearNum;
                //System.out.println("Upshifting to " + gearNum);
            }
            gearbox.engageGearNum(gearNum); // TODO not instantaneous

        } else if (gear.isForward()) {
            int numGears = gearbox.countForwardGears();
            if (signedKph < minKph && gearNum > 1) {
                --gearNum;
                //System.out.println("Downshifting to " + gearNum);
            } else if (signedKph > maxKph && gearNum < numGears) {
                ++gearNum;
                //System.out.println("Upshifting to " + gearNum);
            }
            gearbox.engageGearNum(gearNum); // TODO not instantaneous
        }
        // TODO handle neutral gear

        gear = gearBox.getGear(gearNum);
        float redlineKph = gear.getRedlineKph();
        float revs = signedKph / redlineKph; // as a fraction of redline
        float accelerateSignal = vehicle.accelerateSignal();
        if (accelerateSignal == 0f) { // coasting
            revs = 0f;

        } else if (wheelCount > 0) {
            float boostRevs = 0f;
            for (int wheelIndex = 0; wheelIndex < wheelCount; ++wheelIndex) {
                Wheel wheel = vehicle.getWheel(wheelIndex);
                float wheelFraction = wheel.getPowerFraction();
                float scaledSignal = wheelFraction * accelerateSignal;

                // how much the tire is slipping (0-1, 0=full traction)
                float slipFraction = 1f - wheel.traction();

                // If both accelerating and slipping, boost the revs.
                boostRevs += scaledSignal * slipFraction;
            }
            revs += boostRevs;
        }
        /*
         * The drivetrain has some rotational inertia, even when slipping,
         * so limit how quickly the engine speed can change.
         */
        float oldRevs = engine.rpmFraction();
        revs = FastMath.clamp(revs, oldRevs - 2f * tpf, oldRevs + 0.2f * tpf);

        // Prevent the engine from stalling or exceeding its redline.
        float idleFraction = engine.idleFraction();
        revs = FastMath.clamp(revs, idleFraction, 1f);

        engine.setRpmFraction(revs);
    }
}
