package com.minecolonies.core.network.messages.server;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.Colony;
import com.minecolonies.core.network.messages.client.OpenCantFoundColonyWarningMessage;
import com.minecolonies.core.network.messages.client.OpenColonyFoundingCovenantMessage;
import com.minecolonies.core.network.messages.client.OpenDeleteAbandonColonyMessage;
import com.minecolonies.core.network.messages.client.OpenReactivateColonyMessage;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.BuildingConstants.DEACTIVATED;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.core.MineColonies.getConfig;

/**
 * Message for asking the server for some colony info before creation.
 */
public class GetColonyInfoMessage implements IMessage
{
    /**
     * Position the player wants to found the colony at.
     */
    BlockPos pos;

    public GetColonyInfoMessage()
    {
        super();
    }

    public GetColonyInfoMessage(final BlockPos pos)
    {
        this.pos = pos;
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        pos = buf.readBlockPos();
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

        if (IColonyManager.getInstance().getColonyByPosFromWorld(world, pos) instanceof Colony)
        {
            MessageUtils.format(HUT_BLOCK_MISSING_BUILDING).sendTo(sender);
            return;
        }

        if (IColonyManager.getInstance().getIColonyByOwner(world, sender) instanceof Colony colony)
        {
            Network.getNetwork().sendToPlayer(new OpenDeleteAbandonColonyMessage(pos, colony.getName(), colony.getCenter(), colony.getID()), sender);
            return;
        }

        final IColony nextColony = IColonyManager.getInstance().getClosestColony(world, pos);
        if (IColonyManager.getInstance().isFarEnoughFromColonies(world, pos))
        {
            final double spawnDistance = Math.sqrt(BlockPosUtil.getDistanceSquared2D(pos, world.getSharedSpawnPos()));
            if (spawnDistance < MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get())
            {
                Network.getNetwork().sendToPlayer(new OpenCantFoundColonyWarningMessage(Component.translatable("com.minecolonies.core.founding.tooclosetospawn", (int) (MineColonies.getConfig().getServer().minDistanceFromWorldSpawn.get() - spawnDistance)), pos, true), sender);
            }
            else if (spawnDistance > MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get())
            {
                Network.getNetwork().sendToPlayer(new OpenCantFoundColonyWarningMessage(Component.translatable("com.minecolonies.core.founding.toofarfromspawn", (int) (spawnDistance - MineColonies.getConfig().getServer().maxDistanceFromWorldSpawn.get())), pos, true), sender);
            }
            else if (world.getBlockEntity(pos) instanceof TileEntityColonyBuilding townhall && townhall.getPositionedTags().containsKey(BlockPos.ZERO) && townhall.getPositionedTags().get(BlockPos.ZERO).contains(DEACTIVATED))
            {
                Network.getNetwork().sendToPlayer(new OpenReactivateColonyMessage(nextColony.getName(), (int) BlockPosUtil.getDistance(nextColony.getCenter(), pos) - (getConfig().getServer().initialColonySize.get() << 4), pos), sender);
            }
            else
            {
                Network.getNetwork().sendToPlayer(new OpenColonyFoundingCovenantMessage(nextColony == null ? "" : nextColony.getName(), nextColony == null ? Integer.MAX_VALUE : (int) BlockPosUtil.getDistance(nextColony.getCenter(), pos) - (getConfig().getServer().initialColonySize.get() << 4), pos), sender);
            }
        }
        else
        {
            final int blockRange = Math.max(MineColonies.getConfig().getServer().minColonyDistance.get(), getConfig().getServer().initialColonySize.get()) << 4;
            final int distance = (int) BlockPosUtil.getDistance(pos, nextColony.getCenter());

            Network.getNetwork().sendToPlayer(new OpenCantFoundColonyWarningMessage(Component.translatable("com.minecolonies.core.founding.tooclosetocolony", Math.max(100, blockRange - distance)), pos, false), sender);
        }
    }
}
