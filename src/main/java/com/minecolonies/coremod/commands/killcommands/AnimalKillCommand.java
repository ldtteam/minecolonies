package com.minecolonies.coremod.commands.killcommands;

import net.minecraft.entity.passive.EntityAnimal;

/**
 * Command for killing all Animals on map
 */
public class AnimalKillCommand extends AbstractKillCommand<EntityAnimal>
{
    public static final String DESC = "animals";

    /**
     * Initialize this SubCommand with it's parents.
     */
    public AnimalKillCommand()
    {
        super(DESC);
    }

    @Override
    public String getDesc()
    {
        return DESC;
    }

    @Override
    public Class<EntityAnimal> getEntityClass()
    {
        return EntityAnimal.class;
    }
}
