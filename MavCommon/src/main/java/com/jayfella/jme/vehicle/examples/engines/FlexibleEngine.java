package com.jayfella.jme.vehicle.examples.engines;

import com.jayfella.jme.vehicle.part.Engine;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * A "flexible" Engine (such as a diesel) that delivers good torque across a
 * wide range of speeds.
 */
public class FlexibleEngine extends Engine {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(FlexibleEngine.class.getName());
    /**
     * points defining the power curve: x=rpmFraction, y=powerFraction
     */
    final private static Vector3f[] points = new Vector3f[]{
        new Vector3f(0f, 0f, 0f),
        new Vector3f(0.08f, 0.072f, 0f),
        new Vector3f(0.12f, 0.12f, 0f),
        new Vector3f(0.2f, 0.22f, 0f),
        new Vector3f(0.3f, 0.36f, 0f),
        new Vector3f(0.4f, 0.5f, 0f),
        new Vector3f(0.45f, 0.585f, 0f), // approx max torque
        new Vector3f(0.5f, 0.645f, 0f),
        new Vector3f(0.6f, 0.768f, 0f),
        new Vector3f(0.7f, 0.882f, 0f),
        new Vector3f(0.8f, 0.992f, 0f), // max power
        new Vector3f(0.9f, 0.954f, 0f),
        new Vector3f(1f, 0.9f, 0f) // redline
    };
    // *************************************************************************
    // fields

    /**
     * power curve
     */
    final private Spline spline;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a flexible Engine.
     *
     * @param name the desired name (not null)
     * @param maxWatts the desired maximum power output (in Watts, &gt;0)
     * @param idleRpm the desired idle speed (in RPMs, &ge;0, &lt;redlineRpm)
     * @param redlineRpm the desired redline speed (&gt;0)
     */
    public FlexibleEngine(String name, float maxWatts, float idleRpm,
            float redlineRpm) {
        super(name, maxWatts, idleRpm, redlineRpm);
        this.spline = new Spline(Spline.SplineType.Linear, points, 0.1f, false);
    }
    // *************************************************************************
    // Engine methods

    /**
     * Determine the fractional power output at the specified speed.
     *
     * @param rpm the angular speed of the crankshaft (in RPMs, &ge;0)
     * @return the power as a fraction of the maximum (&ge;0, &le;1)
     */
    @Override
    public float powerFraction(float rpm) {
        Validate.nonNegative(rpm, "rpm");

        float rpmFraction = rpm / redlineRpm();
        float result = evaluateSpline(spline, rpmFraction);
        return result;
    }
}
