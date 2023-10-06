package com.jayfella.jme.vehicle.niftydemo.tool;

import com.jayfella.jme.vehicle.niftydemo.view.CameraMode;
import com.jayfella.jme.vehicle.niftydemo.view.Cameras;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;

/**
 * The controller for the "Camera" tool in the main HUD of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class CameraTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(CameraTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    CameraTool(GuiScreenController screenController) {
        super(screenController, "camera");
    }
    // *************************************************************************
    // Tool methods

    /**
     * Enumerate this tool's radio buttons.
     *
     * @return a new list of names (unique id prefixes)
     */
    @Override
    protected List<String> listRadioButtons() {
        List<String> result = super.listRadioButtons();
        result.add("cameraChase");
        result.add("cameraDash");
        result.add("cameraDynamic");
        result.add("cameraOrbit");

        return result;
    }

    /**
     * Update the MVC model based on this tool's radio buttons.
     *
     * @param buttonName the name (unique id prefix) of the button (not null)
     */
    @Override
    public void onRadioButtonChanged(String buttonName) {
        switch (buttonName) {
            case "cameraChase":
                Cameras.setDesiredMode(CameraMode.Chase);
                break;

            case "cameraDash":
                Cameras.setDesiredMode(CameraMode.Dash);
                break;

            case "cameraDynamic":
                Cameras.setDesiredMode(CameraMode.Dynamic);
                break;

            case "cameraOrbit":
                Cameras.setDesiredMode(CameraMode.Orbit);
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
        CameraMode mode = Cameras.getMode();
        String name = mode.toString();
        String id = "camera" + name + "RadioButton";
        getScreenController().setRadioButton(id);
    }
}
