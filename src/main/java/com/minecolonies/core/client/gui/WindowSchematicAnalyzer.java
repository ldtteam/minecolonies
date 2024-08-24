package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.views.Box;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.api.util.ItemStorage;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.gui.AbstractWindowSkeleton;
import com.ldtteam.structurize.client.gui.WindowExtendedBuildTool;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.minecolonies.api.util.Log;
import com.minecolonies.core.items.ItemScanAnalyzer;
import com.minecolonies.core.util.SchemAnalyzerUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import static com.minecolonies.core.items.ItemScanAnalyzer.TEMP_SCAN;

/**
 * Window for finishing a scan.
 */
public class WindowSchematicAnalyzer extends AbstractWindowSkeleton
{
    /**
     * Link to the xml file of the window.
     */
    private static final String ID = "minecolonies:gui/analyzer/windowanalyze.xml";

    /**
     * Xml ID's for analyzer/analyzedisplay.xml
     */
    private static final String BUTTON_SELECTION_LEFT   = "selectleft";
    private static final String BUTTON_SELECTION_RIGHT  = "selectright";
    private static final String BUTTON_SELECT_SCHEMATIC = "selectschematic";
    private static final String BUTTON_VIEW_CURRENT     = "viewschem";
    private static final String BOX_RESULTS             = "results";
    private static final String BOX_LEFT                = "left";
    private static final String BOX_RIGHT               = "right";
    private static final String LABEL_SCORE             = "score";
    private static final String LABEL_BLOCK_COUNTS      = "blockcounts";
    private static final String LABEL_SIZE              = "size";
    private static final String LABEL_BUILDINGS         = "buildings";
    private static final String BUTTON_SHOW_RES         = "showresources";
    private static final String BUTTON_CANCEL           = "cancel";
    private static final String LIST_RES                = "resources";
    private static final String LIST_ENTRY_ITEMICON     = "resourceIcon";
    private static final String LIST_ENTRY_LABEL        = "resourceName";
    private static final String LIST_ENTRY_COUNT        = "resourceCount";

    /**
     * Cached analyzed blueprints
     */
    public static Map<Blueprint, SchemAnalyzerUtil.SchematicAnalyzationResult> analyzationResults = new LinkedHashMap<>();

    /**
     * Sorted list of the analyzation results, for UI purposes to scroll through
     */
    private static List<SchemAnalyzerUtil.SchematicAnalyzationResult> sortedAnalyzationResults = new ArrayList<>();

    /**
     * Current selection for left/right schematic result display
     */
    private static SchemAnalyzerUtil.SchematicAnalyzationResult selectedLeft  = null;
    private static SchemAnalyzerUtil.SchematicAnalyzationResult selectedRight = null;

    public WindowSchematicAnalyzer()
    {
        super(ID);
        registerButton(BUTTON_CANCEL, b -> {
            close();
        });
        registerButton(BUTTON_SELECT_SCHEMATIC, b -> {
            new WindowExtendedBuildTool(
              BlockPos.containing(Minecraft.getInstance().player.position().add(Minecraft.getInstance().player.getLookAngle().multiply(10, 10, 10))),
              1,
              (window, blueprint) -> {
                  Minecraft.getInstance().setScreen(this.getScreen());
                  final SchemAnalyzerUtil.SchematicAnalyzationResult result = analyzationResults.computeIfAbsent(blueprint, SchemAnalyzerUtil::analyzeSchematic);
                  sortAnalyzationResults();
                  switchSelectionTo(getBoxForSide(b), result);
              },
              (a) -> true
            ).open();
        });

        registerButton(BUTTON_SELECTION_LEFT, b -> {
            switchSelection(b, false);
        });

        registerButton(BUTTON_SELECTION_RIGHT, b -> {
            switchSelection(b, true);
        });

        registerButton(BUTTON_SHOW_RES, this::showResourcesFor);

        if (ItemScanAnalyzer.blueprint != null)
        {
            analyzationResults.keySet().removeIf(blueprint -> blueprint.getName().equals(TEMP_SCAN));
            analyzationResults.put(ItemScanAnalyzer.blueprint, SchemAnalyzerUtil.analyzeSchematic(ItemScanAnalyzer.blueprint));
        }

        sortAnalyzationResults();

        for (SchemAnalyzerUtil.SchematicAnalyzationResult sortedAnalyzationResult : sortedAnalyzationResults)
        {
            if (sortedAnalyzationResult.blueprint.equals(ItemScanAnalyzer.blueprint))
            {
                selectedRight = sortedAnalyzationResult;
                ItemScanAnalyzer.blueprint = null;
                break;
            }
        }

        selectedLeft = getPrevFor(getNextFor(selectedLeft));

        switchSelectionTo(getLeftSide(), selectedLeft);
        switchSelectionTo(getRightSide(), selectedRight);
    }

