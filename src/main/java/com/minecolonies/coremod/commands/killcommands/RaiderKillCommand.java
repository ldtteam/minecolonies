package com.minecolonies.coremod.commands.killcommands;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;

/**
 * Command for killing all Barbarians on map
 */
public class RaiderKillCommand extends AbstractKillCommand<AbstractEntityMinecoloniesMob>
{
    public static final String DESC = "raiders";

    /**
     * Initialize this SubCommand with it's parents.
     */
    public RaiderKillCommand()
    {
        super(DESC);
    }

    @Override
    public String getDesc()
    {
        return DESC;
    }

    @Override
    public Class<AbstractEntityMinecoloniesMob> getEntityClass()
    {
        return AbstractEntityMinecoloniesMob.class;
    }
}
