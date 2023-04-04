package com.jayfella.jme.vehicle.tire;

import com.jme3.math.FastMath;
import jme3utilities.math.MyMath;

/**
 * Derived from the PajeckaTireModel class in the Advanced Vehicles project.
 */
// https://github.com/chrisoco/M120/blob/master/RaceCar/RCAS/src/rcas/model/MagicFormulaTireModel.java
public class PacejkaTireModel {

    private TireSettings.ChangeListener changeListener;

    private String name;

    private TireSettings lateral;
    private TireSettings longitudinal;
    private TireSettings alignMoment;

    // the maximum load the tire can handle
    private float maxLoad;
    // the amount of load on the tire
    private float load;

    private float lateralValue;
    private float longitudinalValue;
    private float momentValue;

    // friction circle result.
    // a method of combining Fx and Fy together.
    private float frictionCircle;

    /**
     * Instantiate a curve with the specified name and parameters.
     *
     * @param name name for curve
     * @param lateral TireSettings for the lateral component
     * @param longitudinal TireSettings for the longitudinal component
     * @param alignMoment TireSettings for the align-moment component
     * @param maxLoad maximum load the tire can handle (in Newtons)
     */
    public PacejkaTireModel(String name, TireSettings lateral,
            TireSettings longitudinal, TireSettings alignMoment,
            float maxLoad) {
        this.name = name;
        this.lateral = lateral;
        this.longitudinal = longitudinal;
        this.alignMoment = alignMoment;
        this.maxLoad = maxLoad;
    }

    /**
     * Return the name of this curve.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Alter the name of this curve.
     *
     * @param name the desired name (default=null)
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Access the TireSettings for the lateral component.
     *
     * @return the pre-existing instance
     */
    public TireSettings getLateral() {
        return lateral;
    }

    /**
     * Replace the TireSettings for the lateral component.
     *
     * @param lateral the settings to use (alias created)
     */
    public void setLateral(TireSettings lateral) {
        this.lateral = lateral;
    }

    /**
     * Access the TireSettings for the longitudinal component.
     *
     * @return the pre-existing instance
     */
    public TireSettings getLongitudinal() {
        return longitudinal;
    }

    /**
     * Replace the TireSettings for the longitudinal component.
     *
     * @param longitudinal the settings to use (alias created)
     */
    public void setLongitudinal(TireSettings longitudinal) {
        this.longitudinal = longitudinal;
    }

    /**
     * Access the TireSettings for the align-moment component.
     *
     * @return the pre-existing instance
     */
    public TireSettings getAlignMoment() {
        return alignMoment;
    }

    /**
     * Replace the TireSettings for the align-moment component.
     *
     * @param alignMoment the settings to use (alias created)
     */
    public void setAlignMoment(TireSettings alignMoment) {
        this.alignMoment = alignMoment;
    }

    /**
     * Return the load limit.
     *
     * @return the maximum load the tire can handle (in Newtons)
     */
    public float getMaxLoad() {
        return maxLoad;
    }

    /**
     * Alter the maximum load the tire can handle.
     *
     * @param maxLoad the desired limit value (in Newtons, default=0)
     */
    public void setMaxLoad(float maxLoad) {
        this.maxLoad = maxLoad;
        this.changeListener.valueChanged();
    }

    // slipAngle is in RADIANS
    private static float calcSlipAngleFactor(
            float slipAngle, TireSettings settings) {
        // float x = slipAngle * FastMath.DEG_TO_RAD;
        // float x = slipAngle;

        float b = settings.getSlipAngleCoefficientB();
        float bsa = b * slipAngle;
        float c = settings.getSlipAngleCoefficientC();
        float e = settings.getSlipAngleCoefficientE();
        float angle = c * FastMath.atan(bsa - e * (bsa - FastMath.atan(bsa)));
        return FastMath.sin(angle);
    }

    private static float calcLoadForce(float load, TireSettings settings) {
        float ka = settings.getLoadCoefficientKA();
        float kb = settings.getLoadCoefficientKB();
        return ka * (1 - kb * load) * load;
    }

    /**
     * Calculates the lateral cornering force for this tire in N.<br>
     * <br>
     * <b>CAUTION:</b> this function returns a value in Newtons (N)!
     *
     * @param slipAngle - the slip angle in degrees (°).
     *
     * @return - lateral tire force in N.
     */
    public float calcLateralTireForce(float slipAngle) {
        this.lateralValue = calcSlipAngleFactor(slipAngle, lateral)
                * calcLoadForce(load, lateral);
        return lateralValue;
    }

