package com.jayfella.jme.vehicle.niftydemo.tool;

import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import com.jayfella.jme.vehicle.niftydemo.state.PropProposal;
import com.jayfella.jme.vehicle.niftydemo.state.PropType;
import java.util.logging.Logger;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;

/**
 * The controller for the "Prop Proposal" tool in the main HUD of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class PropProposalTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(PropProposalTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized Tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    PropProposalTool(GuiScreenController screenController) {
        super(screenController, "propProposal");
    }
    // *************************************************************************
    // Tool methods

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        DemoState demoState = MavDemo2.getDemoState();
        PropProposal proposal = demoState.getPropProposal();

        PropType type = proposal.type();
        String typeButton = type.toString();
        setButtonText("ppType", typeButton);

        float mass = proposal.totalMass();
        String massButton = String.format("%.2f kg", mass);
        setButtonText("ppMass", massButton);

        float scale = proposal.scaleFactor();
        String scaleButton = String.format("%.0f%%", scale * 100f);
        setButtonText("ppScale", scaleButton);
    }
}
