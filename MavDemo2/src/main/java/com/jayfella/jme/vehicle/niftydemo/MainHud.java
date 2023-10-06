package com.jayfella.jme.vehicle.niftydemo;

import com.jayfella.jme.vehicle.niftydemo.tool.Tools;
import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.BulletAppState;
import java.util.logging.Logger;
import jme3utilities.InitialState;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.ui.InputMode;

/**
 * The screen controller for the main heads-up display (HUD) of MavDemo2. The
 * GUI includes a menu bar and some tool windows.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class MainHud extends GuiScreenController {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(MainHud.class.getName());
    // *************************************************************************
    // fields

    /**
     * physics simulation speed, saved when the HUD was disabled
     */
    private float savedPhysicsSpeed = 1f;
    /**
     * controllers for tool windows
     */
    final public Tools tools = new Tools(this);
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized, disabled screen that will be enabled during
     * initialization.
     */
    MainHud() {
        super("main", "/Interface/Nifty/huds/main.xml", InitialState.Enabled);
        /*
         * Acorus has already initialized and enabled the default input mode.
         * Disable it and load the desired bindings.
         */
        InputMode dim = MavDemo2.getApplication().getDefaultInputMode();
        dim.setEnabled(false);
        dim.setConfigPath("Interface/bindings/default.properties");
        dim.loadBindings();

        setListener(dim);
        dim.influence(this);
    }
    // *************************************************************************
    // GuiScreenController methods

    /**
     * Disable the screen controller.
     */
    @Override
    protected void disable() {
        // pause the physics simulation
        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        this.savedPhysicsSpeed = bas.getSpeed();
        bas.setSpeed(0f);

        super.disable();
    }

    /**
     * Enable the screen controller.
     */
    @Override
    protected void enable() {
        // resume the physics simulation
        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        bas.setSpeed(savedPhysicsSpeed);

        super.enable();
    }

    /**
     * Initialize this app state on the first update after it gets attached.
     *
     * @param sm application's state manager (not null)
     * @param app application which owns this state (not null)
     */
    @Override
    public void initialize(AppStateManager sm, Application app) {
        if (!(app instanceof MavDemo2)) {
            throw new IllegalArgumentException(
                    "application should be a MavDemo2 instance");
        }
        super.initialize(sm, app);
        tools.attachAll(stateManager);
    }
}
