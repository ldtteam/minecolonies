package com.minecolonies.coremod.colony.colonyEvents.raidEvents.norsemenevent;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.AbstractShipRaidEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.TranslationConstants.RAID_NORSEMEN;

/**
 * The Norsemen raid event, spawns a ship with Norsemen spawners onboard.
 */
public class NorsemenShipRaidEvent extends AbstractShipRaidEvent
{
    /**
     * This raids event id, registry entries use res locations as ids.
     */
    public static final ResourceLocation NORSEMEN_RAID_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "norsemen_ship_raid");

    /**
     * Create a new Norsemen raid event.
     *
     * @param colony the colony.
     */
    public NorsemenShipRaidEvent(@NotNull final IColony colony)
    {
        super(colony);
    }

    @Override
    public String getShipDesc()
    {
        return "norsemen_ship";
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return NORSEMEN_RAID_EVENT_TYPE_ID;
    }

    @Override
    protected void updateRaidBar()
    {
        super.updateRaidBar();
        raidBar.setCreateFog(true);
    }

    /**
     * Loads the raid event from the given nbt.
     *
     * @param colony   the events colony
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static IColonyEvent loadFromNBT(@NotNull final IColony colony, @NotNull final CompoundNBT compound)
    {
        final NorsemenShipRaidEvent raidEvent = new NorsemenShipRaidEvent(colony);
        raidEvent.readFromNBT(compound);
        return raidEvent;
    }

    @Override
    public EntityType<?> getNormalRaiderType()
    {
        return ModEntities.SHIELDMAIDEN;
    }

    @Override
    public EntityType<?> getArcherRaiderType()
    {
        return ModEntities.NORSEMEN_ARCHER;
    }

    @Override
    public EntityType<?> getBossRaiderType()
    {
        return ModEntities.NORSEMEN_CHIEF;
    }

    @Override
    protected ITextComponent getDisplayName()
    {
        return new StringTextComponent(LanguageHandler.format(RAID_NORSEMEN));
    }
}
