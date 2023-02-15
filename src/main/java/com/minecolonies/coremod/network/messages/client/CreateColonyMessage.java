package com.minecolonies.coremod.network.messages.client;

import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static com.minecolonies.api.util.constant.BuildingConstants.DEACTIVATED;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Message for trying to create a new colony.
 */
public class CreateColonyMessage implements IMessage
{
    /**
     * Town hall position to create building on.
     */
    BlockPos townHall;

    /**
     * If claim action.
     */
    boolean claim;

    /**
     * The colony name.
     */
    String colonyName;

    /**
     * The structure pack name.
     */
    String packName;

    /**
     * The structure path name.
     */
    String pathName;

    public CreateColonyMessage()
    {
        super();
    }

    public CreateColonyMessage(final BlockPos townHall, boolean claim, final String colonyName, final String packName, final String pathName)
    {
        this.townHall = townHall;
        this.claim = claim;
        this.colonyName = colonyName;
        this.packName = packName;
        this.pathName = pathName;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(townHall);
        buf.writeBoolean(claim);
        buf.writeUtf(colonyName);
        buf.writeUtf(packName);
        buf.writeUtf(pathName);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        townHall = buf.readBlockPos();
        claim = buf.readBoolean();
        colonyName = buf.readUtf(32767);
        packName = buf.readUtf(32767);
        pathName = buf.readUtf(32767);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final ServerPlayer sender = ctxIn.getSender();
        final Level world = ctxIn.getSender().level;

        if (sender == null)
        {
            return;
        }

        final IColony colony = IColonyManager.getInstance().getClosestColony(world, townHall);

        String pack = packName;
        final BlockEntity tileEntity = world.getBlockEntity(townHall);

        if (!(tileEntity instanceof final TileEntityColonyBuilding hut))
        {
            MessageUtils.format(WARNING_TOWN_HALL_NO_TILE_ENTITY).with(ChatFormatting.BOLD, ChatFormatting.DARK_RED).sendTo(sender);
            return;
        }

        if (hut.getStructurePack() != null && claim)
        {
            pack = hut.getStructurePack().getName();
        }

        if (hut.getPositionedTags().getOrDefault(BlockPos.ZERO, new ArrayList<>()).contains(DEACTIVATED))
        {
            hut.reactivate();
            if (hut.getStructurePack() != null)
            {
                pack = hut.getStructurePack().getName();
            }
        }

        hut.setStructurePack(StructurePacks.getStructurePack(pack));
        hut.setBlueprintPath(pathName);

        if (MineColonies.getConfig().getServer().restrictColonyPlacement.get())
        {
            final double spawnDistance = Math.sqrt(BlockPosUtil.getDistanceSquared2D(townHall, ((ServerLevel) world).getSharedSpawnPos()));
            if (spawnDistance < MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get())
            {
                if (!world.isClientSide)
                {
                    MessageUtils.format(CANT_PLACE_COLONY_TOO_CLOSE_TO_SPAWN, MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get()).sendTo(sender);
                }
                return;
            }
            else if (spawnDistance > MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get())
            {
                if (!world.isClientSide)
                {
                    MessageUtils.format(CANT_PLACE_COLONY_TOO_FAR_FROM_SPAWN, MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get()).sendTo(sender);
                }
                return;
            }
        }

        if (colony != null && !IColonyManager.getInstance().isFarEnoughFromColonies(world, townHall))
        {
            MessageUtils.format( MESSAGE_COLONY_CREATE_DENIED_TOO_CLOSE, colony.getName()).sendTo(sender);
            return;
        }

        final IColony ownedColony = IColonyManager.getInstance().getIColonyByOwner(world, sender);

        if (ownedColony == null)
        {
            IColonyManager.getInstance().createColony(world, townHall, sender, colonyName, pack);
            IColonyManager.getInstance().getIColonyByOwner(world, sender).getBuildingManager().addNewBuilding((TileEntityColonyBuilding) tileEntity, world);
            MessageUtils.format(MESSAGE_COLONY_FOUNDED).with(ChatFormatting.GOLD).sendTo(sender);
            return;
        }

        ownedColony.getPackageManager().sendColonyViewPackets();
        ownedColony.getPackageManager().sendPermissionsPackets();
        MessageUtils.format(WARNING_COLONY_FOUNDING_FAILED).with(ChatFormatting.BOLD, ChatFormatting.DARK_RED).sendTo(sender);
    }
}
