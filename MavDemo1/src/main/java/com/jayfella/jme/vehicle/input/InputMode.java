package com.jayfella.jme.vehicle.input;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.Button;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.StateFunctionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import jme3utilities.Validate;

/**
 * An AppState which, when enabled, handles state changes for a set of Lemur
 * input functions. New instances are disabled by default.
 *
 * @author Stephen Gold sgold@sonic.net
 */
abstract public class InputMode extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(InputMode.class.getName());
    // *************************************************************************
    // fields

    /**
     * map buttons -> functions
     */
    final private Map<Button, FunctionId> buttonToFunction = new HashMap<>(9);
    /**
     * map functions -> handlers: a null handler means "do nothing"
     */
    final private Map<FunctionId, StateFunctionListener> functionToHandler
            = new HashMap<>(25);
    /**
     * map hotkeys -> functions
     */
    final private Map<Integer, FunctionId> keyToFunction = new HashMap<>(20);
    // *************************************************************************
    // constructors

    /**
     * Instantiate a disabled InputMode to handle the specified functions.
     *
     * @param appStateId identify this AppState
     * @param functions the functions to be handled
     */
    protected InputMode(String appStateId, FunctionId... functions) {
        super(appStateId);
        super.setEnabled(false);

        // Initially, none of the functions do anything.
        for (FunctionId function : functions) {
            functionToHandler.put(function, null);
        }
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Assign the specified function to the specified Button. Allowed only when
     * the mode is disabled. Replaces any function previously assigned to the
     * Button in this mode.
     *
     * @param function the desired function (not null, alias created)
     * @param button the Button (not null, alias created)
     * @return this
     */
    public InputMode assign(FunctionId function, Button button) {
        Validate.nonNull(function, "function");
        Validate.nonNull(button, "button");
        if (!functionToHandler.containsKey(function)) {
            String message = "Function isn't handled by this mode: " + function;
            throw new IllegalArgumentException(message);
        }
        if (isEnabled()) {
            String message = "Can't modify InputMode while enabled.";
            throw new IllegalStateException(message);
        }

        buttonToFunction.put(button, function);

        return this;
    }

    /**
     * Assign the specified function to the specified hotkeys. Allowed only when
     * the InputMode is disabled. Replaces any functions previously assigned to
     * those hotkeys in this mode.
     *
     * @param function the desired function (not null, alias created)
     * @param keyCodes the codes of the hotkeys
     * @return this
     */
    public InputMode assign(FunctionId function, int... keyCodes) {
        Validate.nonNull(function, "function");
        if (!functionToHandler.containsKey(function)) {
            String message = "Function isn't handled by this mode: " + function;
            throw new IllegalArgumentException(message);
        }
        if (isEnabled()) {
            String message = "Can't modify InputMode while enabled.";
            throw new IllegalStateException(message);
        }

        for (int keyCode : keyCodes) {
            keyToFunction.put(keyCode, function);
        }

        return this;
    }
    // *************************************************************************
    // new protected methods

    /**
     * Assign the specified handler to the specified functions. Allowed only
     * when the mode is disabled. Replaces any handlers previously assigned to
     * the functions in this mode.
     *
     * @param handler the desired handler (not null, alias created)
     * @param functions the functions
     */
    final protected void assign(
            StateFunctionListener handler, FunctionId... functions) {
        Validate.nonNull(handler, "handler");
        if (isEnabled()) {
            String message = "Can't modify InputMode while enabled.";
            throw new IllegalStateException(message);
        }

        for (FunctionId function : functions) {
            if (!functionToHandler.containsKey(function)) {
                String message
                        = "Function isn't handled by this mode: " + function;
                throw new IllegalArgumentException(message);
            }
            functionToHandler.put(function, handler);
        }
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
     * Callback invoked whenever this InputMode ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();

        for (Map.Entry<FunctionId, StateFunctionListener> entry
                : functionToHandler.entrySet()) {
            StateFunctionListener listener = entry.getValue();
            if (listener != null) {
                FunctionId function = entry.getKey();
                inputMapper.removeStateListener(listener, function);
            }
        }

        for (Map.Entry<Button, FunctionId> entry
                : buttonToFunction.entrySet()) {
            Button button = entry.getKey();
            FunctionId function = entry.getValue();
            inputMapper.removeMapping(function, button);
        }

        for (Map.Entry<Integer, FunctionId> entry
                : keyToFunction.entrySet()) {
            int keyCode = entry.getKey();
            FunctionId function = entry.getValue();
            inputMapper.removeMapping(function, keyCode);
        }
    }

    /**
     * Callback invoked whenever this InputMode becomes both attached and
     * enabled.
     */
    @Override
    protected void onEnable() {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();

        for (Map.Entry<Button, FunctionId> entry
                : buttonToFunction.entrySet()) {
            Button button = entry.getKey();
            FunctionId function = entry.getValue();

            inputMapper.map(function, button);
        }

        for (Map.Entry<Integer, FunctionId> entry
                : keyToFunction.entrySet()) {
            int keyCode = entry.getKey();
            FunctionId function = entry.getValue();

            inputMapper.map(function, keyCode);
        }

        for (Map.Entry<FunctionId, StateFunctionListener> entry
                : functionToHandler.entrySet()) {
            StateFunctionListener listener = entry.getValue();
            if (listener != null) {
                FunctionId function = entry.getKey();
                inputMapper.addStateListener(listener, function);
            }
        }
    }
}
