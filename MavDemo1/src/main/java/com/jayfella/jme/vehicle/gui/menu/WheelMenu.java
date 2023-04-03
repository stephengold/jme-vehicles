package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.WheelModel;
import com.jayfella.jme.vehicle.examples.wheels.BasicAlloyWheel;
import com.jayfella.jme.vehicle.examples.wheels.BuggyFrontWheel;
import com.jayfella.jme.vehicle.examples.wheels.BuggyRearWheel;
import com.jayfella.jme.vehicle.examples.wheels.CruiserWheel;
import com.jayfella.jme.vehicle.examples.wheels.DarkAlloyWheel;
import com.jayfella.jme.vehicle.examples.wheels.HatchbackWheel;
import com.jayfella.jme.vehicle.examples.wheels.InvisibleWheel;
import com.jayfella.jme.vehicle.examples.wheels.MotorcycleFrontWheel;
import com.jayfella.jme.vehicle.examples.wheels.MotorcycleRearWheel;
import com.jayfella.jme.vehicle.examples.wheels.RangerWheel;
import com.jayfella.jme.vehicle.examples.wheels.RotatorFrontWheel;
import com.jayfella.jme.vehicle.examples.wheels.RotatorRearWheel;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to choose among the available wheel models.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class WheelMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(WheelMenu.class.getName());
    // *************************************************************************
    // AnimatedMenuState methods

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    @Override
    protected List<Button> createItems() {
        List<Button> result = new ArrayList<>(13);

        addButton(result, "Basic Alloy",
                source -> setModel(BasicAlloyWheel.class));
        addButton(result, "Buggy Front",
                source -> setModel(BuggyFrontWheel.class));
        addButton(result, "Buggy Rear",
                source -> setModel(BuggyRearWheel.class));
        addButton(result, "Cruiser",
                source -> setModel(CruiserWheel.class));
        addButton(result, "Dark Alloy",
                source -> setModel(DarkAlloyWheel.class));
        addButton(result, "Hatchback",
                source -> setModel(HatchbackWheel.class));
        addButton(result, "Invisible",
                source -> setModel(InvisibleWheel.class));
        addButton(result, "Motorcycle Front",
                source -> setModel(MotorcycleFrontWheel.class));
        addButton(result, "Motorcycle Rear",
                source -> setModel(MotorcycleRearWheel.class));
        addButton(result, "Ranger",
                source -> setModel(RangerWheel.class));
        addButton(result, "Rotator Front",
                source -> setModel(RotatorFrontWheel.class));
        addButton(result, "Rotator Rear",
                source -> setModel(RotatorRearWheel.class));
        addButton(result, "<< Back",
                source -> animateOut(() -> goTo(new CustomizationMenu())));

        return result;
    }
    // *************************************************************************
    // private methods

    private static void setModel(Class<? extends WheelModel> wheelModelClass) {
        Vehicle vehicle = MavDemo1.getVehicle();
        int numWheels = vehicle.countWheels();
        for (int wheelIndex = 0; wheelIndex < numWheels; ++wheelIndex) {
            vehicle.setWheelModel(wheelIndex, wheelModelClass);
        }
    }
}
