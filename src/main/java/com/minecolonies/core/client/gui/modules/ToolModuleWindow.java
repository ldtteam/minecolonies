package com.minecolonies.core.client.gui.modules;

import com.ldtteam.blockui.controls.Text;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.ToolModuleView;
import com.minecolonies.core.network.messages.server.colony.building.GiveToolMessage;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.ForgeRegistries;

public class ToolModuleWindow extends AbstractModuleWindow
{
    /**
     * Id of the button to give tool
     */
    private static final String BUTTON_GIVE_TOOL = "giveTool";

    /**
     * The matching module view to the window.
     */
    private final ToolModuleView moduleView;

    /**
     * Constructor for the minimum stock window view.
     *
     * @param building class extending
     * @param moduleView the module view.
     */
    public ToolModuleWindow(final String res, final IBuildingView building, final ToolModuleView moduleView)
    {
        super(building, res);

        this.moduleView = moduleView;

        findPaneOfTypeByID("desc", Text.class).setText(Component.translatable("com.minecolonies.coremod.gui.tooldesc." + ForgeRegistries.ITEMS.getKey(moduleView.getTool()).getPath()));
        registerButton(BUTTON_GIVE_TOOL, this::givePlayerScepter);
    }

    /**
     * Send message to player to add scepter to his inventory.
     */
    private void givePlayerScepter()
    {
        Network.getNetwork().sendToServer(new GiveToolMessage(buildingView, moduleView.getTool()));
    }
}
