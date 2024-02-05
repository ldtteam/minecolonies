package com.minecolonies.core.network.messages.client;

import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Locale;

import static com.ldtteam.structurize.api.util.constant.Constants.BLUEPRINT_FOLDER;
import static com.ldtteam.structurize.api.util.constant.Constants.SCANS_FOLDER;

/**
 * Handles sendScanMessages.
 */
public class SaveStructureNBTMessage implements IMessage
{
    private static final String TAG_MILLIS    = "millies";
    public static final  String TAG_SCHEMATIC = "schematic";

    private CompoundTag compoundNBT;
    private String      fileName;

    /**
     * Send a scan compound to the client.
     */
    public SaveStructureNBTMessage()
    {
        super();
    }

    /**
     * Send a scan compound to the client.
     *
     * @param CompoundNBT the stream.
     * @param fileName  String with the name of the file.
     */
    public SaveStructureNBTMessage(final CompoundTag CompoundNBT, final String fileName)
    {
        this.fileName = fileName;
        this.compoundNBT = CompoundNBT;
    }

    @Override
    public void fromBytes(final FriendlyByteBuf buf)
    {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        try (ByteBufInputStream stream = new ByteBufInputStream(buffer))
        {
            final CompoundTag wrapperCompound = NbtIo.readCompressed(stream);
            this.compoundNBT = wrapperCompound.getCompound(TAG_SCHEMATIC);
            this.fileName = wrapperCompound.getString(TAG_MILLIS);
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().info("Structure too big to be processed", e);
        }
        catch (final IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
        }
    }

    @Override
    public void toBytes(final FriendlyByteBuf buf)
    {
        final CompoundTag wrapperCompound = new CompoundTag();
        wrapperCompound.putString(TAG_MILLIS, fileName);
        wrapperCompound.put(TAG_SCHEMATIC, compoundNBT);

        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        try (ByteBufOutputStream stream = new ByteBufOutputStream(buffer))
        {
            NbtIo.writeCompressed(wrapperCompound, stream);
        }
        catch (final IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
        }
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (compoundNBT != null)
        {
            final String packName = Minecraft.getInstance().getUser().getName().toLowerCase(Locale.US);
            RenderingCache.getOrCreateBlueprintPreviewData("blueprint").setBlueprintFuture(
              StructurePacks.storeBlueprint(packName, compoundNBT, Minecraft.getInstance().gameDirectory.toPath()
                                                                  .resolve(BLUEPRINT_FOLDER)
                                                                  .resolve(Minecraft.getInstance().getUser().getName().toLowerCase(Locale.US))
                                                                  .resolve(SCANS_FOLDER).resolve(fileName)));
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("Scan successfully saved as %s", fileName), false);
        }
    }
}
