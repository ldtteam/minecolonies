package com.minecolonies.coremod.entity.mobs.egyptians;

import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import com.minecolonies.api.entity.mobs.egyptians.IPharaoEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/**
 * Class for the Pharao entity.
 */
public class EntityPharao extends AbstractEntityEgyptian implements IPharaoEntity
{

    /**
     * Constructor of the entity.
     * @param type the entity type.
     * @param worldIn world to construct it in.
     */
    public EntityPharao(final EntityType type, final World worldIn)
    {
        super(type, worldIn);
    }
}
