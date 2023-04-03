package com.jayfella.jme.vehicle.niftydemo;

import com.github.stephengold.garrett.GarrettVersion;
import com.github.stephengold.jmepower.JmePowerVersion;
import com.jayfella.jme.vehicle.Sky;
import com.jayfella.jme.vehicle.SpeedUnit;
import com.jayfella.jme.vehicle.Vehicle;
import com.jayfella.jme.vehicle.World;
import com.jayfella.jme.vehicle.examples.Attribution;
import com.jayfella.jme.vehicle.examples.skies.AnimatedDaySky;
import com.jayfella.jme.vehicle.examples.skies.AnimatedNightSky;
import com.jayfella.jme.vehicle.examples.skies.PurpleNebulaSky;
import com.jayfella.jme.vehicle.examples.skies.QuarrySky;
import com.jayfella.jme.vehicle.examples.vehicles.ClassicMotorcycle;
import com.jayfella.jme.vehicle.examples.vehicles.DuneBuggy;
import com.jayfella.jme.vehicle.examples.vehicles.GrandTourer;
import com.jayfella.jme.vehicle.examples.vehicles.HatchBack;
import com.jayfella.jme.vehicle.examples.vehicles.HoverTank;
import com.jayfella.jme.vehicle.examples.vehicles.Nismo;
import com.jayfella.jme.vehicle.examples.vehicles.PickupTruck;
import com.jayfella.jme.vehicle.examples.vehicles.Rotator;
import com.jayfella.jme.vehicle.examples.worlds.EndlessPlain;
import com.jayfella.jme.vehicle.examples.worlds.Mountains;
import com.jayfella.jme.vehicle.examples.worlds.Playground;
import com.jayfella.jme.vehicle.examples.worlds.Racetrack;
import com.jayfella.jme.vehicle.niftydemo.action.ActionPrefix;
import com.jayfella.jme.vehicle.niftydemo.tool.Tools;
import com.jayfella.jme.vehicle.niftydemo.view.View;
import com.jme3.system.JmeVersion;
import de.lessvoid.nifty.Nifty;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.minie.MinieVersion;
import jme3utilities.nifty.LibraryVersion;
import jme3utilities.nifty.PopupMenuBuilder;
import jme3utilities.nifty.bind.BindScreen;
import jme3utilities.nifty.displaysettings.DsScreen;
import jme3utilities.sky.Constants;
import jme3utilities.ui.InputMode;
import jme3utilities.ui.UiVersion;

