package com.minecolonies.coremod.compatibility.jei.transfer;

import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.containers.WindowCrafting;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.coremod.compatibility.jei.GenericRecipeCategory;
import com.minecolonies.coremod.network.messages.server.TransferRecipeCraftingTeachingMessage;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Implements a "show recipes" button on the crafting teaching window, and allows you to drag
 * individual ingredients directly from JEI to the teaching grid without using cheat mode.
 */
public class CraftingGuiHandler extends AbstractTeachingGuiHandler<WindowCrafting>
{
    public CraftingGuiHandler(@NotNull final List<GenericRecipeCategory> categories)
    {
        super(categories);
    }

    @NotNull
    @Override
    protected Class<WindowCrafting> getWindowClass()
    {
        return WindowCrafting.class;
    }

    @NotNull
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull final WindowCrafting containerScreen,
                                                              final double mouseX,
                                                              final double mouseY)
    {
        final List<IGuiClickableArea> areas = new ArrayList<>();
        final GenericRecipeCategory category = getRecipeCategory(containerScreen.getBuildingView());
        if (category != null)
        {
            areas.add(IGuiClickableArea.createBasic(90, 34, 22, 17, category.getUid()));
        }
        return areas;
    }

    @Override
    protected boolean isSupportedCraftingModule(@NotNull final CraftingModuleView moduleView)
    {
        return moduleView.canLearnCraftingRecipes();
    }

    @Override
    protected boolean isSupportedSlot(@NotNull Slot slot)
    {
        return slot.inventory instanceof CraftingInventory;
    }

    @Override
    protected void updateServer(@NotNull final WindowCrafting gui)
    {
        final Map<Integer, ItemStack> matrix = new HashMap<>();
        final CraftingInventory inventory = gui.getContainer().getInv();
        if (gui.isCompleteCrafting())
        {
            for (int i = 0; i < 9; ++i)
            {
                matrix.put(i, inventory.getStackInSlot(i));
            }
        }
        else
        {
            matrix.put(0, inventory.getStackInSlot(0));
            matrix.put(1, inventory.getStackInSlot(1));
            matrix.put(3, inventory.getStackInSlot(2));
            matrix.put(4, inventory.getStackInSlot(3));
        }

        final TransferRecipeCraftingTeachingMessage message = new TransferRecipeCraftingTeachingMessage(matrix, gui.isCompleteCrafting());
        Network.getNetwork().sendToServer(message);
    }
}
