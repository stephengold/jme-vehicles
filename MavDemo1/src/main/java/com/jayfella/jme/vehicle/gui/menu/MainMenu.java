package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.debug.DebugTabState;
import com.jayfella.jme.vehicle.debug.EnginePowerGraphState;
import com.jayfella.jme.vehicle.debug.TireDataState;
import com.jayfella.jme.vehicle.debug.VehicleEditorState;
import com.jayfella.jme.vehicle.gui.lemur.DriverHud;
import com.jayfella.jme.vehicle.input.DrivingInputMode;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.jme3.app.state.AppStateManager;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MainMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(MainMenu.class.getName());
    // *************************************************************************
    // AnimatedMenuState methods

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    @Override
    protected List<Button> createItems() {
        MavDemo1 application = MavDemo1.getApplication();
        List<Button> result = new ArrayList<>(6);

        Button button = new Button("Drive");
        button.addClickCommands(source -> animateOut(()
                -> drive()
        ));
        result.add(button);

        button = new Button("Change World");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new WorldMenu())
        ));
        result.add(button);

        button = new Button("Change Vehicle");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new VehicleMenu())
        ));
        result.add(button);

        button = new Button("Customize");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new CustomizationMenu())
        ));
        result.add(button);

        button = new Button("Attribution");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new AttributionMenu())
        ));
        result.add(button);

        button = new Button("Quit the Demo");
        button.addClickCommands(source -> animateOut(()
                -> application.stop()
        ));
        result.add(button);

        return result;
    }
    // *************************************************************************
    // private methods

    /**
     * Drive the selected Vehicle in the selected World.
     */
    private void drive() {
        Vehicle vehicle = MavDemo1.getVehicle();
        vehicle.getEngine().setRunning(true);
        DriverHud hud = getState(DriverHud.class);
        hud.setVehicle(vehicle);
        hud.setEnabled(true);

        getState(DrivingInputMode.class).setEnabled(true);

        // engine graph GUI for viewing torque/power @ revs
        EnginePowerGraphState enginePowerGraphState = new EnginePowerGraphState(vehicle);
        enginePowerGraphState.setEnabled(false);
        AppStateManager stateManager = getStateManager();
        stateManager.attach(enginePowerGraphState);

        // tire data GUI for viewing how much grip each tire has according to the Pacejka formula
        TireDataState tireDataState = new TireDataState(vehicle);
        tireDataState.setEnabled(false);
        stateManager.attach(tireDataState);

        // the main vehicle editor to modify aspects of the vehicle in real time
        VehicleEditorState vehicleEditorState = new VehicleEditorState(vehicle);
        stateManager.attach(vehicleEditorState);

        // vehicle debug add-on to enable/disable debug screens
        DebugTabState debugTabState = new DebugTabState();
        stateManager.attach(debugTabState);

        stateManager.detach(this);
    }
}
