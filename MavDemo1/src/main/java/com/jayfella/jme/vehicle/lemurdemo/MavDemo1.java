package com.jayfella.jme.vehicle.lemurdemo;

import com.atr.jme.font.asset.TrueTypeLoader;
import com.github.stephengold.garrett.CameraSignal;
import com.github.stephengold.garrett.OrbitCamera;
import com.github.stephengold.jmepower.JmePowerVersion;
import com.jayfella.jme.vehicle.ChunkManager;
import com.jayfella.jme.vehicle.GlobalAudio;
import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.World;
import com.jayfella.jme.vehicle.examples.skies.AnimatedNightSky;
import com.jayfella.jme.vehicle.examples.skies.QuarrySky;
import com.jayfella.jme.vehicle.examples.vehicles.ClassicMotorcycle;
import com.jayfella.jme.vehicle.examples.vehicles.DuneBuggy;
import com.jayfella.jme.vehicle.examples.vehicles.GrandTourer;
import com.jayfella.jme.vehicle.examples.vehicles.HatchBack;
import com.jayfella.jme.vehicle.examples.vehicles.HoverTank;
import com.jayfella.jme.vehicle.examples.vehicles.Nismo;
import com.jayfella.jme.vehicle.examples.vehicles.PickupTruck;
import com.jayfella.jme.vehicle.examples.vehicles.Rotator;
import com.jayfella.jme.vehicle.examples.worlds.Mountains;
import com.jayfella.jme.vehicle.examples.worlds.Playground;
import com.jayfella.jme.vehicle.examples.worlds.Racetrack;
import com.jayfella.jme.vehicle.gui.CompassState;
import com.jayfella.jme.vehicle.gui.GearNameState;
import com.jayfella.jme.vehicle.gui.VehiclePointsState;
import com.jayfella.jme.vehicle.gui.lemur.AudioHud;
import com.jayfella.jme.vehicle.gui.lemur.CameraNameState;
import com.jayfella.jme.vehicle.gui.lemur.DriverHud;
import com.jayfella.jme.vehicle.gui.lemur.PhysicsHud;
import com.jayfella.jme.vehicle.gui.menu.MainMenu;
import com.jayfella.jme.vehicle.input.CameraInputMode;
import com.jayfella.jme.vehicle.input.DrivingInputMode;
import com.jayfella.jme.vehicle.input.DumpMode;
import com.jayfella.jme.vehicle.input.InputMode;
import com.jayfella.jme.vehicle.input.PhysicsMode;
import com.jayfella.jme.vehicle.input.ScreenshotMode;
import com.jayfella.jme.vehicle.input.SignalMode;
import com.jme3.app.DetailedProfilerState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.ConstantVerifierState;
import com.jme3.audio.AudioListenerState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.util.NativeLibrary;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickConnectionListener;
import com.jme3.input.KeyInput;
import com.jme3.material.Material;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeVersion;
import com.simsilica.lemur.input.Button;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.Loadable;
import jme3utilities.MyCamera;
import jme3utilities.MyString;
import jme3utilities.SignalTracker;
import jme3utilities.minie.FilterAll;
import jme3utilities.minie.MinieVersion;

/**
 * A simple application with a Lemur GUI to demonstrate the MaVehicles library.
 * The application's main entry point is in this class.
 *
 * Derived from the Main class in the Advanced Vehicles project.
 */
