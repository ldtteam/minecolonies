package com.minecolonies.network.packets;

import com.minecolonies.colony.ColonyManager;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;
import java.util.UUID;

/**
 * Add or Update a ColonyView on the client
 */
public class ColonyViewPacket extends AbstractPacket
{
    private UUID colonyId;
    private NBTTagCompound colonyView;

    public ColonyViewPacket(){}

    public ColonyViewPacket(UUID colonyId, NBTTagCompound colonyView)
    {
        this.colonyId = colonyId;
        this.colonyView = colonyView;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, PacketBuffer buffer) throws IOException
    {
        buffer.writeLong(colonyId.getMostSignificantBits());
        buffer.writeLong(colonyId.getLeastSignificantBits());
        buffer.writeNBTTagCompoundToBuffer(colonyView);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, PacketBuffer buffer) throws IOException
    {
        colonyId = new UUID(buffer.readLong(), buffer.readLong());
        colonyView = buffer.readNBTTagCompoundFromBuffer();
    }

    @Override
    public void handleClientSide(EntityPlayer player)
    {
        ColonyManager.handleColonyViewPacket(colonyId, colonyView);
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
    }
}
