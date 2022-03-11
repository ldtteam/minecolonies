package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.tileentities.TileEntityGrave;
import com.minecolonies.coremod.client.gui.modules.ArcheologistsWindow;
import com.minecolonies.coremod.client.gui.modules.GraveyardManagementWindow;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArcheologistsModuleView extends AbstractBuildingModuleView
{
    /**
     * The position that is currently being targeted by the archeologist.
     */
    @Nullable
    private BlockPos target;

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        if (buf.readBoolean()) {
            target = buf.readBlockPos();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BOWindow getWindow()
    {
        return new ArcheologistsWindow(buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return "archeologist";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.gui.workerhuts.archeologist.workers";
    }

    @Nullable
    public BlockPos getTarget()
    {
        return target;
    }

    public boolean hasTarget() {
        return target != null;
    }
}
