package com.jayfella.jme.vehicle.gui.menu;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;
import java.util.List;
import java.util.logging.Logger;
import jme3utilities.math.MyMath;

/**
 * A Lemur menu whose buttons slide in and out of the display.
 *
 * Derived from the AnimatedMenuState class in the Advanced Vehicles project.
 */
abstract class AnimatedMenu extends BaseAppState {
    // *************************************************************************
    // constants and loggers

    /**
     * delay between successive button animations (in seconds)
     */
    final private static float buttonDelay = 0.05f;
    /**
     * duration of each button animation (in seconds)
     */
    final private static float duration = 0.2f;
    /**
     * delay before the first animation starts (in seconds)
     */
    final private static float startupDelay = 0.1f;
    /**
     * message logger for this class
     */
    final public static Logger logger0
            = Logger.getLogger(AnimatedMenu.class.getName());
    // *************************************************************************
    // fields

    private AnimCompleteEvent completeEvent;
    private boolean allComplete = false;
    private boolean in = true;
    /**
     * width of the widest button
     */
    private float maxWidth;
    /**
     * elapsed startup delay (in seconds)
     */
    private float startupTime = 0f;
    /**
     * elapsed time since the animation started (in seconds)
     */
    private float time = 0f;
    /**
     * buttons in this menu
     */
    private List<Button> buttons;
    final private Node node = new Node("Menu");
    // *************************************************************************
    // new protected methods

    /**
     * Convenience method to create a GUI Button and add it to the specified
     * list.
     *
     * @param list the list to extend (not null, modified)
     * @param label the text to display on the Button
     * @param command the command to execute when the Button is clicked
     */
    protected static void addButton(List<? super Button> list, String label,
            Command<? super Button> command) {
        Button button = new Button(label);
        button.addClickCommands(command);
        list.add(button);
    }

    protected void animateOut(AnimCompleteEvent animComplete) {
        this.time = 0f;
        this.allComplete = false;
        this.in = false;
        this.completeEvent = animComplete;
    }

    /**
     * Generate the buttons for this menu.
     *
     * @return a new array of GUI buttons
     */
    abstract protected List<Button> createItems();

    /**
     * Detach this menu and attach the specified AppState.
     *
     * @param next what to attach (not null)
     */
    protected void goTo(AppState next) {
        AppStateManager stateManager = getStateManager();
        stateManager.attach(next);
        stateManager.detach(this);
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
     * Callback invoked after this menu is attached but before onEnable().
     *
     * @param application the application instance (not null)
     */
    @Override
    protected void initialize(Application application) {
        this.buttons = createItems();

        for (Button button : buttons) {
            formatButton(button);
            node.attachChild(button);

            // Find the widest button, so we can move them all offscreen.
            float width = button.getPreferredSize().x;
            if (width > maxWidth) {
                this.maxWidth = width;
            }
        }

        float height = application.getCamera().getHeight() - 20f;
        for (Button button : buttons) {
            // make all the buttons the same width
            Vector3f preferredSize = button.getPreferredSize();
            preferredSize.x = maxWidth;
            button.setPreferredSize(preferredSize);

            // position them all one below the other.
            button.setLocalTranslation(-maxWidth, height, 1f);
            height -= preferredSize.y;
        }
    }

    /**
     * Callback invoked whenever this AppState ceases to be both attached and
     * enabled.
     */
    @Override
    protected void onDisable() {
        node.removeFromParent();
    }

    /**
     * Callback invoked whenever this menu becomes both attached and enabled.
     */
    @Override
    protected void onEnable() {
        SimpleApplication simpleApp = (SimpleApplication) getApplication();
        simpleApp.getGuiNode().attachChild(node);
    }

    /**
     * Callback to update this AppState, invoked once per frame when the
     * AppState is both attached and enabled.
     *
     * @param tpf the time interval between frames (in seconds, &ge;0)
     */
    @Override
    public void update(float tpf) {
        super.update(tpf);
        if (allComplete) {
            return;
        }

        // There's a delay before the first button starts moving. It looks nicer.
        this.startupTime += tpf;
        if (startupTime < startupDelay) {
            return;
        }

        // prevent time from growing endlessly
        this.time = FastMath.clamp(time + tpf, 0f, 100f);

        int numButtons = buttons.size();
        for (int i = 0; i < numButtons; ++i) {
            float currentDelay = buttonDelay * i;
            // make each button wait its turn
            if (time < currentDelay) {
                continue;
            }

            float easeTime = FastMath.clamp(time - currentDelay, 0f, duration);
            Button button = buttons.get(i);

            Vector3f translation = button.getLocalTranslation();
            float x;
            if (in) {
                x = MyMath.easeInQuartic(easeTime, -maxWidth, 20f, duration);
            } else {
                x = MyMath.easeOutQuartic(easeTime, 20f, -maxWidth, duration);
            }
            button.setLocalTranslation(x, translation.y, translation.z);

            if (i == numButtons - 1 && easeTime == duration) {
                this.allComplete = true;

                if (completeEvent != null) {
                    completeEvent.completed();
                    this.completeEvent = null;
                }
            }
        }
    }
    // *************************************************************************
    // private methods

    private static void formatButton(Button button) {
        button.setTextHAlignment(HAlignment.Center);
        ((TbtQuadBackgroundComponent) button.getBackground()).setMargin(10f, 5f);
        button.setFontSize(16f);
        button.setInsets(new Insets3f(0f, 0f, 5f, 0f));
    }
}
