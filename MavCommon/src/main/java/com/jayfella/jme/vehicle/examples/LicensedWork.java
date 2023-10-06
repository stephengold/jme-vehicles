package com.jayfella.jme.vehicle.examples;

/**
 * Attribution information for a work with a Creative Commons license.
 * Immutable.
 *
 * @author Stephen Gold sgold@sonic.net
 */
public class LicensedWork {
    // *************************************************************************
    // fields

    /**
     * author's name or username
     */
    final public String authorName;
    /**
     * URL of the author's webpage, or null if none
     */
    final public String authorUrl;
    /**
     * work's name or title
     */
    final public String workName;
    /**
     * URL of the work's webpage
     */
    final public String workUrl;
    /**
     * type of license (for example, "CC-BY-4.0")
     */
    final public String licenseType;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a new work.
     *
     * @param authorName the author's name or username
     * @param authorUrl the URL of the author's webpage
     * @param workName the work's name or title
     * @param workUrl the URL of the work's webpage
     * @param licenseType the type of license (for example, "CC-BY-4.0")
     */
    public LicensedWork(String authorName, String authorUrl, String workName,
            String workUrl, String licenseType) {
        this.authorName = authorName;
        this.authorUrl = authorUrl;
        this.workName = workName;
        this.workUrl = workUrl;
        this.licenseType = licenseType;
    }
}
