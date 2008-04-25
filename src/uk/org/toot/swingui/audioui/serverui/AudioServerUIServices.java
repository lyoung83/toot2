// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.serverui;

import java.util.Iterator;
import java.util.Properties;
import uk.org.toot.service.*;
import uk.org.toot.audio.server.*;
import uk.org.toot.swingui.audioui.serverui.spi.AudioServerUIServiceProvider;
import javax.swing.*;

public class AudioServerUIServices extends Services
{
    protected AudioServerUIServices() {
    }

    /**
     * Allow changes to server and sample.rate properties
     */
    public static JPanel createServerSetupUI(Properties properties) {
 		return new AudioServerSetup(properties);
    }

    public static JComponent createServerUI(AudioServer server, AudioServerConfiguration p) {
        JComponent ui;
        Iterator<AudioServerUIServiceProvider> it = providers();
        while ( it.hasNext() ) {
            ui = it.next().createServerUI(server, p);
            if ( ui != null ) return ui;
        }
        return null;
    }

    public static Iterator<AudioServerUIServiceProvider> providers() {
        return lookup(AudioServerUIServiceProvider.class);
    }

    public static void accept(ServiceVisitor v, Class<?> clazz) {
        Iterator<AudioServerUIServiceProvider> pit = providers();
        while ( pit.hasNext() ) {
            AudioServerUIServiceProvider asp = pit.next();
            asp.accept(v, clazz);
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

