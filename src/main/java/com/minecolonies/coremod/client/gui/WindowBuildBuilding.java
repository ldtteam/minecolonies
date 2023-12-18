package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.storage.ClientFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.coremod.network.messages.server.colony.building.BuildPickUpMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.BuildRequestMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.BuildingSetStyleMessage;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.TriPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.placement.AbstractBlueprintIterator.NULL_POS;
import static com.minecolonies.api.util.constant.TranslationConstants.ACTION_BUILD;
import static com.minecolonies.api.util.constant.TranslationConstants.ACTION_UPGRADE;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for selecting the style and confirming the resources.
 */
public class WindowBuildBuilding extends AbstractWindowSkeleton
{
    /**
     * Link to the xml file of the window.
     */
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowbuildbuilding.xml";

    /**
     * Predicate defining things we don't want the builders to ever touch.
     */
    protected TriPredicate<BlueprintPositionInfo, BlockPos, IStructureHandler> DONT_TOUCH_PREDICATE = (info, worldPos, handler) ->
    {
        final BlockState worldState = handler.getWorld().getBlockState(worldPos);

        return worldState.getBlock() instanceof IBuilderUndestroyable
                 || worldState.getBlock() == Blocks.BEDROCK
                 || (info.getBlockInfo().getState().getBlock() instanceof AbstractBlockHut && handler.getWorldPos().equals(worldPos));
    };

    /**
     * The view of the current building.
     */
    private final IBuildingView building;

    /**
     * Contains all resources needed for a certain build.
     */
    private final Map<String, ItemStorage> resources = new HashMap<>();

    /**
     * Drop down list for style.
     */
    private DropDownList stylesDropDownList;

    /**
     * Drop down list for style.
     */
    private DropDownList buildersDropDownList;

    /**
     * White color.
     */
    private static final int WHITE = Color.getByName("white", 0);

    /**
     * List of style for the section.
     */
    @NotNull
    private List<String> styles = new ArrayList<>();

    /**
     * List of style for the section.
     */
    @NotNull
    private List<Tuple<String, BlockPos>> builders = new ArrayList<>();

    /**
     * Update delay.
     */
    private int tick;

    /**
     * Constructor for the window when the player wants to hire a worker for a certain job.
     *
     * @param c        the colony view.
     * @param building the building.
     */
    public WindowBuildBuilding(final IColonyView c, final IBuildingView building)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.building = building;

        initStyleNavigation();
        registerButton(BUTTON_BUILD, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_REPAIR, this::repairClicked);
        registerButton(BUTTON_DECONSTRUCT_BUILDING, this::deconstructBuildingClicked);
        registerButton(BUTTON_PICKUP_BUILDING, this::pickUpBuilding);

        final Button buttonBuild = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
        final IBuildingView parentBuilding = c.getBuilding(building.getParent());

        if (building.getBuildingLevel() == 0)
        {
            buttonBuild.setText(Component.translatable("com.minecolonies.coremod.gui.workerhuts.build"));
            findPaneOfTypeByID(BUTTON_REPAIR, Button.class).hide();
            findPaneOfTypeByID(BUTTON_DECONSTRUCT_BUILDING, Button.class).hide();
            findPaneOfTypeByID(BUTTON_PICKUP_BUILDING, Button.class).show();
        }
        else if (!canBeUpgraded())
        {
            buttonBuild.hide();
        }
        else
        {
            buttonBuild.setText(Component.translatable(ACTION_UPGRADE));
        }

