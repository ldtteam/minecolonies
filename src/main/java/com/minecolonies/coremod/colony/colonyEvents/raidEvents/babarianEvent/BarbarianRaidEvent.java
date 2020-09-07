package com.minecolonies.coremod.colony.colonyEvents.raidEvents.babarianEvent;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.HordeRaidEvent;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityBarbarian;
import com.minecolonies.coremod.entity.mobs.barbarians.EntityChiefBarbarian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import static com.minecolonies.api.entity.ModEntities.*;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_BARBARIAN;

/**
 * Barbarian raid event for the colony, triggers a horde of barbarians which spawn and attack the colony.
 */
public class BarbarianRaidEvent extends HordeRaidEvent
{
    /**
     * This raids event id, registry entries use res locations as ids.
     */
    public static final ResourceLocation BABARIAN_RAID_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "babarian_raid");

    public BarbarianRaidEvent(IColony colony)
    {
        super(colony);
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return BABARIAN_RAID_EVENT_TYPE_ID;
    }

    @Override
    public void registerEntity(final Entity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob) || !entity.isAlive())
        {
            entity.remove();
            return;
        }

        if (entity instanceof EntityChiefBarbarian && boss.keySet().size() < horde.numberOfBosses)
        {
            boss.put(entity, entity.getUniqueID());
            return;
        }

        if (entity instanceof EntityArcherBarbarian && archers.keySet().size() < horde.numberOfArchers)
        {
            archers.put(entity, entity.getUniqueID());
            return;
        }

        if (entity instanceof EntityBarbarian && normal.keySet().size() < horde.numberOfRaiders)
        {
            normal.put(entity, entity.getUniqueID());
            return;
        }

        entity.remove();
    }

    @Override
    public void onEntityDeath(final LivingEntity entity)
    {
        if (!(entity instanceof AbstractEntityMinecoloniesMob))
        {
            return;
        }

        if (entity instanceof EntityChiefBarbarian)
        {
            boss.remove(entity);
            horde.numberOfBosses--;
        }

        if (entity instanceof EntityArcherBarbarian)
        {
            archers.remove(entity);
            horde.numberOfArchers--;
        }

        if (entity instanceof EntityBarbarian)
        {
            normal.remove(entity);
            horde.numberOfRaiders--;
        }

        horde.hordeSize--;

        if (horde.hordeSize == 0)
        {
            status = EventStatus.DONE;
        }

        sendHordeMessage();
    }

    /**
     * Loads the event from the nbt compound.
     *
     * @param colony   colony to load into
     * @param compound NBTcompound with saved values
     * @return the raid event.
     */
    public static BarbarianRaidEvent loadFromNBT(final IColony colony, final CompoundNBT compound)
    {
        BarbarianRaidEvent event = new BarbarianRaidEvent(colony);
        event.readFromNBT(compound);
        return event;
    }

    @Override
    public EntityType<?> getNormalRaiderType()
    {
        return BARBARIAN;
    }

    @Override
    public EntityType<?> getArcherRaiderType()
    {
        return ARCHERBARBARIAN;
    }

    @Override
    public EntityType<?> getBossRaiderType()
    {
        return CHIEFBARBARIAN;
    }

    @Override
    protected ITextComponent getDisplayName()
    {
        return new StringTextComponent(LanguageHandler.format(RAID_BARBARIAN));
    }
}
