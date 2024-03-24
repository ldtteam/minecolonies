package com.minecolonies.core.compatibility.jei.transfer;

import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.core.client.gui.containers.WindowFurnaceCrafting;
import com.minecolonies.core.colony.buildings.moduleviews.CraftingModuleView;
import com.minecolonies.core.compatibility.jei.JobBasedRecipeCategory;
import com.minecolonies.core.network.messages.server.TransferRecipeCraftingTeachingMessage;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Implements a "show recipes" button on the furnace teaching window, and allows you to drag
 * individual ingredients directly from JEI to the teaching grid without using cheat mode.
 */
public class FurnaceCraftingGuiHandler extends AbstractTeachingGuiHandler<WindowFurnaceCrafting>
{
    public FurnaceCraftingGuiHandler(@NotNull final List<JobBasedRecipeCategory<?>> categories)
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
        final JobBasedRecipeCategory<?> category = getRecipeCategory(containerScreen.getBuildingView());
        if (category != null)
        {
            areas.add(IGuiClickableArea.createBasic(90, 34, 22, 17, category.getRecipeType()));
        }
        return areas;
    }

    @Override
    protected boolean isSupportedCraftingModule(@NotNull final CraftingModuleView moduleView)
    {
        return moduleView.canLearn(ModCraftingTypes.SMELTING.get());
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
        message.sendToServer();
    }
}
