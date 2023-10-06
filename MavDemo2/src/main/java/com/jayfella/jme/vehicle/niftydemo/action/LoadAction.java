package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.niftydemo.MainHud;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.Menus;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.nifty.PopupMenuBuilder;

/**
 * Process actions that start with the word "load".
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class LoadAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(LoadAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private LoadAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "load".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;
        switch (actionString) {
            case Action.loadSky:
                Menus.loadSky();
                break;

            case Action.loadVehicle:
                loadVehicle();
                break;

            case Action.loadWorld:
                loadWorld();
                break;

            default:
                handled = false;
        }

        if (handled) {
            return true;
        }

        String arg;
        if (actionString.startsWith(ActionPrefix.loadSky)) {
            arg = MyString.remainder(actionString, ActionPrefix.loadSky);
            handled = Menus.menuSky(arg);

        } else if (actionString.startsWith(ActionPrefix.loadVehicle)) {
            arg = MyString.remainder(actionString, ActionPrefix.loadVehicle);
            handled = Menus.menuWorld(arg);

        } else if (actionString.startsWith(ActionPrefix.loadWorld)) {
            arg = MyString.remainder(actionString, ActionPrefix.loadWorld);
            handled = Menus.menuWorld(arg);

        } else {
            handled = false;
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Handle a "load vehicle" action.
     */
    private static void loadVehicle() {
        PopupMenuBuilder builder = new PopupMenuBuilder();
        Menus.buildVehicleMenu(builder);

        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showPopupMenu(ActionPrefix.loadVehicle, builder);
    }

    /**
     * Handle a "load world" action.
     */
    private static void loadWorld() {
        PopupMenuBuilder builder = new PopupMenuBuilder();
        Menus.buildWorldMenu(builder);

        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showPopupMenu(ActionPrefix.loadWorld, builder);
    }
}
