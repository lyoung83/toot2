// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.project;

/**
 * A ProjectListener listens for when the current project changes.
 */
public interface ProjectListener
{
    void open();
    void save();
}


