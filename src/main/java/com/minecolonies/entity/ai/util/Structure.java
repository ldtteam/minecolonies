package com.minecolonies.entity.ai.util;

import com.minecolonies.util.Schematic;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Represents a build task for the Structure AI.
 * <p>
 * It internally uses a schematic it transparently loads.
 */
public class Structure
{
    private final Stage     stage;
    /**
     * The internal schematic loaded.
     */
    private final Schematic schematic;

    /**
     * Create a new building task.
     *
     * @param targetWorld       the world to build it in
     * @param buildingLocation  the location where we should build this Structure
     * @param schematicFileName the schematic file to load it from
     * @param rotation          the rotation it should have
     * @param stageProgress     the stage is should start with
     * @param blockProgress     the block it should start with
     * @throws StructureException when there is an error loading the schematic file
     */
    public Structure(World targetWorld, BlockPos buildingLocation, String schematicFileName, int rotation) throws StructureException
    {
        this(targetWorld, buildingLocation, schematicFileName, rotation, Stage.CLEAR, null);
    }

    /**
     * Create a new building task.
     *
     * @param targetWorld       the world to build it in
     * @param buildingLocation  the location where we should build this Structure
     * @param schematicFileName the schematic file to load it from
     * @param rotation          the rotation it should have
     * @param stageProgress     the stage is should start with
     * @param blockProgress     the block it should start with
     * @throws StructureException when there is an error loading the schematic file
     */
    public Structure(World targetWorld, BlockPos buildingLocation, String schematicFileName, int rotation, Stage stageProgress, BlockPos blockProgress) throws StructureException
    {
        this.schematic = loadSchematic(targetWorld, buildingLocation, schematicFileName, rotation, stageProgress, blockProgress);
        this.stage = stageProgress;
    }

    /**
     * Load the schematic for this building.
     *
     * @param targetWorld       the targetWorld we want to place it
     * @param buildingLocation  the location where we should place the schematic
     * @param schematicFileName the filename of the schematic we should load
     * @param rotation          The rotation this schematic should be in
     * @param stageProgress     the stage we are in
     * @param blockProgress     the progress we have made so far
     * @throws StructureException when there is an error loading the schematic file
     */
    private static Schematic loadSchematic(World targetWorld, BlockPos buildingLocation, String schematicFileName, int rotation, Stage stageProgress, BlockPos blockProgress)
            throws StructureException
    {
        if (targetWorld == null || buildingLocation == null || schematicFileName == null)
        {
            throw new StructureException(String.format("Some parameters were null! (targetWorld: %s), (buildingLocation: %s), (schematicFileName: %s)",
                                                       targetWorld, buildingLocation, schematicFileName));
        }
        Schematic tempSchematic = null;
        //failsafe for faulty schematic files
        try
        {
            tempSchematic = new Schematic(targetWorld, schematicFileName);
        }
        catch (IllegalStateException e)
        {
            throw new StructureException("failed to load schematic file!", e);
        }

        //put the building into place
        tempSchematic.rotate(rotation);
        tempSchematic.setPosition(buildingLocation);
        //start this building by initializing the current work pointer
        if (stageProgress == Stage.CLEAR)
        {
            tempSchematic.decrementBlock();
        }
        else
        {
            tempSchematic.incrementBlock();
        }
        if (blockProgress != null)
        {
            tempSchematic.setLocalPosition(blockProgress);
        }
        return tempSchematic;
    }

    /**
     * Get the current stage we're in.
     *
     * @return the current Stage.
     */
    public Stage getStage()
    {
        return stage;
    }

    /**
     * Get the schematic for this Structure.
     *
     * @return the schematic.
     */
    public Schematic getSchematic()
    {
        return schematic;
    }

    /**
     * The different stages a Structure building process can be in.
     */
    public enum Stage
    {
        CLEAR,
        BUILD,
        DECORATE,
        SPAWN,
        COMPLETE
    }

    /**
     * This exception get's thrown when a Schematic file could not be loaded.
     */
    public static class StructureException extends Exception
    {
        /**
         * Create this exception to throw a previously catched one.
         *
         * @param message the message to pass along
         * @param cause   the cause of this exception
         */
        public StructureException(String message, Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Create this exception with a message.
         *
         * @param message the message to pass along.
         */
        public StructureException(String message)
        {
            super(message);
        }
    }
}
