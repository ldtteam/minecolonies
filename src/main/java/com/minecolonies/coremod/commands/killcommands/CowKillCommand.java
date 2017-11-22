package com.minecolonies.coremod.commands.killcommands;

import net.minecraft.entity.passive.EntityCow;

/**
 * Command for killing all Cows on map
 */
public class CowKillCommand extends AbstractKillCommand<EntityCow>
{
    public static final String DESC = "cow";

    /**
     * Initialize this SubCommand with it's parents.
     */
    public CowKillCommand()
    {
        super(DESC);
    }

    @Override
    public String getDesc()
    {
        return DESC;
    }

    @Override
    public Class<EntityCow> getEntityClass()
    {
        return EntityCow.class;
    }
}
