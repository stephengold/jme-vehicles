package com.jayfella.jme.vehicle.niftydemo;

import com.github.stephengold.jmepower.JmeLoadingState;
import com.github.stephengold.jmepower.JmePowerVersion;
import com.jayfella.jme.vehicle.ChunkManager;
import com.jayfella.jme.vehicle.Vehicle;
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
import com.jayfella.jme.vehicle.gui.VehiclePointsState;
import com.jayfella.jme.vehicle.niftydemo.action.Action;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import com.jayfella.jme.vehicle.niftydemo.view.Cameras;
import com.jayfella.jme.vehicle.niftydemo.view.View;
import com.jme3.app.BasicProfilerState;
import com.jme3.app.StatsAppState;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.ConstantVerifierState;
import com.jme3.app.state.ScreenshotAppState;
import com.jme3.audio.AudioListenerState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeVersion;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.Loadable;
import jme3utilities.MyString;
import jme3utilities.SignalTracker;
import jme3utilities.debug.PerformanceAppState;
import jme3utilities.math.RectSizeLimits;
import jme3utilities.minie.MinieVersion;
import jme3utilities.minie.PhysicsDumper;
import jme3utilities.nifty.GuiApplication;
import jme3utilities.nifty.bind.BindScreen;
import jme3utilities.nifty.displaysettings.DsScreen;
import jme3utilities.ui.ActionApplication;
import jme3utilities.ui.DisplaySettings;
import jme3utilities.ui.InputMode;
import jme3utilities.ui.ShowDialog;

