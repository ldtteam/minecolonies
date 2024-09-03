package com.minecolonies.api.items.registry;

import com.minecolonies.api.items.ModToolTypes;
import com.minecolonies.api.util.constant.IToolType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;
import java.util.function.Predicate;

public class ToolTypeEntry implements IToolType {
    private final String name;
    private final boolean variableMaterials;
    private final Component displayName;
    private final Predicate<ItemStack> isTool;
    private final Function<ItemStack, Integer> itemLevel;

    private ToolTypeEntry(final String name, final boolean variableMaterials, final Component displayName, final Predicate<ItemStack> isTool, final Function<ItemStack, Integer> itemLevel) {
        this.name = name;
        this.variableMaterials = variableMaterials;
        this.displayName = displayName;
        this.isTool = isTool;
        this.itemLevel = itemLevel;
    }

    public String getName() {
        return name;
    }

    public boolean hasVariableMaterials() {
        return variableMaterials;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public Boolean checkIsTool(ItemStack itemStack) {
        return isTool.test(itemStack);
    }

    public int getMiningLevel(ItemStack itemStack) {
        return isTool.test(itemStack) ? itemLevel.apply(itemStack) : -1;
    }

    public static class Builder {
        private String name;
        private boolean variableMaterials;
        private Component displayName;
        private Predicate<ItemStack> isTool;
        private Function<ItemStack, Integer> itemLevel;

        public Builder setName(final String name) {
            this.name = name;
            return this;
        }
        public Builder setVariableMaterials(final boolean variableMaterials) {
            this.variableMaterials = variableMaterials;
            return this;
        }
        public Builder setDisplayName(final Component displayName) {
            this.displayName = displayName;
            return this;
        }
        public Builder setIsTool(final Predicate<ItemStack> isTool) {
            this.isTool = isTool;
            return this;
        }
        public Builder setToolLevel(final Function<ItemStack, Integer> itemLevel) {
            this.itemLevel = itemLevel;
            return this;
        }

        public ToolTypeEntry build() {
            return new ToolTypeEntry(name, variableMaterials, displayName, isTool, itemLevel);
        }
    }

    public static IToolType getToolType(final String tool)
    {
        for (ResourceLocation resourceLocation : ModToolTypes.toolTypes) {
            ToolTypeEntry toolType = ModToolTypes.getToolType(resourceLocation);
            if (toolType.getName().equals(tool)) {
                return toolType;
            }
        }

        return ModToolTypes.none.get();
    }
}
