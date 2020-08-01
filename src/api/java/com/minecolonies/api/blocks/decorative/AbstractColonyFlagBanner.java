package com.minecolonies.api.blocks.decorative;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.tileentities.TileEntityColonyFlag;
import com.minecolonies.api.util.constant.Suppression;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;

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
    public TileEntity createNewTileEntity(IBlockReader worldIn)
    {
        LogManager.getLogger().info("Creating new TileEntity");
        return new TileEntityColonyFlag();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        if (placer instanceof PlayerEntity)
        {
            IColony colony = IColonyManager.getInstance().getIColonyByOwner(worldIn, (PlayerEntity) placer);
            if (colony != null)
            {
                TileEntity te = worldIn.getTileEntity(pos);
                if (te instanceof TileEntityColonyFlag)
                    ((TileEntityColonyFlag) te).setColonyId(colony.getID());
            }
        }
    }

    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity instanceof TileEntityColonyFlag ? ((TileEntityColonyFlag)tileentity).getItem() : super.getItem(worldIn, pos, state);
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
