package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.modules.WindowBuilderResModule;
import com.minecolonies.core.colony.buildings.utils.BuildingBuilderResource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BuildingResourcesModuleView extends AbstractBuildingModuleView
{
    /**
     * The resources they have to keep.
     */
    private final HashMap<String, BuildingBuilderResource> resources = new HashMap<>();

    /**
     * The building they are working on.
     */
    private int workOrderId;

    /**
     * Building progress.
     */
    private double progress;

    /**
     * Information on the phases.
     */
    private int finishedStages = 0;
    private int totalStages    = 1;

    @Override
    public void deserialize(@NotNull final RegistryFriendlyByteBuf buf)
    {
        final int size = buf.readInt();
        resources.clear();

        for (int i = 0; i < size; i++)
        {
            final ItemStack itemStack = Utils.deserializeCodecMess(buf);;
            final int amountAvailable = buf.readInt();
            final int amountNeeded = buf.readInt();
            final BuildingBuilderResource resource = new BuildingBuilderResource(itemStack, amountNeeded, amountAvailable);
            final int hashCode = itemStack.hasTag() ? itemStack.getTag().hashCode() : 0;
            final String key = itemStack.getDescriptionId() + "-" + hashCode;
            resources.put(key, resource);
        }

        workOrderId = buf.readInt();
        progress = buf.readDouble();
        totalStages = buf.readInt();
        finishedStages = buf.readInt();
    }

    /**
     * Get the work order id.
     *
     * @return a string describing it.
     */
    public int getWorkOrderId()
    {
        return workOrderId;
    }

    /**
     * Getter for the needed resources.
     *
     * @return a copy of the HashMap(String, Object).
     */

    public Map<String, BuildingBuilderResource> getResources()
    {
        return Collections.unmodifiableMap(resources);
    }

    /**
     * Get the building progress (relative to items used)
     *
     * @return the progress.
     */
    public int getProgress()
    {
        int localProgress = Math.max(100 - (int) (progress * 100), 0);
        if (finishedStages == 0)
        {
            if (totalStages == finishedStages)
            {
                return 0;
            }
        }
        return localProgress;
    }

    /**
     * Get the current stage status.
     * @return the stage.
     */
    public int getCurrentStage()
    {
        return finishedStages;
    }

    /**
     * Get the total number of stages,
     * @return all stages.
     */
    public int getTotalStages()
    {
        return totalStages;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BOWindow getWindow()
    {
        return new WindowBuilderResModule(Constants.MOD_ID + ":gui/layouthuts/layoutbuilderres.xml", buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return "inventory";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.workerhuts.resourcelist";
    }
}
