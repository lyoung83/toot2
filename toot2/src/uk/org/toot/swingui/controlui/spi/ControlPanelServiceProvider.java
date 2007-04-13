// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.swingui.controlui.spi;

import java.util.List;
import uk.org.toot.control.CompoundControl;
import uk.org.toot.service.ServiceProvider;
import uk.org.toot.service.ServiceDescriptor;
import uk.org.toot.control.ControlSelector;
import uk.org.toot.swingui.controlui.PanelFactory;
import javax.swing.JComponent;

public abstract class ControlPanelServiceProvider extends ServiceProvider
{
    private List<ServiceDescriptor> uis;

    /**
     * Constructs a <code>ControlPanelServiceProvider</code> with a given
     * provider name, description and version identifier.
     *
     * @param providerName the provider name.
     * @param description the description of the provided services
     * @param version a version identifier.
     */
    public ControlPanelServiceProvider(int providerId, String providerName, String description, String version) {
        super(providerId, providerName, description, version);
        uis = service(JComponent.class);
    }

    /**
     * @param axis either 0 (BoxLayout.X_AXIS) or 1 (BoxLayout.Y_AXIS)
     */
    public abstract JComponent createControlPanel(CompoundControl c, int axis,
        	ControlSelector s, PanelFactory f, boolean hasBorder, boolean hasHeader);
}