/**
 * Menus in the main heads-up display (HUD) of MavDemo2.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class Menus {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Menus.class.getName());
    /**
     * message to be displayed in the attribution dialog
     */
    final private static String attributionMessage = Attribution.plainMessage(
            Attribution.opelGtRetopo,
            Attribution.fordRanger,
            Attribution.nissanGtr,
            Attribution.hcr2Buggy,
            Attribution.hcr2Rotator,
            Attribution.modernHatchbackLowPoly,
            Attribution.batcPack,
            Attribution.raceSuit,
            Attribution.classicMotorcycle);
    /**
     * level separator in menu paths
     */
    final static String menuPathSeparator = " -> ";
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Menus() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Build a "Sky" pop-up menu.
     *
     * @param builder (not null, modified)
     */
    public static void buildSkyMenu(PopupMenuBuilder builder) {
        builder.add("Animated Day Sky");
        builder.add("Animated Night Sky");
        builder.add("Purple Nebula Sky");
        builder.add("Quarry Sky");
    }

    /**
     * Build a "Vehicle" pop-up menu.
     *
     * @param builder (not null, modified)
     */
    public static void buildVehicleMenu(PopupMenuBuilder builder) {
        builder.add("Classic Motorcycle");
        builder.add("Dune Buggy");
        builder.add("Grand Tourer");
        builder.add("Hatchback");
        builder.add("Hovertank");
        builder.add("Nismo");
        builder.add("Pickup Truck");
        builder.add("Rotator");
    }

    /**
     * Build a "World" pop-up menu.
     *
     * @param builder (not null, modified)
     */
    public static void buildWorldMenu(PopupMenuBuilder builder) {
        builder.add("Endless Plain");
        builder.add("Mountains");
        builder.add("Playground");
        builder.add("Racetrack");
    }

    /**
     * Handle a "load sky" action.
     */
    public static void loadSky() {
        PopupMenuBuilder builder = new PopupMenuBuilder();
        buildSkyMenu(builder);

        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showPopupMenu(ActionPrefix.loadSky, builder);
    }

    /**
     * Handle a "select menuItem" action from the Sky menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    public static boolean menuSky(String remainder) {
        Sky sky;
        switch (remainder) {
            case "Animated Day Sky":
                sky = new AnimatedDaySky();
                break;

            case "Animated Night Sky":
                sky = new AnimatedNightSky();
                break;

            case "Purple Nebula Sky":
                sky = new PurpleNebulaSky();
                break;

            case "Quarry Sky":
                sky = new QuarrySky();
                break;

            default:
                return false;
        }

        View view = MavDemo2.findAppState(View.class);
        view.setSky(sky);
        return true;
    }

    /**
     * Handle a "select menuItem" action from the Vehicle menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    public static boolean menuVehicle(String remainder) {
        Vehicle vehicle;
        switch (remainder) {
            case "Classic Motorcycle":
                vehicle = new ClassicMotorcycle();
                break;

            case "Dune Buggy":
                vehicle = new DuneBuggy();
                break;

            case "Grand Tourer":
                vehicle = new GrandTourer();
                break;

            case "Hatchback":
                vehicle = new HatchBack();
                break;

            case "Hovertank":
                vehicle = new HoverTank();
                break;

            case "Nismo":
                vehicle = new Nismo();
                break;

            case "Pickup Truck":
                vehicle = new PickupTruck();
                break;

            case "Rotator":
                vehicle = new Rotator();
                break;

            default:
                return false;
        }

        MavDemo2.getDemoState().setVehicle(vehicle);
        return true;
    }

    /**
     * Handle a "select menuItem" action from the World menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    public static boolean menuWorld(String remainder) {
        World world;
        switch (remainder) {
            case "Endless Plain":
                world = new EndlessPlain();
                break;

            case "Mountains":
                world = new Mountains();
                break;

            case "Playground":
                world = new Playground();
                break;

            case "Racetrack":
                world = new Racetrack();
                break;

            default:
                return false;
        }

        MavDemo2.getDemoState().setWorld(world);
        return true;
    }

    /**
     * Handle all "select menuItem " actions.
     *
     * @param menuPath the active menu path (not null)
     * @return true if handled, otherwise false
     */
    public static boolean selectMenuItem(String menuPath) {
        boolean handled;
        int separatorBegin = menuPath.indexOf(menuPathSeparator);
        if (separatorBegin == -1) { // top-level menu
            handled = menuBar(menuPath);
        } else { // submenu
            int separatorEnd = separatorBegin + menuPathSeparator.length();
            String menuName = menuPath.substring(0, separatorBegin);
            String remainder = menuPath.substring(separatorEnd);
            handled = menu(menuName, remainder);
        }

        return handled;
    }
    // *************************************************************************
    // private methods

    /**
     * Display an "About MavDemo2" dialog.
     */
    private static void aboutDialog() {
        Nifty nifty = MavDemo2.getApplication().getNifty();
        String niftyVersion = nifty.getVersion();
        String text = "MavDemo2\n\nYou are currently running MavDemo2, a "
                + "tech demo for jMonkeyEngine vehicles.\n\nThe version you "
                + "are using incorporates the following libraries:";
        text += String.format("%n   jme3-core version=%s hash=%s (BSD license)",
                MyString.quote(JmeVersion.FULL_NAME),
                JmeVersion.GIT_SHORT_HASH);
        text += String.format("%n   nifty version=%s (BSD license)",
                MyString.quote(niftyVersion));
        text += String.format("%n   Acorus version=%s (BSD license)",
                MyString.quote(UiVersion.versionShort()));
        text += String.format("%n   Garrett version=%s (BSD license)",
                MyString.quote(GarrettVersion.versionShort()));
        text += String.format("%n   Heart version=%s (BSD license)",
                MyString.quote(Heart.versionShort()));
        text += String.format("%n   JmePower version=%s (BSD license)",
                MyString.quote(JmePowerVersion.versionShort()));
        text += String.format("%n   Minie version=%s (BSD license)",
                MyString.quote(MinieVersion.versionShort()));
        text += String.format("%n   SkyControl version=%s (BSD license)",
                MyString.quote(Constants.versionShort()));
        text += String.format(
                "%n   jme3-utilities-nifty version=%s (BSD license)",
                MyString.quote(LibraryVersion.versionShort()));

        text += String.format("%n   jme3-desktop (BSD license)");
        text += String.format("%n   jme3-effects (BSD license)");
        text += String.format("%n   jme3-jogg (BSD license)");
        text += String.format("%n   jme3-lwjgl3 (BSD license)");
        text += String.format("%n   jme3-plugins (BSD license)");
        text += String.format("%n   jme3-terrain (BSD license)");

        text += String.format("%n   nifty (BSD license)");
        text += String.format("%n   nifty-default-controls (BSD license)");

        text += String.format("%n   jme-ttf (%s)",
                "part FPL license, part BSD license");
        text += String.format("%n   sfntly (Apache license)");
        text += String.format("%n   sim-math (BSD license)");

        text += String.format("%n%n");

        MainHud hud = MavDemo2.findAppState(MainHud.class);
        hud.closeAllPopups();
        hud.showInfoDialog("About MavDemo2", text);
    }

    /**
     * Display an attribution dialog.
     */
    private static void attributionDialog() {
        MainHud hud = MavDemo2.findAppState(MainHud.class);
        hud.closeAllPopups();
        hud.showInfoDialog("Attribution", attributionMessage);
    }

    /**
     * Build a "Help" menu.
     *
     * @param builder (not null, modified)
     */
    private static void buildHelpMenu(PopupMenuBuilder builder) {
        builder.add("About", "Textures/icons/dialog.png");
        builder.add("Attribution", "Textures/icons/dialog.png");
    }

    /**
     * Build a "Settings" menu.
     *
     * @param builder (not null, modified)
     */
    private static void buildSettingsMenu(PopupMenuBuilder builder) {
        builder.add("Display", "Textures/icons/dialog.png");
        builder.add("Engine sound", "Textures/icons/submenu.png");
        builder.add("Hotkeys", "Textures/icons/dialog.png");
        builder.add("Sky", "Textures/icons/submenu.png");
        builder.add("Speedometer", "Textures/icons/submenu.png");
        builder.add("Tire smoke", "Textures/icons/submenu.png");
        builder.add("View", "Textures/icons/tool.png");
        builder.add("Wheels", "Textures/icons/submenu.png");
    }

    /**
     * Build a "Tools" menu.
     *
     * @param builder (not null, modified)
     */
    private static void buildToolsMenu(PopupMenuBuilder builder) {
        builder.add("Hide all tools");
        builder.add("Show all tools", "Textures/icons/tool.png");
        builder.add("Show the tools tool", "Textures/icons/tool.png");
    }

    /**
     * Drive the selected Vehicle.
     */
    private static void drive() {
        Vehicle vehicle = MavDemo2.getDemoState().getVehicles().getSelected();
        vehicle.getEngine().setRunning(true);

        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.findTool("driving").setEnabled(true);
    }

    /**
     * Handle a "select menuItem" action for a submenu.
     *
     * @param menuName name of the top-level menu (not null)
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    private static boolean menu(String menuName, String remainder) {
        assert menuName != null;
        assert remainder != null;

        boolean handled;
        switch (menuName) {
            case "Help":
                handled = menuHelp(remainder);
                break;

            case "Settings":
                handled = menuSettings(remainder);
                break;

            case "Tools":
                handled = menuTools(remainder);
                break;

            case "Vehicle":
                handled = menuVehicle(remainder);
                break;

            case "World":
                handled = menuWorld(remainder);
                break;

            default:
                handled = false;
        }

        return handled;
    }

    /**
     * Handle a "select menuItem" action for a top-level menu, typically from
     * the menu bar.
     *
     * @param menuName name of the menu to open (not null)
     * @return true if handled, otherwise false
     */
    private static boolean menuBar(String menuName) {
        assert menuName != null;

        // Dynamically generate the menu's list of items.
        PopupMenuBuilder builder = new PopupMenuBuilder();
        switch (menuName) {
            case "Drive":
                drive();
                return true;

            case "Help":
                buildHelpMenu(builder);
                break;

            case "Quit":
                MavDemo2.getApplication().stop();
                return true;

            case "Settings":
                buildSettingsMenu(builder);
                break;

            case "Tools":
                buildToolsMenu(builder);
                break;

            case "Vehicle":
                buildVehicleMenu(builder);
                break;

            case "World":
                buildWorldMenu(builder);
                break;

            default:
                return false;
        }

        if (builder.isEmpty()) {
            logger.log(Level.WARNING, "no items for the {0} menu",
                    MyString.quote(menuName));
        } else {
            String actionPrefix = ActionPrefix.selectMenuItem + menuName
                    + menuPathSeparator;
            MainHud mainHud = MavDemo2.findAppState(MainHud.class);
            mainHud.showPopupMenu(actionPrefix, builder);
        }

        return true;
    }

    /**
     * Handle a "select menuItem" action from the Help menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    private static boolean menuHelp(String remainder) {
        boolean handled = true;
        MainHud mainHud = MavDemo2.findAppState(MainHud.class);

        switch (remainder) {
            case "About":
                aboutDialog();
                break;

            case "Attribution":
                attributionDialog();
                break;

            default:
                handled = false;
        }

        return handled;
    }

    /**
     * Handle a "select menuItem" action from the Settings menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    private static boolean menuSettings(String remainder) {
        boolean handled = true;
        MainHud hud = MavDemo2.findAppState(MainHud.class);
        PopupMenuBuilder builder = new PopupMenuBuilder();

        switch (remainder) {
            case "Display":
                hud.closeAllPopups();
                DsScreen dss = MavDemo2.findAppState(DsScreen.class);
                dss.activate();
                break;

            case "Engine sound":
                selectEngineSound();
                break;

            case "Hotkeys":
                hud.closeAllPopups();
                BindScreen bindScreen = MavDemo2.findAppState(BindScreen.class);
                InputMode current = InputMode.getActiveMode();
                bindScreen.activate(current);
                break;

            case "Sky":
                loadSky();
                break;

            case "Speedometer":
                selectSpeedometerUnits();
                break;

            case "Tire smoke":
                selectTireSmokeColor();
                break;

            case "View":
                Tools.select("view");
                break;

            case "Wheels":
                selectAllWheelModel();
                break;

            default:
                handled = false;
        }

        return handled;
    }

    /**
     * Handle a "select menuItem" action from the Tools menu.
     *
     * @param remainder not-yet-parsed portion of the menu path (not null)
     * @return true if the action is handled, otherwise false
     */
    private static boolean menuTools(String remainder) {
        boolean handled = true;
        MainHud hud = MavDemo2.findAppState(MainHud.class);

        switch (remainder) {
            case "Hide all tools":
                hud.tools.setAllEnabled(false);
                break;

            case "Show all tools":
                hud.tools.setAllEnabled(true);
                break;

            case "Show the tools tool":
                Tools.select("tools");
                break;

            default:
                handled = false;
        }

        return handled;
    }

    /**
     * Handle a "select menuItem Settings -> Wheels" action.
     */
    private static void selectAllWheelModel() {
        PopupMenuBuilder builder = new PopupMenuBuilder();
        builder.add("Basic Alloy");
        builder.add("Buggy Front");
        builder.add("Buggy Rear");
        builder.add("Cruiser");
        builder.add("Dark Alloy");
        builder.add("Hatchback");
        builder.add("Invisible");
        builder.add("Motorcycle Front");
        builder.add("Motorcycle Rear");
        builder.add("Ranger");
        builder.add("Rotator Front");
        builder.add("Rotator Rear");

        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showPopupMenu(ActionPrefix.selectAllWheelModel, builder);
    }

    /**
     * Handle a "select menuItem Settings -> Engine sound" action.
     */
    private static void selectEngineSound() {
        PopupMenuBuilder builder = new PopupMenuBuilder();

        builder.add("Engine-1");
        builder.add("Engine-2");
        builder.add("Engine-4");
        builder.add("Engine-5");
        builder.add("Silence");

        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showPopupMenu(ActionPrefix.selectEngineSound, builder);
    }

    /**
     * Handle a "select menuItem Settings -> Speedometer" action.
     */
    private static void selectSpeedometerUnits() {
        PopupMenuBuilder builder = new PopupMenuBuilder();

        for (SpeedUnit type : SpeedUnit.values()) {
            String name = type.toString();
            builder.add(name);
        }
        builder.add("None");

        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showPopupMenu(ActionPrefix.selectSpeedometerUnits, builder);
    }

    /**
     * Handle a "select menuItem Settings -> Tire smoke" action.
     */
    private static void selectTireSmokeColor() {
        PopupMenuBuilder builder = new PopupMenuBuilder();

        builder.add("Black");
        builder.add("Blue");
        builder.add("Gray");
        builder.add("Green");
        builder.add("Red");
        builder.add("White");
        builder.add("Yellow");
        builder.add("None");

        MainHud mainHud = MavDemo2.findAppState(MainHud.class);
        mainHud.showPopupMenu(ActionPrefix.selectTireSmokeColor, builder);
    }
}
