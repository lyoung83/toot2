package uk.org.toot.synth.synths.vsti;

import java.io.File;

import com.synthbot.audioplugin.vst.vst2.JVstHost2;
import com.synthbot.audioplugin.vst.JVstLoadException;

import uk.org.toot.service.ServiceDescriptor;
import uk.org.toot.synth.SynthControls;

import static uk.org.toot.synth.id.TootSynthControlsId.VSTI_SYNTH_ID;

public class VstiSynthControls extends SynthControls
{
	public final static int ID = VSTI_SYNTH_ID;

	private JVstHost2 vsti;
	
	public VstiSynthControls(ServiceDescriptor d) throws JVstLoadException {
		super(ID, d.getName());
		// buffer size is large for bad plugins that only set it ONCE
		vsti = JVstHost2.newInstance(new File(d.getDescription()), 44100, 4410);
	}
	
	// causes plugins to show Preset menu
	public boolean isPluginParent() { 
		return true; 
	}
	
	public JVstHost2 getVsti() {
		return vsti;
	}
}
