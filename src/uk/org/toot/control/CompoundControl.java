// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.control;

import java.util.List;
import java.util.Collections;
import java.util.Hashtable;

/**
 * A <code>CompoundControl</code>, such as a graphic equalizer, provides control
 * over two or more related properties, each of which is itself represented as
 * a <code>Control</code>.
 */
public abstract class CompoundControl extends Control
{
    public static final int MAX_INSTANCES = 8;

    /**
     * @link aggregation
     * @supplierCardinality 0..1 
     */
    private static CompoundControlPersistence persistence;

    /**
     * The set of member controls.
     * @link aggregationByValue
     * @supplierCardinality 0..*
     */
    protected List<Control> controls;

    int instanceIndex = 0;

    private Hashtable<Object, Object> properties;

    protected CompoundControl(int id, String name) {
        this(id, deriveInstanceIndex(name), name);
        // if name ends with #n, instanceIndex = n-1;
/*        int hash = name.lastIndexOf('#');
        if ( hash > 0 ) {
            instanceIndex = Integer.parseInt(name.substring(hash+1)) - 1;
        } */
    }

    protected CompoundControl(int id, int instanceIndex, String name) {
        super(id, name);
        checkInstanceIndex(instanceIndex);
        this.instanceIndex = instanceIndex;
    }

    protected static int deriveInstanceIndex(String name) {
        // if name ends with #n, instanceIndex = n-1;
        int hash = name.lastIndexOf('#');
        return ( hash > 0 ) ? Integer.parseInt(name.substring(hash+1)) - 1 : 0;
    }

	protected void checkInstanceIndex(int index) {
        if ( index < 0 )
            throw new IllegalArgumentException(getName()+" instance "+index+" < 0!");
        if ( index > 7 )
            throw new IllegalArgumentException(getName()+" instance "+index+" > 7!");
    }

    protected void add(Control control) {
        if ( control == null ) return;
        if ( controls == null ) {
            controls = new java.util.ArrayList<Control>();
        }
        controls.add(control);
        control.parent = this;
    }

    protected void remove(Control control) {
        if ( control == null ) return;
        controls.remove(control);
        control.parent = null;
    }

    /**
     * Returns the set of member controls that comprise the compound control.
     * @return the set of member controls.
     */
    public Control[] getMemberControls() {
        Control[] emptyArray = new Control[0];
        if ( controls == null ) return emptyArray;
        return controls.toArray(emptyArray);
    }

    public List<Control> getControls() {
        if ( controls == null ) return Collections.emptyList();
        return Collections.unmodifiableList(controls);
    }

    /**
     * Provides a string representation of the control
     * @return a string description
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < controls.size(); i++) {
            if (i != 0) {
                buf.append(", ");
                if ((i + 1) == controls.size()) {
                    buf.append("and ");
                }
            }
            buf.append(controls.get(i).getName());
        }
        return getName() + " Control containing " + buf + " Controls.";
    }

    public boolean isAlwaysVertical() { return false; }

    public boolean isAlwaysHorizontal() { return false; }

    public boolean isNeverBordered() { return false; }

    // override for tab
    public String getAlternate() { return null; }

    public int getProviderId() {
        return getParent().getProviderId(); // CoR
    }

    public int getInstanceIndex() { return instanceIndex; }

    @SuppressWarnings(value={"unchecked"})
    public <T> T find(Class<T> clazz) {
        if ( controls == null ) return null;
        for ( Control control : controls ) {
            if ( clazz.isInstance(control) ) {
                return (T)control;
            }
        }
        return null;
    }

    public Control find(String name) {
        if ( controls == null ) return null;
        for ( Control c : controls ) {
            if ( c.getName().equals(name) ) {
                return c;
            }
        }
        return null;
    }

    public CompoundControl find(int providerId, int moduleId, int instanceIndex) {
        // linear search for matching module
        for ( Control m : getMemberControls() ) {
            if ( m instanceof CompoundControl ) {
	            CompoundControl cc = (CompoundControl)m;
    	        if ( providerId == cc.getProviderId() &&
	    	         moduleId == cc.getId() &&
	        	     instanceIndex == cc.getInstanceIndex() ) {
 					return cc;
            	}
            }
        }
        return null;
    }

	public Control deepFind(int controlId) {
        // depth first search of control tree
        for ( Control c : getMemberControls() ) {
            if ( c instanceof CompoundControl ) {
                Control c2 = ((CompoundControl)c).deepFind(controlId);
                if ( c2 != null ) return c2;
            } else if ( controlId == c.getId() ) {
 				return c;
            }
        }
        return null;
    }

    public final Object getClientProperty(Object key) {
        if ( properties == null ) return null; // lazy instantiation intentional
        return properties.get(key);
    }

    public void putClientProperty(Object key, Object value) {
        if (properties == null) {
            properties = new Hashtable<Object, Object>();
        }
        properties.put(key, value);
    }

    public static CompoundControlPersistence getPersistence() {
        return persistence;
    }

    public static void setPersistence(CompoundControlPersistence p){
        persistence = p;
    }

    public boolean canBeMoved() { return true; }

    public boolean canBeMovedBefore() { return true; }

    public boolean canBeInsertedBefore() { return true; }

    public boolean canBeDeleted() { return true; }

    public boolean hasPresets() { return true; }

    /**
     * A ControlColumn groups certain Controls vertically.
     * It is always vertical and never bordered.
     */
    protected static class ControlColumn extends CompoundControl
    {
        public ControlColumn() {
            super(0, ""); // must be unnamed
        }

        public void add(Control c) { // make public
            super.add(c);
        }

        public boolean isAlwaysVertical() { return true; }

        public boolean isNeverBordered() { return true; }
    }

    /**
     * A ControlRow groups certain Controls horizontally.
     * It is always horizontal and never bordered.
     */
    protected static class ControlRow extends CompoundControl
    {
        public ControlRow() {
            super(0, ""); // must be unnamed
        }

        public void add(Control c) { // make public
            super.add(c);
        }

        public boolean isAlwaysHorizontal() { return true; }

        public boolean isNeverBordered() { return true; }
    }

    /**
     * A BypassControl is used if canBypass() is overridden to return true
     * (default is false).
     */
    public static class BypassControl extends BooleanControl
    {
        public BypassControl(int id) {
            super(id, "Bypass", true); // !!! !!! Id required, 127 ???
            // we set it to hidden because we don't want it to be automatically
            // displayed in the UI, we want to incorporate it in the UI header.
            setHidden(true);
        }
    }
}
