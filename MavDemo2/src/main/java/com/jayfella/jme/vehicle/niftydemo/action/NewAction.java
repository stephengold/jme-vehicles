package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import com.jme3.app.state.ScreenshotAppState;
import java.util.logging.Logger;

/**
 * Process actions that start with the word "new".
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class NewAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(NewAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private NewAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process a non-ongoing action that starts with the word "new".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processNotOngoing(String actionString) {
        boolean handled = true;

        switch (actionString) {
            case Action.newProp:
                DemoState demoState = MavDemo2.getDemoState();
                demoState.addProp();
                break;

            default:
                handled = false;
        }

        return handled;
    }

    /**
     * Process an ongoing action that starts with the word "new".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;

        switch (actionString) {
            case Action.newProp:
                MavDemo2.getDemoState().getPropProposal().setActive(true);
                break;

            case Action.newScreenShot:
                ScreenshotAppState screenshotAppState
                        = MavDemo2.findAppState(ScreenshotAppState.class);
                screenshotAppState.takeScreenshot();
                break;

            default:
                handled = false;
        }

        return handled;
    }
}
