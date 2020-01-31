package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.Box;
import com.ldtteam.blockout.views.DragView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.interfaces.IGlobalResearch;
import com.minecolonies.api.research.interfaces.ILocalResearch;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import com.minecolonies.coremod.network.messages.TryResearchMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    private final BuildingUniversity.View  building;

    /**
     * The previous window.
     */
    private final WindowHutUniversity last;

    /**
     * Create the research tree window.
     * @param branch the branch being researched.
     * @param building the associated university.
     * @param last the GUI we opened this from.
     */
    public WindowResearchTree(final String branch, final BuildingUniversity.View building, final WindowHutUniversity last)
    {
        super(Constants.MOD_ID + R_TREE_RESOURCE_SUFFIX);
        this.branch = branch;
        this.building = building;
        this.last = last;

        final List<String> researchList = GlobalResearchTree.researchTree.getPrimaryResearch(branch);
        final DragView view = findPaneOfTypeByID(DRAG_VIEW_ID, DragView.class);

        drawTree(0, 0, view, researchList, building.getColony().getResearchTree(), true, false, 0);

        for (int i = 1; i <= MAX_DEPTH; i++)
        {
            final Label timeLabel = new Label();
            timeLabel.setLabelText(Math.pow(2, i-1) + "h");
            timeLabel.setPosition((i-1) * (GRADIENT_WIDTH + X_SPACING) + GRADIENT_WIDTH / 2, TIMELABEL_Y_POSITION);
            timeLabel.setColor(Color.rgbaToInt(218, 202, 171, 255));
            view.addChild(timeLabel);
        }
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        super.onButtonClicked(button);

        final IGlobalResearch research = GlobalResearchTree.researchTree.getResearch(branch, button.getID());
        if (research != null && building.getBuildingLevel() > building.getColony().getResearchTree().getResearchInProgress().size() && building.getBuildingLevel() > building.getColony().getResearchTree().getResearchInProgress().size() && research.hasEnoughResources(new InvWrapper(Minecraft.getInstance().player.inventory)))
        {
            Network.getNetwork().sendToServer(new TryResearchMessage(research.getId(), research.getBranch(), building.getColony().getID(), building.getColony().getDimension(), building.getID()));
            close();
        }

        if (button.getID().equals("cancel"))
        {
            this.close();
            last.open();
        }
    }

    /**
     * Draw the tree of research.
     * @param height the start y offset.
     * @param depth the current depth.
     * @param view the view to append it to.
     * @param researchList the list of research to go through.
     * @param tree the local tree of the colony.
     * @param parentResearched if possibly can be researched.
     * @param abandoned if abandoned child.
     * @return the next y offset.
     */
    public int drawTree(final int height, final int depth, final DragView view, final List<String> researchList, final LocalResearchTree tree, final boolean parentResearched, final boolean abandoned, final int parentHeight)
    {
        int nextHeight = height;
        for (int i = 0; i < researchList.size(); i++)
        {
            final String researchLabel = researchList.get(i);
            int offsetX = (depth * (GRADIENT_WIDTH + X_SPACING));

            final IGlobalResearch research = GlobalResearchTree.researchTree.getResearch(branch, researchLabel);
            final ILocalResearch localResearch = tree.getResearch(branch, research.getId());
            final ResearchState state = localResearch == null ? ResearchState.NOT_STARTED : localResearch.getState();

            final Gradient gradient = new Gradient();
            gradient.setSize(GRADIENT_WIDTH, GRADIENT_HEIGHT);
            gradient.setPosition(offsetX + INITIAL_X_OFFSET, (nextHeight + Math.min(i, 1)) * (GRADIENT_HEIGHT + Y_SPACING) + Y_SPACING);
            if (state == ResearchState.IN_PROGRESS)
            {
                gradient.setGradientStart(227, 249, 184, 255);
                gradient.setGradientEnd(227, 249, 184, 255);
                view.addChild(gradient);
            }
            else if (!parentResearched)
            {
                gradient.setGradientStart(239, 230, 215, 255);
                gradient.setGradientEnd(239, 230, 215, 255);
                view.addChild(gradient);
            }
            else if (abandoned && state != ResearchState.FINISHED)
            {
                gradient.setGradientStart(191, 184, 172, 255);
                gradient.setGradientEnd(191, 184, 172, 255);
                view.addChild(gradient);
            }
            else if (state != ResearchState.FINISHED)
            {
                gradient.setGradientStart(102, 204, 255, 255);
                gradient.setGradientEnd(102, 204, 255, 255);
                view.addChild(gradient);
            }

            final Box box = new Box();
            box.setColor(218, 202, 171);
            box.setSize(GRADIENT_WIDTH, GRADIENT_HEIGHT);
            box.setPosition(gradient.getX(), gradient.getY());
            view.addChild(box);

            final Label nameLabel = new Label();
            nameLabel.setLabelText(research.getDesc());
            nameLabel.setPosition(offsetX + INITIAL_X_OFFSET + NAME_OFFSET, (nextHeight + Math.min(i, 1)) * (GRADIENT_HEIGHT + Y_SPACING) + Y_SPACING + INITIAL_Y_OFFSET);
            nameLabel.setColor(Color.rgbaToInt(160 , 160 , 160, 255));
            view.addChild(nameLabel);

            if (state == ResearchState.IN_PROGRESS)
            {
                //Calculates how much percent of the next level has been completed.
                final double progressRatio = (localResearch.getProgress()+1)/(Math.pow(2, localResearch.getDepth()-1) * (double) BASE_RESEARCH_TIME) * 100;

                @NotNull final Image xpBar = new Image();
                xpBar.setImage(Screen.GUI_ICONS_LOCATION, XP_BAR_ICON_COLUMN, XP_BAR_EMPTY_ROW, XP_BAR_WIDTH, XP_HEIGHT, false);
                xpBar.setPosition(offsetX + X_SPACING + TEXT_X_OFFSET, nameLabel.getY() + nameLabel.getHeight() + XPBAR_Y_OFFSET);

                @NotNull final Image xpBar2 = new Image();
                xpBar2.setImage(Screen.GUI_ICONS_LOCATION, XP_BAR_ICON_COLUMN_END, XP_BAR_EMPTY_ROW, XP_BAR_ICON_COLUMN_END_WIDTH, XP_HEIGHT, false);
                xpBar2.setPosition(XPBAR_LENGTH + offsetX + X_SPACING + TEXT_X_OFFSET, nameLabel.getY() + nameLabel.getHeight() + XPBAR_Y_OFFSET);

                view.addChild(xpBar);
                view.addChild(xpBar2);

                if (progressRatio > 0)
                {
                    @NotNull final Image xpBarFull = new Image();
                    xpBarFull.setImage(Screen.GUI_ICONS_LOCATION, XP_BAR_ICON_COLUMN, XP_BAR_FULL_ROW, (int) progressRatio, XP_HEIGHT, false);
                    xpBarFull.setPosition(offsetX + X_SPACING + TEXT_X_OFFSET, nameLabel.getY() + nameLabel.getHeight() + XPBAR_Y_OFFSET);
                    view.addChild(xpBarFull);
                }
            }
            else if (research.getResearchRequirement() != null && state != ResearchState.FINISHED)
            {
                final Label requirementLabel = new Label();
                requirementLabel.setLabelText(research.getResearchRequirement().getDesc().getFormattedText());
                requirementLabel.setPosition(offsetX + INITIAL_X_OFFSET + TEXT_X_OFFSET, nameLabel.getY() + nameLabel.getHeight() + INITIAL_Y_OFFSET);
                requirementLabel.setColor(Color.rgbaToInt(160 , 160 , 160, 255));

                view.addChild(requirementLabel);
            }

            final Label effectLabel = new Label();
            effectLabel.setLabelText(research.getEffect().getDesc().getFormattedText());
            effectLabel.setPosition(offsetX + INITIAL_X_OFFSET + TEXT_X_OFFSET, nameLabel.getY() + nameLabel.getHeight() * 2 + INITIAL_Y_OFFSET + INITIAL_Y_OFFSET);
            effectLabel.setColor(Color.rgbaToInt(160 , 160 , 160, 255));

            view.addChild(effectLabel);

            if ( parentResearched && state == ResearchState.NOT_STARTED && !abandoned )
            {
                final ButtonImage buttonImage = new ButtonImage();
                buttonImage.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
                buttonImage.setLabel(LanguageHandler.format("com.minecolonies.coremod.research.research"));
                buttonImage.setTextColor(Color.getByName("black", 0));
                buttonImage.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
                buttonImage.setPosition(effectLabel.getX(), effectLabel.getY() + effectLabel.getHeight() + TEXT_Y_OFFSET);
                buttonImage.setID(research.getId());

                if (building.getBuildingLevel() <= building.getColony().getResearchTree().getResearchInProgress().size() || !research.hasEnoughResources(new InvWrapper(Minecraft.getInstance().player.inventory)))
                {
                    buttonImage.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium_large_disabled.png"));
                }

                view.addChild(buttonImage);

                int storageOffset = 2;
                for (final ItemStorage storage : research.getCostList())
                {
                    final ItemStack stack = storage.getItemStack().copy();
                    stack.setCount(storage.getAmount());
                    final ItemIcon icon = new ItemIcon();
                    icon.setItem(stack);
                    icon.setPosition(buttonImage.getX() + buttonImage.getWidth() + storageOffset, effectLabel.getY() + effectLabel.getHeight() + INITIAL_Y_OFFSET);
                    icon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                    view.addChild(icon);

                    storageOffset += COST_OFFSET;
                }
            }
            else if (!parentResearched)
            {
                final Image lockIcon = new Image();
                lockIcon.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/locked_icon.png"));
                lockIcon.setSize(LOCK_WIDTH, LOCK_HEIGHT);
                lockIcon.setPosition( effectLabel.getX() + BUTTON_LENGTH, effectLabel.getY() + effectLabel.getHeight() + INITIAL_Y_OFFSET);
                view.addChild(lockIcon);
            }

            final boolean firstSibling = i == 0;
            final boolean secondSibling = i == 1;

            final boolean lastSibling = i+1 >= researchList.size();

            if (!research.getParent().isEmpty())
            {
                if (firstSibling && lastSibling)
                {
                    final Image corner = new Image();
                    corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right.png"));
                    corner.setSize(X_SPACING, GRADIENT_HEIGHT);
                    corner.setPosition(gradient.getX() - X_SPACING, gradient.getY());
                    view.addChild(corner);
                }
                else
                {
                    if (secondSibling)
                    {
                        for (int dif = 1; dif < (nextHeight + Math.min(i, 1)) - parentHeight; dif++)
                        {
                            final Image corner = new Image();
                            corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_down.png"));
                            corner.setSize(X_SPACING, GRADIENT_HEIGHT + Y_SPACING);
                            corner.setPosition(gradient.getX() - X_SPACING, (dif) * (gradient.getHeight() + Y_SPACING) + Y_SPACING);
                            view.addChild(corner);
                        }
                    }

                    if (firstSibling)
                    {
                        final Image corner = new Image();
                        corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right_down.png"));
                        corner.setSize(X_SPACING, GRADIENT_HEIGHT + Y_SPACING);
                        corner.setPosition(gradient.getX() - X_SPACING, gradient.getY());
                        view.addChild(corner);
                    }
                    else
                    {
                        if (GlobalResearchTree.researchTree.getResearch(branch, research.getParent()).hasOnlyChild())
                        {
                            final Label orLabel = new Label();
                            orLabel.setColor(Color.getByName("black", 0));
                            orLabel.setLabelText("or");
                            orLabel.setPosition(gradient.getX() - X_SPACING + OR_X_OFFSET, gradient.getY() + OR_Y_OFFSET);
                            view.addChild(orLabel);

                            if (lastSibling)
                            {
                                final Image circle = new Image();
                                circle.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_or.png"));
                                circle.setSize(X_SPACING, GRADIENT_HEIGHT);
                                circle.setPosition(gradient.getX() - X_SPACING, gradient.getY());
                                view.addChild(circle);
                            }
                            else
                            {
                                final Image corner = new Image();
                                corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_or_down.png"));
                                corner.setSize(X_SPACING, GRADIENT_HEIGHT + Y_SPACING);
                                corner.setPosition(gradient.getX() - X_SPACING, gradient.getY());
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
                                corner.setPosition(gradient.getX() - X_SPACING, gradient.getY());
                                view.addChild(corner);
                            }
                            else
                            {
                                final Image corner = new Image();
                                corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right_and_more.png"));
                                corner.setSize(X_SPACING, GRADIENT_HEIGHT + Y_SPACING);
                                corner.setPosition(gradient.getX() - X_SPACING, gradient.getY());
                                view.addChild(corner);
                            }
                        }
                    }
                }

            }

            if (!research.getChilds().isEmpty())
            {
                nextHeight = drawTree(nextHeight + Math.min(i, 1), depth + 1, view, research.getChilds(), tree, state == ResearchState.FINISHED, research.hasOnlyChild() && research.hasResearchedChild(tree), (nextHeight + Math.min(i, 1)));
            }
            else
            {
                nextHeight += Math.min(i, 1);
            }
        }

        return nextHeight;
    }

}
