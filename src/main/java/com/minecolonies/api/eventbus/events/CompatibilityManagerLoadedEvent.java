package com.minecolonies.api.eventbus.events;

public class CompatibilityManagerLoadedEvent extends AbstractModEvent
{
    private final boolean isClientSide;

    public CompatibilityManagerLoadedEvent(final boolean isClientSide)
    {
        this.isClientSide = isClientSide;
    }

    public boolean isClientSide()
    {
        return this.isClientSide;
    }
}
