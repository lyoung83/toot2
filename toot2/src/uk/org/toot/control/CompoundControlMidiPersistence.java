/* Copyright Steve Taylor 2006 */

package uk.org.toot.control;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.sound.midi.*;
// !!! !!!
import static uk.org.toot.control.ControlSysexMsg.*; // !!! !!!

public class CompoundControlMidiPersistence implements CompoundControlPersistence
{
    private File root;

    /** @link dependency */
    /*#MidiPersistence lnkMidiPersistence;*/

    public CompoundControlMidiPersistence(File root) {
        this.root = root;
    }

    /*#public List getPresets(CompoundControl c);*/
    public List<String> getPresets(CompoundControl c) {
        File dir = new File(root, path(c.getProviderId(), c.getId()));
        List<String> names = new java.util.ArrayList<String>();
        if ( !dir.exists() || !dir.isDirectory() ) return names;
        File[] files = dir.listFiles();
        for ( File file : files ) {
            if ( file.isDirectory() ) continue;
            names.add(file.getName());
        }
        return names; 
    }

    public void loadPreset(CompoundControl c, String name) {
        int providerId = c.getProviderId();
        int moduleId = c.getId();
   	    File path = new File(root, path(providerId, moduleId));
        File file = new File(path, name);
        if ( !file.exists() ) return;
        try {
	        Sequence sequence = MidiSystem.getSequence(file);
            Track track = sequence.getTracks()[0];
            for ( int i = 0; i < track.size(); i++ ) {
                MidiMessage msg = track.get(i).getMessage();
                if ( !isControl(msg) ) continue;
                if ( getProviderId(msg) != providerId ) continue;
                if ( getModuleId(msg) != moduleId ) continue;
                Control control = c.deepFind(getControlId(msg));
                if ( control == null ) continue;
                // for sanity we ignore bypass controls
                if ( control instanceof CompoundControl.BypassControl ) continue;
                control.setIntValue(getValue(msg));
            }
        } catch ( IOException ioe ) {
            ioe.printStackTrace();
        } catch ( InvalidMidiDataException imde ) {
            imde.printStackTrace();
        }
    }

    public void savePreset(CompoundControl c, String name) {
        int providerId = c.getProviderId();
        int moduleId = c.getId();
        try {
	        Sequence sequence = new Sequence(Sequence.PPQ, 1);
    	    Track track = sequence.createTrack();
        	MidiPersistence.store(providerId, moduleId, 0, c, track);
    	    File path = new File(root, path(providerId, moduleId));
            path.mkdirs();
        	MidiSystem.write(sequence, 0, new File(path, name));
        } catch ( IOException ioe ) {
            ioe.printStackTrace();
        } catch ( InvalidMidiDataException imde ) {
            imde.printStackTrace();
        }
    }

	protected String path(int providerId, int moduleId) {
        // <providerId>/<moduleId>
        return providerId+File.separator+moduleId;
    }

	protected String path(int providerId, int moduleId, String name) {
        // <providerId>/<moduleId>/name
        return path(providerId, moduleId)+File.separator+name;
    }

}
