package com.jayfella.jme.vehicle.niftydemo.view;

/**
 * Enumerate the on/off flags supported by View.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public enum ViewFlags {
    // *************************************************************************
    // values

    /**
     * physics debug for joints
     */
    PhysicsJoints,
    /**
     * collision shapes for props
     */
    PropShapes,
    /**
     * swept spheres for props
     */
    PropSpheres,
    /**
     * shadows
     */
    Shadows,
    /**
     * targets and dash-camera frustum for the selected vehicle
     */
    VehiclePoints,
    /**
     * collision shapes for vehicles
     */
    VehicleShapes,
    /**
     * swept spheres for vehicles
     */
    VehicleSpheres,
    /**
     * collision shapes for the World
     */
    WorldShapes
}
