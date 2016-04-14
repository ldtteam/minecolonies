package com.minecolonies.network.messages;

import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.event.EventHandler;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.Log;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/**
 * Send build tool data to the server. Verify the data on the server side and then place the building.
 * Created: August 13, 2015
 *
 * @author Colton
 */
public class BuildToolPlaceMessage implements IMessage, IMessageHandler<BuildToolPlaceMessage, IMessage>
{
    private String hut, style;
    private int x, y, z, rotation;

    public BuildToolPlaceMessage() {}

    /**
     * Create the building that was made with the build tool.
     * Item in inventory required
     *
     * @param hut       String representation of sort of hut that made the request
     * @param style     String representation of style that was requested
     * @param x         x-coordinate
     * @param y         y-coordinate
     * @param z         z-coordinate
     * @param rotation  int representation of the rotation
     */
    public BuildToolPlaceMessage(String hut, String style, int x, int y, int z, int rotation)
    {
        this.hut = hut;
        this.style = style;
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotation = rotation;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, hut);
        ByteBufUtils.writeUTF8String(buf, style);

        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);

        buf.writeInt(rotation);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        hut = ByteBufUtils.readUTF8String(buf);
        style = ByteBufUtils.readUTF8String(buf);

        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();

        rotation = buf.readInt();
    }

    @Override
    public IMessage onMessage(BuildToolPlaceMessage message, MessageContext ctx)
    {
        Block block = Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + message.hut);
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;

        if(player.inventory.hasItem(Item.getItemFromBlock(block)) && EventHandler.onBlockHutPlaced(world, player, block, message.x, message.y, message.z))
        {
            world.setBlockState(message.x, message.y, message.z, block);
            block.onBlockPlacedBy(world, message.x, message.y, message.z, player, null);

            player.inventory.consumeInventoryItem(Item.getItemFromBlock(block));

            Building building = ColonyManager.getBuilding(world, message.x, message.y, message.z);

            if(building != null)
            {
                building.setRotation(message.rotation);
                building.setStyle(message.style);
            }
            else
            {
                Log.logger.error("BuildTool: building is null!");
            }
        }

        return null;
    }
}
