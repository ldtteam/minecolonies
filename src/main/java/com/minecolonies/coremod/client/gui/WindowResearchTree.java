package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.Box;
import com.ldtteam.blockout.views.ZoomDragView;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import com.minecolonies.coremod.network.messages.server.colony.building.university.TryResearchMessage;
import com.minecolonies.coremod.research.AlternateBuildingResearchRequirement;
import com.minecolonies.coremod.research.BuildingResearchRequirement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window to display a particular branch of the tree.
 */
public class WindowResearchTree extends AbstractWindowSkeleton
{
    /**
     * The branch of this research.
     */
    private final String branch;

    /**
     * The university building.
     */
    private final BuildingUniversity.View building;

    /**
     * The previous window.
     */
    private final WindowHutUniversity last;

    /**
     * If has a max research for this branch already.
     */
    private boolean hasMax;

    /**
     * The undo button, if one is present.
     */
    private final ButtonImage undoButton = new ButtonImage();

    /**
     * The undo cost icons, if one is present.
     */
    private ItemIcon[] undoCostIcons = new ItemIcon[0];

    /**
     * Displaced tooltip text, if present.
     */
    private List<IFormattableTextComponent> displacedHoverText;

    /**
     * Button that the tooltip text has been displaced from.
     */
    private Button displacedButton;

    /**
     * Create the research tree window.
     *
     * @param branch   the branch being researched.
     * @param building the associated university.
     * @param last     the GUI we opened this from.
     */
    public WindowResearchTree(final String branch, final BuildingUniversity.View building, final WindowHutUniversity last)
    {
        super(Constants.MOD_ID + R_TREE_RESOURCE_SUFFIX, last);
        this.branch = branch;
        this.building = building;
        this.last = last;
        this.hasMax = false;

        final List<String> researchList = IGlobalResearchTree.getInstance().getPrimaryResearch(branch);
        this.hasMax = building.getColony().getResearchManager().getResearchTree().branchFinishedHighestLevel(branch);

        final ZoomDragView view = findPaneOfTypeByID(DRAG_VIEW_ID, ZoomDragView.class);

        drawTreeBackground(view);
        drawTree(0, 0, view, researchList, false);
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        super.onButtonClicked(button);

        // drawUndoProgressButton and drawUndoCompleteButton replace a Research's normal tooltip
        // and adds a button and icon(s) representing the cost of resetting the research.
        // See their respective functions for details on the how.
        // These branches remove those buttons from the DragTreeView, if present, on pressing any button.
        // That should occur no matter what buttons are pressed, even disabled buttons, as a "no, I don't want to".
        // We'll do that even for branches that will close the WindowResearchTree for now,
        // since down the road we may want to be able to cancel or start multiple researches without
        // closing and reopening the WindowResearchTree.
        if (button.getParent().getChildren().contains(undoButton))
        {
            button.getParent().removeChild(undoButton);
        }
        for(ItemIcon icon : undoCostIcons)
        {
            if(button.getParent().getChildren().contains(icon))
            {
                button.getParent().removeChild(icon);
            }
        }
        if(displacedButton != null && displacedHoverText != null)
        {
            displacedButton.setHoverToolTip(displacedHoverText);
        }

        // Check for an empty button Id.  These reflect, disabled buttons normally
        // but a sufficiently malformed datapack may also have a blank research id,
        // and we don't want to try to try to parse that.
        // May eventually want a sound handler here, but SoundUtils.playErrorSound is a bit much.
        if(button.getID().isEmpty())
        {
        }
        // Undo just the selected research.
        else if (button.getID().contains("undo."))
        {
            final ILocalResearch cancelResearch = building.getColony().getResearchManager().getResearchTree().getResearch(branch, button.getID().split("\\.")[1]);
            if (cancelResearch != null)
            {
                // Canceled research will eventually be removed from the local tree on synchronization from server,
                // But this can be long enough (~5 seconds) to cause confusion if the player reopens the WindowResearchTree.
                // Completely removing the research to null will allow players to unintentionally restart it.
                // While the server-side logic prevents this from taking items, it would be confusing.
                // Setting to NOT_STARTED means that it can't be sent, as only null  research states
                // are eligible to send TryResearchMessages, and only IN_PROGRESS, or FINISHED
                // are eligible to drawUndo buttons.
                cancelResearch.setState(ResearchState.NOT_STARTED);
                Network.getNetwork().sendToServer(new TryResearchMessage(building, cancelResearch.getId(), cancelResearch.getBranch(), true));
                close();
            }
        }
        else if (IGlobalResearchTree.getInstance().getResearch(branch, button.getID()) != null
                   && (building.getBuildingLevel() >= IGlobalResearchTree.getInstance().getResearch(branch, button.getID()).getDepth()
                         || building.getBuildingLevel() == building.getBuildingMaxLevel()))
        {
            final IGlobalResearch research = IGlobalResearchTree.getInstance().getResearch(branch, button.getID());
            final ILocalResearch localResearch = building.getColony().getResearchManager().getResearchTree().getResearch(branch, research.getId());
            if (localResearch == null && building.getBuildingLevel() > building.getColony().getResearchManager().getResearchTree().getResearchInProgress().size()&&
                  (research.hasEnoughResources(new InvWrapper(Minecraft.getInstance().player.inventory)) || (mc.player.isCreative())))
            {
                // This side won't actually start research; it'll be overridden the next colony update from the server.
                // It will, however, update for the next WindowResearchTree if the colony update is slow to come back.
                // Again, the server will prevent someone from paying items twice, but this avoids some confusion.
                research.startResearch(building.getColony().getResearchManager().getResearchTree());
                Network.getNetwork().sendToServer(new TryResearchMessage(building, research.getId(), research.getBranch(), false));
                close();
            }
            else if (localResearch != null)
            {
                if (localResearch.getState() == ResearchState.IN_PROGRESS)
                {
                    drawUndoProgressButton(button);
                }
                if (localResearch.getState() == ResearchState.FINISHED)
                {
                    if(hasMax && research.getDepth() > building.getBuildingMaxLevel() && building.getBuildingLevel() == building.getBuildingMaxLevel() && !research.isImmutable())
                    {
                        drawUndoCompleteButton(button);
                    }
                    for(String childId : research.getChildren())
                    {
                        if(building.getColony().getResearchManager().getResearchTree().getResearch(branch, childId) != null
                          && building.getColony().getResearchManager().getResearchTree().getResearch(branch, childId).getState() != ResearchState.NOT_STARTED)
                        {
                            return;
                        }
                    }
                    String parentId = IGlobalResearchTree.getInstance().getResearch(branch, research.getId()).getParent();
                    while(!parentId.isEmpty())
                    {
                        if (IGlobalResearchTree.getInstance().getResearch(branch, parentId) != null
                              && IGlobalResearchTree.getInstance().getResearch(branch, parentId).hasOnlyChild())
                        {
                            drawUndoCompleteButton(button);
                            break;
                        }
                        parentId = IGlobalResearchTree.getInstance().getResearch(branch, parentId).getParent();
                    }
                }
            }
        }
        // Cancel the entire WindowResearchTree
        else if (button.getID().equals("cancel"))
        {
            this.close();
            last.open();
        }
    }

