package com.minecolonies.api.entity.mobs.pirates;

import com.minecolonies.api.entity.mobs.IArcherMobEntity;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

public interface IArcherPirateEntity extends IPirateEntity, IArcherMobEntity
{
    /**
     * Loot table of the entity.
     */
    ResourceLocation LOOT_TABLE = new ResourceLocation(Constants.MOD_ID, "entitychiefpiratedrops");
}
