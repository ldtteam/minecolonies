package com.minecolonies.coremod.entity.mobs.amazons;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.api.entity.mobs.amazons.IAmazonSpearman;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/**
 * Class for the Spearman amazon entity
 */
public class EntityAmazonSpearman extends AbstractEntityAmazon implements IAmazonSpearman
{
    /**
     * Constructor of the entity.
     *
     * @param type  the entity type
     * @param world the world to construct it in
     */
    public EntityAmazonSpearman(final EntityType<? extends AbstractEntityAmazon> type, final World world)
    {
        super(type, world);
    }
}
