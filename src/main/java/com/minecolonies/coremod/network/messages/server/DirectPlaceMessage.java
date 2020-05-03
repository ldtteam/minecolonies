package com.minecolonies.coremod.network.messages.server;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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

/**
 * Place a building directly without buildtool.
 */
public class DirectPlaceMessage implements IMessage
{
    /**
     * The state to be placed..
     */
    private BlockState state;

    /**
     * The position to place it at.
     */
    private BlockPos pos;

    /**
     * The stack which is going to be placed.
     */
    private ItemStack stack;

    /**
     * Empty constructor used when registering the 
     */
    public DirectPlaceMessage()
    {
        super();
    }

    /**
     * Place the building.
     * @param state the state to be placed.
     * @param pos the pos to place it at.
     * @param stack the stack in the hand.
     */
    public DirectPlaceMessage(final BlockState state, final BlockPos pos, final ItemStack stack)
    {
        super();
        this.state = state;
        this.pos = pos;
        this.stack = stack;
    }

    /**
     * Reads this packet from a {@link PacketBuffer}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        state = Block.getStateById(buf.readInt());
        pos = buf.readBlockPos();
        stack = buf.readItemStack();
    }

    /**
     * Writes this packet to a {@link PacketBuffer}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(Block.getStateId(state));
        buf.writeBlockPos(pos);
        buf.writeItemStack(stack);
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
        final ServerPlayerEntity player = ctxIn.getSender();
        final World world = player.getEntityWorld();
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
        if ((colony == null && state.getBlock() == ModBlocks.blockHutTownHall) || (colony != null && colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS)))
        {
            player.getEntityWorld().setBlockState(pos, state);
            InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.inventory), stack);
            state.getBlock().onBlockPlacedBy(world, pos, state, player, stack);
        }
    }
}
