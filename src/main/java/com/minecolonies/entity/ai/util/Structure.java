package com.minecolonies.entity.ai.util;

import com.minecolonies.util.Schematic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
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
     * Calculates the position of the block we are working on.
     *
     * @return a BlockPos of that position.
     */
    public BlockPos getCurrentBlockPosition()
    {
        return this.schematic.getBlockPosition();
    }

    public SchematicBlock getCurrentBlock()
    {
        //initialize schematic if needed
        if (this.schematic.getBlock() == null)
        {
            advanceBlock();
        }
        return new SchematicBlock(
                this.schematic.getBlock(),
                this.schematic.getBlockPosition(),
                this.schematic.getMetadata(),
                this.schematic.getItem()
        );
    }

    public Boolean advanceBlock()
    {
        if (this.stage == Stage.CLEAR)
        {
            //todo: check if there is a better method for it
            return this.schematic.decrementBlock();
        }
        else
        {
            //todo: check if there is a better method for it
            return this.schematic.incrementBlock();
        }
    }

    /**
     * Calculates the width of this structure.
     *
     * @return the width as an int
     */
    public int getWidth()
    {
        return this.schematic.getWidth();
    }

    /**
     * Calculates the length of this structure.
     *
     * @return the length as an int
     */
    public int getLength()
    {
        return this.schematic.getLength();
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

    public static final class SchematicBlock
    {

        public final Block       block;
        public final BlockPos    blockPosition;
        public final IBlockState metadata;
        public final Item        item;

        /**
         * Create one immutable Block containing all information needed.
         *
         * @param block         the minecraft block this block has.
         * @param blockPosition the BlockPos this block has.
         * @param metadata      the metadata this block has.
         * @param item          the item needed to place this block
         */
        public SchematicBlock(final Block block, final BlockPos blockPosition, final IBlockState metadata, final Item item)
        {
            this.block = block;
            this.blockPosition = blockPosition;
            this.metadata = metadata;
            this.item = item;
        }
    }
}
