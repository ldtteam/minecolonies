package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.io.IOException;
import java.util.Locale;

import static com.ldtteam.structurize.api.constants.Constants.BLUEPRINT_FOLDER;
import static com.ldtteam.structurize.api.constants.Constants.SCANS_FOLDER;

/**
 * Handles sendScanMessages.
 */
public class SaveStructureNBTMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "save_structure_nbt", SaveStructureNBTMessage::new);

    private static final String TAG_MILLIS    = "millies";
    public static final  String TAG_SCHEMATIC = "schematic";

    private final CompoundTag compoundNBT;
    private final String      fileName;

    /**
     * Send a scan compound to the client.
     *
     * @param CompoundNBT the stream.
     * @param fileName  String with the name of the file.
     */
    public SaveStructureNBTMessage(final CompoundTag CompoundNBT, final String fileName)
    {
        super(TYPE);
        this.fileName = fileName;
        this.compoundNBT = CompoundNBT;
    }

    protected SaveStructureNBTMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        final FriendlyByteBuf buffer = new FriendlyByteBuf(buf);
        CompoundTag compoundNBT = null;
        String fileName = null;
        try (ByteBufInputStream stream = new ByteBufInputStream(buffer))
        {
            final CompoundTag wrapperCompound = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
            compoundNBT = wrapperCompound.getCompound(TAG_SCHEMATIC);
            fileName = wrapperCompound.getString(TAG_MILLIS);
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().info("Structure too big to be processed", e);
        }
        catch (final IOException e)
        {
            Log.getLogger().info("Problem at retrieving structure on server.", e);
        }
        this.compoundNBT = compoundNBT;
        this.fileName = fileName;
    }

    @Override
    protected void toBytes(final FriendlyByteBuf buf)
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

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final Player player)
    {
        if (compoundNBT != null)
        {
            final String packName = Minecraft.getInstance().getUser().getName().toLowerCase(Locale.US);
            RenderingCache.getOrCreateBlueprintPreviewData("blueprint").setBlueprintFuture(
              StructurePacks.storeBlueprint(packName, compoundNBT, Minecraft.getInstance().gameDirectory.toPath()
                                                                  .resolve(BLUEPRINT_FOLDER)
                                                                  .resolve(Minecraft.getInstance().getUser().getName().toLowerCase(Locale.US))
                                                                  .resolve(SCANS_FOLDER).resolve(fileName)));
            player.displayClientMessage(Component.translatableEscape("Scan successfully saved as %s", fileName), false);
        }
    }
}
