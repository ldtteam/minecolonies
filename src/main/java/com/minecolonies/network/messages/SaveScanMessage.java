package com.minecolonies.network.messages;

import com.minecolonies.util.ClientStructureWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles sendScanMessages.
 */
public class SaveScanMessage implements IMessage, IMessageHandler<SaveScanMessage, IMessage>
{
    private NBTTagCompound nbttagcompound;
    private String         storeLocation;

    /**
     * Public standard constructor.
     */
    public SaveScanMessage()
    {
        /*
         * Intentionally left empty.
         */
    }
    /**
     * Send a scan compound to the client.
     *
     * @param nbttagcompound the stream.
     * @param storeAt string describing where to store the scan.
     */
    public SaveScanMessage(NBTTagCompound nbttagcompound, String storeAt)
    {
        this.nbttagcompound = nbttagcompound;
        this.storeLocation = storeAt;
    }

    @Override
    public void fromBytes(@NotNull ByteBuf buf)
    {
        nbttagcompound = ByteBufUtils.readTag(buf);
        storeLocation = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(@NotNull ByteBuf buf)
    {
        ByteBufUtils.writeTag(buf, nbttagcompound);
        ByteBufUtils.writeUTF8String(buf, storeLocation);
    }

    @Nullable
    @Override
    public IMessage onMessage(@NotNull SaveScanMessage message, MessageContext ctx)
    {
        ClientStructureWrapper.handleSaveScanMessage(message.nbttagcompound, message.storeLocation);
        return null;
    }
}
