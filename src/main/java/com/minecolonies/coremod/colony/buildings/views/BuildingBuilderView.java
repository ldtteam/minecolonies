package com.minecolonies.coremod.colony.buildings.views;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutBuilder;
import com.minecolonies.coremod.colony.ColonyView;
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
public class BuildingBuilderView extends AbstractBuildingWorker.View
{
    private final HashMap<String, BuildingBuilderResource> resources = new HashMap<>();

    /**
     * Public constructor of the view, creates an instance of it.
     *
     * @param c the colony.
     * @param l the position.
     */
    public BuildingBuilderView(final ColonyView c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Gets the blockOut Window.
     *
     * @return the window of the builder building.
     */
    @NotNull
    @Override
    public Window getWindow()
    {
        return new WindowHutBuilder(this);
    }

    @Override
    public void deserialize(@NotNull ByteBuf buf)
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
            resources.put(itemStack.getDisplayName(), resource);
        }
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

    @NotNull
    @Override
    public AbstractBuildingWorker.Skill getPrimarySkill()
    {
        return AbstractBuildingWorker.Skill.INTELLIGENCE;
    }

    @NotNull
    @Override
    public AbstractBuildingWorker.Skill getSecondarySkill()
    {
        return AbstractBuildingWorker.Skill.STRENGTH;
    }
}

