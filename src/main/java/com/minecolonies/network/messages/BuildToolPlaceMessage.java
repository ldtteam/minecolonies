package com.minecolonies.network.messages;

import com.minecolonies.colony.ColonyManager;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.event.EventHandler;
import com.minecolonies.lib.Constants;
import com.minecolonies.util.Log;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
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
    private String hut, style;
    private int rotation;

    private BlockPos pos;

    public BuildToolPlaceMessage() {}

    /**
     * Create the building that was made with the build tool.
     * Item in inventory required
     *
     * @param hut       String representation of sort of hut that made the request
     * @param style     String representation of style that was requested
     * @param pos       BlockPos
     * @param rotation  int representation of the rotation
     */
    public BuildToolPlaceMessage(String hut, String style, BlockPos pos, int rotation)
    {
        this.hut = hut;
        this.style = style;
        this.pos = pos;
        this.rotation = rotation;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, hut);
        ByteBufUtils.writeUTF8String(buf, style);

        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());

        buf.writeInt(rotation);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        hut = ByteBufUtils.readUTF8String(buf);
        style = ByteBufUtils.readUTF8String(buf);

        pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());

        rotation = buf.readInt();
    }

    @Override
    public IMessage onMessage(BuildToolPlaceMessage message, MessageContext ctx)
    {
        Block block = Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + message.hut);
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;
        BlockPos pos = message.pos;

        if(player.inventory.hasItem(Item.getItemFromBlock(block)) && EventHandler.onBlockHutPlaced(world, player, block, pos))
        {
            world.destroyBlock(pos, true);
            world.setBlockState(pos, block.getDefaultState());
            block.onBlockPlacedBy(world, pos, world.getBlockState(pos), player, null);

            player.inventory.consumeInventoryItem(Item.getItemFromBlock(block));

            Building building = ColonyManager.getBuilding(world, pos);

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
