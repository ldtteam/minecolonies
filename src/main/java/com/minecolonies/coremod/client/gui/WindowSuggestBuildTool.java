package com.minecolonies.coremod.client.gui;

import com.ldtteam.structurize.items.ModItems;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.DirectPlaceMessage;
import com.minecolonies.coremod.network.messages.server.SwitchBuildingWithToolMessage;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import static com.ldtteam.structurize.api.util.constant.Constants.GROUNDSTYLE_RELATIVE;
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
     * @param state the block he is trying to place.
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
        Network.getNetwork().sendToServer(new DirectPlaceMessage(building, pos, stack));
        close();
    }

    /**
     * Open buildtool GUI has been chosen.
     */
    private void buildToolClicked()
    {
        if (InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(Minecraft.getInstance().player.inventory), ModItems.buildTool.get()) != -1)
        {
            Network.getNetwork().sendToServer(new SwitchBuildingWithToolMessage(stack));
            new WindowMinecoloniesBuildTool(this.pos, GROUNDSTYLE_RELATIVE).open();
            return;
        }
        LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, "item.buildtool.missing");
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
