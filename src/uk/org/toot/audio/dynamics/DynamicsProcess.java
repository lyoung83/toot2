/* Copyright (C) 2006 Steve Taylor (toot.org.uk) */

package uk.org.toot.audio.dynamics;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.SimpleAudioProcess;

abstract public class DynamicsProcess extends SimpleAudioProcess
{
    protected float envelope = 0f;

    protected boolean isPeak = false;
    protected float threshold;
    protected float thresholddB;
    protected float ratio;
    protected float attack, release;
    protected float makeupGain;
    protected float ratio2;

    protected ProcessVariables vars;

    private boolean wasBypassed;

    private int sampleRate = 0;

    private int NSQUARESUMS = 10;
    private float[] squaresums = new float[NSQUARESUMS];
    private int nsqsum = 0;
    
    public DynamicsProcess(ProcessVariables vars) {
        this(vars, false);
        wasBypassed = !vars.isBypassed(); // force update
    }

    public DynamicsProcess(ProcessVariables vars, boolean peak) {
        this.vars = vars;
        this.isPeak = peak;
    }

    public void clear() {
        envelope = 1f; // envelope of gain
       	vars.setDynamicGain(1f);
    }

    /**
     * Called once per AudioBuffer
     */
    protected void cacheProcessVariables() {
        // update local variables
        threshold = vars.getThreshold();
        thresholddB = vars.getThresholddB();
        ratio = vars.getRatio();
        attack = vars.getAttack();
        release = vars.getRelease();
        makeupGain = vars.getGain();
        ratio2 = (1f - ratio) / ratio;
    }

    /**
     * Called once per AudioBuffer
     */
	public int processAudio(AudioBuffer buffer) {
        boolean bypassed = vars.isBypassed();
        if ( bypassed ) {
            if ( !wasBypassed ) {
                clear();
            }
            wasBypassed = true;
            return AUDIO_OK;
        }
        int sr = (int)buffer.getSampleRate();
        if ( sr != sampleRate ) {
        	sampleRate = sr;
        	vars.update(sr); // rederives attack, release
        }
        cacheProcessVariables();
        int nc = buffer.getChannelCount();
        int len = buffer.getSampleCount();
        int mslen = (int)(buffer.getSampleRate() / 1000);

        float targetGain = 1f; // unity
        float gain = makeupGain; // keeps compiler happy

        float[][] samples = new float[nc][];
		for ( int c = 0; c < nc; c++ ) {
			samples[c] = buffer.getChannel(c);
		}
        for ( int i = 0; i < len; i++ ) {
        	float key = 0;
        	if ( isPeak ) {
        		for ( int c = 0; c < nc; c++ ) {
        			key = Math.max(key, Math.abs(samples[c][i]));
        		}
        		targetGain = function(key);
        	} else if ( (i % mslen) == 0 && (i + mslen) < len ) {
        		// the rms side chain calculations, every millisecond
        		float sample;
        		float sumOfSquares = 0f;
        		for ( int c = 0; c < nc; c++ ) {
        			for ( int j = 0; j < mslen; j++ ) {
        				sample = samples[c][i+j];
        				sumOfSquares += sample * sample;
        			}
        		}
        		squaresums[nsqsum] = sumOfSquares / (mslen * nc);
        		float mean = 0;
        		for ( int s = 0; s < NSQUARESUMS; s++ ) {
        			mean += squaresums[s];
        		}
        		if ( ++nsqsum >= NSQUARESUMS ) nsqsum = 0;
        		key = (float)Math.sqrt(mean/NSQUARESUMS);
        		targetGain = function(key);
        	}

        	gain = dynamics(targetGain);
        	// affect all channels identically to preserve positional image
        	for ( int c = 0; c < nc; c++ ) {
        		samples[c][i] *= gain * makeupGain;
        	}
        }
        // we only announce the final value at the end of the buffer
        // this effectively subsamples the dynamic gain
        // but typically attack and release will provide sufficient smoothing
        // for the avoidance of aliasing
		vars.setDynamicGain(gain);
        wasBypassed = bypassed;
        return AUDIO_OK;
    }

    // effect of comparison of detected against threshold - subclass issue
    protected abstract float function(float value);

    // hold is a gate subclass issue
    protected float dynamics(float target) {
        // anti-denormal not needed, decays to unity
        // seems back to front because (>)0 is max and 1 is min gain reduction
        float factor = target < envelope ?  attack : release;
		envelope = factor * (envelope - target) + target;
        return envelope;
    }

    /**
     * Specifies parameters in implementation terms
     */
    public interface ProcessVariables {
        void update(float sampleRate);
        boolean isBypassed();
        float getThreshold(); 	//  NOT dB, the actual level
        float getThresholddB(); //  dB
        float getRatio();
        float getKnee();		//	NOT dB, the actual level
        float getAttack();		//	NOT ms, the exponential coefficient
        int getHold();			//	NOT ms, samples
        float getRelease();		//	NOT ms, the exponential coefficient
        float getDepth();		//	NOT dB, the actual level
        float getGain();		// 	NOT dB, the actual static makeup gain
        void setDynamicGain(float gain); // NOT dB, the actual (sub sampled) dynamic gain
    }
}
