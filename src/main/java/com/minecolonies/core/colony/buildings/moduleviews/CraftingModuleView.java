package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.modules.WindowListRecipes;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.network.messages.server.colony.building.OpenCraftingGUIMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.MenuProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private Set<CraftingType> recipeTypeSet = new HashSet<>();

    /**
     * The list of recipes the worker knows, correspond to a subset of the recipes in the colony.
     */
    protected final List<IRecipeStorage> recipes = new ArrayList<>();

    /**
     * The list of disabled recipes.
     */
    protected final List<IRecipeStorage> disabledRecipes = new ArrayList<>();

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
            this.jobEntry = buf.readById(MinecoloniesAPIProxy.getInstance().getJobRegistry());
        }
        else
        {
            this.jobEntry = null;
        }

        recipeTypeSet.clear();
        final int size = buf.readVarInt();
        for (int i = 0; i < size; ++i)
        {
            final CraftingType type = buf.readById(MinecoloniesAPIProxy.getInstance().getCraftingTypeRegistry());
            if (type != null)
            {
                recipeTypeSet.add(type);
            }
        }

        recipes.clear();
        disabledRecipes.clear();

        final int recipesSize = buf.readInt();
        for (int i = 0; i < recipesSize; i++)
        {
            final IRecipeStorage storage = StandardFactoryController.getInstance().deserialize(buf.readNbt());
            if (storage != null)
            {
                recipes.add(storage);
            }
        }

        final int disabledRecipeSize = buf.readInt();
        for (int i = 0; i < disabledRecipeSize; i++)
        {
            final IRecipeStorage storage = StandardFactoryController.getInstance().deserialize(buf.readNbt());
            if (storage != null)
            {
                disabledRecipes.add(storage);
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
        return !recipeTypeSet.isEmpty();
    }

    /**
     * Check if the worker can learn a certain type of recipe.
     * @param type the type to check for.
     * @return true if so.
     */
    public boolean canLearn(final CraftingType type)
    {
        return getSupportedCraftingTypes().contains(type);
    }

    /**
     * Get the supported crafting types.
     * @return a set of types.
     */
    public Set<CraftingType> getSupportedCraftingTypes()
    {
        return recipeTypeSet;
    }

    /**
     * Unique id of the crafting module view.
     * @return the id.
     */
    @Deprecated
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
    public BOWindow getWindow()
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
    public void switchOrder(final int i, final int j, final boolean fullMove)
    {
        if (fullMove)
        {
            if (i > j)
            {
                recipes.add(0, recipes.remove(i));
            }
            else
            {
                recipes.add(recipes.remove(i));
            }
        }
        else if (i < recipes.size() && j < recipes.size() && i >= 0 && j >= 0)
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

    public void openCraftingGUI()
    {
        final BlockPos pos = buildingView.getPosition();
        Minecraft.getInstance().player.openMenu((MenuProvider) Minecraft.getInstance().level.getBlockEntity(pos));
        Network.getNetwork().sendToServer(new OpenCraftingGUIMessage((AbstractBuildingView) buildingView, this.getProducer().getRuntimeID()));
    }

    /**
     * Enable/disable a recipe.
     * @param row the location of the recipe.
     */
    public void toggle(final int row)
    {
        final IRecipeStorage storage = recipes.get(row);
        if (disabledRecipes.contains(storage))
        {
            disabledRecipes.remove(storage);
        }
        else
        {
            disabledRecipes.add(storage);
        }
    }

    /**
     * Check if a recipe is disabled.
     * @param recipe the recipe to check for.
     * @return true if so.
     */
    public boolean isDisabled(final IRecipeStorage recipe)
    {
        return disabledRecipes.contains(recipe);
    }
}
