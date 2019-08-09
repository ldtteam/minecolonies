package com.minecolonies.coremod.colony.buildings.views;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a view of the builder building class.
 */
public abstract class AbstractBuildingBuilderView extends AbstractBuildingWorker.View
{
    /**
     * The resources he has to keep.
     */
    private final HashMap<String, BuildingBuilderResource> resources = new HashMap<>();

    /**
     * The building he is working on.
     */
    private String constructionName;

    /**
     * The building he is working on.
     */
    private String constructionPos;

    /**
     * The name of the worker at this building.
     */
    private String workerName;

    /**
     * Building progress.
     */
    private double progress;

    /**
     * Public constructor of the view, creates an instance of it.
     *
     * @param c the colony.
     * @param l the position.
     */
    public AbstractBuildingBuilderView(final IColonyView c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public void deserialize(@NotNull final ByteBuf buf)
    {
        super.deserialize(buf);

        final int size = buf.readInt();
        resources.clear();

        for (int i = 0; i < size; i++)
        {
            final ItemStack itemStack = ByteBufUtils.readItemStack(buf);
            final int amountAvailable = buf.readInt();
            final int amountNeeded = buf.readInt();
            final BuildingBuilderResource resource = new BuildingBuilderResource(itemStack, amountNeeded, amountAvailable);
            resources.put(itemStack.getDisplayName() + ":" + itemStack.getItemDamage(), resource);
        }

        constructionName = ByteBufUtils.readUTF8String(buf);
        constructionPos = ByteBufUtils.readUTF8String(buf);
        progress = buf.readDouble();
        workerName = ByteBufUtils.readUTF8String(buf);
    }

    /**
     * Get the construction name he is working at.
     * @return a string describing it.
     */
    public String getConstructionName()
    {
        return constructionName;
    }

    /**
     * Get the construction pos he is working at.
     * @return a string describing the pos.
     */
    public String getConstructionPos()
    {
        return constructionPos;
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
     * Get the name of the worker assigned to this building.
     * @return the name.
     */
    public String getWorkerName()
    {
        return workerName;
    }

    /**
     * Get the building progress (relative to items used)
     * @return the progress.
     */
    public String getProgress()
    {
        return 100 - (int) (progress*100) + "%";
    }
}

