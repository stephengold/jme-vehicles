/*
 Copyright (c) 2020-2022, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its contributors
 may be used to endorse or promote products derived from this software without
 specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jayfella.jme.vehicle.lemurdemo;

import com.github.stephengold.jmepower.JmeLoadingState;
import com.jme3.app.Application;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;
import jme3utilities.Loadable;
import jme3utilities.Validate;

/**
 * An AppState to display a Cinematic while warming up the AssetCache and
 * initializing Lemur. When its work is done, it disables (but does not detach)
 * itself.
 */
public class LemurLoadingState extends JmeLoadingState {
    // *************************************************************************
    // constants and loggers

    /**
     * message logger for this class
     */
    final public static Logger logger2
            = Logger.getLogger(LemurLoadingState.class.getName());
    // *************************************************************************
    // constructors

    /**
     * Instantiate an enabled AppState to preload the specified assets.
     *
     * @param loadables the assets to preload (not null)
     */
    public LemurLoadingState(Loadable... loadables) {
        super(loadables);
    }
    // *************************************************************************
    // JmeLoadingState methods

    /**
     * Create and start the background threads.
     *
     * @param numAdditionalThreads (&ge;0)
     */
    @Override
    protected void startThreads(int numAdditionalThreads) {
        Validate.nonNegative(
                numAdditionalThreads, "number of additional threads");
        super.startThreads(numAdditionalThreads + 1);
        /*
         * Start an additional thread to initialize Lemur.
         */
        final CountDownLatch latch = getLatch();
        Thread thread = new Thread() {
            @Override
            public void run() {
                initializeLemur();
                latch.countDown();
            }
        };
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
    // *************************************************************************
    // private methods

    /**
     * Initialize the Lemur library with the "glass" style.
     */
    private void initializeLemur() {
//        long startMillis = System.currentTimeMillis();

        Application application = getApplication();
        GuiGlobals.initialize(application);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

//        long latencyMillis = System.currentTimeMillis() - startMillis;
//        float seconds = latencyMillis / 1_000f;
//        System.out.println("initialized Lemur in " + seconds + " seconds");
    }
}
