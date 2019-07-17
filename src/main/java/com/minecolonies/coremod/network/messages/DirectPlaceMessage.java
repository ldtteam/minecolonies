package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.*;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Place a building directly without buildtool.
 */
public class DirectPlaceMessage extends AbstractMessage<DirectPlaceMessage, IMessage>
{
    /**
     * The state to be placed..
     */
    private IBlockState state;

    /**
     * The position to place it at.
     */
    private BlockPos pos;

    /**
     * The stack which is going to be placed.
     */
    private ItemStack stack;

    /**
     * Empty constructor used when registering the message.
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
    public DirectPlaceMessage(final IBlockState state, final BlockPos pos, final ItemStack stack)
    {
        super();
        this.state = state;
        this.pos = pos;
        this.stack = stack;
    }

    /**
     * Reads this packet from a {@link ByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        state = NBTUtil.readBlockState(ByteBufUtils.readTag(buf));
        pos = BlockPosUtil.readFromByteBuf(buf);
        stack = ByteBufUtils.readItemStack(buf);
    }

    /**
     * Writes this packet to a {@link ByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, NBTUtil.writeBlockState(new NBTTagCompound(), state));
        BlockPosUtil.writeToByteBuf(buf, pos);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    @Override
    public void messageOnServerThread(final DirectPlaceMessage message, final EntityPlayerMP player)
    {
        final World world = player.getServerWorld();
        final Colony colony = ColonyManager.getColonyByPosFromWorld(world, message.pos);
        if (colony == null || colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
        {
            player.getServerWorld().setBlockState(message.pos, message.state);
            InventoryUtils.reduceStackInItemHandler(new InvWrapper(player.inventory), message.stack);
            message.state.getBlock().onBlockPlacedBy(world, message.pos, message.state, player, message.stack);
        }
    }
}
