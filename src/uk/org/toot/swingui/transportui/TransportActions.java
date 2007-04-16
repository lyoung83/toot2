/* Generated by TooT */

package uk.org.toot.swingui.transportui;

import uk.org.toot.transport.*;
import java.awt.event.ActionEvent;
import javax.swing.Action;

import uk.org.toot.audio.server.NonRealTimeAudioServer;

/**
 * Transport Actions, Play, Stop, Record
 */
public class TransportActions
{
    private TransportAction loopAction = new LoopAction(); // !!! !!!
    private TransportAction stopAction = new StopAction();
    private TransportAction playAction = new PlayAction();
    private TransportAction recordAction = new RecordAction();
    private TransportAction realTimeAction = null;
    private TransportListener transportListener;

    private Transport transport;

    public TransportActions(Transport t) {
        transport = t;
        // add transport listener to follow external state changes
        transportListener = new TransportAdapter() {
           	public void stop() {
                stopAction.setSelected(true);
                playAction.setSelected(false);
                if ( realTimeAction != null ) realTimeAction.setEnabled(true);
           	}
           	public void play() {
                stopAction.setSelected(false);
                playAction.setSelected(true);
                if ( realTimeAction != null ) realTimeAction.setEnabled(false);
           	}
    	    public void record(boolean rec) {
           		recordAction.setSelected(transport.isRecording());
   	        }
       	};
        transport.addTransportListener(transportListener);
    }

    public void dispose() {
        transport.removeTransportListener(transportListener);
        transportListener = null;
        loopAction = null;
        stopAction = null;
        playAction = null;
        recordAction = null;
        realTimeAction = null;
    }

    public Transport getTransport() { return transport; }

    public Action getLoopAction() { return loopAction; } // !!! !!!
    public Action getStopAction() { return stopAction; }
    public Action getPlayAction() { return playAction; }
    public Action getRecordAction() { return recordAction; }

    public Action getRealTimeAction(NonRealTimeAudioServer server) {
        realTimeAction = new RealTimeAction(server);
        return realTimeAction;

    }

    public void enableActions(boolean enable) {
        loopAction.setEnabled(enable);
        stopAction.setEnabled(enable);
        playAction.setEnabled(enable);
        recordAction.setEnabled(enable);
        if ( realTimeAction != null ) realTimeAction.setEnabled(enable);
    }

    static public class LoopAction extends TransportAction {
        public LoopAction() {
            putValue(Action.NAME, "Loop");
            putValue(Action.SMALL_ICON, icon("general/Refresh16", "Loop"));
            putValue(Action.SHORT_DESCRIPTION, "Loop");
        }
        public void actionPerformed(ActionEvent e) {
//            sequencer.setLooping(!sequencer.isLooping());
        }
    } // end of LoopAction

    public class StopAction extends TransportAction {
        public StopAction() {
            putValue(Action.NAME, "Stop");
            putValue(Action.SMALL_ICON, icon("media/Stop16", "Stop"));
            putValue(Action.SHORT_DESCRIPTION, "Stop");
			setSelected(true);
        }
        public void actionPerformed(ActionEvent e) {
	        boolean reset = !transport.isPlaying();
    	    transport.stop();
            // reset position on second and subsequent stops, like Cubase
        	    if ( reset ) {
//            	    sequencer.setMicrosecondPosition(0);
//                    updatePosition();
                }
        }
    } // end of StopAction

    public class PlayAction extends TransportAction {
        public PlayAction() {
            putValue(Action.NAME, "Play");
            putValue(Action.SMALL_ICON, icon("media/Play16", "Play"));
            putValue(Action.SHORT_DESCRIPTION, "Play");
        }
        public void actionPerformed(ActionEvent e) {
    	    transport.play();
        }
    } // end of PlayAction

    public class RecordAction extends TransportAction {
        public RecordAction() {
            putValue(Action.NAME, "Record");
            putValue(Action.SMALL_ICON, icon("general/Stop16", "Record"));
            putValue(Action.SHORT_DESCRIPTION, "Record");
        }
        public void actionPerformed(ActionEvent e) {
            transport.record(!transport.isRecording());
        }
    } // end of RecordAction

    static public class RealTimeAction extends TransportAction {
        private NonRealTimeAudioServer server;
        public RealTimeAction(NonRealTimeAudioServer server) {
            this.server = server;
            setSelected(true); // default real-time
            putValue(Action.NAME, "RealTime");
//            putValue(Action.SMALL_ICON, icon("general/Stop16", "Record"));
            putValue(Action.SHORT_DESCRIPTION, "RealTime");
        }
        public void actionPerformed(ActionEvent e) {
            setSelected(!isSelected());
			server.setRealTime(isSelected());
        }
    } // end of RealTimeAction
}


