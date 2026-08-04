package com.SpecifcObjectHider;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class HiddenObject
{
    @EqualsAndHashCode.Include
    private final int id;

    @EqualsAndHashCode.Include
    private final int x;

    @EqualsAndHashCode.Include
    private final int y;

    @EqualsAndHashCode.Include
    private final int plane;

    private final String name;

    // Excluded from equality so toggling active state doesn't break set lookups
    @EqualsAndHashCode.Exclude
    private boolean disabled = false;
}