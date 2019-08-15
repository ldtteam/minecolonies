package com.minecolonies.coremod.entity.mobs.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.barbarians.IMeleeBarbarianEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Class for the Barbarian entity.
 */
public class EntityBarbarian extends AbstractEntityBarbarian implements IMeleeBarbarianEntity
{

    /**
     * Constructor of the entity.
     * @param worldIn world to construct it in.
     */
    public EntityBarbarian(final EntityType type, final World worldIn)
    {
        super(type, worldIn);
    }
}
