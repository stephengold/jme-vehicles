package com.jayfella.jme.vehicle.niftydemo.tool;

import com.jayfella.jme.vehicle.niftydemo.MainHud;
import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jme3.app.state.AppStateManager;
import de.lessvoid.nifty.elements.Element;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.MyString;
import jme3utilities.Validate;
import jme3utilities.nifty.Tool;

/**
 * Tools in the main HUD of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class Tools {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Tools.class.getName());
    // *************************************************************************
    // fields

    final private AudioTool audio;
    final private CameraTool camera;
    final private DrivingTool driving;
    final private DumpPhysicsTool dumpPhysics;
    final private DumpSceneTool dumpScene;
    final private PhysicsTool physics;
    final private PropProposalTool propProposal;
    final private ToolsTool tools;
    final private ViewTool view;

    final private Tool[] allTools;
    // *************************************************************************
    // constructors

    /**
     * Instantiate all the tools in the specified screen.
     *
     * @param screenController the screen's controller (not null)
     */
    public Tools(MainHud screenController) {
        Validate.nonNull(screenController, "screen controller");

        this.audio = new AudioTool(screenController);
        this.camera = new CameraTool(screenController);
        this.driving = new DrivingTool(screenController);
        this.dumpPhysics = new DumpPhysicsTool(screenController);
        this.dumpScene = new DumpSceneTool(screenController);
        this.physics = new PhysicsTool(screenController);
        this.propProposal = new PropProposalTool(screenController);

        this.tools = new ToolsTool(screenController);
        tools.setEnabled(true);

        this.view = new ViewTool(screenController);

        this.allTools = new Tool[]{
            audio, camera, driving, dumpPhysics, dumpScene,
            physics, propProposal, tools, view
        };
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Attach all tool controllers to the specified state manager.
     *
     * @param stateManager (not null, modified)
     */
    public void attachAll(AppStateManager stateManager) {
        stateManager.attachAll(allTools);
        camera.setEnabled(true);
        driving.setEnabled(true);
    }

    /**
     * Describe the location of the named Tool.
     *
     * @param toolName which Tool to describe (not null, not empty)
     * @return textual description
     */
    String describeLocation(String toolName) {
        Validate.nonEmpty(toolName, "tool name");

        String result = "??";
        MainHud screen = MavDemo2.findAppState(MainHud.class);
        Tool tool = screen.findTool(toolName);
        if (tool == null) {
            String message = String.format("unimplemented feature (tool = %s)",
                    MyString.quote(toolName));
            logger.log(Level.WARNING, message);
        } else {
            Element element = tool.getElement();
            int x = element.getX();
            int y = element.getY();
            result = String.format("x=\"%d\" y=\"%d\"", x, y);
        }

        return result;
    }

    /**
     * Test whether the named Tool is enabled.
     *
     * @param toolName which Tool to show or hide (not null, not empty)
     * @return true if enabled, otherwise false
     */
    public static boolean isEnabled(String toolName) {
        Validate.nonEmpty(toolName, "tool name");

        boolean result = false;
        MainHud screen = MavDemo2.findAppState(MainHud.class);
        Tool tool = screen.findTool(toolName);
        if (tool == null) {
            String message = String.format("unimplemented feature (tool = %s)",
                    MyString.quote(toolName));
            logger.log(Level.WARNING, message);
        } else {
            result = tool.isEnabled();
        }

        return result;
    }

    /**
     * Select the named Tool.
     *
     * @param toolName which Tool to select (not null, not empty)
     */
    public void select(String toolName) {
        Validate.nonEmpty(toolName, "tool name");

        MainHud screen = MavDemo2.findAppState(MainHud.class);
        Tool tool = screen.findTool(toolName);
        if (tool == null) {
            String message = String.format("unimplemented feature (tool = %s)",
                    MyString.quote(toolName));
            logger.log(Level.WARNING, message);
        } else {
            tool.select();
        }
    }

    /**
     * Show or hide all tools.
     *
     * @param newState the desired state
     */
    public void setAllEnabled(boolean newState) {
        for (Tool tool : allTools) {
            tool.setEnabled(newState);
        }
    }

    /**
     * Show or hide the named Tool without selecting it.
     *
     * @param toolName which Tool to show or hide (not null, not empty)
     * @param newState the desired state
     */
    void setEnabled(String toolName, boolean newState) {
        Validate.nonEmpty(toolName, "tool name");

        MainHud screen = MavDemo2.findAppState(MainHud.class);
        Tool tool = screen.findTool(toolName);
        if (tool == null) {
            String message = String.format("unimplemented feature (tool = %s)",
                    MyString.quote(toolName));
            logger.log(Level.WARNING, message);
        } else {
            tool.setEnabled(newState);
        }
    }
}
