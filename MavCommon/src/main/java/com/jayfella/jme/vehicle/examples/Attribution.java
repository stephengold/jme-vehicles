package com.jayfella.jme.vehicle.examples;

import java.util.logging.Logger;

/**
 * Generate attribution messages for assets based on licensed works.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class Attribution {
    // *************************************************************************
    // constants and loggers

    /**
     * info for "Barrier &amp; Traffic Cone Pack"
     */
    final public static LicensedWork batcPack = new LicensedWork(
            "Sabri Ayeş",
            "https://sketchfab.com/sabriayes",
            "Barrier & Traffic Cone Pack",
            "https://skfb.ly/6n8ST",
            "CC-BY-4.0");
    /**
     * info for "Classic Motorcycle"
     */
    final public static LicensedWork classicMotorcycle = new LicensedWork(
            "Mora",
            "https://sketchfab.com/Fopen",
            "Classic Motorcycle",
            "https://skfb.ly/6WVHS",
            "CC-BY-4.0");
    /**
     * info for "Ford Ranger"
     */
    final public static LicensedWork fordRanger = new LicensedWork(
            "mauro.zampaoli",
            "https://sketchfab.com/mauro.zampaoli",
            "Ford Ranger",
            "https://sketchfab.com/3d-models/ford-ranger-dade78dc96e34f1a8cbcf14dd47d84de",
            "CC-BY-4.0");
    /**
     * info for "HCR2 Buggy"
     */
    final public static LicensedWork hcr2Buggy = new LicensedWork(
            "oakar258",
            "https://sketchfab.com/oakar258",
            "HCR2 Buggy",
            "https://sketchfab.com/3d-models/hcr2-buggy-a65fe5c27464448cbce7fe61c49159ef",
            "CC-BY-4.0");
    /**
     * info for "HCR2 Rotator"
     */
    final public static LicensedWork hcr2Rotator = new LicensedWork(
            "oakar258",
            "https://sketchfab.com/oakar258",
            "HCR2 Rotator",
            "https://sketchfab.com/3d-models/hcr2-rotator-f03e95525b4c48cfb659064a76d8cd53",
            "CC-BY-4.0");
    /**
     * info for "Modern Hatchback - Low Poly model"
     */
    final public static LicensedWork modernHatchbackLowPoly = new LicensedWork(
            "Daniel Zhabotinsky",
            "https://sketchfab.com/DanielZhabotinsky",
            "Modern Hatchback - Low Poly model",
            "https://sketchfab.com/3d-models/modern-hatchback-low-poly-model-055ff8a21b8d4d279debca089e2fafcd",
            "CC-BY-4.0");
    /**
     * info for "Nissan GT-R"
     */
    final public static LicensedWork nissanGtr = new LicensedWork(
            "iSteven",
            "https://sketchfab.com/Steven007",
            "Nissan GT-R",
            "https://sketchfab.com/3d-models/nissan-gt-r-5f5781614c6f4ff4b7cb1d3cff9d931c",
            "CC-BY-NC-SA");
    /**
     * info for "Opel GT Retopo"
     */
    final public static LicensedWork opelGtRetopo = new LicensedWork(
            "Thomas Glenn Thorne",
            "https://www.tgthorne.com/contact",
            "Opel GT Retopo",
            "https://sketchfab.com/3d-models/opel-gt-retopo-badcab3c8a3d42359c8416db8a7427fe",
            "CC-BY-NC-SA");
    /**
     * info for "Elvs Racing Fire suit Male1"
     */
    final public static LicensedWork raceSuit = new LicensedWork(
            "Elvaerwyn",
            null,
            "Elvs Racing Fire suit Male1",
            "http://www.makehumancommunity.org/clothes/elvs_racing_fire_suit_male1.html",
            "CC-BY-4.0");
    /**
     * message logger for this class
     */
    final public static Logger logger
            = Logger.getLogger(Attribution.class.getName());
    // *************************************************************************
    // constructors

    /**
     * A private constructor to inhibit instantiation of this class.
     */
    private Attribution() {
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Generate a Markdown attribution message for the specified works.
     *
     * @param licensedWorks the works to be attributed (not null)
     * @return the text (not null)
     */
    public static String mdMessage(LicensedWork... licensedWorks) {
        StringBuilder builder = new StringBuilder(512);
        for (LicensedWork work : licensedWorks) {
            String workName = work.workName;
            String workUrl = work.workUrl;
            String authorName = work.authorName;
            String authorUrl = work.authorUrl;
            String licenseType = work.licenseType;

            builder.append("+ This work is based on \"");
            builder.append(workName);
            builder.append("\"\n  ("); // TODO hardcoded line separators
            builder.append(workUrl);
            builder.append(")\n  by ");
            builder.append(authorName);
            builder.append(' ');
            if (authorUrl != null) {
                builder.append('(');
                builder.append(authorUrl);
                builder.append(")\n  ");
            }
            builder.append("licensed under ");
            builder.append(licenseType);
            if (licenseType.equals("CC-BY-4.0")) {
                builder.append(
                        " (https://creativecommons.org/licenses/by/4.0/)");
            } else if (licenseType.equals("CC-BY-NC-SA")) {
                builder.append(
                        " (https://creativecommons.org/licenses/by-nc-sa/4.0/)");
            }
            builder.append(".\n");
        }

        return builder.toString();
    }

    /**
     * Generate a plain-text attribution message for the specified works.
     *
     * @param licensedWorks the works to be attributed (not null)
     * @return the text (not null)
     */
    public static String plainMessage(LicensedWork... licensedWorks) {
        StringBuilder builder = new StringBuilder(512);
        for (LicensedWork work : licensedWorks) {
            String workName = work.workName;
            String workUrl = work.workUrl;
            String authorName = work.authorName.replace("ş", "s");
            String authorUrl = work.authorUrl;
            String licenseType = work.licenseType;

            builder.append("This work is based on \"");
            builder.append(workName);
            builder.append("\"\n(");
            builder.append(workUrl);
            builder.append(")\nby ");
            builder.append(authorName);
            builder.append(' ');
            if (authorUrl != null) {
                builder.append('(');
                builder.append(authorUrl);
                builder.append(")\n");
            }
            builder.append("licensed under ");
            builder.append(licenseType);
            if (licenseType.equals("CC-BY-4.0")) {
                builder.append(
                        " (https://creativecommons.org/licenses/by/4.0/)");
            }
            builder.append('.');
            builder.append("\n\n");
        }
        builder.append("\n");

        return builder.toString();
    }
}
