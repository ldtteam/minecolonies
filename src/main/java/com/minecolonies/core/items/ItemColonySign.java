package com.minecolonies.core.items;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import com.minecolonies.core.tileentities.TileEntityColonySign;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Class describing the colony sign item.
 */
public class ItemColonySign extends BlockItem
{
    /**
     * Tag of the colony.
     */
    public static final String TAG_COLONY = "colony";

    /**
     * Sets the name, creative tab, and registers the Clipboard item.
     *
     * @param properties the properties.
     */
    public ItemColonySign(final Properties properties)
    {
        super(ModBlocks.blockColonySign, properties.stacksTo(STACKSIZE));
    }

    @Override
    public InteractionResult useOn(final UseOnContext ctx)
    {
        final ItemStack sign = ctx.getPlayer().getItemInHand(ctx.getHand());

        final CompoundTag compound = sign.getOrCreateTag();
        final BlockEntity entity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
        if (ctx.getPlayer().isShiftKeyDown())
        {
            if (entity instanceof TileEntityColonyBuilding buildingEntity)
            {
                //todo only on gatehouse!
                compound.putInt(TAG_COLONY, buildingEntity.getColonyId());
                if (!ctx.getLevel().isClientSide)
                {
                    MessageUtils.format(COM_MINECOLONIES_SIGN_COLONY_SET, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
                }
                return InteractionResult.SUCCESS;
            }
            else if (entity instanceof TileEntityColonySign signEntity)
            {
                compound.putInt(TAG_COLONY, signEntity.getColonyId());
                BlockPosUtil.write(compound, TAG_POS, ctx.getClickedPos());
                if (!ctx.getLevel().isClientSide)
                {
                    MessageUtils.format(COM_MINECOLONIES_SIGN_COLONY_SET, signEntity.getColonyName()).sendTo(ctx.getPlayer());
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(ctx);
    }

    @Override
    protected boolean canPlace(final BlockPlaceContext ctx, final BlockState state)
    {
        if (!ctx.getItemInHand().getOrCreateTag().contains(TAG_COLONY))
        {
            if (ctx.getLevel().isClientSide)
            {
                MessageUtils.format(COM_MINECOLONIES_SIGN_NEED_COLONY);
            }
            return false;
        }
        return super.canPlace(ctx, state);
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint = Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_CHORUS_BREAD_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        tooltip.add(guiHint);

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
