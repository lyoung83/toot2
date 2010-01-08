// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.channels.total;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.synth.PolyphonicSynthChannel;
import uk.org.toot.synth.modules.amplifier.AmplifierVariables;
import uk.org.toot.synth.modules.envelope.EnvelopeGenerator;
import uk.org.toot.synth.modules.envelope.EnvelopeVariables;
import uk.org.toot.synth.modules.oscillator.DSFOscillatorSS;
import uk.org.toot.synth.modules.oscillator.DSFOscillatorVariables;
import uk.org.toot.synth.modules.oscillator.UnisonVariables;

/**
 * A synth channel based on Discrete Summation Formulae, hence named Total, the
 * result of summation.
 * @author st
 */
public class TotalSynthChannel extends PolyphonicSynthChannel
{
	private DSFOscillatorVariables oscVars;
	private UnisonVariables unisonVars;
	private EnvelopeVariables envAVars;
	private AmplifierVariables ampVars;
	
	private int rolloffInt = -1;
	
	public TotalSynthChannel(TotalSynthControls controls) {
		super(controls.getName());
		oscVars = controls.getOscillatorVariables();
		unisonVars = controls.getUnisonVariables();
		envAVars = controls.getEnvelopeVariables(0);
		ampVars = controls.getAmplifierVariables();
	}

	protected void setSampleRate(int rate) {
		super.setSampleRate(rate);
		envAVars.setSampleRate(rate);
	}
	
	@Override
	protected Voice createVoice(int pitch, int velocity, int sampleRate) {
		return new TotalVoice(pitch, velocity);
	}

	public class TotalVoice extends AbstractVoice
	{
		private DSFOscillatorSS[] osc;
		private EnvelopeGenerator envelopeA;
		private int nosc;
		private float ampT; // amp tracking factor
		private float ampLevel;
		
		public TotalVoice(int pitch, int velocity) {
			super(pitch, velocity);
			nosc = unisonVars.getOscillatorCount();
			if ( (nosc & 1) == 0 ) nosc += 1; // force odd TODO Even/Odd IntegerLaws
			osc = new DSFOscillatorSS[nosc];
			float wn = (float)(frequency * 2 * Math.PI / sampleRate);
			float ratio = (float)oscVars.getRatioNumerator() / oscVars.getRatioDenominator();
			int np = oscVars.getPartialCount();
			float a  = oscVars.getPartialRolloffFactor();
			
			osc[0] = new DSFOscillatorSS(wn, wn * ratio, np, a);
			
			// maximum detune more than 50 cents
			float detune = unisonVars.getPitchSpread() * 0.03f * wn;
			// maximum offset in samples, should be doubled but works well
			float offset = unisonVars.getPhaseSpread() * (float)Math.PI / wn;
			int npairs = (nosc - 1) / 2;
			float wo, ws;
			int off;
			DSFOscillatorSS osct;
			for ( int o = 0; o < npairs; o++ ) {
				wo = detune * (float)(npairs - o) / npairs;
				ws = wn + wo;
				osct = osc[o+o+1] = new DSFOscillatorSS(ws, ws * ratio, np, a);
				off = (int)(offset * Math.random());
				while ( off-- > 0 ) osct.getSample();
				ws = wn - wo;
				osct = osc[o+o+2] = new DSFOscillatorSS(ws, ws * ratio, np, a);				
				off = (int)(offset * Math.random());
				while ( off-- > 0 ) osct.getSample();
			}
			
			envelopeA = new EnvelopeGenerator(envAVars);
			envelopeA.trigger();

			float ampTracking = ampVars.getVelocityTrack();
			ampT = velocity == 0 ? 0f : (1 - ampTracking * (1 - amplitude));
		}


		public void setSampleRate(int sr) {
			// can't change sample rate dynamically !!!
		}
		
		@Override
		public boolean mix(AudioBuffer buffer) {
			int ro = oscVars.getPartialRolloffInt();
			if ( rolloffInt != ro ) {
				// rolloff factor has changed
				float a = oscVars.getPartialRolloffFactor();
				for ( int i = 0; i < nosc; i++ ) {
					osc[i].update(a);
				}
				rolloffInt = ro;
			}
			ampLevel = ampVars.getLevel() * ampT;
			return super.mix(buffer);
		}
		
		@Override
		protected float getSample() {
			float sample = 0f;
			for ( int i = 0; i < nosc; i++ ) {
				sample += osc[i].getSample();
			}
			return sample * ampLevel * envelopeA.getEnvelope(release);
		}

		@Override
		protected boolean isComplete() {
			return envelopeA.isComplete();
		}
	}
}