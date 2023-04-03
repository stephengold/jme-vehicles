package com.jayfella.jme.vehicle.tire;

/**
 * Derived from the TyreSettings class in the Advanced Vehicles project.
 */
public class TireSettings {

    // LATERAL: default settings
    // public static float DEFAULT_COEFF_C = 1.3f;
    // public static float DEFAULT_COEFF_B = 15.2f;
    // public static float DEFAULT_COEFF_E = -1.6f;
    // public static float DEFAULT_COEFF_KA = 2.0f;
    // public static float DEFAULT_COEFF_KB = 0.000055f;
    // a listener for changes, so we can re-draw the graph:
    private ChangeListener changeListener;

    /**
     * coefficient C for the normalized slip-angle curve
     */
    private float slipAngleCoefficientC;
    /**
     * coefficient B for the normalized slip-angle curve
     */
    private float slipAngleCoefficientB;
    /**
     * coefficient E for the normalized slip-angle curve
     */
    private float slipAngleCoefficientE;
    private float loadCoefficientKA; // coefficient KA for the load function.
    private float loadCoefficientKB; // coefficient KB for the load function.

    /**
     * Creates a Race Car Tire Model Object with the given curve coefficients.
     * Use these example coefficient values for basic tire to start with:<br>
     * C = 1.3<br>
     * B = 15.2<br>
     * E = -1.6<br>
     * KA = 2.0<br>
     * KB = 0.000055
     *
     * @param slipAngleCoefficientC - coefficient C in the normalised slip angle
     * curve function f1.
     * @param slipAngleCoefficientB - coefficient B in the normalised slip angle
     * curve function f1.
     * @param slipAngleCoefficientE - coefficient E in the normalised slip angle
     * curve function f1.
     * @param loadCoefficientKA - coefficient KA in the load curve function f2.
     * @param loadCoefficientKB - coefficient KB in the load curve function f2.
     */
    public TireSettings(float slipAngleCoefficientC,
            float slipAngleCoefficientB, float slipAngleCoefficientE,
            float loadCoefficientKA, float loadCoefficientKB) {
        this.slipAngleCoefficientC = slipAngleCoefficientC;
        this.slipAngleCoefficientB = slipAngleCoefficientB;
        this.slipAngleCoefficientE = slipAngleCoefficientE;
        this.loadCoefficientKA = loadCoefficientKA;
        this.loadCoefficientKB = loadCoefficientKB;
    }

    /**
     * Return the "C" coefficient of the normalized slip-angle curve.
     *
     * @return the coefficient value (units?)
     */
    public float getSlipAngleCoefficientC() {
        return slipAngleCoefficientC;
    }

    /**
     * Alter the "C" coefficient of the normalized slip-angle curve.
     *
     * @param slipAngleCoefficientC the desired coefficient value (units?)
     */
    public void setSlipAngleCoefficientC(float slipAngleCoefficientC) {
        this.slipAngleCoefficientC = slipAngleCoefficientC;
        changeListener.valueChanged();
    }

    /**
     * Return the "B" coefficient of the normalized slip-angle curve.
     *
     * @return the coefficient value (units?)
     */
    public float getSlipAngleCoefficientB() {
        return slipAngleCoefficientB;
    }

    /**
     * Alter the "B" coefficient of the normalized slip-angle curve.
     *
     * @param slipAngleCoefficientB the desired coefficient value (units?)
     */
    public void setSlipAngleCoefficientB(float slipAngleCoefficientB) {
        this.slipAngleCoefficientB = slipAngleCoefficientB;
        changeListener.valueChanged();
    }

    /**
     * Return the "E" coefficient of the normalized slip-angle curve.
     *
     * @return the coefficient value (units?)
     */
    public float getSlipAngleCoefficientE() {
        return slipAngleCoefficientE;
    }

    /**
     * Alter the "E" coefficient of the normalized slip-angle curve.
     *
     * @param slipAngleCoefficientE the desired coefficient value (units?)
     */
    public void setSlipAngleCoefficientE(float slipAngleCoefficientE) {
        this.slipAngleCoefficientE = slipAngleCoefficientE;
        changeListener.valueChanged();
    }

    /**
     * Return the "KA" coefficient used to estimate force based on load.
     *
     * @return the coefficient value (units?)
     */
    public float getLoadCoefficientKA() {
        return loadCoefficientKA;
    }

    /**
     * Alter the "KA" coefficient used to estimate force based on load.
     *
     * @param loadCoefficientKA the desired coefficient value (units?)
     */
    public void setLoadCoefficientKA(float loadCoefficientKA) {
        this.loadCoefficientKA = loadCoefficientKA;
        changeListener.valueChanged();
    }

    /**
     * Return the "KB" coefficient used to estimate force based on load.
     *
     * @return the coefficient value (units?)
     */
    public float getLoadCoefficientKB() {
        return loadCoefficientKB;
    }

    /**
     * Alter the "KB" coefficient used to estimate force based on load.
     *
     * @param loadCoefficientKB the desired coefficient value (units?)
     */
    public void setLoadCoefficientKB(float loadCoefficientKB) {
        this.loadCoefficientKB = loadCoefficientKB;
        changeListener.valueChanged();
    }

    /**
     * Access the assigned ChangeListener.
     *
     * @return the pre-existing object, or null if none assigned
     */
    public ChangeListener getChangeListener() {
        return changeListener;
    }

    /**
     * Assign the specified ChangeListener. This cancels any listener previously
     * assigned.
     *
     * @param changeListener the desired listener (alias created) or null for
     * none
     */
    public void setChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    /**
     * Listen for changes to a curve.
     */
    abstract public static class ChangeListener {
        /**
         * A no-arg constructor to avoid javadoc warnings from JDK 18.
         */
        protected ChangeListener() {
            // do nothing
        }

        /**
         * Callback invoked after every coefficient change.
         */
        abstract public void valueChanged();
    }
}
