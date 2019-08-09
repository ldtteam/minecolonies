package com.minecolonies.coremod.entity.mobs.pirates;

import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import com.minecolonies.api.entity.mobs.pirates.ICaptainPirateEntity;
import com.minecolonies.api.util.constant.LootTableConstants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Class for the Chief Pirate entity.
 */
public class EntityCaptainPirate extends AbstractEntityPirate implements ICaptainPirateEntity
{

    /**
     * Constructor of the entity.
     * @param worldIn world to construct it in.
     */
    public EntityCaptainPirate(final World worldIn)
    {
        super(worldIn);
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable()
    {
        return LootTableConstants.CHIEF_PIRATE_DROPS;
    }
}
