package com.minecolonies.coremod.items;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.tileentities.TileEntityColonyFlag;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.*;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemColonyFlagBanner extends BannerItem
{
    public ItemColonyFlagBanner(String name, ItemGroup tab, Properties properties)
    {
        this(ModBlocks.blockColonyBanner, ModBlocks.blockColonyWallBanner, properties.maxStackSize(16).group(tab));
        setRegistryName(Constants.MOD_ID, name);
    }

    public ItemColonyFlagBanner(Block standingBanner, Block wallBanner, Properties builder)
    {
        super(standingBanner, wallBanner, builder);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        // Duplicate the patterns of the banner that was clicked on
        TileEntity te = context.getWorld().getTileEntity(context.getPos());
        BlockState state = context.getWorld().getBlockState(context.getPos());
        ItemStack stack = context.getPlayer().getHeldItemMainhand();

        if (te instanceof BannerTileEntity || te instanceof TileEntityColonyFlag) {
            CompoundNBT source =
                (te instanceof BannerTileEntity
                    ? ((BannerTileEntity) te).getItem(state)
                    : ((TileEntityColonyFlag) te).getItem())
                .getTag()
                .getCompound("BlockEntityTag");
            ListNBT patternList = source.getList("Patterns", 10);

            // Set the base pattern, if there wasn't one set.
            // This saves us attempting to alter the item itself to change the base color.
            if (!patternList.getCompound(0).getString("Pattern").equals(BannerPattern.BASE.getHashname()))
            {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putString("Pattern", BannerPattern.BASE.getHashname());
                nbt.putInt("Color", ((AbstractBannerBlock) state.getBlock()).getColor().getId());
                patternList.add(0, nbt);
            }

            CompoundNBT tag = stack.getOrCreateChildTag("BlockEntityTag");
            tag.put("Patterns", patternList);

            return ActionResultType.SUCCESS;
        }
        return super.onItemUse(context);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        // Remove the base, as they have no translations (Mojang were lazy. Or maybe saving space?)
        if (tooltip.size() > 1) tooltip.remove(1);
    }
}
