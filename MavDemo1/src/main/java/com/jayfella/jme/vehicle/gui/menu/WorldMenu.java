package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.World;
import com.jayfella.jme.vehicle.examples.worlds.EndlessPlain;
import com.jayfella.jme.vehicle.examples.worlds.Mountains;
import com.jayfella.jme.vehicle.examples.worlds.Playground;
import com.jayfella.jme.vehicle.examples.worlds.Racetrack;
import com.jayfella.jme.vehicle.lemurdemo.MavDemo1;
import com.jme3.asset.AssetManager;
import com.simsilica.lemur.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An AnimatedMenu to choose among the available worlds.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class WorldMenu extends AnimatedMenu {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(WorldMenu.class.getName());
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

        addButton(result, "Endless Plain",
                source -> setWorld(new EndlessPlain()));
        addButton(result, "Mountains",
                source -> setWorld(new Mountains()));
        addButton(result, "Playground",
                source -> setWorld(new Playground()));
        addButton(result, "Racetrack",
                source -> setWorld(new Racetrack()));
        addButton(result, "<< Back",
                source -> animateOut(() -> goTo(new MainMenu())));

        return result;
    }
    // *************************************************************************
    // private methods

    private static void setWorld(World newWorld) {
        MavDemo1 main = MavDemo1.getApplication();
        AssetManager assetManager = main.getAssetManager();
        newWorld.load(assetManager);

        main.setWorld(newWorld);
    }
}
