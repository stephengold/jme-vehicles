package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.examples.vehicles.ClassicMotorcycle;
import com.jayfella.jme.vehicle.examples.vehicles.DuneBuggy;
import com.jayfella.jme.vehicle.examples.vehicles.GrandTourer;
import com.jayfella.jme.vehicle.examples.vehicles.HatchBack;
import com.jayfella.jme.vehicle.examples.vehicles.HoverTank;
import com.jayfella.jme.vehicle.examples.vehicles.Nismo;
import com.jayfella.jme.vehicle.examples.vehicles.PickupTruck;
import com.jayfella.jme.vehicle.examples.vehicles.Rotator;
import com.jayfella.jme.vehicle.input.CameraInputMode;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.jme3.asset.AssetManager;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to choose among the available vehicles.
 *
 * Derived from the CarSelectorState class in the Advanced Vehicles project.
 */
class VehicleMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(VehicleMenu.class.getName());
    // *************************************************************************
    // AnimatedMenuState methods

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    @Override
    protected List<Button> createItems() {
        List<Button> result = new ArrayList<>(9);

        addButton(result, "Classic Motorcycle",
                source -> setVehicle(new ClassicMotorcycle()));
        addButton(result, "Grand Tourer",
                source -> setVehicle(new GrandTourer()));
        addButton(result, "Nismo",
                source -> setVehicle(new Nismo()));
        addButton(result, "Pickup Truck",
                source -> setVehicle(new PickupTruck()));
        addButton(result, "Hatchback",
                source -> setVehicle(new HatchBack()));
        addButton(result, "Dune Buggy",
                source -> setVehicle(new DuneBuggy()));
        addButton(result, "Rotator",
                source -> setVehicle(new Rotator()));
        addButton(result, "Hover Tank",
                source -> setVehicle(new HoverTank()));
        addButton(result, "<< Back",
                source -> animateOut(() -> goTo(new MainMenu())));

        return result;
    }
    // *************************************************************************
    // private methods

    private void setVehicle(Vehicle vehicle) {
        MavDemo1 main = MavDemo1.getApplication();
        AssetManager assetManager = main.getAssetManager();
        vehicle.load(assetManager);
        MavDemo1.setVehicle(vehicle);

        getState(CameraInputMode.class).setVehicle(vehicle);
    }
}
