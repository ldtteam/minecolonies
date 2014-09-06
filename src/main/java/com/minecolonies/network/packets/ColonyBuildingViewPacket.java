package com.minecolonies.network.packets;

import com.minecolonies.colony.ColonyManager;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.UUID;

/**
 * Add or Update a Building.View to a ColonyView on the client
 */
public class ColonyBuildingViewPacket extends AbstractPacket
{
    private UUID           colonyId;
    private NBTTagCompound building;

    public ColonyBuildingViewPacket(){}

    public ColonyBuildingViewPacket(UUID colonyId, NBTTagCompound building)
    {
        this.colonyId = colonyId;
        this.building = building;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, PacketBuffer buffer) throws IOException
    {
        buffer.writeLong(colonyId.getMostSignificantBits());
        buffer.writeLong(colonyId.getLeastSignificantBits());
        buffer.writeNBTTagCompoundToBuffer(building);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, PacketBuffer buffer) throws IOException
    {
        colonyId = new UUID(buffer.readLong(), buffer.readLong());
        building = buffer.readNBTTagCompoundFromBuffer();
    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {
        ColonyManager.handleColonyBuildingViewPacket(colonyId, building);
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
    }
}
