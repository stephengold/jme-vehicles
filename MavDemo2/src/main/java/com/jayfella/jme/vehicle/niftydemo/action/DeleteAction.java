package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import java.util.logging.Logger;

/**
 * Process actions that start with the word "delete".
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class DeleteAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(DeleteAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private DeleteAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "delete".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;
        DemoState demoState = MavDemo2.getDemoState();

        switch (actionString) {
            case Action.deleteAllProps:
                demoState.deleteAllProps();
                break;

            case Action.deleteProp:
                demoState.getSelectedProp().removeFromWorld();
                break;

            default:
                handled = false;
        }

        return handled;
    }
}
