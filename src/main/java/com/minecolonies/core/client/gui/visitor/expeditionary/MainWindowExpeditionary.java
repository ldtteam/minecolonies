package com.minecolonies.core.client.gui.visitor.expeditionary;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.support.DataProviders.CheckListDataProvider;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.ScrollingList.DataProvider;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.managers.interfaces.expeditions.ColonyExpedition.GuardsComparator;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.client.gui.generic.ResourceItem;
import com.minecolonies.core.client.gui.generic.ResourceItem.ResourceAvailability;
import com.minecolonies.core.client.gui.generic.ResourceItem.ResourceComparator;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement.RequirementHandler;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeDifficulty.Difficulty;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeDifficulty;
import com.minecolonies.core.items.ItemExpeditionSheet;
import com.minecolonies.core.network.messages.server.OpenExpeditionSheetInventoryMessage;
import com.minecolonies.core.network.messages.server.colony.visitor.expeditionary.AssignGuardMessage;
import com.minecolonies.core.network.messages.server.colony.visitor.expeditionary.TransferItemsMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ExpeditionConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Main window for the expeditionary their GUI.
 */
public class MainWindowExpeditionary extends AbstractWindowSkeleton
{
    /**
     * Window constants.
     */
    private static final String ID_EXPEDITION_NAME              = "expedition_name";
    private static final String ID_EXPEDITION_DIFFICULTY        = "expedition_difficulty";
    private static final String ID_EXPEDITION_ITEMS             = "expedition_items";
    private static final String ID_EXPEDITION_ITEMS_HEADER      = "expedition_items_header";
    private static final String ID_EXPEDITION_ITEMS_INVENTORY   = "expedition_inventory";
    private static final String ID_EXPEDITION_ITEMS_SUBHEADER   = "expedition_items_subheader";
    private static final String ID_EXPEDITION_GUARDS            = "expedition_guards";
    private static final String ID_EXPEDITION_GUARDS_HEADER     = "expedition_guards_header";
    private static final String ID_EXPEDITION_GUARDS_SUBHEADER  = "expedition_guards_subheader";
    private static final String ID_EXPEDITION_GUARDS_NAME       = "citizen";
    private static final String ID_EXPEDITION_GUARDS_WEAPON     = "weapon";
    private static final String ID_EXPEDITION_GUARDS_HELMET     = "helmet";
    private static final String ID_EXPEDITION_GUARDS_CHESTPLATE = "chestplate";
    private static final String ID_EXPEDITION_GUARDS_LEGGINGS   = "leggings";
    private static final String ID_EXPEDITION_GUARDS_BOOTS      = "boots";
    private static final String ID_EXPEDITION_GUARDS_ASSIGN     = "guardAssign";

    /**
     * The colony view.
     */
    @NotNull
    private final IColonyView colonyView;

    /**
     * The current expedition type.
     */
    @NotNull
    private final ColonyExpeditionType expeditionType;

    /**
     * Which hand the player used to open the GUI with from item.
     */
    @NotNull
    private final InteractionHand hand;

    /**
     * The container surrounding the item stack for the expedition.
     */
    @NotNull
    private final ItemExpeditionSheet.ExpeditionSheetContainerManager container;

    /**
     * The comparator instance for the resources list.
     */
    private final ResourceComparator resourceComparator;

    /**
     * The comparator instance for the guards list.
     */
    private final GuardsComparator guardsComparator;

    /**
     * The requirements for this expedition type.
     */
    private final List<RequirementHandler> requirements;

    /**
     * The list of guards in the colony.
     */
    private final List<ICitizenDataView> guards;

    private final ScrollingList itemsList;
    private final ScrollingList guardsList;

