package com.minecolonies.coremod.entity.ai.mobs.barbarians;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Class for the Barbarian entity.
 */
public class EntityBarbarian extends AbstractEntityBarbarian
{
    /**
     * Loot table of the entity.
     */
    public static final ResourceLocation LOOT_TABLE = new ResourceLocation(Constants.MOD_ID, "EntityBarbarianDrops");

    /**
     * Constructor of the entity.
     * @param worldIn world to construct it in.
     */
    public EntityBarbarian(final World worldIn)
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
