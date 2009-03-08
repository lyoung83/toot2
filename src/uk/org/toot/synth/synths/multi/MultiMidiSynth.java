package uk.org.toot.synth.synths.multi;

import java.util.Observable;
import java.util.Observer;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.synth.BasicMidiSynth;
import uk.org.toot.synth.PolyphonicSynthChannel;
import uk.org.toot.synth.SynthChannelServices;

/**
 * This class allows each SynthChannel to be externally set.
 * @author st
 *
 */
public class MultiMidiSynth extends BasicMidiSynth
{
	public MultiMidiSynth(final MultiSynthControls controls) {
		super(controls.getName());
		controls.addObserver(
			new Observer() {
				public void update(Observable obs, Object obj) {
					if ( obj instanceof Integer ) {
						int chan = ((Integer)obj).intValue();
						if ( chan < 0 || chan > 15 ) return;
						CompoundControl channelControls = controls.getChannelControls(chan);
						if ( channelControls != null ) {
							// SPI lookup plugin SynthChannel for these controls
							PolyphonicSynthChannel synthChannel = SynthChannelServices.createSynthChannel(channelControls);
							if ( synthChannel == null ) {
								System.err.println("No SynthChannel for SynthControls "+channelControls.getName());
							} else {
								synthChannel.setLocation(MultiMidiSynth.this.getLocation()+" Channel "+(1+chan));
							}
							setChannel(chan, synthChannel);
						} else {
							setChannel(chan, null);
						}
					}
				}					
			}
		);
	}

	protected void setChannel(int chan, PolyphonicSynthChannel synthChannel) {
		PolyphonicSynthChannel old = (PolyphonicSynthChannel)getChannel(chan);
		if ( old != null ) {
			removeAudioOutput(old);
		}
		super.setChannel(chan, synthChannel);
		if ( synthChannel != null ) {
			addAudioOutput(synthChannel);
		}
	}
}
