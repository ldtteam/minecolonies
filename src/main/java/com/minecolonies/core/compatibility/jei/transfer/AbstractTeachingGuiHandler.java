package com.minecolonies.core.compatibility.jei.transfer;

import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.core.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.core.compatibility.jei.JobBasedRecipeCategory;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Common base class for JEI teaching GUI extensions.
 */
public abstract class AbstractTeachingGuiHandler<W extends AbstractContainerScreen<?>> implements IGuiContainerHandler<W>, IGhostIngredientHandler<W>
{
    @NotNull
    private final Map<ResourceLocation, JobBasedRecipeCategory<?>> categories;

    protected AbstractTeachingGuiHandler(@NotNull final List<JobBasedRecipeCategory<?>> categories)
    {
        this.categories = categories.stream()
                .collect(Collectors.toMap(category -> category.getRecipeType().getUid(), Function.identity()));
    }

    public void register(@NotNull final IGuiHandlerRegistration registration)
    {
        registration.addGuiContainerHandler(getWindowClass(), this);
        registration.addGhostIngredientHandler(getWindowClass(), this);
    }

    @NotNull protected abstract Class<W> getWindowClass();
    protected abstract boolean isSupportedCraftingModule(@NotNull CraftingModuleView moduleView);
    protected abstract boolean isSupportedSlot(@NotNull Slot slot);
    protected abstract void updateServer(@NotNull W gui);

    @Nullable
    protected JobBasedRecipeCategory<?> getRecipeCategory(@NotNull final AbstractBuildingView view)
    {
        for (final CraftingModuleView moduleView : view.getModuleViews(CraftingModuleView.class))
        {
            if (!isSupportedCraftingModule(moduleView)) continue;

            final JobEntry jobEntry = moduleView.getJobEntry();
            if (jobEntry != null)
            {
                final ResourceLocation uid = ICraftingBuildingModule.getUid(jobEntry, moduleView.getId());
                final JobBasedRecipeCategory<?> category = this.categories.get(uid);
                if (category != null)
                {
                    return category;
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public <I> List<Target<I>> getTargetsTyped(@NotNull final W gui,
                                               @NotNull final ITypedIngredient<I> ingredient,
                                               final boolean doStart)
    {
        final List<Target<I>> targets = new ArrayList<>();
        if (ingredient.getType().getIngredientClass() == ItemStack.class)
        {
            for (final Slot slot : gui.getMenu().slots)
            {
                if (!slot.isActive() || !isSupportedSlot(slot)) continue;

                final Rect2i bounds = new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 17, 17);

                targets.add(new Target<I>()
                {
                    @NotNull
                    @Override
                    public Rect2i getArea()
                    {
                        return bounds;
                    }

                    @Override
                    public void accept(@NotNull I ingredient)
                    {
                        slot.set((ItemStack) ingredient);
                        updateServer(gui);
                    }
                });
            }
        }
        return targets;
    }

    @Override
    public void onComplete()
    {
    }
}
