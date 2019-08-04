package com.minecolonies.api.entity.mobs.barbarians;

import com.minecolonies.api.entity.mobs.IMeleeMobEntity;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

public interface IMeleeBarbarianEntity extends IMeleeMobEntity, IBarbarianEntity
{
    /**
     * Loot table of the entity.
     */
    ResourceLocation LOOT_TABLE = new ResourceLocation(Constants.MOD_ID, "EntityBarbarianDrops");
}
