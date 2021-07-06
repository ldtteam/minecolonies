package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.tileentities.TileEntityColonyBuilding;
import com.minecolonies.api.util.SoundUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

import net.minecraft.item.Item.Properties;

/**
 * Magic scroll which highlights and speedbuffs workers of the building it is applied to
 */
public class ItemScrollHighlight extends AbstractItemScroll
{
    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param properties the properties.
     */
    public ItemScrollHighlight(final Properties properties)
    {
        super("scroll_highlight", properties);
    }

    @Override
    @NotNull
    public ActionResultType useOn(ItemUseContext ctx)
    {
        // Right click on block
        if (ctx.getLevel().isClientSide || ctx.getPlayer() == null || !ctx.getPlayer().isShiftKeyDown())
        {
            return ActionResultType.PASS;
        }

        final TileEntity te = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
        if (te instanceof TileEntityColonyBuilding)
        {
            ctx.getItemInHand().shrink(1);

            if (ctx.getLevel().random.nextInt(10) == 0)
            {
                ctx.getPlayer()
                  .displayClientMessage(new TranslationTextComponent(
                    "minecolonies.scroll.failed" + (ctx.getLevel().random.nextInt(FAIL_RESPONSES_TOTAL) + 1)).setStyle(Style.EMPTY.withColor(
                    TextFormatting.GOLD)), true);
                ctx.getPlayer().addEffect(new EffectInstance(Effects.GLOWING, TICKS_SECOND * 300));
                SoundUtils.playSoundForPlayer((ServerPlayerEntity) ctx.getPlayer(), SoundEvents.ENDER_CHEST_OPEN, 0.3f, 1.0f);
                return ActionResultType.SUCCESS;
            }

            final TileEntityColonyBuilding building = (TileEntityColonyBuilding) te;
            final List<ICitizenData> citizens = building.getColony().getBuildingManager().getBuilding(ctx.getClickedPos()).getAssignedCitizen();

            for (final ICitizenData citizenData : citizens)
            {
                if (citizenData.getEntity().isPresent())
                {
                    citizenData.getEntity().get().addEffect(new EffectInstance(Effects.GLOWING, TICKS_SECOND * 120));
                    citizenData.getEntity().get().addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, TICKS_SECOND * 120));
                }
            }

            SoundUtils.playSoundForPlayer((ServerPlayerEntity) ctx.getPlayer(), SoundEvents.PLAYER_LEVELUP, 0.3f, 1.0f);
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    protected boolean needsColony()
    {
        return false;
    }

    @Override
    protected ItemStack onItemUseSuccess(final ItemStack itemStack, final World world, final ServerPlayerEntity player)
    {
        return itemStack;
    }

    @Override
    public void appendHoverText(
      @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final IFormattableTextComponent guiHint = LanguageHandler.buildChatComponent("item.minecolonies.scroll_highlight.tip");
        guiHint.setStyle(Style.EMPTY.withColor(TextFormatting.DARK_GREEN));
        tooltip.add(guiHint);
    }
}
