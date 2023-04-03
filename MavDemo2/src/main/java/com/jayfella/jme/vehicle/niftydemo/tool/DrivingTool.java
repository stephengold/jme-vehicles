package com.jayfella.jme.vehicle.niftydemo.tool;

import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.part.Engine;
import com.jayfella.jme.vehicle.part.GearBox;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.SignalTracker;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;

/**
 * The controller for the "Driving" tool in the main HUD of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class DrivingTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(DrivingTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    DrivingTool(GuiScreenController screenController) {
        super(screenController, "driving");
    }
    // *************************************************************************
    // Tool methods

    /**
     * Enumerate this tool's checkboxes.
     *
     * @return a new list of names (unique id prefixes)
     */
    @Override
    protected List<String> listCheckBoxes() {
        List<String> result = super.listCheckBoxes();
        result.add("engineRunning");
        result.add("horn");
        result.add("mainBrake");
        result.add("parkingBrake");

        return result;
    }

    /**
     * Enumerate this tool's radio buttons.
     *
     * @return a new list of names (unique id prefixes)
     */
    @Override
    protected List<String> listRadioButtons() {
        List<String> result = super.listRadioButtons();
        result.add("drive");
        result.add("reverse");

        return result;
    }

    /**
     * Update the MVC model based on a check-box event.
     *
     * @param name the name (unique id prefix) of the checkbox
     * @param isChecked the new state of the checkbox (true&rarr;checked,
     * false&rarr;unchecked)
     */
    @Override
    public void onCheckBoxChanged(String name, boolean isChecked) {
        Vehicle vehicle = MavDemo2.getDemoState().getVehicles().getSelected();
        if (vehicle == null) {
            return;
        }

        switch (name) {
            case "engineRunning":
                Engine engine = vehicle.getEngine();
                engine.setRunning(isChecked);
                break;

            case "horn":
            case "mainBrake":
            case "parkingBrake":
                // no effect - indicator only
                break;

            default:
                super.onCheckBoxChanged(name, isChecked);
        }
    }

    /**
     * Update the MVC model based on this tool's radio buttons.
     *
     * @param buttonName the name (unique id prefix) of the button (not null)
     */
    @Override
    public void onRadioButtonChanged(String buttonName) {
        Vehicle vehicle = MavDemo2.getDemoState().getVehicles().getSelected();
        if (vehicle == null) {
            return;
        }
        GearBox gearBox = vehicle.getGearBox();
        int engagedGearNum = gearBox.getEngagedGearNum();

        switch (buttonName) {
            case "drive":
                if (engagedGearNum < 1) {
                    gearBox.engageGearNum(1);
                }
                break;
            case "reverse":
                gearBox.setReversing(true);
                break;
            default:
                super.onRadioButtonChanged(buttonName);
        }
    }

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        Vehicle vehicle = MavDemo2.getDemoState().getVehicles().getSelected();
        Engine engine = vehicle.getEngine();
        boolean engineRunning = engine.isRunning();
        setChecked("engineRunning", engineRunning);

        GearBox gearBox = vehicle.getGearBox();
        int gearNum = gearBox.getEngagedGearNum();
        String modeName;
        if (gearNum > 0) {
            modeName = "drive";
        } else {
            modeName = "reverse";
        }
        getScreenController().setRadioButton(modeName + "RadioButton");

        String gearName = gearBox.getEngagedGear().name();
        setStatusText("drivingGear", gearName);

        SignalTracker signals = getSignals();
        boolean mainBrake = signals.test("mainBrake");
        setChecked("mainBrake", mainBrake);

        boolean parkingBrake = signals.test("parkingBrake");
        setChecked("parkingBrake", parkingBrake);

        boolean soundHorn = signals.test("soundHorn");
        setChecked("horn", soundHorn);
    }
}
