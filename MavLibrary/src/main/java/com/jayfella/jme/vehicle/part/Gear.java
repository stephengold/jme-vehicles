package com.jayfella.jme.vehicle.part;

import com.jayfella.jme.vehicle.Vehicle;
import java.util.logging.Logger;
import jme3utilities.MyString;

/**
 * A single configuration of a GearBox, including its redline speed and its
 * downshift/upshift speeds in the automatic transmission.
 *
 * Derived from the Gear class in the Advanced Vehicles project.
 */
public class Gear {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger = Logger.getLogger(Gear.class.getName());
    // *************************************************************************
    // fields

    /**
     * signed tread speed (in kilometers per hour) that should trigger an
     * automatic downshift: negative for a reverse gear
     */
    private float downshiftKph;
    /**
     * signed tread speed (in kilometers per hour) at the redline: negative for
     * a reverse gear
     */
    private float redlineKph;
    /**
     * signed tread speed (in kilometers per hour) that should trigger an
     * automatic upshift: negative for a reverse gear
     */
    private float upshiftKph;
    /**
     * descriptive name, such as "2nd" or "reverse"
     */
    private String name = "neutral";
    // *************************************************************************
    // constructors

    /**
     * A no-arg constructor to avoid javadoc warnings from JDK 18.
     */
    public Gear() {
        // do nothing
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the tread speed that triggers an automatic upshift.
     *
     * @return the signed speed (in kilometers per hour)
     */
    public float getMaxKph() {
        return upshiftKph;
    }

    /**
     * Determine the tread speed that triggers an automatic downshift.
     *
     * @return the signed speed (in kilometers per hour)
     */
    public float getMinKph() {
        return downshiftKph;
    }

    /**
     * Determine the tread speed at the redline.
     *
     * @return the signed speed (in kilometers per hour)
     */
    public float getRedlineKph() {
        return redlineKph;
    }

    /**
     * Determine the inverse speed ratio, defined as axle (output) angular rate
     * divided by crankshaft (input) angular rate: negative for reverse, 0 for
     * neutral, positive for a forward gear, and &gt;1 for overdrive
     *
     * @param vehicle the Vehicle containing this Gear (not null, unaffected)
     * @return the ratio
     */
    public float inverseRatio(Vehicle vehicle) {
        float redlineWups = redlineKph * Vehicle.KPH_TO_WUPS;
        float circumferenceWu = vehicle.driveWheelCircumference();
        float axleRedlineRpm = 60 * redlineWups / circumferenceWu;
        float crankshaftRedlineRpm = vehicle.getEngine().redlineRpm();
        float result = axleRedlineRpm / crankshaftRedlineRpm;

        return result;
    }

    /**
     * Test whether this is a forward gear.
     *
     * @return true if forward gear, otherwise false
     */
    public boolean isForward() {
        if (redlineKph > 0f) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Test whether this is neutral gear.
     *
     * @return true if neutral gear, otherwise false
     */
    public boolean isNeutral() {
        if (redlineKph == 0f) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Test whether this is a reverse gear.
     *
     * @return true if reverse gear, otherwise false
     */
    public boolean isReverse() {
        if (redlineKph < 0f) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine the name of this gear.
     *
     * @return the descriptive name (not null, not empty)
     */
    public String name() {
        assert name != null && !name.isEmpty() : MyString.quote(name);
        return name;
    }

    /**
     * Alter the tread speed that triggers an automatic upshift.
     *
     * @param upshiftKph the desired signed speed (in kilometers per hour)
     */
    public void setMaxKph(float upshiftKph) {
        this.upshiftKph = upshiftKph;
    }

    /**
     * Alter the tread speed that triggers an automatic downshift.
     *
     * @param downshiftKph the desired signed speed (in kilometers per hour)
     */
    public void setMinKph(float downshiftKph) {
        this.downshiftKph = downshiftKph;
    }

    /**
     * Alter the name of this Gear.
     *
     * @param newName the desired name (not null, not empty)
     * @return this, for chaining
     */
    public Gear setName(String newName) {
        this.name = newName;
        return this;
    }

    /**
     * Convenience method to configure the speed range and ratio.
     *
     * @param downshiftKph the desired signed speed for downshift (in kilometers
     * per hour)
     * @param upshiftKph the desired signed speed for upshift (in kilometers per
     * hour)
     * @param redlineKph the desired signed tread speed at the redline (in
     * kilometers per hour)
     */
    public void setMinMaxRedKph(
            float downshiftKph, float upshiftKph, float redlineKph) {
        setMinKph(downshiftKph);
        setMaxKph(upshiftKph);
        setRedlineKph(redlineKph);
    }

    /**
     * Alter the tread speed at the redline.
     *
     * @param redlineKph the desired signed speed (in kilometers per hour)
     */
    public void setRedlineKph(float redlineKph) {
        this.redlineKph = redlineKph;
    }
}
