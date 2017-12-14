package com.minecolonies.coremod.commands.killcommands;

import net.minecraft.entity.passive.EntityPig;

/**
 * Command for killing all Cows on map
 */
public class PigKillCommand extends AbstractKillCommand<EntityPig>
{
    public static final String DESC = "pig";

    /**
     * Initialize this SubCommand with it's parents.
     */
    public PigKillCommand()
    {
        super(DESC);
    }

    @Override
    public String getDesc()
    {
        return DESC;
    }

    @Override
    public Class<EntityPig> getEntityClass()
    {
        return EntityPig.class;
    }
}
