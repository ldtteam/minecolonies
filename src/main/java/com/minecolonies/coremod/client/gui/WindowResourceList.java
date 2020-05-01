package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.utils.BuildingBuilderResource;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBuilder;
import com.minecolonies.coremod.network.messages.MarkBuildingDirtyMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.WindowHutBuilder.*;

/**
 * Window for the resource list item.
 */
public class WindowResourceList extends AbstractWindowSkeleton
{
    @Nullable
    private final BuildingBuilder.View builder;

    /**
     * List of resources needed.
     */
    @NotNull
    private final List<BuildingBuilderResource> resources = new ArrayList<>();

    /**
     * Constructor for the resource scroll window.
     * @param colonyId the colony id.
     * @param buildingPos the building position.
     */
    public WindowResourceList(final int colonyId, final BlockPos buildingPos)
    {
        super(Constants.MOD_ID + RESOURCE_SCROLL_RESOURCE_SUFFIX);
        final IColonyView colonyView = IColonyManager.getInstance().getColonyView(colonyId, Minecraft.getInstance().world.getDimension().getType().getId());
        if (colonyView != null)
        {
            final IBuildingView buildingView = colonyView.getBuilding(buildingPos);
            if (buildingView instanceof BuildingBuilder.View)
            {
                this.builder = (BuildingBuilder.View) buildingView;
                return;
            }
        }

        LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, "com.minecolonies.coremod.gui.resourcescroll.nobuilder");
        this.builder = null;
    }

    /**
     * Retrieve resources from the building to display in GUI.
     */
    private void pullResourcesFromHut()
    {
        final IBuildingView newView = builder.getColony().getBuilding(builder.getID());
        if (newView instanceof BuildingBuilder.View)
        {
            final BuildingBuilder.View updatedView = (BuildingBuilder.View) newView;
            final PlayerInventory inventory = this.mc.player.inventory;
            final boolean isCreative = this.mc.player.isCreative();

            resources.clear();
            resources.addAll(updatedView.getResources().values());
            for (final BuildingBuilderResource resource : resources)
            {
                final int amountToSet;
                if (isCreative)
                {
                    amountToSet = resource.getAmount();
                }
                else
                {
                    amountToSet =
                            InventoryUtils.getItemCountInItemHandler(new InvWrapper(inventory),
                                    stack -> !ItemStackUtils.isEmpty(stack) && stack.isItemEqual(resource.getItemStack()));
                }
                resource.setPlayerAmount(amountToSet);
            }

            resources.sort(new BuildingBuilderResource.ResourceComparator());
        }
    }

    @Override
    public void onOpened()
    {
        if (this.builder == null)
        {
            Minecraft.getInstance().player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.resourcescroll.nobuilder"));
            close();
            return;
        }
        super.onOpened();

        pullResourcesFromHut();

        final ScrollingList resourceList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        if (resourceList == null)
        {
            Minecraft.getInstance().player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.resourcescroll.null"));
            close();
            return;
        }
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return resources.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                updateResourcePane(index, rowPane);
            }
        });

        //Make sure we have a fresh view
        Network.getNetwork().sendToServer(new MarkBuildingDirtyMessage(builder));

        findPaneOfTypeByID(LABEL_WORKERNAME, Label.class).setLabelText(builder.getWorkerName());
        findPaneOfTypeByID(LABEL_CONSTRUCTION_NAME, Label.class).setLabelText(builder.getConstructionName());
        findPaneOfTypeByID(LABEL_PROGRESS, Label.class).setLabelText(builder.getProgress());
    }

    /**
     * Update one row pad with its resource informations.
     *
     * @param index   index in the list of resources.
     * @param rowPane The Pane to use to display the information.
     */
    private void updateResourcePane(final int index, @NotNull final Pane rowPane)
    {
        final BuildingBuilderResource resource = resources.get(index);
        final Label resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Label.class);
        final Label resourceMissingLabel = rowPane.findPaneOfTypeByID(RESOURCE_MISSING, Label.class);
        final Label neededLabel = rowPane.findPaneOfTypeByID(RESOURCE_AVAILABLE_NEEDED, Label.class);

        switch (resource.getAvailabilityStatus())
        {
            case DONT_HAVE:
                resourceLabel.setColor(RED, RED);
                resourceMissingLabel.setColor(RED, RED);
                neededLabel.setColor(RED, RED);
                break;
            case NEED_MORE:
                resourceLabel.setColor(RED, RED);
                resourceMissingLabel.setColor(RED, RED);
                neededLabel.setColor(RED, RED);
                break;
            case HAVE_ENOUGH:
                resourceLabel.setColor(DARKGREEN, DARKGREEN);
                resourceMissingLabel.setColor(DARKGREEN, DARKGREEN);
                neededLabel.setColor(DARKGREEN, DARKGREEN);
                break;
            case NOT_NEEDED:
            default:
                resourceLabel.setColor(BLACK, BLACK);
                resourceMissingLabel.setColor(BLACK, BLACK);
                neededLabel.setColor(BLACK, BLACK);
                break;
        }

        resourceLabel.setLabelText(resource.getName());
        final int missing = resource.getMissingFromPlayer();
        if (missing < 0)
        {
            resourceMissingLabel.setLabelText(Integer.toString(missing));
        }
        else
        {
            resourceMissingLabel.setLabelText("");
        }

        neededLabel.setLabelText(resource.getAvailable() + " / " + resource.getAmount());
        rowPane.findPaneOfTypeByID(RESOURCE_ID, Label.class).setLabelText(Integer.toString(index));
        rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Label.class).setLabelText(Integer.toString(resource.getAmount() - resource.getAvailable()));

        final ItemStack stack = new ItemStack(resource.getItem(), 1);
        stack.setTag(resource.getItemStack().getTag());
        rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(stack);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        pullResourcesFromHut();
        window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class).refreshElementPanes();
    }
}
