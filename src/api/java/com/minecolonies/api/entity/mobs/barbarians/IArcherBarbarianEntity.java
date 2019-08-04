package com.minecolonies.api.entity.mobs.barbarians;

import com.minecolonies.api.entity.mobs.IArcherMobEntity;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

public interface IArcherBarbarianEntity extends IBarbarianEntity, IArcherMobEntity
{
    /**
     * Loot table of the entity.
     */
    ResourceLocation LOOT_TABLE = new ResourceLocation(Constants.MOD_ID, "EntityArcherBarbarianDrops");
}
