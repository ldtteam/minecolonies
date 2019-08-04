package com.minecolonies.api.entity.mobs.pirates;

import com.minecolonies.api.entity.mobs.IChiefMobEntity;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

public interface ICaptainPirateEntity extends IPirateEntity, IChiefMobEntity
{
    /**
     * Loot table of the entity.
     */
    ResourceLocation LOOT_TABLE = new ResourceLocation(Constants.MOD_ID, "entityarcherpiratedrops");
}
