package com.minecolonies.core.client.gui.visitor.expeditionary;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.colony.expeditions.IExpedition;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.colony.expeditions.Expedition;
import com.minecolonies.core.colony.expeditions.ExpeditionVisitorMember;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType.Difficulty;
import com.minecolonies.core.network.messages.server.colony.OpenInventoryMessage;
import com.minecolonies.core.network.messages.server.colony.visitor.expeditionary.StartExpeditionMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.EXPEDITIONARY_MAIN_RESOURCE_SUFFIX;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.EXPEDITIONARY_DIFFICULTY;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.EXPEDITIONARY_DIFFICULTY_PREFIX;
import static com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.EXTRA_DATA_EXPEDITION_TYPE;

/**
 * Main window for the expeditionary their GUI.
 */
public class MainWindowExpeditionary extends AbstractWindowSkeleton
{
    /**
     * Window constants.
     */
    private static final String LABEL_EXPEDITION_NAME      = "expedition_name";
    private static final String VIEW_EXPEDITION_DIFFICULTY = "expedition_difficulty";

    /**
     * The visitor data.
     */
    @NotNull
    private final IVisitorViewData visitorData;

    /**
     * The current expedition type.
     */
    private final ColonyExpeditionType expeditionType;

    /**
     * Default constructor.
     */
    public MainWindowExpeditionary(final @NotNull IVisitorViewData visitorData)
    {
        super(Constants.MOD_ID + EXPEDITIONARY_MAIN_RESOURCE_SUFFIX);
        this.visitorData = visitorData;
        this.expeditionType = visitorData.getExtraDataValue(EXTRA_DATA_EXPEDITION_TYPE).orElseThrow();

        findPaneOfTypeByID(LABEL_EXPEDITION_NAME, Text.class).setText(expeditionType.getName());

        updateDifficulty();

        registerButton(LABEL_EXPEDITION_NAME, this::openVisitorInventory);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    /**
     * Open visitor inventory for providing tools.
     */
    private void openVisitorInventory()
    {
        Network.getNetwork().sendToServer(new OpenInventoryMessage(visitorData.getColony(), visitorData.getName(), visitorData.getId()));
    }

    /**
     * Update the difficulty icons.
     */
    private void updateDifficulty()
    {
        final int maxDifficulty = Arrays.stream(Difficulty.values())
                                    .filter(m -> m.equals(expeditionType.getDifficulty()) || !m.isHidden())
                                    .mapToInt(Difficulty::getLevel)
                                    .max()
                                    .orElse(0);
        final Difficulty currentDifficulty = expeditionType.getDifficulty();

        for (int i = currentDifficulty.getLevel(); i <= maxDifficulty; i++)
        {
            findPaneOfTypeByID("diff_" + i, Image.class).setVisible(true);
        }

        for (int i = 1; i <= currentDifficulty.getLevel(); i++)
        {
            final Image iconPane = findPaneOfTypeByID("diff_" + i, Image.class);
            iconPane.setVisible(true);
            iconPane.setImage(new ResourceLocation("textures/item/" + currentDifficulty.getIcon().toString() + ".png"), false);
        }

        PaneBuilders.tooltipBuilder()
          .append(Component.translatable(EXPEDITIONARY_DIFFICULTY, Component.translatable(EXPEDITIONARY_DIFFICULTY_PREFIX + currentDifficulty.getKey()))
                    .withStyle(currentDifficulty.getStyle()))
          .hoverPane(findPaneOfTypeByID(VIEW_EXPEDITION_DIFFICULTY, View.class))
          .build();
    }

    /**
     * Triggers starting the expedition.
     */
    private void startExpedition()
    {
        // Gather the inventory and the armor slots from the inventory.
        final List<ItemStack> equipment = InventoryUtils.getItemHandlerAsList(visitorData.getInventory());
        for (final EquipmentSlot equipmentSlot : EquipmentSlot.values())
        {
            final ItemStack armorItem = visitorData.getInventory().getArmorInSlot(equipmentSlot);
            if (armorItem != ItemStack.EMPTY)
            {
                equipment.add(armorItem);
            }
        }

        final List<IExpeditionMember> members = new ArrayList<>();
        members.add(new ExpeditionVisitorMember(visitorData));
        // TODO: Iterate assigned guards

        final IExpedition expedition = new Expedition(expeditionType.getDimension(), equipment, members);
        expedition.setStatus(ExpeditionStatus.EMBARKED);
        Network.getNetwork().sendToServer(new StartExpeditionMessage(visitorData.getColony(), expedition));
    }
}