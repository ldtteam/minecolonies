package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.blocks.interfaces.IBlockMinecoloniesSeat;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.EntitySitable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract class for all seat blocks.
 */
public abstract class AbstractBlockMinecoloniesSeat<B extends AbstractBlockMinecoloniesSeat<B>> extends Block implements IBlockMinecoloniesSeat<B>
{
    /**
     * Holds a temporary pointer to the seat Entity
     */
    private EntitySitable seatEntity;

    /**
     * Constructor for the block.
     * @param materialIn Material type
     */
    public AbstractBlockMinecoloniesSeat(final Material materialIn)
    {
        super(materialIn);
    }

    
    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     * @return the block itself.
     */
    @Override
    @SuppressWarnings(Suppression.UNCHECKED)
    public B registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(this);
        return (B) this;
    }

    /**
     * Registery block at gameregistry.
     *
     * @param registry the registry to use.
     */
    @Override
    public void registerItemBlock(final IForgeRegistry<Item> registry)
    {
        registry.register((new ItemBlock(this)).setRegistryName(this.getRegistryName()));
    }

    /**
     * Checks if this block can be placed exactly at the given position.
     */
    @Override
    public boolean canPlaceBlockAt(final World worldIn, final BlockPos pos)
    {
        return super.canPlaceBlockAt(worldIn, pos) && this.canBlockStay(worldIn, pos);
    }

    /**
     * Called when a neighboring block was changed and marks that this state should perform any checks during a neighbor
     * change. Cases may include when redstone power is updated, cactus blocks popping off due to a neighboring solid
     * block, etc.
     */
    @Override
    public void neighborChanged(final IBlockState state, final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos)
    {
        this.checkForDrop(worldIn, pos, state);
    }

    /**
     * Checks to see if the block can stay or if it needs to fall.
     *
     * @param worldIn the world.
     * @param pos the position.
     * @param state the state.
     * @return return false if block dropped
     */
    private boolean checkForDrop(final World worldIn, final BlockPos pos, final IBlockState state)
    {
        if (!this.canBlockStay(worldIn, pos))
        {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Check the block below to see if you can place the seat at the Block Pos.
     *
     * @param worldIn the world.
     * @param pos the pos.
     * @return indicates if the block can stay at current location
     */
    private boolean canBlockStay(World worldIn, BlockPos pos)
    {
        final IBlockState state = worldIn.getBlockState(pos.down());
        return state.isFullBlock() && !worldIn.isAirBlock(pos.down()) && !(state.getBlock() instanceof AbstractBlockMinecoloniesSeat);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote)
        {
            // Creates a dummy entity the player can ride in order to show the
            // player as sitting
            if (playerIn != null && playerIn.getRidingEntity() == null
                    && worldIn.getBlockState(pos.add(0, 1, 0)).getBlock() == Blocks.AIR) {

                seatEntity = createSettingEntity(worldIn);
                if (!seatEntity.isBeingRidden())
                {
                    seatEntity.setPosition(pos.getX() + 0.5, pos.getY()+0.5, pos.getZ() + 0.5);
                    worldIn.spawnEntity(seatEntity);
                    return playerIn.startRiding(seatEntity);
                }
            }
            else if (playerIn != null)
            {
                playerIn.dismountRidingEntity();
                seatEntity = null;
            }
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.minecolonies.coremod.blocks.interfaces.IBlockMinecoloniesSeat#startSeating(net.minecraft.world.World,
     * net.minecraft.util.math.BlockPos, com.minecolonies.coremod.entity.EntityCitizen)
     */
    public boolean startSeating(final World world, final BlockPos chairPosition, final EntityCitizen citizen)
    {
        seatEntity = createSettingEntity(world);
        if (!seatEntity.isBeingRidden())
        {
            seatEntity.setPosition(chairPosition.getX() + 0.5, chairPosition.getY(), chairPosition.getZ() + 0.5);
            world.spawnEntity(seatEntity);
            return citizen.startRiding(seatEntity);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.minecolonies.coremod.blocks.interfaces.IBlockMinecoloniesSeat#dismountSeat(net.minecraft.world.World)
     */
    public void dismountSeat(final World world)
    {
        if (seatEntity != null)
        {
            seatEntity.removePassengers();
            seatEntity = null;
        }

    }

    /**
     * Creates a dummy entity so citizen/player can sit on the seat block.
     * @param world pointer to the world
     * @return returns a new entity to seat on.
     */
    private EntitySitable createSettingEntity(final World world)
    {
        return new EntitySitable(world, 0);
    }

    @NotNull
    @Override
    public BlockFaceShape getBlockFaceShape(final IBlockAccess worldIn, final IBlockState state, final BlockPos pos, final EnumFacing face)
    {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public boolean isFullBlock(final IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(final IBlockState state)
    {
        return false;
    }
}
