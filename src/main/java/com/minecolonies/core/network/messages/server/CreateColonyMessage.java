package com.minecolonies.core.network.messages.server;

import com.ldtteam.common.network.AbstractServerPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.event.ColonyCreatedEvent;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.network.messages.client.colony.OpenBuildingUIMessage;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.MineColonies;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

import static com.minecolonies.api.util.constant.BuildingConstants.DEACTIVATED;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Message for trying to create a new colony.
 */
public class CreateColonyMessage extends AbstractServerPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "create_colony", CreateColonyMessage::new);

    /**
     * Town hall position to create building on.
     */
    private final BlockPos townHall;

    /**
     * If claim action.
     */
    private final boolean claim;

    /**
     * The colony name.
     */
    private final String colonyName;

    /**
     * The structure pack name.
     */
    private final String packName;

    /**
     * The structure path name.
     */
    private final String pathName;

    public CreateColonyMessage(final BlockPos townHall, final boolean claim, final String colonyName, final String packName, final String pathName)
    {
        super(TYPE);
        this.townHall = townHall;
        this.claim = claim;
        this.colonyName = colonyName;
        this.packName = packName;
        this.pathName = pathName;
    }

    @Override
    protected void toBytes(final RegistryFriendlyByteBuf buf)
    {
        buf.writeBlockPos(townHall);
        buf.writeBoolean(claim);
        buf.writeUtf(colonyName);
        buf.writeUtf(packName);
        buf.writeUtf(pathName);
    }

    protected CreateColonyMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        townHall = buf.readBlockPos();
        claim = buf.readBoolean();
        colonyName = buf.readUtf(32767);
        packName = buf.readUtf(32767);
        pathName = buf.readUtf(32767);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final ServerPlayer sender)
    {
        final ServerLevel world = sender.serverLevel();

        final IColony colony = IColonyManager.getInstance().getClosestColony(world, townHall);

        String pack = packName;
        final BlockEntity tileEntity = world.getBlockEntity(townHall);

        if (!(tileEntity instanceof final TileEntityColonyBuilding hut))
        {
            MessageUtils.format(WARNING_TOWN_HALL_NO_TILE_ENTITY)
              .withPriority(MessagePriority.DANGER)
              .sendTo(sender);
            return;
        }

        if (hut.getStructurePack() != null && claim)
        {
            pack = hut.getStructurePack().getName();
        }

        final boolean reactivate = hut.getPositionedTags().getOrDefault(BlockPos.ZERO, new ArrayList<>()).contains(DEACTIVATED);
        if (reactivate)
        {
            hut.reactivate();
            if (hut.getStructurePack() != null)
            {
                pack = hut.getStructurePack().getName();
            }
        }

        hut.setStructurePack(StructurePacks.getStructurePack(pack));
        hut.setBlueprintPath(pathName);

        final double spawnDistance = Math.sqrt(BlockPosUtil.getDistanceSquared2D(townHall, world.getSharedSpawnPos()));
        if (spawnDistance < MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get())
        {
            if (!world.isClientSide)
            {
                MessageUtils.format(CANT_PLACE_COLONY_TOO_CLOSE_TO_SPAWN, MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get() - spawnDistance).sendTo(sender);
            }
            return;
        }
        else if (spawnDistance > MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get())
        {
            if (!world.isClientSide)
            {
                MessageUtils.format(CANT_PLACE_COLONY_TOO_FAR_FROM_SPAWN, spawnDistance - MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get()).sendTo(sender);
            }
            return;
        }

        if (colony != null && !IColonyManager.getInstance().isFarEnoughFromColonies(world, townHall))
        {
            MessageUtils.format(MESSAGE_COLONY_CREATE_DENIED_TOO_CLOSE, colony.getName()).sendTo(sender);
            return;
        }

        final IColony ownedColony = IColonyManager.getInstance().getIColonyByOwner(world, sender);

        if (ownedColony == null)
        {
            final IColony createdColony = IColonyManager.getInstance().createColony(world, townHall, sender, colonyName, pack);
            final IBuilding building = createdColony.getBuildingManager().addNewBuilding((TileEntityColonyBuilding) tileEntity, world);

            if (reactivate)
            {
                MessageUtils.format(MESSAGE_COLONY_REACTIVATED, colonyName)
                  .withPriority(MessagePriority.IMPORTANT)
                  .sendTo(sender);
            }
            else
            {
                MessageUtils.format(MESSAGE_COLONY_FOUNDED)
                  .withPriority(MessagePriority.IMPORTANT)
                  .sendTo(sender);
            }

            new OpenBuildingUIMessage(building).sendToPlayer(sender);;

            try
            {
                NeoForge.EVENT_BUS.post(new ColonyCreatedEvent(createdColony));
            }
            catch (final Exception e)
            {
                Log.getLogger().error("Error during ColonyCreatedEvent", e);
            }
            return;
        }

        ownedColony.getPackageManager().sendColonyViewPackets();
        ownedColony.getPackageManager().sendPermissionsPackets();
        MessageUtils.format(WARNING_COLONY_FOUNDING_FAILED)
          .withPriority(MessagePriority.DANGER)
          .sendTo(sender);
    }
}
