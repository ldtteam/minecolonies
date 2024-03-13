package com.minecolonies.core.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.structurize.api.RotationMirror;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IRSComponent;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.core.client.gui.WindowPostBox;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingView;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to manage the postbox building block.
 */
public class PostBox extends AbstractBuilding implements IRSComponent
{
    /**
     * Description of the block used to set this block.
     */
    private static final String POST_BOX = "postbox";

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public PostBox(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        return ImmutableList.of();
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return POST_BOX;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return 0;
    }

    @Override
    public boolean canBeGathered()
    {
        return false;
    }

    @Override
    public void onRequestedRequestCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
    {
        super.onRequestedRequestCancelled(manager, request);
        if (request.getState() == RequestState.FAILED && request.getRequest() instanceof Stack)
        {
            final IDeliverable req = ((Stack) request.getRequest()).copyWithCount(((Stack) request.getRequest()).getCount());
            createRequest(req, false);
        }
    }

    @Override
    public Tuple<BlockPos, BlockPos> getCorners()
    {
        return new Tuple<>(getPosition(),getPosition());
    }

    @Override
    public RotationMirror getRotationMirror()
    {
        return RotationMirror.NONE.mirrorate(super.getRotationMirror().mirror());
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingView
    {
        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public BOWindow getWindow()
        {
            return new WindowPostBox(this);
        }

        @NotNull
        @Override
        public MutableComponent getRequesterDisplayName(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request)
        {
            return Component.translatableEscape("block.minecolonies.blockpostbox.name");
        }
    }
}
