package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.Sound;
import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.WheelModel;
import com.jayfella.jme.vehicle.gui.SpeedometerState;
import com.jayfella.jme.vehicle.niftydemo.MainHud;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.Menus;
import com.jayfella.jme.vehicle.niftydemo.state.PropProposal;
import com.jayfella.jme.vehicle.niftydemo.state.PropType;
import com.jayfella.jme.vehicle.part.Wheel;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.nifty.PopupMenuBuilder;

/**
 * Process actions that start with the word "select".
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class SelectAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SelectAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private SelectAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "select".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;

        switch (actionString) {
            case Action.selectPropType:
                selectPropType();
                break;

            default:
                handled = false;
        }
        if (handled) {
            return true;
        }

        String arg;
        if (actionString.startsWith(ActionPrefix.selectAllWheelModel)) {
            arg = MyString.remainder(
                    actionString, ActionPrefix.selectAllWheelModel);
            handled = selectAllWheelModel(arg);

        } else if (actionString.startsWith(ActionPrefix.selectEngineSound)) {
            arg = MyString.remainder(
                    actionString, ActionPrefix.selectEngineSound);
            handled = selectEngineSound(arg);

        } else if (actionString.startsWith(ActionPrefix.selectMenuItem)) {
            arg = MyString.remainder(actionString, ActionPrefix.selectMenuItem);
            handled = Menus.selectMenuItem(arg);

        } else if (actionString.startsWith(ActionPrefix.selectPropType)) {
            arg = MyString.remainder(actionString, ActionPrefix.selectPropType);
            handled = selectPropType(arg);

        } else if (actionString.startsWith(
                ActionPrefix.selectSpeedometerUnits)) {
            arg = MyString.remainder(
                    actionString, ActionPrefix.selectSpeedometerUnits);
            handled = selectSpeedometerUnits(arg);

        } else if (actionString.startsWith(ActionPrefix.selectTireSmokeColor)) {
            arg = MyString.remainder(
                    actionString, ActionPrefix.selectTireSmokeColor);
            handled = selectTireSmokeColor(arg);

        } else {
            handled = false;
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Process a "select allWheelModel " action with an argument.
     *
     * @param arg the action argument (not null)
     * @return true if the action was handled, otherwise false
     */
    @SuppressWarnings("unchecked")
    private static boolean selectAllWheelModel(String arg) {
        String name = arg.replace(" ", "") + "Wheel";
        String className
                = "com.jayfella.jme.vehicle.examples.wheels." + name;
        Class<? extends WheelModel> clazz;
        try {
            clazz = (Class<? extends WheelModel>) Class.forName(className);
        } catch (ReflectiveOperationException exception) {
            logger.severe(exception.toString());
            return false;
        }

        Vehicle vehicle = MavDemo2.getDemoState().getVehicles().getSelected();
        int numWheels = vehicle.countWheels();
        for (int wheelIndex = 0; wheelIndex < numWheels; ++wheelIndex) {
            vehicle.setWheelModel(wheelIndex, clazz);
        }
        return true;
    }

    /**
     * Process a "select engineSound " action with an argument.
     *
     * @param arg the action argument (not null)
     * @return true if the action was handled, otherwise false
     */
    @SuppressWarnings("unchecked")
    private static boolean selectEngineSound(String arg) {
        Sound selectedSound;
        if (arg.equals("Silence")) {
            selectedSound = null;
        } else {
            String name = arg.replace("-", "Sound");
            String className
                    = "com.jayfella.jme.vehicle.examples.sounds." + name;
            try {
                Class<? extends Sound> clazz
                        = (Class<? extends Sound>) Class.forName(className);
                selectedSound = clazz.getConstructor().newInstance();
            } catch (ReflectiveOperationException exception) {
                logger.severe(exception.toString());
                return false;
            }

            AssetManager assetManager
                    = MavDemo2.getApplication().getAssetManager();
            selectedSound.load(assetManager);
        }

        Vehicle vehicle = MavDemo2.getDemoState().getVehicles().getSelected();
        vehicle.getEngine().setSound(selectedSound);

        return true;
    }

    /**
     * Display a menu to set the type of the proposed Prop using the "select
     * propType " action prefix.
     */
    private static void selectPropType() {
        PopupMenuBuilder builder = new PopupMenuBuilder();

        PropProposal proposal = MavDemo2.getDemoState().getPropProposal();
        PropType selectedType = proposal.type();
        for (PropType type : PropType.values()) {
            if (type != selectedType) {
                String name = type.toString();
                builder.add(name);
            }
        }

        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showPopupMenu(ActionPrefix.selectPropType, builder);
    }

    /**
     * Process a "select propType" action with an argument.
     *
     * @param argument the argument text (not null, not empty)
     * @return true
     */
    private static boolean selectPropType(String argument) {
        PropType type = PropType.valueOf(argument);
        PropProposal proposal = MavDemo2.getDemoState().getPropProposal();
        proposal.setType(type);

        return true;
    }

    /**
     * Process a "select speedometerUnits" action with an argument.
     *
     * @param argument the argument text (not null, not empty)
     * @return true
     */
    private static boolean selectSpeedometerUnits(String argument) {
        SpeedUnit newUnits = null;
        if (!argument.equals("None")) {
            newUnits = SpeedUnit.valueOf(argument);
        }

        Vehicle vehicle = MavDemo2.getDemoState().getVehicles().getSelected();
        SpeedUnit oldUnits = vehicle.getSpeedometerUnits();
        if (newUnits != oldUnits) {
            AppStateManager mgr = MavDemo2.getApplication().getStateManager();

            SpeedometerState oldState = mgr.getState(SpeedometerState.class);
            if (oldState != null) {
                mgr.detach(oldState);
            }

            if (newUnits != null) {
                SpeedometerState newState
                        = new SpeedometerState(vehicle, newUnits);
                mgr.attach(newState);
            }
            vehicle.setSpeedometerUnits(newUnits);
        }

        return true;
    }

    /**
     * Process a "select tireSmokeColor" action with an argument.
     *
     * @param argument the argument text (not null, not empty)
     * @return true if successful, otherwise false
     */
    private static boolean selectTireSmokeColor(String argument) {
        boolean enableSmoke = true;
        ColorRGBA color = new ColorRGBA();
        switch (argument) {
            case "Black":
                color.set(0f, 0f, 0f, 1f);
                break;
            case "Blue":
                color.set(0f, 0f, 1f, 1f);
                break;
            case "Gray":
                color.set(0.6f, 0.6f, 0.6f, 1f);
                break;
            case "Green":
                color.set(0f, 1f, 0f, 1f);
                break;
            case "None":
                enableSmoke = false;
                break;
            case "Red":
                color.set(1f, 0f, 0f, 1f);
                break;
            case "White":
                color.set(1f, 1f, 1f, 1f);
                break;
            case "Yellow":
                color.set(1f, 1f, 0f, 1f);
                break;

            default:
                logger.log(Level.SEVERE, "unknown color name:  {0}",
                        MyString.quote(argument));
                return false;
        }

        Vehicle vehicle = MavDemo2.getDemoState().getVehicles().getSelected();
        for (Wheel wheel : vehicle.listWheels()) {
            wheel.setTireSmokeColor(color);
        }
        vehicle.setTireSmokeEnabled(enableSmoke);

        return true;
    }
}
