package com.minecolonies.coremod.util;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.structures.helpers.StructureProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for using the structure codebase.
 */
public final class StructureWrapper
{
    /**
     * The position we use as our uninitialized value.
     */
    private static final BlockPos NULL_POS = new BlockPos(-1, -1, -1);

    /**
     * The Structure position we are at. Defaulted to NULL_POS.
     */
    private final BlockPos.MutableBlockPos progressPos = new BlockPos.MutableBlockPos(-1, -1, -1);
    /**
     * The minecraft world this struture is displayed in.
     */
    private final World          world;
    /**
     * The structure this structure comes from.
     */
    private final StructureProxy structure;
    /**
     * The name this structure has.
     */
    private final String         name;
    /**
     * The anchor position this structure will be
     * placed on in the minecraft world.
     */
    private       BlockPos       position;

    /**
     * Load a structure into this world.
     *
     * @param worldObj the world to load in
     * @param name     the structure name
     */
    public StructureWrapper(final World worldObj, final String name)
    {
        this(worldObj, new StructureProxy(worldObj, name), name);
    }

    /**
     * Create a new StructureProxy.
     *
     * @param worldObj  the world to show it in
     * @param structure the structure it comes from
     * @param name      the name this structure has
     */
    private StructureWrapper(final World worldObj, final StructureProxy structure, final String name)
    {
        world = worldObj;
        this.structure = structure;
        this.name = name;
    }

