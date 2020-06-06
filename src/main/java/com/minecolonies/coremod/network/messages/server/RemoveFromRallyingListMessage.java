package com.minecolonies.coremod.network.messages.server;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.colony.requestsystem.locations.StaticLocation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.coremod.items.ItemBannerRallyGuards.removeGuardTowerAtLocation;

/**
 * Removes a guard tower from the rallying list
 */
public class RemoveFromRallyingListMessage implements IMessage
{
    /**
     * The banner to be modified.
     */
    private ItemStack banner;

    /**
     * The position of the guard tower
     */
    private ILocation location;

    /**
     * Empty constructor used when registering the message
     */
    public RemoveFromRallyingListMessage()
    {
        super();
    }

    /**
     * Toggle the banner
     *
     * @param banner The banner to be modified.
     * @param location The position of the guard tower
     */
    public RemoveFromRallyingListMessage(final ItemStack banner, final ILocation location)
    {
        super();
        this.banner = banner;
        this.location = location;
    }

    /**
     * Reads this packet from a {@link PacketBuffer}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        banner = buf.readItemStack();
        final int dimension = buf.readInt();
        final BlockPos position = buf.readBlockPos();
        location = new StaticLocation(position, dimension);
    }

    /**
     * Writes this packet to a {@link PacketBuffer}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeItemStack(banner);
        buf.writeInt(location.getDimension());
        buf.writeBlockPos(location.getInDimensionLocation());
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
        removeGuardTowerAtLocation(banner, location);
    }
}
