// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer;

import java.util.List;
import java.util.Observer;
import java.util.Observable;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import uk.org.toot.control.*;
import uk.org.toot.audio.core.*;
import uk.org.toot.audio.server.AudioClient;
import uk.org.toot.audio.server.AudioServer;
import uk.org.toot.audio.meter.*;
import static uk.org.toot.audio.mixer.MixerControlsIds.*;

/**
 * AudioMixer provides a 'crossbar' of AudioMixerStrips and AudioMixerBusses,
 * at each strip/bus intesection a MixProcess is used to potentially mix
 * a portion of the audio signal from the strip to the bus (or to a strip if
 * routed).
 * The audio signal is not modified by MixProcess.

 * Prohibited: groups routing to groups
 *
 * 1 buffer per bus plus 1 buffer per group
 */
public class AudioMixer implements AudioClient
{
    /**
     * @link aggregationByValue
     * @supplierCardinality 1 
     */
    @SuppressWarnings("unused")
	private MixerControls controls;

    /**
     * @supplierCardinality 1
     * @link aggregationByValue 
     * @label Main
     */
    protected AudioMixerBus mainBus;

    /**
     * @link aggregationByValue
     * @supplierCardinality 1..*
     * @label all 
     */
    /*#protected AudioMixerBus lnkBusses;*/
    protected List<AudioMixerBus> busses;

    /**
     * @link aggregationByValue
     * @supplierCardinality 0..*
     * @label Aux
     */
    /*#protected AudioMixerBus lnkAuxBusses;*/
    protected List<AudioMixerBus> auxBusses;

    /**
     * @link aggregationByValue
     * @supplierCardinality 0..*
     * @label Fx
     */
    /*#protected AudioMixerBus lnkFxBusses;*/
    protected List<AudioMixerBus> fxBusses;

    /**
     * @link aggregationByValue
     * @supplierCardinality 1..*
     * @label All
     */
    /*#protected AudioMixerStrip lnkStrips;*/
    private List<AudioMixerStrip> strips;

    /**
     * @link aggregationByValue
     * @supplierCardinality 0..*
     * @label Channels
     */
    /*#protected AudioMixerStrip lnkChannelStrips;*/
    private List<AudioMixerStrip> channelStrips;

    /**
     * @link aggregationByValue
     * @supplierCardinality 0..*
     * @label Groups
     */
    /*#protected AudioMixerStrip lnkGroupStrips;*/
    private List<AudioMixerStrip> groupStrips;

    /**
     * @link aggregationByValue
     * @supplierCardinality 0..*
     * @label Fx
     */
    /*#protected AudioMixerStrip lnkFxStrips;*/
    private List<AudioMixerStrip> fxStrips;

    /**
     * @link aggregationByValue
     * @supplierCardinality 0..*
     * @label Aux
     */
    /*#protected AudioMixerStrip lnkAuxStrips;*/
    private List<AudioMixerStrip> auxStrips;

    /**
     * @supplierCardinality 1
     * @link aggregationByValue
     * @label Main*/
    private AudioMixerStrip mainStrip;

    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private AudioServer server;

    private AudioBuffer sharedAudioBuffer;

    private ConcurrentLinkedQueue<MixerControls.Mutation> mutationQueue;
    private boolean enabled = true;
//    private int activeStripCount = 0;

    public AudioMixer(MixerControls controls, AudioServer server) throws Exception {
        if ( controls == null ) {
            throw new IllegalArgumentException("null MixerControls");
        }
        if ( server == null ) {
            throw new IllegalArgumentException("null AudioServer");
        }
        this.controls = controls;
        this.server = server; // should be an audio buffer factory !!! !!!
        sharedAudioBuffer = server.createAudioBuffer("Mixer (shared)");
        mutationQueue = new ConcurrentLinkedQueue<MixerControls.Mutation>();
        strips = new java.util.ArrayList<AudioMixerStrip>();
        channelStrips = new java.util.ArrayList<AudioMixerStrip>();
        groupStrips = new java.util.ArrayList<AudioMixerStrip>();
        fxStrips = new java.util.ArrayList<AudioMixerStrip>();
        auxStrips = new java.util.ArrayList<AudioMixerStrip>();
        // must create Busses first, bus Strips borrow their bus buffers
        createBusses(controls);
        createStrips(controls);
        controls.addObserver(new MixerControlsObserver());
//       	System.out.println("Mixer created");
    }

    protected AudioBuffer getSharedBuffer() {
        return sharedAudioBuffer;
    }

    protected AudioBuffer createBuffer(String name) {
        return server.createAudioBuffer(name);
    }

    public boolean isMutating() {
        return !mutationQueue.isEmpty();
    }

    public void waitForMutations() {
        while ( isMutating() ) {
            if ( isEnabled() && server.isRunning() ) {
	            try {
    	            Thread.sleep(5);
        	    } catch ( InterruptedException ie ) {
            	    // intentionally no code
            	}
            } else {
                processMutations();
            }
        }
        // just in case mutation hasn't completed yet - yuk !!! !!!
        if ( isEnabled() && server.isRunning() ) {
	        try {
    	        Thread.sleep(20);
   	    	} catch ( InterruptedException ie ) {
       	    	// intentionally no code
       		}
        }
    }

    public AudioMixerStrip getStrip(String name) {
        waitForMutations();
        for ( AudioMixerStrip strip : strips ) {
            if ( strip.getName().equals(name) ) return strip;
        }
        return null; // !!!
    }

    public List<AudioMixerStrip> getStrips() {
        waitForMutations();
        return Collections.unmodifiableList(strips);
    }

