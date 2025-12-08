package com.minecolonies.core.client.gui.modules.building;

import com.ldtteam.blockui.controls.Text;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractModuleWindow;
import com.minecolonies.core.colony.buildings.moduleviews.ToolModuleView;
import com.minecolonies.core.network.messages.server.colony.building.GiveToolMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ToolModuleWindow extends AbstractModuleWindow<ToolModuleView>
{
    /**
     * ID of the button to give tool
     */
    private static final String BUTTON_GIVE_TOOL = "giveTool";

    /**
     * Constructor for the minimum stock window view.
     *
     * @param moduleView the module view.
     */
    public ToolModuleWindow(final ToolModuleView moduleView)
    {
        super(moduleView,  new ResourceLocation(Constants.MOD_ID, "gui/layouthuts/layouttool.xml"));

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
