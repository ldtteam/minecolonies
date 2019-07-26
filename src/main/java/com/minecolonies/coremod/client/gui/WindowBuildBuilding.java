package com.minecolonies.coremod.client.gui;

import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.StructurePlacementUtils;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.Constants;
import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.DropDownList;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.network.messages.BuildRequestMessage;
import com.minecolonies.coremod.network.messages.BuildingSetStyleMessage;
import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.network.messages.SchematicRequestMessage;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structures.helpers.Structure;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * The view of the current building.
     */
    private final AbstractBuildingView building;

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
     * @param c          the colony view.
     * @param building the building.
     */
    public WindowBuildBuilding(final ColonyView c, final AbstractBuildingView building)
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
            buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.build"));
            findPaneOfTypeByID(BUTTON_MOVE_BUILDING, Button.class).hide();
        }
        else if (building.getBuildingLevel() == building.getBuildingMaxLevel())
        {
            buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.switchStyle"));
        }
        else
        {
            buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.upgrade"));
        }
    }

    /**
     * When the move building button has been clicked.
     */
    private void moveBuildingClicked()
    {
        final WindowMoveBuilding window = new WindowMoveBuilding(building.getLocation(), building, styles.get(stylesDropDownList.getSelectedIndex()));
        window.open();
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
        final BlockPos builder = buildersDropDownList.getSelectedIndex() == 0 ? BlockPos.ORIGIN : builders.get(buildersDropDownList.getSelectedIndex()).getB();
        MineColonies.getNetwork().sendToServer(new BuildingSetStyleMessage(building, styles.get(stylesDropDownList.getSelectedIndex())));
        if (building.getBuildingLevel() == building.getBuildingMaxLevel())
        {
            MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.REPAIR, builder));
        }
        else
        {
            MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.BUILD, builder));
        }
        cancelClicked();
    }

    /**
     * Action when repair button is clicked.
     */
    private void repairClicked()
    {
        final BlockPos builder = buildersDropDownList.getSelectedIndex() == 0 ? BlockPos.ORIGIN : builders.get(buildersDropDownList.getSelectedIndex()).getB();
        MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.REPAIR, builder));
        cancelClicked();
    }

    /**
     * Update the builders list but try to keep the same one.
     */
    private void updateBuilders()
    {
        builders.clear();
        builders.add(new Tuple<>(LanguageHandler.format("com.minecolonies.coremod.job.Builder") + ":", BlockPos.ORIGIN));
        builders.addAll(building.getColony().getBuildings().stream()
                          .filter(build -> build instanceof AbstractBuildingBuilderView && !((AbstractBuildingBuilderView) build).getWorkerName().isEmpty() && !(build instanceof BuildingMiner.View))
                          .map(build -> new Tuple<>(((AbstractBuildingBuilderView) build).getWorkerName(), build.getLocation()))
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
        final World world = Minecraft.getMinecraft().world;
        resources.clear();

        final int nextLevel = building.getBuildingLevel() == building.getBuildingMaxLevel() ?
                                building.getBuildingMaxLevel() : (building.getBuildingLevel() + 1);
        final StructureName sn = new StructureName(Structures.SCHEMATICS_PREFIX, styles.get(stylesDropDownList.getSelectedIndex()),
          building.getSchematicName() + nextLevel);
        final Structure structure = new Structure(world, sn.toString(), new PlacementSettings());
        final String md5 = Structures.getMD5(sn.toString());
        if (structure.isBluePrintMissing() || !structure.isCorrectMD5(md5))
        {
            if (structure.isBluePrintMissing())
            {
                Log.getLogger().info("Template structure " + sn + " missing");
            }
            else
            {
                Log.getLogger().info("structure " + sn + " md5 error");
            }

            Log.getLogger().info("Request To Server for structure " + sn);
            if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
            {
                Structurize.getNetwork().sendToServer(new SchematicRequestMessage(sn.toString()));
                return;
            }
            else
            {
                Log.getLogger().error("WindowMinecoloniesBuildTool: Need to download schematic on a standalone client/server. This should never happen");
            }
        }

        structure.setPosition(building.getLocation());
        structure.rotate(BlockPosUtil.getRotationFromRotations(building.getRotation()), world, building.getLocation(), building.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE);
        while (structure.findNextBlock())
        {
            @Nullable final BlockInfo blockInfo = structure.getBlockInfo();
            @Nullable final BlockState blockState = blockInfo.getState();

            if (blockState == null)
            {
                continue;
            }

            @Nullable final Block block = blockState.getBlock();

            if (StructurePlacementUtils.isStructureBlockEqualWorldBlock(world, structure.getBlockPosition(), blockState)
                  || (blockState.getBlock() instanceof BlockBed && blockState.getValue(BlockBed.PART).equals(BlockBed.EnumPartType.FOOT))
                  || (blockState.getBlock() instanceof BlockDoor && blockState.getValue(BlockDoor.HALF).equals(BlockDoor.EnumDoorHalf.UPPER)))
            {
                continue;
            }

            if (block != Blocks.AIR
                  && !AbstractEntityAIStructure.isBlockFree(block, 0)
                  && block != com.ldtteam.structurize.blocks.ModBlocks.blockSolidSubstitution
                  && block != com.ldtteam.structurize.blocks.ModBlocks.blockSubstitution)
            {
                if (structure.getBlockInfo().getTileEntityData() != null)
                {
                    final List<ItemStack> itemList = new ArrayList<>();
                    if (structure.getBlockInfo().getState() != null && structure.getBlockInfo().getTileEntityData() != null)
                    {
                        itemList.addAll(ItemStackUtils.getItemStacksOfTileEntity(structure.getBlockInfo().getTileEntityData(), world));
                    }

                    for (final ItemStack stack : itemList)
                    {
                        addNeededResource(stack, 1);
                    }
                }

                addNeededResource(BlockUtils.getItemStackFromBlockState(blockState), 1);
            }
        }

        for (final CompoundNBT entityInfo : structure.getEntityData())
        {
            if (entityInfo != null)
            {
                for (final ItemStorage stack : ItemStackUtils.getListOfStackForEntityInfo(entityInfo, world, Minecraft.getMinecraft().player))
                {
                    if (!ItemStackUtils.isEmpty(stack.getItemStack()))
                    {
                        addNeededResource(stack.getItemStack(), 1);
                    }
                }
            }
        }

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
     * Called when the GUI has been opened.
     * Will fill the fields and lists.
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
                final Label resourceLabel = rowPane.findPaneOfTypeByID(RESOURCE_NAME, Label.class);
                final Label quantityLabel = rowPane.findPaneOfTypeByID(RESOURCE_QUANTITY_MISSING, Label.class);
                resourceLabel.setLabelText(resource.getItemStack().getDisplayName());
                quantityLabel.setLabelText(Integer.toString(resource.getAmount()));
                resourceLabel.setColor(WHITE, WHITE);
                quantityLabel.setColor(WHITE, WHITE);
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(new ItemStack(resource.getItem(), 1, resource.getDamageValue()));
            }
        });
    }
}
