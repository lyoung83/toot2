// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth;

import java.util.Iterator;
import java.util.List;
import uk.org.toot.service.*;
import uk.org.toot.synth.spi.SynthServiceProvider;

/**
 * SynthServices specialises Services with static methods to simplify the
 * provision of plugin synth services extending SynthChannel and SynthControls.
 */
public class SynthServices extends Services
{
    private static List<SynthServiceProvider> providers =
        new java.util.ArrayList<SynthServiceProvider>();

    static {
        scan();
    }

    protected SynthServices() { // prevent direct instantiation
    }

    public static String lookupModuleName(int providerId, int moduleId) {
        String name;
		for ( SynthServiceProvider provider : providers ) {
            if ( provider.getProviderId() == providerId ) {
	            name = provider.lookupName(moduleId);
    	        if ( name != null ) {
            	    return name;
            	}
            }
        }
        return null;
    }

    public static SynthControls createControls(String name) {
        SynthControls controls;
		for ( SynthServiceProvider provider : providers ) {
            controls = provider.createControls(name);
            if ( controls != null ) {
                controls.setProviderId(provider.getProviderId());
                return controls;
            }
        }
        return null;
    }

    public static SynthChannel createSynthChannel(SynthControls controls) {
        SynthChannel process;
		for ( SynthServiceProvider provider : providers ) {
            process = provider.createSynthChannel(controls);
            if ( process != null ) return process;
        }
        return null;
    }

    public static void scan() {
        Iterator<SynthServiceProvider> it = lookup(SynthServiceProvider.class);
        providers.clear();
        while ( it.hasNext() ) {
            providers.add((SynthServiceProvider)it.next());
        }
    }

    public static void accept(ServiceVisitor v, Class<?> clazz) {
		for ( SynthServiceProvider provider : providers ) {
            provider.accept(v, clazz);
        }
	}

	public static void printServiceDescriptors(Class<?> clazz) {
        accept(new ServicePrinter(), clazz);
    }

    public static void main(String[] args) {
        try {
	        printServiceDescriptors(null);
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        try {
            System.in.read();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}

