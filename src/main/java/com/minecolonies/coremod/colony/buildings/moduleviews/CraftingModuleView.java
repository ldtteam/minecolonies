package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.modules.WindowListRecipes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Client side representation of the crafting module.
 */
public class CraftingModuleView extends AbstractBuildingModuleView
{
    /**
     * The id of this crafting module view.
     */
    private String id;

    /**
     * Job entry.
     */
    private JobEntry jobEntry;

    /**
     * Different flags.
     */
    private boolean canLearnCraftingRecipes;
    private boolean canLearnFurnaceRecipes;
    private boolean canLearnLargeRecipes;

    /**
     * The list of recipes the worker knows, correspond to a subset of the recipes in the colony.
     */
    protected final List<IRecipeStorage> recipes = new ArrayList<>();

    /**
     * The max recipes.
     */
    private int maxRecipes;

    /**
     * Check if the page should be displayed.
     */
    private boolean isVisible = false;

    @Override
    public void deserialize(@NotNull FriendlyByteBuf buf)
    {
        if (buf.readBoolean())
        {
            this.jobEntry = buf.readRegistryIdSafe(JobEntry.class);
        }
        else
        {
            this.jobEntry = null;
        }

        this.canLearnCraftingRecipes = buf.readBoolean();
        this.canLearnFurnaceRecipes = buf.readBoolean();
        this.canLearnLargeRecipes = buf.readBoolean();

        recipes.clear();

        final int recipesSize = buf.readInt();
        for (int i = 0; i < recipesSize; i++)
        {
            final IRecipeStorage storage = StandardFactoryController.getInstance().deserialize(buf.readNbt());
            if (storage != null)
            {
                recipes.add(storage);
            }
        }
        this.maxRecipes = buf.readInt();
        this.id = buf.readUtf(32767);
        this.isVisible = buf.readBoolean();
    }

    /**
     * Gets the job associated with this crafting module.
     * @return The job, or null if there was no such job.
     */
    @Nullable
    public JobEntry getJobEntry()
    {
        return this.jobEntry;
    }

    /**
     * Check if recipes can be taught.
     * @return true if so.
     */
    public boolean isRecipeAlterationAllowed()
    {
        return canLearnCraftingRecipes || canLearnFurnaceRecipes;
    }

    /** True if this module can be taught crafting recipes. */
    public boolean canLearnCraftingRecipes() { return this.canLearnCraftingRecipes; }
    /** True if this module can be taught smelting recipes. */
    public boolean canLearnFurnaceRecipes() { return this.canLearnFurnaceRecipes; }
    /** True if this module can be taught 3x3 crafting recipes. */
    public boolean canLearnLargeRecipes() { return this.canLearnLargeRecipes; }

    /**
     * Unique id of the crafting module view.
     * @return the id.
     */
    public String getId()
    {
        return this.id;
    }

    @Override
    public boolean isPageVisible()
    {
        return isVisible;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Window getWindow()
    {
        return new WindowListRecipes(buildingView, Constants.MOD_ID + ":gui/layouthuts/layoutlistrecipes.xml", this);
    }

    @Override
    public String getIcon()
    {
        return id;
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.workerhuts.recipe." + id ;
    }

    /**
     * Get a list of all recipes.
     * @return the list.
     */
    public List<IRecipeStorage> getRecipes()
    {
        return recipes;
    }

    /**
     * Remove the recipe at index.
     * @param index the index to remove.
     */
    public void removeRecipe(int index)
    {
        recipes.remove(index);
    }

    /**
     * Switch order in recipe list.
     * @param i first index.
     * @param j second index.
     */
    public void switchOrder(final int i, final int j)
    {
        if (i < recipes.size() && j < recipes.size() && i >= 0 && j >= 0)
        {
            final IRecipeStorage storage = recipes.get(i);
            recipes.set(i, recipes.get(j));
            recipes.set(j, storage);
        }
    }

    public int getMaxRecipes()
    {
        return maxRecipes;
    }
}
