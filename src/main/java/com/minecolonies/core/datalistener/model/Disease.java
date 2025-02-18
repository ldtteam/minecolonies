package com.minecolonies.core.datalistener.model;

import com.minecolonies.api.crafting.ItemStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A possible disease.
 *
 * @param id        the id of the disease.
 * @param name      the name of the disease.
 * @param rarity    the rarity of the disease.
 * @param cureItems the list of items needed to heal.
 */
public record Disease(ResourceLocation id, Component name, int rarity, List<ItemStorage> cureItems) implements WeightedEntry
{
    /**
     * Predicate for the different usages to check if inventory contains a cure.
     *
     * @param cure the expected cure item.
     * @return the predicate for checking if the cure exists.
     */
    public static Predicate<ItemStack> hasCureItem(final ItemStorage cure)
    {
        return stack -> isCureItem(stack, cure);
    }

    /**
     * Check if the given item is a cure item.
     *
     * @param stack the input stack.
     * @param cure  the cure item.
     * @return true if so.
     */
    public static boolean isCureItem(final ItemStack stack, final ItemStorage cure)
    {
        return Objects.equals(new ItemStorage(stack), cure);
    }

    /**
     * Get the cure string containing all items required for the cure.
     *
     * @return the cure string.
     */
    public Component getCureString()
    {
        final MutableComponent cureString = Component.literal("");
        for (int i = 0; i < cureItems.size(); i++)
        {
            final ItemStorage cureStack = cureItems.get(i);
            cureString.append(String.valueOf(cureStack.getItemStack().getCount())).append(" ").append(cureStack.getItemStack().getHoverName());
            if (i != cureItems.size() - 1)
            {
                cureString.append(" + ");
            }
        }
        return cureString;
    }

    @Override
    @NotNull
    public Weight getWeight()
    {
        return Weight.of(rarity);
    }
}
