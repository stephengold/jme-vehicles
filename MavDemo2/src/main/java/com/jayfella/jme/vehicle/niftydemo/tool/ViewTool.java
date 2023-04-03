package com.jayfella.jme.vehicle.niftydemo.tool;

import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.view.View;
import com.jayfella.jme.vehicle.niftydemo.view.ViewFlags;
import com.jme3.bullet.BulletAppState;
import com.jme3.renderer.Renderer;
import com.jme3.renderer.opengl.GLRenderer;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;

/**
 * The controller for the "View" tool in the main HUD of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class ViewTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ViewTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    ViewTool(GuiScreenController screenController) {
        super(screenController, "view");
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
        result.add("viewPhysicsJoints");
        result.add("viewPropShapes");
        result.add("viewPropSpheres");
        result.add("viewShadows");
        result.add("viewVehiclePoints");
        result.add("viewVehicleShapes");
        result.add("viewVehicleSpheres");
        result.add("viewWorldShapes");

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
        View view = MavDemo2.findAppState(View.class);

        switch (boxName) {
            case "viewPhysicsJoints":
                view.setEnabled(ViewFlags.PhysicsJoints, isChecked);
                break;

            case "viewPropShapes":
                view.setEnabled(ViewFlags.PropShapes, isChecked);
                break;

            case "viewPropSpheres":
                view.setEnabled(ViewFlags.PropSpheres, isChecked);
                break;

            case "viewShadows":
                view.setEnabled(ViewFlags.Shadows, isChecked);
                break;

            case "viewVehiclePoints":
                view.setEnabled(ViewFlags.VehiclePoints, isChecked);
                break;

            case "viewVehicleShapes":
                view.setEnabled(ViewFlags.VehicleShapes, isChecked);
                break;

            case "viewVehicleSpheres":
                view.setEnabled(ViewFlags.VehicleSpheres, isChecked);
                break;

            case "viewWorldShapes":
                view.setEnabled(ViewFlags.WorldShapes, isChecked);
                break;

            default:
                super.onCheckBoxChanged(boxName, isChecked);
        }
    }

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        View view = MavDemo2.findAppState(View.class);
        boolean flag;

        flag = view.isEnabled(ViewFlags.PhysicsJoints);
        setChecked("viewPhysicsJoints", flag);

        flag = view.isEnabled(ViewFlags.PropShapes);
        setChecked("viewPropShapes", flag);

        flag = view.isEnabled(ViewFlags.PropSpheres);
        setChecked("viewPropSpheres", flag);

        flag = view.isEnabled(ViewFlags.Shadows);
        setChecked("viewShadows", flag);

        flag = view.isEnabled(ViewFlags.VehiclePoints);
        setChecked("viewVehiclePoints", flag);

        flag = view.isEnabled(ViewFlags.VehicleShapes);
        setChecked("viewVehicleShapes", flag);

        flag = view.isEnabled(ViewFlags.VehicleSpheres);
        setChecked("viewVehicleSpheres", flag);

        flag = view.isEnabled(ViewFlags.WorldShapes);
        setChecked("viewWorldShapes", flag);

        Renderer renderer = MavDemo2.getApplication().getRenderer();
        GLRenderer glRenderer = (GLRenderer) renderer;
        int degree = glRenderer.getDefaultAnisotropicFilter();
        String anisoStatus = Integer.toString(degree);
        setButtonText("viewDefaultAniso", anisoStatus);

        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        float axesLength = bas.debugAxisLength();
        String axesLengthStatus = String.format("%.0f cm", 100f * axesLength); // TODO wuToCm
        setButtonText("viewPhysicsAxes", axesLengthStatus);

        Sky sky = view.getSky();
        String skyName = sky.getClass().getSimpleName();
        setButtonText("viewSky", skyName);
    }
}
