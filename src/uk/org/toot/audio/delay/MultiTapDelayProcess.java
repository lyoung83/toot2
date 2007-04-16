// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.ChannelFormat;

/**
 * A Multi Tap Delay Process
 * Basically delegating to DelayBuffer
 */
public class MultiTapDelayProcess implements AudioProcess
{
    /**
     * @link aggregationByValue
     * @supplierCardinality 1 
     */
    private DelayBuffer delayBuffer;

    private DelayBuffer tappedBuffer; // just for conform()

    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private final MultiTapDelayVariables vars;

    private boolean wasBypassed;

    public MultiTapDelayProcess(MultiTapDelayVariables vars) {
        this.vars = vars;
        wasBypassed = !vars.isBypassed(); // force update
    }

    public void open() {
        // defer delay buffer allocation until sample rate known
    }

    /*
   	If all taps delays are > buffer time
    the delayed output is independent of input
	but need 3 buffers: buffer (in/out), delayBuffer, tappedBuffer
   	*/
    public int processAudio(AudioBuffer buffer) {
        boolean bypassed = vars.isBypassed();
        if ( bypassed ) {
            if ( !wasBypassed ) {
                if ( delayBuffer != null ) {
                    // silence delay buffer on transition to bypassed
                    delayBuffer.makeSilence();
                }
                wasBypassed = true;
            }
            return AUDIO_OK;
        }
        if ( buffer.getChannelCount() < 2 ) {
        	buffer.convertTo(ChannelFormat.STEREO);
        }
        float sampleRate = buffer.getSampleRate();
        int ns = buffer.getSampleCount();
        int nc = buffer.getChannelCount();

        float feedback = vars.getFeedback();
		float mix = vars.getMix();

        if ( delayBuffer == null ) {
	        delayBuffer = new DelayBuffer(nc,
                msToSamples(vars.getMaxDelayMilliseconds(), sampleRate),
                sampleRate);
        } else {
            delayBuffer.conform(buffer);
        }

        if ( tappedBuffer == null ) {
	        tappedBuffer = new DelayBuffer(nc, ns, sampleRate);
        } else {
            tappedBuffer.conform(buffer);
            // conform only changes number of channels and sample rate
            if ( tappedBuffer.getSampleCount() != ns ) {
                tappedBuffer.changeSampleCount(ns, false);
            }
        }

    	// tapped from delay
    	tappedBuffer.makeSilence();
        float delayFactor = vars.getDelayFactor();
        for ( int c = 0; c < nc; c++ ) {
	        for ( DelayTap tap : vars.getTaps(c) ) {
	            float level = tap.getLevel();
    	        if ( level < 0.001 ) continue;
				int delay = (int)msToSamples(tap.getDelayMilliseconds()*delayFactor, sampleRate);
            	if ( delay < ns ) continue; // can't evaluate. push down to called method?
    			delayBuffer.tap(c, tappedBuffer, delay, level); // optimised mix
			}
		}
    	// delay append process + tapped * feedback
    	delayBuffer.append(buffer, tappedBuffer, feedback);
    	// process mixed from process and tapped
        for ( int c = 0; c < nc; c++ ) {
            float[] samples = buffer.getChannel(c);
            float[] tapped = tappedBuffer.getChannel(c);
            for ( int i = 0; i < ns; i++ ) {
                samples[i] += mix * tapped[i];
            }
        }

        wasBypassed = bypassed;
        return AUDIO_OK;
    }

    public void close() {
        delayBuffer = null;
        tappedBuffer = null;
    }

    protected int msToSamples(float ms, float sr) {
        return (int)((ms * sr) / 1000); // !!! !!! move elsewhere
    }
}
