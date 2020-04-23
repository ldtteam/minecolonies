package com.minecolonies.coremod.entity.mobs.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.barbarians.IChiefBarbarianEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/**
 * Class for the Chief Barbarian entity.
 */
public class EntityChiefBarbarian extends AbstractEntityBarbarian implements IChiefBarbarianEntity
{

    /**
     * Constructor of the entity.
     * @param worldIn world to construct it in.
     * @param type the entity type.
     */
    public EntityChiefBarbarian(final EntityType type, final World worldIn)
    {
        super(type, worldIn);
    }
}
