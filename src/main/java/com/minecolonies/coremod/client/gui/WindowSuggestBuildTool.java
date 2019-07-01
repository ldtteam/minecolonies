package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.controls.Text;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.network.messages.DirectPlaceMessage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

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
    private final IBlockState building;

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
     */
    public WindowSuggestBuildTool(@NotNull final BlockPos pos, @NotNull final IBlockState state, @NotNull final ItemStack stack)
    {
        super(Constants.MOD_ID + SUGGEST_BUILDING_SOURCE_SUFFIX);
        this.pos = pos;
        this.building = state;
        this.stack = stack;
        registerButton(BUTTON_BUILDTOOL, this::buildToolClicked);
        registerButton(BUTTON_DIRECT, this::directClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        findPaneOfTypeByID("text", Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.placement.warning"));
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    /**
     * Direct placement has been chosen.
     */
    private void directClicked()
    {
        MineColonies.getNetwork().sendToServer(new DirectPlaceMessage(building, pos, stack));
        close();
    }

    /**
     * Open buildtool GUI has been chosen.
     */
    private void buildToolClicked()
    {
        new WindowMinecoloniesBuildTool(this.pos).open();
    }

    /**
     * Cancel the current structure.
     */
    private void cancelClicked()
    {
        close();
    }
}
