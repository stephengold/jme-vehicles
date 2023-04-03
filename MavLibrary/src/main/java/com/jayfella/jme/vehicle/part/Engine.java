package com.jayfella.jme.vehicle.part;

import com.jayfella.jme.vehicle.HasNode;
import com.jayfella.jme.vehicle.Sound;
import com.jme3.math.FastMath;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * An engine to propel a Vehicle.
 *
 * Derived from the Engine class in the Advanced Vehicles project.
 */
abstract public class Engine implements EngineSpeed {
    // *************************************************************************
    // constants and loggers

    /**
     * approximate factor to convert horsepower to Watts
     */
    final public static float HP_TO_W = 745f;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(Engine.class.getName());
    // *************************************************************************
    // fields

    /**
     * true when on/running, false when off/stopped
     */
    private boolean isRunning;
    /**
     * crankshaft rotation rate while idling (in revolutions per minute, &ge;0)
     */
    private float idleRpm;
    /**
     * maximum power output (in Watts, &gt;0)
     */
    private float maxOutputWatts;
    /**
     * crankshaft rotation rate at the redline (in revolutions per minute,
     * &ge;idleRpm)
     */
    final private float redlineRpm;
    /**
     * rotational speed as a fraction of the redline (&ge;0)
     */
    private float rpmFraction;
    /**
     * sound produced when the Engine is running, or null for silence
     */
    private Sound sound;
    /**
     * descriptive name (not null)
     */
    final private String name;
    /**
     * vehicle that contains this Engine, or null if none
     */
    private HasNode vehicle;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an Engine in the "off" state, without audio.
     *
     * @param name the desired name (not null)
     * @param maxWatts the desired maximum power output (in Watts, &gt;0)
     * @param idleRpm the desired idle speed (in RPMs, &ge;0, &lt;redlineRpm)
     * @param redlineRpm the desired redline speed (&gt;0)
     */
    public Engine(
            String name, float maxWatts, float idleRpm, float redlineRpm) {
        Validate.positive(maxWatts, "max Watts");
        Validate.inRange(idleRpm, "idle RPM", 0f, redlineRpm);
        Validate.positive(redlineRpm, "redline RPM");

        this.name = name;
        this.maxOutputWatts = maxWatts;
        this.idleRpm = idleRpm;
        this.redlineRpm = redlineRpm;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the maximum power output.
     *
     * @return the power (in Watts, &gt;0)
     */
    public float getMaxOutputWatts() {
        assert maxOutputWatts > 0f : maxOutputWatts;
        return maxOutputWatts;
    }

    /**
     * Determine this engine's name.
     *
     * @return the descriptive name (not null)
     */
    public String getName() {
        assert name != null;
        return name;
    }

    /**
     * Access the engine's sound.
     *
     * @return the pre-existing Sound, or null for silence
     */
    public Sound getSound() {
        return sound;
    }

    /**
     * Test whether this Engine is running.
     *
     * @return true if running, otherwise false
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Determine the current power output.
     *
     * @return the power (in Watts, &gt;0, &le;maxOutputWatts)
     */
    public float outputWatts() {
        float revs = rpm();
        revs = FastMath.clamp(revs, 0, redlineRpm() - 0.01f);
        float powerFraction = powerFraction(revs);
        float result = powerFraction * getMaxOutputWatts();

        assert result >= 0f && result <= getMaxOutputWatts() : result;
        return result;
    }

    /**
     * Determine the power output for the specified speed.
     *
     * @param rpm the crankshaft rotation rate (in revolutions per minute,
     * &ge;0)
     * @return the fractional power output (between 0 and 1)
     */
    abstract public float powerFraction(float rpm);

    /**
     * Alter this engine's maximum power output.
     *
     * @param watts the desired amount of power (&gt;0)
     */
    public void setMaxOutputWatts(float watts) {
        Validate.positive(watts, "watts");
        this.maxOutputWatts = watts;
    }

    /**
     * Alter the engine's speed as a fraction of the redline.
     *
     * @param revs the desired fraction (&ge;0)
     */
    public void setRpmFraction(float revs) {
        Validate.fraction(revs, "revs");
        this.rpmFraction = revs;
    }

    /**
     * Alter whether this Engine is running.
     *
     * @param newState the desired state (default=false)
     */
    public void setRunning(boolean newState) {
        this.isRunning = newState;
    }

    /**
     * Alter the engine's Sound.
     *
     * @param desiredSound the desired Sound (loaded), or null for silence
     */
    public void setSound(Sound desiredSound) {
        if (sound != null && vehicle != null) {
            sound.mute();
            sound.detach();
        }
        this.sound = desiredSound;
        if (sound != null && vehicle != null) {
            Node parent = vehicle.getNode();
            sound.attachTo(parent);
        }
    }

    /**
     * Alter which vehicle (if any) contains this Engine.
     *
     * @param desiredVehicle the desired Vehicle, or null for none
     */
    public void setVehicle(HasNode desiredVehicle) {
        if (sound != null && vehicle != null) {
            sound.mute();
            sound.detach();
        }
        this.vehicle = desiredVehicle;
        if (sound != null && vehicle != null) {
            Node parent = vehicle.getNode();
            sound.attachTo(parent);
        }
    }
    // *************************************************************************
    // new protected methods

    /**
     * Determine the power output for the specified speed, using a Spline.
     *
     * @param powerCurve the spline to use (not null, unaffected)
     * @param rpm the crankshaft rotation rate (in revolutions per minute)
     * @return the power as a fraction of the maximum (&ge;0, &le;1)
     */
    protected static float evaluateSpline(Spline powerCurve, float rpm) {
        List<Vector3f> points = powerCurve.getControlPoints();
        int lastIndex = points.size() - 1;
        for (int lowIndex = 0; lowIndex < lastIndex; ++lowIndex) {
            float lowRpm = points.get(lowIndex).x;
            float highRpm = points.get(lowIndex + 1).x;
            if (rpm >= lowRpm && rpm <= highRpm) {
                float t = FastMath.unInterpolateLinear(rpm, lowRpm, highRpm);
                Vector3f interpolatedPoint
                        = powerCurve.interpolate(t, lowIndex, null);
                assert FastMath.approximateEquals(interpolatedPoint.x, rpm);
                float result = interpolatedPoint.y;

                assert result >= 0f && result <= 1f : result;
                return result;
            }
        }

        throw new IllegalArgumentException("rpm = " + rpm);
    }
    // *************************************************************************
    // EngineSpeed methods

    /**
     * Determine the idle speed as a fraction of the redline.
     *
     * @return the fraction (&ge;0)
     */
    @Override
    public float idleFraction() {
        float result = idleRpm / redlineRpm;
        assert result >= 0f : result;
        return result;
    }

    /**
     * Determine the idle speed of the Engine.
     *
     * @return the crankshaft rotation rate (in revolutions per minute, &ge;0,
     * &le;redlineRpm)
     */
    @Override
    public float idleRpm() {
        assert idleRpm >= 0f && idleRpm <= redlineRpm : idleRpm;
        return idleRpm;
    }

    /**
     * Determine the redline speed of the Engine.
     *
     * @return the crankshaft rotation rate (in revolutions per minute, &gt;0,
     * &ge;idleRpm)
     */
    @Override
    public float redlineRpm() {
        assert redlineRpm > 0f : redlineRpm;
        assert redlineRpm >= idleRpm : redlineRpm;
        return redlineRpm;
    }

    /**
     * Determine the current engine speed.
     *
     * @return the crankshaft rotation rate (in revolutions per minute, &ge;0)
     */
    @Override
    public float rpm() {
        float result = rpmFraction * redlineRpm;
        assert result >= 0f : result;
        return result;
    }

    /**
     * Determine the current engine speed as a fraction of the redline.
     *
     * @return the fraction (&ge;0, &le;1)
     */
    @Override
    public float rpmFraction() {
        assert rpmFraction >= 0f && rpmFraction <= 1f : rpmFraction;
        return rpmFraction;
    }
}
