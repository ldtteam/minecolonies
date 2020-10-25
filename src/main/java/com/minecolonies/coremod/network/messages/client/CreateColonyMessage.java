package com.minecolonies.coremod.network.messages.client;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;
import static com.minecolonies.api.util.constant.TranslationConstants.CANT_PLACE_COLONY_TOO_CLOSE_TO_SPAWN;
import static com.minecolonies.api.util.constant.TranslationConstants.CANT_PLACE_COLONY_TOO_FAR_FROM_SPAWN;

/**
 * Message for trying to create a new colony.
 */
public class CreateColonyMessage implements IMessage
{
    /**
     * Townhall position to create building on
     */
    BlockPos townHall;

    public CreateColonyMessage()
    {
        super();
    }

    public CreateColonyMessage(final BlockPos townHall)
    {
        this.townHall = townHall;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeBlockPos(townHall);
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        townHall = buf.readBlockPos();
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
        final ServerPlayerEntity sender = ctxIn.getSender();
        final World world = ctxIn.getSender().world;

        if (sender == null)
        {
            return;
        }

        if (sender.getStats().getValue(Stats.ITEM_USED.get(ModItems.supplyChest)) <= 0 && !sender.isCreative())
        {
            LanguageHandler.sendPlayerMessage(sender, "com.minecolonies.coremod.supplyneed");
            return;
        }

        final IColony colony = IColonyManager.getInstance().getClosestColony(world, townHall);

        String style = Constants.DEFAULT_STYLE;
        final TileEntity tileEntity = world.getTileEntity(townHall);

        if (!(tileEntity instanceof TileEntityColonyBuilding))
        {
            LanguageHandler.sendPlayerMessage(sender, "com.minecolonies.coremod.gui.colony.create.notileentity");
            return;
        }

        if (!((AbstractTileEntityColonyBuilding) tileEntity).getStyle().isEmpty())
        {
            style = ((AbstractTileEntityColonyBuilding) tileEntity).getStyle();
        }

        if (MineColonies.getConfig().getCommon().protectVillages.get()
              && ((ServerChunkProvider) world.getChunkProvider())
                   .getChunkGenerator()
                   .findNearestStructure(world, "Village", townHall, MineColonies.getConfig().getCommon().minColonyDistance.get() * BLOCKS_PER_CHUNK, false) != null)
        {
            Log.getLogger().warn("Village close by!");
            LanguageHandler.sendPlayerMessage(sender,
              "block.blockhuttownhall.messagetooclosetovillage");
            return;
        }

        if (MineColonies.getConfig().getCommon().restrictColonyPlacement.get())
        {
            final double spawnDistance = Math.sqrt(BlockPosUtil.getDistanceSquared2D(townHall, world.getSpawnPoint()));
            if (spawnDistance < MineColonies.getConfig().getCommon().minDistanceFromWorldSpawn.get())
            {
                if (!world.isRemote)
                {
                    LanguageHandler.sendPlayerMessage(sender, CANT_PLACE_COLONY_TOO_CLOSE_TO_SPAWN, MineColonies.getConfig().getCommon().minDistanceFromWorldSpawn.get());
                }
                return;
            }
            else if (spawnDistance > MineColonies.getConfig().getCommon().maxDistanceFromWorldSpawn.get())
            {
                if (!world.isRemote)
                {
                    LanguageHandler.sendPlayerMessage(sender, CANT_PLACE_COLONY_TOO_FAR_FROM_SPAWN, MineColonies.getConfig().getCommon().maxDistanceFromWorldSpawn.get());
                }
                return;
            }
        }

        if (colony == null || !IColonyManager.getInstance().isTooCloseToColony(world, townHall))
        {
            final IColony ownedColony = IColonyManager.getInstance().getIColonyByOwner(world, sender);

            if (ownedColony == null)
            {
                IColonyManager.getInstance().createColony(world, townHall, sender, style);
                IColonyManager.getInstance().getIColonyByOwner(world, sender).getBuildingManager().addNewBuilding((TileEntityColonyBuilding) tileEntity, world);
                LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.coremod.progress.colony_founded");
                return;
            }
        }

        LanguageHandler.sendPlayerMessage((PlayerEntity) sender, "com.minecolonies.coremod.gui.colony.create.failed");
    }
}
