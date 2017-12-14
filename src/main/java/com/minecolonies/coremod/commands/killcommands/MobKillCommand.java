package com.minecolonies.coremod.commands.killcommands;

import net.minecraft.entity.monster.EntityMob;

/**
 * Command for killing all Mobs on map
 */
public class MobKillCommand extends AbstractKillCommand<EntityMob>
{
    public static final String DESC = "mob";

    /**
     * Initialize this SubCommand with it's parents.
     */
    public MobKillCommand()
    {
        super(DESC);
    }

    @Override
    public String getDesc()
    {
        return DESC;
    }

    @Override
    public Class<EntityMob> getEntityClass()
    {
        return EntityMob.class;
    }
}
