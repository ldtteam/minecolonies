package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.google.common.collect.Sets;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.coremod.client.gui.modules.ArcheologistsWindow;
import com.minecolonies.coremod.entity.ai.citizen.archeologist.StructureTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class ArcheologistsModuleView extends AbstractBuildingModuleView
{
    /**
     * The position that is currently being targeted by the archeologist.
     */
    @Nullable
    private StructureTarget target;

    @NotNull
    private final Set<BlockPos> previouslyVisitedStructures = Sets.newHashSet();

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        if (buf.readBoolean()) {
            target = new StructureTarget(
              buf.readBlockPos(),
              buf.readBlockPos(),
              buf.readResourceLocation()
            );
        }
        previouslyVisitedStructures.clear();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            previouslyVisitedStructures.add(buf.readBlockPos());
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
    public StructureTarget getTarget()
    {
        return target;
    }

    public boolean hasTarget() {
        return target != null;
    }

    public List<BlockPos> getPreviouslyVisitedStructures()
    {
        return List.copyOf(previouslyVisitedStructures);
    }
}
