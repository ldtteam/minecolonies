package com.minecolonies.api.entity.mobs.pirates;

import com.minecolonies.api.entity.mobs.IMeleeMobEntity;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

public interface IMeleePirateEntity extends IPirateEntity, IMeleeMobEntity
{
    /**
     * Loot table of the entity.
     */
    ResourceLocation LOOT_TABLE = new ResourceLocation(Constants.MOD_ID, "entitypiratedrops");
}
