package com.jayfella.jme.vehicle.view;

import com.github.stephengold.garrett.CameraSignal;
import com.jayfella.jme.vehicle.Vehicle;
import com.jme3.math.FastMath;
import com.jme3.renderer.Camera;
import java.util.EnumMap;
import jme3utilities.MyCamera;
import jme3utilities.SignalTracker;
import jme3utilities.Validate;

/**
 * Abstract camera controller for More Advanced Vehicles.
 */
abstract public class CameraController {
    // *************************************************************************
    // constants and loggers

    /**
     * frustum's Y tangent ratio at lowest magnification (&gt;minYTangent)
     */
    final protected static float maxYTangent = 2f;
    /**
     * frustum's Y tangent ratio at highest magnification (&gt;0)
     */
    final protected static float minYTangent = 0.01f;
    /**
     * analog zoom input multiplier (in log units per click)
     */
    final protected static float zoomMultiplier = 0.3f;
    /**
     * names of analog events
     */
    final protected static String analogZoomIn = "zoom in";
    final protected static String analogZoomOut = "zoom out";
    // *************************************************************************
    // fields

    /**
     * Camera being controlled (not null)
     */
    final protected Camera camera;
    /**
     * map functions to signal names
     */
    final protected EnumMap<CameraSignal, String> signalNames
            = new EnumMap<>(CameraSignal.class);
    /**
     * status of named signals
     */
    final protected SignalTracker signalTracker;
    /**
     * Vehicle with which the Camera is associated
     */
    protected Vehicle vehicle;
    // *************************************************************************
    // constructor

    protected CameraController(
            Vehicle vehicle, Camera camera, SignalTracker tracker) {
        Validate.nonNull(vehicle, "vehicle");

        this.vehicle = vehicle;
        this.camera = camera;
        signalTracker = tracker;
    }
    // *************************************************************************
    // new methods exposed

    abstract public void attach();

    abstract public void detach();

    /**
     * Alter which signal is assigned to the specified function.
     *
     * @param function which function to alter (not null)
     * @param signalName the desired signal name (may be null)
     */
    public void setSignalName(CameraSignal function, String signalName) {
        Validate.nonNull(function, "function");
        signalNames.put(function, signalName);
    }

    /**
     * Alter which Vehicle is associated with this camera.
     *
     * @param newVehicle the desired target vehicle (not null)
     */
    public void setVehicle(Vehicle newVehicle) {
        Validate.nonNull(newVehicle, "new vehicle");
        vehicle = newVehicle;
    }

    /**
     * Callback to update this controller, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    abstract public void update(float tpf);
    // *************************************************************************
    // new protected methods

    /**
     * Test whether the specified camera function (signal) is active.
     *
     * @param function the function to test (not null)
     * @return true if active, otherwise false
     */
    protected boolean isActive(CameraSignal function) {
        String signalName = signalNames.get(function);
        if (signalName != null && signalTracker.test(signalName)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Magnify the view by the specified factor.
     *
     * @param factor the factor to increase magnification (&gt;0)
     */
    protected void magnify(float factor) {
        assert factor > 0f : factor;

        float frustumYTangent = MyCamera.yTangent(camera);
        frustumYTangent /= factor;
        frustumYTangent
                = FastMath.clamp(frustumYTangent, minYTangent, maxYTangent);
        MyCamera.setYTangent(camera, frustumYTangent);
    }
}
