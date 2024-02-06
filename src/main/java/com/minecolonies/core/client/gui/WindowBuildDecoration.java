package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.Color;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.DropDownList;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.placement.BlockPlacementResult;
import com.ldtteam.structurize.placement.StructurePhasePlacementResult;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.views.AbstractBuildingBuilderView;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ldtteam.structurize.placement.AbstractBlueprintIterator.NULL_POS;
import static com.minecolonies.api.util.constant.TranslationConstants.ACTION_BUILD;
import static com.minecolonies.api.util.constant.TranslationConstants.OUT_OF_COLONY;
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
     * Pack meta of the deco.
     */
    private final String packMeta;

    /**
     * Path of the blueprint in the pack.
     */
    private final String path;

    /**
     * Rotation.
     */
    private final Rotation rotation;

    /**
     * Mirror.
     */
    private final boolean mirror;

    /**
     * A function that will supply the message, given the position of the requested builder, if any.
     */
    private final Function<BlockPos, IMessage> buildRequestMessage;

    /**
     * Drop down list for builders.
     */
    private DropDownList buildersDropDownList;

    /**
     * Contains all resources needed for a certain build.
     */
    private final Map<String, ItemStorage> resources = new HashMap<>();

    /**
     * The position of the decoration anchor
     */
    private final BlockPos structurePos;

    /**
     * The blueprint future of this.
     */
    private Future<Blueprint> blueprintFuture;

    /**
     * Constructs the decoration build confirmation dialog
     */
    public WindowBuildDecoration(
      final BlockPos pos,
      final String packMeta,
      final String path,
      final Rotation rotation,
      final boolean mirror,
      Function<BlockPos, IMessage> buildRequestMessage)
    {
        super(Constants.MOD_ID + BUILDING_NAME_RESOURCE_SUFFIX);
        this.packMeta = packMeta;
        this.path = path;
        this.structurePos = pos;

        registerButton(BUTTON_BUILD, this::confirmedBuild);
        registerButton(BUTTON_CANCEL, this::close);

        findPaneOfTypeByID(BUTTON_BUILD, Button.class).setText(Component.translatable(ACTION_BUILD));
        findPaneOfTypeByID(BUTTON_BUILD, Button.class).hide();
        findPaneOfTypeByID(BUTTON_DECONSTRUCT_BUILDING, Button.class).hide();
        findPaneOfTypeByID(BUTTON_REPAIR, Button.class).hide();
        findPaneOfTypeByID(BUTTON_NEXT_STYLE_ID, Button.class).hide();
        findPaneOfTypeByID(BUTTON_PREVIOUS_STYLE_ID, Button.class).hide();
        findPaneOfTypeByID(DROPDOWN_STYLE_ID, DropDownList.class).disable();
        findPaneOfTypeByID(DROPDOWN_STYLE_ID, DropDownList.class).hide();

        final String cleanedPackName = packMeta.replace(Minecraft.getInstance().player.getUUID().toString(), "");
        blueprintFuture = StructurePacks.getBlueprintFuture(cleanedPackName, path);
        this.rotation = rotation;
        this.mirror = mirror;
        this.buildRequestMessage = buildRequestMessage;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        updateBuilders();
        updateResources();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateResources();
    }

    /**
     * Update the builders list but try to keep the same one.
     */
    private void updateBuilders()
    {
        IColonyView colony = (IColonyView) IColonyManager.getInstance()
                                             .getIColony(Minecraft.getInstance().level, structurePos);

        if (colony == null)
        {
            MessageUtils.format(OUT_OF_COLONY, path, structurePos.getX(), structurePos.getZ()).sendTo(Minecraft.getInstance().player);
            close();
            return;
        }

        builders.clear();
        builders.add(new Tuple<>(Component.translatable(ModJobs.builder.get().getTranslationKey()).getString() + ":", BlockPos.ZERO));
        builders.addAll(colony.getBuildings().stream()
                          .filter(build -> build instanceof AbstractBuildingBuilderView && !((AbstractBuildingBuilderView) build).getWorkerName().isEmpty()
                                             && build.getBuildingType() != ModBuildings.miner.get())
                          .map(build -> new Tuple<>(((AbstractBuildingBuilderView) build).getWorkerName(), build.getPosition()))
                          .sorted(Comparator.comparing(item -> item.getB().distSqr(structurePos)))
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
        if (blueprintFuture == null || !blueprintFuture.isDone())
        {
            return;
        }

        final Level world = Minecraft.getInstance().level;
        resources.clear();

        try
        {
            if (blueprintFuture.get() == null)
            {
                blueprintFuture = null;
                return;
            }
            final LoadOnlyStructureHandler structure = new LoadOnlyStructureHandler(
              world,
              structurePos,
              blueprintFuture.get(),
              new PlacementSettings(),
              true);
            structure.getBluePrint().rotateWithMirror(rotation, mirror ? Mirror.FRONT_BACK : Mirror.NONE, Minecraft.getInstance().level);

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

            findPaneOfTypeByID(BUTTON_BUILD, Button.class).show();
            window.findPaneOfTypeByID(LIST_RESOURCES, ScrollingList.class).refreshElementPanes();
            updateResourceList();
            blueprintFuture = null;
        }
        catch (final InterruptedException | ExecutionException ex)
        {
            // Noop
        }
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
                resourceLabel.setText(Component.literal(resource.getItemStack().getHoverName().getString()));
                quantityLabel.setText(Component.literal(Integer.toString(resource.getAmount())));
                resourceLabel.setColors(WHITE);
                quantityLabel.setColors(WHITE);
                final ItemStack itemIcon = new ItemStack(resource.getItem(), 1);
                itemIcon.setTag(resource.getItemStack().getTag());
                rowPane.findPaneOfTypeByID(RESOURCE_ICON, ItemIcon.class).setItem(itemIcon);
            }
        });
    }

    private void confirmedBuild()
    {
        final BlockPos builder = buildersDropDownList.getSelectedIndex() == 0
                                   ? BlockPos.ZERO
                                   : builders.get(buildersDropDownList.getSelectedIndex()).getB();

        Network.getNetwork().sendToServer(buildRequestMessage.apply(builder));
        close();
    }
}