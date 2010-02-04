// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.reverb;

import uk.org.toot.audio.spi.TootAudioServiceProvider;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.AudioControls;

import static uk.org.toot.misc.Localisation.*;

/**
 * Exposes distortion as a plugin service
 * @author st
 */
public class ReverbServiceProvider extends TootAudioServiceProvider
{
    public ReverbServiceProvider() {
        super(getString("Reverb"), "0.1");
        String family = description;
        addControls(PlateControls.class, ReverbIds.PLATE_ID, 
        	"Plate Reverb", family, "0.1");
        add(PlateProcess.class, "Plate Reverb", family, "0.1");
    }

    public AudioProcess createProcessor(AudioControls c) {
        if ( c instanceof PlateVariables ) {
            return new PlateProcess((PlateVariables)c);
        } 
        return null; // caller then tries another provider
    }
}