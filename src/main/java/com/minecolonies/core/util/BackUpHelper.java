package com.minecolonies.core.util;

import com.google.common.base.Function;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.util.ColonyUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.colony.Colony;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.commons.io.function.Uncheck;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.minecolonies.api.util.constant.ColonyManagerConstants.*;

public final class BackUpHelper
{
    /**
     * The maximum amount of colonies we're trying to load from a backup
     */
    private final static int MAX_COLONY_LOAD = 5000;

    /**
     * Base name of region data folders
     */
    private static final String REGION_FOLDER = "region";

    /**
     * Export colony filename scheme
     */
    public static final String FILENAME_EXPORT = "colony%dExport.zip";

    /**
     * Maximum amount of backup zips
     */
    private static final int MAX_BACKUPS = 20;

    /**
     * Last backup timer before the next is allowed
     */
    public static        long lastBackupTime          = 0;
    private static final long MAX_TIME_TO_NEXT_BACKUP = 1000 * 60 * 5;

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
        if (System.currentTimeMillis() - lastBackupTime < MAX_TIME_TO_NEXT_BACKUP)
        {
            return false;
        }
        lastBackupTime = System.currentTimeMillis();

        BackUpHelper.saveColonies();
        try (OutputStream fos = Files.newOutputStream(getBackupSaveLocation(new Date())))
        {
            final Path saveDir = getRootSaveDir();
            final ZipOutputStream zos = new ZipOutputStream(fos);

            for (final ResourceKey<Level> dimensionType : ServerLifecycleHooks.getCurrentServer().levelKeys())
            {
                for (int i = 1; i <= IColonyManager.getInstance().getTopColonyId() + 1; i++)
                {
                    final Path file = getFolderForDimension(saveDir, dimensionType.location()).resolve(String.format(FILENAME_COLONY, i));
                    final Path fileDeleted = getFolderForDimension(saveDir, dimensionType.location()).resolve(String.format(FILENAME_COLONY_DELETED, i));
                    if (Files.exists(file))
                    {
                        // mark existing files
                        if (IColonyManager.getInstance().getColonyByDimension(i, dimensionType) == null)
                        {
                            markColonyDeleted(i, dimensionType);
                            addToZipFile(getFolderForDimension(dimensionType.location()).resolve(String.format(FILENAME_COLONY_DELETED, i)), zos, saveDir);
                        }
                        else
                        {
                            addToZipFile(getFolderForDimension(dimensionType.location()).resolve(String.format(FILENAME_COLONY, i)), zos, saveDir);
                        }
                    }
                    else if (Files.exists(fileDeleted))
                    {
                        addToZipFile(getFolderForDimension(dimensionType.location()).resolve(String.format(FILENAME_COLONY_DELETED, i)), zos, saveDir);
                    }
                }
            }
            addToZipFile(getSaveLocation().getFileName(), zos, saveDir);
            zos.close();

            final List<Path> fileList = new ArrayList<>();

            Files.list(saveDir).forEach(current -> {
                if (Files.isDirectory(current) || !Files.exists(current) || !current.getFileName().toString().contains("colonies-"))
                {
                    return;
                }

                fileList.add(current);
            });

            if (fileList.size() <= MAX_BACKUPS)
            {
                return true;
            }

            fileList.sort(Comparator.comparing(path -> Uncheck.apply(p -> Files.getLastModifiedTime(path), path)));

            int deleteCount = fileList.size() - MAX_BACKUPS;
            for (Path current : fileList)
            {
                if (deleteCount <= 0)
                {
                    break;
                }

                deleteCount--;
                Files.delete(current);
            }
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
     * Loads the colony managers backup file
     */
    public static void loadManagerBackup()
    {
        try
        {
            @NotNull final Path file = BackUpHelper.getSaveLocation();
            @Nullable final CompoundTag data = BackUpHelper.loadNBTFromPath(file);
            if (data != null)
            {
                Log.getLogger().info("Loading Minecolonies colony manager Backup Data");
                IColonyManager.getInstance().read(data);
                Log.getLogger().info("Backup Load Complete");
            }
        }
        catch (Exception e)
        {
            Log.getLogger().error("Error during restoring colony manager:", e);
        }
    }

    /**
     * Loads all colonies from backup files which the world cap is missing.
     */
    public static void loadMissingColonies()
    {
        final Path saveDir = getRootSaveDir();

        for (final ResourceKey<Level> dimensionType : ServerLifecycleHooks.getCurrentServer().levelKeys())
        {
            int missingFilesInRow = 0;
            for (int i = 1; i <= MAX_COLONY_LOAD && missingFilesInRow < 5; i++)
            {
                // Check non-deleted files for colony id + dim
                final Path file = getFolderForDimension(saveDir, dimensionType.location()).resolve(String.format(FILENAME_COLONY, i));
                if (Files.exists(file))
                {
                    missingFilesInRow = 0;
                    // Load colony if null
                    if (IColonyManager.getInstance().getColonyByDimension(i, dimensionType) == null)
                    {
                        loadColonyBackup(i, dimensionType, false, false);
                    }
                }
                else
                {
                    missingFilesInRow++;
                }
            }
        }
    }

    /**
     * Get save location for Minecolonies backup data, from the world/save directory.
     *
     * @param date the current time.
     * @return Save file for minecolonies.
     */
    @NotNull
    private static Path getBackupSaveLocation(final Date date)
    {
        return getRootSaveDir().resolve(String.format(FILENAME_MINECOLONIES_BACKUP, new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(date)));
    }

    private static Path getRootSaveDir()
    {
        return ServerLifecycleHooks.getCurrentServer().getWorldPath(LevelResource.ROOT).resolve(FILENAME_MINECOLONIES_PATH);
    }

    /**
     * Add zip to file.
     *
     * @param fileName the file name.
     * @param zos      the output stream.
     * @param folder   the folder.
     */
    private static void addToZipFile(final Path fileName, final ZipOutputStream zos, final Path folder)
    {
        addFileToZipWithPath(fileName, zos, folder.resolve(fileName));
    }

    /**
     * Add the file to the given zip, with the path
     *
     * @param zipPath path to use in the zip
     * @param zos     zip
     * @param file    file to put
     */
    private static void addFileToZipWithPath(final Path zipPath, final ZipOutputStream zos, final Path file)
    {
        try
        {
            zos.putNextEntry(new ZipEntry(zipPath.toString()));
            Files.copy(file, zos);
        }
        catch (final Exception e)
        {
            /*
             * Intentionally not being thrown.
             */
            Log.getLogger().warn("Error packing " + zipPath + " into the zip.", e);
        }
    }

    /**
     * Add the file to the given zip, with the path
     *
     * @param zipPath path to use in the zip
     * @param zos     zip
     * @param file    file to put
     */
    @Deprecated
    private static void addFileToZipWithPath(final String zipPath, final ZipOutputStream zos, final Path file)
    {
        try
        {
            zos.putNextEntry(new ZipEntry(zipPath));
            Files.copy(file, zos);
        }
        catch (final Exception e)
        {
            /*
             * Intentionally not being thrown.
             */
            Log.getLogger().warn("Error packing " + zipPath + " into the zip.", e);
        }
    }

    /**
     * Get save location for Minecolonies data, from the world/save directory.
     *
     * @return Save file for minecolonies.
     */
    @NotNull
    public static Path getSaveLocation()
    {
        final Path saveDir = getRootSaveDir();
        return saveDir.resolve(FILENAME_MINECOLONIES);
    }

    /**
     * Save an CompoundTag to a file.  Does so in a safe manner using an intermediate tmp file.
     *
     * @param file     The destination file to write the data to.
     * @param compound The CompoundTag to write to the file.
     */
    public static void saveNBTToPath(@Nullable final Path file, @NotNull final CompoundTag compound)
    {
        try
        {
            if (file != null)
            {
                Files.createDirectories(file.getParent());
                NbtIo.write(compound, file);
            }
        }
        catch (final IOException exception)
        {
            Log.getLogger().error("Exception when saving ColonyManager", exception);
        }
    }

    /**
     * Load a file and return the data as an CompoundTag.
     *
     * @param file The path to the file.
     * @return the data from the file as an CompoundTag, or null.
     */
    public static CompoundTag loadNBTFromPath(@Nullable final Path file)
    {
        try
        {
            if (file != null && Files.exists(file))
            {
                return NbtIo.read(file);
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
        @NotNull final CompoundTag compound = new CompoundTag();
        IColonyManager.getInstance().write(compound);

        @NotNull final Path file = getSaveLocation();
        saveNBTToPath(file, compound);
        final Path saveDir = getRootSaveDir();
        for (final IColony colony : IColonyManager.getInstance().getAllColonies())
        {
            final CompoundTag colonyCompound = new CompoundTag();
            colony.write(colonyCompound);
            saveNBTToPath(getFolderForDimension(saveDir, colony.getDimension().location()).resolve(String.format(FILENAME_COLONY, colony.getID())), colonyCompound);
        }
    }

    /**
     * Marks a colony's backup file as deleted.
     *
     * @param colonyID    id of the colony to delete
     * @param dimensionID dimension of the colony to delete
     */
    public static void markColonyDeleted(final int colonyID, final ResourceKey<Level> dimensionID)
    {
        final Path saveDir = getRootSaveDir();
        final Path toDelete = getFolderForDimension(saveDir, dimensionID.location()).resolve(String.format(FILENAME_COLONY, colonyID));
        if (Files.exists(toDelete))
        {
            final Path fileName = getFolderForDimension(saveDir, dimensionID.location()).resolve(String.format(FILENAME_COLONY_DELETED, colonyID));
            try
            {
                Files.delete(toDelete);
                Files.move(toDelete, fileName);
            }
            catch (final IOException e)
            {
                Log.getLogger().error("Exception when marking colony as deleted!", e);
            }
        }
    }

    /**
     * Loads all colonies from the backup files, skips deleted colonies.
     */
    public static void loadAllBackups()
    {
        final Path saveDir = getRootSaveDir();

        ServerLifecycleHooks.getCurrentServer().levelKeys().forEach(dimensionType -> {
            for (int i = 1; i <= IColonyManager.getInstance().getTopColonyId() + 1; i++)
            {
                final Path file = getFolderForDimension(saveDir, dimensionType.location()).resolve(String.format(FILENAME_COLONY, i));
                if (Files.exists(file))
                {
                    loadColonyBackup(i, dimensionType, false, false);
                }
            }
        });
    }

    /**
     * Resource location name to file name
     *
     * @param location resource location
     * @return file name to look for
     */
    private static Path getFolderForDimension(final Path base, final ResourceLocation location)
    {
        return base.resolve(location.getNamespace()).resolve(location.getPath());
    }

    /**
     * Resource location name to file name
     *
     * @param location resource location
     * @return file name to look for
     */
    private static Path getFolderForDimension(final ResourceLocation location)
    {
        return Path.of(location.getNamespace(), location.getPath());
    }

    /**
     * Load the colony backup by colony, also works for backups of deleted colonies.
     *
     * @param colonyId    of the colony.
     * @param dimension   the colony dimension.
     * @param loadDeleted whether to load deleted colonies aswell.
     * @param claimChunks if chunks shall be claimed on loading.
     */
    public static void loadColonyBackup(final int colonyId, final ResourceKey<Level> dimension, boolean loadDeleted, boolean claimChunks)
    {
        final Path saveDir = getRootSaveDir();
        final Path backupFile = getFolderForDimension(saveDir, dimension.location()).resolve(String.format(FILENAME_COLONY, colonyId));
        CompoundTag compound = loadNBTFromPath(backupFile);
        if (compound == null)
        {
            if (loadDeleted)
            {
                compound = loadNBTFromPath(getFolderForDimension(saveDir, dimension.location()).resolve(String.format(FILENAME_COLONY_DELETED, colonyId)));
            }
            if (compound == null)
            {
                Log.getLogger().warn("Can't find NBT of colony: " + colonyId + " at location: " + backupFile);
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
            final ServerLevel colonyWorld = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer().getLevel(dimension);
            final Colony loadedColony = Colony.loadColony(compound, colonyWorld);
            if (loadedColony == null || colonyWorld == null)
            {
                Log.getLogger().warn("Colony:" + colonyId + " loadBackup failed!");
                return;
            }

            IColonyManager.getInstance().addColonyDirect(loadedColony, colonyWorld);

            if (claimChunks)
            {
                final LevelChunk chunk = ((LevelChunk) colonyWorld.getChunk(loadedColony.getCenter()));
                if (ColonyUtils.getOwningColony(chunk) != colonyId)
                {
                    reclaimChunks(loadedColony);
                }
            }
        }

        Log.getLogger().warn("Successfully restored colony:" + colonyId);
    }

    /**
     * Reclaims chunks for a colony
     *
     * @param colony
     */
    public static void reclaimChunks(final Colony colony)
    {
        ChunkDataHelper.claimColonyChunks(colony.getWorld(), true, colony, colony.getCenter());
        for (final IBuilding building : colony.getBuildingManager().getBuildings().values())
        {
            ChunkDataHelper.claimBuildingChunks(colony,
              true,
              building.getPosition(),
              building.getClaimRadius(building.getBuildingLevel()),
              building.getCorners());
        }
    }

    /**
     * Exports a certain colony and its part of the world and colony data data to a zip
     *
     * @param colony colony to export
     * @return file Path and name
     */
    public static String exportColony(final IColony colony)
    {
        // TODO: move this to nio Path
        final MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        final File topworldDir = server.getWorldPath(LevelResource.ROOT).toFile();
        final File minecraftDir = new File(topworldDir.getAbsolutePath().replace(topworldDir.getPath(), ""));

        final String worldname = topworldDir.getParent().replace("." + File.separator, "");
        final String minecoloniesZipDir = worldname + File.separator + "minecolonies";
        final File saveDir = new File(topworldDir, FILENAME_MINECOLONIES_PATH);
        try (FileOutputStream fos = new FileOutputStream(new File(saveDir, String.format(FILENAME_EXPORT, colony.getID()))))
        {
            final ZipOutputStream zos = new ZipOutputStream(fos);

            // Save region content for Colony
            final File regionDir = new File(DimensionType.getStorageFolder(colony.getDimension(), server.getWorldPath(LevelResource.ROOT)).toFile(), REGION_FOLDER);

            int maxX = Integer.MIN_VALUE;
            int minX = Integer.MAX_VALUE;
            int maxZ = Integer.MIN_VALUE;
            int minZ = Integer.MAX_VALUE;

            for (final BlockPos buildingPos : colony.getBuildingManager().getBuildings().keySet())
            {
                if (buildingPos.getX() > maxX)
                {
                    maxX = buildingPos.getX();
                }

                if (buildingPos.getX() < minX)
                {
                    minX = buildingPos.getX();
                }

                if (buildingPos.getZ() > maxZ)
                {
                    maxZ = buildingPos.getZ();
                }

                if (buildingPos.getZ() < minZ)
                {
                    minZ = buildingPos.getZ();
                }
            }

            // Convert to region coords
            maxX = maxX >> 9;
            minX = minX >> 9;
            maxZ = maxZ >> 9;
            minZ = minZ >> 9;

            for (final File currentRegion : regionDir.listFiles())
            {
                if (currentRegion != null && currentRegion.getName().contains(".mca"))
                {
                    final String[] split = currentRegion.getName().split("\\.");
                    if (split.length != 4)
                    {
                        continue;
                    }

                    // Current region file X/Z positions
                    final int regionX = Integer.parseInt(split[1]);
                    final int regionZ = Integer.parseInt(split[2]);

                    if (regionX <= maxX && regionX >= minX && regionZ <= maxZ && regionZ >= minZ)
                    {
                        addFileToZipWithPath(regionDir.getPath().replace("." + File.separator, "") + File.separator + currentRegion.getName(), zos, currentRegion.toPath());
                    }
                }
            }

            // Save colony.dat backup
            final File file = new File(saveDir, getFolderForDimension(colony.getDimension().location()) + String.format(FILENAME_COLONY, colony.getID()));
            final File fileDeleted = new File(saveDir, getFolderForDimension(colony.getDimension().location()) + String.format(FILENAME_COLONY_DELETED, colony.getID()));
            if (file.exists())
            {
                addFileToZipWithPath(
                  minecoloniesZipDir + File.separator + getFolderForDimension(colony.getDimension().location()) + String.format(FILENAME_COLONY, colony.getID()), zos, file.toPath());
            }

            if (fileDeleted.exists())
            {
                addFileToZipWithPath(
                  minecoloniesZipDir + File.separator + getFolderForDimension(colony.getDimension().location()) + String.format(FILENAME_COLONY_DELETED, colony.getID()),
                  zos,
                  file.toPath());
            }

            // Save colony manager
            final File colonyManager = getSaveLocation().toFile();
            if (colonyManager.exists())
            {
                addFileToZipWithPath(minecoloniesZipDir + File.separator + colonyManager.getName(), zos, colonyManager.toPath());
            }

            // Save level.dat
            final File levelDat = new File(topworldDir, "level.dat");
            if (levelDat.exists())
            {
                addFileToZipWithPath(worldname + File.separator + levelDat.getName(), zos, levelDat.toPath());
            }

            // Save config
            final File config = new File(topworldDir, "serverconfig" + File.separator + "minecolonies-server.toml");
            if (config.exists())
            {
                addFileToZipWithPath(worldname + File.separator + "serverconfig" + File.separator + "minecolonies-server.toml", zos, config.toPath());
            }

            // Mod list
            final File modFolder = new File(minecraftDir, "mods");
            final Set<String> mods = new HashSet<>();
            if (modFolder.exists() && modFolder.isDirectory())
            {
                for (final File mod : modFolder.listFiles())
                {
                    if (mod.exists())
                    {
                        mods.add(mod.getName());
                    }
                }
            }

            if (!mods.isEmpty())
            {
                zos.putNextEntry(new ZipEntry(worldname + File.separator + "mods.txt"));
                for (final String mod : mods)
                {
                    zos.write(mod.concat("\n").getBytes());
                }
            }

            // Latest.log
            final File latestlog = new File(minecraftDir, "logs" + File.separator + "latest.log");
            if (latestlog.exists())
            {
                addFileToZipWithPath(worldname + File.separator + latestlog.getName(), zos, latestlog.toPath());
            }

            zos.close();
        }
        catch (final Exception e)
        {
            /*
             * Intentionally not being thrown.
             */
            Log.getLogger().warn("Unable to to create colony export", e);
            return "Unable to to create colony export";
        }


        return new File(saveDir, String.format(FILENAME_EXPORT, colony.getID())).getAbsolutePath();
    }
}
