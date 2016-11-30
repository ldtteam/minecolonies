package com.minecolonies.entity.ai.util;

import com.minecolonies.configuration.Configurations;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.StructureWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a build task for the Structure AI.
 * <p>
 * It internally uses a schematic it transparently loads.
 */
public class Structure
{
    /**
     * This exception get's thrown when a StructureProxy file could not be loaded.
     */
    public static final class StructureException extends Exception
    {
        public static final long serialVersionUID = 8632728763984762837L;

        /**
         * Create this exception to throw a previously catched one.
         *
         * @param message the message to pass along
         * @param cause   the cause of this exception
         */
        public StructureException(final String message, final Throwable cause)
        {
            super(message, cause);
        }

        /**
         * Create this exception with a message.
         *
         * @param message the message to pass along.
         */
        public StructureException(final String message)
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
        public final Block       worldBlock;
        public final IBlockState worldMetadata;

        /**
         * Create one immutable Block containing all information needed.
         *
         * @param block         the minecraft block this block has.
         * @param blockPosition the BlockPos this block has.
         * @param metadata      the metadata this block has.
         * @param item          the item needed to place this block
         * @param worldBlock    the block to be replaced with the schematic block
         * @param worldMetadata the metadata of the world block
         */
        public SchematicBlock(final Block block, final BlockPos blockPosition, final IBlockState metadata, final Item item, final Block worldBlock, final IBlockState worldMetadata)
        {
            this.block = block;
            this.blockPosition = blockPosition;
            this.metadata = metadata;
            this.item = item;
            this.worldBlock = worldBlock;
            this.worldMetadata = worldMetadata;
        }
    }

    private final Stage            stage;
    /**
     * The internal schematic loaded.
     */
    @Nullable
    private final StructureWrapper schematic;
    /**
     * the targetWorld to build the structure in.
     */
    private final World            targetWorld;

    /**
     * Create a new building task.
     *
     * @param targetWorld       the world to build it in
     * @param buildingLocation  the location where we should build this Structure
     * @param schematicFileName the schematic file to load it from
     * @param rotation          the rotation it should have
     * @throws StructureException when there is an error loading the schematic file
     */
    public Structure(final World targetWorld, final BlockPos buildingLocation, final String schematicFileName, final int rotation) throws StructureException
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
    public Structure(final World targetWorld, final BlockPos buildingLocation, final String schematicFileName, final int rotation, final Stage stageProgress, final BlockPos blockProgress) throws StructureException
    {
        this.schematic = loadSchematic(targetWorld, buildingLocation, schematicFileName, rotation, stageProgress, blockProgress);
        this.stage = stageProgress;
        this.targetWorld = targetWorld;
    }

    /**
     * Load the schematic for this building.
     *
     * @param targetWorld       the world we want to place it
     * @param buildingLocation  the location where we should place the schematic
     * @param schematicFileName the filename of the schematic we should load
     * @param rotation          The rotation this schematic should be in
     * @param stageProgress     the stage we are in
     * @param blockProgress     the progress we have made so far
     * @throws StructureException when there is an error loading the schematic file
     */
    @Nullable
    private static StructureWrapper loadSchematic(
                                                   @Nullable final World targetWorld,
                                                   @Nullable final BlockPos buildingLocation,
                                                   @Nullable final String schematicFileName,
                                                   final int rotation,
                                                   final Stage stageProgress,
                                                   @Nullable final BlockPos blockProgress)
      throws StructureException
    {
        if (targetWorld == null || buildingLocation == null || schematicFileName == null)
        {
            throw new StructureException(String.format("Some parameters were null! (targetWorld: %s), (buildingLocation: %s), (schematicFileName: %s)",
              targetWorld, buildingLocation, schematicFileName));
        }
        @Nullable StructureWrapper tempSchematic = null;
        //failsafe for faulty schematic files
        try
        {
            tempSchematic = new StructureWrapper(targetWorld, schematicFileName);
        }
        catch (final IllegalStateException e)
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

    /**
     * Advance one block in the StructureProxy.
     * <p>
     * Will skip blocks not relevant.
     *
     * @return a Result enum specifying the result
     */
    @NotNull
    public Result advanceBlock()
    {
        switch (this.stage)
        {
            case CLEAR:
                return advanceBlocks(this.schematic::decrementBlock,
                  schematicBlock -> schematicBlock.blockPosition.getX() <= 0
                                      || this.targetWorld.isAirBlock(schematicBlock.blockPosition));
            case BUILD:
                return advanceBlocks(this.schematic::incrementBlock, schematicBlock -> false);
            case DECORATE:
                return advanceBlocks(this.schematic::incrementBlock, schematicBlock -> false);
            default:
                return Result.NEW_BLOCK;
        }
    }

    /**
     * Advance many blocks until either moveOneBlock or checkIfApplies return false
     * or if we reached the maximum of iterations in maxBlocksCheckedByBuilder.
     *
     * @param moveOneBlock   this will be called to advance the schematic one block.
     * @param checkIfApplies this will be evaluated to check if we should skip a block.
     * @return a Result enum specifying the result
     */
    @NotNull
    private Result advanceBlocks(@NotNull final Supplier<Boolean> moveOneBlock, @NotNull final Function<SchematicBlock, Boolean> checkIfApplies)
    {
        for (int i = 0; i < Configurations.maxBlocksCheckedByBuilder; i++)
        {
            if (!moveOneBlock.get())
            {
                return Result.AT_END;
            }
            if (!checkIfApplies.apply(getCurrentBlock()))
            {
                return Result.NEW_BLOCK;
            }
        }
        return Result.CONFIG_LIMIT;
    }

    /**
     * Gather all information needed to evaluate one block.
     *
     * @return a SchematicBlock having all information for the current block.
     */
    @NotNull
    public SchematicBlock getCurrentBlock()
    {
        return new SchematicBlock(
                                   this.schematic.getBlock(),
                                   this.schematic.getBlockPosition(),
                                   this.schematic.getBlockState(),
                                   this.schematic.getItem(),
                                   BlockPosUtil.getBlock(targetWorld, this.schematic.getBlockPosition()),
                                   BlockPosUtil.getBlockState(targetWorld, this.schematic.getBlockPosition())
        );
    }

    /**
     * Check if the worldBlock equals the schematicBlock
     *
     * @param blocksToTest the blocks to test
     * @return true if they are the same
     */
    private static boolean checkBlocksEqual(@NotNull final SchematicBlock blocksToTest)
    {
        return blocksToTest.block == blocksToTest.worldBlock
                 && Objects.equals(blocksToTest.metadata, blocksToTest.worldMetadata);
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
     * The different results when advancing the structure.
     */
    public enum Result
    {
        NEW_BLOCK,
        AT_END,
        CONFIG_LIMIT
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
}
