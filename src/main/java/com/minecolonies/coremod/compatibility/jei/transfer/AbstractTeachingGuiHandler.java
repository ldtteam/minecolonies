package com.minecolonies.coremod.compatibility.jei.transfer;

import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.coremod.compatibility.jei.GenericRecipeCategory;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler.Target;

/**
 * Common base class for JEI teaching GUI extensions.
 */
public abstract class AbstractTeachingGuiHandler<W extends ContainerScreen<?>>
        implements IGuiContainerHandler<W>, IGhostIngredientHandler<W>
{
    @NotNull
    private final List<GenericRecipeCategory> categories;

    protected AbstractTeachingGuiHandler(@NotNull final List<GenericRecipeCategory> categories)
    {
        this.categories = categories;
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
    protected GenericRecipeCategory getRecipeCategory(@NotNull final AbstractBuildingWorker.View view)
    {
        for (final CraftingModuleView moduleView : view.getModuleViews(CraftingModuleView.class))
        {
            if (!isSupportedCraftingModule(moduleView)) continue;

            final JobEntry jobEntry = moduleView.getJobEntry();
            if (jobEntry != null)
            {
                for (GenericRecipeCategory category : this.categories)
                {
                    if (category.getJob().getJobRegistryEntry().getRegistryName().equals(jobEntry.getRegistryName()))
                    {
                        return category;
                    }
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public <I> List<Target<I>> getTargets(@NotNull final W gui,
                                          @NotNull final I ingredient,
                                          final boolean doStart)
    {
        final List<Target<I>> targets = new ArrayList<>();
        if (ingredient instanceof ItemStack)
        {
            for (final Slot slot : gui.getMenu().slots)
            {
                if (!slot.isActive() || !isSupportedSlot(slot)) continue;

                final Rectangle2d bounds = new Rectangle2d(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 17, 17);

                targets.add(new Target<I>()
                {
                    @NotNull
                    @Override
                    public Rectangle2d getArea()
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