    /**
     * Load a structure into this world
     * and place it in the right position and rotation.
     *
     * @param worldObj  the world to load it in
     * @param name      the structures name
     * @param pos       coordinates
     * @param rotations number of times rotated
     * @param mirror    the mirror used.
     */
    public static void loadAndPlaceStructureWithRotation(
                                                          final World worldObj, @NotNull final String name,
                                                          @NotNull final BlockPos pos, final int rotations, @NotNull final Mirror mirror)
    {
        try
        {
            @NotNull final StructureWrapper structureWrapper = new StructureWrapper(worldObj, name);
            structureWrapper.rotate(rotations, worldObj, pos, mirror);
            structureWrapper.placeStructure(pos);
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
    }

    /**
     * Load a structure into this world
     * and place it in the right position and rotation.
     *
     * @param worldObj  the world to load it in
     * @param name      the structures name
     * @param pos       coordinates
     * @param rotations number of times rotated
     * @param mirror    the mirror used.
     * @return true if succesful.
     */

    public static boolean tryToLoadAndPlaceSupplyCampWithRotation(final World worldObj, @NotNull final String name,
            @NotNull final BlockPos pos, final int rotations, @NotNull final Mirror mirror)
    {
        try
        {
            @NotNull final StructureWrapper structureWrapper = new StructureWrapper(worldObj, name);
            structureWrapper.rotate(rotations, worldObj, pos, mirror);
            if(structureWrapper.checkForFreeSpace(pos))
            {
                structureWrapper.placeStructure(pos);
                return true;
            }
            return false;
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
        return false;
    }

    /**
     * Rotates the structure x times.
     *
     * @param times     times to rotateWithMirror.
     * @param world     world it's rotating it in.
     * @param rotatePos position to rotateWithMirror it around.
     * @param mirror    the mirror to rotate with.
     */
    public void rotate(final int times, @NotNull final World world, @NotNull final BlockPos rotatePos, @NotNull final Mirror mirror)
    {
        structure.rotateWithMirror(times, world, rotatePos, mirror);
    }

    /**
     * Place a structure into the world.
     *
     * @param pos coordinates
     */
    private boolean checkForFreeSpace(@NotNull final BlockPos pos)
    {
        setLocalPosition(pos);
        //structure.getBlockInfo()[0].pos
        for (int j = 0; j < structure.getHeight(); j++)
        {
            for (int k = 0; k < structure.getLength(); k++)
            {
                for (int i = 0; i < structure.getWidth(); i++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(i, j, k);

                    final BlockPos worldPos = pos.add(localPos);

                    if(worldPos.getY() <= pos.getY() && !world.getBlockState(worldPos.down()).getMaterial().isSolid())
                    {
                        return false;
                    }

                    final IBlockState worldState = world.getBlockState(worldPos);
                    if(worldState.getBlock() == Blocks.BEDROCK)
                    {
                        return false;
                    }

                    if(worldPos.getY() > pos.getY() && worldState.getBlock() != Blocks.AIR)
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Place a structure into the world.
     *
     * @param pos coordinates
     */
    private void placeStructure(@NotNull final BlockPos pos)
    {
        setLocalPosition(pos);

        @NotNull final List<BlockPos> delayedBlocks = new ArrayList<>();

        //structure.getBlockInfo()[0].pos
        for (int j = 0; j < structure.getHeight(); j++)
        {
            for (int k = 0; k < structure.getLength(); k++)
            {
                for (int i = 0; i < structure.getWidth(); i++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(i, j, k);
                    final IBlockState localState = this.structure.getBlockState(localPos);
                    final Block localBlock = localState.getBlock();

                    final BlockPos worldPos = pos.add(localPos);
                    final IBlockState worldState = world.getBlockState(worldPos);

                    if (localBlock == ModBlocks.blockSubstitution)
                    {
                        continue;
                    }

                    if(localBlock == ModBlocks.blockSolidSubstitution)
                    {
                        if( !worldState.getMaterial().isSolid())
                        {
                            final IBlockState subBlock = BlockUtils.getSubstitutionBlockAtWorld(world, worldPos);
                            placeBlock(subBlock, subBlock.getBlock(), worldPos);
                        }
                    }
                    else if (localBlock == Blocks.AIR && !worldState.getMaterial().isSolid())
                    {
                        world.setBlockToAir(worldPos);
                    }
                    else if (localState.getMaterial().isSolid())
                    {
                        placeBlock(localState, localBlock, worldPos);
                    }
                    else
                    {
                        delayedBlocks.add(localPos);
                    }
                }
            }
        }

        for (@NotNull final BlockPos coords : delayedBlocks)
        {
            final IBlockState localState = this.structure.getBlockState(coords);
            final Block localBlock = localState.getBlock();
            final BlockPos newWorldPos = pos.add(coords);

            placeBlock(localState, localBlock, newWorldPos);
        }
    }

    private void placeBlock(final IBlockState localState, @NotNull final Block localBlock, @NotNull final BlockPos worldPos)
    {
        world.setBlockState(worldPos, localState, 0x03);
        if (world.getBlockState(worldPos).getBlock() == localBlock)
        {
            if (world.getBlockState(worldPos) != localState)
            {
                world.setBlockState(worldPos, localState, 0x03);
            }
            localBlock.onBlockAdded(world, worldPos, localState);
        }
    }

    /**
     * Find the next block that doesn't already exist in the world.
     *
     * @return true if a new block is found and false if there is no next block.
     */
    public boolean findNextBlock()
    {
        int count = 0;
        do
        {
            count++;
            if (!incrementBlock())
            {
                return false;
            }
        }
        while (isStructureBlockEqualWorldBlock() && count < Configurations.gameplay.maxBlocksCheckedByBuilder);

        return true;
    }

    /**
     * Increment progressPos.
     *
     * @return false if the all the block have been incremented through.
     */
    public boolean incrementBlock()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            this.progressPos.setPos(-1, 0, 0);
        }

        this.progressPos.setPos(this.progressPos.getX() + 1, this.progressPos.getY(), this.progressPos.getZ());
        if (this.progressPos.getX() == structure.getWidth())
        {
            this.progressPos.setPos(0, this.progressPos.getY(), this.progressPos.getZ() + 1);
            if (this.progressPos.getZ() == structure.getLength())
            {
                this.progressPos.setPos(this.progressPos.getX(), this.progressPos.getY() + 1, 0);
                if (this.progressPos.getY() == structure.getHeight())
                {
                    reset();
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks if the block in the world is the same as what is in the structure.
     *
     * @return true if the structure block equals the world block.
     */
    public boolean isStructureBlockEqualWorldBlock()
    {
        final IBlockState structureBlockState = structure.getBlockState(this.getLocalPosition());
        final Block structureBlock = structureBlockState.getBlock();

        //All worldBlocks are equal the substitution block
        if (structureBlock == ModBlocks.blockSubstitution)
        {
            return true;
        }

        final BlockPos worldPos = this.getBlockPosition();

        final IBlockState worldBlockState = world.getBlockState(worldPos);

        if(structureBlock == ModBlocks.blockSolidSubstitution && worldBlockState.getMaterial().isSolid())
        {
            return true;
        }

        //list of things to only check block for.
        //For the time being any flower pot is equal to each other.
        if (structureBlock instanceof BlockDoor || structureBlock == Blocks.FLOWER_POT)
        {
            return structureBlock == worldBlockState.getBlock();
        }
        else if ((structureBlock instanceof BlockStairs && structureBlockState == worldBlockState)
                || BlockUtils.isGrassOrDirt(structureBlock, worldBlockState.getBlock(), structureBlockState, worldBlockState))
        {
            return true;
        }

        final Template.EntityInfo entityInfo = structure.getEntityinfo(this.getLocalPosition());
        if (entityInfo != null)
        {
            return false;
            //todo get entity at position.
        }

        //had this problem in a super flat world, causes builder to sit doing nothing because placement failed
        return worldPos.getY() <= 0
                 || structureBlockState == worldBlockState;
    }

    /**
     * Reset the progressPos.
     */
    public void reset()
    {
        BlockPosUtil.set(this.progressPos, NULL_POS);
    }

    /**
     * @return progressPos as an immutable.
     */
    @NotNull
    public BlockPos getLocalPosition()
    {
        return this.progressPos.toImmutable();
    }

    /**
     * Change the current progressPos. Used when loading progress.
     *
     * @param localPosition new progressPos.
     */
    public void setLocalPosition(@NotNull final BlockPos localPosition)
    {
        BlockPosUtil.set(this.progressPos, localPosition);
    }

    /**
     * @return World position.
     */
    public BlockPos getBlockPosition()
    {
        return this.progressPos.add(getOffsetPosition());
    }

    /**
     * @return Min world position for the structure.
     */
    public BlockPos getOffsetPosition()
    {
        return getPosition().subtract(getOffset());
    }

    /**
     * Base position of the structure.
     *
     * @return BlockPos representing where the structure is.
     */
    public BlockPos getPosition()
    {
        if (position == null)
        {
            return new BlockPos(0, 0, 0);
        }
        return position;
    }

    /**
     * @return Where the hut (or any offset) is in the structure.
     */
    public BlockPos getOffset()
    {
        return structure.getOffset();
    }

    /**
     * Set the position, used when loading.
     *
     * @param position Where the structure is in the world.
     */
    public void setPosition(final BlockPos position)
    {
        this.position = position;
    }

    /**
     * Looks for blocks that should be cleared out.
     *
     * @return false if there are no more blocks to clear.
     */
    public boolean findNextBlockToClear()
    {
        int count = 0;
        do
        {
            count++;
            //decrement because we clear from top to bottom.
            if (!decrementBlock())
            {
                return false;
            }
        }
        //Check for air blocks and if blocks below the hut are different from the structure
        while ((worldBlockAir() || isStructureBlockEqualWorldBlock()) && count < Configurations.gameplay.maxBlocksCheckedByBuilder);

        return true;
    }

    /**
     * Looks for structure blocks to place.
     *
     * @return false if there are no more structure blocks left.
     */
    public boolean findNextBlockSolid()
    {
        int count = 0;
        do
        {
            count++;
            if (!incrementBlock())
            {
                return false;
            }
        }
        while ((isStructureBlockEqualWorldBlock() || isBlockNonSolid()) && count < Configurations.gameplay.maxBlocksCheckedByBuilder);

        return true;
    }

    private boolean isBlockNonSolid()
    {
        final IBlockState state = getBlockState();
        return state != null && !state.getMaterial().isSolid();
    }

    /**
     * Gets the block state for the current local block.
     *
     * @return Current local block state.
     */
    @Nullable
    public IBlockState getBlockState()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            return null;
        }
        return this.structure.getBlockState(this.progressPos);
    }

    /**
     * Looks for decoration blocks to place.
     *
     * @return false if there are no more structure blocks left.
     */
    public boolean findNextBlockNonSolid()
    {
        int count = 0;
        do
        {
            count++;
            if (!incrementBlock())
            {
                return false;
            }
        }
        while ((isStructureBlockEqualWorldBlock() || isBlockSolid()) && count < Configurations.gameplay.maxBlocksCheckedByBuilder);

        return true;
    }

    private boolean isBlockSolid()
    {
        final IBlockState state = getBlockState();
        return state != null && state.getMaterial().isSolid();
    }

    /**
     * Decrement progressPos.
     *
     * @return false if progressPos can't be decremented any more.
     */
    public boolean decrementBlock()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            this.progressPos.setPos(structure.getWidth(), structure.getHeight() - 1, structure.getLength() - 1);
        }

        this.progressPos.setPos(this.progressPos.getX() - 1, this.progressPos.getY(), this.progressPos.getZ());
        if (this.progressPos.getX() == -1)
        {
            this.progressPos.setPos(structure.getWidth() - 1, this.progressPos.getY(), this.progressPos.getZ() - 1);
            if (this.progressPos.getZ() == -1)
            {
                this.progressPos.setPos(this.progressPos.getX(), this.progressPos.getY() - 1, structure.getLength() - 1);
                if (this.progressPos.getY() == -1)
                {
                    reset();
                    return false;
                }
            }
        }

        return true;
    }

    private boolean worldBlockAir()
    {
        final BlockPos pos = this.getBlockPosition();
        //had this problem in a superflat world, causes builder to sit doing nothing because placement failed
        return pos.getY() <= 0 || world.isAirBlock(pos);
    }

    /**
     * @return A list of all the entities in the structure.
     */
    @NotNull
    public List<Template.EntityInfo> getEntities()
    {
        return structure.getTileEntities();
    }

    /**
     * Calculate the item needed to place the current block in the structure.
     *
     * @return an item or null if not initialized.
     */
    @Nullable
    public Item getItem()
    {
        @Nullable final Block block = this.getBlock();
        @Nullable final IBlockState blockState = this.getBlockState();
        if (block == null || blockState == null || block == Blocks.AIR || blockState.getMaterial().isLiquid())
        {
            return null;
        }

        return BlockUtils.getItemStackFromBlockState(blockState).getItem();
    }

    /**
     * Calculate the current block in the structure.
     *
     * @return the current block or null if not initialized.
     */
    @Nullable
    public Block getBlock()
    {
        @Nullable final IBlockState state = getBlockState();
        if (state == null)
        {
            return null;
        }
        return state.getBlock();
    }

    /**
     * @return The name of the structure.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return The height of the structure.
     */
    public int getHeight()
    {
        return structure.getHeight();
    }

    /**
     * @return The width of the structure.
     */
    public int getWidth()
    {
        return structure.getWidth();
    }

    /**
     * @return The length of the structure.
     */
    public int getLength()
    {
        return structure.getLength();
    }

    /**
     * @return The StructureProxy that houses all the info about what is stored
     * in a structure.
     */
    public StructureProxy structure()
    {
        return structure;
    }

    /**
     * Calculate the current block in the structure.
     *
     * @return the current block or null if not initialized.
     */
    @Nullable
    public Template.BlockInfo getBlockInfo()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            return null;
        }
        return this.structure.getBlockInfo(this.progressPos);
    }

    /**
     * Calculate the current entity in the structure.
     *
     * @return the entityInfo.
     */
    @Nullable
    public Template.EntityInfo getEntityinfo()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            return null;
        }
        return this.structure.getEntityinfo(this.progressPos);
    }
}
