package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.coremod.client.gui.modules.GraveyardManagementWindow;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GraveyardManagementModuleView extends AbstractBuildingModuleView
{
    /**
     * Contains a view object of all the graves in the colony.
     */
    @NotNull
    private List<BlockPos> graves = new ArrayList<>();

    /**
     * Contains a view object of all the restingCitizen in the colony.
     */
    @NotNull
    private List<String> restingCitizen = new ArrayList<>();

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        graves = new ArrayList<>();
        final int size = buf.readInt();
        for (int i = 1; i <= size; i++)
        {
            @NotNull final BlockPos pos = buf.readBlockPos();
            graves.add(pos);
        }

        restingCitizen = new ArrayList<>();
        final int sizeRIP = buf.readInt();
        for (int i = 1; i <= sizeRIP; i++)
        {
            restingCitizen.add(buf.readUtf());
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Window getWindow()
    {
        return new GraveyardManagementWindow(buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return "grave";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.gui.workerhuts.enchanter.workers";
    }


    /**
     * Getter of the graves list.
     *
     * @return an unmodifiable List.
     */
    @NotNull
    public List<BlockPos> getGraves()
    {
        return graves;
    }

    /**
     * Clean the list of graves if a grave is missing from the world.
     */
    public void cleanGraves()
    {
        for (final BlockPos grave : new ArrayList<>(graves))
        {
            final TileEntity entity = buildingView.getColony().getWorld().getBlockEntity(grave);
            if (!(entity instanceof TileEntityGrave))
            {
                graves.remove(grave);
            }
        }
    }

    /**
     * Getter of the restingCitizen list.
     *
     * @return an unmodifiable List.
     */
    @NotNull
    public List<String> getRestingCitizen()
    {
        return Collections.unmodifiableList(restingCitizen);
    }
}
