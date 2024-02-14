package com.minecolonies.core.client.gui.visitor.expeditionary;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.ScrollingList.DataProvider;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.client.gui.generic.ResourceItem;
import com.minecolonies.core.client.gui.generic.ResourceItem.ResourceComparator;
import com.minecolonies.core.colony.expeditions.Expedition;
import com.minecolonies.core.colony.expeditions.ExpeditionBuilder;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType.Difficulty;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionTypeManager;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement.RequirementHandler;
import com.minecolonies.core.network.messages.server.colony.OpenInventoryMessage;
import com.minecolonies.core.network.messages.server.colony.visitor.expeditionary.StartExpeditionMessage;
import com.minecolonies.core.network.messages.server.colony.visitor.expeditionary.TransferItemsMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ExpeditionConstants.EXPEDITIONARY_DIFFICULTY;
import static com.minecolonies.api.util.constant.ExpeditionConstants.EXPEDITIONARY_DIFFICULTY_PREFIX;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.EXTRA_DATA_EXPEDITION;
import static com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.EXTRA_DATA_EXPEDITION_TYPE;

/**
 * Main window for the expeditionary their GUI.
 */
public class MainWindowExpeditionary extends AbstractWindowSkeleton
{
    /**
     * Window constants.
     */
    private static final String ID_EXPEDITION_NAME       = "expedition_name";
    private static final String ID_EXPEDITION_ITEMS      = "expedition_items";
    private static final String ID_EXPEDITION_DIFFICULTY = "expedition_difficulty";

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
     * The builder instance for the expedition.
     */
    private final ExpeditionBuilder expeditionBuilder;

    /**
     * The requirements for this expedition type.
     */
    private final List<RequirementHandler> requirements;

    /**
     * Default constructor.
     */
    public MainWindowExpeditionary(final @NotNull IVisitorViewData visitorData)
    {
        super(Constants.MOD_ID + EXPEDITIONARY_MAIN_RESOURCE_SUFFIX);
        this.visitorData = visitorData;

        final ResourceLocation expeditionTypeId = visitorData.getExtraDataValue(EXTRA_DATA_EXPEDITION_TYPE);
        expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(expeditionTypeId);
        if (expeditionType == null)
        {
            throw new IllegalStateException(String.format("Expedition with id '%s' does not exist.", expeditionTypeId.toString()));
        }

        expeditionBuilder = visitorData.getExtraDataValue(EXTRA_DATA_EXPEDITION);

        requirements = expeditionType.getRequirements().stream().map(m -> m.createHandler(visitorData::getInventory)).collect(Collectors.toList());
        requirements.sort(new ResourceComparator());

        registerButton(RESOURCE_ADD, this::transferItems);
    }

    /**
     * Open visitor inventory for providing tools.
     */
    private void openVisitorInventory()
    {
        Network.getNetwork().sendToServer(new OpenInventoryMessage(visitorData.getColony(), visitorData.getName(), visitorData.getId()));
    }

    /**
     * Render the difficulty icons.
     */
    private void renderDifficulty()
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

        final List<ICitizenDataView> allGuards = visitorData.getColony().getCitizens().values().stream()
                                                   .filter(f -> f.getJobView() != null && f.getJobView().isGuard() && f.getJobView().isCombatGuard())
                                                   .toList();

        PaneBuilders.tooltipBuilder()
          .append(Component.translatable(EXPEDITIONARY_DIFFICULTY, Component.translatable(EXPEDITIONARY_DIFFICULTY_PREFIX + currentDifficulty.getKey()))
                    .withStyle(currentDifficulty.getStyle()))
          .hoverPane(findPaneOfTypeByID(ID_EXPEDITION_DIFFICULTY, View.class))
          .build();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        renderDifficulty();
        renderItemsList();

        findPaneOfTypeByID(ID_EXPEDITION_NAME, Text.class).setText(expeditionType.getName());

        registerButton(ID_EXPEDITION_NAME, this::openVisitorInventory);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        requirements.sort(new ResourceComparator());
    }

    /**
     * Render the item requirement list.
     */
    private void renderItemsList()
    {
        final ScrollingList itemsList = findPaneOfTypeByID(ID_EXPEDITION_ITEMS, ScrollingList.class);
        itemsList.setDataProvider(new DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return requirements.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                ResourceItem.updateResourcePane(requirements.get(index), index, rowPane);
            }
        });
    }

    /**
     * Transfers the items from player inventory to target inventory.
     *
     * @param button the clicked button.
     */
    private void transferItems(@NotNull final Button button)
    {
        final Pane pane = button.getParent();
        button.disable();
        final Text idLabel = pane.findPaneOfTypeByID(RESOURCE_ID, Text.class);
        final int index = Integer.parseInt(idLabel.getTextAsString());
        final RequirementHandler requirement = requirements.get(index);
        if (requirement == null)
        {
            Log.getLogger().warn("WindowHutBuilder.transferItems: Error - Could not find the resource.");
            return;
        }

        Network.getNetwork().sendToServer(new TransferItemsMessage(visitorData, expeditionType, requirement.getId()));

        final int needed = requirement.getAmount() - requirement.getAmountAvailable();
        InventoryUtils.transferItemStackIntoNextFreeSlotFromItemHandler(new InvWrapper(mc.player.getInventory()),
          requirement.getItemPredicate(),
          needed,
          visitorData.getInventory());
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
        expeditionBuilder.addEquipment(equipment);

        final Expedition expedition = expeditionBuilder.build();
        expedition.setStatus(ExpeditionStatus.EMBARKED);
        Network.getNetwork().sendToServer(new StartExpeditionMessage(visitorData.getColony(), expedition));
    }
}