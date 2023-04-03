package com.jayfella.jme.vehicle.gui.lemur;

import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.gui.AtmiState;
import com.jayfella.jme.vehicle.gui.GearNameState;
import com.jayfella.jme.vehicle.gui.SpeedometerState;
import com.jayfella.jme.vehicle.gui.SteeringWheelState;
import com.jayfella.jme.vehicle.gui.TachometerState;
import com.jayfella.jme.vehicle.input.DrivingInputMode;
import com.jayfella.jme.vehicle.input.SignalMode;
import com.jayfella.jme.vehicle.part.Engine;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import com.simsilica.lemur.event.MouseEventControl;
import java.util.logging.Logger;
import jme3utilities.MyAsset;
import jme3utilities.SignalTracker;
import jme3utilities.mesh.DiscMesh;

/**
 * Heads-up display (HUD) for driving a Vehicle. This AppState should be
 * instantiated once and then enabled/disabled as needed. It directly manages
 * portions of the graphical user interface, namely:
 * <ul>
 * <li>the exit button</li>
 * <li>the horn button</li>
 * <li>the power button</li>
 * </ul>
 * It indirectly manages:
 * <ul>
 * <li>the automatic-transmission mode indicator</li>
 * <li>the gear-name indicator</li>
 * <li>the speedometer</li>
 * <li>the steering-wheel indicator</li>
 * <li>the tachometer</li>
 * </ul>
 */
