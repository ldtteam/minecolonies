package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.modules.FarmerFieldsModuleWindow;
import com.minecolonies.coremod.network.messages.server.colony.building.farmer.AssignFieldMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.farmer.AssignmentModeMessage;
import com.minecolonies.coremod.tileentities.ScarecrowTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Client side version of the abstract class for all buildings which require a filterable list of allowed items.
 */
public class FarmerFieldModuleView extends AbstractBuildingModuleView
{
    /**
     * Checks if fields should be assigned manually.
     */
    private boolean shouldAssignFieldManually;

    /**
     * Contains a view object of all the fields in the colony.
     */
    @NotNull
    private List<BlockPos> fields = new ArrayList<>();

    /**
     * The amount of fields the farmer owns.
     */
    private int amountOfFields;

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.workerhuts.farmerhut.fields";
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        fields = new ArrayList<>();

        shouldAssignFieldManually = buf.readBoolean();
        final int size = buf.readInt();
        for (int i = 1; i <= size; i++)
        {
            @NotNull final BlockPos pos = buf.readBlockPos();
            fields.add(pos);
        }
        amountOfFields = buf.readInt();
    }

    /**
     * Should the farmer be assigned manually to the fields.
     *
     * @return true if yes.
     */
    public boolean assignFieldManually()
    {
        return shouldAssignFieldManually;
    }

    /**
     * Getter of the fields list.
     *
     * @return an unmodifiable List.
     */
    @NotNull
    public List<BlockPos> getFields()
    {
        return fields;
    }

    /**
     * Getter for amount of fields.
     *
     * @return the amount of fields.
     */
    public int getAmountOfFields()
    {
        return amountOfFields;
    }

    /**
     * Sets the assignedFieldManually in the view.
     *
     * @param assignFieldManually variable to set.
     */
    public void setAssignFieldManually(final boolean assignFieldManually)
    {
        Network.getNetwork().sendToServer(new AssignmentModeMessage(buildingView, assignFieldManually));
        this.shouldAssignFieldManually = assignFieldManually;
    }

    /**
     * Change a field at a certain position.
     *
     * @param id                  the position of the field.
     * @param addNewField         should new field be added.
     * @param scarecrowTileEntity the tileEntity.
     */
    public void changeFields(final BlockPos id, final boolean addNewField, final ScarecrowTileEntity scarecrowTileEntity)
    {
        if (buildingView != null && (!addNewField || amountOfFields < buildingView.getBuildingLevel()))
        {
            Network.getNetwork().sendToServer(new AssignFieldMessage(buildingView, addNewField, id));
            scarecrowTileEntity.setTaken(addNewField);

            final WorkerBuildingModuleView view = buildingView.getModuleViewMatching(WorkerBuildingModuleView.class, m -> m.getJobEntry() == ModJobs.farmer.get());
            if (addNewField)
            {
                if (!view.getAssignedCitizens().isEmpty())
                {
                    scarecrowTileEntity.setOwner(view.getAssignedCitizens().get(0), getColony());
                }
                amountOfFields++;
            }
            else
            {
                scarecrowTileEntity.setOwner(0, getColony());
                amountOfFields--;
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BOWindow getWindow()
    {
        return new FarmerFieldsModuleWindow(buildingView, this);
    }

    @Override
    public String getIcon()
    {
        return "field";
    }
}
