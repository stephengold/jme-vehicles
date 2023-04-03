package com.jayfella.jme.vehicle.input;

import com.github.stephengold.garrett.CameraSignal;
import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.debug.DebugTabState;
import com.jayfella.jme.vehicle.debug.EnginePowerGraphState;
import com.jayfella.jme.vehicle.debug.TireDataState;
import com.jayfella.jme.vehicle.debug.VehicleEditorState;
import com.jayfella.jme.vehicle.gui.lemur.DriverHud;
import com.jayfella.jme.vehicle.gui.menu.MainMenu;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.jayfella.jme.vehicle.part.GearBox;
import com.jayfella.jme.vehicle.view.CameraController;
import com.jayfella.jme.vehicle.view.ChaseCamera;
import com.jayfella.jme.vehicle.view.ChaseOption;
import com.jayfella.jme.vehicle.view.DashCamera;
import com.jayfella.jme.vehicle.view.VehicleCamView;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputState;
import java.util.logging.Logger;
import jme3utilities.MyCamera;
import jme3utilities.SignalTracker;
import jme3utilities.math.noise.Generator;

/**
 * An InputMode that's enabled while the user is driving a Vehicle. It uses 2
 * camera modes: one for the dash camera and one for the chase camera.
 */
public class DrivingInputMode extends InputMode {
    // *************************************************************************
    // constants and loggers

    /**
     * input functions handled by this mode
     */
    final public static FunctionId F_FORWARD
            = new FunctionId("Vehicle Move");
    final public static FunctionId F_MAIN_BRAKE
            = new FunctionId("Vehicle Footbrake");
    final public static FunctionId F_PARKING_BRAKE
            = new FunctionId("Vehicle Parking Brake");
    final public static FunctionId F_RESET
            = new FunctionId("Vehicle Reset");
    final public static FunctionId F_RETURN
            = new FunctionId("Return to Main Menu");
    final public static FunctionId F_REVERSE
            = new FunctionId("Vehicle reverse");
    final public static FunctionId F_START_ENGINE
            = new FunctionId("Vehicle Start Engine");
    final public static FunctionId F_TURN_LEFT
            = new FunctionId("Vehicle Turn Left");
    final public static FunctionId F_TURN_RIGHT
            = new FunctionId("Vehicle Turn Right");
    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(DrivingInputMode.class.getName());
    // *************************************************************************
    // fields

    final private float maxSteeringAngle = 1f; // radians
    final private float returnRate = 2f; // radians per second
    final private float turnRate = 0.5f; // radians per second

    private boolean accelerating;
    private boolean mainBrake;
    private boolean parkingBrake;
    private boolean turningLeft;
    private boolean turningRight;

    private float steeringAngle = 0f;
    /**
     * generate pseudo-random offsets for resetVehicle()
     */
    final private static Generator generator = new Generator();

