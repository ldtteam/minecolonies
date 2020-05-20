package com.minecolonies.coremod.entity.mobs.amazons;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.api.entity.mobs.amazons.IAmazonChief;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/**
 * Class for the Amazon Chief entity.
 */
public class EntityAmazonChief extends AbstractEntityAmazon implements IAmazonChief
{

    /**
     * Constructor of the entity.
     * @param type the entity type.
     * @param worldIn world to construct it in.
     */
    public EntityAmazonChief(final EntityType type, final World worldIn)
    {
        super(type, worldIn);
    }
}
