package com.minecolonies.coremod.commands.killcommands;

import net.minecraft.entity.passive.EntityChicken;

/**
 * Command for killing all chickens on map
 */
public class ChickenKillCommand extends AbstractKillCommand<EntityChicken>
{
    public static final String DESC = "chicken";

    /**
     * Initialize this SubCommand with it's parents.
     */
    public ChickenKillCommand()
    {
        super(DESC);
    }

    @Override
    public String getDesc()
    {
        return DESC;
    }

    @Override
    public Class<EntityChicken> getEntityClass()
    {
        return EntityChicken.class;
    }
}
