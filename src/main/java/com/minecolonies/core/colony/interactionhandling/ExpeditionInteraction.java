package com.minecolonies.core.colony.interactionhandling;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.expeditions.ExpeditionStatus;
import com.minecolonies.api.colony.expeditions.IExpeditionMember;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.colony.managers.interfaces.expeditions.ColonyExpedition;
import com.minecolonies.api.colony.managers.interfaces.expeditions.CreatedExpedition;
import com.minecolonies.api.items.AbstractItemExpeditionSheet;
import com.minecolonies.api.items.AbstractItemExpeditionSheet.ExpeditionSheetInfo;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.events.ColonyExpeditionEvent;
import com.minecolonies.core.colony.expeditions.ExpeditionCitizenMember;
import com.minecolonies.core.colony.expeditions.ExpeditionVisitorMember;
import com.minecolonies.core.colony.expeditions.colony.ColonyExpeditionBuilder;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement.RequirementHandler;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionType;
import com.minecolonies.core.datalistener.ColonyExpeditionTypeListener;
import com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.DespawnTimeData.DespawnTime;
import com.minecolonies.core.items.ItemExpeditionSheet.ExpeditionSheetContainerManager;
import com.minecolonies.core.network.messages.server.colony.InteractionResponse;
import com.minecolonies.core.network.messages.server.colony.OpenInventoryMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static com.minecolonies.api.util.constant.Constants.TICKS_HOUR;
import static com.minecolonies.api.util.constant.ExpeditionConstants.*;
import static com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.DEFAULT_DESPAWN_TIME;
import static com.minecolonies.core.entity.visitor.ExpeditionaryVisitorType.EXTRA_DATA_DESPAWN_TIME;

/**
 * The interaction for expeditionary visitors.
 * <p>
 * This interaction has several states it can be in.
 * <ul>
 * <li>Accept phase: Option to accept or cancel the expedition.</li>
 * <li>Prepare phase: Option to finish, ask for the guide or cancel the expedition.</li>
 * <li>Finished phase: Option to view the loot, or ignore.</li>
 * </ul>
 */
public class ExpeditionInteraction extends ServerCitizenInteraction
{
    /**
     * All possible questions.
     */
    private static final Function<Component, Component> acceptInquiry   = (to) -> Component.translatable(EXPEDITION_INTERACTION_INQUIRY_ACCEPT, to);
    private static final Component                      prepareInquiry  = Component.translatable(EXPEDITION_INTERACTION_INQUIRY_PREPARE);
    private static final Component                      finishedInquiry = Component.translatable(EXPEDITION_INTERACTION_INQUIRY_FINISHED);

