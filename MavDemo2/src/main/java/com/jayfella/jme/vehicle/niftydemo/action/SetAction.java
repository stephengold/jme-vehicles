package com.jayfella.jme.vehicle.niftydemo.action;

import com.jayfella.jme.vehicle.niftydemo.MainHud;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import com.jayfella.jme.vehicle.niftydemo.state.PropProposal;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.renderer.Limits;
import com.jme3.renderer.Renderer;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.nifty.dialog.AllowNull;
import jme3utilities.nifty.dialog.DialogController;
import jme3utilities.nifty.dialog.FloatDialog;
import jme3utilities.nifty.dialog.IntegerDialog;

/**
 * Process actions that start with the word "set".
 *
 * @author Stephen Gold sgold@sonic.net
 */
final class SetAction {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(SetAction.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private SetAction() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Process an ongoing action that starts with the word "set".
     *
     * @param actionString textual description of the action (not null)
     * @return true if the action is handled, otherwise false
     */
    static boolean processOngoing(String actionString) {
        boolean handled = true;

        switch (actionString) {
            case Action.setDefaultAniso:
                setDefaultAniso();
                break;

            case Action.setDumpIndentSpaces:
                setDumpIndentSpaces();
                break;

            case Action.setDumpMaxChildren:
                setDumpMaxChildren();
                break;

            case Action.setPhysicsAxes:
                setPhysicsAxes();
                break;

            case Action.setPhysicsIterations:
                setPhysicsIterations();
                break;

            case Action.setPhysicsMargin:
                setPhysicsMargin();
                break;

            case Action.setPhysicsSpeed:
                setPhysicsSpeed();
                break;

            case Action.setPhysicsTimeStep:
                setPhysicsTimeStep();
                break;

            case Action.setPropDescaledMass:
                setPropDescaledMass();
                break;

            case Action.setPropMass:
                setPropMass();
                break;

            case Action.setPropScale:
                setPropScale();
                break;

            default:
                handled = false;
        }
        if (handled) {
            return true;
        }
        handled = true;

        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        PhysicsSpace physicsSpace = bas.getPhysicsSpace();
        Renderer renderer = MavDemo2.getApplication().getRenderer();
        DemoState demoState = MavDemo2.getDemoState();
        PropProposal propProposal = demoState.getPropProposal();

        String arg;
        if (actionString.startsWith(ActionPrefix.setDefaultAniso)) {
            arg = MyString.remainder(
                    actionString, ActionPrefix.setDefaultAniso);
            int degree = Integer.parseInt(arg);
            renderer.setDefaultAnisotropicFilter(degree);

        } else if (actionString.startsWith(ActionPrefix.setDumpIndentSpaces)) {
            arg = MyString.remainder(
                    actionString, ActionPrefix.setDumpIndentSpaces);
            int numSpaces = Integer.parseInt(arg);
            setDumpIndentSpaces(numSpaces);

        } else if (actionString.startsWith(ActionPrefix.setDumpMaxChildren)) {
            arg = MyString.remainder(
                    actionString, ActionPrefix.setDumpMaxChildren);
            int maxChildren = Integer.parseInt(arg);
            MavDemo2.dumper.setMaxChildren(maxChildren);

        } else if (actionString.startsWith(ActionPrefix.setPhysicsAxes)) {
            arg = MyString.remainder(actionString, ActionPrefix.setPhysicsAxes);
            float length = Float.parseFloat(arg);
            bas.setDebugAxisLength(length);

        } else if (actionString.startsWith(ActionPrefix.setPhysicsIterations)) {
            arg = MyString.remainder(
                    actionString, ActionPrefix.setPhysicsIterations);
            int numIterations = Integer.parseInt(arg);
            physicsSpace.setSolverNumIterations(numIterations);

        } else if (actionString.startsWith(ActionPrefix.setPhysicsMargin)) {
            arg = MyString.remainder(
                    actionString, ActionPrefix.setPhysicsMargin);
            float margin = Float.parseFloat(arg);
            CollisionShape.setDefaultMargin(margin);

        } else if (actionString.startsWith(ActionPrefix.setPhysicsSpeed)) {
            arg = MyString.remainder(
                    actionString, ActionPrefix.setPhysicsSpeed);
            float speed = Float.parseFloat(arg);
            bas.setSpeed(speed);

        } else if (actionString.startsWith(ActionPrefix.setPhysicsTimeStep)) {
            arg = MyString.remainder(
                    actionString, ActionPrefix.setPhysicsTimeStep);
            float timeStep = Float.parseFloat(arg);
            physicsSpace.setAccuracy(timeStep);

        } else if (actionString.startsWith(ActionPrefix.setPropDescaledMass)) {
            arg = MyString.remainder(
                    actionString, ActionPrefix.setPropDescaledMass);
            float descaledMass = Float.parseFloat(arg);
            propProposal.setDescaledMass(descaledMass);

        } else if (actionString.startsWith(ActionPrefix.setPropMass)) {
            arg = MyString.remainder(actionString, ActionPrefix.setPropMass);
            float mass = Float.parseFloat(arg);
            propProposal.setTotalMass(mass);

        } else if (actionString.startsWith(ActionPrefix.setPropScale)) {
            arg = MyString.remainder(actionString, ActionPrefix.setPropScale);
            float scale = Float.parseFloat(arg);
            propProposal.setScale(scale);

        } else {
            handled = false;
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Display a "set defaultAniso" dialog.
     */
    private static void setDefaultAniso() {
        Renderer renderer = MavDemo2.getApplication().getRenderer();
        int degree = renderer.getDefaultAnisotropicFilter();
        String defaultText = Integer.toString(degree);

        int maxDegree = renderer.getLimits().get(Limits.TextureAnisotropy);
        DialogController controller
                = new IntegerDialog("Set", 1, maxDegree, AllowNull.No);
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showTextEntryDialog("Enter degree of filtering:",
                defaultText, ActionPrefix.setDefaultAniso, controller);
    }

    /**
     * Display a "set dumpIndentSpaces" dialog.
     */
    private static void setDumpIndentSpaces() {
        int numSpaces = MavDemo2.dumper.indentIncrement().length();
        String defaultText = Integer.toString(numSpaces);

        DialogController controller
                = new IntegerDialog("Set", 0, Integer.MAX_VALUE, AllowNull.No);
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showTextEntryDialog("Enter number of spaces:",
                defaultText, ActionPrefix.setDumpIndentSpaces, controller);
    }

    /**
     * Process a "set dumpIndentSpaces" action with an argument.
     *
     * @param numSpaces the desired number of spaces (&ge;0)
     */
    private static void setDumpIndentSpaces(int numSpaces) {
        String indent = MyString.repeat(" ", numSpaces);
        MavDemo2.dumper.setIndentIncrement(indent);
    }

    /**
     * Display a "set dumpMaxChildren" dialog.
     */
    private static void setDumpMaxChildren() {
        int maxChildren = MavDemo2.dumper.maxChildren();
        String defaultText = Integer.toString(maxChildren);

        DialogController controller
                = new IntegerDialog("Set", 0, Integer.MAX_VALUE, AllowNull.No);
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showTextEntryDialog("Enter max number of children:",
                defaultText, ActionPrefix.setDumpMaxChildren, controller);
    }

    /**
     * Display a "set physicsAxes" dialog.
     */
    private static void setPhysicsAxes() {
        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        float length = bas.debugAxisLength();
        String defaultText = Float.toString(length);

        DialogController controller
                = new FloatDialog("Set", 0f, Float.MAX_VALUE, AllowNull.No);
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showTextEntryDialog("Enter axes length, in world units:",
                defaultText, ActionPrefix.setPhysicsAxes, controller);
    }

    /**
     * Display a "set physicsIterations" dialog.
     */
    private static void setPhysicsIterations() {
        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        PhysicsSpace physicsSpace = bas.getPhysicsSpace();
        int numIterations = physicsSpace.getSolverNumIterations();
        String defaultText = Integer.toString(numIterations);

        DialogController controller
                = new IntegerDialog("Set", 1, Integer.MAX_VALUE, AllowNull.No);
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showTextEntryDialog("Enter number of solver iterations:",
                defaultText, ActionPrefix.setPhysicsIterations, controller);
    }

    /**
     * Display a "set physicsMargin" dialog.
     */
    private static void setPhysicsMargin() {
        float margin = CollisionShape.getDefaultMargin();
        String defaultText = Float.toString(margin);

        DialogController controller = new FloatDialog(
                "Set", Float.MIN_VALUE, Float.MAX_VALUE, AllowNull.No);
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showTextEntryDialog(
                "Enter margin for new shapes, in physics-space units:",
                defaultText, ActionPrefix.setPhysicsMargin, controller);
    }

    /**
     * Display a "set physicsSpeed" dialog.
     */
    private static void setPhysicsSpeed() {
        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        float speed = bas.getSpeed();
        String defaultText = Float.toString(speed);

        DialogController controller
                = new FloatDialog("Set", 0f, Float.MAX_VALUE, AllowNull.No);
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showTextEntryDialog("Enter speedup factor:",
                defaultText, ActionPrefix.setPhysicsSpeed, controller);
    }

    /**
     * Display a "set physicsTimeStep" dialog.
     */
    private static void setPhysicsTimeStep() {
        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        PhysicsSpace physicsSpace = bas.getPhysicsSpace();
        float timeStep = physicsSpace.getAccuracy();
        String defaultText = Float.toString(timeStep);

        DialogController controller = new FloatDialog(
                "Set", Float.MIN_VALUE, Float.MAX_VALUE, AllowNull.No);
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showTextEntryDialog("Enter timestep, in seconds:", defaultText,
                ActionPrefix.setPhysicsTimeStep, controller);
    }

    /**
     * Display a "set propDescaledMass" dialog.
     */
    private static void setPropDescaledMass() {
        PropProposal proposal = MavDemo2.getDemoState().getPropProposal();
        float mass = proposal.descaledMass();
        String defaultText = Float.toString(mass);

        DialogController controller = new FloatDialog(
                "Set", Float.MIN_VALUE, Float.MAX_VALUE, AllowNull.No);
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showTextEntryDialog("Enter base mass of prop, in kilograms:",
                defaultText, ActionPrefix.setPropDescaledMass, controller);
    }

    /**
     * Display a "set propMass" dialog.
     */
    private static void setPropMass() {
        PropProposal proposal = MavDemo2.getDemoState().getPropProposal();
        float mass = proposal.totalMass();
        String defaultText = Float.toString(mass);

        DialogController controller = new FloatDialog(
                "Set", Float.MIN_VALUE, Float.MAX_VALUE, AllowNull.No);
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showTextEntryDialog("Enter mass of prop, in kilograms:",
                defaultText, ActionPrefix.setPropMass, controller);
    }

    /**
     * Display a "set propScale" dialog.
     */
    private static void setPropScale() {
        PropProposal proposal = MavDemo2.getDemoState().getPropProposal();
        float scale = proposal.scaleFactor();
        String defaultText = Float.toString(scale);

        DialogController controller = new FloatDialog(
                "Set", Float.MIN_VALUE, Float.MAX_VALUE, AllowNull.No);
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showTextEntryDialog(
                "Enter scale of prop, in world units per model unit:",
                defaultText, ActionPrefix.setPropScale, controller);
    }
}
