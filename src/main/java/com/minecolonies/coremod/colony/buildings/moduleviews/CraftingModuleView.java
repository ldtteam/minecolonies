package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Client side representation of the crafting module.
 */
public class CraftingModuleView extends AbstractBuildingModuleView
{
    private JobEntry jobEntry;
    private boolean canLearnCraftingRecipes;
    private boolean canLearnFurnaceRecipes;
    private boolean canLearnLargeRecipes;

    @Override
    public void deserialize(@NotNull PacketBuffer buf)
    {
        if (buf.readBoolean())
        {
            this.jobEntry = buf.readRegistryIdSafe(JobEntry.class);
        }
        else
        {
            this.jobEntry = null;
        }

        this.canLearnCraftingRecipes = buf.readBoolean();
        this.canLearnFurnaceRecipes = buf.readBoolean();
        this.canLearnLargeRecipes = buf.readBoolean();
    }

    /**
     * Gets the job associated with this crafting module.
     * @return The job, or null if there was no such job.
     */
    @Nullable
    public JobEntry getJobEntry()
    {
        return this.jobEntry;
    }

    /** True if this module can be taught crafting recipes. */
    public boolean canLearnCraftingRecipes() { return this.canLearnCraftingRecipes; }
    /** True if this module can be taught smelting recipes. */
    public boolean canLearnFurnaceRecipes() { return this.canLearnFurnaceRecipes; }
    /** True if this module can be taught 3x3 crafting recipes. */
    public boolean canLearnLargeRecipes() { return this.canLearnLargeRecipes; }

    @Override
    public boolean isPageVisible()
    {
        return false;   // todo this later
    }

    @Override
    public Window getWindow()
    {
        return null;    // todo this later
    }

    @Override
    public String getIcon()
    {
        return "crafting";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.workerhuts.recipe";
    }
}
