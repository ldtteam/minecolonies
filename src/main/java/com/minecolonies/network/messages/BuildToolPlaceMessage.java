package com.minecolonies.network.messages;

import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.permissions.Permissions;
import com.minecolonies.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.event.EventHandler;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.LanguageHandler;
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
    private String hutDec, style;
    private int rotation;

    private BlockPos pos;

    private boolean isHut;

    public BuildToolPlaceMessage() {}

    /**
     * Create the building that was made with the build tool.
     * Item in inventory required
     *
     * @param hutDec       String representation of sort of hutDec that made the request
     * @param style     String representation of style that was requested
     * @param pos       BlockPos
     * @param rotation  int representation of the rotation
     * @param isHut      true if hut, false if decoration
     */
    public BuildToolPlaceMessage(String hutDec, String style, BlockPos pos, int rotation, boolean isHut)
    {
        this.hutDec = hutDec;
        this.style = style;
        this.pos = pos;
        this.rotation = rotation;
        this.isHut = isHut;
    }

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

    @Override
    public void fromBytes(ByteBuf buf)
    {
        hutDec = ByteBufUtils.readUTF8String(buf);
        style = ByteBufUtils.readUTF8String(buf);

        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

        rotation = buf.readInt();

        isHut = buf.readBoolean();
    }

    @Override
    public IMessage onMessage(BuildToolPlaceMessage message, MessageContext ctx)
    {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;
        BlockPos pos = message.pos;
        if (message.isHut)
        {
            Block block = Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + message.hutDec);

            if (player.inventory.hasItem(Item.getItemFromBlock(block)) && EventHandler.onBlockHutPlaced(world, player, block, pos))
            {
                world.destroyBlock(pos, true);
                world.setBlockState(pos, block.getDefaultState());
                block.onBlockPlacedBy(world, pos, world.getBlockState(pos), player, null);

                player.inventory.consumeInventoryItem(Item.getItemFromBlock(block));

                Building building = ColonyManager.getBuilding(world, pos);

                if (building != null)
                {
                    building.setRotation(message.rotation);
                    building.setStyle(message.style);
                }
                else
                {
                    Log.logger.error("BuildTool: building is null!");
                }
            }
        }
        else
        {
            Colony colony = ColonyManager.getColony(world, pos);
            if(colony != null && colony.getPermissions().hasPermission(player, Permissions.Action.PLACE_HUTS))
            {
                colony.getWorkManager().addWorkOrder(new WorkOrderBuildDecoration(message.hutDec, message.style, pos));
                LanguageHandler.sendPlayerLocalizedMessage(player, "com.minecolonies.workOrderAdded");
            }
        }

        return null;
    }
}
