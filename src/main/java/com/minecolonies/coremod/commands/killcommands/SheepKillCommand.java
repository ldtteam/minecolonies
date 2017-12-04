package com.minecolonies.coremod.commands.killcommands;

import net.minecraft.entity.passive.EntitySheep;

/**
 * Command for killing all Sheep on map
 */
public class SheepKillCommand extends AbstractKillCommand<EntitySheep>
{
    public static final String DESC = "sheep";

    /**
     * Initialize this SubCommand with it's parents.
     */
    public SheepKillCommand()
    {
        super(DESC);
    }

    @Override
    public String getDesc()
    {
        return DESC;
    }

    @Override
    public Class<EntitySheep> getEntityClass()
    {
        return EntitySheep.class;
    }
}
