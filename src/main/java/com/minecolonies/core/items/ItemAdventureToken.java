package com.minecolonies.core.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ENTITY_TYPE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ADVENTURE_TOKEN_NAME_GUI;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ADVENTURE_TOKEN_TOOLTIP_GUI;

public class ItemAdventureToken extends AbstractItemMinecolonies
{
    /**
     * This item is purely for matching, and carrying data in Tags
     *
     * @param properties
     */
    public ItemAdventureToken(Properties properties)
    {
        super("adventure_token", properties);
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint = Component.translatable(COM_MINECOLONIES_COREMOD_ADVENTURE_TOKEN_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        tooltip.add(guiHint);

        tooltip.add(Component.literal(stack.getTag().getAsString()).withStyle(ChatFormatting.GRAY));

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public Component getName(ItemStack stack)
    {
        if (stack.hasTag() && stack.getTag().contains(TAG_ENTITY_TYPE))
        {
            EntityType<?> mobType = EntityType.byString(stack.getTag().getString(TAG_ENTITY_TYPE)).orElse(EntityType.ZOMBIE);
            return Component.translatable(COM_MINECOLONIES_COREMOD_ADVENTURE_TOKEN_NAME_GUI, mobType.getDescription());
        }

        return super.getName(stack);
    }
}