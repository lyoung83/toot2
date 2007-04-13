// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.audio.filter;

import java.util.Observer;

interface FilterDesign
{
    void design(int sampleRate);

    FilterSpecification getFilterSpecification();
}