    /**
     * Draw the tree of research.
     *
     * @param height                 the start y offset.
     * @param depth                  the current depth.
     * @param view                   the view to append it to.
     * @param researchList           the list of research to go through.
     * @param abandoned              if abandoned child.
     * @return the next y offset.
     */
    public int drawTree(
      final int height,
      final int depth,
      final ZoomDragView view,
      final List<String> researchList,
      final boolean abandoned)
    {
        int nextHeight = height;
        for (int i = 0; i < researchList.size(); i++)
        {
            if(i > 0)
            {
                nextHeight++;
            }
            final String researchLabel = researchList.get(i);

            final IGlobalResearch research = IGlobalResearchTree.getInstance().getResearch(branch, researchLabel);
            if (research.isHidden() && !IGlobalResearchTree.getInstance().isResearchRequirementsFulfilled(research.getResearchRequirement(), this.building.getColony()))
            {
                continue;
            }

            final int offsetX = (depth * (GRADIENT_WIDTH + X_SPACING));
            final int offsetY = nextHeight * (GRADIENT_HEIGHT + Y_SPACING) + Y_SPACING + TIMELABEL_Y_POSITION;

            final boolean trueAbandoned = drawResearchItem(view, offsetX, offsetY, research, abandoned);

            if (!research.getParent().isEmpty())
            {
                drawArrows(view, offsetX + INITIAL_X_OFFSET - X_SPACING, offsetY, researchList, research.getParent(), i, nextHeight, height);
            }
            if (!research.getChildren().isEmpty())
            {
                nextHeight =
                  drawTree(nextHeight, depth + 1, view, research.getChildren(), trueAbandoned);
            }
        }

        return nextHeight;
    }

    /**
     * Draw the background gradients and labels for the research tree.
     *
     * @param view the view to append it to.
     */
    private void drawTreeBackground(final ZoomDragView view)
    {
        for (int i = 1; i <= MAX_DEPTH; i++)
        {
            final Label timeLabel = new Label();
            timeLabel.setSize(TIME_WIDTH, TIME_HEIGHT);
            timeLabel.setLabelText(new TranslationTextComponent("com.minecolonies.coremod.gui.research.tier.header",
              (i > building.getBuildingMaxLevel()) ? i : building.getBuildingMaxLevel(),
              (IGlobalResearchTree.getInstance().getBranchTime(branch) * Math.pow(2, i - 1))));
            timeLabel.setPosition((i - 1) * (GRADIENT_WIDTH + X_SPACING) + GRADIENT_WIDTH / 2 - TIME_WIDTH / 4, TIMELABEL_Y_POSITION);
            if (building.getBuildingLevel() < i && (building.getBuildingLevel() != building.getBuildingMaxLevel() || hasMax))
            {
                final Gradient gradient = new Gradient();
                gradient.setGradientStart(80, 80, 80, 70);
                gradient.setGradientEnd(60, 60, 60, 70);
                gradient.setSize(1200, 1200);
                gradient.setPosition((i - 1) * (GRADIENT_WIDTH + X_SPACING), 0);
                view.addChild(gradient);
                timeLabel.setColor(COLOR_TEXT_NEGATIVE, COLOR_TEXT_NEGATIVE);
            }
            else
            {
                timeLabel.setColor(COLOR_TEXT_LABEL, COLOR_TEXT_LABEL);
            }
            view.addChild(timeLabel);
        }
    }

