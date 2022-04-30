package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

public class ItemArcheologistLoot extends AbstractItemMinecolonies
{

    private final Type type;
    private final DegradationLevel degradationLevel;
    private final StructureFeature<?> structureFeature;

    public ItemArcheologistLoot(
      final Properties properties,
      final Type type,
      final DegradationLevel degradationLevel,
      final StructureFeature<?> specializedFor)
    {
        super(
          "archeologist_loot_%s_%s_%s_%s".formatted(type.name().toLowerCase(Locale.ROOT),
            degradationLevel.name().toLowerCase(Locale.ROOT),
            Objects.requireNonNull(specializedFor.getRegistryName()).getNamespace(),
            specializedFor.getRegistryName().getPath()), properties.stacksTo(1).tab(ModCreativeTabs.MINECOLONIES_ARCHEOLOGIST_LOOT));
        this.type = type;
        this.degradationLevel = degradationLevel;
        structureFeature = specializedFor;
    }

    @Override
    public @NotNull Component getName(final @NotNull ItemStack stack)
    {
        return new TranslatableComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_ITEM_ARCHEOLOGIST_LOOT_NAME,
          new TranslatableComponent(
            String.format(
              TranslationConstants.COM_MINECOLONIES_COREMOD_ITEM_ARCHEOLOGIST_LOOT_TYPE_FOR_STRUCTURE_FORMAT,
              getType().name().toLowerCase(Locale.ROOT),
              Objects.requireNonNull(getStructureFeature().getRegistryName()).toString().toLowerCase(Locale.ROOT).replace("_", ".").replace(":", ".")
            )
          ),
          new TranslatableComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_ITEM_ARCHEOLOGIST_LOOT_DAMAGE_PREFIX + getDegradationLevel().name().toLowerCase(Locale.ROOT))
          );
    }

    public Type getType()
    {
        return type;
    }

    public DegradationLevel getDegradationLevel()
    {
        return degradationLevel;
    }

    public StructureFeature<?> getStructureFeature()
    {
        return structureFeature;
    }

    public boolean isFoil(final @NotNull ItemStack stack)
    {
        return degradationLevel == DegradationLevel.NONE;
    }

    public enum DegradationLevel {
        NONE,
        SLIGHTLY,
        MODERATELY,
        SEVERELY,
        EXTREMELY
    }

    public enum Type {
        SMALL,
        LARGE,
        TABLET
    }
}