    public void work(int nFrames) {
        processMutations();
        silenceStrips(groupStrips);
        silenceStrips(fxStrips);
        silenceStrips(auxStrips);
        mainStrip.silence();
        evaluateStrips(channelStrips); // mix to main, aux & fx busses
        evaluateStrips(groupStrips);   // mix to main, aux & fx busses
        evaluateStrips(fxStrips); 	   // mix to main & aux busses
        evaluateStrips(auxStrips);
        mainStrip.processBuffer();
        writeBusBuffers(); // export external busses
    }

    // process a single mutation each iteration
    // called in sync with server when server is running
    protected void processMutations() {
        MixerControls.Mutation m = mutationQueue.poll();
        if ( m == null ) return;
        processMutation(m);
	}

    protected void processMutation(MixerControls.Mutation m) {
        if ( !(m.getControl() instanceof AudioControlsChain) ) return;
        AudioControlsChain controlsChain = (AudioControlsChain)m.getControl();
        switch ( m.getOperation() ) {
        case MixerControls.Mutation.ADD:
           	createStrip(controlsChain);
            break;
        case MixerControls.Mutation.REMOVE:
            removeStrip(controlsChain);
            break;
        }
    }

    protected void evaluateStrips(List<AudioMixerStrip> evalStrips) {
        for ( AudioMixerStrip strip : evalStrips) {
            /*if ( */strip.processBuffer() /*) activeStrips += 1*/;
        }
    }

    protected void silenceStrips(List<AudioMixerStrip> evalStrips) {
        for ( AudioMixerStrip strip : evalStrips) {
            strip.silence();
        }
    }

    protected void writeBusBuffers() {
        for ( AudioMixerBus bus : busses ) {
            bus.write();
        }
/*        for ( int i = 0; i < busses.size(); i++ ) {
        	busses.get(i).write();
        } */
    }

    protected void createBusses(MixerControls mixerControls) {
        busses = new java.util.ArrayList<AudioMixerBus>();
        auxBusses = new java.util.ArrayList<AudioMixerBus>();
        fxBusses = new java.util.ArrayList<AudioMixerBus>();
        AudioMixerBus bus;
        for ( BusControls busControls : mixerControls.getAuxBusControls() ) {
            bus = createBus(busControls);
            busses.add(bus);
            auxBusses.add(bus);
        }
        for ( BusControls busControls : mixerControls.getFxBusControls() ) {
            bus = createBus(busControls);
            busses.add(bus);
            fxBusses.add(bus);
        }
        mainBus = createBus(mixerControls.getMainBusControls());
        busses.add(mainBus);
    }

    protected AudioMixerBus createBus(BusControls busControls) {
        return new AudioMixerBus(this, busControls);
    }

    public AudioMixerBus getBus(String name) {
        for ( AudioMixerBus bus : busses ) {
            if ( bus.getName().equals(name) ) return bus;
        }
        return null; // !!!
    }

    public AudioMixerBus getMainBus() {
        return mainBus;
    }

    public AudioMixerStrip getMainStrip() {
        if ( mainStrip == null ) {
            System.err.println("getMainStrip() called before mainStrip set");
        }
        return mainStrip;
    }

    protected void createStrips(MixerControls mixerControls) {
        for ( Control control : mixerControls.getControls() ) {
            if ( control instanceof AudioControlsChain ) {
            	createStrip((AudioControlsChain)control);
            }
        }
    }

    protected AudioMixerStrip createStrip(AudioControlsChain controls) {
        AudioMixerStrip strip = new AudioMixerStrip(this, controls) {
		    protected AudioProcess createProcess(AudioControls controls) {
                if ( controls instanceof MeterControls ) {
                    return new MeterProcess((MeterControls)controls);
                }
                return super.createProcess(controls);
            }
        };
        switch ( strip.getId() ) {
        case CHANNEL_STRIP:	channelStrips.add(strip); break;
        case GROUP_STRIP: 	groupStrips.add(strip); break;
        case FX_STRIP: 		fxStrips.add(strip); break;
        case AUX_STRIP:		auxStrips.add(strip); break;
        case MAIN_STRIP:
            if ( mainStrip == null ) {
				mainStrip = strip;
        	} else {
                throw new IllegalArgumentException("Only one main strip allowed");
            }
            break;
        }
        strips.add(strip);
        strip.open();
        return strip;
    }

    protected void removeStrip(AudioControlsChain controls) {
        for ( AudioMixerStrip strip : strips ) {
            if ( strip.getName().equals(controls.getName()) ) {
                strip.close();
                strips.remove(strip);
		        switch ( strip.getId() ) {
        		case CHANNEL_STRIP:	channelStrips.remove(strip); break;
		        case GROUP_STRIP: 	groupStrips.remove(strip); break;
        		case FX_STRIP: 		fxStrips.remove(strip); break;
		        case AUX_STRIP:		auxStrips.remove(strip); break;
        		case MAIN_STRIP:	mainStrip = null; break;
		        }
                return;
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * A MixerControlsObserver observes a MixerControls for
     * asynchronous Mutation commands, adding these mutations to a
     * thread-safe queue for use at 'process-time'.
     */
    protected class MixerControlsObserver implements Observer
    {
        public void update(Observable obs, Object obj) {
            if ( obj instanceof MixerControls.Mutation ) {
                if ( isEnabled() && server.isRunning() ) {
            		mutationQueue.add((MixerControls.Mutation)obj);
                } else {
//                    System.out.println("Mixer mutation : "+(MixerControls.Mutation)obj);
                    processMutation((MixerControls.Mutation)obj);
                }
            }
        }
    }
}