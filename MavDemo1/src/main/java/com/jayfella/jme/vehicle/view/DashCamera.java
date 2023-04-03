package com.jayfella.jme.vehicle.view;

import com.github.stephengold.garrett.CameraSignal;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.SignalTracker;
import jme3utilities.Validate;

/**
 * A CameraController to control a forward-facing Camera attached to a Vehicle.
 */
final public class DashCamera
        extends CameraController
        implements AnalogListener {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DashCamera.class.getName());
    // *************************************************************************
    // fields

    final private CameraNode cameraNode;
    /**
     * accumulated analog zoom amount since the last update (in clicks)
     */
    private float zoomAnalogSum = 0f;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a controller attached to the specified Vehicle.
     *
     * @param vehicle the Vehicle to attach to (not null, alias created)
     * @param camera the Camera to control (not null, alias created)
     * @param tracker the status tracker for named signals (not null, alias
     * created)
     */
    public DashCamera(Vehicle vehicle, Camera camera, SignalTracker tracker) {
        super(vehicle, camera, tracker);
        this.cameraNode = new CameraNode("Dash Camera Node", camera);
    }
    // *************************************************************************
    // AnalogListener methods

    /**
     * Callback to receive an analog input event.
     *
     * @param eventName the name of the input event (not null, not empty)
     * @param reading the input reading (&ge;0)
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAnalog(String eventName, float reading, float tpf) {
        Validate.nonEmpty(eventName, "event name");
        Validate.nonNegative(reading, "reading");
        Validate.nonNegative(tpf, "time per frame");

        switch (eventName) {
            case analogZoomIn:
                this.zoomAnalogSum += reading;
                break;

            case analogZoomOut:
                this.zoomAnalogSum -= reading;
                break;

            default:
                throw new IllegalArgumentException(eventName);
        }
    }
    // *************************************************************************
    // VehicleCamera methods

    @Override
    public void attach() {
        Vector3f offset = new Vector3f();
        vehicle.locateDashCam(offset);
        cameraNode.setLocalTranslation(offset);
        Node controlled = (Node) vehicle.getVehicleControl().getSpatial();
        controlled.attachChild(cameraNode);
        enable();
    }

    @Override
    public void detach() {
        cameraNode.removeFromParent();
        disable();
    }

    /**
     * Callback to update this state prior to rendering. (Invoked once per frame
     * while the state is attached and enabled.)
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        // Sum the discrete inputs (signals).
        int zoomSignalDirection = 0;
        for (CameraSignal function : CameraSignal.values()) {
            if (isActive(function)) {
                switch (function) {
                    case Back:
                    case DragToOrbit:
                    case Forward:
                    case OrbitDown:
                    case OrbitUp:
                    case Xray:
                        // do nothing
                        break;

                    case ZoomIn:
                        ++zoomSignalDirection;
                        break;

                    case ZoomOut:
                        --zoomSignalDirection;
                        break;

                    default:
                        throw new RuntimeException(function.toString());
                }
            }
        }
        /*
         * Apply focal zoom, if any:
         * first the discrete signals and then the analog values.
         */
        if (zoomSignalDirection != 0) {
            float zoomFactor = FastMath.exp(zoomSignalDirection * tpf);
            magnify(zoomFactor);
        }
        if (zoomAnalogSum != 0f) {
            float zoomFactor = FastMath.exp(zoomMultiplier * zoomAnalogSum);
            magnify(zoomFactor);
            this.zoomAnalogSum = 0f;
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Disable this camera controller. Assumes it is initialized and enabled.
     */
    private void disable() {
        // Configure the analog inputs.
        InputManager inputManager = MavDemo1.getApplication().getInputManager();
        inputManager.deleteMapping(analogZoomIn);
        inputManager.deleteMapping(analogZoomOut);
        inputManager.removeListener(this);

        inputManager.setCursorVisible(true);
    }

    /**
     * Enable this camera controller. Assumes it is initialized and disabled.
     */
    private void enable() {
        camera.setName("dash camera");
        float yDegrees;
        if (camera.isParallelProjection()) {
            // Configure perspective.
            yDegrees = 30f;
            float aspectRatio = MyCamera.viewAspectRatio(camera);
            float near = 0.1f;
            float far = 3300f;
            camera.setFrustumPerspective(yDegrees, aspectRatio, near, far);
        } else {
            MyCamera.setNearFar(camera, 0.1f, 3300f);
        }

        // Configure the analog inputs.
        InputManager inputManager = MavDemo1.getApplication().getInputManager();
        inputManager.addMapping(analogZoomIn,
                new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(analogZoomOut,
                new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        inputManager.addListener(this, analogZoomIn, analogZoomOut);
    }
}
