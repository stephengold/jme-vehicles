package com.jayfella.jme.vehicle.examples.engines;

import com.jayfella.jme.vehicle.part.Engine;
import com.jme3.math.Spline;
import com.jme3.math.Vector3f;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * A 180-horsepower engine with a 600-RPM idle and a 5,000-RPM redline.
 */
public class Engine180HP extends Engine {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Engine180HP.class.getName());
    /**
     * points defining the power curve: x=RPMs, y=powerFraction
     */
    final private static Vector3f[] points = new Vector3f[]{
        new Vector3f(0f, 0.25f, 0f),
        new Vector3f(1_000, 0.35f, 0f),
        new Vector3f(2_000, 0.4f, 0f),
        new Vector3f(3_000, 0.8f, 0f),
        new Vector3f(4_000, 0.99f, 0f),
        new Vector3f(5_000, 0.95f, 0f)
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
     * Instantiate a 180-horsepower engine.
     */
    public Engine180HP() {
        super("Basic 180", 180f * HP_TO_W, 600f, 5_000f);
        spline = new Spline(Spline.SplineType.Linear, points, 0.1f, false);
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
        float result = evaluateSpline(spline, rpm);
        return result;
    }
}