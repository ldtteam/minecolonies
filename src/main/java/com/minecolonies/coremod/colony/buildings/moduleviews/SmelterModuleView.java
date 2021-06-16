package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.crafting.IRecipeStorage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Client side representation of the crafting module.
 */
public class SmelterModuleView extends AbstractBuildingModuleView
{
    /**
     * The id of this crafting module view.
     */
    private String id;

    /**
     * Job entry.
     */
    private JobEntry jobEntry;

    /**
     * Different flags.
     */
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

        final int recipesSize = buf.readInt();
        for (int i = 0; i < recipesSize; i++)
        {
            final IRecipeStorage storage = StandardFactoryController.getInstance().deserialize(buf.readCompoundTag());
        }
        buf.readInt();
        this.id = buf.readString(32767);
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

    /**
     * Check if recipes can be taught.
     * @return true if so.
     */
    public boolean isRecipeAlterationAllowed()
    {
        return canLearnCraftingRecipes || canLearnFurnaceRecipes;
    }

    /** True if this module can be taught crafting recipes. */
    public boolean canLearnCraftingRecipes() { return this.canLearnCraftingRecipes; }
    /** True if this module can be taught smelting recipes. */
    public boolean canLearnFurnaceRecipes() { return this.canLearnFurnaceRecipes; }
    /** True if this module can be taught 3x3 crafting recipes. */
    public boolean canLearnLargeRecipes() { return this.canLearnLargeRecipes; }

    /**
     * Unique id of the crafting module view.
     * @return the id.
     */
    public String getId()
    {
        return this.id;
    }

    @Override
    public boolean isPageVisible()
    {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Window getWindow()
    {
        return null;
    }

    @Override
    public String getIcon()
    {
        return id;
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.workerhuts.recipe";
    }
}