public class DriverHud extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * Z-coordinate for most GUI geometries
     */
    final private static float guiZ = 1f;
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(DriverHud.class.getName());
    // *************************************************************************
    // fields

    /**
     * Appstate to manage the automatic-transmission mode indicator
     */
    final private AtmiState atmiState = new AtmiState();
    /**
     * height of the GUI viewport (in pixels)
     */
    private float viewPortHeight;
    /**
     * width of the GUI viewport (in pixels)
     */
    private float viewPortWidth;

    private Geometry exitButton;
    private Geometry hornButton;
    private Geometry powerButton;
    /**
     * pre-loaded materials for iconic buttons
     */
    private Material exitMaterial;
    private Material hornSilentMaterial;
    private Material hornSoundMaterial;
    private Material powerOffMaterial;
    private Material powerOnMaterial;
    /**
     * app states to manage rotary indicators
     */
    private SpeedometerState speedometer;
    private SteeringWheelState steeringWheel;
    private TachometerState tachometer;
    /**
     * corresponding Vehicle, or null if none assigned
     */
    private Vehicle vehicle;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled HUD.
     */
    public DriverHud() {
        super("Driver HUD");
        super.setEnabled(false);
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Associate a Vehicle with this HUD prior to enabling it.
     *
     * @param vehicle the Vehicle to use (or null for none)
     */
    public void setVehicle(Vehicle vehicle) {
        assert !isEnabled();
        this.vehicle = vehicle;
        atmiState.setVehicle(vehicle);
    }

    /**
     * Display the horn button.
     *
     * @param sounding true &rarr; show it in the "sounding" state, false &rarr;
     * show it in the "silent" state
     */
    public void showHornButton(boolean sounding) {
        if (hornButton.getParent() == null) {
            attachToGui(hornButton);
        }

        if (sounding) {
            hornButton.setMaterial(hornSoundMaterial);
        } else {
            hornButton.setMaterial(hornSilentMaterial);
        }

        // Position the button in the viewport.
        float x = 0.5f * viewPortWidth;
        float y = 0.18f * viewPortHeight;
        hornButton.setLocalTranslation(x, y, guiZ);
    }

    /**
     * Toggle the engine between the started and stopped states.
     */
    public void toggleEngineStarted() {
        boolean wasStarted = vehicle.getEngine().isRunning();
        if (wasStarted) {
            vehicle.stopEngine();
            showPowerButton(false);
        } else {
            vehicle.startEngine();
            showPowerButton(true);
        }
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void cleanup(Application application) {
        AppStateManager stateManager = getApplication().getStateManager();
        stateManager.detach(atmiState);
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        Camera camera = application.getCamera();
        this.viewPortHeight = camera.getHeight();
        this.viewPortWidth = camera.getWidth();

        // pre-load unshaded materials for buttons
        AssetManager manager = application.getAssetManager();
        Texture texture = manager.loadTexture("Textures/sgold/exit.png");
        this.exitMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("/Textures/Georg/horn-silent.png");
        this.hornSilentMaterial
                = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("/Textures/Georg/horn-sound.png");
        this.hornSoundMaterial
                = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("/Textures/Georg/power-off.png");
        this.powerOffMaterial
                = MyAsset.createUnshadedMaterial(manager, texture);

        texture = manager.loadTexture("/Textures/Georg/power-on.png");
        this.powerOnMaterial = MyAsset.createUnshadedMaterial(manager, texture);

        AppStateManager stateManager = getApplication().getStateManager();
        stateManager.attach(atmiState);

        // Construct a Geometry for the horn button.
        float radius = 0.05f * viewPortHeight;
        int numVertices = 25;
        Mesh mesh = new DiscMesh(radius, numVertices);
        this.hornButton = new Geometry("horn button", mesh);

        // Add an Expander to toggle the horn sounding/silent.
        Expander listener = new Expander(hornButton) {
            @Override
            public void onClick(boolean isPressed) {
                SignalMode mode = getState(SignalMode.class);
                SignalTracker signalTracker = mode.getSignalTracker();
                String signalName = SignalMode.F_HORN1.getId();
                signalTracker.setActive(signalName, 999, isPressed);
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        hornButton.addControl(control);
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        atmiState.setEnabled(false);
        hideExitButton();
        hideGearName();
        hideHornButton();
        hidePowerButton();
        hideSpeedometer();
        hideSteeringWheel();
        hideTachometer();
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        atmiState.setEnabled(true);
        showExitButton();
        showGearName();

        boolean isHornRequested = vehicle.isHornRequested();
        showHornButton(isHornRequested);

        boolean isEngineRunning = vehicle.getEngine().isRunning();
        showPowerButton(isEngineRunning);

        SpeedUnit speedometerUnits = vehicle.getSpeedometerUnits();
        if (speedometerUnits != null) {
            showSpeedometer(speedometerUnits);
        }
        showSteeringWheel();
        showTachometer();
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);

        // Re-orient the horn button and the steering-wheel indicator.
        float angle = vehicle.steeringWheelAngle();
        Quaternion orientation = new Quaternion(); // TODO garbage
        orientation.fromAngles(0f, 0f, angle);
        hornButton.setLocalRotation(orientation);

        boolean isHornRequested = vehicle.isHornRequested();
        showHornButton(isHornRequested);
    }
    // *************************************************************************
    // private methods

    /**
     * Attach the specified Spatial to the GUI node.
     *
     * @param spatial (not null, alias created)
     */
    private void attachToGui(Spatial spatial) {
        SimpleApplication simpleApp = (SimpleApplication) getApplication();
        simpleApp.getGuiNode().attachChild(spatial);
    }

    /**
     * Hide the exit button.
     */
    private void hideExitButton() {
        if (exitButton != null) {
            exitButton.removeFromParent();
            this.exitButton = null;
        }
    }

    /**
     * Hide the gear-name indicator.
     */
    private void hideGearName() {
        GearNameState state = getState(GearNameState.class);
        state.setEnabled(false);
    }

    /**
     * Hide the horn button.
     */
    private void hideHornButton() {
        if (hornButton.getParent() != null) {
            hornButton.removeFromParent();
        }
    }

    /**
     * Hide the power button.
     */
    private void hidePowerButton() {
        if (powerButton != null) {
            powerButton.removeFromParent();
            this.powerButton = null;
        }
    }

    /**
     * Hide the steering wheel.
     */
    private void hideSteeringWheel() {
        if (steeringWheel != null) {
            getStateManager().detach(steeringWheel);
            this.steeringWheel = null;
        }
    }

    /**
     * Hide the speedometer.
     */
    private void hideSpeedometer() {
        if (speedometer != null) {
            getStateManager().detach(speedometer);
            this.speedometer = null;
        }
    }

    /**
     * Hide the tachometer.
     */
    private void hideTachometer() {
        if (tachometer != null) {
            getStateManager().detach(tachometer);
            this.tachometer = null;
        }
    }

    /**
     * Stop driving the Vehicle and return to the main menu.
     */
    private void returnToMainMenu() {
        getState(DrivingInputMode.class).returnToMainMenu();
    }

    /**
     * Display the exit button.
     */
    private void showExitButton() {
        hideExitButton();

        float radius = 0.035f * viewPortHeight;
        int numVertices = 25;
        Mesh mesh = new DiscMesh(radius, numVertices);
        this.exitButton = new Geometry("exit button", mesh);
        attachToGui(exitButton);

        exitButton.setMaterial(exitMaterial);

        // Position the button in the viewport.
        float x = 0.975f * viewPortWidth;
        float y = 0.955f * viewPortHeight;
        exitButton.setLocalTranslation(x, y, guiZ);

        // Add an Expander to return to the main menu.
        Expander listener = new Expander(exitButton) {
            @Override
            public void onClick(boolean isPressed) {
                if (isPressed) {
                    returnToMainMenu();
                }
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        exitButton.addControl(control);
    }

    /**
     * Show the gear-name indicator.
     */
    private void showGearName() {
        GearNameState state = getState(GearNameState.class);
        state.setVehicle(vehicle);
        state.setEnabled(true);
    }

    /**
     * Display the power button.
     *
     * @param on true &rarr; show it in the "on" state, false &rarr; show it in
     * the "off" state
     */
    private void showPowerButton(boolean on) {
        hidePowerButton();

        float radius = 0.035f * viewPortHeight;
        int numVertices = 25;
        Mesh mesh = new DiscMesh(radius, numVertices);
        this.powerButton = new Geometry("power button", mesh);
        attachToGui(powerButton);

        if (on) {
            powerButton.setMaterial(powerOnMaterial);
        } else {
            powerButton.setMaterial(powerOffMaterial);
        }

        // Position the button in the viewport.
        float x = 0.625f * viewPortWidth;
        float y = 0.07f * viewPortHeight;
        powerButton.setLocalTranslation(x, y, guiZ);

        // Add an Expander to toggle the engine on/off.
        Expander listener = new Expander(powerButton) {
            @Override
            public void onClick(boolean isPressed) {
                if (isPressed) {
                    toggleEngineStarted();
                }
            }
        };
        MouseEventControl control = new MouseEventControl(listener);
        powerButton.addControl(control);
    }

    /**
     * Display the speedometer using the specified speed unit.
     *
     * @param speedUnit (not null)
     */
    private void showSpeedometer(SpeedUnit speedUnit) {
        hideSpeedometer();

        this.speedometer = new SpeedometerState(vehicle, speedUnit);
        getStateManager().attach(speedometer);
    }

    /**
     * Display the steering wheel.
     */
    private void showSteeringWheel() {
        hideSteeringWheel();

        float radius = 120f; // pixels
        float x = 0.5f * viewPortWidth;
        float y = 0.18f * viewPortHeight;
        float z = guiZ - 0.1f; // behind the horn button
        Vector3f location = new Vector3f(x, y, z);
        this.steeringWheel = new SteeringWheelState(radius, location);
        getStateManager().attach(steeringWheel);

        steeringWheel.setVehicle(vehicle);
        steeringWheel.setEnabled(true);
    }

    /**
     * Display the tachometer.
     */
    private void showTachometer() {
        hideTachometer();

        Engine engine = vehicle.getEngine();
        this.tachometer = new TachometerState(engine);
        getStateManager().attach(tachometer);
    }
}
