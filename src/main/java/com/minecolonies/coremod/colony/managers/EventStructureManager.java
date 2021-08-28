package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.util.BlockInfo;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.colony.managers.interfaces.IEventStructureManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CreativeBuildingStructureHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.util.CreativeRaiderStructureHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider.TAG_BLUEPRINTDATA;
import static com.ldtteam.structurize.management.Structures.SCHEMATIC_EXTENSION_NEW;
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
      final String schematicPath,
      final BlockPos targetSpawnPoint,
      final int eventID,
      final int rotations,
      final Mirror mirror)
    {
        if (eventManager.getEventByID(eventID) == null)
        {
            return false;
        }

        final Level world = colony.getWorld();

        int y = 3;

        final BlockInfo info = structure.getBlockInfoAsMap().getOrDefault(structure.getPrimaryBlockOffset(), null);
        if (info.getTileEntityData() != null)
        {
            final CompoundTag teData = structure.getTileEntityData(targetSpawnPoint, structure.getPrimaryBlockOffset());
            if (teData != null && teData.contains(TAG_BLUEPRINTDATA))
            {
                final BlockEntity entity = BlockEntity.loadStatic(info.getState(), info.getTileEntityData());
                if (entity instanceof IBlueprintDataProvider)
                {
                    for (final Map.Entry<BlockPos, List<String>> entry : ((IBlueprintDataProvider) entity).getPositionedTags().entrySet())
                    {
                        if (entry.getValue().contains("groundlevel"))
                        {
                            y = entry.getKey().getY();
                        }
                    }
                }
            }
        }

        final BlockPos spawnPos = targetSpawnPoint.offset(0, -y, 0);

        final BlockPos zeroPos = targetSpawnPoint.subtract(structure.getPrimaryBlockOffset()).offset(0, -y, 0);
        final BlockPos cornerPos = new BlockPos(zeroPos.getX() + structure.getSizeX() - 1, zeroPos.getY() + structure.getSizeY(), zeroPos.getZ() + structure.getSizeZ() - 1);

        final BlockPos anchor = new BlockPos(zeroPos.getX() + structure.getSizeX() / 2, zeroPos.getY(), zeroPos.getZ() + structure.getSizeZ() / 2);

        final String backupPath = Structures.SCHEMATICS_PREFIX + "/" + STRUCTURE_BACKUP_FOLDER + "/" + colony.getID() + "/" + colony.getDimension().location().getNamespace() + colony.getDimension().location().getPath() + "/" + anchor;

        if (!ItemScanTool.saveStructureOnServer(world,
          zeroPos,
          cornerPos,
          backupPath,
          false))
        {
            // No structure spawn if we didn't successfully save the surroundings before
            Log.getLogger().info("Failed to save schematics for event");
            return false;
        }
        backupSchematics.put(anchor, eventID);

        CreativeRaiderStructureHandler.loadAndPlaceStructureWithRotation(world,
          schematicPath,
          spawnPos,
          BlockPosUtil.getRotationFromRotations(rotations),
          mirror,
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
                final String oldBackupPath = String.valueOf(colony.getID()) + colony.getDimension() + entry.getKey();
                String fileName = new StructureName("cache", "backup", Structures.SCHEMATICS_PREFIX + "/" + STRUCTURE_BACKUP_FOLDER).toString() + "/" +
                        String.valueOf(colony.getID()) + "/" + colony.getDimension().location().getNamespace() + colony.getDimension().location().getPath() + "/" + entry.getKey();

                // TODO: remove compat for colony.getDimension()-based file names after sufficient time has passed from PR#6305
                if(CreativeBuildingStructureHandler.loadAndPlaceStructureWithRotation(colony.getWorld(),
                    fileName,
                    entry.getKey(),
                    Rotation.NONE,
                    Mirror.NONE,
                    true, null) == null)
                {
                    fileName = new StructureName("cache", "backup", Structures.SCHEMATICS_PREFIX + STRUCTURE_BACKUP_FOLDER).toString() + oldBackupPath;
                    CreativeBuildingStructureHandler.loadAndPlaceStructureWithRotation(colony.getWorld(),
                            fileName,
                            entry.getKey(),
                            Rotation.NONE,
                            Mirror.NONE,
                            true, null);
                }

                try
                {
                    Structurize.proxy.getSchematicsFolder().toPath().resolve(fileName + SCHEMATIC_EXTENSION_NEW).toFile().delete();
                }
                catch (Exception e)
                {
                    Log.getLogger().info("Minor issue: Failed at deleting a backup schematic at " + fileName, e);
                }

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
            final ListTag schematicTags = structureManagerCompound.getList(TAG_SCHEMATIC_LIST, Constants.NBT.TAG_COMPOUND);

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
