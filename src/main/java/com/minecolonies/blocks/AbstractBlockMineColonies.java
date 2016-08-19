package com.minecolonies.blocks;

import com.minecolonies.achievements.ModAchievements;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * Abstract {@link Block} wrapper.
 * 
 * @author Isfirs
 * @since 0.1
 */
public abstract class AbstractBlockMineColonies extends Block
{

    /**
     * Parent constructor.
     * 
     * @param materialIn
     * @see Block#Block(Material)
     */
    public AbstractBlockMineColonies(Material materialIn)
    {
        super(materialIn);
    }

    /**
     * Parent constructor.
     *
     * @param blockMaterialIn
     * @param blockMapColorIn
     * @see Block#Block(Material, MapColor)
     */
    public AbstractBlockMineColonies(Material blockMaterialIn, MapColor blockMapColorIn)
    {
        super(blockMaterialIn, blockMapColorIn);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if (placer instanceof EntityPlayer)
        {
            final EntityPlayer player = (EntityPlayer) placer;
            final Block block = state.getBlock();

            if (block == ModBlocks.blockHutTownHall)
            {
                player.triggerAchievement(ModAchievements.achievementBuildingTownhall);
            }
        }

        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

}