    private VehicleCamView cameraMode = VehicleCamView.ChaseCam;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled InputMode to drive the selected Vehicle.
     */
    public DrivingInputMode() {
        super("Driving Mode", F_FORWARD, F_MAIN_BRAKE, F_PARKING_BRAKE, F_RESET,
                F_RETURN, F_REVERSE, F_START_ENGINE, F_TURN_LEFT, F_TURN_RIGHT);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                accelerating = true;
            } else {
                accelerating = false;
            }
        }, F_FORWARD);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                mainBrake = true;
            } else {
                mainBrake = false;
            }
        }, F_MAIN_BRAKE);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                parkingBrake = true;
            } else {
                parkingBrake = false;
            }
        }, F_PARKING_BRAKE);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                resetVehicle();
            }
        }, F_RESET);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                returnToMainMenu();
            }
        }, F_RETURN);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            GearBox gearBox = MavDemo1.getVehicle().getGearBox();
            if (inputState == InputState.Positive) {
                boolean isInReverse = gearBox.isInReverse();
                gearBox.setReversing(!isInReverse);
            }
        }, F_REVERSE);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                getState(DriverHud.class).toggleEngineStarted();
            }
        }, F_START_ENGINE);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                turningLeft = true;
            } else {
                turningLeft = false;
            }
        }, F_TURN_LEFT);

        assign((FunctionId function, InputState inputState, double tpf) -> {
            if (inputState == InputState.Positive) {
                turningRight = true;
            } else {
                turningRight = false;
            }
        }, F_TURN_RIGHT);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Advance to the next camera mode.
     */
    public void nextCameraMode() {
        cameraMode = cameraMode.next();
        setCameraControlMode(cameraMode);
    }

    /**
     * Stop driving, reload the vehicle, and return to the main menu.
     */
    public void returnToMainMenu() {
        AppStateManager stateManager = getStateManager();
        setEnabled(false);

        EnginePowerGraphState enginePowerGraphState
                = getState(EnginePowerGraphState.class);
        stateManager.detach(enginePowerGraphState);

        TireDataState tireDataState = getState(TireDataState.class);
        stateManager.detach(tireDataState);

        VehicleEditorState vehicleEditorState
                = getState(VehicleEditorState.class);
        stateManager.detach(vehicleEditorState);

        DebugTabState debugTabState = getState(DebugTabState.class);
        stateManager.detach(debugTabState);

        getState(DriverHud.class).setEnabled(false);

        Vehicle vehicle = MavDemo1.getVehicle();
        Vehicle newVehicle;
        try {
            Class<? extends Vehicle> clazz = vehicle.getClass();
            newVehicle = clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
        MavDemo1 main = MavDemo1.getApplication();
        AssetManager assetManager = main.getAssetManager();
        newVehicle.load(assetManager);
        MavDemo1.setVehicle(newVehicle);

        stateManager.attach(new MainMenu());
        getState(CameraInputMode.class).orbit();
    }
    // *************************************************************************
    // InputMode methods

    /**
     * Callback invoked whenever this InputMode becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        super.onEnable();
        setCameraControlMode(cameraMode);
    }

    /**
     * Callback to update this InputMode, invoked once per frame when the mode
     * is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);

        updateTurn(tpf);
        updateBrakeAndAccelerate();

        SignalMode signalMode = getState(SignalMode.class);
        SignalTracker signalTracker = signalMode.getSignalTracker();
        boolean requested = signalTracker.test(SignalMode.F_HORN1.getId());
        MavDemo1.getVehicle().setHornStatus(requested);
    }
    // *************************************************************************
    // private methods

    /**
     * Re-orient the Vehicle so that it is upright, reset its motion to zero,
     * and attempt to locate it someplace it won't immediately collide.
     */
    private static void resetVehicle() {
        Vehicle vehicle = MavDemo1.getVehicle();
        VehicleControl engineBody = vehicle.getVehicleControl();

        float[] angles = new float[3];
        engineBody.getPhysicsRotation().toAngles(angles);
        Vector3f location = engineBody.getPhysicsLocation();

        vehicle.removeFromPhysicsSpace();
        vehicle.warpAllBodies(location, angles[1]);
        if (vehicle.contactTest()) {
            Vector3f newLocation = location.add(0f, 1f, 0f);
            for (int iteration = 0; iteration < 9; ++iteration) {
                vehicle.warpAllBodies(newLocation, angles[1]);
                if (vehicle.contactTest()) {
                    break;
                }
                Vector3f offset = generator.nextVector3f();
                offset.multLocal(0.1f * (iteration + 1));
                newLocation.addLocal(offset);
            }
        }
        vehicle.addToPhysicsSpace();
    }

    private void setCameraControlMode(VehicleCamView controlMode) {
        Camera camera = getApplication().getCamera();
        MyCamera.setYTangent(camera, 1f);

        SignalMode signalMode = getState(SignalMode.class);
        SignalTracker signalTracker = signalMode.getSignalTracker();
        Vehicle vehicle = MavDemo1.getVehicle();

        CameraController activeCamera;
        switch (controlMode) {
            case ChaseCam:
                float rearBias = 1f;
                ChaseCamera chaseCamera
                        = new ChaseCamera(ChaseOption.StrictFollow, rearBias);
                activeCamera = chaseCamera;
                chaseCamera.setVehicle(vehicle);
                for (CameraSignal function : CameraSignal.values()) {
                    String signalName = function.toString();
                    chaseCamera.setSignalName(function, signalName);
                }
                break;

            case DashCam:
                DashCamera dashCamera
                        = new DashCamera(vehicle, camera, signalTracker);
                activeCamera = dashCamera;
                dashCamera.setSignalName(
                        CameraSignal.ZoomIn, CameraSignal.ZoomIn.toString());
                dashCamera.setSignalName(
                        CameraSignal.ZoomOut, CameraSignal.ZoomOut.toString());
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown camera-control mode: " + controlMode);
        }

        getState(CameraInputMode.class).setActiveCamera(activeCamera);
    }

    private void updateBrakeAndAccelerate() {
        Vehicle vehicle = MavDemo1.getVehicle();

        // Update the brake control signals.
        float main = mainBrake ? 1f : 0f;
        float parking = parkingBrake ? 1f : 0f;
        vehicle.setBrakeSignals(main, parking);

        // Update the "accelerate" control signal.
        float acceleration = 0f;
        boolean isEngineRunning = vehicle.getEngine().isRunning();
        if (isEngineRunning && accelerating) {
            float kph = vehicle.forwardSpeed(SpeedUnit.KPH);
            GearBox gearBox = vehicle.getGearBox();
            if (gearBox.isInReverse()) {
                float maxKph = gearBox.maxReverseSpeed(SpeedUnit.KPH);
                if (kph > maxKph) {
                    acceleration = 1f;
                }
            } else {
                float maxKph = gearBox.maxForwardSpeed(SpeedUnit.KPH);
                if (kph < maxKph) {
                    acceleration = 1f;
                }
            }
        }
        vehicle.setAccelerateSignal(acceleration);
    }

    private void updateTurn(float tpf) {
        if (turningLeft && !turningRight && steeringAngle >= 0f) {
            // turn more to the left
            this.steeringAngle += turnRate * tpf;
            this.steeringAngle = Math.min(steeringAngle, maxSteeringAngle);

        } else if (turningRight && !turningLeft && steeringAngle <= 0f) {
            // turn more to the right
            this.steeringAngle -= turnRate * tpf;
            this.steeringAngle = Math.max(steeringAngle, -maxSteeringAngle);

        } else if (steeringAngle > 0f) {
            // return from turning left
            this.steeringAngle -= returnRate * tpf;
            this.steeringAngle = Math.max(steeringAngle, 0f);

        } else if (steeringAngle < 0f) {
            // return from turning right
            this.steeringAngle += returnRate * tpf;
            this.steeringAngle = Math.min(steeringAngle, 0f);
        }

        MavDemo1.getVehicle().steer(steeringAngle);
    }
}
