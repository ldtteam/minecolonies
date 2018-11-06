package com.minecolonies.coremod.entity.ai.mobs.barbarians;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Class for the Chief Barbarian entity.
 */
public class EntityChiefBarbarian extends AbstractEntityBarbarian
{
    /**
     * Loot table of the entity.
     */
    public static final ResourceLocation LOOT_TABLE = new ResourceLocation(Constants.MOD_ID, "EntityChiefBarbarianDrops");

    /**
     * Constructor of the entity.
     * @param worldIn world to construct it in.
     */
    public EntityChiefBarbarian(final World worldIn)
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
