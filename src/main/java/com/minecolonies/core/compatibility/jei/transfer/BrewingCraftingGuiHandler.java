package com.minecolonies.core.compatibility.jei.transfer;

import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.inventory.container.ContainerCraftingBrewingstand;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.containers.WindowBrewingstandCrafting;
import com.minecolonies.core.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.core.compatibility.jei.JobBasedRecipeCategory;
import com.minecolonies.core.network.messages.server.TransferRecipeCraftingTeachingMessage;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Implements a "show recipes" button on the brewing teaching window, and allows you to drag
 * individual ingredients directly from JEI to the teaching grid without using cheat mode.
 */
public class BrewingCraftingGuiHandler extends AbstractTeachingGuiHandler<WindowBrewingstandCrafting>
{
    public BrewingCraftingGuiHandler(@NotNull final List<JobBasedRecipeCategory<?>> categories)
    {
        super(categories);
    }

    @NotNull
    @Override
    protected Class<WindowBrewingstandCrafting> getWindowClass()
    {
        return WindowBrewingstandCrafting.class;
    }

    @NotNull
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull final WindowBrewingstandCrafting containerScreen,
                                                              final double mouseX,
                                                              final double mouseY)
    {
        final List<IGuiClickableArea> areas = new ArrayList<>();
        final JobBasedRecipeCategory<?> category = getRecipeCategory(containerScreen.getBuildingView());
        if (category != null)
        {
            areas.add(IGuiClickableArea.createBasic(34, 15, 44, 34, category.getRecipeType()));
        }
        return areas;
    }

    @Override
    protected boolean isSupportedCraftingModule(@NotNull final CraftingModuleView moduleView)
    {
        return moduleView.canLearn(ModCraftingTypes.BREWING.get());
    }

    @Override
    protected boolean isSupportedSlot(@NotNull Slot slot)
    {
        return slot.index >= 0 && slot.index <= 3;
    }

    @Override
    protected void updateServer(@NotNull final WindowBrewingstandCrafting gui)
    {
        final Map<Integer, ItemStack> matrix = new HashMap<>();
        final ContainerCraftingBrewingstand inventory = gui.getMenu();

        matrix.put(0, inventory.getSlot(0).getItem());
        for (int slot = 1; slot <= 3; ++slot)
        {
            final ItemStack container = inventory.getSlot(1).getItem();
            if (!container.isEmpty())
            {
                matrix.put(1, container);
                break;
            }
        }

        final TransferRecipeCraftingTeachingMessage message = new TransferRecipeCraftingTeachingMessage(matrix, false);
        Network.getNetwork().sendToServer(message);
    }
}
