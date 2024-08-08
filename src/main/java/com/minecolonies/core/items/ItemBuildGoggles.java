package com.minecolonies.core.items;

import com.minecolonies.core.client.render.worldevent.ColonyBlueprintRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;

import static com.minecolonies.apiimp.initializer.ModItemsInitializer.DEFERRED_REGISTER;

public class ItemBuildGoggles extends ArmorItem
{
    public static final Holder<ArmorMaterial> GOGGLES = DEFERRED_REGISTER.register("build_goggles", () -> new ArmorMaterial(
      // Determines the defense value of this armor material, depending on what armor piece it is.
      Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
          map.put(ArmorItem.Type.BOOTS, 0);
          map.put(ArmorItem.Type.LEGGINGS, 0);
          map.put(ArmorItem.Type.CHESTPLATE, 0);
          map.put(ArmorItem.Type.HELMET, 0);
      }),
      20,
      SoundEvents.ARMOR_EQUIP_LEATHER,
      () -> Ingredient.EMPTY,
      List.of(),
      0,
      0
    ));

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
