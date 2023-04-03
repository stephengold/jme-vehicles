package com.jayfella.jme.vehicle.niftydemo.state;

import com.jayfella.jme.vehicle.Prop;
import com.jayfella.jme.vehicle.examples.props.WarningSign;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;

/**
 * A proposed configuration for a new Prop.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class PropProposal implements JmeCloneable {
    // *************************************************************************
    // constants and loggers

    /**
     * argument-type array for getDeclaredConstructor()
     */
    final private static Class[] twoFloats = {float.class, float.class};
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(PropProposal.class.getName());
    // *************************************************************************
    // fields

    /**
     * true&rarr;active, false&rarr;inactive
     */
    private boolean active = false;
    /**
     * true&rarr;selected, false&rarr;not selected
     */
    private boolean autoSelect;
    /**
     * true&rarr;valid, false&rarr;invalid
     */
    private boolean valid;
    /**
     * factor to convert distances from model units to world units (&gt;0)
     */
    private float scaleFactor;
    /**
     * total mass (in kilograms, &gt;0)
     */
    private float totalMass;
    /**
     * type (not null)
     */
    private PropType propType;
    /**
     * initial orientation (in world coordinates)
     */
    private Quaternion initialOrientation = new Quaternion();
    /**
     * initial location (in world coordinates)
     */
    private Vector3f initialLocation = new Vector3f();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a default proposal.
     */
    PropProposal() {
        reset();
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Instantiate an unloaded Prop based on this proposal.
     *
     * @return a new instance
     */
    public Prop create() {
        Prop result = create(propType, scaleFactor, totalMass);
        return result;
    }

    /**
     * Determine the descaled total mass.
     *
     * @return the mass for scale=1 (in kilograms, &gt;0)
     */
    public float descaledMass() {
        return totalMass / MyMath.cube(scaleFactor);
    }

    /**
     * Mark this proposal invalid.
     */
    public void invalidate() {
        this.valid = false;
    }

    /**
     * Test whether this proposal is active.
     *
     * @return true if active, otherwise false
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Test whether the proposed Prop will be automatically selected after its
     * creation.
     *
     * @return true if selected, otherwise false
     */
    public boolean isAutoSelected() {
        return autoSelect;
    }

    /**
     * Test whether this proposal is valid.
     *
     * @return true if valid, otherwise false
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Copy the proposed initial location.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a location vector (in world coordinates, either storeResult or a
     * new instance, not null)
     */
    public Vector3f location(Vector3f storeResult) {
        logger.log(Level.INFO, "");

        Vector3f result;
        if (storeResult == null) {
            result = initialLocation.clone();
        } else {
            result = storeResult.set(initialLocation);
        }

        return result;
    }

    /**
     * Determine the proposed initial orientation.
     *
     * @param storeResult storage for the result (modified if not null)
     * @return a unit quaternion (in world coordinates, either storeResult or a
     * new instance, not null)
     */
    public Quaternion orientation(Quaternion storeResult) {
        Quaternion result;
        if (storeResult == null) {
            result = initialOrientation.clone();
        } else {
            result = storeResult.set(initialOrientation);
        }

        return result;
    }

    /**
     * Reset this proposal to the default values (except the active flag).
     */
    final public void reset() {
        this.autoSelect = true;
        this.valid = false;
        this.scaleFactor = 1f;
        this.totalMass = new WarningSign(1f, 1f).defaultDescaledMass();
        this.propType = PropType.WarningSign;
        initialOrientation.loadIdentity();
        initialLocation.zero();
    }

    /**
     * Rotate the proposed initial orientation around the world Y axis.
     *
     * @param angle the angle of rotation (in radians, counter-clockwise around
     * +Y)
     */
    public void rotateY(float angle) {
        Quaternion rotation = new Quaternion();
        rotation.fromAngles(0f, angle, 0f);
        rotation.mult(initialOrientation, initialOrientation);
    }

    /**
     * Determine the proposed scale factor.
     *
     * @return the scale factor (&gt;0, default=10)
     */
    public float scaleFactor() {
        return scaleFactor;
    }

    /**
     * Alter whether this proposal is active/visualized.
     *
     * @param setting true&rarr;activate, false&rarr;deactivate
     */
    public void setActive(boolean setting) {
        this.active = setting;
    }

    /**
     * Alter whether to select the new Prop.
     *
     * @param setting true&rarr;select, false&rarr;don't select
     */
    public void setAutoSelect(boolean setting) {
        this.autoSelect = setting;
    }

    /**
     * Alter the initial location and mark as valid.
     *
     * @param location the desired location vector (in world coordinates, not
     * null)
     */
    public void setLocation(Vector3f location) {
        logger.log(Level.INFO, "{0}", location);
        Validate.finite(location, "location");

        initialLocation.set(location);
        valid = true;
    }

    /**
     * Alter the proposed descaled total mass.
     *
     * @param descaledMass the desired mass for scale=1 (in kilograms, &gt;0)
     */
    public void setDescaledMass(float descaledMass) {
        Validate.positive(descaledMass, "descaled mass");
        this.totalMass = MyMath.cube(scaleFactor) * descaledMass;
    }

    /**
     * Alter the proposed total mass.
     *
     * @param mass the desired mass (in kilograms, &gt;0)
     */
    public void setTotalMass(float mass) {
        Validate.positive(mass, "mass");
        this.totalMass = mass;
    }

    /**
     * Alter the initial orientation.
     *
     * @param orientation the desired orientation (in world coordinates, not
     * null, unaffected)
     */
    public void setOrientation(Quaternion orientation) {
        logger.log(Level.INFO, "{0}", orientation);
        Validate.nonNull(orientation, "orientation");

        initialOrientation.set(orientation);
    }

    /**
     * Alter the proposed scale factor.
     *
     * @param scale the desired scale factor (&gt;0)
     */
    public void setScale(float scale) {
        Validate.positive(scale, "scale");

        if (scale != scaleFactor) {
            float increaseFactor = scale / scaleFactor;
            this.totalMass *= MyMath.cube(increaseFactor);

            this.scaleFactor = scale;
        }
    }

    /**
     * Alter the proposed type of Prop.
     *
     * @param type the desired type (not null)
     */
    public void setType(PropType type) {
        Validate.nonNull(type, "type");

        if (type != propType) {
            this.propType = type;
            this.initialOrientation.loadIdentity();

            float scaleFactor = 1f;
            float totalMass = 1f;
            Prop tmpProp = create(type, scaleFactor, totalMass);
            float descaledMass = tmpProp.defaultDescaledMass();
            totalMass = descaledMass * MyMath.cube(scaleFactor);
        }
    }

    /**
     * Determine the proposed total mass.
     *
     * @return the mass (in kilograms, &gt;0)
     */
    public float totalMass() {
        return totalMass;
    }

    /**
     * Determine the proposed type of Prop.
     *
     * @return an enum value (not null)
     */
    public PropType type() {
        assert propType != null;
        return propType;
    }
    // *************************************************************************
    // JmeCloneable methods

    /**
     * Don't use this method; use a {@link com.jme3.util.clone.Cloner} instead.
     *
     * @return never
     * @throws CloneNotSupportedException always
     */
    @Override
    public PropProposal clone() throws CloneNotSupportedException {
        super.clone();
        throw new CloneNotSupportedException("use a cloner");
    }

    /**
     * Callback from {@link com.jme3.util.clone.Cloner} to convert this
     * shallow-cloned proposal into a deep-cloned one, using the specified
     * Cloner and original to resolve copied fields.
     *
     * @param cloner the Cloner currently cloning this proposal
     * @param original the instance from which this proposal was shallow-cloned
     */
    @Override
    public void cloneFields(Cloner cloner, Object original) {
        this.initialOrientation = cloner.clone(initialOrientation);
        this.initialLocation = cloner.clone(initialLocation);
    }

    /**
     * Create a shallow clone for a JME cloner.
     *
     * @return a new instance
     */
    @Override
    public PropProposal jmeClone() {
        try {
            PropProposal clone = (PropProposal) super.clone();
            return clone;
        } catch (CloneNotSupportedException exception) {
            throw new RuntimeException(exception);
        }
    }
    // *************************************************************************
    // private methods

    @SuppressWarnings("unchecked")
    private static Prop create(
            PropType propType, float scaleFactor, float totalMass) {
        String className = "com.jayfella.jme.vehicle.examples.props."
                + propType.toString();
        try {
            Class<? extends Prop> clazz
                    = (Class<? extends Prop>) Class.forName(className);
            Constructor<? extends Prop> constructor
                    = clazz.getDeclaredConstructor(twoFloats);
            Prop result = constructor.newInstance(scaleFactor, totalMass);
            return result;

        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }
}
