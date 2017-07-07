package com.minecolonies.coremod.entity.ai.util;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.util.StructureWrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a build task for the Structure AI.
 * <p>
 * It internally uses a structure it transparently loads.
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

    /**
     * Class used to describe a certain structure block of the structure.
     */
    public static final class StructureBlock
    {
        /**
         * The block.
         */
        public final Block block;

        /**
         * The position of the block.
         */
        public final BlockPos blockPosition;

        /**
         * The metadata of the block.
         */
        public final IBlockState metadata;

        /**
         * The item of the block.
         */
        public final Item item;

        /**
         * The world block at the same position.
         */
        public final Block worldBlock;

        /**
         * The metadata of the world block at the same position.
         */
        public final IBlockState worldMetadata;

        /**
         * The entityInfo block.
         */
        public final Template.EntityInfo entity;

        /**
         * Create one immutable Block containing all information needed.
         *
         * @param block         the minecraft block this block has.
         * @param blockPosition the BlockPos this block has.
         * @param metadata      the metadata this block has.
         * @param entity        the entity in the structure.
         * @param item          the item needed to place this block
         * @param worldBlock    the block to be replaced with the structure block
         * @param worldMetadata the metadata of the world block
         */
        public StructureBlock(
                               final Block block, final BlockPos blockPosition, final IBlockState metadata, final Template.EntityInfo entity,
                               final Item item, final Block worldBlock, final IBlockState worldMetadata)
        {
            this.block = block;
            this.blockPosition = blockPosition;
            this.metadata = metadata;
            this.entity = entity;
            this.item = item;
            this.worldBlock = worldBlock;
            this.worldMetadata = worldMetadata;
        }

        /**
         * Checks if the structureBlock equals the worldBlock.
         *
         * @return true if so.
         */
        public boolean doesStructureBlockEqualWorldBlock()
        {
            final IBlockState structureBlockState = metadata;
            final Block structureBlock = structureBlockState.getBlock();

            //All worldBlocks are equal the substitution block
            if (structureBlock == ModBlocks.blockSubstitution
                  || (structureBlock == ModBlocks.blockSolidSubstitution && worldMetadata.getMaterial().isSolid()
                        && !(worldBlock instanceof BlockOre) && worldBlock != Blocks.AIR))
            {
                return true;
            }

            final IBlockState worldBlockState = worldMetadata;

            //list of things to only check block for.
            //For the time being any flower pot is equal to each other.
            if (structureBlock instanceof BlockDoor || structureBlock == Blocks.FLOWER_POT)
            {
                return structureBlock == worldBlockState.getBlock();
            }
            else if ((structureBlock instanceof BlockStairs && structureBlockState.equals(worldBlockState))
                    || BlockUtils.isGrassOrDirt(structureBlock, worldBlock, structureBlockState, worldBlockState))
            {
                return true;
            }
            else if ((structureBlock == Blocks.DIRT || structureBlock == Blocks.GRASS) && (worldBlock == Blocks.DIRT || worldBlock == Blocks.GRASS))
            {
                return true;
            }

            return structureBlockState.equals(worldBlockState);
        }

        private static boolean structureBlockEqualsWorldBlock(@NotNull final Block structureBlock,
                @NotNull final Block worldBlock, @NotNull final IBlockState worldMetadata)
        {
            return structureBlock == ModBlocks.blockSubstitution || (structureBlock == ModBlocks.blockSolidSubstitution
                    && worldMetadata.getMaterial().isSolid() && !(worldBlock instanceof BlockOre) && worldBlock != Blocks.AIR);
        }
    }

    /**
     * The internal structure loaded.
     */
    @Nullable
    private final StructureWrapper structure;
    /**
     * the targetWorld to build the structure in.
     */
    private final World            targetWorld;
    private       Stage            stage;

    /**
     * Create a new building task.
     *
     * @param targetWorld       the world to build it in
     * @param buildingLocation  the location where we should build this Structure
     * @param schematicFileName the structure file to load it from
     * @param rotation          the rotation it should have
     * @param mirror            the mirror.
     * @throws StructureException when there is an error loading the structure file
     */
    public Structure(final World targetWorld, final BlockPos buildingLocation, final String schematicFileName, final int rotation, @NotNull final Mirror mirror)
      throws StructureException
    {
        this(targetWorld, buildingLocation, schematicFileName, rotation, Stage.CLEAR, null, mirror);
    }

    /**
     * Create a new building task.
     *
     * @param targetWorld       the world to build it in
     * @param buildingLocation  the location where we should build this Structure
     * @param structureFileName the structure file to load it from
     * @param rotation          the rotation it should have
     * @param stageProgress     the stage is should start with
     * @param blockProgress     the block it should start with
     * @param mirror            the mirror.
     * @throws StructureException when there is an error loading the structure file
     */
    public Structure(
                      final World targetWorld,
                      final BlockPos buildingLocation,
                      final String structureFileName,
                      final int rotation,
                      final Stage stageProgress,
                      final BlockPos blockProgress,
                      final Mirror mirror) throws StructureException
    {
        this.structure = loadStructure(targetWorld, buildingLocation, structureFileName, rotation, stageProgress, blockProgress, mirror);
        this.stage = stageProgress;
        this.targetWorld = targetWorld;
    }

    /**
     * Load the structure for this building.
     *
     * @param targetWorld       the world we want to place it
     * @param buildingLocation  the location where we should place the structure
     * @param schematicFileName the filename of the structure we should load
     * @param rotation          The rotation this structure should be in
     * @param stageProgress     the stage we are in
     * @param blockProgress     the progress we have made so far
     * @param mirror            the mirror.
     * @throws StructureException when there is an error loading the structure file
     */
    @Nullable
    private static StructureWrapper loadStructure(
                                                   @Nullable final World targetWorld,
                                                   @Nullable final BlockPos buildingLocation,
                                                   @Nullable final String schematicFileName,
                                                   final int rotation,
                                                   final Stage stageProgress,
                                                   @Nullable final BlockPos blockProgress,
                                                   @NotNull final Mirror mirror)
      throws StructureException
    {
        if (targetWorld == null || buildingLocation == null || schematicFileName == null)
        {
            throw new StructureException(String.format("Some parameters were null! (targetWorld: %s), (buildingLocation: %s), (schematicFileName: %s)",
              targetWorld, buildingLocation, schematicFileName));
        }
        @Nullable final StructureWrapper tempSchematic;
        //failsafe for faulty structure files
        try
        {
            tempSchematic = new StructureWrapper(targetWorld, schematicFileName);
        }
        catch (final IllegalStateException e)
        {
            throw new StructureException("failed to load structure file!", e);
        }

        //put the building into place
        tempSchematic.rotate(rotation, targetWorld, buildingLocation, mirror);
        tempSchematic.setPosition(buildingLocation);
        if (blockProgress != null)
        {
            tempSchematic.setLocalPosition(blockProgress);
        }
        return tempSchematic;
    }

    /**
     * Create a new building task.
     *
     * @param targetWorld   the world.
     * @param structure     the structure.
     * @param stageProgress the stage to start off with.
     */
    public Structure(final World targetWorld, final StructureWrapper structure, final Stage stageProgress)
    {
        this.structure = structure;
        this.stage = stageProgress;
        this.targetWorld = targetWorld;
    }

    /**
     * Check if the worldBlock equals the schematicBlock.
     *
     * @param blocksToTest the blocks to test.
     * @return true if they are the same.
     */
    public static boolean checkBlocksEqual(@NotNull final StructureBlock blocksToTest)
    {
        return blocksToTest.block == blocksToTest.worldBlock
                 && Objects.equals(blocksToTest.metadata, blocksToTest.worldMetadata);
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

    public void setStage(final Stage stage)
    {
        this.stage = stage;
    }

    /**
     * Calculates the position of the block we are working on.
     *
     * @return a BlockPos of that position.
     */
    public BlockPos getCurrentBlockPosition()
    {
        return this.structure.getBlockPosition();
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
                return advanceBlocks(this.structure::decrementBlock,
                  structureBlock -> structureBlock.doesStructureBlockEqualWorldBlock()
                                      || structureBlock.worldBlock == Blocks.AIR);
            case BUILD:
                return advanceBlocks(this.structure::incrementBlock, structureBlock -> structureBlock.doesStructureBlockEqualWorldBlock()
                                                                                         && structureBlock.block == Blocks.AIR
                                                                                         && !structureBlock.metadata.getMaterial().isSolid());
            case SPAWN:
                return advanceBlocks(this.structure::decrementBlock, structureBlock ->
                                                                       structureBlock.entity == null);
            case DECORATE:
                return advanceBlocks(this.structure::incrementBlock, structureBlock ->
                                                                       structureBlock.doesStructureBlockEqualWorldBlock()
                                                                         || structureBlock.metadata.getMaterial().isSolid());
            default:
                return Result.NEW_BLOCK;
        }
    }

    /**
     * Advance many blocks until either moveOneBlock or checkIfApplies return false
     * or if we reached the maximum of iterations in maxBlocksCheckedByBuilder.
     *
     * @param moveOneBlock   this will be called to advance the structure one block.
     * @param checkIfApplies this will be evaluated to check if we should skip a block.
     * @return a Result enum specifying the result
     */
    @NotNull
    private Result advanceBlocks(@NotNull final Supplier<Boolean> moveOneBlock, @NotNull final Function<StructureBlock, Boolean> checkIfApplies)
    {
        for (int i = 0; i < Configurations.gameplay.maxBlocksCheckedByBuilder; i++)
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
     * @return a StructureBlock having all information for the current block.
     */
    @NotNull
    public StructureBlock getCurrentBlock()
    {
        return new StructureBlock(
                                   this.structure.getBlock(),
                                   this.structure.getBlockPosition(),
                                   this.structure.getBlockState(),
                                   this.structure.getEntityinfo(),
                                   this.structure.getItem(),
                                   BlockPosUtil.getBlock(targetWorld, this.structure.getBlockPosition()),
                                   BlockPosUtil.getBlockState(targetWorld, this.structure.getBlockPosition())
        );
    }

    /**
     * Calculates the width of this structure.
     *
     * @return the width as an int
     */
    public int getWidth()
    {
        return this.structure.getWidth();
    }

    /**
     * Calculates the length of this structure.
     *
     * @return the length as an int
     */
    public int getLength()
    {
        return this.structure.getLength();
    }

    /**
     * Calculates the height of this structure.
     *
     * @return the height as an int
     */
    public int getHeight()
    {
        return this.structure.getHeight();
    }

    /**
     * Get the center position of the structure.
     * @return the blockPos.
     */
    public BlockPos getCenter()
    {
        return this.structure.getPosition();
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
