package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.DropDownList;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.network.messages.SchematicRequestMessage;
import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlueprintPositionInfo;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.ai.citizen.builder.IBuilderUndestroyable;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.network.messages.server.colony.building.BuildRequestMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.BuildingSetStyleMessage;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.placement.BlueprintIterator.NULL_POS;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for selecting the style and confirming the resources.
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
        registerButton(BUTTON_MOVE_BUILDING, this::moveBuildingClicked);

        final Button buttonBuild = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
        if (building.getBuildingLevel() == 0)
        {
            buttonBuild.setText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.build"));
            findPaneOfTypeByID(BUTTON_MOVE_BUILDING, Button.class).hide();
        }
        else if (building.getBuildingLevel() == building.getBuildingMaxLevel())
        {
            buttonBuild.hide();
        }
        else
        {
            buttonBuild.setText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.upgrade"));
        }

        if (building.isDeconstructed())
        {
            findPaneOfTypeByID(BUTTON_MOVE_BUILDING, Button.class).setText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.pickup"));
        }
    }

    /**
     * When the move building button has been clicked.
     */
    private void moveBuildingClicked()
    {
        final BlockPos builder = buildersDropDownList.getSelectedIndex() == 0 ? BlockPos.ZERO : builders.get(buildersDropDownList.getSelectedIndex()).getB();
        Network.getNetwork().sendToServer(new BuildingSetStyleMessage(building, styles.get(stylesDropDownList.getSelectedIndex())));
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
        if (building.getBuildingLevel() > 0
            && !building.getStyle().equals(styles.get(stylesDropDownList.getSelectedIndex()))
            && !building.isDeconstructed())
            return;

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
        Network.getNetwork().sendToServer(new BuildingSetStyleMessage(building, building.getStyle()));
        Network.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.Mode.REPAIR, builder));
        cancelClicked();
    }

    /**
     * Update the builders list but try to keep the same one.
     */
    private void updateBuilders()
    {
        builders.clear();
        builders.add(new Tuple<>(LanguageHandler.format("com.minecolonies.coremod.job.Builder") + ":", BlockPos.ZERO));
        builders.addAll(building.getColony().getBuildings().stream()
                .filter(build -> build instanceof AbstractBuildingBuilderView && !((AbstractBuildingBuilderView) build).getWorkerName().isEmpty()
                        && !(build instanceof BuildingMiner.View))
                .map(build -> new Tuple<>(((AbstractBuildingBuilderView) build).getWorkerName(), build.getPosition()))
                .sorted(Comparator.comparing(item -> item.getB().distanceSq(building.getPosition())))
                .collect(Collectors.toList()));

        initBuilderNavigation();
    }

    /**
     * Update the styles list but try to keep the same one.
     */
    private void updateStyles()
    {
        styles = Structures.getStylesFor(building.getSchematicName());
        int newIndex = styles.indexOf(building.getStyle());
        if (newIndex == -1)
        {
            newIndex = 0;
        }

        final boolean enabled;
        if (Settings.instance.isStaticSchematicMode())
        {
            enabled = false;
        }
        else
        {
            enabled = styles.size() > 1;
        }

        findPaneOfTypeByID(BUTTON_PREVIOUS_STYLE_ID, Button.class).setEnabled(enabled);
        findPaneOfTypeByID(DROPDOWN_STYLE_ID, DropDownList.class).setEnabled(enabled);
        findPaneOfTypeByID(BUTTON_NEXT_STYLE_ID, Button.class).setEnabled(enabled);
        stylesDropDownList.setSelectedIndex(newIndex);
    }

    /**
     * Clears and resets/updates all resources.
     */
    private void updateResources()
    {
        // Ensure the player cannot change a style of an already constructed building
        if (building.getBuildingLevel() > 0 && stylesDropDownList.getSelectedIndex() != -1)
        {
            findPaneOfTypeByID(BUTTON_BUILD, Button.class).setText(
                    LanguageHandler.format(
                            !building.getStyle().equals(styles.get(stylesDropDownList.getSelectedIndex()))
                            && !building.isDeconstructed()
                                ? "com.minecolonies.coremod.gui.workerhuts.bad_style"
                                : "com.minecolonies.coremod.gui.workerhuts.upgrade"));
        }

        final World world = Minecraft.getInstance().world;
        resources.clear();

        final int nextLevel = building.getBuildingLevel() == building.getBuildingMaxLevel() ?
                                building.getBuildingMaxLevel() : (building.getBuildingLevel() + 1);
        final StructureName sn = new StructureName(Structures.SCHEMATICS_PREFIX, styles.get(stylesDropDownList.getSelectedIndex()),
          building.getSchematicName() + nextLevel);
        final LoadOnlyStructureHandler structure = new LoadOnlyStructureHandler(world, building.getPosition(), sn.toString(), new PlacementSettings(), true);
        final String md5 = Structures.getMD5(sn.toString());
        if (!structure.hasBluePrint() || !structure.isCorrectMD5(md5))
        {
            if (!structure.hasBluePrint())
            {
                Log.getLogger().info("Template structure " + sn + " missing");
            }
            else
            {
                Log.getLogger().info("structure " + sn + " md5 error");
            }

            Log.getLogger().info("Request To Server for structure " + sn);
            if (ServerLifecycleHooks.getCurrentServer() == null)
            {
                com.ldtteam.structurize.Network.getNetwork().sendToServer(new SchematicRequestMessage(sn.toString()));
                return;
            }
            else
            {
                Log.getLogger().error("WindowMinecoloniesBuildTool: Need to download schematic on a standalone client/server. This should never happen", new Exception());
            }
        }

        structure.getBluePrint().rotateWithMirror(BlockPosUtil.getRotationFromRotations(building.getRotation()), building.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE, world);
        StructurePlacer placer = new StructurePlacer(structure);
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
        ItemStorage resource = resources.get(res.getTranslationKey());
        if (resource == null)
        {
            resource = new ItemStorage(res);
            resource.setAmount(amount);
        }
        else
        {
            resource.setAmount(resource.getAmount() + amount);
        }
        resources.put(res.getTranslationKey(), resource);
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
                resourceLabel.setText(resource.getItemStack().getDisplayName());
                quantityLabel.setText(Integer.toString(resource.getAmount()));
                resourceLabel.setColors(WHITE);
                quantityLabel.setColors(WHITE);
                final ItemStack itemIcon = new ItemStack(resource.getItem(), 1);
                itemIcon.setTag(resource.getItemStack().getTag());
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(itemIcon);
            }
        });
    }
}
