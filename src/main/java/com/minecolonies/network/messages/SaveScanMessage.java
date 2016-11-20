package com.minecolonies.network.messages;

import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.StructureWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Send a scan outputStream to the client.
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
        File file = new File(FMLClientHandler.instance().getClient().mcDataDir, message.storeLocation);
        StructureWrapper.createScanDirectory(FMLClientHandler.instance().getClient().theWorld);

        try(OutputStream outputstream = new FileOutputStream(file))
        {
            CompressedStreamTools.writeCompressed(message.nbttagcompound, outputstream);
        }
        catch (Exception e)
        {
            LanguageHandler.sendPlayerLocalizedMessage(FMLClientHandler.instance().getClient().thePlayer, LanguageHandler.format("item.scepterSteel.scanFailure"));
            return null;
        }

        LanguageHandler.sendPlayerLocalizedMessage(FMLClientHandler.instance().getClient().thePlayer, LanguageHandler.format("item.scepterSteel.scanSuccess",message.storeLocation));
        return null;
    }
}
