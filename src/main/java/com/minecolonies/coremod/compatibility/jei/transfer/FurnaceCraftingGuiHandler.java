package com.minecolonies.coremod.compatibility.jei.transfer;

import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.containers.WindowFurnaceCrafting;
import com.minecolonies.coremod.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.coremod.compatibility.jei.GenericRecipeCategory;
import com.minecolonies.coremod.network.messages.server.TransferRecipeCraftingTeachingMessage;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Implements a "show recipes" button on the furnace teaching window, and allows you to drag
 * individual ingredients directly from JEI to the teaching grid without using cheat mode.
 */
public class FurnaceCraftingGuiHandler extends AbstractTeachingGuiHandler<WindowFurnaceCrafting>
{
    public FurnaceCraftingGuiHandler(@NotNull final List<GenericRecipeCategory> categories)
    {
        super(categories);
    }

    @NotNull
    @Override
    protected Class<WindowFurnaceCrafting> getWindowClass()
    {
        return WindowFurnaceCrafting.class;
    }

    @NotNull
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(@NotNull final WindowFurnaceCrafting containerScreen,
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
        return moduleView.canLearnFurnaceRecipes();
    }

    @Override
    protected boolean isSupportedSlot(@NotNull Slot slot)
    {
        return slot.getSlotIndex() == 0;
    }

    @Override
    protected void updateServer(@NotNull final WindowFurnaceCrafting gui)
    {
        final Map<Integer, ItemStack> matrix = new HashMap<>();
        matrix.put(0, gui.getMenu().getSlot(0).getItem());

        final TransferRecipeCraftingTeachingMessage message = new TransferRecipeCraftingTeachingMessage(matrix, false);
        Network.getNetwork().sendToServer(message);
    }
}
