package com.crafting;

import com.google.common.base.Objects;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * One bit of knowledge from the crafting system.
 */
public final class Knowledge
{
    @NotNull
    private final List<ItemStack> requirements;
    @NotNull
    private final ItemStack       output;

    /**
     * Try creating a new Knowledge
     *
     * @param craftingGrid the grid to try with
     * @param recipe       the recipe to try
     * @param world        the world to try in
     * @return a new Knowledge object
     *
     * @throws KnowledgeCreationException when it did not match
     */
    public static Knowledge tryCreate(@NotNull final InventoryCrafting craftingGrid, @NotNull final IRecipe recipe, @NotNull World world) throws KnowledgeCreationException
    {
        if (!recipe.matches(craftingGrid, world))
        {
            throw new KnowledgeCreationException("Recipe did not match!");
        }
        @Nullable final ItemStack recipeOutput = recipe.getRecipeOutput();
        if (recipeOutput == null)
        {
            throw new KnowledgeCreationException("Recipe did output null!");
        }
        return new Knowledge(InventoryUtils.getInventoryAsList(craftingGrid), recipeOutput);
    }

    private Knowledge(@NotNull final List<ItemStack> requirements, @NotNull final ItemStack output)
    {
        this.requirements = requirements;
        this.output = output;
    }

    public void readFromNBT(@NotNull NBTTagCompound compound)
    {

    }

    public void writeToNBT(@NotNull NBTTagCompound compound)
    {

    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final Knowledge knowledge = (Knowledge) o;
        return Objects.equal(requirements, knowledge.requirements) &&
                 Objects.equal(output, knowledge.output);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(output);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                 .add("recipe", recipe)
                 .add("requirements", requirements)
                 .add("output", output)
                 .toString();
    }

    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("recipe", this.recipe.);
        return null;
    }

    public void deserializeNBT(final NBTTagCompound nbt)
    {

    }
}
