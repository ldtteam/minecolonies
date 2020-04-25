package com.minecolonies.coremod.util;

import com.google.common.io.Files;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.*;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;
import static com.minecolonies.coremod.MineColonies.COLONY_MANAGER_CAP;

public final class BackUpHelper
{
    /**
     * The maximum amount of colonies we're trying to load from a backup
     */
    private final static int MAX_COLONY_LOAD = 5000;

    /**
     * Private constructor to hide implicit one.
     */
    private BackUpHelper()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Backup the colony
     *
     * @return true if succesful.
     */
    public static boolean backupColonyData()
    {
        BackUpHelper.saveColonies();
        try (FileOutputStream fos = new FileOutputStream(getBackupSaveLocation(new Date())))
        {
            @NotNull final File saveDir =
              new File(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
            final ZipOutputStream zos = new ZipOutputStream(fos);

            DimensionType.getAll().forEach(dimensionType -> {
                for (int i = 1; i <= IColonyManager.getInstance().getTopColonyId() + 1; i++)
                {
                    @NotNull final File file = new File(saveDir, String.format(FILENAME_COLONY, i, dimensionType.getId()));
                    @NotNull final File fileDeleted = new File(saveDir, String.format(FILENAME_COLONY_DELETED, i, dimensionType.getId()));
                    if (file.exists())
                    {
                        // mark existing files
                        if (IColonyManager.getInstance().getColonyByDimension(i, dimensionType.getId()) == null)
                        {
                            markColonyDeleted(i, dimensionType.getId());
                            addToZipFile(String.format(FILENAME_COLONY_DELETED, i, dimensionType.getId()), zos, saveDir);
                        }
                        else
                        {
                            addToZipFile(String.format(FILENAME_COLONY, i, dimensionType.getId()), zos, saveDir);
                        }
                    }
                    else if (fileDeleted.exists())
                    {
                        addToZipFile(String.format(FILENAME_COLONY_DELETED, i, dimensionType.getId()), zos, saveDir);
                    }
                }
            });
            addToZipFile(getSaveLocation().getName(), zos, saveDir);
            zos.close();
        }
        catch (final Exception e)
        {
            /*
             * Intentionally not being thrown.
             */
            Log.getLogger().warn("Unable to backup colony data, please contact an administrator", e);
            return false;
        }

        return true;
    }

    /**
     * Loads all colonies from backup files which the world cap is missing.
     */
    public static void loadMissingColonies()
    {
        @NotNull final File saveDir =
          new File(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);

        DimensionType.getAll().forEach(dimensionType ->
        {
            int missingFilesInRow = 0;
            for (int i = 1; i <= MAX_COLONY_LOAD && missingFilesInRow < 5; i++)
            {
                // Check non-deleted files for colony id + dim
                @NotNull final File file = new File(saveDir, String.format(FILENAME_COLONY, i, dimensionType.getId()));
                if (file.exists())
                {
                    missingFilesInRow = 0;
                    // Load colony if null
                    if (IColonyManager.getInstance().getColonyByDimension(i, dimensionType.getId()) == null)
                    {
                        loadColonyBackup(i, dimensionType.getId(), false, false);
                    }
                }
                else
                {
                    missingFilesInRow++;
                }
            }
        });
    }

    /**
     * Get save location for Minecolonies backup data, from the world/save directory.
     *
     * @return Save file for minecolonies.
     */
    @NotNull
    private static File getBackupSaveLocation(final Date date)
    {
        @NotNull final File saveDir =
          new File(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        return new File(saveDir, String.format(FILENAME_MINECOLONIES_BACKUP, new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(date)));
    }

    /**
     * Add zip to file.
     *
     * @param fileName the file name.
     * @param zos      the output stream.
     * @param folder   the folder.
     */
    private static void addToZipFile(final String fileName, final ZipOutputStream zos, final File folder)
    {
        final File file = new File(folder, fileName);
        try (FileInputStream fis = new FileInputStream(file))
        {
            zos.putNextEntry(new ZipEntry(fileName));
            Files.copy(file, zos);
        }
        catch (final Exception e)
        {
            /*
             * Intentionally not being thrown.
             */
            Log.getLogger().warn("Error packing " + fileName + " into the zip.", e);
        }
    }

    /**
     * Get save location for Minecolonies data, from the world/save directory.
     *
     * @return Save file for minecolonies.
     */
    @NotNull
    public static File getSaveLocation()
    {
        @NotNull final File saveDir =
          new File(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        return new File(saveDir, FILENAME_MINECOLONIES);
    }

    /**
     * Get save location for Minecolonies data, from the world/save directory.
     *
     * @param world the server world object to use.
     * @return Save file for minecolonies.
     */
    @NotNull
    public static File getSaveLocation(final ServerWorld world)
    {
        @NotNull final File saveDir =
          new File(world.getServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        return new File(saveDir, FILENAME_MINECOLONIES);
    }

    /**
     * Save an CompoundNBT to a file.  Does so in a safe manner using an intermediate tmp file.
     *
     * @param file     The destination file to write the data to.
     * @param compound The CompoundNBT to write to the file.
     */
    public static void saveNBTToPath(@Nullable final File file, @NotNull final CompoundNBT compound)
    {
        try
        {
            if (file != null)
            {
                file.getParentFile().mkdir();
                CompressedStreamTools.safeWrite(compound, file);
            }
        }
        catch (final IOException exception)
        {
            Log.getLogger().error("Exception when saving ColonyManager", exception);
        }
    }

    /**
     * Load a file and return the data as an CompoundNBT.
     *
     * @param file The path to the file.
     * @return the data from the file as an CompoundNBT, or null.
     */
    public static CompoundNBT loadNBTFromPath(@Nullable final File file)
    {
        try
        {
            if (file != null && file.exists())
            {
                return CompressedStreamTools.read(file);
            }
        }
        catch (final IOException exception)
        {
            Log.getLogger().error("Exception when loading file from path in ColonyManager!", exception);
        }
        return null;
    }

    /**
     * Save all the Colonies.
     */
    public static void saveColonies()
    {
        @NotNull final CompoundNBT compound = new CompoundNBT();
        IColonyManager.getInstance().write(compound);

        @NotNull final File file = getSaveLocation();
        saveNBTToPath(file, compound);
        @NotNull final File saveDir =
          new File(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        for (final IColony colony : IColonyManager.getInstance().getAllColonies())
        {
            final CompoundNBT colonyCompound = new CompoundNBT();
            colony.write(colonyCompound);
            saveNBTToPath(new File(saveDir, String.format(FILENAME_COLONY, colony.getID(), colony.getDimension())), colonyCompound);
        }
    }

    /**
     * Marks a colony's backup file as deleted.
     *
     * @param colonyID    id of the colony to delete
     * @param dimensionID dimension of the colony to delete
     */
    public static void markColonyDeleted(final int colonyID, final int dimensionID)
    {
        @NotNull final File saveDir =
          new File(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        final File todelete = new File(saveDir, String.format(FILENAME_COLONY, colonyID, dimensionID));
        if (todelete.exists())
        {
            new File(saveDir, String.format(FILENAME_COLONY_DELETED, colonyID, dimensionID)).delete();
            todelete.renameTo(new File(saveDir, String.format(FILENAME_COLONY_DELETED, colonyID, dimensionID)));
        }
    }

    /**
     * Loads all colonies from the backup files, skips deleted colonies.
     */
    public static void loadAllBackups()
    {
        @NotNull final File saveDir =
          new File(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);

        DimensionType.getAll().forEach(dimensionType -> {
            for (int i = 1; i <= IColonyManager.getInstance().getTopColonyId() + 1; i++)
            {
                @NotNull final File file = new File(saveDir, String.format(FILENAME_COLONY, i, dimensionType.getId()));
                if (file.exists())
                {
                    loadColonyBackup(i, dimensionType.getId(), false, false);
                }
            }
        });
    }

    /**
     * Load the colony backup by colony, also works for backups of deleted colonies.
     *
     * @param colonyId    of the colony.
     * @param dimension   the colony dimension.
     * @param loadDeleted whether to load deleted colonies aswell.
     * @param claimChunks if chunks shall be claimed on loading.
     */
    public static void loadColonyBackup(final int colonyId, final int dimension, boolean loadDeleted, boolean claimChunks)
    {
        @NotNull final File saveDir =
          new File(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getWorldDirectory(), FILENAME_MINECOLONIES_PATH);
        CompoundNBT compound = loadNBTFromPath(new File(saveDir, String.format(FILENAME_COLONY, colonyId, dimension)));
        if (compound == null)
        {
            if (loadDeleted)
            {
                compound = loadNBTFromPath(new File(saveDir, String.format(FILENAME_COLONY_DELETED, colonyId, dimension)));
            }
            if (compound == null)
            {
                Log.getLogger().warn("Can't find NBT of colony: " + colonyId + " at location: " + new File(saveDir, String.format(FILENAME_COLONY, colonyId, dimension)));
                return;
            }
        }

        IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        if (colony != null)
        {
            colony.read(compound);
        }
        else
        {
            Log.getLogger().warn("Colony:" + colonyId + " is missing, loading backup!");
            final World colonyWorld = ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.getById(dimension));
            colony = Colony.loadColony(compound, colonyWorld);
            if (colony == null)
            {
                Log.getLogger().warn("Colony:" + colonyId + " loadBackup failed!");
                return;
            }

            colonyWorld.getCapability(COLONY_MANAGER_CAP, null).orElse(null).addColony(colony);

            if (claimChunks)
            {
                IColonyTagCapability cap = ((Chunk) colonyWorld.getChunk(colony.getCenter())).getCapability(CLOSE_COLONY_CAP, null).orElse(null);
                if (cap != null && cap.getOwningColony() != colonyId)
                {
                    for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
                    {
                        ChunkDataHelper.claimColonyChunks(colony,
                          true,
                          building.getPosition(),
                          building.getClaimRadius(building.getBuildingLevel()));
                    }

                    ChunkDataHelper.claimColonyChunks(colonyWorld, true, colony.getID(), colony.getCenter(), colony.getDimension());
                }
            }
        }

        Log.getLogger().warn("Successfully restored colony:" + colonyId);
    }
}