    /**
     * Draw the entirety of an individual research item on a tree, including icons and tooltips.
     *
     * @param view                  the view to append it to.
     * @param offsetX               the horizontal offset of the left side of the research block.
     * @param offsetY               the vertical offset of the top side of the research block.
     * @param research              the global research characteristics to draw.
     * @param abandoned             the abandoned status of the parent of the research, if one is present.
     * @return abandoned status, true if the research is blocked in the local colony the completion of a sibling research, or an ancestor's sibling's research.
     */
    private boolean drawResearchItem(final ZoomDragView view, final int offsetX, final int offsetY, final IGlobalResearch research, boolean abandoned)
    {
        final ILocalResearchTree tree = building.getColony().getResearchManager().getResearchTree();
        final boolean parentResearched = tree.hasCompletedResearch(research.getParent());
        final IGlobalResearch parentResearch = IGlobalResearchTree.getInstance().getResearch(branch, research.getParent());
        final ResearchState state = tree.getResearch(branch, research.getId()) == null ? ResearchState.NOT_STARTED : tree.getResearch(branch, research.getId()).getState();

        if (research.getDepth() != 1 && state != ResearchState.FINISHED
              && parentResearch.hasResearchedChild(tree) && parentResearch.hasOnlyChild())
        {
            abandoned = true;
        }

        drawResearchBox(view, offsetX, offsetY, state, research.getDepth(), parentResearched, abandoned);
        if (state == ResearchState.IN_PROGRESS)
        {
            drawResearchProgress(view, offsetX, offsetY, research, tree.getResearch(branch, research.getId()).getDepth());
        }

        drawResearchLabels(view, offsetX, offsetY, research, parentResearched);
        drawResearchButton(view, offsetX, offsetY, research, state, parentResearched, abandoned);
        drawResearchIcons(view, offsetX, offsetY, research, state != ResearchState.NOT_STARTED, abandoned);

        return abandoned;
    }

    /**
     * Draw the container block of an individual research item on a tree.
     *
     * @param view             the view to append it to.
     * @param offsetX          the horizontal offset of the left side of the research block.
     * @param offsetY          the vertical offset of the top side of the research block.
     * @param state            the status of the selected research.
     * @param researchLevel    the research's university level tier.
     * @param parentResearched if the parent
     * @param abandoned        the abandoned status of the research.
     */
    private void drawResearchBox(final ZoomDragView view, final int offsetX, final int offsetY, final ResearchState state, final int researchLevel, final boolean parentResearched, final boolean abandoned)
    {
        final Gradient gradient = new Gradient();
        gradient.setSize(GRADIENT_WIDTH, GRADIENT_HEIGHT);
        gradient.setPosition(offsetX + INITIAL_X_OFFSET, offsetY);
        if (state == ResearchState.IN_PROGRESS)
        {
            gradient.setGradientStart(227, 249, 184, 255);
            gradient.setGradientEnd(227, 249, 184, 255);
            view.addChild(gradient);
        }
        else if (state == ResearchState.FINISHED)
        {
            gradient.setGradientStart(102, 225, 80, 30);
            gradient.setGradientEnd(102, 225, 80, 30);
            view.addChild(gradient);
        }
        else if (abandoned)
        {
            gradient.setGradientStart(191, 184, 172, 255);
            gradient.setGradientEnd(191, 184, 172, 255);
            view.addChild(gradient);
        }
        else if (!parentResearched ||
                   (researchLevel > building.getBuildingLevel() && !(researchLevel > building.getBuildingMaxLevel()
                                                                       && !hasMax && building.getBuildingLevel() == building.getBuildingMaxLevel())))
        {
            gradient.setGradientStart(80, 80, 80, 100);
            gradient.setGradientEnd(70, 70, 70, 100);
            view.addChild(gradient);
        }
        else
        {
            gradient.setGradientStart(102, 204, 255, 255);
            gradient.setGradientEnd(102, 204, 255, 255);
            view.addChild(gradient);
        }

        final Box box = new Box();
        box.setColor(218, 202, 171);
        box.setSize(GRADIENT_WIDTH, GRADIENT_HEIGHT);
        box.setPosition(offsetX + INITIAL_X_OFFSET, offsetY);
        view.addChild(box);
    }

