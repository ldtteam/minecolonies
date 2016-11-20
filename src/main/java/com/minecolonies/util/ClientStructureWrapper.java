package com.minecolonies.util;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
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
     * @param nbttagcompound compound to store.
     * @param storeLocation where to store it at.
     */
    public static void handleSaveScanMessage(NBTTagCompound nbttagcompound, String storeLocation)
    {
        File file = new File(Minecraft.getMinecraft().mcDataDir, storeLocation);
        createScanDirectory(Minecraft.getMinecraft().theWorld);

        try (OutputStream outputstream = new FileOutputStream(file))
        {
            CompressedStreamTools.writeCompressed(nbttagcompound, outputstream);
        }
        catch (Exception e)
        {
            LanguageHandler.sendPlayerLocalizedMessage(Minecraft.getMinecraft().thePlayer, LanguageHandler.format("item.scepterSteel.scanFailure"));
            return;
        }

        LanguageHandler.sendPlayerLocalizedMessage(Minecraft.getMinecraft().thePlayer,
                LanguageHandler.format("item.scepterSteel.scanSuccess", storeLocation));

    }

    /**
     * Creates the scan directories for the scanTool.
     * @param world the worldIn.
     */
    private static void createScanDirectory(@NotNull World world)
    {
        File minecolonies;
        if (world.isRemote)
        {
            minecolonies = new File(Minecraft.getMinecraft().mcDataDir, "minecolonies/");
        }
        else
        {
            MinecraftServer server = world.getMinecraftServer();
            if(server != null)
            {
                minecolonies = server.getFile("minecolonies/");
            }
            else
            {
                return;
            }
        }
        checkDirectory(minecolonies);

        @NotNull File scans = new File(minecolonies, "scans/");
        checkDirectory(scans);
    }

    /**
     * Checks if directory exists, else creates it.
     * @param directory the directory to check.
     */
    private static void checkDirectory(@NotNull File directory)
    {
        if (!directory.exists() && !directory.mkdirs())
        {
            Log.getLogger().error("Directory doesn't exist and failed to be created: " + directory.toString());
        }
    }

}
