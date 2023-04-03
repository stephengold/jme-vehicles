package com.jayfella.jme.vehicle.niftydemo.tool;

import com.jayfella.jme.vehicle.Prop;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.minie.PhysicsDumper;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;

/**
 * The controller for the "Dump Scene" tool in the main HUD of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class DumpSceneTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(DumpSceneTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    DumpSceneTool(GuiScreenController screenController) {
        super(screenController, "dumpScene");
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
        result.add("dumpBuckets");
        result.add("dumpCullHints");
        result.add("dumpMatParams");
        result.add("dumpMpos");
        result.add("dumpShadows");
        result.add("dumpTransforms");
        result.add("dumpUserData");

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
            case "dumpBuckets":
                dumper.setDumpBucket(isChecked);
                break;
            case "dumpCullHints":
                dumper.setDumpCull(isChecked);
                break;
            case "dumpMatParams":
                dumper.setDumpMatParam(isChecked);
                break;
            case "dumpMpos":
                dumper.setDumpOverride(isChecked);
                break;
            case "dumpShadows":
                dumper.setDumpShadow(isChecked);
                break;
            case "dumpTransforms":
                dumper.setDumpTransform(isChecked);
                break;
            case "dumpUserData":
                dumper.setDumpUser(isChecked);
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

        boolean dumpBuckets = dumper.isDumpBucket();
        setChecked("dumpBuckets", dumpBuckets);

        boolean dumpCullHints = dumper.isDumpCull();
        setChecked("dumpCullHints", dumpCullHints);

        boolean dumpMatParams = dumper.isDumpMatParam();
        setChecked("dumpMatParams", dumpMatParams);

        boolean dumpMpo = dumper.isDumpOverride();
        setChecked("dumpMpos", dumpMpo);

        boolean dumpShadows = dumper.isDumpShadow();
        setChecked("dumpShadows", dumpShadows);

        boolean dumpTransforms = dumper.isDumpTransform();
        setChecked("dumpTransforms", dumpTransforms);

        boolean dumpUserData = dumper.isDumpUser();
        setChecked("dumpUserData", dumpUserData);

        String indentIncrement = dumper.indentIncrement();
        int numSpaces = indentIncrement.length();
        String text = Integer.toString(numSpaces);
        setButtonText("dumpIndent", text);

        int maxChildren = dumper.maxChildren();
        if (maxChildren == Integer.MAX_VALUE) {
            text = "All";
        } else {
            text = Integer.toString(maxChildren);
        }
        setButtonText("dumpMaxChildren", text);

        Prop selectedProp = MavDemo2.getDemoState().getSelectedProp();
        text = "";
        if (selectedProp != null) {
            text = selectedProp.getName();
        }
        setButtonText("dumpSceneProp", text);

        Vehicle selectedVehicle
                = MavDemo2.getDemoState().getVehicles().getSelected();
        text = "";
        if (selectedVehicle != null) {
            text = selectedVehicle.getName();
        }
        setButtonText("dumpSceneVehicle", text);
    }
}
