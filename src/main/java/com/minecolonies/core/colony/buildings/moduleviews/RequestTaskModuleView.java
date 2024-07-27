package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.modules.WindowHutRequestTaskModule;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Request task module to display tasks in the UI.
 */
public abstract class RequestTaskModuleView extends AbstractBuildingModuleView
{
    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BOWindow getWindow()
    {
        return new WindowHutRequestTaskModule(buildingView,Constants.MOD_ID + ":gui/layouthuts/layouttasklist.xml");
    }

    @Override
    public String getIcon()
    {
        return "info";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.workerhuts.crafter.tasks";
    }

    /**
     * Get the specific task list.
     * @return the task list.
     */
    public abstract List<IToken<?>> getTasks();
}
