package com.minecolonies.core.client.gui;

import com.ldtteam.structurize.client.gui.WindowExtendedBuildTool;
import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.DirectPlaceMessage;
import com.minecolonies.core.network.messages.server.SwitchBuildingWithToolMessage;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.ldtteam.structurize.api.constants.Constants.GROUNDSTYLE_RELATIVE;
import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_MISSING_BUILD_TOOL;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Move Building Window.
 */
public class WindowSuggestBuildTool extends AbstractWindowSkeleton
{
    /**
     * Position the player is trying to place at.
     */
    @NotNull
    private final BlockPos pos;

    /**
     * Building the worker is trying to place.
     */
    @NotNull
    private final BlockState building;

    /**
     * The stack in the players hand.
     */
    @NotNull
    private ItemStack stack;

    /**
     * Creates a window to suggest the user to use the buildtool.
     *
     * @param pos   the position of the placement.
     * @param state the block they are trying to place.
     * @param stack the stack to suggest it for.
     */
    public WindowSuggestBuildTool(@NotNull final BlockPos pos, @NotNull final BlockState state, @NotNull final ItemStack stack)
    {
        super(Constants.MOD_ID + SUGGEST_BUILDING_SOURCE_SUFFIX);
        this.pos = pos;
        this.building = state;
        this.stack = stack;
        registerButton(BUTTON_BUILDTOOL, this::buildToolClicked);
        registerButton(BUTTON_DIRECT, this::directClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
    }

    /**
     * Direct placement has been chosen.
     */
    private void directClicked()
    {
        new DirectPlaceMessage(building, pos, stack).sendToServer();
        close();
    }

    /**
     * Open buildtool GUI has been chosen.
     */
    private void buildToolClicked()
    {
        if (InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(Minecraft.getInstance().player.getInventory()), ModItems.buildTool.get()) != -1)
        {
            new SwitchBuildingWithToolMessage(stack).sendToServer();
            new WindowExtendedBuildTool(this.pos, GROUNDSTYLE_RELATIVE, mc.level.registryAccess()).open();
            return;
        }
        MessageUtils.format(WARNING_MISSING_BUILD_TOOL).sendTo(Minecraft.getInstance().player);
        close();
    }

    /**
     * Cancel the current structure.
     */
    private void cancelClicked()
    {
        close();
    }
}
