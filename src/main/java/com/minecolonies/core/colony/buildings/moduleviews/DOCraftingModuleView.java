package com.minecolonies.core.colony.buildings.moduleviews;

import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.core.client.gui.modules.DOCraftingWindow;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Client side representation of the DO architects cutter crafting module.
 */
public class DOCraftingModuleView extends CraftingModuleView
{
    private final Supplier<OptionalPredicate<ItemStack>> validator;

    public DOCraftingModuleView(@NotNull final Supplier<OptionalPredicate<ItemStack>> validator)
    {
        this.validator = validator;
    }

    @Override
    public void openCraftingGUI()
    {
        new DOCraftingWindow(buildingView, this).open();
    }

    /**
     * Gets the ingredient validator for this module.
     * @return a predicate that reports whether the given ingredient is valid for this module.
     */
    public @NotNull OptionalPredicate<ItemStack> getIngredientValidator()
    {
        return this.validator.get();
    }
}