/**
 * An application with a Nifty GUI to demonstrate the MaVehicles library. The
 * application's main entry point is in this class.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class MavDemo2 extends GuiApplication {
    // *************************************************************************
    // constants and loggers

    /**
     * steering-control parameters
     */
    final private static float maxSteerAngle = 1f;
    final private static float turnRate = 0.5f;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(MavDemo2.class.getName());
    /**
     * application name (for the title bar of the app's window)
     */
    final private static String applicationName
            = MavDemo2.class.getSimpleName();
    // *************************************************************************
    // fields

    /**
     * true once {@link #startup1()} has completed, until then false
     */
    private static boolean didStartup1 = false;
    /**
     * demo's "game state"
     */
    private static DemoState demoState;
    /**
     * Nifty screen for editing display settings
     */
    private static DsScreen displaySettingsScreen;
    /**
     * steer angle from the previous update
     */
    private static float steerAngle = 0f;
    /**
     * application instance
     */
    private static MavDemo2 application;
    /**
     * dump state for debugging
     */
    final public static PhysicsDumper dumper = new PhysicsDumper();
    // *************************************************************************
    // constructors

    /**
     * Instantiate the application.
     */
    public MavDemo2() {
        /*
         * This application doesn't want DebugKeysAppState nor FlyCamAppState.
         * StatsAppState will be attached AFTER the loading cinematic completes.
         */
        super(new AudioListenerState(), new ConstantVerifierState());
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Find the first attached AppState that's an instance of the specified
     * class.
     *
     * @param <T> type of subclass
     * @param subclass the kind of AppState to search for (not null)
     * @return the pre-existing instance (not null)
     */
    public static <T extends AppState> T findAppState(Class<T> subclass) {
        AppStateManager manager = application.getStateManager();
        T appState = manager.getState(subclass);

        assert appState != null;
        return appState;
    }

    /**
     * Access the application instance from a static context.
     *
     * @return the pre-existing instance (not null)
     */
    public static MavDemo2 getApplication() {
        assert application != null;
        return application;
    }

    /**
     * Access the live DemoState.
     *
     * @return the pre-existing instance (not null)
     */
    public static DemoState getDemoState() {
        assert demoState != null;
        return demoState;
    }

    /**
     * Main entry point for the MavDemo2 application.
     *
     * @param arguments array of command-line arguments (not null)
     */
    public static void main(String[] arguments) {
        // Mute the chatty loggers found in certain packages.
        Heart.setLoggingLevels(Level.WARNING);
//        Preloader.logger.setLevel(Level.INFO);

        ShowDialog showDialog = ShowDialog.FirstTime;

        // Process any command-line arguments.
        for (String arg : arguments) {
            switch (arg) {
                case "-f":
                case "--forceDialog":
                    showDialog = ShowDialog.Always;
                    break;

                case "--noDialog":
                    showDialog = ShowDialog.Never;
                    break;

                case "-v":
                case "--verbose":
                    Heart.setLoggingLevels(Level.INFO);
                    break;

                default:
                    logger.log(Level.WARNING,
                            "Unknown command-line argument {0}",
                            MyString.quote(arg));
            }
        }

        String title = applicationName + " " + MyString.join(arguments);
        mainStartup(showDialog, title);
    }
    // *************************************************************************
    // GuiApplication methods

    /**
     * Initialize the MavDemo2 application.
     */
    @Override
    public void guiInitializeApplication() {
        logger.info("");

        if (!Heart.areAssertionsEnabled()) {
            logger.warning("Assertions are disabled!");
        }

        // Log version strings.
        logger.log(Level.INFO, "jme3-core version is {0}",
                MyString.quote(JmeVersion.FULL_NAME));
        logger.log(Level.INFO, "Heart version is {0}",
                MyString.quote(Heart.versionShort()));
        logger.log(Level.INFO, "Minie version is {0}",
                MyString.quote(MinieVersion.versionShort()));
        logger.log(Level.INFO, "JmePower version is {0}",
                MyString.quote(JmePowerVersion.versionShort()));

        renderer.setDefaultAnisotropicFilter(8);

        dumper.setDumpBucket(true)
                .setDumpCull(true)
                .setDumpShadow(true)
                .setDumpTransform(true);

        Loadable[] preloadArray = {
            new ClassicMotorcycle(),
            new DuneBuggy(),
            new HoverTank(),
            new GrandTourer(),
            new Playground(),
            new HatchBack(),
            new Mountains(),
            new QuarrySky(),
            new Nismo(),
            new PickupTruck(),
            new Rotator(),
            new Racetrack()
        };
        JmeLoadingState loading = new JmeLoadingState(preloadArray);
        stateManager.attach(loading);
    }

    /**
     * Process an action from the GUI or keyboard that wasn't handled by the
     * active InputMode.
     *
     * @param actionString textual description of the action (not null)
     * @param ongoing true if the action is ongoing, otherwise false
     * @param tpf time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void onAction(String actionString, boolean ongoing, float tpf) {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "Got action {0} ongoing={1}", new Object[]{
                MyString.quote(actionString), ongoing
            });
        }

        boolean handled;
        if (ongoing) {
            handled = Action.processOngoing(actionString);
        } else {
            handled = Action.processNotOngoing(actionString);
        }

        if (!handled) {

            // Forward unhandled action to the superclass.
            super.onAction(actionString, ongoing, tpf);
        }
    }

    /**
     * Callback invoked once per frame.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);

        AppState loading = stateManager.getState(JmeLoadingState.class);
        if (loading != null) {
            if (!loading.isEnabled()) {
                // The loading cinematic has completed.
                stateManager.detach(loading);
                attachAppStates();
            }
            return;
        }

        if (!didStartup1) {
            startup1();
            didStartup1 = true;
        } else {
            Cameras.update();
        }

        SignalTracker signals = getSignals();
        Vehicle vehicle = MavDemo2.getDemoState().getVehicles().getSelected();
        if (vehicle.getEngine().isRunning()) {
            float accelerate = signals.test("accelerate") ? 1f : 0f;
            vehicle.setAccelerateSignal(accelerate);
        }

        float mainBrake = signals.test("mainBrake") ? 1f : 0f;
        float parkingBrake = signals.test("parkingBrake") ? 1f : 0f;
        vehicle.setBrakeSignals(mainBrake, parkingBrake);

        updateSteering(tpf);

        boolean requested = signals.test("soundHorn");
        vehicle.setHornStatus(requested);
    }
    // *************************************************************************
    // private methods

    /**
     * Attach app states during initialization.
     */
    private void attachAppStates() {
        boolean success;

        BasicProfilerState profiler = new BasicProfilerState();
        success = stateManager.attach(profiler);
        assert success;

        StatsAppState stats = new StatsAppState();
        success = stateManager.attach(stats);
        assert success;

        BindScreen bindScreen = new BindScreen();
        success = stateManager.attach(bindScreen);
        assert success;

        ChunkManager chunk = new ChunkManager();
        success = stateManager.attach(chunk);
        assert success;

        CompassState compass = new CompassState();
        InputMode dim = MavDemo2.getApplication().getDefaultInputMode();
        compass.setEnabled(dim.isEnabled());
        dim.influence(compass);
        success = stateManager.attach(compass);
        assert success;

        PhysicsSpace physicsSpace = configurePhysics();

        Cameras.configure();
        View view = new View();
        success = stateManager.attach(view);
        assert success;

        VehiclePointsState vehiclePoints = new VehiclePointsState();
        success = stateManager.attach(vehiclePoints);
        assert success;

        this.demoState = new DemoState(physicsSpace);
        Vehicle vehicle = new GrandTourer();
        vehicle.load(assetManager);
        demoState.addVehicle(vehicle);

        success = stateManager.attach(displaySettingsScreen);
        assert success;
        /*
         * Create and attach a screen controller for the heads-up display (HUD)
         * and link it to the DefaultInputMode.
         */
        MainHud hud = new MainHud();
        success = stateManager.attach(hud);
        assert success;

        PerformanceAppState pas = new PerformanceAppState();
        success = stateManager.attach(pas);
        assert success;

        String directory
                = System.getProperty("user.dir") + File.separator;
        String filenamePrefix = "screen_shot";
        ScreenshotAppState screenshotAppState
                = new ScreenshotAppState(directory, filenamePrefix);
        success = stateManager.attach(screenshotAppState);
        assert success;
    }

    /**
     * Configure physics during startup.
     *
     * @return the new physics space
     */
    private PhysicsSpace configurePhysics() {
        BulletAppState bulletAppState = new BulletAppState();
        boolean success = stateManager.attach(bulletAppState);
        assert success;

        PhysicsSpace result = bulletAppState.getPhysicsSpace();
        result.setAccuracy(0.008f);
        result.setGravity(new Vector3f(0f, -9.8f, 0f));
        result.setSolverNumIterations(15);

        return result;
    }

    /**
     * Initialization performed immediately after parsing the command-line
     * arguments.
     *
     * @param showDialog when to show the JME settings dialog (not null)
     * @param title for the title bar of the app's window
     */
    private static void mainStartup(ShowDialog showDialog, String title) {
        // Instantiate the application.
        application = new MavDemo2();

        String status = Heart.areAssertionsEnabled() ? "enabled" : "disabled";
        logger.log(Level.WARNING, "Assertions are {0}.", status);

        // Instantiate the display-settings screen.
        RectSizeLimits dsl = new RectSizeLimits(
                1_280, 720, // min width, height
                2_048, 1_080 // max width, height
        );
        DisplaySettings displaySettings
                = new DisplaySettings(application, applicationName, dsl) {
            @Override
            protected void applyOverrides(AppSettings settings) {
                super.applyOverrides(settings);

                setShowDialog(showDialog);
                settings.setGammaCorrection(true);
                settings.setRenderer(AppSettings.LWJGL_OPENGL2); // JME issue 1903
                settings.setResolution(1280, 720);
                settings.setSamples(8);
                settings.setTitle(title);
                settings.setVSync(true);
            }
        };
        displaySettingsScreen = new DsScreen(displaySettings);

        AppSettings appSettings = displaySettings.initialize();
        if (appSettings != null) {
            application.setSettings(appSettings);
            /*
             * Don't pause on lost focus.  This simplifies debugging by
             * permitting the application to run while minimized.
             */
            application.setPauseOnLostFocus(false);
            /*
             * If the settings dialog should be shown, it has already been shown
             * by DisplaySettings.initialize().
             */
            application.setShowSettings(false);
            /*
             * Designate a sandbox directory.
             * This has to be done *prior to* initialization.
             */
            try {
                ActionApplication.designateSandbox("Written Assets");
            } catch (IOException exception) {
                // do nothing
            }

            application.start();
            // ... and onward to MavDemo2.guiInitializeApplication()!
        }
    }

    /**
     * Initialization performed once, after the loading cinematic completes.
     */
    private void startup1() {
        logger.info("");
        /*
         * Disable the JME statistic displays.
         * These can be re-enabled by pressing the F5 hotkey.
         */
        setDisplayFps(false);
        setDisplayStatView(false);

        // Re-enable the default input mode, which was disabled by MainHud.
        getDefaultInputMode().setEnabled(true);
    }

    /**
     * Implement progressive steering, for better control and more fun.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    private void updateSteering(float tpf) {
        SignalTracker signals = getSignals();

        if (signals.test("steerLeft")) {
            steerAngle += tpf * turnRate;
            steerAngle = Math.min(steerAngle, maxSteerAngle);

        } else if (signals.test("steerRight")) {
            steerAngle -= tpf * turnRate;
            steerAngle = Math.max(steerAngle, -maxSteerAngle);

        } else {
            steerAngle = 0f;
        }

        Vehicle vehicle = MavDemo2.getDemoState().getVehicles().getSelected();
        vehicle.steer(steerAngle);
    }
}
