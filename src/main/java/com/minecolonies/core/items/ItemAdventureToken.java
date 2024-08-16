package com.minecolonies.core.items;

import com.minecolonies.api.items.ModDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.UnaryOperator;

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
        super("adventure_token", properties);
    }

    @Override
    public Component getName(ItemStack stack)
    {
        final @Nullable AdventureData component = AdventureData.readFromItemStack(stack);
        if (component != null)
        {
            return Component.translatableEscape(COM_MINECOLONIES_COREMOD_ADVENTURE_TOKEN_NAME_GUI, component.entityType.getDescription());
        }
        
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final TooltipContext ctx, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint = Component.translatableEscape(COM_MINECOLONIES_COREMOD_ADVENTURE_TOKEN_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        tooltip.add(guiHint);

        super.appendHoverText(stack, ctx, tooltip, flagIn);
    }

    public record AdventureData(EntityType<?> entityType, float damage, int xp)
    {
        public static final Codec<AdventureData> CODEC = RecordCodecBuilder.create(
          builder -> builder
                       .group(BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(AdventureData::entityType),
                         Codec.FLOAT.fieldOf("damage").forGetter(AdventureData::damage),
                         Codec.INT.fieldOf("xp").forGetter(AdventureData::xp))
                       .apply(builder, AdventureData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AdventureData> STREAM_CODEC =
          StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ENTITY_TYPE), AdventureData::entityType,
            ByteBufCodecs.FLOAT, AdventureData::damage,
            ByteBufCodecs.VAR_INT, AdventureData::xp,
            AdventureData::new);

        public void writeToItemStack(final ItemStack itemStack)
        {
            itemStack.set(ModDataComponents.ADVENTURE_COMPONENT, this);
        }

        @Nullable
        public static AdventureData readFromItemStack(final ItemStack itemStack)
        {
            return itemStack.get(ModDataComponents.ADVENTURE_COMPONENT);
        }

        public static void updateItemStack(final ItemStack itemStack, final UnaryOperator<AdventureData> updater)
        {
            updater.apply(readFromItemStack(itemStack)).writeToItemStack(itemStack);
        }
    }
}