    /**
     * Default constructor.
     */
    public MainWindowExpeditionary(
      final @NotNull IColonyView colonyView,
      final @NotNull ColonyExpeditionType expeditionType,
      final @NotNull InteractionHand hand,
      final @NotNull ItemExpeditionSheet.ExpeditionSheetContainerManager container)
    {
        super(Constants.MOD_ID + EXPEDITIONARY_MAIN_RESOURCE_SUFFIX);
        this.colonyView = colonyView;
        this.expeditionType = expeditionType;
        this.hand = hand;
        this.container = container;

        resourceComparator = new ResourceComparator();
        requirements = expeditionType.requirements().stream().map(m -> m.createHandler(() -> new InvWrapper(container))).collect(Collectors.toList());
        requirements.sort(resourceComparator);

        guardsComparator = new GuardsComparator(container.getMembers());
        guards = colonyView.getCitizens().values().stream()
                   .filter(f -> f.getJobView() != null && f.getJobView().isGuard() && f.getJobView().isCombatGuard() && !f.getColony()
                                                                                                                           .getTravelingManager()
                                                                                                                           .isTravelling(f.getId()))
                   .collect(Collectors.toList());
        guards.sort(guardsComparator);

        itemsList = findPaneOfTypeByID(ID_EXPEDITION_ITEMS, ScrollingList.class);
        guardsList = findPaneOfTypeByID(ID_EXPEDITION_GUARDS, ScrollingList.class);

        registerButton(RESOURCE_ADD, this::transferItems);
        registerButton(ID_EXPEDITION_ITEMS_INVENTORY, this::openVisitorInventory);
    }

    /**
     * Find the first possible weapon in the inventory (swords or bows)
     *
     * @param inventory the target inventory.
     * @return the item stack containing the weapon or empty.
     */
    public static ItemStack getFirstWeapon(final IItemHandler inventory)
    {
        final int swordSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(inventory, ToolType.SWORD, 0, 5);
        if (swordSlot != -1)
        {
            return inventory.getStackInSlot(swordSlot);
        }

        final int bowSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(inventory, ToolType.BOW, 0, 5);
        if (bowSlot != -1)
        {
            return inventory.getStackInSlot(bowSlot);
        }

        return ItemStack.EMPTY;
    }

    /**
     * Open visitor inventory for providing tools.
     */
    private void openVisitorInventory()
    {
        Network.getNetwork().sendToServer(new OpenExpeditionSheetInventoryMessage(hand));
    }

    /**
     * Render the difficulty icons.
     */
    private void renderDifficulty()
    {
        final int maxDifficulty = Arrays.stream(ColonyExpeditionTypeDifficulty.Difficulty.values())
                                    .filter(m -> m.equals(expeditionType.difficulty()) || !m.isHidden())
                                    .mapToInt(ColonyExpeditionTypeDifficulty.Difficulty::getLevel)
                                    .max()
                                    .orElse(0);
        final Difficulty currentDifficulty = expeditionType.difficulty();

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
          .hoverPane(findPaneOfTypeByID(ID_EXPEDITION_DIFFICULTY, View.class))
          .build();
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        renderHeaders();
        renderDifficulty();
        renderItemsList();
        renderGuardsList();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        requirements.sort(resourceComparator);
    }

    /**
     * Render the different textual headers.
     */
    private void renderHeaders()
    {
        findPaneOfTypeByID(ID_EXPEDITION_NAME, Text.class).setText(expeditionType.name());

        findPaneOfTypeByID(ID_EXPEDITION_ITEMS_HEADER, Text.class).setText(Component.translatable(EXPEDITIONARY_ITEMS_HEADER));
        findPaneOfTypeByID(ID_EXPEDITION_GUARDS_HEADER, Text.class).setText(Component.translatable(EXPEDITIONARY_GUARDS_HEADER, expeditionType.guards()));

        final boolean itemRequirementsMet = requirements.stream().allMatch(m -> m.getAvailabilityStatus().equals(ResourceAvailability.NOT_NEEDED));
        findPaneOfTypeByID(ID_EXPEDITION_ITEMS_SUBHEADER, Text.class)
          .setText(itemRequirementsMet
                     ? Component.translatable(EXPEDITIONARY_ITEMS_SUBHEADER_MET)
                     : Component.translatable(EXPEDITIONARY_ITEMS_SUBHEADER_NOT_MET));

        final int currentGuardCount = container.getMembers().size();
        final boolean guardRequirementMet = currentGuardCount >= expeditionType.guards();
        findPaneOfTypeByID(ID_EXPEDITION_GUARDS_SUBHEADER, Text.class)
          .setText(guardRequirementMet
                     ? Component.translatable(EXPEDITIONARY_GUARDS_SUBHEADER_MET)
                     : Component.translatable(EXPEDITIONARY_GUARDS_SUBHEADER_NOT_MET, expeditionType.guards() - currentGuardCount));
    }

