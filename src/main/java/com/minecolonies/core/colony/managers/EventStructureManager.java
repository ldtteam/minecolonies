package com.minecolonies.core.colony.managers;

import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.blueprints.v1.BlueprintTagUtils;
import com.ldtteam.structurize.blueprints.v1.BlueprintUtil;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.operations.PlaceStructureOperation;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.CreativeStructureHandler;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.storage.ServerFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.colony.managers.interfaces.IEventStructureManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.util.CreativeRaiderStructureHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static com.ldtteam.structurize.api.constants.Constants.BLUEPRINT_FOLDER;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Manager for event structures
 */
public class EventStructureManager implements IEventStructureManager
{
    /**
     * NBT Tags
     */
    private static final String TAG_EVENT_STRUCTURE_MANAGER = "eventstructure_manager";

    /**
     * Backup schematics folder name.
     */
    public static final String STRUCTURE_BACKUP_FOLDER = "structbackup";

    /**
     * The map which holds the backup schematics to load. pos,event id
     */
    private final Map<BlockPos, Integer> backupSchematics = new HashMap<>();

    /**
     * Reference to the related event manager
     */
    private final EventManager eventManager;

    /**
     * The colony this manager belongs to
     */
    private final IColony colony;

    public EventStructureManager(final EventManager eventManager, final IColony colony)
    {
        this.eventManager = eventManager;
        this.colony = colony;
    }

    /**
     * Spawns the given structure at the blockpos and saves a backup for the previous blocks.
     *
     * @param structure the structure to spawn.
     * @param eventID   the id of the event.
     */
    @Override
    public boolean spawnTemporaryStructure(
      final Blueprint structure,
      final BlockPos targetSpawnPoint,
      final int eventID)
    {
        if (eventManager.getEventByID(eventID) == null)
        {
            return false;
        }

        final Level world = colony.getWorld();

        final int y = BlueprintTagUtils.getNumberOfGroundLevels(structure, 4) - 1;
        final BlockPos spawnPos = targetSpawnPoint.below(y).above(structure.getPrimaryBlockOffset().getY());
        final BlockPos zeroPos = spawnPos.subtract(structure.getPrimaryBlockOffset());
        final BlockPos anchor = new BlockPos(zeroPos.getX() + structure.getSizeX() / 2, zeroPos.getY(), zeroPos.getZ() + structure.getSizeZ() / 2);

        final Path outputPath = new File(".").toPath()
          .resolve(BLUEPRINT_FOLDER)
          .resolve(STRUCTURE_BACKUP_FOLDER)
          .resolve(Integer.toString(colony.getID()))
          .resolve(colony.getDimension().location().getNamespace() + colony.getDimension().location().getPath())
                                  .resolve(anchor.toString() + ".blueprint");

        final CompoundTag bp = BlueprintUtil.writeBlueprintToNBT(BlueprintUtil.createBlueprint(world, zeroPos, true,
                (short) structure.getSizeX(), structure.getSizeY(), (short) structure.getSizeZ(), anchor.toString(), Optional.of(anchor)));

        StructurePacks.storeBlueprint(STRUCTURE_BACKUP_FOLDER, bp, outputPath, world.registryAccess());

        backupSchematics.put(anchor, eventID);

        CreativeRaiderStructureHandler.loadAndPlaceStructure(world,
          structure,
          spawnPos,
          true, colony.getID(), (IColonyRaidEvent) eventManager.getEventByID(eventID), null);

        return true;
    }

    @Override
    public void loadBackupForEvent(final int eventID)
    {
        final Iterator<Map.Entry<BlockPos, Integer>> iterator = backupSchematics.entrySet().iterator();

        while (iterator.hasNext())
        {
            final Map.Entry<BlockPos, Integer> entry = iterator.next();

            if (entry.getValue() == eventID)
            {
                final Path backupPath = new File(".").toPath()
                  .resolve(BLUEPRINT_FOLDER)
                  .resolve(STRUCTURE_BACKUP_FOLDER)
                  .resolve(Integer.toString(colony.getID()))
                  .resolve(colony.getDimension().location().getNamespace() + colony.getDimension().location().getPath())
                  .resolve(entry.getKey().toString() + ".blueprint");


                ServerFutureProcessor.queueBlueprint(new ServerFutureProcessor.BlueprintProcessingData(StructurePacks.getBlueprintFuture(STRUCTURE_BACKUP_FOLDER, backupPath, colony.getWorld().registryAccess()), colony.getWorld(), (blueprint -> {

                    if (blueprint == null)
                    {
                        Log.getLogger().info("Minor issue: Failed to restore backup" + backupPath.toString());
                        return;
                    }

                    final IStructureHandler structure = new CreativeStructureHandler(colony.getWorld(), entry.getKey(), blueprint, RotationMirror.NONE, true);
                    Manager.addToQueue(new PlaceStructureOperation(new StructurePlacer(structure), null));

                    try
                    {
                        Files.delete(backupPath);
                    }
                    catch (Exception e)
                    {
                        Log.getLogger().info("Minor issue: Failed at deleting a backup schematic at " + backupPath.toString(), e);
                    }

                })));

                iterator.remove();
            }
        }
    }

    @Override
    public void readFromNBT(@NotNull final CompoundTag compound)
    {
        if (compound.contains(TAG_EVENT_STRUCTURE_MANAGER))
        {
            backupSchematics.clear();
            final CompoundTag structureManagerCompound = compound.getCompound(TAG_EVENT_STRUCTURE_MANAGER);
            final ListTag schematicTags = structureManagerCompound.getList(TAG_SCHEMATIC_LIST, Tag.TAG_COMPOUND);

            for (final Tag base : schematicTags)
            {
                final CompoundTag tagCompound = (CompoundTag) base;
                final BlockPos pos = BlockPosUtil.read(tagCompound, TAG_POS);
                final int eventID = tagCompound.getInt(TAG_EVENT_ID);
                if (eventManager.getEventByID(eventID) != null)
                {
                    backupSchematics.put(pos, eventID);
                }
                else
                {
                    loadBackupForEvent(eventID);
                    Log.getLogger().debug("Discarding schematic backup for event id:" + eventID + " seems the event went missing.");
                }
            }
        }
    }

    @Override
    public void writeToNBT(@NotNull final CompoundTag compound)
    {
        final CompoundTag structureManagerCompound = new CompoundTag();
        @NotNull final ListTag schematicTagList = new ListTag();

        for (final Map.Entry<BlockPos, Integer> entry : backupSchematics.entrySet())
        {
            final CompoundTag entryCompound = new CompoundTag();
            entryCompound.putInt(TAG_EVENT_ID, entry.getValue());
            BlockPosUtil.write(entryCompound, TAG_POS, entry.getKey());
            schematicTagList.add(entryCompound);
        }

        structureManagerCompound.put(TAG_SCHEMATIC_LIST, schematicTagList);
        compound.put(TAG_EVENT_STRUCTURE_MANAGER, structureManagerCompound);
    }
}
