package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
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
     * @param properties
     */
    public ItemAdventureToken(Properties properties)
    {
        super("adventure_token", properties.tab(ModCreativeTabs.MINECOLONIES));
    }

    @Override
    public ITextComponent getName(ItemStack stack) {
        if(stack.hasTag() && stack.getTag().contains(TAG_ENTITY_TYPE))
        {
            EntityType<?> mobType = EntityType.byString(stack.getTag().getString(TAG_ENTITY_TYPE)).orElse(EntityType.ZOMBIE);
            return new TranslationTextComponent(COM_MINECOLONIES_COREMOD_ADVENTURE_TOKEN_NAME_GUI, mobType.getDescription());
        }
        
        return super.getName(stack);
    }


    @Override
    public void appendHoverText(
    @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final IFormattableTextComponent guiHint = new TranslationTextComponent(COM_MINECOLONIES_COREMOD_ADVENTURE_TOKEN_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(TextFormatting.GRAY));
        tooltip.add(guiHint);

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}