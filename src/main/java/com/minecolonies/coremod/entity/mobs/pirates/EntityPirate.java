package com.minecolonies.coremod.entity.mobs.pirates;

import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import com.minecolonies.api.entity.mobs.pirates.IMeleePirateEntity;
import com.minecolonies.api.util.constant.LootTableConstants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Class for the Pirate entity.
 */
public class EntityPirate extends AbstractEntityPirate implements IMeleePirateEntity
{

    /**
     * Constructor of the entity.
     * @param worldIn world to construct it in.
     */
    public EntityPirate(final World worldIn)
    {
        super(worldIn);
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable()
    {
        return LootTableConstants.MELEE_PIRATE_DROPS;
    }
}
