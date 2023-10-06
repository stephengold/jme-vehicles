package com.jayfella.jme.vehicle.niftydemo.tool;

import com.jayfella.jme.vehicle.niftydemo.MavDemo2;
import com.jayfella.jme.vehicle.niftydemo.state.DemoState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import java.util.logging.Logger;
import jme3utilities.nifty.GuiScreenController;
import jme3utilities.nifty.Tool;

/**
 * The controller for the "Physics" tool in the main HUD of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
class PhysicsTool extends Tool {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(PhysicsTool.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an uninitialized tool.
     *
     * @param screenController the controller of the screen that will contain
     * the tool (not null)
     */
    PhysicsTool(GuiScreenController screenController) {
        super(screenController, "physics");
    }
    // *************************************************************************
    // Tool methods

    /**
     * Update this tool prior to rendering. (Invoked once per frame while this
     * tool is displayed.)
     */
    @Override
    protected void toolUpdate() {
        BulletAppState bas = MavDemo2.findAppState(BulletAppState.class);
        PhysicsSpace space = bas.getPhysicsSpace();

        int numRigids = space.getRigidBodyList().size();
        String numRigidsText = Integer.toString(numRigids);
        setStatusText("physicsPrbCount", numRigidsText);

        int numActive = 0;
        for (PhysicsRigidBody body : space.getRigidBodyList()) {
            if (body.isDynamic() && body.isActive()) {
                ++numActive;
            }
        }
        String numActiveText = Integer.toString(numActive);
        setStatusText("physicsActiveCount", numActiveText);

        int numJoints = space.countJoints();
        String numJointsText = Integer.toString(numJoints);
        setStatusText("physicsJointCount", numJointsText);

        float simulationSpeed = bas.getSpeed();
        String speedText;
        if (simulationSpeed == 0f) {
            speedText = "paused";
        } else {
            speedText = String.format("%.0f%%", 100f * simulationSpeed);
        }
        setButtonText("physicsSpeed", speedText);

        DemoState demoState = MavDemo2.getDemoState();
        double elapsedTime = demoState.elapsedTime();
        int minutes = (int) Math.floor(elapsedTime / 60);
        double residue = elapsedTime - 60 * minutes;
        int seconds = (int) Math.floor(residue);
        int milliseconds = (int) Math.floor(1000 * (residue - seconds));
        String elapsedText
                = String.format("%d:%02d.%03d", minutes, seconds, milliseconds);
        setButtonText("physicsElapsedTime", elapsedText);

        float timestep = space.getAccuracy();
        String timestepText = String.format("%.2f ms", 1000f * timestep);
        setButtonText("physicsTimestep", timestepText);

        float tickDuration = demoState.tickDuration();
        float load = tickDuration / timestep;
        String loadText = String.format("%.0f%%", 100f * load);
        setStatusText("physicsLoad", loadText);

        int numIterations = space.getSolverNumIterations();
        String iterationsText = Integer.toString(numIterations);
        setButtonText("physicsIterations", iterationsText);

        float margin = CollisionShape.getDefaultMargin();
        String marginText = String.format("%.0f mm", 1000f * margin);
        setButtonText("physicsMargin", marginText);
    }
}
