package com.minecolonies.coremod.client.gui.huts;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.SwitchView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractWindowWorkerModuleBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import com.minecolonies.coremod.network.messages.server.colony.building.guard.GuardSetMinePosMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.miner.MinerSetLevelMessage;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Window for the miner hut.
 */
public class WindowHutMinerModule extends AbstractWindowWorkerModuleBuilding<BuildingMiner.View>
{
    private static final String                        LIST_LEVELS               = "levels";
    private static final String                        LIST_GUARDS               = "guards";
    private static final String                        PAGE_LEVELS               = "levelActions";
    private static final String                        PAGE_GUARDS               = "guardActions";
    private static final String                        BUTTON_CURRENTLEVEL       = "changeToLevel";
    private static final String                        BUTTON_ASSIGNGUARD        = "assignGuard";
    private static final String                        VIEW_PAGES                = "pages";
    private static final String                        HUT_MINER_RESOURCE_SUFFIX = ":gui/windowhutminer.xml";
    private final        BuildingMiner.View            miner;
    private              List<Tuple<Integer, Integer>> levelsInfo;
    private              ScrollingList                 levelList;
    private              List<ICitizenDataView>        guardsInfo;
    private              ScrollingList                 guardsList;

    /**
     * Constructor for the window of the miner hut.
     *
     * @param building {@link BuildingMiner.View}.
     */
    public WindowHutMinerModule(final BuildingMiner.View building)
    {
        super(building, Constants.MOD_ID + HUT_MINER_RESOURCE_SUFFIX);
        this.miner = building;
        pullLevelsFromHut();
        pullGuardsFromHut();
    }

    /**
     * Retrieve levels from the building to display in GUI.
     */
    private void pullLevelsFromHut()
    {
        if (miner.getColony().getBuilding(miner.getID()) != null)
        {
            levelsInfo = miner.levelsInfo;
        }
    }

    /**
     * Retrieve guards from the building to display in GUI.
     */
    private void pullGuardsFromHut()
    {
        if (miner.getColony().getBuilding(miner.getID()) != null)
        {
            guardsInfo = miner.pullGuards();
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        levelList = findPaneOfTypeByID(LIST_LEVELS, ScrollingList.class);
        levelList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return levelsInfo.size();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                if (index == miner.current)
                {
                    rowPane.findPaneOfTypeByID("lvl", Text.class).setColors(Color.getByName("red", 0));
                }

                rowPane.findPaneOfTypeByID("lvl", Text.class).setText(Integer.toString(index));
                rowPane.findPaneOfTypeByID("nONodes", Text.class)
                  .setText(LanguageHandler.format("com.minecolonies.coremod.gui.workerhuts.minerNode") + ": " + levelsInfo.get(index).getA());
                rowPane.findPaneOfTypeByID("yLevel", Text.class)
                  .setText("Y: " + (levelsInfo.get(index).getB() + 1));
                // ^^ 1 is for Y depth fix
            }
        });
        guardsList = findPaneOfTypeByID(LIST_GUARDS, ScrollingList.class);
        guardsList.setDataProvider(new ScrollingList.DataProvider() {
            @Override
            public int getElementCount()
            {
                return guardsInfo.size();
            }

            @Override
            public void updateElement(final int i, final Pane pane)
            {
                final ICitizenDataView citizen = guardsInfo.get(i);
                if (citizen != null)
                {
                    final IBuildingView building = miner.getColony().getBuilding(citizen.getWorkBuilding());
                    if (building instanceof AbstractBuildingGuards.View)
                    {
                        pane.findPaneOfTypeByID("guardName", Text.class).setText(citizen.getName());
                        final AbstractBuildingGuards.View guardbuilding = (AbstractBuildingGuards.View) building;
                        final Button button = pane.findPaneOfTypeByID("assignGuard", Button.class);
                        if (guardbuilding.getMinePos() == null)
                        {
                            button.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.hiring.buttonassign"));
                            if (miner.assignedGuards >= miner.getMaxGuards())
                            {
                                button.setEnabled(false);
                            }
                            else
                            {
                                button.setEnabled(true);
                            }
                        }
                        else if (guardbuilding.getMinePos().equals(miner.getPosition()))
                        {
                            button.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.hiring.buttonunassign"));
                        }
                        else
                        {
                            button.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.hiring.buttonassign"));
                            button.setEnabled(false);
                        }
                    }
                }
                else
                {
                    final Button button = pane.findPaneOfTypeByID("assignGuard", Button.class);
                    button.setEnabled(false);
                }
            }
        });
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.minerHut";
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        final String currentPage = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).getCurrentView().getID();
        if (currentPage.equals(PAGE_LEVELS))
        {
            pullLevelsFromHut();
            window.findPaneOfTypeByID(LIST_LEVELS, ScrollingList.class).refreshElementPanes();
        }
        else if (currentPage.equals(PAGE_GUARDS))
        {
            pullGuardsFromHut();
            window.findPaneOfTypeByID(LIST_GUARDS, ScrollingList.class).refreshElementPanes();
        }
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        switch (button.getID())
        {
            case BUTTON_CURRENTLEVEL:
                final int row = levelList.getListElementIndexByPane(button);
                if (row != miner.current && row >= 0 && row < levelsInfo.size())
                {
                    miner.current = row;
                    Network.getNetwork().sendToServer(new MinerSetLevelMessage(miner, row));
                }
                break;
            case BUTTON_ASSIGNGUARD:
                final int guardRow = guardsList.getListElementIndexByPane(button);
                final ICitizenDataView guard = guardsInfo.get(guardRow);
                if (guard != null)
                {
                    final AbstractBuildingGuards.View guardbuilding = (AbstractBuildingGuards.View) miner.getColony().getBuilding(guard.getWorkBuilding());
                    if (guardbuilding.getMinePos() == null)
                    {
                        if (miner.assignedGuards < miner.getMaxGuards())
                        {
                            Network.getNetwork().sendToServer(new GuardSetMinePosMessage(guardbuilding, miner.getPosition()));
                            button.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.hiring.buttonunassign"));
                            guardbuilding.setMinePos(miner.getPosition());
                            miner.assignedGuards++;
                        }
                    }
                    else if (guardbuilding.getMinePos().equals(miner.getPosition()))
                    {
                        Network.getNetwork().sendToServer(new GuardSetMinePosMessage(guardbuilding));
                        button.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.hiring.buttonassign"));
                        guardbuilding.setMinePos(null);
                        miner.assignedGuards--;
                    }
                }
                pullGuardsFromHut();
                break;
            default:
                super.onButtonClicked(button);
                break;
        }
    }
}

