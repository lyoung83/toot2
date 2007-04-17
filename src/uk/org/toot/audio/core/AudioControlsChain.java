// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.audio.core;

import java.util.List;
import uk.org.toot.service.*;
import uk.org.toot.control.CompoundControlChain;
import uk.org.toot.control.CompoundControl;
import uk.org.toot.audio.spi.AudioControlServiceDescriptor;

/**
 * An AudioControlsChain extends CompoundControlChain to provide
 * information regarding audio control services which may be plugged in.
 */
public class AudioControlsChain extends CompoundControlChain
{
    private String sourceLabel;

    /**
     * null means there are no format constraints
     * so all descriptors are available for insert
     * if set non-null, only compatible descriptors are returned
     * @supplierCardinality 0..1
     * @link aggregation 
     */
    private ChannelFormat constraintChannelFormat = null;

    public AudioControlsChain(int id, String name) {
        super(id, name);
    }

    public AudioControlsChain(int id, int index, String name, ChannelFormat constraintFormat) {
        super(id, index, name);
        constraintChannelFormat = constraintFormat;
    }

    public void setSourceLabel(String label) {
//        System.out.println(getName()+": "+label);
        sourceLabel = label;
        setChanged();
        notifyObservers();
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public ChannelFormat getConstraintChannelFormat() {
        return constraintChannelFormat;
    }

    protected CompoundControl createControl(String name) {
        return AudioServices.createControls(name);
    }

	protected void checkInstanceIndex(int index) {
        if ( index < 0 )
            throw new IllegalArgumentException(getName()+" instance "+index+" < 0!");
        if ( index > 127 )
            throw new IllegalArgumentException(getName()+" instance "+index+" > 127!");
    }

    protected boolean isCompatibleDescriptor(ServiceDescriptor d) {
        if ( constraintChannelFormat == null ) return true; // we're not fixed format
        if ( d instanceof AudioControlServiceDescriptor ) {
            AudioControlServiceDescriptor acsd =
                (AudioControlServiceDescriptor)d;
            ChannelFormat descriptorFormat = acsd.getChannelFormat();
			if ( descriptorFormat == null ) return true; // plugin can cope
            if ( descriptorFormat.getCount() > constraintChannelFormat.getCount() ) {
/*                System.out.println(getName()+" ("+channelFormat.getName()+
                    ") is incompatible with "+acsd.getName()+" ("+
                    descriptorFormat.getName()+"), "+descriptorFormat.getCount()+
                    " > "+channelFormat.getCount()); */
                // plugin requires more channels than allowed
                return false;
            }
        }
        return true;
    }

    // intended for use by UIs
    // to create a popup menu tree by category from descriptors
	public List<ServiceDescriptor> descriptors() {
        final List<ServiceDescriptor> descriptors =
            new java.util.ArrayList<ServiceDescriptor>();
        AudioServices.accept(
            new ServiceVisitor() {
            	public void visitDescriptor(ServiceDescriptor d) {
                	if ( isCompatibleDescriptor(d) ) {
                		descriptors.add(d);
                	}
            	}
        	}, AudioControls.class
        );
        return descriptors;
    }

/*    public void notifyParent(uk.org.toot.control.Control control) {
        Thread thread = Thread.currentThread();
    	if ( thread.getPriority() > 7 ) {
        	thread.dumpStackTrace();
    	}
        super.notifyParent(control);
    } */
}
