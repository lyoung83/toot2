/* Copyright Steve Taylor 2006 */

package uk.org.toot.demo;

import uk.org.toot.audio.core.*;

public class DemoSourceProcess extends SimpleAudioProcess
{
    private DemoSourceControls vars;

    public DemoSourceProcess(DemoSourceControls controls) {
        vars = controls;
    }

    public int processAudio(AudioBuffer buffer) {
        // attach source name meta info so our mixer strip shows our name
        buffer.setMetaInfo(vars.getMetaInfo());
        buffer.setChannelFormat(ChannelFormat.MONO); // a mono input
        if ( vars.isEnabled() ) {
            float[] samples = buffer.getChannel(0);
            int ns = buffer.getSampleCount();
            for ( int i = 0; i < ns; i++ ) {
                samples[i] = (float)(0.1 * Math.sin(2 * Math.PI * i / ns));
            }
        } else { // probably either muted or not soloed in solo mode
            buffer.makeSilence();
        }
        return AUDIO_OK;
    }
}
