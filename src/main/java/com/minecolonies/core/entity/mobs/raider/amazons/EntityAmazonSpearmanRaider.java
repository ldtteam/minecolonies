package com.minecolonies.core.entity.mobs.raider.amazons;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazonRaider;
import com.minecolonies.api.entity.mobs.amazons.IAmazonSpearman;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Class for the Amazon Spearman entity.
 */
public class EntityAmazonSpearmanRaider extends AbstractEntityAmazonRaider implements IAmazonSpearman
{
    /**
     * Constructor of the entity.
     *
     * @param type  the entity type
     * @param world the world to construct it in
     */
    public EntityAmazonSpearmanRaider(final EntityType<? extends AbstractEntityAmazonRaider> type, final Level world)
    {
        super(type, world);
    }
}
