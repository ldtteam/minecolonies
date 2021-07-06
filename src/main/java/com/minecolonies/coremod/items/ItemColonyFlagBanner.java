package com.minecolonies.coremod.items;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.tileentities.TileEntityColonyFlag;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * This item represents the colony flag banner, both wall and floor blocks.
 * Allows duplication of other banner pattern lists to its own default
 */
public class ItemColonyFlagBanner extends BannerItem
{
    public ItemColonyFlagBanner(String name, ItemGroup tab, Properties properties)
    {
        this(ModBlocks.blockColonyBanner, ModBlocks.blockColonyWallBanner, properties.stacksTo(16).tab(tab));
        setRegistryName(Constants.MOD_ID, name);
    }

    public ItemColonyFlagBanner(Block standingBanner, Block wallBanner, Properties builder)
    {
        super(standingBanner, wallBanner, builder);
    }

    @NotNull
    @Override
    public ActionResultType useOn(ItemUseContext context)
    {
        // Duplicate the patterns of the banner that was clicked on
        TileEntity te = context.getLevel().getBlockEntity(context.getClickedPos());
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        ItemStack stack = context.getPlayer().getMainHandItem();

        if (te instanceof BannerTileEntity || te instanceof TileEntityColonyFlag)
        {
            CompoundNBT source;
            if (te instanceof BannerTileEntity)
            {
                source = ((BannerTileEntity) te).getItem(state)
                           .getTag().getCompound("BlockEntityTag");
            }
            else
            {
                source = (context.getLevel().isClientSide ? ((TileEntityColonyFlag) te).getItemClient() : ((TileEntityColonyFlag) te).getItemServer())
                           .getTag().getCompound("BlockEntityTag");
            }

            ListNBT patternList = source.getList(TAG_BANNER_PATTERNS, 10);

            // Set the base pattern, if there wasn't one set.
            // This saves us attempting to alter the item itself to change the base color.
            if (!patternList.getCompound(0).getString(TAG_SINGLE_PATTERN).equals(BannerPattern.BASE.getHashname()))
            {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putString(TAG_SINGLE_PATTERN, BannerPattern.BASE.getHashname());
                nbt.putInt(TAG_PATTERN_COLOR, ((AbstractBannerBlock) state.getBlock()).getColor().getId());
                patternList.add(0, nbt);
            }

            CompoundNBT tag = stack.getOrCreateTagElement("BlockEntityTag");
            tag.put(TAG_BANNER_PATTERNS, patternList);

            return ActionResultType.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        // Remove the base, as they have no translations (Mojang were lazy. Or maybe saving space?)
        if (tooltip.size() > 1) tooltip.remove(1);
    }
}
