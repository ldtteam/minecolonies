package com.minecolonies.core.items;

import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A custom item class for bowl food items.
 */
public class ItemBowlFood extends Item implements IMinecoloniesFoodItem
{
    /**
     * The food tier.
     */
    private final int tier;

    /**
     * Creates a new food item.
     *
     * @param builder the item properties to use.
     * @param tier the nutrition tier.
     */
    public ItemBowlFood(@NotNull final Properties builder, final int tier)
    {
        super(builder);
        this.tier = tier;
    }

    @NotNull
    @Override
    public ItemStack finishUsingItem(@NotNull final ItemStack stack, @NotNull final Level level, @NotNull final LivingEntity entity)
    {
        // implementation of this is deliberately similar to HoneyBottleItem; in particular it only
        // gives extra drops to Players because citizens eating food are dealt with by the caller.

        final ItemStack bowl = new ItemStack(Items.BOWL);

        final ItemStack remainder = super.finishUsingItem(stack, level, entity);
        if (ItemStackUtils.isEmpty(remainder))
        {
            return bowl;
        }

        if (entity instanceof final Player player && !player.getAbilities().instabuild)
        {
            if (!player.getInventory().add(bowl))
            {
                player.drop(bowl, false);
            }
        }

        return stack;
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        tooltip.add(Component.translatable(TranslationConstants.TIER_TOOLTIP + this.tier));
    }

    @Override
    public int getTier()
    {
        return tier;
    }
}
