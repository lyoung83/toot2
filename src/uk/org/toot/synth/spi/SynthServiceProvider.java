// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.spi;

import java.util.List;
import uk.org.toot.service.*;
import uk.org.toot.control.spi.*;
import uk.org.toot.synth.*;

abstract public class SynthServiceProvider extends ServiceProvider
{
    private List<ServiceDescriptor> controls;

    /**
     * Constructs an <code>SynthServiceProvider</code> with a given
     * provider name and version identifier.
     *
     * @param providerName the provider name.
     * @param version a version identifier.
     *
     * @exception IllegalArgumentException if <code>providerName</code>
     * is <code>null</code>.
     * @exception IllegalArgumentException if <code>version</code>
     * is <code>null</code>.
     */
    public SynthServiceProvider(int providerId, String providerName, String description, String version) {
        super(providerId, providerName, description, version);
        controls = service(SynthControls.class);
    }

    public String lookupName(int moduleId) {
        for ( ServiceDescriptor d : controls ) {
            try {
	            if ( ((ControlServiceDescriptor)d).getModuleId() == moduleId ) {
    	            return d.getName();
        	    }
            } catch ( Exception e ) {
                e.printStackTrace();
			}
        }
        return null;
    }

    public SynthControls createControls(int moduleId) {
        for ( ServiceDescriptor d : controls ) {
            try {
	            if ( ((ControlServiceDescriptor)d).getModuleId() == moduleId ) {
    	            return (SynthControls)d.getServiceClass().newInstance();
        	    }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Adds a ControlServiceDescriptor for the matching service.
     * The service can cope with any channel format.
     */
    protected void addControls(Class clazz, int moduleId, String name, String description, String version) {
        add(new SynthControlServiceDescriptor(clazz, moduleId, name, description, version));
    }

    public SynthControls createControls(String name) {
        for ( ServiceDescriptor d : controls ) {
            try {
	            if ( d.getName().equals(name) ) {
    	            return (SynthControls)d.getServiceClass().newInstance();
        	    }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        return null;
    }

	public abstract SynthChannel createSynthChannel(SynthControls controls2);

/*    public Iterator<ServiceDescriptor> controlsDescriptors() {
        return controls.iterator();
    }

    public Iterator<ServiceDescriptor> processDescriptors() {
        return processors.iterator();
    } */


}
