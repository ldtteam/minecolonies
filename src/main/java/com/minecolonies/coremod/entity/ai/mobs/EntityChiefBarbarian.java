package com.minecolonies.coremod.entity.ai.mobs;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityChiefBarbarian extends AbstractEntityBarbarian
{
    public static final ResourceLocation LOOT = new ResourceLocation(Constants.MOD_ID, "EntityChiefBarbarianDrops");

    /**
     * Constructor method for Chief Barbarian Entity
     *
     * @param worldIn The world that it is in/
     */
    public EntityChiefBarbarian(final World worldIn)
    {
        super(worldIn);
    }
}
