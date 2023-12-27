package com.minecolonies.coremod.items;

import com.minecolonies.coremod.client.render.worldevent.ColonyBlueprintRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemBuildGoggles extends ArmorItem
{
    public static final ArmorMaterial GOGGLES =
            new MineColoniesArmorMaterial("minecolonies:build_goggles", 2, new int[] {0, 0, 0, 0}, 20, SoundEvents.ARMOR_EQUIP_LEATHER, 0F, Ingredient.EMPTY);

    /**
     * Constructor
     *
     * @param name            the name.
     * @param properties      the item properties.
     */
    public ItemBuildGoggles(
            @NotNull final String name,
            final Item.Properties properties)
    {
        super(GOGGLES, Type.HELMET, properties.setNoRepair().rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack,
                                @Nullable final Level world,
                                @NotNull final List<Component> components,
                                @NotNull final TooltipFlag flags)
    {
        super.appendHoverText(stack, world, components, flags);

        components.add(Component.translatable("\"%s\"",
                        Component.translatable("item.minecolonies.build_goggles.lore")
                                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        components.add(Component.translatable(ColonyBlueprintRenderer.willRenderBlueprints()
                ? "item.minecolonies.build_goggles.enabled" : "item.minecolonies.build_goggles.disabled")
                .withStyle(ChatFormatting.GRAY));
    }
}
