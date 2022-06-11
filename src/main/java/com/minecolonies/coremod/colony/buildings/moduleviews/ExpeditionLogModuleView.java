package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.coremod.client.gui.modules.ExpeditionLogModuleWindow;
import com.minecolonies.coremod.colony.buildings.modules.expedition.ExpeditionLog;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Building module view to display an expedition log
 */
public class ExpeditionLogModuleView extends AbstractBuildingModuleView
{
    private boolean updated;
    private boolean unlocked;
    private ExpeditionLog log = new ExpeditionLog();

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        this.unlocked = buf.readBoolean();
        if (this.unlocked)
        {
            this.log.deserialize(buf);
        }
        this.updated = true;
    }

    public boolean checkAndResetUpdated()
    {
        final boolean wasUpdated = this.updated;
        this.updated = false;
        return wasUpdated;
    }

    public ExpeditionLog getLog()
    {
        return this.log;
    }

    @Override
    public boolean isPageVisible()
    {
        return this.unlocked;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Window getWindow()
    {
        return new ExpeditionLogModuleWindow(getBuildingView(), this);
    }

    @Override
    public String getIcon()
    {
        return "sword";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.gui.workerhuts.expeditionlog";
    }
}
