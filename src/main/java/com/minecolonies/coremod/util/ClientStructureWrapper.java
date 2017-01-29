package com.minecolonies.coremod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
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
        final String storeLocation = "/minecolonies/scans/" + LanguageHandler.format("item.scepterSteel.scanFormat", currentMillis, ".nbt");

        final File file = new File(Minecraft.getMinecraft().mcDataDir, fileName);
        createScanDirectory(Minecraft.getMinecraft().world);

        try (OutputStream outputstream = new FileOutputStream(file))
        {
            CompressedStreamTools.writeCompressed(nbttagcompound, outputstream);
        }
        catch (final IOException e)
        {
            LanguageHandler.sendPlayerLocalizedMessage(Minecraft.getMinecraft().player, LanguageHandler.format("item.scepterSteel.scanFailure"));
            Log.getLogger().warn("Exception while trying to scan.", e);
            return;
        }

        LanguageHandler.sendPlayerLocalizedMessage(Minecraft.getMinecraft().player,
          LanguageHandler.format("item.scepterSteel.scanSuccess", fileName));
    }

    /**
     * Creates the scan directories for the scanTool.
     *
     * @param world the worldIn.
     */
    private static void createScanDirectory(@NotNull final World world)
    {
        final File minecolonies;
        if (world.isRemote)
        {
            minecolonies = new File(Minecraft.getMinecraft().mcDataDir, "minecolonies/");
        }
        else
        {
            final MinecraftServer server = world.getMinecraftServer();
            if (server == null)
            {
                return;
            }
            minecolonies = server.getFile("minecolonies/");
        }
        checkDirectory(minecolonies);

        @NotNull final File scans = new File(minecolonies, "scans/");
        checkDirectory(scans);
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