final public class MavDemo1 extends SimpleApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(MavDemo1.class.getName());
    // *************************************************************************
    // fields

    /**
     * enumerate preload tasks
     */
    private static Loadable[] preloads = new Loadable[]{
        new AnimatedNightSky(),
        new CameraNameState(),
        new ClassicMotorcycle(),
        new DuneBuggy(),
        new GrandTourer(),
        new HatchBack(),
        new HoverTank(),
        new Mountains(),
        new Nismo(),
        new Playground(),
        new PickupTruck(),
        new QuarrySky(),
        new Racetrack(),
        new Rotator(),
        new VehiclePointsState()
    };
    /**
     * application instance
     */
    private static MavDemo1 application;
    /**
     * selected sky, including lights and post-processing (not null)
     */
    private static Sky sky = new QuarrySky();
    /**
     * selected Vehicle (not null)
     */
    private static Vehicle vehicle = new GrandTourer();
    /**
     * selected World (not null)
     */
    private static World world = new Playground();
    // *************************************************************************
    // constructors

    /**
     * Instantiate a SimpleApplication without FlyCam or debug keys.
     */
    private MavDemo1() {
        super(
                new AudioListenerState(),
                new ConstantVerifierState(),
                new DetailedProfilerState(),
                new LemurLoadingState(preloads),
                new StatsAppState()
        );
        preloads = null; // to allow garbage collection
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Find the first attached AppState that's an instance of the specified
     * class.
     *
     * @param <T> the kind of AppState
     * @param subclass the kind of AppState to search for (not null)
     * @return the pre-existing instance (not null)
     * @throws IllegalArgumentException if the state is not attached
     */
    public static <T extends AppState> T findAppState(Class<T> subclass) {
        AppStateManager manager = application.getStateManager();
        boolean failOnMiss = true;
        T result = manager.getState(subclass, failOnMiss);

        assert result != null;
        return result;
    }

    /**
     * Access the application instance from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static MavDemo1 getApplication() {
        assert application != null;
        return application;
    }

    /**
     * Access the selected Vehicle from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static Vehicle getVehicle() {
        assert vehicle != null;
        return vehicle;
    }

    /**
     * Access the selected World from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static World getWorld() {
        assert world != null;
        return world;
    }

    /**
     * Main entry point for the MavDemo1 application.
     *
     * @param args array of command-line arguments (not null)
     */
    public static void main(String... args) {
        /*
         * Mute the chatty loggers found in some imported packages.
         */
        Heart.setLoggingLevels(Level.WARNING);
        Logger materialLogger = Logger.getLogger(Material.class.getName());
        materialLogger.setLevel(Level.SEVERE);

        boolean showSettingsDialog = true;
        /*
         * Process any command-line arguments.
         */
        for (String arg : args) {
            switch (arg) {
                case "--noDialog":
                    showSettingsDialog = false;
                    break;

                default:
                    logger.log(Level.WARNING,
                            "Unknown command-line argument {0}",
                            MyString.quote(arg));
            }
        }

        String status = Heart.areAssertionsEnabled() ? "enabled" : "disabled";
        logger.log(Level.WARNING, "Assertions are {0}.", status);

        boolean loadDefaults = true;
        AppSettings appSettings = new AppSettings(loadDefaults);
        appSettings.setGammaCorrection(true);
        appSettings.setRenderer(AppSettings.LWJGL_OPENGL32);
        appSettings.setResolution(1280, 720);
        appSettings.setSamples(8);
        appSettings.setTitle("More Advanced Vehicles");
        appSettings.setUseJoysticks(true);
        appSettings.setVSync(true);

        application = new MavDemo1();
        application.setDisplayFps(false);
        application.setDisplayStatView(false);
        application.setSettings(appSettings);
        application.setShowSettings(showSettingsDialog);
        application.start();
    }

    /**
     * Replace the current Sky with a new one.
     *
     * @param newSky the desired Sky (not null, loaded)
     */
    public void setSky(Sky newSky) {
        sky.removeFromWorld();
        sky = newSky;
        sky.addToWorld(world);
    }

    /**
     * Select a Vehicle.
     *
     * @param newVehicle the Vehicle to select (not null, loaded)
     */
    public void setVehicle(Vehicle newVehicle) {
        vehicle.removeFromWorld();
        vehicle = newVehicle;

        GlobalAudio globalAudio = findAppState(AudioHud.class);
        vehicle.addToWorld(world, globalAudio);

        findAppState(VehiclePointsState.class).setVehicle(vehicle);
    }

    /**
     * Replace the current World with a new one.
     *
     * @param newWorld the desired world (not null, loaded)
     */
    public void setWorld(World newWorld) {
        sky.removeFromWorld();
        vehicle.removeFromWorld();
        world.detach();

        world = newWorld;
        attachAllToScene();
        /*
         * Re-use the existing input state with the new Vehicle instance.
         */
        findAppState(CameraInputMode.class).setVehicle(vehicle);
    }
    // *************************************************************************
    // SimpleApplication methods

    @Override
    public void simpleInitApp() {
        NativeLibrary.setStartupMessageEnabled(false);
        /*
         * Log library versions.
         */
        logger.log(Level.INFO, "jme3-core version is {0}",
                MyString.quote(JmeVersion.FULL_NAME));
        logger.log(Level.INFO, "Heart version is {0}",
                MyString.quote(Heart.versionShort()));
        logger.log(Level.INFO, "JmePower version is {0}",
                MyString.quote(JmePowerVersion.versionShort()));
        logger.log(Level.INFO, "Minie version is {0}",
                MyString.quote(MinieVersion.versionShort()));

        assetManager.registerLoader(TrueTypeLoader.class, "ttf");
        renderer.setDefaultAnisotropicFilter(8);
        findAppState(DetailedProfilerState.class).setEnabled(false);
        Sky.setApplication(this);

        inputManager.addJoystickConnectionListener(new JoystickConnectionListener() {
            @Override
            public void onConnected(Joystick joystick) {
                System.out.println("Joystick connected: " + joystick);
            }

            @Override
            public void onDisconnected(Joystick joystick) {
                System.out.println("Joystick disconnected: " + joystick);
            }
        });

        inputManager.clearMappings();
        inputManager.clearRawInputListeners();
    }

    @Override
    public void simpleUpdate(float tpf) {
        AppState loader = stateManager.getState(LemurLoadingState.class);
        if (loader != null && !loader.isEnabled()) {
            stateManager.detach(loader);
            doneLoading();
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Configure, attach, and enable a new InputMode for camera control.
     */
    private void activateCameraMode() {
        InputMode cameraInputMode = new CameraInputMode()
                .assign(CameraInputMode.F_CAMERA_RESET_FOV, KeyInput.KEY_NUMPAD6)
                .assign(CameraInputMode.F_CAMERA_RESET_OFFSET, Button.MOUSE_BUTTON2)
                .assign(CameraInputMode.F_CAMERA_RESET_OFFSET, KeyInput.KEY_NUMPAD5)
                .assign(CameraInputMode.F_CAMVIEW, KeyInput.KEY_F5);

        stateManager.attach(cameraInputMode);
        cameraInputMode.setEnabled(true);
    }

    /**
     * Configure, attach, and enable a new InputMode for dumping.
     */
    private void activateDumpMode() {
        InputMode dumpMode = new DumpMode()
                .assign(DumpMode.F_DUMP_CAMERA, KeyInput.KEY_C)
                .assign(DumpMode.F_DUMP_PHYSICS, KeyInput.KEY_O)
                .assign(DumpMode.F_DUMP_RENDER_MANAGER, KeyInput.KEY_P);

        stateManager.attach(dumpMode);
        dumpMode.setEnabled(true);
    }

    /**
     * Configure, attach, and enable a new InputMode for physics simulation.
     */
    private void activatePhysicsMode() {
        InputMode physicsMode = new PhysicsMode()
                .assign(PhysicsMode.F_PAUSE,
                        KeyInput.KEY_PAUSE, KeyInput.KEY_PERIOD)
                .assign(PhysicsMode.F_SINGLE_STEP, KeyInput.KEY_COMMA)
                .assign(PhysicsMode.F_TOGGLE_PHYSICS_DEBUG, KeyInput.KEY_SLASH);

        stateManager.attach(physicsMode);
        physicsMode.setEnabled(true);
    }

    /**
     * Configure, attach, and enable a new InputMode for screenshots.
     */
    private void activateScreenshotMode() {
        ScreenshotMode screenshotMode = new ScreenshotMode();

        // Some Linux window managers block SYSRQ/PrtSc, so map F12 instead.
        screenshotMode.assign(ScreenshotMode.F_SCREEN_SHOT, KeyInput.KEY_F12);

        stateManager.attach(screenshotMode);
        screenshotMode.setEnabled(true);
    }

    /**
     * Configure, attach, and enable a new InputMode for signals.
     */
    private void activateSignalMode() {
        InputMode signalMode = new SignalMode()
                .assign(SignalMode.F_CAMERA_BACK1, KeyInput.KEY_NUMPAD1)
                .assign(SignalMode.F_CAMERA_DOWN1, KeyInput.KEY_NUMPAD2)
                .assign(SignalMode.F_CAMERA_DRAG_TO_ORBIT1, Button.MOUSE_BUTTON3)
                .assign(SignalMode.F_CAMERA_FORWARD1, KeyInput.KEY_NUMPAD7)
                .assign(SignalMode.F_CAMERA_UP1, KeyInput.KEY_NUMPAD8)
                .assign(SignalMode.F_CAMERA_XRAY1, KeyInput.KEY_NUMPAD0)
                .assign(SignalMode.F_CAMERA_ZOOM_IN1, KeyInput.KEY_NUMPAD9)
                .assign(SignalMode.F_CAMERA_ZOOM_OUT1, KeyInput.KEY_NUMPAD3)
                .assign(SignalMode.F_HORN1, KeyInput.KEY_H);

        stateManager.attach(signalMode);
        signalMode.setEnabled(true);
    }

    /**
     * Attach the selected Sky, World, and Vehicle to the scene.
     */
    private void attachAllToScene() {
        BulletAppState bulletAppState
                = getStateManager().getState(BulletAppState.class);
        PhysicsSpace physicsSpace = bulletAppState.getPhysicsSpace();
        world.attach(this, rootNode, physicsSpace);

        sky.addToWorld(world);

        GlobalAudio globalAudio = findAppState(AudioHud.class);
        vehicle.addToWorld(world, globalAudio);

        findAppState(VehiclePointsState.class).setVehicle(vehicle);
    }

    /**
     * Configure and attach a new (disabled) driving InputMode.
     */
    private void attachDrivingMode() {
        InputMode drivingMode = new DrivingInputMode()
                .assign(DrivingInputMode.F_FORWARD, KeyInput.KEY_W)
                .assign(DrivingInputMode.F_MAIN_BRAKE, KeyInput.KEY_S)
                .assign(DrivingInputMode.F_PARKING_BRAKE, KeyInput.KEY_SPACE)
                .assign(DrivingInputMode.F_RESET, KeyInput.KEY_R)
                .assign(DrivingInputMode.F_RETURN, KeyInput.KEY_ESCAPE)
                .assign(DrivingInputMode.F_REVERSE, KeyInput.KEY_E)
                .assign(DrivingInputMode.F_START_ENGINE, KeyInput.KEY_Y)
                .assign(DrivingInputMode.F_TURN_LEFT, KeyInput.KEY_A)
                .assign(DrivingInputMode.F_TURN_RIGHT, KeyInput.KEY_D);

        stateManager.attach(drivingMode);
    }

    /**
     * Configure and attach a new (disabled) OrbitCamera.
     */
    private void attachOrbitCamera() {
        SignalMode signalMode = findAppState(SignalMode.class);
        SignalTracker tracker = signalMode.getSignalTracker();
        OrbitCamera orbitCamera = new OrbitCamera("orbit", cam, tracker);

        FilterAll filter = new FilterAll(true);
        orbitCamera.setObstructionFilter(filter);
        float lowerAngle = 0.2f;
        float upperAngle = 1.7f;
        orbitCamera.setPoleExclusionAngles(upperAngle, lowerAngle);

        orbitCamera.setSignalName(CameraSignal.Back,
                SignalMode.F_CAMERA_BACK1.getId());
        orbitCamera.setSignalName(CameraSignal.DragToOrbit,
                SignalMode.F_CAMERA_DRAG_TO_ORBIT1.getId());
        orbitCamera.setSignalName(CameraSignal.Forward,
                SignalMode.F_CAMERA_FORWARD1.getId());
        orbitCamera.setSignalName(CameraSignal.OrbitCcw,
                SignalMode.F_CAMERA_CCW1.getId());
        orbitCamera.setSignalName(CameraSignal.OrbitCw,
                SignalMode.F_CAMERA_CW1.getId());
        orbitCamera.setSignalName(CameraSignal.OrbitDown,
                SignalMode.F_CAMERA_DOWN1.getId());
        orbitCamera.setSignalName(CameraSignal.OrbitUp,
                SignalMode.F_CAMERA_UP1.getId());
        orbitCamera.setSignalName(CameraSignal.ZoomIn,
                SignalMode.F_CAMERA_ZOOM_IN1.getId());
        orbitCamera.setSignalName(CameraSignal.ZoomOut,
                SignalMode.F_CAMERA_ZOOM_OUT1.getId());

        stateManager.attach(orbitCamera);
    }

    /**
     * Finish initializing the application after LoadingState has warmed up the
     * AssetCache and initialized Lemur.
     */
    private void doneLoading() {
        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        stateManager.attachAll(
                new AudioHud(),
                new CameraNameState(),
                new ChunkManager(),
                new CompassState(),
                new DriverHud(),
                new GearNameState(),
                new MainMenu(),
                new PhysicsHud(),
                new VehiclePointsState()
        );

        Sky.initialize();
        attachAllToScene();
        /*
         * Attach input modes.
         */
        activateDumpMode();
        activatePhysicsMode();
        activateScreenshotMode();
        activateSignalMode();
        activateCameraMode();
        attachDrivingMode(); // needs the SignalTracker of the SignalMode
        attachOrbitCamera(); // needs the SignalTracker of the SignalMode

        MyCamera.setYTangent(cam, 1f);
        world.resetCameraPosition();
    }
}
