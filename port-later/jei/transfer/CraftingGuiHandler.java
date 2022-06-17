package com.minecolonies.coremod.compatibility.jei.transfer;

import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.containers.WindowCrafting;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.coremod.compatibility.jei.JobBasedRecipeCategory;
import com.minecolonies.coremod.network.messages.server.TransferRecipeCraftingTeachingMessage;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Implements a "show recipes" button on the crafting teaching window, and allows you to drag
 * individual ingredients directly from JEI to the teaching grid without using cheat mode.
 */
public class CraftingGuiHandler extends AbstractTeachingGuiHandler<WindowCrafting>
{
    public CraftingGuiHandler(@NotNull final List<JobBasedRecipeCategory<?>> categories)
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
        final JobBasedRecipeCategory<?> category = getRecipeCategory(containerScreen.getBuildingView());
        if (category != null)
        {
            areas.add(IGuiClickableArea.createBasic(90, 34, 22, 17, category.getUid()));
        }
        return areas;
    }

    @Override
    protected boolean isSupportedCraftingModule(@NotNull final CraftingModuleView moduleView)
    {
        return moduleView.canLearn(ModCraftingTypes.SMALL_CRAFTING.get());
    }

    @Override
    protected boolean isSupportedSlot(@NotNull Slot slot)
    {
        return slot.container instanceof CraftingContainer;
    }

    @Override
    protected void updateServer(@NotNull final WindowCrafting gui)
    {
        final Map<Integer, ItemStack> matrix = new HashMap<>();
        final CraftingContainer inventory = gui.getMenu().getInv();
        if (gui.isCompleteCrafting())
        {
            for (int i = 0; i < 9; ++i)
            {
                matrix.put(i, inventory.getItem(i));
            }
        }
        else
        {
            matrix.put(0, inventory.getItem(0));
            matrix.put(1, inventory.getItem(1));
            matrix.put(3, inventory.getItem(2));
            matrix.put(4, inventory.getItem(3));
        }

        final TransferRecipeCraftingTeachingMessage message = new TransferRecipeCraftingTeachingMessage(matrix, gui.isCompleteCrafting());
        Network.getNetwork().sendToServer(message);
    }
}
