package com.minecolonies.coremod.entity.ai.util;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.AbstractBlockMinecoloniesRack;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import net.minecraft.block.*;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a build task for the StructureIterator AI.
 * <p>
 * It internally uses a structure it transparently loads.
 */
public class StructureIterator
{
    /**
     * The internal structure loaded.
     */
    @Nullable
    private final Structure theStructure;

    /**
     * the targetWorld to build the structure in.
     */
    private final World            targetWorld;
    private       Stage            stage;

    /**
     * Create a new building task.
     *
     * @param targetWorld       the world to build it in
     * @param buildingLocation  the location where we should build this StructureIterator
     * @param schematicFileName the structure file to load it from
     * @param rotation          the rotation it should have
     * @param mirror            the mirror.
     * @throws StructureException when there is an error loading the structure file
     */
    public StructureIterator(final World targetWorld, final BlockPos buildingLocation, final String schematicFileName, final int rotation, @NotNull final Mirror mirror)
      throws StructureException
    {
        this(targetWorld, buildingLocation, schematicFileName, rotation, Stage.CLEAR, null, mirror);
    }

    /**
     * Create a new building task.
     *
     * @param targetWorld       the world to build it in
     * @param buildingLocation  the location where we should build this StructureIterator
     * @param structureFileName the structure file to load it from
     * @param rotation          the rotation it should have
     * @param stageProgress     the stage is should start with
     * @param blockProgress     the block it should start with
     * @param mirror            the mirror.
     * @throws StructureException when there is an error loading the structure file
     */
    public StructureIterator(
                      final World targetWorld,
                      final BlockPos buildingLocation,
                      final String structureFileName,
                      final int rotation,
                      final Stage stageProgress,
                      final BlockPos blockProgress,
                      final Mirror mirror) throws StructureException
    {
        this.theStructure = loadStructure(targetWorld, buildingLocation, structureFileName, rotation, stageProgress, blockProgress, mirror);
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
     * @return the newly loaded structure
     */
    private static Structure loadStructure(
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
        @Nullable final Structure tempSchematic;
        //failsafe for faulty structure files
        try
        {
            tempSchematic = new Structure(targetWorld, schematicFileName, new PlacementSettings());
        }
        catch (final IllegalStateException e)
        {
            throw new StructureException("failed to load structure file!", e);
        }

        //put the building into place
        tempSchematic.rotate(BlockPosUtil.getRotationFromRotations(rotation), targetWorld, buildingLocation, mirror);
        tempSchematic.setPosition(buildingLocation);
        tempSchematic.setPlacementSettings(new PlacementSettings(mirror, BlockPosUtil.getRotationFromRotations(rotation)));
        if (blockProgress != null)
        {
            tempSchematic.setLocalPosition(blockProgress);
        }
        return tempSchematic;
    }

    /**
     * Check if the blueprint is missing.
     * @return true if so.
     */
    public boolean isBluePrintMissing()
    {
        return theStructure.isBluePrintMissing();
    }

    /**
     * Create a new building task.
     *
     * @param targetWorld   the world.
     * @param structure     the structure.
     * @param stageProgress the stage to start off with.
     */
    public StructureIterator(final World targetWorld, @Nullable final Structure structure, final Stage stageProgress)
    {
        this.theStructure = structure;
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
     * Get the center pos of the structure.
     * @return the BlockPos.
     */
    public BlockPos getPos()
    {
        return this.theStructure.getOffsetPosition();
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
        return this.theStructure.getBlockPosition();
    }

    /**
     * Calculates the local of the block we are working on.
     *
     * @return a BlockPos of that position.
     */
    public BlockPos getLocalBlockPosition()
    {
        return this.theStructure.getLocalPosition();
    }

    /**
     * Advance one block in the StructureProxy.
     * <p>
     * Will skip blocks not relevant.
     * @param abstractEntityAIStructure the entity.
     * @return a Result enum specifying the result
     */
    @NotNull
    public Result advanceBlock(final AbstractEntityAIStructure abstractEntityAIStructure)
    {
        switch (this.stage)
        {
            case CLEAR:
                return advanceBlocks(this.theStructure::decrementBlock,
                  structureBlock -> structureBlock.doesStructureBlockEqualWorldBlock()
                                      || structureBlock.worldBlock instanceof AirBlock
                                      || structureBlock.worldBlock instanceof FlowingFluidBlock);
            case BUILD:
                return advanceBlocks(this.theStructure::incrementBlock, structureBlock -> doesStructureBlockEqualWorldBlock(structureBlock, abstractEntityAIStructure)
                                                                                         || structureBlock.block instanceof AirBlock
                                                                                         || !structureBlock.metadata.getMaterial().isSolid());
            case FLUID_DETECT:
                return advanceBlocks(this.theStructure::decrementBlock, structureBlock -> (!(structureBlock.worldBlock instanceof FlowingFluidBlock)
                                                                                         || (structureBlock.block instanceof FlowingFluidBlock && structureBlock.worldBlock == structureBlock.block))
                                                                                         && (structureBlock.worldMetadata.getFluidState().isEmpty() || (!structureBlock.metadata.getFluidState().isEmpty() 
                                                                                         && structureBlock.worldMetadata.getFluidState().getFluid() == structureBlock.metadata.getFluidState().getFluid())));
            case DECORATE:
                return advanceBlocks(this.theStructure::incrementBlock, structureBlock -> doesStructureBlockEqualWorldBlock(structureBlock, abstractEntityAIStructure)
                                                                                         || structureBlock.metadata.getMaterial().isSolid());
            case REMOVE:
                return advanceBlocks(this.theStructure::decrementBlock, structureBlock -> structureBlock.worldBlock instanceof AirBlock || !(structureBlock.block instanceof AirBlock));
            case SPAWN:
                return advanceBlocks(this.theStructure::incrementBlock, structureBlock -> structureBlock.entity == null || structureBlock.entity.length <= 0);
            default:
                return Result.NEW_BLOCK;
        }
    }

    /**
     * Check if the block equals the world block and connect with the building if necessary.
     * @param block the structure block.
     * @param entityAIStructure the AI instance.
     * @return true if so.
     */
    private boolean doesStructureBlockEqualWorldBlock(final StructureBlock block, final AbstractEntityAIStructure entityAIStructure)
    {
        if (block.doesStructureBlockEqualWorldBlock())
        {
            entityAIStructure.connectBlockToBuildingIfNecessary(block.metadata, block.blockPosition);
            return true;
        }
        return false;
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
        for (int i = 0; i < MinecoloniesAPIProxy.getInstance().getConfig().getCommon().maxBlocksCheckedByBuilder.get(); i++)
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
        final CompoundNBT[] entityData;
        if (stage == Stage.SPAWN)
        {
            entityData = Arrays.stream(this.theStructure.getEntityData()).filter(data -> data != null && isAtPos(data, this.theStructure.getLocalPosition())).toArray(CompoundNBT[]::new);
        }
        else
        {
            entityData = null;
        }
        return new StructureBlock(this.theStructure.getBlock(),
                                   this.theStructure.getBlockPosition(),
                                   this.theStructure.getBlockstate() == null ? null : this.theStructure.getBlockstate().getBlockState(),
                                    entityData,
                                   this.theStructure.getItem(),
                                   BlockPosUtil.getBlock(targetWorld, this.theStructure.getBlockPosition()),
                                   BlockPosUtil.getBlockState(targetWorld, this.theStructure.getBlockPosition()));
    }

    private boolean isAtPos(@NotNull final CompoundNBT entityData, final BlockPos pos)
    {
        final ListNBT list = entityData.getList("Pos", 6);
        final int x = (int) list.getDouble(0);
        final int y = (int) list.getDouble(1);
        final int z = (int) list.getDouble(2);
        return new BlockPos(x, y, z).equals(pos);
    }

    /**
     * Calculates the width of this structure.
     *
     * @return the width as an int
     */
    public int getWidth()
    {
        return this.theStructure.getWidth();
    }

    /**
     * Calculates the length of this structure.
     *
     * @return the length as an int
     */
    public int getLength()
    {
        return this.theStructure.getLength();
    }

    /**
     * Calculates the height of this structure.
     *
     * @return the height as an int
     */
    public int getHeight()
    {
        return this.theStructure.getHeight();
    }

    /**
     * Get the center position of the structure.
     *
     * @return the blockPos.
     */
    public BlockPos getCenter()
    {
        return this.theStructure.getPosition();
    }

    /**
     * Set the current block.
     * @param progressPos the progress pos.
     */
    public void setCurrentBlock(@Nullable final BlockPos progressPos)
    {
        if (progressPos != null)
        {
            this.theStructure.setLocalPosition(progressPos);
        }
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
     * The different stages a StructureIterator building process can be in.
     */
    public enum Stage
    {
        CLEAR,
        BUILD,
        FLUID_DETECT,
        DECORATE,
        SPAWN,
        COMPLETE,
        REMOVE
    }

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
        public final BlockState metadata;

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
        public final BlockState worldMetadata;

        /**
         * The entityInfo block.
         */
        public final CompoundNBT[] entity;

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
          final Block block, final BlockPos blockPosition, final BlockState metadata, final CompoundNBT[] entity,
          final Item item, final Block worldBlock, final BlockState worldMetadata)
        {
            this.block = block;
            this.blockPosition = blockPosition;
            this.metadata = metadata;
            this.entity = entity == null ? null : entity.clone();
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
            if (metadata == null)
            {
                return true;
            }

            final BlockState structureBlockState = metadata;
            final Block structureBlock = structureBlockState.getBlock();

            //All worldBlocks are equal the substitution block
            if (structureBlockEqualsWorldBlock(structureBlock, worldBlock, worldMetadata))
            {
                return true;
            }

            final BlockState worldBlockState = worldMetadata;

            //list of things to only check block for.
            //For the time being any flower pot is equal to each other.
            if (structureBlock instanceof DoorBlock || structureBlock == Blocks.FLOWER_POT)
            {
                return structureBlock == worldBlockState.getBlock();
            }
            else if ((structureBlock instanceof StairsBlock && structureBlockState.equals(worldBlockState))
                       || (worldBlock instanceof AbstractBlockMinecoloniesRack && AbstractBlockMinecoloniesRack.shouldBlockBeReplacedWithRack(structureBlock)))
            {
                return true;
            }

            return structureBlockState.equals(worldBlockState);
        }

        private static boolean structureBlockEqualsWorldBlock(
          @NotNull final Block structureBlock,
          @NotNull final Block worldBlock, @NotNull final BlockState worldMetadata)
        {
            return structureBlock == com.ldtteam.structurize.blocks.ModBlocks.blockSubstitution || (
              structureBlock == com.ldtteam.structurize.blocks.ModBlocks.blockSolidSubstitution
                && worldMetadata.getMaterial().isSolid() && !(IColonyManager.getInstance().getCompatibilityManager().isOre(worldMetadata))
                && !(worldBlock instanceof AirBlock));
        }
    }
}
