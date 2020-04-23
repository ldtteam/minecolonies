package com.minecolonies.coremod.colony.managers;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.items.ItemScanTool;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.managers.interfaces.IEventStructureManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InstantStructurePlacer;
import com.minecolonies.api.util.Log;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.ldtteam.structurize.management.Structures.SCHEMATIC_EXTENSION_NEW;
import static com.minecolonies.api.colony.colonyEvents.NBTTags.TAG_EVENT_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_SCHEMATIC_LIST;

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
     * @param eventID the id of the event.
     */
    @Override
    public boolean spawnTemporaryStructure(
      final Structure structure,
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

        final World world = colony.getWorld();

        final String backupPath = Structures.SCHEMATICS_PREFIX + STRUCTURE_BACKUP_FOLDER + colony.getID() + colony.getDimension() + targetSpawnPoint.down(3);

        if (!ItemScanTool.saveStructureOnServer(world,
          targetSpawnPoint.add(structure.getWidth() - 1, structure.getHeight(), structure.getLength() - 1).subtract(structure.getOffset()),
          targetSpawnPoint.down(3).subtract(structure.getOffset()), backupPath, false))
        {
            // No structure spawn if we didnt successfully save the surroundings before
            Log.getLogger().info("Failed to save schematics for event");
            return false;
        }

        backupSchematics.put(targetSpawnPoint.down(3), eventID);

        InstantStructurePlacer.loadAndPlaceStructureWithRotation(world,
          schematicPath,
          targetSpawnPoint.down(3),
          rotations,
          mirror,
          false);

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
                final String backupPath = String.valueOf(colony.getID()) + colony.getDimension() + entry.getKey();
                final String fileName = new StructureName("cache", "backup", Structures.SCHEMATICS_PREFIX + STRUCTURE_BACKUP_FOLDER).toString() + backupPath;

                InstantStructurePlacer.loadAndPlaceStructureWithRotation(colony.getWorld(),
                  fileName,
                  entry.getKey(),
                  0,
                  Mirror.NONE,
                  true);

                try
                {

                    Structurize.proxy.getSchematicsFolder().toPath().resolve(fileName + SCHEMATIC_EXTENSION_NEW).toFile().delete();
                }
                catch (Exception e)
                {
                    Log.getLogger().info("Minor issue: Failed at deleteing a backup schematic at " + fileName, e);
                }

                iterator.remove();
            }
        }
    }

    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        if (compound.contains(TAG_EVENT_STRUCTURE_MANAGER))
        {
            backupSchematics.clear();
            final CompoundNBT structureManagerCompound = compound.getCompound(TAG_EVENT_STRUCTURE_MANAGER);
            final ListNBT schematicTags = structureManagerCompound.getList(TAG_SCHEMATIC_LIST, Constants.NBT.TAG_COMPOUND);

            for (final INBT base : schematicTags)
            {
                final CompoundNBT tagCompound = (CompoundNBT) base;
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
    public void writeToNBT(@NotNull final CompoundNBT compound)
    {
        final CompoundNBT structureManagerCompound = new CompoundNBT();
        @NotNull final ListNBT schematicTagList = new ListNBT();

        for (final Map.Entry<BlockPos, Integer> entry : backupSchematics.entrySet())
        {
            final CompoundNBT entryCompound = new CompoundNBT();
            entryCompound.putInt(TAG_EVENT_ID, entry.getValue());
            BlockPosUtil.write(entryCompound, TAG_POS, entry.getKey());
            schematicTagList.add(entryCompound);
        }

        structureManagerCompound.put(TAG_SCHEMATIC_LIST, schematicTagList);
        compound.put(TAG_EVENT_STRUCTURE_MANAGER, structureManagerCompound);
    }
}
