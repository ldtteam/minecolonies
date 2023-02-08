package com.minecolonies.api.blocks.decorative;

import com.minecolonies.api.blocks.interfaces.IBlockMinecolonies;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.tileentities.TileEntityColonyFlag;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents the common functions of both the wall and floor colony flag banner blocks
 */
public abstract class AbstractColonyFlagBanner<B extends AbstractColonyFlagBanner<B>> extends AbstractBannerBlock implements IBlockMinecolonies<AbstractColonyFlagBanner<B>>
{
    public static final String REGISTRY_NAME = "colony_banner";
    public static final String REGISTRY_NAME_WALL = "colony_wall_banner";

    public AbstractColonyFlagBanner()
    {
        super(
            DyeColor.WHITE,
            Properties.of(Material.WOOD)
                .noCollission()
                .strength(1F)
                .sound(SoundType.WOOD)
        );
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos blockPos, @NotNull final BlockState blockState)
    {
        return new TileEntityColonyFlag(blockPos, blockState);
    }

    @Override
    public void setPlacedBy(final Level worldIn, final @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack)
    {
        if (worldIn.isClientSide) return;

        BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof TileEntityColonyFlag && ((TileEntityColonyFlag) te).colonyId == -1 )
        {
            IColony colony = IColonyManager.getInstance().getIColony(worldIn, pos);

            // Allow the player to place their own beyond the colony
            if (colony == null && placer instanceof Player)
                colony = IColonyManager.getInstance().getIColonyByOwner(worldIn, (Player) placer);

            if (colony != null)
                ((TileEntityColonyFlag) te).colonyId = colony.getID();
        }

    }

    @NotNull
    @Override
    public ItemStack getCloneItemStack(final BlockGetter worldIn, @NotNull final BlockPos pos, @NotNull final BlockState state)
    {
        BlockEntity tileentity = worldIn.getBlockEntity(pos);
        if (tileentity instanceof TileEntityColonyFlag)
        {
            if (worldIn instanceof ClientLevel)
            {
                ((TileEntityColonyFlag)tileentity).getItemClient();
            }
            else
            {
                ((TileEntityColonyFlag)tileentity).getItemServer();
            }
        }
        return super.getCloneItemStack(worldIn, pos, state);
    }

    @Override
    public AbstractColonyFlagBanner<B> registerBlock(final IForgeRegistry<Block> registry)
    {
        registry.register(getRegistryName(), this);
        return this;
    }

    @Override
    public void registerBlockItem(final IForgeRegistry<Item> registry, final Item.Properties properties)
    {
        // Occurs in ModItems.
    }
}
