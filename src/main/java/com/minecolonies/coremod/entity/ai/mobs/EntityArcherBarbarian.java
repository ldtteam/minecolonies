package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityArcherBarbarian extends AbstractEntityBarbarian
{
    public static final ResourceLocation LOOT = new ResourceLocation(Constants.MOD_ID, "EntityArcherBarbarianDrops");

    /**
     * Constructor method for Archer Barbarian Entity
     *
     * @param worldIn The world that it is in/
     */
    public EntityArcherBarbarian(final World worldIn)
    {
        super(worldIn);
    }
}
