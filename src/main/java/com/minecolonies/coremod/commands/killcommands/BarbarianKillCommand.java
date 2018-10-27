package com.minecolonies.coremod.commands.killcommands;

import com.minecolonies.coremod.entity.ai.mobs.AbstractEntityMinecoloniesMob;

/**
 * Command for killing all Barbarians on map
 */
public class BarbarianKillCommand extends AbstractKillCommand<AbstractEntityMinecoloniesMob>
{
    public static final String DESC = "barbarians";

    /**
     * Initialize this SubCommand with it's parents.
     */
    public BarbarianKillCommand()
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