    /**
     * Displays the resources list for a blueprint
     *
     * @param b
     */
    private void showResourcesFor(final Button b)
    {
        final ScrollingList resourceList = b.getParent().findPaneOfTypeByID(LIST_RES, ScrollingList.class);
        resourceList.setVisible(true);
        b.setVisible(false);
        SchemAnalyzerUtil.SchematicAnalyzationResult selected = getCurrentSelectionData(b);
        final List<ItemStorage> resources = new ArrayList<>(selected.differentBlocks);
        resources.sort(Comparator.comparingInt((ItemStorage itemStorage) -> itemStorage.getAmount() * itemStorage.getItemStack().getCount()).reversed());

        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return resources.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                final ItemStorage storage = resources.get(index);
                final Text resourceLabel = rowPane.findPaneOfTypeByID(LIST_ENTRY_LABEL, Text.class);
                final Text countLabel = rowPane.findPaneOfTypeByID(LIST_ENTRY_COUNT, Text.class);
                countLabel.setText(Component.literal(Integer.toString(storage.getAmount())).withStyle(ChatFormatting.YELLOW));
                PaneBuilders.tooltipBuilder().hoverPane(countLabel)
                  .append(Component.translatable("com.minecolonies.coremod.gui.analyzer.score",
                    storage.getItemStack().getCount(),
                    storage.getItemStack().getCount() * storage.getAmount()))
                  .build();
                resourceLabel.setText(storage.getItemStack().getHoverName());
                final ItemStack copy = storage.getItemStack().copy();
                copy.setCount(1);
                rowPane.findPaneOfTypeByID(LIST_ENTRY_ITEMICON, ItemIcon.class).setItem(copy);
            }
        });
    }

    /**
     * Get the left result display box
     *
     * @return
     */
    private Box getLeftSide()
    {
        return findPaneOfTypeByID(BOX_LEFT, Box.class);
    }

    /**
     * Get the right result display box
     *
     * @return
     */
    private Box getRightSide()
    {
        return findPaneOfTypeByID(BOX_RIGHT, Box.class);
    }

    /**
     * Find the right/left side result display box for the given pane
     *
     * @param context
     * @return
     */
    private Box getBoxForSide(final Pane context)
    {
        if (isLeft(context))
        {
            return getLeftSide();
        }
        return getRightSide();
    }

    /**
     * Check on which side the current element is
     *
     * @param current
     * @return
     */
    private boolean isLeft(final Pane current)
    {
        if (current.getID().equals(BOX_LEFT))
        {
            return true;
        }
        if (current.getParent() != null && current.getParent().getID().equals(BOX_LEFT))
        {
            return true;
        }

        if (current.getParent().getParent() != null && current.getParent().getParent().getID().equals(BOX_LEFT))
        {
            return true;
        }

        return false;
    }

    /**
     * Get the current selection data depending on side
     *
     * @param context
     * @return
     */
    private SchemAnalyzerUtil.SchematicAnalyzationResult getCurrentSelectionData(Pane context)
    {

        SchemAnalyzerUtil.SchematicAnalyzationResult selection;
        if (isLeft(context))
        {
            selection = selectedLeft;
        }
        else

        {
            selection = selectedRight;
        }

        return selection;
    }

    /**
     * Gets the next entry in the list
     *
     * @param current
     * @return
     */
    private SchemAnalyzerUtil.SchematicAnalyzationResult getNextFor(SchemAnalyzerUtil.SchematicAnalyzationResult current)
    {
        final int currentIndex = sortedAnalyzationResults.indexOf(current);
        if (current != null && currentIndex == -1 && !sortedAnalyzationResults.isEmpty())
        {
            return sortedAnalyzationResults.get(0);
        }

        if (currentIndex != -1 && currentIndex + 1 < sortedAnalyzationResults.size())
        {
            return sortedAnalyzationResults.get(currentIndex + 1);
        }

        return current;
    }

    /**
     * Gets the previous entry in the list
     *
     * @param current
     * @return
     */
    private SchemAnalyzerUtil.SchematicAnalyzationResult getPrevFor(SchemAnalyzerUtil.SchematicAnalyzationResult current)
    {
        final int currentIndex = sortedAnalyzationResults.indexOf(current);
        if (current != null && currentIndex == -1 && !sortedAnalyzationResults.isEmpty())
        {
            return sortedAnalyzationResults.get(0);
        }

        if (currentIndex - 1 >= 0 && currentIndex - 1 < sortedAnalyzationResults.size())
        {
            return sortedAnalyzationResults.get(currentIndex - 1);
        }

        return current;
    }

    /**
     * Sorts the results in the saved list
     */
    private void sortAnalyzationResults()
    {
        sortedAnalyzationResults = new ArrayList<>(analyzationResults.values());
        sortedAnalyzationResults.sort((o1, o2) -> o1.blueprint.getName().compareToIgnoreCase(o2.blueprint.getName()));
    }

    /**
     * Switches the selection to the next schematic
     *
     * @param buttonClicked
     * @param next
     */
    private void switchSelection(final Button buttonClicked, boolean next)
    {
        SchemAnalyzerUtil.SchematicAnalyzationResult result;
        if (next)
        {
            result = getNextFor(getCurrentSelectionData(buttonClicked));
        }
        else
        {
            result = getPrevFor(getCurrentSelectionData(buttonClicked));
        }

        switchSelectionTo(getBoxForSide(buttonClicked), result);
    }

    /**
     * Switches the selection to the given schematic
     *
     * @param next
     */
    private void switchSelectionTo(final Box parent, SchemAnalyzerUtil.SchematicAnalyzationResult next)
    {
        if (isLeft(parent))
        {
            selectedLeft = next;
        }
        else
        {
            selectedRight = next;
        }

        final Box box = parent.findPaneOfTypeByID(BOX_RESULTS, Box.class);
        if (box == null)
        {
            Log.getLogger().warn("Nonexisting pane");
            return;
        }

        if (next == null)
        {
            parent.findPaneOfTypeByID(BUTTON_VIEW_CURRENT, ButtonVanilla.class).setText(Component.literal("none"));
            box.hide();
            box.findPaneOfTypeByID(BUTTON_SHOW_RES, ButtonImage.class).setVisible(false);
            return;
        }

        box.show();

        String name = next.blueprint.getName();
        if (next.blueprint.getFileName() != null && next.blueprint.getFilePath() != null)
        {
            final String[] split = next.blueprint.getFileName().split("/");
            name = next.blueprint.getFilePath().toString().replace("blueprints/minecolonies/", "") + "/" + split[split.length - 1];
        }
        name = name.replace(".blueprint", "");
        parent.findPaneOfTypeByID(BUTTON_VIEW_CURRENT, ButtonVanilla.class).setText(Component.literal(name));

        box.findPaneOfTypeByID(LABEL_SCORE, Text.class)
          .setText(Component.translatable("com.minecolonies.coremod.gui.analyzer.complexity", Component.literal("" + next.costScore).withStyle(
            ChatFormatting.RED).withStyle(ChatFormatting.BOLD)));

        box.findPaneOfTypeByID(LABEL_BLOCK_COUNTS, Text.class)
          .setText(Component.translatable("com.minecolonies.coremod.gui.analyzer.blockcounts", Component.literal("" + next.differentBlocks.size()).withStyle(
            ChatFormatting.BLUE).withStyle(ChatFormatting.BOLD)));

        PaneBuilders.tooltipBuilder()
          .append(Component.translatable("com.minecolonies.coremod.gui.analyzer.score", next.differentBlocks.size() * 40, next.costScore))
          .hoverPane(box.findPaneOfTypeByID(LABEL_BLOCK_COUNTS, Text.class))
          .build();

        box.findPaneOfTypeByID(LABEL_SIZE, Text.class)
          .setText(Component.translatable("com.minecolonies.coremod.gui.analyzer.size", Component.literal("[" + next.blueprint.getSizeX() + " " + next.blueprint.getSizeY() + " "
                                                                                                            + next.blueprint.getSizeZ() + "]")
            .withStyle(ChatFormatting.YELLOW)
            .withStyle(ChatFormatting.BOLD)));
        box.findPaneOfTypeByID(LABEL_BUILDINGS, Text.class)
          .setText(Component.translatable("com.minecolonies.coremod.gui.analyzer.buildings",
            Component.literal("" + next.containedBuildings).withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD)));

        final ScrollingList resourceList = box.findPaneOfTypeByID(LIST_RES, ScrollingList.class);
        resourceList.setVisible(false);
        box.findPaneOfTypeByID(BUTTON_SHOW_RES, ButtonImage.class).setText(Component.translatable("com.ldtteam.structurize.gui.scantool.showres"));
        box.findPaneOfTypeByID(BUTTON_SHOW_RES, ButtonImage.class).setVisible(true);
    }

    @Override
    public void onClosed()
    {
        RenderingCache.removeBox("analyzer");
        super.onClosed();
    }
}
