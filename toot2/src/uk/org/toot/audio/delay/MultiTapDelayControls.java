// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import uk.org.toot.audio.core.AudioControls;
import java.util.List;

public class MultiTapDelayControls extends AudioControls
{
    private List<DelayTap> taps;

    public MultiTapDelayControls(int idOffset, int ntaps, float msMax, String name) {
        super(idOffset, name);
        taps = new java.util.ArrayList<DelayTap>();
        for ( int t = 0; t < ntaps; t++ ) {
            DelayTapControls controls = new DelayTapControls(idOffset+t+t, msMax);
            add(controls);
            taps.add(controls);
        }
    }

    public List<DelayTap> getTaps() {
        return taps;
    }

    public boolean isNeverBordered() { return true; }

    // override for tabbed UI
    public String getAlternate() { return "Channel"; }
}