    /**
     * Render the item requirement list.
     */
    private void renderItemsList()
    {
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
                ResourceItem.updateResourcePane(requirements.get(index), mc.player, index, rowPane);
            }
        });
    }

    /**
     * Render the guards list.
     */
    private void renderGuardsList()
    {
        guardsList.setDataProvider(new CheckListDataProvider()
        {
            @Override
            public int getElementCount()
            {
                return guards.size();
            }

            @Override
            public String getCheckboxId()
            {
                return ID_EXPEDITION_GUARDS_ASSIGN;
            }

            @Override
            public boolean isChecked(final int index)
            {
                return container.getMembers().contains(guards.get(index).getId());
            }

            @Override
            public void setChecked(final int index, final boolean checked)
            {
                final ICitizenDataView guard = guards.get(index);
                Network.getNetwork().sendToServer(new AssignGuardMessage(guard, checked, hand));
                container.toggleMember(guard.getId(), checked);
                guards.sort(guardsComparator);
                renderHeaders();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane, final boolean checked)
            {
                final ICitizenDataView guard = guards.get(index);

                rowPane.findPaneOfTypeByID(ID_EXPEDITION_GUARDS_NAME, Text.class).setText(guard.getJobComponent().append(": ").append(Component.literal(guard.getName())));

                renderGuardEquipment(getFirstWeapon(guard.getInventory()), ID_EXPEDITION_GUARDS_WEAPON, rowPane);
                renderGuardEquipment(guard.getInventory().getArmorInSlot(EquipmentSlot.HEAD), ID_EXPEDITION_GUARDS_HELMET, rowPane);
                renderGuardEquipment(guard.getInventory().getArmorInSlot(EquipmentSlot.CHEST), ID_EXPEDITION_GUARDS_CHESTPLATE, rowPane);
                renderGuardEquipment(guard.getInventory().getArmorInSlot(EquipmentSlot.LEGS), ID_EXPEDITION_GUARDS_LEGGINGS, rowPane);
                renderGuardEquipment(guard.getInventory().getArmorInSlot(EquipmentSlot.FEET), ID_EXPEDITION_GUARDS_BOOTS, rowPane);
            }
        });
    }

    /**
     * Render one of the inventory slots for the given guard.
     *
     * @param itemStack the item stack to show in the slot.
     * @param id        the id of what pane to load.
     * @param rowPane   the row pane.
     */
    private void renderGuardEquipment(final ItemStack itemStack, final String id, final Pane rowPane)
    {
        rowPane.findPaneOfTypeByID(id, ItemIcon.class).setItem(itemStack);
        rowPane.findPaneOfTypeByID(id + "_back", Image.class).setVisible(itemStack.equals(ItemStack.EMPTY));
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
            Log.getLogger().warn("MainWindowExpeditionary.transferItems: Error - Could not find the resource.");
            return;
        }

        Network.getNetwork().sendToServer(new TransferItemsMessage(colonyView, expeditionType.id(), requirement.getId(), hand));

        final int needed = requirement.getAmount() - requirement.getAmountAvailable();
        if (mc.player.isCreative())
        {
            InventoryUtils.addItemStackToItemHandler(new InvWrapper(container), requirement.getDefaultItemStack().copyWithCount(needed));
        }
        else
        {
            InventoryUtils.transferItemStackIntoNextFreeSlotFromItemHandler(new InvWrapper(mc.player.getInventory()),
              requirement.getItemPredicate(),
              needed,
              new InvWrapper(container));
        }

        renderHeaders();
    }
}