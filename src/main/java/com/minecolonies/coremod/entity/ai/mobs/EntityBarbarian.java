package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityBarbarian extends AbstractEntityBarbarian
{
    public static final ResourceLocation LOOT = new ResourceLocation(Constants.MOD_ID, "EntityBarbarianDrops");

    /**
     * Constructor method for Barbarian Entity
     *
     * @param worldIn The world that it is in/
     */
    public EntityBarbarian(final World worldIn)
    {
        super(worldIn);
    }
}
