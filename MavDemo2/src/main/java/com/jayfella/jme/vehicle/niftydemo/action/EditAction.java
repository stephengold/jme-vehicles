package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import java.util.logging.Logger;
import jme3utilities.nifty.bind.BindScreen;
import jme3utilities.nifty.displaysettings.DsScreen;
import jme3utilities.ui.InputMode;

/**
 * Process actions that start with the word "edit".
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class EditAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(EditAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private EditAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "edit".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action has been handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;

        switch (actionString) {
            case Action.editBindings:
                editBindings();
                break;

            case Action.editDisplaySettings:
                editDisplaySettings();
                break;

            default:
                handled = false;
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Process an "edit bindings" action.
     */
    private static void editBindings() {
        BindScreen bindScreen = MavDemo2.findAppState(BindScreen.class);
        InputMode current = InputMode.getActiveMode();
        bindScreen.activate(current);
    }

    /**
     * Process an "edit displaySettings" action.
     */
    private static void editDisplaySettings() {
        DsScreen dss = MavDemo2.findAppState(DsScreen.class);
        dss.activate();
    }
}
