package com.jayfella.jme.vehicle.gui.menu;

import com.jayfella.jme.vehicle.World;
import com.jayfella.jme.vehicle.examples.worlds.EndlessPlain;
import com.jayfella.jme.vehicle.examples.worlds.Mountains;
import com.jayfella.jme.vehicle.examples.worlds.Playground;
import com.jayfella.jme.vehicle.examples.worlds.Racetrack;
import com.jayfella.jme.vehicle.lemurdemo.Main;
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

        Button button = new Button("Endless Plain");
        button.addClickCommands(source -> setWorld(new EndlessPlain()));
        result.add(button);

        button = new Button("Mountains");
        button.addClickCommands(source -> setWorld(new Mountains()));
        result.add(button);

        button = new Button("Playground");
        button.addClickCommands(source -> setWorld(new Playground()));
        result.add(button);

        button = new Button("Racetrack");
        button.addClickCommands(source -> setWorld(new Racetrack()));
        result.add(button);

        button = new Button("<< Back");
        button.addClickCommands(source -> animateOut(()
                -> goTo(new MainMenu())
        ));
        result.add(button);

        return result;
    }
    // *************************************************************************
    // private methods

    private void setWorld(World newWorld) {
        Main main = Main.getApplication();
        AssetManager assetManager = main.getAssetManager();
        newWorld.load(assetManager);

        main.setWorld(newWorld);
    }
}