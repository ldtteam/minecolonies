package com.minecolonies.core.items;

import com.minecolonies.api.items.ModArmorMaterials;
import com.minecolonies.core.client.render.worldevent.ColonyBlueprintRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemBuildGoggles extends ArmorItem
{
    /**
     * Constructor
     *
     * @param name            the name.
     * @param properties      the item properties.
     */
    public ItemBuildGoggles(final Item.Properties properties)
    {
        super(ModArmorMaterials.GOGGLES, Type.HELMET, properties.setNoRepair().rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack,
                                @Nullable final TooltipContext ctx,
                                @NotNull final List<Component> components,
                                @NotNull final TooltipFlag flags)
    {
        super.appendHoverText(stack, ctx, components, flags);

        components.add(Component.translatableEscape("\"%s\"",
                        Component.translatableEscape("item.minecolonies.build_goggles.lore")
                                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        components.add(Component.translatableEscape(ColonyBlueprintRenderer.willRenderBlueprints()
                ? "item.minecolonies.build_goggles.enabled" : "item.minecolonies.build_goggles.disabled")
                .withStyle(ChatFormatting.GRAY));
    }
}
