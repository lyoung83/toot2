package uk.org.toot.synth.modules.amplifier;

import static uk.org.toot.misc.Localisation.getString;
import static uk.org.toot.synth.modules.amplifier.AmplifierControlIds.*;

import java.awt.Color;
import java.util.Observable;
import java.util.Observer;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;
import uk.org.toot.control.LogLaw;

public class AmplifierControls extends CompoundControl 
	implements AmplifierVariables
{
	private FloatControl velocityTrackControl;
	private FloatControl levelControl;
	
	private float velocityTrack;
	private float level;
	
	private int idOffset = 0;
	
	private int sampleRate = 44100;
	
	public AmplifierControls(int instanceIndex, String name, int idOffset) {
		this(AmplifierIds.AMPLIFIER_ID , instanceIndex, name, idOffset);
	}
	
	public AmplifierControls(int id, int instanceIndex, String name, final int idOffset) {
		super(id, instanceIndex, name);
		this.idOffset = idOffset;
		createControls();
		deriveSampleRateIndependentVariables();
		deriveSampleRateDependentVariables();
		addObserver(new Observer() {
			public void update(Observable obs, Object obj) {
				Control c = (Control) obj;
//				if (c.isIndicator()) return;
				switch (c.getId()-idOffset) {
				case VEL_TRACK: velocityTrack = deriveVelocityTrack() ; break;
				case LEVEL: level = deriveLevel(); break;
				}
			}
		});
	}

	protected void createControls() {
		add(velocityTrackControl = createVelocityTrackControl());
		add(levelControl = createLevelControl());
	}

	protected void deriveSampleRateIndependentVariables() {
		velocityTrack = deriveVelocityTrack();
		level = deriveLevel();
	}

	protected float deriveVelocityTrack() {
		return velocityTrackControl.getValue();
	}

	protected float deriveLevel() {
		return levelControl.getValue();
	}
	
	protected void deriveSampleRateDependentVariables() {
	}

	protected FloatControl createVelocityTrackControl() {
        ControlLaw law = new LinearLaw(0f, 1f, "");
        FloatControl control = new FloatControl(VEL_TRACK+idOffset, getString("Velocity"), law, 0.01f, 0.5f);
        control.setInsertColor(Color.BLUE);
        return control;				
	}

	protected FloatControl createLevelControl() {
		ControlLaw law = new LogLaw(0.01f, 1f, "");
        FloatControl control = new FloatControl(LEVEL+idOffset, getString("Level"), law, 0.01f, 0.1f);
        control.setInsertColor(Color.BLACK);
        return control;						
	}
	
	public float getVelocityTrack() {
		return velocityTrack;
	}
	
	public float getLevel() {
		return level;
	}
	
	public void setSampleRate(int rate) {
		if ( sampleRate != rate ) {
			sampleRate = rate;
			deriveSampleRateDependentVariables();
		}
	}

}
