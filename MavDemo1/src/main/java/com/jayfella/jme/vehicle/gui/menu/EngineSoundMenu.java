package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Sound;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound1;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound2;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound4;
import com.jayfella.jme.vehicle.examples.sounds.EngineSound5;
import com.jayfella.jme.vehicle.gui.lemur.AudioHud;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to select an engine sound.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class EngineSoundMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(EngineSoundMenu.class.getName());
    // *************************************************************************
    // fields

    /**
     * selected Sound, or null for silence
     */
    private Sound selectedSound;
    // *************************************************************************
    // AnimatedMenuState methods

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    @Override
    protected List<Button> createItems() {
        List<Button> result = new ArrayList<>(6);

        addButton(result, "Engine-1", source -> setSound(new EngineSound1()));
        addButton(result, "Engine-2", source -> setSound(new EngineSound2()));
        addButton(result, "Engine-4", source -> setSound(new EngineSound4()));
        addButton(result, "Engine-5", source -> setSound(new EngineSound5()));
        addButton(result, "Silence", source -> setSound(null));
        addButton(result, "<< Back",
                source -> animateOut(() -> goTo(new CustomizationMenu())));

        return result;
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        if (selectedSound != null) {
            selectedSound.mute();
            selectedSound.detach();
        }
        MavDemo1.getVehicle().getEngine().setSound(selectedSound);
        selectedSound = null;

        super.onDisable();
    }

    /**
     * Callback invoked whenever this menu becomes both attached and enabled.
     */
    @Override
    protected void onEnable() {
        super.onEnable();

        Sound engineSound = MavDemo1.getVehicle().getEngine().getSound();
        if (engineSound == null) {
            selectedSound = null;
        } else {
            try {
                Class<? extends Sound> clazz = engineSound.getClass();
                selectedSound = clazz.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException exception) {
                throw new RuntimeException(exception);
            }
            AssetManager assetManager
                    = MavDemo1.getApplication().getAssetManager();
            selectedSound.load(assetManager);
            configureSelectedSound();

            Node node = MavDemo1.getVehicle().getNode();
            selectedSound.attachTo(node);
        }
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

        if (selectedSound != null) {
            configureSelectedSound(); // in case the effective volume changed
        }
    }
    // *************************************************************************
    // private methods

    /**
     * Configure the selected Sound and attach it to the scene graph.
     */
    private void configureSelectedSound() {
        float pitch = 60f; // 3600 RPM
        float volume = getState(AudioHud.class).effectiveVolume();
        selectedSound.setPitchAndVolume(pitch, volume);
    }

    /**
     * Alter which Sound is selected.
     *
     * @param newSound the desired Sound (not loaded, alias created) or null for
     * silence
     */
    private void setSound(Sound newSound) {
        if (selectedSound != null) {
            selectedSound.mute();
            selectedSound.detach();
        }

        selectedSound = newSound;
        if (newSound != null) {
            AssetManager assetManager
                    = MavDemo1.getApplication().getAssetManager();
            newSound.load(assetManager);
            configureSelectedSound();

            Node node = MavDemo1.getVehicle().getNode();
            selectedSound.attachTo(node);
        }
    }
}
