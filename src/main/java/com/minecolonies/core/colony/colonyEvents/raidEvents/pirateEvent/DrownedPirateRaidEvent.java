package com.minecolonies.core.colony.colonyEvents.raidEvents.pirateEvent;

import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.EventStatus;
import com.minecolonies.api.colony.colonyEvents.IColonyEvent;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.colonyEvents.raidEvents.AbstractShipRaidEvent;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.STORAGE_STYLE;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_EVENT_MESSAGE_PIRATE;
import static com.minecolonies.api.util.constant.TranslationConstants.RAID_PIRATE;

/**
 * The Pirate raid event, spawns a ship with pirate spawners onboard.
 */
public class DrownedPirateRaidEvent extends AbstractShipRaidEvent
{
    /**
     * This raids event id, registry entries use res locations as ids.
     */
    public static final ResourceLocation PIRATE_RAID_EVENT_TYPE_ID = new ResourceLocation(Constants.MOD_ID, "drowned_pirate_raid");

    /**
     * Ship description
     */
    public static final String SHIP_NAME = "sunk_ship";

    /**
     * Depth requirement.
     */
    public static final int DEPTH_REQ = 13;

    /**
     * Create a new Pirate raid event.
     *
     * @param colony the colony.
     */
    public DrownedPirateRaidEvent(@NotNull final IColony colony)
    {
        super(colony);
    }

    @Override
    public String getShipDesc()
    {
        return SHIP_NAME;
    }

    @Override
    public ResourceLocation getEventTypeID()
    {
        return PIRATE_RAID_EVENT_TYPE_ID;
    }

    @Override
    public void onStart()
    {
        status = EventStatus.PREPARING;

        ServerFutureProcessor.queueBlueprint(new ServerFutureProcessor.BlueprintProcessingData(StructurePacks.getBlueprintFuture(STORAGE_STYLE,
          "decorations" + ShipBasedRaiderUtils.SHIP_FOLDER + shipSize.schematicPrefix + this.getShipDesc() + ".blueprint"), colony.getWorld(), (blueprint -> {
            blueprint.rotateWithMirror(BlockPosUtil.getRotationFromRotations(shipRotation), Mirror.NONE, colony.getWorld());

            if (spawnPathResult != null && spawnPathResult.isDone())
            {
                final Path path = spawnPathResult.getPath();
                if (path != null && path.canReach())
                {
                    final BlockPos endpoint = path.getEndNode().asBlockPos().below();
                    if (ShipBasedRaiderUtils.canPlaceShipAt(endpoint, blueprint, colony.getWorld(), DEPTH_REQ))
                    {
                        while (PathfindingUtils.isLiquid(colony.getWorld().getBlockState(spawnPoint)))
                        {
                            spawnPoint = spawnPoint.below();
                        }
                    }
                }
                this.wayPoints = ShipBasedRaiderUtils.createWaypoints(colony.getWorld(), path, WAYPOINT_SPACING);
            }

            if (!ShipBasedRaiderUtils.canPlaceShipAt(spawnPoint, blueprint, colony.getWorld(), DEPTH_REQ))
            {
                while (PathfindingUtils.isLiquid(colony.getWorld().getBlockState(spawnPoint)))
                {
                    spawnPoint = spawnPoint.below();
                }
            }

            if (!ShipBasedRaiderUtils.spawnPirateShip(spawnPoint, colony, blueprint, this))
            {
                // Ship event not successfully started.
                status = EventStatus.CANCELED;
                return;
            }

            updateRaidBar();

            MessageUtils.format(RAID_EVENT_MESSAGE_PIRATE + shipSize.messageID, BlockPosUtil.calcDirection(colony.getCenter(), spawnPoint), colony.getName())
              .withPriority(MessageUtils.MessagePriority.DANGER)
              .sendTo(colony).forManagers();
            colony.markDirty();
        })));
    }

    /**
     * Loads the raid event from the given nbt.
     *
     * @param colony   the events colony
     * @param compound the NBT compound
     * @return the colony to load.
     */
    public static IColonyEvent loadFromNBT(@NotNull final IColony colony, @NotNull final CompoundTag compound)
    {
        final DrownedPirateRaidEvent raidEvent = new DrownedPirateRaidEvent(colony);
        raidEvent.deserializeNBT(compound);
        return raidEvent;
    }

    @Override
    public boolean isUnderWater()
    {
        return true;
    }

    @Override
    public EntityType<?> getNormalRaiderType()
    {
        return ModEntities.DROWNED_PIRATE;
    }

    @Override
    public EntityType<?> getArcherRaiderType()
    {
        return ModEntities.DROWNED_ARCHERPIRATE;
    }

    @Override
    public EntityType<?> getBossRaiderType()
    {
        return ModEntities.DROWNED_CHIEFPIRATE;
    }

    @Override
    protected MutableComponent getDisplayName()
    {
        return Component.translatable(RAID_PIRATE);
    }
}
