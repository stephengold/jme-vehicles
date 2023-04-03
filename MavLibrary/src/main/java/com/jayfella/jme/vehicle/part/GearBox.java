package com.jayfella.jme.vehicle.part;

import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * The gearbox (or transmission) of a Vehicle, including the differential.
 *
 * Derived from the GearBox class in the Advanced Vehicles project.
 */
public class GearBox {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(GearBox.class.getName());
    // *************************************************************************
    // fields

    /**
     * neutral gear (for gearNum=0)
     */
    final private Gear neutral = new Gear();
    /**
     * forward gears, each with their own ratio (for gearNum&gt;0)
     */
    final private Gear[] forwardGears;
    /**
     * reverse gears, each with their own ratio (for gearNum&lt;0, typically
     * just one)
     */
    final private Gear[] reverseGears;
    /**
     * number of the gear that's currently engaged
     */
    private int engagedGearNum = 1;
    /**
     * names of all available modes in the automatic transmission, in
     * top-to-bottom order
     */
    final private List<String> modeNames = new ArrayList<>(9);
    // *************************************************************************
    // constructors

    /**
     * Instantiate a GearBox with the specified numbers of gears.
     *
     * @param numForwardGears the number of forward gears (&ge;1)
     * @param numReverseGears the number of reverse gears (&ge;0, typically 1)
     */
    public GearBox(int numForwardGears, int numReverseGears) {
        Validate.positive(numForwardGears, "number of forward gears");
        Validate.nonNegative(numReverseGears, "number of reverse gears");

        modeNames.add("R");
        modeNames.add("D");

        forwardGears = new Gear[numForwardGears];
        for (int i = 0; i < numForwardGears; ++i) {
            forwardGears[i] = new Gear();
        }

        reverseGears = new Gear[numReverseGears];
        for (int i = 0; i < numReverseGears; ++i) {
            reverseGears[i] = new Gear();
        }
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Count the forward gears.
     *
     * @return the count (&gt;0)
     */
    public int countForwardGears() {
        return forwardGears.length;
    }

    /**
     * Count the reverse gears.
     *
     * @return the count (&ge;0)
     */
    public int countReverseGears() {
        return reverseGears.length;
    }

    /**
     * Alter which gear is engaged.
     *
     * @param gearNum the number of the desired gear
     */
    public void engageGearNum(int gearNum) {
        Validate.inRange(gearNum, "gear number",
                -reverseGears.length, forwardGears.length);
        engagedGearNum = gearNum;
    }

    /**
     * Access the Gear that's currently engaged.
     *
     * @return the pre-existing instance (not null)
     */
    public Gear getEngagedGear() {
        Gear result = getGear(engagedGearNum);
        return result;
    }

    /**
     * Determine which Gear is currently engaged.
     *
     * @return the number of the gear
     */
    public int getEngagedGearNum() {
        return engagedGearNum;
    }

    /**
     * Access a gear by number.
     *
     * @param gearNum the number of the gear
     * @return the pre-existing instance (not null)
     */
    public Gear getGear(int gearNum) {
        Gear result;
        if (gearNum > 0) {
            result = forwardGears[gearNum - 1];
        } else if (gearNum < 0) {
            result = reverseGears[-gearNum - 1];
        } else {
            assert gearNum == 0 : gearNum;
            result = neutral;
        }

        return result;
    }

    /**
     * Test whether this GearBox is in reverse.
     *
     * @return true if a reverse gear is engaged, otherwise false
     */
    public boolean isInReverse() {
        if (engagedGearNum < 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Enumerate all available modes of the automatic transmission.
     *
     * @return an unmodifiable list of names, in top-to-bottom order (not null)
     */
    public List<String> listAutomaticModes() {
        return Collections.unmodifiableList(modeNames);
    }

    /**
     * Determine the maximum forward speed.
     *
     * @param speedUnit (not null)
     * @return the tread speed (&gt;0, in the specified units)
     */
    public float maxForwardSpeed(SpeedUnit speedUnit) {
        int topGearNum = forwardGears.length;
        Gear topGear = forwardGears[topGearNum - 1];
        float kph = topGear.getMaxKph();

        float result;
        switch (speedUnit) {
            case KPH:
                result = kph;
                break;
            case MPH:
                result = kph * Vehicle.KPH_TO_MPH;
                break;
            case WUPS:
                result = kph * Vehicle.KPH_TO_WUPS;
                break;
            default:
                throw new IllegalArgumentException("speedUnit = " + speedUnit);
        }

        return result;
    }

    /**
     * Determine the maximum reverse speed.
     *
     * @param speedUnit (not null)
     * @return the tread speed (&lt;0, in the specified units)
     */
    public float maxReverseSpeed(SpeedUnit speedUnit) {
        int topGearNum = reverseGears.length;
        Gear topGear = reverseGears[topGearNum - 1];
        float kph = topGear.getMaxKph();

        float result;
        switch (speedUnit) {
            case KPH:
                result = kph;
                break;
            case MPH:
                result = kph * Vehicle.KPH_TO_MPH;
                break;
            case WUPS:
                result = kph * Vehicle.KPH_TO_WUPS;
                break;
            default:
                throw new IllegalArgumentException("speedUnit = " + speedUnit);
        }

        return result;
    }

    /**
     * Convenience method to switch between forward and reverse.
     *
     * @param reverse true for reverse, false for forward
     */
    public void setReversing(boolean reverse) {
        boolean isInReverse = isInReverse();
        if (reverse && !isInReverse && reverseGears.length > 0) {
            engageGearNum(-1);
        } else if (isInReverse && !reverse) {
            engageGearNum(1);
        }
    }
}
