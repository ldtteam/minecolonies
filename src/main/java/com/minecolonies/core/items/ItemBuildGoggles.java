package com.minecolonies.core.items;
import com.minecolonies.core.client.render.worldevent.ColonyBlueprintRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import static com.minecolonies.apiimp.initializer.ModItemsInitializer.GOGGLES;
public class ItemBuildGoggles extends Item
{
    public ItemBuildGoggles(
            @NotNull final String name,
            final Item.Properties properties)
    {
        super(properties.humanoidArmor(GOGGLES, ArmorType.HELMET).rarity(Rarity.UNCOMMON));
    }
    @Override
    public void appendHoverText(@NotNull final ItemStack stack,
                                @Nullable final TooltipContext ctx,
                                @NotNull final TooltipDisplay display, Consumer<Component> tooltipConsumer,
                                @NotNull final TooltipFlag flags)
    
    {
        final List<Component> tooltip = new ArrayList<>();
        super.appendHoverText(stack, ctx, display, tooltipConsumer, flags);
        tooltip.add(Component.translatableEscape("\"%s\"",
                        Component.translatableEscape("item.minecolonies.build_goggles.lore")
                                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC))
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatableEscape(ColonyBlueprintRenderer.willRenderBlueprints()
                ? "item.minecolonies.build_goggles.enabled" : "item.minecolonies.build_goggles.disabled")
                .withStyle(ChatFormatting.GRAY));
        tooltip.forEach(tooltipConsumer);
    }
}