    /**
     * Generates and attaches tooltips for a given research to input tipItem.
     *
     * @param tipItem  the Pane to apply the tooltip.
     * @param research the global research characteristics to draw.
     * @param state    the status of the selected research.
     */
    private void generateResearchTooltips(final Pane tipItem, final IGlobalResearch research, final ResearchState state)
    {
        final List<IFormattableTextComponent> hoverTexts = new ArrayList<>();
        hoverTexts.add(new TranslationTextComponent(research.getDesc()).setStyle(Style.EMPTY.setBold(true).setFormatting(TextFormatting.GOLD)));
        if(!research.getSubtitle().isEmpty())
        {
            hoverTexts.add(new TranslationTextComponent(research.getSubtitle()).setStyle(Style.EMPTY.setItalic(true).setFormatting(TextFormatting.GRAY)));
        }
        for (int txt = 0; txt < research.getEffects().size(); txt++)
        {
            hoverTexts.add(research.getEffects().get(txt).getDesc());
            if(!research.getEffects().get(txt).getSubtitle().isEmpty())
            {
                hoverTexts.add(new StringTextComponent("-").append(new TranslationTextComponent(research.getEffects().get(txt).getSubtitle())));
            }
        }
        if (state != ResearchState.FINISHED)
        {
            for (int txt = 0; txt < research.getResearchRequirement().size(); txt++)
            {
                if(research.getResearchRequirement().get(txt).isFulfilled(this.building.getColony()))
                {
                    hoverTexts.add(new TranslationTextComponent(" - ").append(research.getResearchRequirement().get(txt).getDesc().setStyle(Style.EMPTY.setFormatting(TextFormatting.AQUA))));
                }
                else
                {
                    hoverTexts.add(new TranslationTextComponent(" - ").append(research.getResearchRequirement().get(txt).getDesc().setStyle(Style.EMPTY.setFormatting(TextFormatting.RED))));
                }
            }
            if (research.getDepth() > building.getBuildingLevel() && building.getBuildingLevel() != building.getBuildingMaxLevel())
            {
                hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.requirement.university.level", research.getDepth()));
            }
            if (research.getDepth() == MAX_DEPTH)
            {
                if(hasMax)
                {
                    hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.limit.onemaxperbranch").setStyle(Style.EMPTY.setFormatting(TextFormatting.GOLD)));
                }
                else
                {
                    hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.limit.onemaxperbranch").setStyle(Style.EMPTY.setFormatting(TextFormatting.RED)));
                }
            }
        }
        if (research.isImmutable())
        {
            hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.limit.immutable").setStyle(Style.EMPTY.setFormatting(TextFormatting.RED)));
        }
        tipItem.setHoverToolTip(hoverTexts);
    }

    /**
     * Draw the progress bar for a given research.
     *
     * @param view     the view to append it to.
     * @param offsetX  the horizontal offset of the left side of the research block.
     * @param offsetY  the vertical offset of the top side of the research block.
     * @param research the global research characteristics to draw.
     * @param progress the current research progress.
     */
    private void drawResearchProgress(final ZoomDragView view, final int offsetX, final int offsetY, final IGlobalResearch research, final int progress)
    {
        //The player will reach the end of the research if he is in creative mode, auto-completion is enabled, and the research was in progress
        if (mc.player.isCreative() && MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchCreativeCompletion.get()
              && progress < BASE_RESEARCH_TIME * IGlobalResearchTree.getInstance().getBranchTime(branch) * Math.pow(2, research.getDepth() - 1))
        {
            Network.getNetwork().sendToServer(new TryResearchMessage(building, research.getId(), research.getBranch(), false));
        }

        //Calculates how much percent of the next level has been completed.
        final double progressRatio = (progress + 1) / (Math.pow(2, research.getDepth() - 1) * (double) BASE_RESEARCH_TIME * IGlobalResearchTree.getInstance().getBranchTime(branch)) * 100;

        @NotNull final Image xpBar = new Image();
        xpBar.setImage(Screen.GUI_ICONS_LOCATION, XP_BAR_ICON_COLUMN, XP_BAR_EMPTY_ROW, XP_BAR_WIDTH, XP_HEIGHT, false);
        xpBar.setPosition(offsetX + X_SPACING - TEXT_X_OFFSET, offsetY + INITIAL_Y_OFFSET + +XPBAR_Y_OFFSET);

        @NotNull final Image xpBar2 = new Image();
        xpBar2.setImage(Screen.GUI_ICONS_LOCATION, XP_BAR_ICON_COLUMN_END, XP_BAR_EMPTY_ROW, XP_BAR_ICON_COLUMN_END_WIDTH, XP_HEIGHT, false);
        xpBar2.setPosition(XPBAR_LENGTH + offsetX + X_SPACING - TEXT_X_OFFSET, offsetY + INITIAL_Y_OFFSET + XPBAR_Y_OFFSET);

        view.addChild(xpBar);
        view.addChild(xpBar2);

        if (progressRatio > 0)
        {
            @NotNull final Image xpBarFull = new Image();
            xpBarFull.setImage(Screen.GUI_ICONS_LOCATION, XP_BAR_ICON_COLUMN, XP_BAR_FULL_ROW, (int) progressRatio, XP_HEIGHT, false);
            xpBarFull.setPosition(offsetX + X_SPACING - TEXT_X_OFFSET, offsetY + INITIAL_Y_OFFSET + XPBAR_Y_OFFSET);
            view.addChild(xpBarFull);
        }
    }

