package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.Schematics;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.event.EventHandler;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.Log;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building.
 * Created: August 13, 2015
 *
 * @author Colton
 */
public class BuildToolPlaceMessage implements IMessage, IMessageHandler<BuildToolPlaceMessage, IMessage>
{
    private String hutDec;
    private String style;
    private int    rotation;

    private BlockPos pos;

    private boolean isHut;

    /**
     * Empty constructor used when registering the message.
     */
    public BuildToolPlaceMessage()
    {
        // Called using reflection by Forge.
    }

    /**
     * Create the building that was made with the build tool.
     * Item in inventory required
     *
     * @param hutDec   String representation of sort of hutDec that made the request
     * @param style    String representation of style that was requested
     * @param pos      BlockPos
     * @param rotation int representation of the rotation
     * @param isHut    true if hut, false if decoration
     */
    public BuildToolPlaceMessage(String hutDec, String style, BlockPos pos, int rotation, boolean isHut)
    {
        this.hutDec = hutDec;
        this.style = style;
        this.pos = pos;
        this.rotation = rotation;
        this.isHut = isHut;
    }

    /**
     * Reads this packet from a {@link ByteBuf}.
     *
     * @param buf The buffer begin read from.
     */
    @Override
    public void fromBytes(ByteBuf buf)
    {
        hutDec = ByteBufUtils.readUTF8String(buf);
        style = ByteBufUtils.readUTF8String(buf);

        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

        rotation = buf.readInt();

        isHut = buf.readBoolean();
    }

    /**
     * Writes this packet to a {@link ByteBuf}.
     *
     * @param buf The buffer being written to.
     */
    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, hutDec);
        ByteBufUtils.writeUTF8String(buf, style);

        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        buf.writeInt(rotation);

        buf.writeBoolean(isHut);
    }

    /**
     * {@link BuildToolPlaceMessage} handler.
     *
     * @param message Packet received.
     * @param ctx     Contains info about the Client that sent the packet.
     * @return null - Don't send a response packet.
     */
    @Override
    public IMessage onMessage(BuildToolPlaceMessage message, MessageContext ctx)
    {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;
        if (message.isHut)
        {
            handleHut(world, player, message.hutDec, message.style, message.rotation, message.pos);
        }
        else
        {
            handleDecoration(world, player, message.hutDec, message.style, message.rotation, message.pos);
        }

        return null;
    }

    /**
     * Handles the placement of huts.
     *
     * @param world    World the hut is being placed into.
     * @param player   Who placed the hut.
     * @param hut      The hut we are placing.
     * @param style    The style of the hut.
     * @param rotation The number of times the schematic should be rotated.
     * @param buildPos The location the hut is being placed.
     */
    private static void handleHut(World world, EntityPlayer player, String hut, String style, int rotation, BlockPos buildPos)
    {
        if (Schematics.getStylesForHut(hut) == null)
        {
            Log.logger.error("No record of hut: " + hut);
            return;
        }

        Block block = Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + hut);

        if (player.inventory.hasItem(Item.getItemFromBlock(block)) && EventHandler.onBlockHutPlaced(world, player, block, buildPos))
        {
            world.destroyBlock(buildPos, true);
            world.setBlockState(buildPos, block.getDefaultState());
            block.onBlockPlacedBy(world, buildPos, world.getBlockState(buildPos), player, null);

            player.inventory.consumeInventoryItem(Item.getItemFromBlock(block));

            AbstractBuilding building = ColonyManager.getBuilding(world, buildPos);

            if (building != null)
            {
                building.setStyle(style);
                building.setRotation(rotation);
            }
            else
            {
                Log.logger.error("BuildTool: building is null!");
            }
        }
    }

    /**
     * Creates the {@link WorkOrderBuildDecoration} to start building the decoration.
     *
     * @param world      The world the decoration is being built in.
     * @param player     The player who placed the decoration.
     * @param decoration The name of the decoration.
     * @param style      The style of the decoration.
     * @param rotation   The number of times the decoration is rotated.
     * @param buildPos   The location the decoration will be built.
     */
    private static void handleDecoration(World world, EntityPlayer player, String decoration, String style, int rotation, BlockPos buildPos)
    {
        if (Schematics.getStylesForDecoration(decoration) == null)
        {
            Log.logger.error("No record of decoration: " + decoration);
            return;
        }

        Colony colony = ColonyManager.getColony(world, buildPos);
        if (colony != null && colony.getPermissions().hasPermission(player, Permissions.Action.PLACE_HUTS))
        {
            colony.getWorkManager().addWorkOrder(new WorkOrderBuildDecoration(decoration, style, rotation, buildPos));
        }
    }
}
