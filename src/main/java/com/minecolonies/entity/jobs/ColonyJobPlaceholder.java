package com.minecolonies.entity.jobs;

import com.minecolonies.entity.EntityCitizen;

public class ColonyJobPlaceholder extends ColonyJob
{
    public ColonyJobPlaceholder(EntityCitizen entity)
    {
        super(entity);
    }

    public String getName() { return "Placeholder"; }

    public boolean isNeeded() { return false; }
}
