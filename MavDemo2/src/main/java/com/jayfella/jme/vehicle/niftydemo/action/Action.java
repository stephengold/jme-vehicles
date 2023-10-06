package com.jayfella.jme.vehicle.niftydemo.action;

import java.util.logging.Logger;
import jme3utilities.ui.InputMode;

/**
 * Action strings for MavDemo2's "main" screen. Each String describes a
 * user-interface action. Each String defined here should appear somewhere in
 * "main.xml" and/or "default.properties". By convention, action strings begin
 * with a verb in all lowercase and never end with a space (' ').
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class Action {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Action.class.getName());

    final static String deleteAllProps = "delete allProps";
    final static String deleteProp = "delete prop";

    final static String dumpCamera = "dump camera";
    final static String dumpPhysics = "dump physics";
    final static String dumpPhysicsSpace = "dump physicsSpace";
    final static String dumpProp = "dump prop";
    final static String dumpPropNode = "dump propNode";
    final static String dumpRenderManager = "dump renderManager";
    final static String dumpRootNode = "dump rootNode";
    final static String dumpStateManager = "dump stateManager";
    final static String dumpVehicle = "dump vehicle";
    final static String dumpVehicleNode = "dump vehicleNode";
    final static String dumpViewPort = "dump viewPort";

    final static String editBindings = "edit bindings";
    final static String editDisplaySettings = "edit displaySettings";

    final static String loadSky = "load sky";
    final static String loadVehicle = "load vehicle";
    final static String loadWorld = "load world";

    final static String newProp = "new prop";
    final static String newScreenShot = "new screenShot";

    final static String nextCamera = "next camera";
    final static String nextPerformanceMode = "next performanceMode";
    final static String nextTimeStep = "next timeStep";

    final static String pickProp = "pick prop";

    final static String resetCameraFov = "reset cameraFov";
    final static String resetCameraOffset = "reset cameraOffset";
    final static String resetElapsedTime = "reset elapsedTime";
    final static String resetPropProposal = "reset propProposal";
    final static String resetVehicle = "reset vehicle";

    final static String selectProp = "select prop";
    final static String selectPropType = "select propType";

    final static String setDefaultAniso = "set defaultAniso";
    final static String setDumpIndentSpaces = "set dumpIndentSpaces";
    final static String setDumpMaxChildren = "set dumpMaxChildren";
    final static String setPhysicsAxes = "set physicsAxes";
    final static String setPhysicsIterations = "set physicsIterations";
    final static String setPhysicsMargin = "set physicsMargin";
    final static String setPhysicsSpeed = "set physicsSpeed";
    final static String setPhysicsTimeStep = "set physicsTimeStep";
    final static String setPropDescaledMass = "set propDescaledMass";
    final static String setPropMass = "set propMass";
    final static String setPropScale = "set propScale";

    final static String toggleEngine = "toggle engine";
    final static String togglePause = "toggle pause";
    final static String togglePhysicsDebug = "toggle physicsDebug";
    final static String toggleReverse = "toggle reverse";
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Action() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Add actions to the specified InputMode without binding them.
     *
     * @param mode the InputMode to modify (not null)
     */
    public static void addUnbound(InputMode mode) {
        mode.addActionName(deleteAllProps);
        mode.addActionName(deleteProp);

        mode.addActionName(dumpCamera);
        mode.addActionName(dumpPhysics);
        mode.addActionName(dumpPhysicsSpace);
        mode.addActionName(dumpProp);
        mode.addActionName(dumpPropNode);
        mode.addActionName(dumpRenderManager);
        mode.addActionName(dumpRootNode);
        mode.addActionName(dumpStateManager);
        mode.addActionName(dumpVehicle);
        mode.addActionName(dumpVehicleNode);
        mode.addActionName(dumpViewPort);

        mode.addActionName(editBindings);
        mode.addActionName(editDisplaySettings);

        mode.addActionName(loadSky);
        mode.addActionName(loadVehicle);
        mode.addActionName(loadWorld);

        mode.addActionName(newProp);
        mode.addActionName(newScreenShot);

        mode.addActionName(nextCamera);
        mode.addActionName(nextPerformanceMode);
        mode.addActionName(nextTimeStep);

        mode.addActionName(pickProp);

        mode.addActionName(resetCameraFov);
        mode.addActionName(resetCameraOffset);
        mode.addActionName(resetElapsedTime);
        mode.addActionName(resetVehicle);

        mode.addActionName(selectProp);
        mode.addActionName(selectPropType);

        mode.addActionName(setDefaultAniso);
        mode.addActionName(setDumpIndentSpaces);
        mode.addActionName(setDumpMaxChildren);
        mode.addActionName(setPhysicsAxes);
        mode.addActionName(setPhysicsIterations);
        mode.addActionName(setPhysicsMargin);
        mode.addActionName(setPhysicsSpeed);
        mode.addActionName(setPhysicsTimeStep);
        mode.addActionName(setPropDescaledMass);
        mode.addActionName(setPropMass);
        mode.addActionName(setPropScale);

        mode.addActionName(toggleEngine);
        mode.addActionName(togglePause);
        mode.addActionName(togglePhysicsDebug);
        mode.addActionName(toggleReverse);
        // TODO signals
    }

    /**
     * Process a non-ongoing action from the GUI or keyboard.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action has been handled, otherwise false
     */
    public static boolean processNotOngoing(String actionString) {
        boolean handled = false;
        String[] words = actionString.split(" ");
        String firstWord = words[0];
        switch (firstWord) {
            case "new":
                handled = NewAction.processNotOngoing(actionString);
                break;
            default:
        }

        return handled;
    }

    /**
     * Process an ongoing action from the GUI or keyboard.
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action has been handled, otherwise false
     */
    public static boolean processOngoing(String actionString) {
        boolean handled = false;
        String[] words = actionString.split(" ");
        String firstWord = words[0];
        switch (firstWord) {
            case "delete":
                handled = DeleteAction.processOngoing(actionString);
                break;

            case "dump":
                handled = DumpAction.processOngoing(actionString);
                break;

            case "edit":
                handled = EditAction.processOngoing(actionString);
                break;

            case "load":
                handled = LoadAction.processOngoing(actionString);
                break;

            case "new":
                handled = NewAction.processOngoing(actionString);
                break;

            case "next":
                handled = NextAction.processOngoing(actionString);
                break;

            case "pick":
                handled = PickAction.processOngoing(actionString);
                break;

            case "reset":
                handled = ResetAction.processOngoing(actionString);
                break;

            case "select":
                handled = SelectAction.processOngoing(actionString);
                break;

            case "set":
                handled = SetAction.processOngoing(actionString);
                break;

            case "toggle":
                handled = ToggleAction.processOngoing(actionString);
                break;

            default:
        }

        return handled;
    }
}
