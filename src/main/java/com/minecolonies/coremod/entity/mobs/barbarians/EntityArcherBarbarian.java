package com.minecolonies.coremod.entity.mobs.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.barbarians.IArcherBarbarianEntity;
import com.minecolonies.api.util.constant.LootTableConstants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Class for the Archer Barbarian entity.
 */
public class EntityArcherBarbarian extends AbstractEntityBarbarian implements IArcherBarbarianEntity
{

    /**
     * Constructor of the entity.
     * @param worldIn world to construct it in.
     */
    public EntityArcherBarbarian(final World worldIn)
    {
        super(worldIn);
    }

    @Nullable
    @Override
    protected ResourceLocation getLootTable()
    {
        return LootTableConstants.ARCHER_BARBARIAN_DROPS;
    }
}
