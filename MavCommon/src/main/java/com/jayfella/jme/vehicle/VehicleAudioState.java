package com.jayfella.jme.vehicle;

import com.jayfella.jme.vehicle.gui.AudioHud;
import com.jayfella.jme.vehicle.part.Engine;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import java.util.logging.Logger;

/**
 * Manage the engine sound for a particular Vehicle.
 */
public class VehicleAudioState extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(VehicleAudioState.class.getName());
    // *************************************************************************
    // fields

    final private Vehicle vehicle;
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled AppState for the specified Vehicle.
     *
     * @param vehicle the desired Vehicle
     */
    public VehicleAudioState(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
    // *************************************************************************
    // BaseAppState methods

    /**
     * Callback invoked after this AppState is detached or during application
     * shutdown if the state is still attached. onDisable() is called before
     * this cleanup() method if the state is enabled at the time of cleanup.
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void cleanup(Application application) {
        // do nothing
    }

    /**
     * Callback invoked after this AppState is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        // do nothing
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        Sound sound = vehicle.getEngineSound();
        if (sound != null) {
            sound.mute();
        }
    }

    /**
     * Callback invoked whenever this AppState becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        // do nothing
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);

        Sound sound = vehicle.getEngineSound();
        if (sound == null) {
            return;
        }

        Engine engine = vehicle.getEngine();
        if (engine.isRunning()) {
            float pitch = engine.rpm() / 60;
            float masterVolume = AudioHud.effectiveVolume();
            sound.setPitchAndVolume(pitch, masterVolume);
        } else {
            sound.mute();
        }
    }
}