package com.minecolonies.coremod.util;

import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Client only structure wrapper methods.
 */
public final class ClientStructureWrapper
{
    /**
     * Private constructor to hide implicit one.
     */
    private ClientStructureWrapper()
    {
        /*
         * Intentionally left empty
         */
    }

    /**
     * Handles the save message of scans.
     *
     * @param nbttagcompound compound to store.
     * @param currentMillis  milli seconds for fileName.
     */
    public static void handleSaveScanMessage(final NBTTagCompound nbttagcompound, final long currentMillis)
    {
        final Structures.StructureName structureName =
          new Structures.StructureName(Structures.SCHEMATICS_SCAN, "new", LanguageHandler.format("item.scepterSteel.scanFormat", currentMillis));
        final File file = new File(Structure.getClientSchematicsFolder(), structureName.toString() + Structures.SCHEMATIC_EXTENSION);
        checkDirectory(file.getParentFile());

        try (OutputStream outputstream = new FileOutputStream(file))
        {
            CompressedStreamTools.writeCompressed(nbttagcompound, outputstream);
        }
        catch (final IOException e)
        {
            LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "item.scepterSteel.scanFailure");
            Log.getLogger().warn("Exception while trying to scan.", e);
            return;
        }

        LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "item.scepterSteel.scanSuccess", file);
        Settings.instance.setStructureName(structureName.toString());
    }

    /**
     * Send a message to the player informing him that the schematic is too big.
     *
     * @param maxSize is the maximum size allowed in bytes.
     */
    public static void sendMessageSchematicTooBig(int maxSize)
    {
        LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "com.minecolonies.coremod.network.messages.schematicsavemessage.toobig", maxSize);
    }

    /**
     * Checks if directory exists, else creates it.
     *
     * @param directory the directory to check.
     */
    private static void checkDirectory(@NotNull final File directory)
    {
        if (!directory.exists() && !directory.mkdirs())
        {
            Log.getLogger().error("Directory doesn't exist and failed to be created: " + directory.toString());
        }
    }
}
