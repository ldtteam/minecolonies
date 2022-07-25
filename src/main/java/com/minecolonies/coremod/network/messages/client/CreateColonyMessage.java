package com.minecolonies.coremod.network.messages.client;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.tileentities.AbstractTileEntityColonyBuilding;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

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
     * The initial name given to a colony.
     */
    String colonyName;

    public CreateColonyMessage()
    {
        super();
    }

    public CreateColonyMessage(final BlockPos townHall, String colonyName)
    {
        this.townHall = townHall;
        this.colonyName = colonyName;
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeBlockPos(townHall);
        buf.writeUtf(colonyName);
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        townHall = buf.readBlockPos();
        colonyName = buf.readUtf(32767);
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
        final World world = ctxIn.getSender().level;

        if (sender == null)
        {
            return;
        }

        if (sender.getStats().getValue(Stats.ITEM_USED.get(ModItems.supplyChest)) <= 0 && !sender.isCreative())
        {
            MessageUtils.format(MESSAGE_COLONY_START_SUPPLY_NEED).sendTo(sender);
            return;
        }

        final IColony colony = IColonyManager.getInstance().getClosestColony(world, townHall);

        String style = Constants.DEFAULT_STYLE;
        final TileEntity tileEntity = world.getBlockEntity(townHall);

        if (!(tileEntity instanceof TileEntityColonyBuilding))
        {
            MessageUtils.format(WARNING_TOWN_HALL_NO_TILE_ENTITY).with(TextFormatting.BOLD, TextFormatting.DARK_RED).sendTo(sender);
            return;
        }

        if (!((AbstractTileEntityColonyBuilding) tileEntity).getStyle().isEmpty())
        {
            style = ((AbstractTileEntityColonyBuilding) tileEntity).getStyle();
        }

        if (MineColonies.getConfig().getServer().restrictColonyPlacement.get())
        {
            final double spawnDistance = Math.sqrt(BlockPosUtil.getDistanceSquared2D(townHall, ((ServerWorld) world).getSharedSpawnPos()));
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
            IColonyManager.getInstance().createColony(world, townHall, sender, colonyName, style);
            IColonyManager.getInstance().getIColonyByOwner(world, sender).getBuildingManager().addNewBuilding((TileEntityColonyBuilding) tileEntity, world);
            MessageUtils.format(MESSAGE_COLONY_FOUNDED).with(TextFormatting.GOLD).sendTo(sender);
            return;
        }

        MessageUtils.format(WARNING_COLONY_FOUNDING_FAILED).with(TextFormatting.BOLD, TextFormatting.DARK_RED).sendTo(sender);
    }
}
