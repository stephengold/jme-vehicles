package com.jayfella.jme.vehicle.niftydemo.action;

import java.util.logging.Logger;

/**
 * Action-string prefixes for MavDemo2's "main" screen. Each prefix describes a
 * user-interface action requiring one or more (textual) arguments. By
 * convention, action prefixes end with a space (' ').
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class ActionPrefix {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(ActionPrefix.class.getName());
    /**
     * argument is the name of a Sky
     */
    final public static String loadSky = "load sky ";
    /**
     * argument is the name of a Vehicle
     */
    final static String loadVehicle = "load vehicle ";
    /**
     * argument is the name of a World
     */
    final static String loadWorld = "load world ";
    /**
     * argument is a WheelModel name
     */
    final public static String selectAllWheelModel = "select allWheelModel ";
    /**
     * argument is a Sound name or "Silence"
     */
    final public static String selectEngineSound = "select engineSound ";
    /**
     * argument is the menu path of a menu item
     */
    final public static String selectMenuItem = "select menuItem ";
    /**
     * argument is a PropCgm name
     */
    final static String selectPropType = "select propType ";
    /**
     * argument is a SpeedUnit name or "None"
     */
    final public static String selectSpeedometerUnits
            = "select speedometerUnits ";
    /**
     * argument is a color name or "None"
     */
    final public static String selectTireSmokeColor = "select tireSmokeColor ";
    /**
     * argument is a positive integer
     */
    final static String setDefaultAniso = "set defaultAniso ";
    /**
     * argument is a non-negative integer
     */
    final static String setDumpIndentSpaces = "set dumpIndentSpaces ";
    /**
     * argument is a non-negative integer
     */
    final static String setDumpMaxChildren = "set dumpMaxChildren ";
    /**
     * argument is a non-negative number
     */
    final static String setPhysicsAxes = "set physicsAxes ";
    /**
     * argument is a positive integer
     */
    final static String setPhysicsIterations = "set physicsIterations ";
    /**
     * argument is a positive number
     */
    final static String setPhysicsMargin = "set physicsMargin ";
    /**
     * argument is a non-negative number
     */
    final static String setPhysicsSpeed = "set physicsSpeed ";
    /**
     * argument is a positive number
     */
    final static String setPhysicsTimeStep = "set physicsTimeStep ";
    /**
     * argument is a positive number
     */
    final static String setPropDescaledMass = "set propDescaledMass ";
    /**
     * argument is a positive number
     */
    final static String setPropMass = "set propMass ";
    /**
     * argument is a positive number
     */
    final static String setPropScale = "set propScale ";
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private ActionPrefix() {
    }
}
