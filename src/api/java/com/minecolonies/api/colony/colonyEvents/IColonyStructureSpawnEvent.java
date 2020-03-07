package com.minecolonies.api.colony.colonyEvents;

import com.minecolonies.api.util.Tuple;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Used by events which do spawn a structure in the world
 */
public interface IColonyStructureSpawnEvent extends IColonyEvent
{
    /**
     * Returns the list of used schematics and their positions. The string should be the full path to the schematic file.
     *
     * @return list of schematics
     */
    public List<Tuple<String, BlockPos>> getSchematicSpawns();
}
