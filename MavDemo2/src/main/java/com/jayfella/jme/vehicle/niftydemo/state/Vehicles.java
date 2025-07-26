package com.jayfella.jme.vehicle.niftydemo.state;

import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.World;
import com.jayfella.jme.vehicle.gui.SpeedometerState;
import com.jayfella.jme.vehicle.gui.SteeringWheelState;
import com.jayfella.jme.vehicle.gui.TachometerState;
import com.jayfella.jme.vehicle.gui.VehiclePointsState;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.part.Engine;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.Validate;
import jme3utilities.math.MyMath;
import jme3utilities.ui.InputMode;

/**
 * The state of all vehicles in MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Vehicles {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Vehicles.class.getName());
    // *************************************************************************
    // fields

    /**
     * instance that owns these vehicles
     */
    final private DemoState demoState;
    /**
     * list of Vehicle instances, from oldest to newest
     */
    final private List<Vehicle> vehicles = new ArrayList<>(99);
    /**
     * selected Vehicle, or null if none
     */
    private Vehicle selected;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a collection of vehicles.
     *
     * @param owner (not null, alias created)
     */
    Vehicles(DemoState owner) {
        this.demoState = owner;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add a Vehicle to the demo.
     *
     * @param newVehicle the vehicle to add (not null, not added)
     */
    void add(Vehicle newVehicle) {
        assert newVehicle != null;
        assert !vehicles.contains(newVehicle);

        vehicles.add(newVehicle);

        World world = demoState.getWorld();
        newVehicle.addToWorld(world, demoState);
    }

    /**
     * Count how many vehicles are in the demo.
     *
     * @return the count (&ge;0)
     */
    public int countVehicles() {
        int count = vehicles.size();
        return count;
    }

    /**
     * Access the indexed Vehicle.
     *
     * @param index the index (&ge;0, &lt;numVehicles, 0=oldest)
     * @return the pre-existing instance (not null)
     */
    public Vehicle getVehicle(int index) {
        Validate.inRange(index, "index", 0, vehicles.size() - 1);
        Vehicle result = vehicles.get(index);

        assert result != null;
        return result;
    }

    /**
     * Access the selected Vehicle.
     *
     * @return the pre-existing Vehicle, or null if none
     */
    public Vehicle getSelected() {
        return selected;
    }

    /**
     * Test whether a Vehicle is selected.
     *
     * @return true if selected, otherwise false
     */
    public boolean isVehicleSelected() {
        if (selected == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Remove the specified Vehicle.
     *
     * @param vehicle the Vehicle to remove
     */
    void remove(Vehicle vehicle) {
        vehicle.removeFromWorld();

        boolean success = vehicles.remove(vehicle);
        assert success;

        if (selected == vehicle) {
            select(null);
        }
    }

    /**
     * Remove all vehicles.
     */
    void removeAll() {
        int numVehicles = vehicles.size();
        Vehicle[] array = new Vehicle[numVehicles];
        vehicles.toArray(array);

        for (Vehicle vehicle : array) {
            remove(vehicle);
        }

        assert vehicles.isEmpty();
        assert selected == null;
    }

    /**
     * Remove the selected Vehicle (if any).
     */
    public void removeSelected() {
        if (selected != null) {
            remove(selected);
        }
        assert selected == null;
    }

    /**
     * Select the specified Vehicle.
     *
     * @param vehicle the Vehicle to select (alias created) or null to deselect
     */
    public void select(Vehicle vehicle) {
        MavDemo2 application = MavDemo2.getApplication();
        AppStateManager stateManager = application.getStateManager();

        if (selected != vehicle) {
            if (selected != null) {
                SpeedometerState speedometer
                        = MavDemo2.findAppState(SpeedometerState.class);
                stateManager.detach(speedometer);

                SteeringWheelState steeringWheel
                        = MavDemo2.findAppState(SteeringWheelState.class);
                stateManager.detach(steeringWheel);

                TachometerState tachometer
                        = MavDemo2.findAppState(TachometerState.class);
                stateManager.detach(tachometer);
            }

            this.selected = vehicle;

            VehiclePointsState vps
                    = MavDemo2.findAppState(VehiclePointsState.class);
            vps.setVehicle(vehicle);

            if (vehicle != null) {
                InputMode dim = MavDemo2.getApplication().getDefaultInputMode();
                boolean dimEnabled = dim.isEnabled();

                SpeedometerState speedometer
                        = new SpeedometerState(vehicle, SpeedUnit.MPH);
                speedometer.setEnabled(dimEnabled);
                dim.influence(speedometer);
                boolean success = stateManager.attach(speedometer);
                assert success;

                float radius = 120f; // pixels
                Camera camera = application.getCamera();
                float x = 0.5f * camera.getWidth();
                float y = 0.18f * camera.getHeight();
                float z = 1f;
                SteeringWheelState steeringWheel
                        = new SteeringWheelState(radius, new Vector3f(x, y, z));
                steeringWheel.setVehicle(vehicle);
                steeringWheel.setEnabled(dimEnabled);
                dim.influence(steeringWheel);
                success = stateManager.attach(steeringWheel);
                assert success;

                Engine engine = vehicle.getEngine();
                TachometerState tachometer = new TachometerState(engine);
                tachometer.setEnabled(dimEnabled);
                dim.influence(tachometer);
                success = stateManager.attach(tachometer);
                assert success;
            }
        }
    }

    /**
     * Select a Vehicle based on its cyclic index.
     *
     * @param cyclicIndex the index (0=oldest, may be negative)
     */
    public void selectIndex(int cyclicIndex) {
        if (vehicles.isEmpty()) {
            throw new IllegalStateException("There aren't any vehicles!");
        }

        int numVehicles = vehicles.size();
        int index = MyMath.modulo(cyclicIndex, numVehicles);
        this.selected = vehicles.get(index);
    }

    /**
     * Select a Vehicle based on its index relative to the selected Vehicle.
     *
     * @param indexOffset the amount to add cyclically to the index
     */
    public void selectNext(int indexOffset) {
        int oldIndex = vehicles.indexOf(selected);
        assert oldIndex >= 0 : oldIndex;
        int cyclicIndex = oldIndex + indexOffset;
        selectIndex(cyclicIndex);
    }
}
