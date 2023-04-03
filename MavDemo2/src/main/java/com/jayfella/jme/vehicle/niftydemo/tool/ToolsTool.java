package com.jayfella.jme.vehicle.niftydemo.tool;

import com.jayfella.jme.vehicle.niftydemo.MainHud;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;

/**
 * The controller for the "Tools" tool in the main HUD of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class ToolsTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ToolsTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized Tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    ToolsTool(GuiScreenController screenController) {
        super(screenController, "tools");
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
        result.add("toolsAudio");
        result.add("toolsCamera");
        result.add("toolsDriving");
        result.add("toolsDumpPhysics");
        result.add("toolsDumpScene");
        result.add("toolsPhysics");
        result.add("toolsPropProposal");
        result.add("toolsView");

        return result;
    }

    /**
     * Update the MVC model based on a check-box event.
     *
     * @param boxName the name (unique id prefix) of the checkbox
     * @param isChecked the new state of the checkbox (true&rarr;checked,
     * false&rarr;unchecked)
     */
    @Override
    public void onCheckBoxChanged(String boxName, boolean isChecked) {
        String toolName = MyString.remainder(boxName, "tools");
        toolName = MyString.firstToLower(toolName);

        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        Tools.setEnabled(toolName, isChecked);
    }

    /**
     * Update this Tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);

        List<String> list = listCheckBoxes();
        for (String boxName : list) {
            String toolName = MyString.remainder(boxName, "tools");
            toolName = MyString.firstToLower(toolName);

            boolean isEnabled = Tools.isEnabled(toolName);
            setChecked(boxName, isEnabled);

            String location = Tools.describeLocation(toolName);
            String statusName = boxName + "Status";
            this.setStatusText(statusName, location);
        }
    }
}
