package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Color;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.DropDownList;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.StructureName;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.network.messages.BuildRequestMessage;
import com.minecolonies.coremod.network.messages.BuildingSetStyleMessage;
import com.minecolonies.coremod.util.StructureWrapper;
import com.minecolonies.structures.helpers.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
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
    private       DropDownList     stylesDropDownList;

    /**
     * White color.
     */
    private static final int WHITE     = Color.getByName("white", 0);

    /**
     * List of style for the section.
     */
    @NotNull
    private List<String> styles = new ArrayList<>();

    /**
     * Constructor for the window when the player wants to hire a worker for a certain job.
     *
     * @param c          the colony view.
     * @param buildingId the building position.
     */
    public WindowBuildBuilding(final ColonyView c, final BlockPos buildingId)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        building = c.getBuilding(buildingId);
        initStyleNavigation();
        registerButton(BUTTON_BUILD, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        final Button buttonBuild = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
        if (building.getBuildingLevel() == 0)
        {
            buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.build"));
        }
        else if(building.getBuildingLevel() == building.getBuildingMaxLevel())
        {
            buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.switchStyle"));
        }
        else
        {
            buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.upgrade"));
        }
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
        MineColonies.getNetwork().sendToServer(new BuildingSetStyleMessage(building, styles.get(stylesDropDownList.getSelectedIndex())));
        if(building.getBuildingLevel() == building.getBuildingMaxLevel())
        {
            MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.REPAIR));
        }
        else
        {
            MineColonies.getNetwork().sendToServer(new BuildRequestMessage(building, BuildRequestMessage.BUILD));
        }
        cancelClicked();
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
        if(Settings.instance.isStaticSchematicMode())
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
        final StructureName sn = new StructureName(Structures.SCHEMATICS_PREFIX, styles.get(stylesDropDownList.getSelectedIndex()) ,
                building.getSchematicName() + nextLevel);
        final StructureWrapper wrapper = new StructureWrapper(world, sn.toString());
        wrapper.setPosition(building.getLocation());
        wrapper.rotate(building.getRotation(), world, building.getLocation(), building.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE);
        while (wrapper.findNextBlock())
        {
            @Nullable final Template.BlockInfo blockInfo = wrapper.getBlockInfo();
            @Nullable final Template.EntityInfo entityInfo = wrapper.getEntityinfo();

            if (entityInfo != null)
            {
                for (final ItemStack stack : ItemStackUtils.getListOfStackForEntity(entityInfo, world, Minecraft.getMinecraft().player))
                {
                    if (!ItemStackUtils.isEmpty(stack))
                    {
                        addNeededResource(stack, 1);
                    }
                }
            }

            if (blockInfo == null)
            {
                continue;
            }

            @Nullable final IBlockState blockState = blockInfo.blockState;
            @Nullable final Block block = blockState.getBlock();

            if (wrapper.isStructureBlockEqualWorldBlock()
                    || (blockState.getBlock() instanceof BlockBed && blockState.getValue(BlockBed.PART).equals(BlockBed.EnumPartType.FOOT))
                    || (blockState.getBlock() instanceof BlockDoor && blockState.getValue(BlockDoor.HALF).equals(BlockDoor.EnumDoorHalf.UPPER)))
            {
                continue;
            }

            if (block != null
                    && block != Blocks.AIR
                    && !AbstractEntityAIStructure.isBlockFree(block, 0)
                    && block != ModBlocks.blockSolidSubstitution
                    && block != ModBlocks.blockSubstitution)
            {
                if (wrapper.getBlockInfo().tileentityData != null)
                {
                    final List<ItemStack> itemList = new ArrayList<>();
                    if (wrapper.getBlockInfo() != null && wrapper.getBlockInfo().tileentityData != null)
                    {
                        itemList.addAll(ItemStackUtils.getItemStacksOfTileEntity(wrapper.getBlockInfo().tileentityData, world));
                    }

                    for (final ItemStack stack : itemList)
                    {
                        addNeededResource(stack, 1);
                    }
                }

                addNeededResource(BlockUtils.getItemStackFromBlockState(blockState), 1);
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
        ItemStorage resource = resources.get(res.getUnlocalizedName());
        if (resource == null)
        {
            resource = new ItemStorage(res);
            resource.setAmount(amount);
        }
        else
        {
            resource.setAmount(resource.getAmount() + amount);
        }
        resources.put(res.getUnlocalizedName(), resource);
    }

    /**
     * Initialise the previous/next and drop down list for style.
     */
    private void initStyleNavigation()
    {
        registerButton(BUTTON_PREVIOUS_STYLE_ID, this::previousStyle);
        registerButton(BUTTON_NEXT_STYLE_ID, this::nextStyle);
        stylesDropDownList = findPaneOfTypeByID(DROPDOWN_STYLE_ID, DropDownList.class);
        stylesDropDownList.setHandler(this::onDropDownListChanged);
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
     * called every time one of the dropdownlist changed.
     *
     * @param list the dropdown list which change
     */
    private void onDropDownListChanged(final DropDownList list)
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