        if (building.isDeconstructed())
        {
            findPaneOfTypeByID(BUTTON_REPAIR, Button.class).setText(Component.translatable(ACTION_BUILD));
            findPaneOfTypeByID(BUTTON_PICKUP_BUILDING, Button.class).show();
        }
    }

    /**
     * Check if this one can be upgraded.
     * @return true if so.
     */
    public boolean canBeUpgraded()
    {
        final IBuildingView parentBuilding = building.getColony().getBuilding(building.getParent());
        return building.getBuildingLevel() < building.getBuildingMaxLevel() && (parentBuilding == null || building.getBuildingLevel() < parentBuilding.getBuildingLevel() || parentBuilding.getBuildingLevel() >= parentBuilding.getBuildingMaxLevel());
    }

    /**
     * When the pickup building button was clicked.
     */
    private void pickUpBuilding()
    {
        Network.getNetwork().sendToServer(new BuildPickUpMessage(building));
        close();
    }

    /**
     * When the deconstruct building button has been clicked.
     */
    private void deconstructBuildingClicked()
    {
        final BlockPos builder = buildersDropDownList.getSelectedIndex() == 0 ? BlockPos.ZERO : builders.get(buildersDropDownList.getSelectedIndex()).getB();
        Network.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.Mode.REMOVE, builder));
        cancelClicked();
    }

    /**
     * On cancel button.
     */
    private void cancelClicked()
    {
        building.openGui(false);
    }

    /**
     * On confirm button.
     */
    private void confirmClicked()
    {
        final BlockPos builder = buildersDropDownList.getSelectedIndex() == 0 ? BlockPos.ZERO : builders.get(buildersDropDownList.getSelectedIndex()).getB();

        Network.getNetwork().sendToServer(new BuildingSetStyleMessage(building, styles.get(stylesDropDownList.getSelectedIndex())));
        if (building.getBuildingLevel() == building.getBuildingMaxLevel())
        {
            Network.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.Mode.REPAIR, builder));
        }
        else
        {
            Network.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.Mode.BUILD, builder));
        }
        cancelClicked();
    }

    /**
     * Action when repair button is clicked.
     */
    private void repairClicked()
    {
        final BlockPos builder = buildersDropDownList.getSelectedIndex() == 0 ? BlockPos.ZERO : builders.get(buildersDropDownList.getSelectedIndex()).getB();
        Network.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.Mode.REPAIR, builder));
        cancelClicked();
    }

    /**
     * Update the builders list but try to keep the same one.
     */
    private void updateBuilders()
    {
        builders.clear();
        builders.add(new Tuple<>(Component.translatable(ModJobs.builder.get().getTranslationKey()).getString() + ":", BlockPos.ZERO));
        builders.addAll(building.getColony().getBuildings().stream()
                          .filter(build -> build instanceof AbstractBuildingBuilderView && !((AbstractBuildingBuilderView) build).getWorkerName().isEmpty()
                                             && build.getBuildingType() != ModBuildings.miner.get())
                          .map(build -> new Tuple<>(((AbstractBuildingBuilderView) build).getWorkerName(), build.getPosition()))
                          .sorted(Comparator.comparing(item -> item.getB().distSqr(building.getPosition())))
                          .collect(Collectors.toList()));

        initBuilderNavigation();
    }

    /**
     * Update the styles list but try to keep the same one.
     */
    private void updateStyles()
    {
        if (!building.getParent().equals(BlockPos.ZERO) && building.getColony().getBuilding(building.getParent()) != null)
        {
            styles = new ArrayList<>();
            styles.add(building.getColony().getBuilding(building.getParent()).getStructurePack());
            if (!styles.isEmpty())
            {
                stylesDropDownList.setSelectedIndex(0);
            }
        }
        else
        {
            styles = new ArrayList<>();
            styles.add(building.getStructurePack());

            if (!styles.isEmpty())
            {
                int newIndex = styles.indexOf(building.getStructurePack());
                if (newIndex == -1)
                {
                    newIndex = 0;
                }
                stylesDropDownList.setSelectedIndex(newIndex);
            }
        }

        findPaneOfTypeByID(BUTTON_PREVIOUS_STYLE_ID, Button.class).setEnabled(enabled);
        findPaneOfTypeByID(DROPDOWN_STYLE_ID, DropDownList.class).setEnabled(enabled);
        findPaneOfTypeByID(BUTTON_NEXT_STYLE_ID, Button.class).setEnabled(enabled);
    }

    /**
     * Clears and resets/updates all resources.
     */
    private void updateResources()
    {
        tick = 20;

        if (stylesDropDownList.getSelectedIndex() == -1)
        {
            return;
        }

        final Level world = Minecraft.getInstance().level;
        int nextLevel = building.getBuildingLevel();
        if (canBeUpgraded())
        {
            nextLevel = building.getBuildingLevel() + 1;
        }

        String name = building.getStructurePath().replace(".blueprint", "");
        if (name.isEmpty())
        {
            return;
        }

        name = name.substring(0, name.length() - 1) + nextLevel + ".blueprint";
        ClientFutureProcessor.queueBlueprint(new ClientFutureProcessor.BlueprintProcessingData(StructurePacks.getBlueprintFuture(styles.get(stylesDropDownList.getSelectedIndex()), name), (blueprint -> {
            resources.clear();
            if (blueprint == null)
            {
                findPaneOfTypeByID(BUTTON_BUILD, Button.class).hide();
                findPaneOfTypeByID(BUTTON_REPAIR, Button.class).hide();
                findPaneOfTypeByID(BUTTON_PICKUP_BUILDING, Button.class).show();
                return;
            }

            blueprint.rotateWithMirror(BlockPosUtil.getRotationFromRotations(building.getRotation()), building.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE, world);
            StructurePlacer placer = new StructurePlacer(new LoadOnlyStructureHandler(Minecraft.getInstance().level, building.getPosition(), blueprint, new PlacementSettings(), true));
            StructurePhasePlacementResult result;
            BlockPos progressPos = NULL_POS;

            do
            {
                result = placer.executeStructureStep(world, null, progressPos, StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                  () -> placer.getIterator().increment(DONT_TOUCH_PREDICATE.and((info, pos, handler) -> false)), true);

                progressPos = result.getIteratorPos();
                for (final ItemStack stack : result.getBlockResult().getRequiredItems())
                {
                    addNeededResource(stack, stack.getCount());
                }
            }
            while (result != null && result.getBlockResult().getResult() != BlockPlacementResult.Result.FINISHED);

            window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class).refreshElementPanes();
            updateResourceList();

        })));

    }

    /**
     * Add a new resource to the needed list.
     *
     * @param res    the resource.
     * @param amount the amount.
     */
    public void addNeededResource(@Nullable final ItemStack res, final int amount)
    {
        if (ItemStackUtils.isEmpty(res) || amount == 0)
        {
            return;
        }
        final int hashCode = res.hasTag() ? res.getTag().hashCode() : 0;
        final String key = res.getDescriptionId() + "-" + hashCode;
        ItemStorage resource = resources.get(key);
        if (resource == null)
        {
            resource = new ItemStorage(res);
            resource.setAmount(amount);
        }
        else
        {
            resource.setAmount(resource.getAmount() + amount);
        }
        resources.put(key, resource);
    }

    /**
     * Initialise the previous/next and drop down list for style.
     */
    private void initStyleNavigation()
    {
        registerButton(BUTTON_PREVIOUS_STYLE_ID, this::previousStyle);
        registerButton(BUTTON_NEXT_STYLE_ID, this::nextStyle);
        stylesDropDownList = findPaneOfTypeByID(DROPDOWN_STYLE_ID, DropDownList.class);
        stylesDropDownList.setHandler(this::onStyleDropDownChanged);
        stylesDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return styles.size();
            }

            @Override
            public String getLabel(final int index)
            {
                if (index >= 0 && index < styles.size())
                {
                    return styles.get(index);
                }
                return "";
            }
        });
    }

    /**
     * Initialise the builder setup..
     */
    private void initBuilderNavigation()
    {
        buildersDropDownList = findPaneOfTypeByID(DROPDOWN_BUILDER_ID, DropDownList.class);
        buildersDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return builders.size();
            }

            @Override
            public String getLabel(final int index)
            {
                if (index >= 0 && index < builders.size())
                {
                    return builders.get(index).getA();
                }
                return "";
            }
        });
        buildersDropDownList.setSelectedIndex(0);
    }

    /**
     * called every time one of the dropdownlist changed.
     *
     * @param list the dropdown list which change
     */
    private void onStyleDropDownChanged(final DropDownList list)
    {
        updateResources();
    }

    /**
     * Change to the next style.
     */
    private void nextStyle()
    {
        stylesDropDownList.selectNext();
    }

    /**
     * Change to the previous style.
     */
    private void previousStyle()
    {
        stylesDropDownList.selectPrevious();
    }

    /**
     * Called when the GUI has been opened. Will fill the fields and lists.
     */
    @Override
    public void onOpened()
    {
        updateStyles();
        updateBuilders();
        updateResources();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (resources.isEmpty() && tick > 0 && --tick == 0)
        {
            updateResources();
        }
    }

    public void updateResourceList()
    {
        final ScrollingList recourseList = findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class);
        recourseList.enable();
        recourseList.show();
        final List<ItemStorage> tempRes = new ArrayList<>(resources.values());

        //Creates a dataProvider for the unemployed recourseList.
        recourseList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return tempRes.size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                final ItemStorage resource = tempRes.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Text.class);
                final Text quantityLabel = rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Text.class);
                resourceLabel.setText(resource.getItemStack().getHoverName());
                quantityLabel.setText(Component.literal(Integer.toString(resource.getAmount())));
                resourceLabel.setColors(WHITE);
                quantityLabel.setColors(WHITE);
                final ItemStack itemIcon = new ItemStack(resource.getItem(), 1);
                itemIcon.setTag(resource.getItemStack().getTag());
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(itemIcon);
            }
        });
    }
}
