package uk.org.toot.synth.synths.multi;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.synth.MidiSynth;
import uk.org.toot.synth.spi.SynthServiceProvider;

import static uk.org.toot.control.id.ProviderId.TOOT_PROVIDER_ID;

public class MultiSynthServiceProvider extends SynthServiceProvider
{
	public MultiSynthServiceProvider() {
		super(TOOT_PROVIDER_ID, "Toot Software", MultiSynthControls.NAME, "0.1");
		String name = MultiSynthControls.NAME;
		addControls(MultiSynthControls.class, MultiSynthControls.ID, name, "", "0.1");
		add(MultiMidiSynth.class, name, "", "0.1");
	}

	public MidiSynth createSynth(CompoundControl c) {
		if ( c instanceof MultiSynthControls ) {
			return new MultiMidiSynth((MultiSynthControls)c);
		}
		return null;
	}

}