    /**
     * All possible answer fields.
     */
    private static final Component acceptOkAnswer        = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_CREATED_ACCEPT);
    private static final Component acceptCancelAnswer    = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_CREATED_NOT_INTERESTED);
    private static final Component prepareFinishAnswer   = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_ACCEPTED_START);
    private static final Component prepareGetSheetAnswer = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_ACCEPTED_GET_SHEET);
    private static final Component prepareLaterAnswer    = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_ACCEPTED_NOT_NOW);
    private static final Component prepareCancelAnswer   = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_ACCEPTED_NOT_INTERESTED);
    private static final Component finishedViewAnswer    = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_FINISHED_VIEW_RESULTS);
    private static final Component finishedLaterAnswer   = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_FINISHED_NOT_NOW);
    private static final Component finishedCancelAnswer  = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_FINISHED_NOT_INTERESTED);

    /**
     * Predicates
     */
    private static final BiPredicate<ItemStack, IColony> IS_FINISHED_EXPEDITION_SHEET = (stack, colony) -> {
        if (stack.getItem() instanceof AbstractItemExpeditionSheet expeditionSheet)
        {
            final ExpeditionSheetInfo expeditionSheetInfo = expeditionSheet.getExpeditionSheetInfo(stack);
            if (expeditionSheetInfo == null)
            {
                return false;
            }

            if (colony.getID() != expeditionSheetInfo.colonyId())
            {
                return false;
            }

            final CreatedExpedition createdExpedition = colony.getExpeditionManager().getCreatedExpedition(expeditionSheetInfo.expeditionId());
            if (createdExpedition == null)
            {
                return false;
            }

            return colony.getExpeditionManager().meetsRequirements(createdExpedition.expeditionTypeId(), new ExpeditionSheetContainerManager(stack));
        }

        return false;
    };

    /**
     * Default constructor.
     */
    public ExpeditionInteraction()
    {
        super(Component.empty(),
          true,
          ChatPriority.IMPORTANT,
          null,
          Component.translatable(EXPEDITION_INTERACTION_VALIDATOR_ID));
        this.loadValidator();
    }

    /**
     * Initializer constructor.
     *
     * @param data the input citizen data.
     */
    public ExpeditionInteraction(final ICitizen data)
    {
        super(data);
    }

    @Override
    public Component getInquiry(final Player player, final ICitizen data)
    {
        final ExpeditionStatus currentState = data.getColony().getExpeditionManager().getExpeditionStatus(data.getId());
        return switch (currentState)
        {
            case CREATED ->
            {
                final ResourceLocation expeditionTypeId = data.getColony().getExpeditionManager().getCreatedExpedition(data.getId()).expeditionTypeId();
                final ColonyExpeditionType expeditionType = ColonyExpeditionTypeListener.getExpeditionType(expeditionTypeId);
                if (expeditionType != null)
                {
                    yield acceptInquiry.apply(expeditionType.toText());
                }
                else
                {
                    yield Component.empty();
                }
            }
            case ACCEPTED -> prepareInquiry;
            case FINISHED -> finishedInquiry;
            default -> Component.empty();
        };
    }

    @Override
    public List<IInteractionResponseHandler> genChildInteractions()
    {
        return Collections.emptyList();
    }

    @Override
    public String getType()
    {
        return ModInteractionResponseHandlers.EXPEDITION.getPath();
    }

    @Override
    public ResourceLocation getInteractionIcon()
    {
        return new ResourceLocation(Constants.MOD_ID, EXPEDITION_INTERACTION_ICON);
    }

    @Override
    public List<Component> getPossibleResponses(final ICitizen data)
    {
        final ExpeditionStatus currentState = data.getColony().getExpeditionManager().getExpeditionStatus(data.getId());
        return switch (currentState)
        {
            case CREATED -> List.of(acceptOkAnswer, acceptCancelAnswer);
            case ACCEPTED -> List.of(prepareFinishAnswer, prepareGetSheetAnswer, prepareLaterAnswer, prepareCancelAnswer);
            case FINISHED -> List.of(finishedViewAnswer, finishedLaterAnswer, finishedCancelAnswer);
            default -> List.of();
        };
    }

    @Override
    public void onServerResponseTriggered(final int responseId, final Player player, final ICitizenData data)
    {
        if (data instanceof IVisitorData visitorData)
        {
            handleResponse(responseId, player, visitorData);
        }
    }

    @Override
    public boolean onClientResponseTriggered(final int responseId, final Player player, final ICitizenDataView data, final BOWindow window)
    {
        Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level.dimension(), this.getInquiry(), responseId));
        final boolean response = handleResponse(responseId, player, data);
        if (!response)
        {
            window.close();
            Minecraft.getInstance().popGuiLayer();
        }
        return response;
    }

    /**
     * Handle the response interaction, identical logic for server and client side.
     *
     * @param responseId the clicked index.
     * @param player     the player who clicked.
     * @param data       the visitor that was clicked.
     * @return if wishing to continue interacting.
     */
    private boolean handleResponse(final int responseId, final Player player, final ICitizen data)
    {
        final Component response = getPossibleResponses(data).get(responseId);
        final int expeditionId = data.getId();

        if (response.equals(acceptOkAnswer))
        {
            // Accept the expedition and reset the despawn time
            data.getColony().getExpeditionManager().acceptExpedition(expeditionId);
            if (data instanceof IVisitorData visitorData)
            {
                visitorData.setExtraDataValue(EXTRA_DATA_DESPAWN_TIME, DespawnTime.fromNow(player.level, DEFAULT_DESPAWN_TIME));
            }
        }

        if (response.equals(acceptOkAnswer) || response.equals(prepareGetSheetAnswer))
        {
            // Give the player the expedition sheet item
            if (!player.level.isClientSide)
            {
                final ItemStack expeditionSheet = ModItems.expeditionSheet.createItemStackForExpedition(new ExpeditionSheetInfo(data.getColony().getID(), expeditionId));
                if (!InventoryUtils.addItemStackToProvider(player, expeditionSheet))
                {
                    InventoryUtils.spawnItemStack(player.level, player.getX(), player.getY(), player.getZ(), expeditionSheet);
                }
            }
        }

        if (response.equals(prepareFinishAnswer))
        {
            // Attempt to start the expedition
            if (!player.level.isClientSide && data instanceof IVisitorData visitorData)
            {
                tryStartExpedition(visitorData, player);
            }
        }

        if (response.equals(acceptCancelAnswer) || response.equals(prepareCancelAnswer) || response.equals(finishedCancelAnswer))
        {
            // Remove the visitor and remove the expedition
            if (!player.level.isClientSide && data instanceof IVisitorData visitorData)
            {
                data.getColony().getVisitorManager().removeCivilian(visitorData);
            }
            data.getColony().getExpeditionManager().removeCreatedExpedition(expeditionId);
        }

        if (response.equals(finishedViewAnswer))
        {
            if (player.level.isClientSide && data instanceof IVisitorViewData visitorData)
            {
                // Open the loot view
                Network.getNetwork().sendToServer(new OpenInventoryMessage(visitorData.getColony(), data.getName(), visitorData.getEntityId()));
            }
        }

        return false;
    }

    /**
     * Starts the expedition, assuming all preconditions are met to be able to start said expedition.
     *
     * @param visitorData the visitor that is being talked to.
     * @param player      the current player.
     */
    private void tryStartExpedition(final IVisitorData visitorData, final Player player)
    {
        final IColony colony = visitorData.getColony();

        // Get the expedition instance.
        final CreatedExpedition createdExpedition = colony.getExpeditionManager().getCreatedExpedition(visitorData.getId());
        if (createdExpedition == null)
        {
            return;
        }

        // Get the expedition type.
        final ColonyExpeditionType colonyExpeditionType = ColonyExpeditionTypeListener.getExpeditionType(createdExpedition.expeditionTypeId());
        if (colonyExpeditionType == null)
        {
            Log.getLogger().warn("Starting expedition failed, expedition type '{}' does not exist on the server side.", createdExpedition.expeditionTypeId());
            return;
        }

        // Find in which slot a valid expedition sheet is located.
        final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(player.getInventory()), stack -> IS_FINISHED_EXPEDITION_SHEET.test(stack, colony));
        final ItemStack expeditionSheet = player.getInventory().getItem(slot);
        if (expeditionSheet.isEmpty())
        {
            return;
        }

        // Create all the data needed for creating an expedition
        final ExpeditionSheetContainerManager expeditionSheetContainerManager = new ExpeditionSheetContainerManager(expeditionSheet);

        final ColonyExpeditionBuilder colonyExpeditionBuilder = new ColonyExpeditionBuilder(new ExpeditionVisitorMember(visitorData));
        expeditionSheetContainerManager.getMembers()
          .forEach(memberId -> colonyExpeditionBuilder.addMember(new ExpeditionCitizenMember(colony.getCitizenManager().getCivilian(memberId))));

        // Process the requirements
        final IItemHandler handler = new InvWrapper(expeditionSheetContainerManager);
        for (final ColonyExpeditionRequirement requirement : colonyExpeditionType.requirements())
        {
            final RequirementHandler requirementHandler = requirement.createHandler(handler);
            List<ItemStack> matchingItems = InventoryUtils.filterItemHandler(handler, requirementHandler.getItemPredicate());
            matchingItems.forEach(item -> requirementHandler.processOnStart(colonyExpeditionBuilder, item));
        }

        // Attempt to start the expedition
        if (!colony.getExpeditionManager().startExpedition(visitorData.getId(), colonyExpeditionBuilder))
        {
            return;
        }

        // Remove the expedition sheet from the inventory
        player.getInventory().removeItem(slot, 1);

        // Create the event related to this expedition.
        final ColonyExpedition expedition = Objects.requireNonNull(colony.getExpeditionManager().getActiveExpedition(visitorData.getId()));
        colony.getEventManager().addEvent(new ColonyExpeditionEvent(colony, expedition));

        // Send expedition start message
        MessageUtils.format(EXPEDITION_START_MESSAGE, visitorData.getName())
          .withPriority(MessagePriority.IMPORTANT)
          .sendTo(colony)
          .forManagers();

        // Add all members to the travelling manager and de-spawn them.
        final BlockPos townHallReturnPosition =
          BlockPosUtil.findSpawnPosAround(colony.getWorld(), colony.getBuildingManager().getTownHall().getPosition());
        for (final IExpeditionMember<?> member : expedition.getMembers())
        {
            colony.getTravelingManager().startTravellingTo(member.getId(), townHallReturnPosition, TICKS_HOUR, false);

            final ICivilianData memberData = member.resolveCivilianData(colony);
            if (memberData != null)
            {
                memberData.getEntity().ifPresent(entity -> entity.remove(RemovalReason.DISCARDED));
            }
        }
    }
}