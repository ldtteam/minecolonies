package com.minecolonies.core.client.gui.visitor.expeditionary;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.support.DataProviders.CheckListDataProvider;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.ScrollingList.DataProvider;
import com.ldtteam.blockui.views.View;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IVisitorViewData;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.client.gui.generic.ResourceItem;
import com.minecolonies.core.client.gui.generic.ResourceItem.ResourceAvailability;
import com.minecolonies.core.client.gui.generic.ResourceItem.ResourceComparator;
import com.minecolonies.core.colony.expeditions.ExpeditionCitizenMember;
import com.minecolonies.core.colony.expeditions.ExpeditionVisitorMember;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpedition;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpedition.GuardsComparator;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionType.Difficulty;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionTypeManager;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement.RequirementHandler;
import com.minecolonies.core.network.messages.server.colony.OpenInventoryMessage;
import com.minecolonies.core.network.messages.server.colony.visitor.expeditionary.AssignGuardMessage;
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
import java.util.Set;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.ExpeditionConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.EXTRA_DATA_EXPEDITION;

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
    private static final String ID_EXPEDITION_START             = "expedition_start";
    private static final String ID_EXPEDITION_ITEMS             = "expedition_items";
    private static final String ID_EXPEDITION_ITEMS_HEADER      = "expedition_items_header";
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
    private final ColonyExpedition expedition;

    /**
     * Cache of the members for easier O(1) lookup.
     */
    private final Set<Integer> membersByIdCache;

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
    public MainWindowExpeditionary(final @NotNull IVisitorViewData visitorData)
    {
        super(Constants.MOD_ID + EXPEDITIONARY_MAIN_RESOURCE_SUFFIX);
        this.visitorData = visitorData;

        expedition = visitorData.getExtraDataValue(EXTRA_DATA_EXPEDITION);

        expeditionType = ColonyExpeditionTypeManager.getInstance().getExpeditionType(expedition.getExpeditionTypeId());
        if (expeditionType == null)
        {
            throw new IllegalStateException(String.format("Expedition with id '%s' does not exist.", expedition.getExpeditionTypeId()));
        }

        membersByIdCache = expedition.getMembers().stream().map(IExpeditionMember::getId).collect(Collectors.toSet());

        resourceComparator = new ResourceComparator();
        requirements = expeditionType.getRequirements().stream().map(m -> m.createHandler(visitorData::getInventory)).collect(Collectors.toList());
        requirements.sort(resourceComparator);

        guardsComparator = new GuardsComparator(expedition);
        guards = visitorData.getColony().getCitizens().values().stream()
                   .filter(f -> f.getJobView() != null && f.getJobView().isGuard() && f.getJobView().isCombatGuard())
                   .collect(Collectors.toList());
        guards.sort(guardsComparator);

        itemsList = findPaneOfTypeByID(ID_EXPEDITION_ITEMS, ScrollingList.class);
        guardsList = findPaneOfTypeByID(ID_EXPEDITION_GUARDS, ScrollingList.class);

        registerButton(RESOURCE_ADD, this::transferItems);
        registerButton(ID_EXPEDITION_NAME, this::openVisitorInventory);
        registerButton(ID_EXPEDITION_START, this::startExpedition);
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
        findPaneOfTypeByID(ID_EXPEDITION_NAME, Text.class).setText(expeditionType.getName());

        findPaneOfTypeByID(ID_EXPEDITION_ITEMS_HEADER, Text.class).setText(Component.translatable(EXPEDITIONARY_ITEMS_HEADER));
        findPaneOfTypeByID(ID_EXPEDITION_GUARDS_HEADER, Text.class).setText(Component.translatable(EXPEDITIONARY_GUARDS_HEADER, expeditionType.getGuards()));

        final boolean itemRequirementsMet = requirements.stream().allMatch(m -> m.getAvailabilityStatus().equals(ResourceAvailability.NOT_NEEDED));
        findPaneOfTypeByID(ID_EXPEDITION_ITEMS_SUBHEADER, Text.class)
          .setText(itemRequirementsMet
                     ? Component.translatable(EXPEDITIONARY_ITEMS_SUBHEADER_MET)
                     : Component.translatable(EXPEDITIONARY_ITEMS_SUBHEADER_NOT_MET));

        final int currentGuardCount = expedition.getMembers().size();
        final boolean guardRequirementMet = currentGuardCount >= expeditionType.getGuards();
        findPaneOfTypeByID(ID_EXPEDITION_GUARDS_SUBHEADER, Text.class)
          .setText(guardRequirementMet
                     ? Component.translatable(EXPEDITIONARY_GUARDS_SUBHEADER_MET)
                     : Component.translatable(EXPEDITIONARY_GUARDS_SUBHEADER_NOT_MET, expeditionType.getGuards() - currentGuardCount));

        findPaneOfTypeByID(ID_EXPEDITION_START, ButtonImage.class).setEnabled(itemRequirementsMet && guardRequirementMet);
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
                return membersByIdCache.contains(guards.get(index).getId());
            }

            @Override
            public void setChecked(final int index, final boolean checked)
            {
                final ICitizenDataView guard = guards.get(index);
                if (checked)
                {
                    expedition.addMember(new ExpeditionCitizenMember(guard));
                    membersByIdCache.add(guard.getId());
                }
                else
                {
                    expedition.removeMember(new ExpeditionCitizenMember(guard));
                    membersByIdCache.remove(guard.getId());
                }

                Network.getNetwork().sendToServer(new AssignGuardMessage(guard, checked));

                guards.sort(guardsComparator);

                renderHeaders();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane, final boolean checked)
            {
                final ICitizenDataView guard = guards.get(index);

                rowPane.findPaneOfTypeByID(ID_EXPEDITION_GUARDS_NAME, Text.class).setText(guard.getJobComponent().append(": ").append(Component.literal(guard.getName())));

                renderGuardEquipment(getFirstWeapon(guard), ID_EXPEDITION_GUARDS_WEAPON, rowPane);
                renderGuardEquipment(guard.getInventory().getArmorInSlot(EquipmentSlot.HEAD), ID_EXPEDITION_GUARDS_HELMET, rowPane);
                renderGuardEquipment(guard.getInventory().getArmorInSlot(EquipmentSlot.CHEST), ID_EXPEDITION_GUARDS_CHESTPLATE, rowPane);
                renderGuardEquipment(guard.getInventory().getArmorInSlot(EquipmentSlot.LEGS), ID_EXPEDITION_GUARDS_LEGGINGS, rowPane);
                renderGuardEquipment(guard.getInventory().getArmorInSlot(EquipmentSlot.FEET), ID_EXPEDITION_GUARDS_BOOTS, rowPane);
            }
        });
    }

    /**
     * Get the first available weapon in the guard it's inventory.
     *
     * @param guard the guard to get the weapon for.
     * @return the item stack containing the weapon or empty.
     */
    private ItemStack getFirstWeapon(final ICitizenDataView guard)
    {
        final int swordSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(guard.getInventory(), ToolType.SWORD, 0, 5);
        if (swordSlot != -1)
        {
            return guard.getInventory().getStackInSlot(swordSlot);
        }

        final int bowSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(guard.getInventory(), ToolType.BOW, 0, 5);
        if (bowSlot != -1)
        {
            return guard.getInventory().getStackInSlot(bowSlot);
        }

        final int axeSlot = InventoryUtils.getFirstSlotOfItemHandlerContainingTool(guard.getInventory(), ToolType.AXE, 0, 5);
        if (axeSlot != -1)
        {
            return guard.getInventory().getStackInSlot(axeSlot);
        }

        return ItemStack.EMPTY;
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

        Network.getNetwork().sendToServer(new TransferItemsMessage(visitorData, expeditionType, requirement.getId()));

        final int needed = requirement.getAmount() - requirement.getAmountAvailable();
        if (mc.player.isCreative())
        {
            InventoryUtils.addItemStackToItemHandler(visitorData.getInventory(), requirement.getDefaultItemStack().copyWithCount(needed));
        }
        else
        {
            InventoryUtils.transferItemStackIntoNextFreeSlotFromItemHandler(new InvWrapper(mc.player.getInventory()),
              requirement.getItemPredicate(),
              needed,
              visitorData.getInventory());
        }

        renderHeaders();
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
        expedition.setEquipment(equipment);
        expedition.addMember(new ExpeditionVisitorMember(visitorData));

        expedition.setStatus(ExpeditionStatus.EMBARKED);
        Network.getNetwork().sendToServer(new StartExpeditionMessage(visitorData.getColony(), expeditionType, expedition));

        close();
    }
}