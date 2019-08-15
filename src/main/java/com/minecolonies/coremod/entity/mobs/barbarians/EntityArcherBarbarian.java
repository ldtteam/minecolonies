package com.minecolonies.coremod.entity.mobs.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.barbarians.IArcherBarbarianEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Class for the Archer Barbarian entity.
 */
public class EntityArcherBarbarian extends AbstractEntityBarbarian implements IArcherBarbarianEntity
{

    /**
     * Constructor of the entity.
     * @param worldIn world to construct it in.
     */
    public EntityArcherBarbarian(final EntityType type, final World worldIn)
    {
        super(type, worldIn);
    }
}
