package com.jayfella.jme.vehicle.niftydemo.view;

/**
 * Enumerate the performance-monitoring modes in MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public enum PerformanceMode {
    // *************************************************************************
    // values

    /**
     * don't display any performance statistics
     */
    Off,
    /**
     * display frames per second and counts of objects rendered using JME's
     * StatsAppState
     */
    JmeStats,
    /**
     * display maximum latency using the PerformanceAppState from the
     * Jme3-utilities project
     */
    DebugPas
}