    /**
     * Draw the labels for a given research
     *
     * @param view             the view to append it to.
     * @param offsetX          the horizontal offset of the left side of the research block.
     * @param offsetY          the vertical offset of the top side of the research block.
     * @param research         the global research characteristics to draw.
     * @param parentResearched if the parent research has been completed.
     */
    private void drawResearchLabels(final ZoomDragView view, final int offsetX, final int offsetY, final IGlobalResearch research, final boolean parentResearched)
    {
        final Label nameLabel = new Label();
        nameLabel.setSize(BUTTON_LENGTH, INITIAL_Y_OFFSET);
        nameLabel.setLabelText(new TranslationTextComponent(research.getDesc()));
        nameLabel.setPosition(offsetX + INITIAL_X_OFFSET + NAME_OFFSET, offsetY + (NAME_OFFSET / 2));
        if (parentResearched)
        {
            nameLabel.setColor(COLOR_TEXT_LABEL, COLOR_TEXT_LABEL);
        }
        else
        {
            nameLabel.setColor(COLOR_TEXT_LIGHT, COLOR_TEXT_LIGHT);
        }
        view.addChild(nameLabel);

        if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get() && !research.getEffects().isEmpty())
        {
            final TranslationTextComponent effectText = new TranslationTextComponent("");
            for (int txt = 0; txt < research.getEffects().size(); txt++)
            {
                effectText.append(research.getEffects().get(txt).getDesc());
            }
            final Label effectLabel = new Label();
            effectLabel.setSize(BUTTON_LENGTH, INITIAL_Y_OFFSET);
            effectLabel.setPosition(offsetX + INITIAL_X_OFFSET + 2 * TEXT_X_OFFSET, offsetY + INITIAL_Y_OFFSET + nameLabel.getHeight() * 2 + INITIAL_Y_OFFSET + INITIAL_Y_OFFSET);
            effectLabel.setColor(COLOR_TEXT_LABEL, COLOR_TEXT_LABEL);
            effectLabel.setLabelText(effectText);
            view.addChild(effectLabel);
        }
        if (MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchDebugLog.get()
              && !research.getResearchRequirement().isEmpty())
        {
            final TranslationTextComponent requirementText = new TranslationTextComponent("");
            for (int txt = 0; txt < research.getResearchRequirement().size(); txt++)
            {
                requirementText.append(research.getResearchRequirement().get(txt).getDesc());
            }
            final Label requirementLabel = new Label();
            requirementLabel.setSize(BUTTON_LENGTH, INITIAL_Y_OFFSET);
            requirementLabel.setPosition(offsetX + INITIAL_X_OFFSET + TEXT_X_OFFSET, offsetY + INITIAL_Y_OFFSET + INITIAL_Y_OFFSET);
            requirementLabel.setColor(COLOR_TEXT_LABEL);
            requirementLabel.setLabelText(requirementText);
            view.addChild(requirementLabel);
        }
    }

    /**
     * Draw the buttons and button images for a given research.
     *
     * @param view              the view to append it to.
     * @param offsetX           the horizontal offset of the left side of the research block.
     * @param offsetY           the vertical offset of the top side of the research block.
     * @param research          the global research characteristics to draw.
     * @param state             the research state.
     * @param parentResearched  if the parent research has been completed.
     * @param abandoned         if the research or an ancestor research has a completed sibling preventing it from being studied.
     */
    private void drawResearchButton(final ZoomDragView view, final int offsetX, final int offsetY, final IGlobalResearch research,
                                    final ResearchState state, final boolean parentResearched, final boolean abandoned)
    {
        final Button button = new Button();
        button.setSize(GRADIENT_WIDTH, GRADIENT_HEIGHT);
        button.setPosition(offsetX + INITIAL_X_OFFSET, offsetY);
        if(!abandoned && parentResearched &&
             (research.getDepth() <= building.getBuildingLevel() || state != ResearchState.NOT_STARTED)
                ||  (!hasMax && research.getDepth() >= building.getBuildingMaxLevel() && building.getBuildingLevel() == building.getBuildingMaxLevel()))
        {
            button.setID(research.getId());
        }
        view.addChild(button);
        generateResearchTooltips(button, research, state);

        if (mc.player.isCreative() || state != ResearchState.NOT_STARTED || abandoned || !parentResearched)
        {
            return;
        }


        final ButtonImage buttonImage = new ButtonImage();
        buttonImage.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
        buttonImage.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.research"));
        buttonImage.setTextColor(Color.getByName("black", 0));
        buttonImage.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
        buttonImage.setPosition(offsetX + INITIAL_X_OFFSET + TEXT_X_OFFSET,
          offsetY + GRADIENT_HEIGHT - INITIAL_Y_OFFSET);

        if (building.getBuildingLevel() <= building.getColony().getResearchManager().getResearchTree().getResearchInProgress().size())
        {
            buttonImage.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_DIS));
            buttonImage.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.research.toomanyinprogress.1"));
            final ButtonImage buttonImage2 = new ButtonImage();
            buttonImage2.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_DIS));
            buttonImage2.setTextColor(Color.getByName("black", 0));
            buttonImage2.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
            buttonImage2.setPosition(offsetX + INITIAL_X_OFFSET + TEXT_X_OFFSET,
              offsetY + GRADIENT_HEIGHT - INITIAL_Y_OFFSET + BUTTON_HEIGHT);
            buttonImage2.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.research.toomanyinprogress.2"));
            view.addChild(buttonImage2);
        }
        else if (!IGlobalResearchTree.getInstance().isResearchRequirementsFulfilled(research.getResearchRequirement(), this.building.getColony()))
        {
            buttonImage.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_DIS));
            buttonImage.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.research.missingrequirements"));
        }
        else if (!research.hasEnoughResources(new InvWrapper(Minecraft.getInstance().player.inventory)))
        {
            buttonImage.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_DIS));
            buttonImage.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.research.notenoughresources"));
        }
        else if (research.getDepth() > building.getBuildingLevel() && building.getBuildingLevel() != building.getBuildingMaxLevel())
        {
            buttonImage.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_DIS));
            buttonImage.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.research.buildingleveltoolow"));
        }
        else if (research.getDepth() > building.getBuildingMaxLevel()
                   && building.getBuildingLevel() == building.getBuildingMaxLevel()
                   && hasMax)
        {
            buttonImage.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_DIS));
            buttonImage.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.research.maxunlocked"));
        }
        else
        {
            buttonImage.setID(research.getId());
        }
        view.addChild(buttonImage);
    }

    /**
     * Draw an undo button in the middle of the parent research, and manages associated tooltips for in-progress research.
     * This function sets normal button and tooltip information into the displacedButton and displacedHoverText fields, to allow
     * switch back to normal functionality without having to redraw the entire tree.
     *
     * @param parent            the parent button to attach it to.
     */
    private void drawUndoProgressButton(final Button parent)
    {
        undoButton.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
        undoButton.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.undo.progress"));
        undoButton.setTextColor(Color.getByName("black", 0));
        undoButton.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
        undoButton.setPosition(parent.getX() + (GRADIENT_WIDTH - BUTTON_LENGTH) / 2, parent.getY() + (GRADIENT_HEIGHT - BUTTON_HEIGHT) / 2);
        undoButton.setID("undo." + parent.getID());
        List<IFormattableTextComponent> hoverTexts = new ArrayList<>();
        hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.undo.progress.tooltip.1").setStyle(Style.EMPTY.setFormatting(TextFormatting.BOLD)
                                                                                                                            .setFormatting(TextFormatting.RED)));
        hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.undo.progress.tooltip.2").setStyle(Style.EMPTY.setFormatting(TextFormatting.BOLD)
                                                                                                                            .setFormatting(TextFormatting.RED)));
        displacedButton = parent;
        displacedHoverText = new ArrayList<>(parent.getHoverToolTip());
        parent.getHoverToolTip().clear();
        parent.setHoverToolTip(hoverTexts);
        parent.getParent().addChild(undoButton);
    }

    /**
     * Draw an undo button in the middle of the parent research, and manages associated tooltips for completed research.
     * This function sets normal button and tooltip information into the displacedButton and displacedHoverText fields, to allow
     * switch back to normal functionality without having to redraw the entire tree.
     *
     * @param parent            the parent button to attach it to.
     */
    private void drawUndoCompleteButton(final Button parent)
    {
        final List<? extends String> costList = IGlobalResearchTree.getInstance().getResearchResetCosts();
        undoCostIcons = new ItemIcon[costList.size()];
        boolean missingItems = false;
        for (int i = 0; i < costList.size(); i++)
        {
            final String[] costParts = costList.get(0).split(":");
            undoCostIcons[i] = new ItemIcon();
            ItemStack is = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(costParts[0], costParts[1])));
            if (InventoryUtils.getItemCountInItemHandler(new InvWrapper(Minecraft.getInstance().player.inventory),
              stack -> !ItemStackUtils.isEmpty(stack) && stack.isItemEqual(is)) < Integer.parseInt(costParts[2]))
            {
                missingItems = true;
            }
            is.setCount(Integer.parseInt(costParts[2]));
            undoCostIcons[i].setItem(is);
            undoCostIcons[i].setPosition(parent.getX() + (GRADIENT_WIDTH - BUTTON_LENGTH) / 2 + BUTTON_LENGTH + DEFAULT_COST_SIZE * i,
              parent.getY() + (GRADIENT_HEIGHT - BUTTON_HEIGHT) / 2);
            undoCostIcons[i].setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
            parent.getParent().addChild(undoCostIcons[0]);
        }
        undoButton.setTextColor(Color.getByName("black", 0));
        undoButton.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
        undoButton.setPosition(parent.getX() + (GRADIENT_WIDTH - BUTTON_LENGTH) / 2, parent.getY() + (GRADIENT_HEIGHT - BUTTON_HEIGHT) / 2);
        final List<IFormattableTextComponent> hoverTexts = new ArrayList<>();
        hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.undo.remove.tooltip.1").setStyle(Style.EMPTY.setFormatting(TextFormatting.BOLD)
                                                                                                                          .setFormatting(TextFormatting.RED)));
        hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.undo.remove.tooltip.2").setStyle(Style.EMPTY.setFormatting(TextFormatting.BOLD)
                                                                                                                          .setFormatting(TextFormatting.RED)));
        if (missingItems)
        {
            undoButton.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_DIS));
            undoButton.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.research.notenoughresources"));
            for(String cost : IGlobalResearchTree.getInstance().getResearchResetCosts())
            {
                final String[] costParts = cost.split(":");
                hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.requirement.research",
                  ForgeRegistries.ITEMS.getValue(new ResourceLocation(costParts[0], costParts[1])).getName()));
            }
        }
        else
        {
            undoButton.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
            undoButton.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.undo.remove"));
            undoButton.setID("undo." + parent.getID());
        }

        displacedButton = parent;
        displacedHoverText = new ArrayList<>(parent.getHoverToolTip());
        parent.getHoverToolTip().clear();
        parent.setHoverToolTip(hoverTexts);
        parent.getParent().addChild(undoButton);
    }

    /**
     * Draw the progress bar for a given research.
     *
     * @param view       the view to append it to.
     * @param offsetX    the horizontal offset of the left side of the research block.
     * @param offsetY    the vertical offset of the top side of the research block.
     * @param research   the global research characteristics to draw.
     * @param started    if the research has been started, including if it is complete or canceled.
     * @param abandoned  if the research can not be initiated, because of a completed sibling or ancestor's sibling research.
     */
    public void drawResearchIcons(final ZoomDragView view, final int offsetX, final int offsetY, final IGlobalResearch research, final boolean started, final boolean abandoned)
    {
        if (!abandoned)
        {
            int storageOffset = -8;
            for (final IResearchRequirement requirement : research.getResearchRequirement())
            {
                if (requirement instanceof BuildingResearchRequirement)
                {
                    final Item item;
                    if (IMinecoloniesAPI.getInstance().getBuildingRegistry().containsKey(
                      new ResourceLocation(Constants.MOD_ID, ((BuildingResearchRequirement) requirement).getBuilding())))
                    {
                        item = IMinecoloniesAPI.getInstance().getBuildingRegistry().getValue(
                          new ResourceLocation(Constants.MOD_ID, ((BuildingResearchRequirement) requirement).getBuilding())).getBuildingBlock().asItem();
                    }
                    else
                    {
                        item = Items.AIR.asItem();
                    }
                    final ItemStack stack = new ItemStack(item);
                    stack.setCount(((BuildingResearchRequirement) requirement).getBuildingLevel());
                    final ItemIcon icon = new ItemIcon();
                    icon.setItem(stack);
                    icon.setPosition(offsetX + INITIAL_X_OFFSET + TEXT_X_OFFSET, offsetY + storageOffset);
                    icon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                    view.addChild(icon);

                    storageOffset += COST_OFFSET;
                }
            }


            final int startingStorageOffset = storageOffset;
            for (final IResearchRequirement requirement : research.getResearchRequirement())
            {
                if (requirement instanceof AlternateBuildingResearchRequirement)
                {
                    final Iterator<Map.Entry<String, Integer>> iterator = ((AlternateBuildingResearchRequirement) requirement).getBuildings().entrySet().iterator();
                    while (iterator.hasNext())
                    {
                        final Map.Entry<String, Integer> building = iterator.next();
                        final Item item;
                        if (IMinecoloniesAPI.getInstance().getBuildingRegistry().containsKey(
                          new ResourceLocation(Constants.MOD_ID, building.getKey())))
                        {
                            item = IMinecoloniesAPI.getInstance().getBuildingRegistry().getValue(
                              new ResourceLocation(Constants.MOD_ID, building.getKey())).getBuildingBlock().asItem();
                        }
                        else
                        {
                            item = Items.AIR.asItem();
                        }
                        final ItemStack stack = new ItemStack(item);
                        stack.setCount(building.getValue());
                        final ItemIcon icon = new ItemIcon();
                        icon.setItem(stack);
                        icon.setPosition(offsetX + INITIAL_X_OFFSET + TEXT_X_OFFSET, offsetY + storageOffset);
                        icon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                        view.addChild(icon);

                        if (iterator.hasNext())
                        {
                            final Label orLabel = new Label();
                            orLabel.setPosition(offsetX + INITIAL_X_OFFSET + TEXT_X_OFFSET + DEFAULT_COST_SIZE + 2, offsetY + storageOffset);
                            orLabel.setSize(OR_WIDTH, OR_HEIGHT);
                            orLabel.setColor(Color.getByName("black", 0), Color.getByName("black", 0));
                            orLabel.setLabelText(new TranslationTextComponent("com.minecolonies.coremod.research.research.or"));
                            view.addChild(orLabel);
                        }
                        storageOffset += COST_OFFSET;
                    }
                }
            }
            if (startingStorageOffset != storageOffset)
            {
                final Box altrequirementbox = new Box();
                altrequirementbox.setColor(218, 202, 171);
                altrequirementbox.setSize(DEFAULT_COST_SIZE, storageOffset - startingStorageOffset);
                altrequirementbox.setPosition(offsetX + INITIAL_X_OFFSET + ICON_X_OFFSET, offsetY + startingStorageOffset - ICON_Y_OFFSET);
                view.addChild(altrequirementbox);
            }

            if(!started)
            {
                storageOffset = -8;
                for (final ItemStorage storage : research.getCostList())
                {
                    final ItemStack stack = storage.getItemStack().copy();
                    stack.setCount(storage.getAmount());
                    final ItemIcon icon = new ItemIcon();
                    icon.setItem(stack);
                    icon.setPosition(offsetX + INITIAL_X_OFFSET + GRADIENT_WIDTH - COST_OFFSET, offsetY + storageOffset);
                    icon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                    view.addChild(icon);

                    storageOffset += COST_OFFSET;
                }
            }
        }
        if (!research.getIcon().isEmpty())
        {
            final String[] iconParts = research.getIcon().split(":");
            if (research.getIcon().contains("."))
            {
                final Image icon = new Image();
                icon.setImage(new ResourceLocation(iconParts[0], iconParts[1]));
                icon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                icon.setPosition(offsetX + GRADIENT_WIDTH / 2, offsetY - (COST_OFFSET / 2));
                view.addChild(icon);
            }
            else
            {
                ItemIcon iconItem = new ItemIcon();
                if (iconParts.length == 2 || iconParts.length == 3)
                {
                    ItemStack is = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(iconParts[0], iconParts[1])));
                    iconItem.setItem(is);
                    if (iconParts.length == 3)
                    {
                        is.setCount(Integer.parseInt(iconParts[2]));
                    }
                }
                iconItem.setPosition(offsetX + GRADIENT_WIDTH / 2, offsetY - (COST_OFFSET / 2));
                iconItem.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                view.addChild(iconItem);
            }
        }
        if (research.isImmutable() && !started)
        {
            final Image immutIcon = new Image();
            immutIcon.setImage(new ResourceLocation("minecraft", "textures/block/redstone_torch.png"));
            immutIcon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
            immutIcon.setPosition(offsetX + GRADIENT_WIDTH / 5, offsetY + GRADIENT_HEIGHT - DEFAULT_COST_SIZE);
            view.addChild(immutIcon);
        }
    }

    /**
     * Draws arrows connecting sibling researches to their parent.
     *
     * @param view           the view to append it to.
     * @param offsetX        the horizontal offset of the left side of the research block.
     * @param offsetY        the vertical offset of the top side of the research block.
     * @param researchList   the list of sibling researches to connect by arrows.
     * @param parentResearch the parent research to connect by arrow.
     * @param currentCounter count of the current target.
     * @param nextHeight     height of the next arrow target.
     * @param parentHeight	 height of the parent arrow target.
     */
    public void drawArrows(final ZoomDragView view, final int offsetX, final int offsetY, final List<String> researchList, final String parentResearch, final int currentCounter, final int nextHeight, final int parentHeight)
    {
        final boolean firstSibling = currentCounter == 0;
        final boolean secondSibling = currentCounter >= 1;

        final boolean lastSibling = currentCounter + 1 >= researchList.size();

        if (firstSibling && lastSibling)
        {
            final Image corner = new Image();
            corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right.png"));
            corner.setSize(X_SPACING, GRADIENT_HEIGHT);
            corner.setPosition(offsetX, offsetY);
            view.addChild(corner);
        }
        else
        {
            if (secondSibling)
            {
                for (int dif = 1; dif < nextHeight - parentHeight; dif++)
                {
                    final Image corner = new Image();
                    corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_down.png"));
                    corner.setSize(X_SPACING, GRADIENT_HEIGHT + Y_SPACING);
                    corner.setPosition(offsetX, offsetY - (dif * corner.getHeight()));
                    view.addChild(corner);
                }
            }

            if (firstSibling)
            {
                final Image corner = new Image();
                corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right_down.png"));
                corner.setSize(X_SPACING, GRADIENT_HEIGHT + Y_SPACING);
                corner.setPosition(offsetX, offsetY);
                view.addChild(corner);
            }
            else
            {
                if (IGlobalResearchTree.getInstance().getResearch(branch, parentResearch).hasOnlyChild())
                {
                    final Label orLabel = new Label();
                    orLabel.setSize(OR_WIDTH, OR_HEIGHT);
                    orLabel.setColor(Color.getByName("black", 0), Color.getByName("black", 0));
                    orLabel.setLabelText(new TranslationTextComponent("com.minecolonies.coremod.research.research.or"));
                    orLabel.setPosition(offsetX + OR_X_OFFSET, offsetY);
                    view.addChild(orLabel);

                    if (lastSibling)
                    {
                        final Image circle = new Image();
                        circle.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_or.png"));
                        circle.setSize(X_SPACING, GRADIENT_HEIGHT);
                        circle.setPosition(offsetX, offsetY);
                        view.addChild(circle);
                    }
                    else
                    {
                        final Image corner = new Image();
                        corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_or_down.png"));
                        corner.setSize(X_SPACING, GRADIENT_HEIGHT + Y_SPACING);
                        corner.setPosition(offsetX, offsetY);
                        view.addChild(corner);
                    }
                }
                else
                {
                    if (lastSibling)
                    {
                        final Image corner = new Image();
                        corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right_and.png"));
                        corner.setSize(X_SPACING, GRADIENT_HEIGHT);
                        corner.setPosition(offsetX, offsetY);
                        view.addChild(corner);
                    }
                    else
                    {
                        final Image corner = new Image();
                        corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right_and_more.png"));
                        corner.setSize(X_SPACING, GRADIENT_HEIGHT + Y_SPACING);
                        corner.setPosition(offsetX, offsetY);
                        view.addChild(corner);
                    }
                }
            }
        }
    }
}

