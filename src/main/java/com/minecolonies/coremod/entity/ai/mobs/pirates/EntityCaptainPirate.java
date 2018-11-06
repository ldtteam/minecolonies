package com.minecolonies.coremod.entity.ai.mobs.pirates;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Class for the Chief Pirate entity.
 */
public class EntityCaptainPirate extends AbstractEntityPirate
{
    /**
     * Loot table of the entity.
     */
    public static final ResourceLocation LOOT_TABLE = new ResourceLocation(Constants.MOD_ID, "entityarcherpiratedrops");

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
        return LOOT_TABLE;
    }
}
