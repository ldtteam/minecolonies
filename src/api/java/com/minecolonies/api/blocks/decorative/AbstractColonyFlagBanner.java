package com.minecolonies.api.blocks.decorative;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.tileentities.TileEntityColonyFlag;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * Represents the common functions of both the wall and floor colony flag banner blocks
 */
public class AbstractColonyFlagBanner<B extends AbstractColonyFlagBanner<B>> extends AbstractBannerBlock implements IBlockMinecolonies<AbstractColonyFlagBanner<B>>
{
    public static final String REGISTRY_NAME = "colony_banner";
    public static final String REGISTRY_NAME_WALL = "colony_wall_banner";

    public AbstractColonyFlagBanner()
    {
        super(
            DyeColor.WHITE,
            Properties.create(Material.WOOD)
                .doesNotBlockMovement()
                .hardnessAndResistance(1F)
                .sound(SoundType.WOOD)
        );
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) { return new TileEntityColonyFlag(); }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        if (worldIn.isRemote) return;

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityColonyFlag)
        {
            IColony colony = IColonyManager.getInstance().getIColony(worldIn, pos);

            // Allow the player to place their own beyond the colony
            if (colony == null && placer instanceof PlayerEntity)
                colony = IColonyManager.getInstance().getIColonyByOwner(worldIn, (PlayerEntity) placer);

            if (colony != null)
                ((TileEntityColonyFlag) te).colonyId = colony.getID();
        }

    }

    @NotNull
    @Override
    public ItemStack getItem(final IBlockReader worldIn, @NotNull final BlockPos pos, @NotNull final BlockState state)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof TileEntityColonyFlag)
        {
            if (worldIn instanceof ClientWorld)
            {
                ((TileEntityColonyFlag)tileentity).getItemClient();
            }
            else
            {
                ((TileEntityColonyFlag)tileentity).getItemServer();
            }
        }
        return super.getItem(worldIn, pos, state);
    }

    @Override
    public AbstractColonyFlagBanner<B> registerBlock(IForgeRegistry<Block> registry)
    {
        registry.register(this);
        return this;
    }

    @Override
    public void registerBlockItem(IForgeRegistry<Item> registry, Item.Properties properties)
    {
        /* Registration occurs in ModItems */
    }
}
