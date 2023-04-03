package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.examples.skies.AnimatedDaySky;
import com.jayfella.jme.vehicle.examples.skies.AnimatedNightSky;
import com.jayfella.jme.vehicle.examples.skies.PurpleNebulaSky;
import com.jayfella.jme.vehicle.examples.skies.QuarrySky;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.jme3.asset.AssetManager;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to choose among the available skies.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class SkyMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SkyMenu.class.getName());
    // *************************************************************************
    // AnimatedMenuState methods

    /**
     * Generate the items for this menu.
     *
     * @return a new array of GUI buttons
     */
    @Override
    protected List<Button> createItems() {
        List<Button> result = new ArrayList<>(5);

        addButton(result, "Animated Day",
                source -> setSky(new AnimatedDaySky()));
        addButton(result, "Animated Night",
                source -> setSky(new AnimatedNightSky()));
        addButton(result, "Purple Nebula",
                source -> setSky(new PurpleNebulaSky()));
        addButton(result, "Quarry Day",
                source -> setSky(new QuarrySky()));
        addButton(result, "<< Back",
                source -> animateOut(() -> goTo(new CustomizationMenu())));

        return result;
    }
    // *************************************************************************
    // private methods

    private static void setSky(Sky sky) {
        MavDemo1 main = MavDemo1.getApplication();
        AssetManager assetManager = main.getAssetManager();
        sky.load(assetManager);

        MavDemo1.setSky(sky);
    }
}
