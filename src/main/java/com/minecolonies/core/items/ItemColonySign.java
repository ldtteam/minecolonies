package com.minecolonies.core.items;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.SoundUtils;
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
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_COLONY_ID;
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
        final BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (ctx.getPlayer().isShiftKeyDown())
        {
            if (state.getBlock() == ModBlocks.blockHutGateHouse && entity instanceof TileEntityColonyBuilding buildingEntity)
            {
                if (!ctx.getLevel().isClientSide)
                {
                    if (buildingEntity.getColony() == null)
                    {
                        MessageUtils.format(COM_MINECOLONIES_SIGN_NULL_COLONY).sendTo(ctx.getPlayer());
                        return InteractionResult.SUCCESS;
                    }

                    // Attempt Connect two colonies.
                    if (compound.contains(TAG_COLONY) && compound.getInt(TAG_COLONY) != buildingEntity.getColonyId())
                    {
                        final IColony sourceColony = IColonyManager.getInstance().getColonyByDimension(compound.getInt(TAG_COLONY), ctx.getLevel().dimension());
                        if (sourceColony == null)
                        {
                            MessageUtils.format(COM_MINECOLONIES_SIGN_NULL_COLONY).sendTo(ctx.getPlayer());
                            return InteractionResult.SUCCESS;
                        }

                        if (!sourceColony.getPermissions().hasPermission(ctx.getPlayer(), Action.MANAGE_HUTS))
                        {
                            MessageUtils.format(COM_MINECOLONIES_SIGN_COLONY_NO_PERM, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
                            return InteractionResult.SUCCESS;
                        }

                        sourceColony.getConnectionManager().attemptEstablishConnection(ctx.getClickedPos(), buildingEntity.getColony());
                        return InteractionResult.SUCCESS;
                    }

                    if (buildingEntity.getColony().getPermissions().hasPermission(ctx.getPlayer(), Action.MANAGE_HUTS))
                    {
                        compound.putInt(TAG_COLONY, buildingEntity.getColonyId());
                        BlockPosUtil.write(compound, TAG_POS, ctx.getClickedPos());
                        MessageUtils.format(COM_MINECOLONIES_SIGN_COLONY_SET, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
                    }
                    else
                    {
                        MessageUtils.format(COM_MINECOLONIES_SIGN_COLONY_NO_PERM, buildingEntity.getColony().getName()).sendTo(ctx.getPlayer());
                    }
                }
                return InteractionResult.SUCCESS;
            }
            else if (entity instanceof TileEntityColonySign signEntity)
            {
                if (!ctx.getLevel().isClientSide)
                {
                    final IColony colony = IColonyManager.getInstance().getColonyByDimension(signEntity.getColonyId(), ctx.getLevel().dimension());
                    if (colony == null)
                    {
                        MessageUtils.format(COM_MINECOLONIES_SIGN_NULL_COLONY).sendTo(ctx.getPlayer());
                        return InteractionResult.SUCCESS;
                    }

                    // Attempt connect two colonies.
                    if (compound.contains(TAG_COLONY) && compound.getInt(TAG_COLONY) != signEntity.getColonyId())
                    {
                        final IColony sourceColony = IColonyManager.getInstance().getColonyByDimension(compound.getInt(TAG_COLONY), ctx.getLevel().dimension());
                        if (sourceColony == null)
                        {
                            MessageUtils.format(COM_MINECOLONIES_SIGN_NULL_COLONY).sendTo(ctx.getPlayer());
                            return InteractionResult.SUCCESS;
                        }

                        if (!sourceColony.getPermissions().hasPermission(ctx.getPlayer(), Action.MANAGE_HUTS))
                        {
                            MessageUtils.format(COM_MINECOLONIES_SIGN_COLONY_NO_PERM, sourceColony.getName()).sendTo(ctx.getPlayer());
                            return InteractionResult.SUCCESS;
                        }

                        sourceColony.getConnectionManager().attemptEstablishConnection(ctx.getClickedPos(), colony);
                        return InteractionResult.SUCCESS;
                    }

                    if (colony.getPermissions().hasPermission(ctx.getPlayer(), Action.MANAGE_HUTS))
                    {
                        compound.putInt(TAG_COLONY, signEntity.getColonyId());
                        BlockPosUtil.write(compound, TAG_POS, ctx.getClickedPos());
                        MessageUtils.format(COM_MINECOLONIES_SIGN_COLONY_SET, colony.getName()).sendTo(ctx.getPlayer());
                    }
                    else
                    {
                        MessageUtils.format(COM_MINECOLONIES_SIGN_COLONY_NO_PERM, colony.getName()).sendTo(ctx.getPlayer());
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.useOn(ctx);
    }

    @Override
    protected boolean canPlace(final BlockPlaceContext ctx, final BlockState state)
    {
        if (!super.canPlace(ctx, state))
        {
            return false;
        }
        if (!ctx.getItemInHand().getOrCreateTag().contains(TAG_COLONY))
        {
            if (ctx.getLevel().isClientSide)
            {
                MessageUtils.format(COM_MINECOLONIES_SIGN_TOO_FAR).sendTo(ctx.getPlayer());
            }
            return false;
        }

        if (!ctx.getLevel().isClientSide)
        {
            final int colonyId = ctx.getItemInHand().getTag().getInt(TAG_COLONY);
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, ctx.getLevel().dimension());
            if (colony == null)
            {
                MessageUtils.format(COM_MINECOLONIES_SIGN_TOO_FAR).sendTo(ctx.getPlayer());
                return false;
            }
            if (colony.getConnectionManager().addNewConnectionNode(ctx.getClickedPos()))
            {
                SoundUtils.playSuccessSound(ctx.getPlayer(), ctx.getClickedPos());
                return true;
            }
            else
            {
                SoundUtils.playErrorSound(ctx.getPlayer(), ctx.getClickedPos());
                return false;
            }
        }

        return true;
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        if (stack.getOrCreateTag().contains(TAG_COLONY))
        {
            final MutableComponent colonyHint = Component.translatable(TranslationConstants.COM_MINECOLONIES_CORE_COLONY_SIGN_TOOLTIP_COLONY, stack.getTag().getInt(TAG_COLONY));
            colonyHint.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_BLUE));
            tooltip.add(colonyHint);
        }

        final MutableComponent guiHint = Component.translatable(TranslationConstants.COM_MINECOLONIES_CORE_COLONY_SIGN_TOOLTIP);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN));
        tooltip.add(guiHint);

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
