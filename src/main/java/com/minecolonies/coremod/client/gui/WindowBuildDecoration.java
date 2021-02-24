package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.DropDownList;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.network.messages.SchematicRequestMessage;
import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.util.LanguageHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingBuilderView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.network.messages.server.BuildToolPlaceMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.placement.BlueprintIterator.NULL_POS;
import static com.minecolonies.api.util.constant.WindowConstants.*;

public class WindowBuildDecoration extends AbstractWindowSkeleton
{
    /**
     * Link to the xml file of the window.
     */
    private static final String BUILDING_NAME_RESOURCE_SUFFIX = ":gui/windowbuildbuilding.xml";

    /**
     * White color.
     */
    private static final int WHITE = Color.getByName("white", 0);

    /**
     * List of style for the section.
     */
    @NotNull
    private final List<Tuple<String, BlockPos>> builders = new ArrayList<>();

    /**
     * Drop down list for builders.
     */
    private DropDownList buildersDropDownList;

    /**
     * Contains all resources needed for a certain build.
     */
    private final Map<String, ItemStorage> resources = new HashMap<>();

    /**
     * Stores the message to be transmitted upon completion
     */
    private final BuildToolPlaceMessage placementMessage;

    /**
     * The name of the structure
     */
    private final StructureName structureName;

    /**
     * The position of the decoration anchor
     */
    private final BlockPos structurePos;

    /**
     * Constructs the decoration build confirmation dialog
     */
    public WindowBuildDecoration(BuildToolPlaceMessage msg, BlockPos pos, StructureName structure)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        placementMessage = msg;
        structureName = structure;
        structurePos = pos;

        registerButton(BUTTON_BUILD, this::confirmedBuild);
        registerButton(BUTTON_CANCEL, this::close);

        findPaneOfTypeByID(BUTTON_BUILD, Button.class)
                .setText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.build"));
        findPaneOfTypeByID(BUTTON_MOVE_BUILDING, Button.class).hide();
        findPaneOfTypeByID(BUTTON_REPAIR, Button.class).hide();
        findPaneOfTypeByID(BUTTON_NEXT_STYLE_ID, Button.class).hide();
        findPaneOfTypeByID(BUTTON_PREVIOUS_STYLE_ID, Button.class).hide();
        findPaneOfTypeByID(DROPDOWN_STYLE_ID, DropDownList.class).disable();
    }

    @Override
    public void onOpened()
    {
        updateBuilders();
        updateResources();
    }

    /**
     * Update the builders list but try to keep the same one.
     */
    private void updateBuilders()
    {
        IColonyView colony = (IColonyView) IColonyManager.getInstance()
                .getIColony(Minecraft.getInstance().world, structurePos);

        if (colony == null)
        {
            LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, TranslationConstants.OUT_OF_COLONY);
            close();
            return;
        }

        builders.clear();
        builders.add(new Tuple<>(LanguageHandler.format("com.minecolonies.coremod.job.Builder") + ":", BlockPos.ZERO));
        builders.addAll(colony.getBuildings().stream()
                .filter(build -> build instanceof AbstractBuildingBuilderView && !((AbstractBuildingBuilderView) build).getWorkerName().isEmpty()
                        && !(build instanceof BuildingMiner.View))
                .map(build -> new Tuple<>(((AbstractBuildingBuilderView) build).getWorkerName(), build.getPosition()))
                .sorted(Comparator.comparing(item -> item.getB().distanceSq(structurePos)))
                .collect(Collectors.toList()));

        initBuilderNavigation();
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
     * Clears and resets/updates all resources.
     */
    private void updateResources()
    {
        final World world = Minecraft.getInstance().world;
        resources.clear();

        final LoadOnlyStructureHandler structure = new LoadOnlyStructureHandler(
                world,
                structurePos,
                structureName.toString(),
                new PlacementSettings(),
                true);

        final String md5 = Structures.getMD5(structureName.toString());
        if (!structure.hasBluePrint() || !structure.isCorrectMD5(md5))
        {
            if (!structure.hasBluePrint())
            {
                Log.getLogger().info("Template structure " + structureName + " missing");
            }
            else
            {
                Log.getLogger().info("structure " + structureName + " md5 error");
            }

            Log.getLogger().info("Request To Server for structure " + structureName);
            if (ServerLifecycleHooks.getCurrentServer() == null)
            {
                com.ldtteam.structurize.Network.getNetwork().sendToServer(new SchematicRequestMessage(structureName.toString()));
                return;
            }
            else
            {
                Log.getLogger().error("WindowMinecoloniesBuildTool: Need to download schematic on a standalone client/server. This should never happen", new Exception());
            }
        }

        StructurePlacer placer = new StructurePlacer(structure);
        StructurePhasePlacementResult result;
        BlockPos progressPos = NULL_POS;

        do
        {
            result = placer.executeStructureStep(world, null, progressPos, StructurePlacer.Operation.GET_RES_REQUIREMENTS,
                    () -> placer.getIterator().increment(), true);

            progressPos = result.getIteratorPos();
            for (final ItemStack stack : result.getBlockResult().getRequiredItems())
            {
                addNeededResource(stack, stack.getCount());
            }
        }
        while (result.getBlockResult().getResult() != BlockPlacementResult.Result.FINISHED);


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
                resourceLabel.setText(resource.getItemStack().getDisplayName().getString());
                quantityLabel.setText(Integer.toString(resource.getAmount()));
                resourceLabel.setColors(WHITE);
                quantityLabel.setColors(WHITE);
                final ItemStack itemIcon = new ItemStack(resource.getItem(), 1);
                itemIcon.setTag(resource.getItemStack().getTag());
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(itemIcon);
            }
        });
    }

    private void confirmedBuild ()
    {
        placementMessage.builder = buildersDropDownList.getSelectedIndex() == 0
                ? BlockPos.ZERO
                : builders.get(buildersDropDownList.getSelectedIndex()).getB();

        Network.getNetwork().sendToServer(placementMessage);
        close();
    }
}
