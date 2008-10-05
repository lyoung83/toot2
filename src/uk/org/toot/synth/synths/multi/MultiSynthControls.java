package uk.org.toot.synth.synths.multi;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.synth.SynthControls;

import static uk.org.toot.synth.id.TootSynthControlsId.MULTI_SYNTH_ID;

public class MultiSynthControls extends SynthControls
{
	public final static int ID = MULTI_SYNTH_ID;
	public final static String NAME = "MultiSynth";
	
	public MultiSynthControls() {
		super(ID, NAME);
	}
	
	public void setChannelControls(int chan, CompoundControl c) {
		CompoundControl old = getChannelControls(chan);
		if ( old != null ) {
			remove(old);
		}
		super.setChannelControls(chan, c);
		setChanged();
		notifyObservers(chan);
	}
	
	// causes plugins to show Preset menu
	public boolean isPluginParent() { 
		return true; 
	}
	
}