    /**
     * Estimate the longitudinal force for the specified slip angle, using the
     * Pacejka "Magic Formula".
     *
     * @param slipAngle the slip angle (in radians)
     * @return the estimated force (in Newtons)
     */
    public float calcLongitudeTireForce(float slipAngle) {
        this.longitudinalValue = calcSlipAngleFactor(slipAngle, longitudinal)
                * calcLoadForce(load, longitudinal);
        return longitudinalValue;
    }

    /**
     * Estimate the align-moment force for the specified slip angle, using the
     * Pacejka "Magic Formula".
     *
     * @param slipAngle the slip angle (in radians)
     * @return the estimated force (in Newtons)
     */
    public float calcAlignMoment(float slipAngle) {
        this.momentValue = calcSlipAngleFactor(slipAngle, alignMoment)
                * calcLoadForce(load, alignMoment);
        return momentValue;
    }

    /**
     * Estimate the reduced lateral force using a circle function.
     *
     * @return the estimated force (in Newtons)
     */
    public float calculateFrictionCircle() {
        /*
            A simple method to combine Fx and Fy (when both are non-zero)
            is an elliptical approach:

            Calculate Fx and Fy separately (as usual)
            Cut down Fy so that the vector (Fx,Fy) doesn't exceed
            the maximum magnitude:

            Fy = Fy0 * sqrt(1-(Fx/Fx0)^2)

            where:
                - Fy is the combined slip lateral force
                - Fy0 is the lateral force
                    as calculated using the normal Fy formula
                - Fx is the longitudinal force
                    as calculated using the normal Fx formula
                - Fx0 is the MAXIMUM longitudinal force possible
                    calculated as D+Sv in the Pacejka Fx formula

            This method favors longitudinal forces over lateral ones
            (cuts down the lateral force and leaves Fx intact).
         */
        this.frictionCircle
                = lateralValue * MyMath.circle(longitudinalValue / 7800f);
        this.frictionCircle = Math.max(0.1f, frictionCircle);

        return frictionCircle;
    }

    /**
     * Return the most recent estimate for the lateral force.
     *
     * @return the estimated force (in Newtons)
     */
    public float getLateralValue() {
        return lateralValue;
    }

    /**
     * Return the most recent estimate for the longitudinal force.
     *
     * @return the estimated force (in Newtons)
     */
    public float getLongitudinalValue() {
        return longitudinalValue;
    }

    /**
     * Return the most recent estimate for the align-moment force.
     *
     * @return the estimated force (in Newtons)
     */
    public float getMomentValue() {
        return momentValue;
    }

    /**
     * Return the most recent estimate for the reduced lateral force.
     *
     * @return the estimated force (in Newtons)
     */
    public float getFrictionCircle() {
        return frictionCircle;
    }

    /**
     * Represent this instance as a String.
     *
     * @return a descriptive string of text (not null, not empty)
     */
    @Override
    public String toString() {
        String format
                = "%s: \"%s\" : %s (C=%.2f, B=%.2f, E=%.2f, KA=%.2f, KB=%.6f)";

        String lat = String.format(
                format, getClass().toString(), name, "Lateral",
                lateral.getSlipAngleCoefficientC(),
                lateral.getSlipAngleCoefficientB(),
                lateral.getSlipAngleCoefficientE(),
                lateral.getLoadCoefficientKA(),
                lateral.getLoadCoefficientKB());

        String lng = String.format(
                format, getClass(), name, "Longitudinal",
                longitudinal.getSlipAngleCoefficientC(),
                longitudinal.getSlipAngleCoefficientB(),
                longitudinal.getSlipAngleCoefficientE(),
                longitudinal.getLoadCoefficientKA(),
                longitudinal.getLoadCoefficientKB());

        String mnt = String.format(
                format, getClass(), name, "Align Moment",
                alignMoment.getSlipAngleCoefficientC(),
                alignMoment.getSlipAngleCoefficientB(),
                alignMoment.getSlipAngleCoefficientE(),
                alignMoment.getLoadCoefficientKA(),
                alignMoment.getLoadCoefficientKB());

        String nl = System.lineSeparator();
        return lat + nl + lng + nl + mnt;
    }

    /**
     * Access the assigned ChangeListener.
     *
     * @return the pre-existing object, or null if none assigned
     */
    public TireSettings.ChangeListener getChangeListener() {
        return changeListener;
    }

    /**
     * Assign the specified ChangeListener. This cancels any listener previously
     * assigned.
     *
     * @param changeListener the desired listener (alias created) or null for
     * none
     */
    public void setChangeListener(TireSettings.ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    /**
     * Return the amount of load on the tire.
     *
     * @return the load force (in Newtons)
     */
    public float getLoad() {
        return load;
    }

    /**
     * Alter the amount of load on the tire.
     *
     * @param load the desired load force (default=0, in Newtons)
     */
    public void setLoad(float load) {
        this.load = load;
        if (changeListener != null) {
            changeListener.valueChanged();
        }
    }
}
