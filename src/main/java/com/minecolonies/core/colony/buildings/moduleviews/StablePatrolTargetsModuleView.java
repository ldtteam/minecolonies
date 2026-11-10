package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.modules.building.StablePatrolTargetsModuleWindow;
import com.minecolonies.core.network.messages.server.colony.building.stable.StablePatrolTargetMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Client view of the Stable's selected automatic patrol targets.
 */
public class StablePatrolTargetsModuleView extends AbstractBuildingModuleView
{
    /**
     * Selected targets in traversal order.
     */
    private final List<BlockPos> patrolTargets = new ArrayList<>();

    /**
     * Current patrol target.
     */
    private @Nullable BlockPos activePatrolTargetPos = null;

    /**
     * Maximum number of selected targets.
     */
    private int targetCapacity;

    /**
     * Deserializes the selected targets and capacity from the server.
     *
     * @param buf the synchronization buffer.
     */
    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        targetCapacity = buf.readVarInt();
        patrolTargets.clear();
        final int targetCount = buf.readVarInt();

        for (int index = 0; index < targetCount; index++)
        {
            patrolTargets.add(buf.readBlockPos());
        }

        boolean onPatrol = buf.readBoolean();
        if (onPatrol)
        {
            activePatrolTargetPos = buf.readBlockPos();
        } 
        else
        {
            activePatrolTargetPos = null;
        }
    }

    /**
     * @return Position of building that is the active patrol target.
     */
    @Nullable
    public BlockPos getActivePatrolTarget()
    {
        return activePatrolTargetPos;
    }

    /**
     * Gets the selected patrol targets in traversal order.
     *
     * @return an immutable copy of the selected targets.
     */
    public List<BlockPos> getPatrolTargets()
    {
        return Collections.unmodifiableList(new ArrayList<>(patrolTargets));
    }

    /**
     * Gets the maximum number of selected patrol targets.
     *
     * @return the target capacity.
     */
    public int getTargetCapacity()
    {
        return targetCapacity;
    }

    /**
     * Requests that the server change a patrol target's selected state and updates the local view optimistically.
     *
     * @param target the target building position.
     * @param selected whether the target should be selected.
     */
    public void setPatrolTargetSelected(final BlockPos target, final boolean selected)
    {
        if (selected && !patrolTargets.contains(target))
        {
            patrolTargets.add(target);
        }
        else if (!selected)
        {
            patrolTargets.remove(target);
        }

        Network.getNetwork().sendToServer(new StablePatrolTargetMessage(buildingView, target, selected));
    }

    /**
     * Creates the patrol target selection window.
     *
     * @return the module window.
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public BOWindow getWindow()
    {
        return new StablePatrolTargetsModuleWindow(this);
    }

    /**
     * Gets the module icon.
     *
     * @return the icon resource location.
     */
    @Override
    public ResourceLocation getIconResourceLocation()
    {
        return new ResourceLocation(Constants.MOD_ID, "textures/gui/modules/horse.png");
    }

    /**
     * Gets the module description.
     *
     * @return the translated description.
     */
    @Override
    public Component getDesc()
    {
        return Component.translatable("com.minecolonies.gui.workerhuts.stable.patroltargets");
    }
}
