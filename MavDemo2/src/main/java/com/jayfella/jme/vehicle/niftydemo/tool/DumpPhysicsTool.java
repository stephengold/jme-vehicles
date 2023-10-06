package com.jayfella.jme.vehicle.niftydemo.tool;

import com.jayfella.jme.vehicle.Prop;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.minie.DumpFlags;
import jme3utilities.minie.PhysicsDumper;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;

/**
 * The controller for the "Dump Physics" tool in the main HUD of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class DumpPhysicsTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(DumpPhysicsTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    DumpPhysicsTool(GuiScreenController screenController) {
        super(screenController, "dumpPhysics");
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
        result.add("dumpChildShapes");
        result.add("dumpIgnores");
        result.add("dumpJib");
        result.add("dumpJis");
        result.add("dumpMotors");
        result.add("dumpNativeIDs");
        result.add("dumpPcos");

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
        PhysicsDumper dumper = MavDemo2.dumper;

        switch (name) {
            case "dumpChildShapes":
                dumper.setEnabled(DumpFlags.ChildShapes, isChecked);
                break;
            case "dumpIgnores":
                dumper.setEnabled(DumpFlags.Ignores, isChecked);
                break;
            case "dumpJib":
                dumper.setEnabled(DumpFlags.JointsInBodies, isChecked);
                break;
            case "dumpJis":
                dumper.setEnabled(DumpFlags.JointsInSpaces, isChecked);
                break;
            case "dumpMotors":
                dumper.setEnabled(DumpFlags.Motors, isChecked);
                break;
            case "dumpNativeIDs":
                dumper.setEnabled(DumpFlags.NativeIDs, isChecked);
                break;
            case "dumpPcos":
                dumper.setEnabled(DumpFlags.Pcos, isChecked);
                break;

            default:
                super.onCheckBoxChanged(name, isChecked);
        }
    }

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        PhysicsDumper dumper = MavDemo2.dumper;

        boolean dumpChildShapes = dumper.isEnabled(DumpFlags.ChildShapes);
        setChecked("dumpChildShapes", dumpChildShapes);

        boolean dumpIgnores = dumper.isEnabled(DumpFlags.Ignores);
        setChecked("dumpIgnores", dumpIgnores);

        boolean dumpJib = dumper.isEnabled(DumpFlags.JointsInBodies);
        setChecked("dumpJib", dumpJib);

        boolean dumpJis = dumper.isEnabled(DumpFlags.JointsInSpaces);
        setChecked("dumpJis", dumpJis);

        boolean dumpMotors = dumper.isEnabled(DumpFlags.Motors);
        setChecked("dumpMotors", dumpMotors);

        boolean dumpNativeIDs = dumper.isEnabled(DumpFlags.NativeIDs);
        setChecked("dumpNativeIDs", dumpNativeIDs);

        boolean dumpPcos = dumper.isEnabled(DumpFlags.Pcos);
        setChecked("dumpPcos", dumpPcos);

        String indentIncrement = dumper.indentIncrement();
        int numSpaces = indentIncrement.length();
        String text = Integer.toString(numSpaces);
        setButtonText("dumpIndent2", text);

        DemoState demoState = MavDemo2.getDemoState();
        Prop selectedProp = demoState.getSelectedProp();
        text = "";
        if (selectedProp != null) {
            text = selectedProp.getName();
        }
        setButtonText("dumpPhysicsProp", text);

        Vehicle selectedVehicle = demoState.getVehicles().getSelected();
        text = "";
        if (selectedVehicle != null) {
            text = selectedVehicle.getName();
        }
        setButtonText("dumpPhysicsVehicle", text);
    }
}
