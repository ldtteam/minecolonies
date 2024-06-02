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
import com.minecolonies.api.entity.visitor.ModVisitorTypes;
import com.minecolonies.api.items.AbstractItemExpeditionSheet.ExpeditionSheetInfo;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.MessageUtils.MessagePriority;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.expeditions.ExpeditionCitizenMember;
import com.minecolonies.core.colony.expeditions.ExpeditionVisitorMember;
import com.minecolonies.core.colony.events.ColonyExpeditionEvent;
import com.minecolonies.core.items.ItemExpeditionSheet.ExpeditionSheetContainerManager;
import com.minecolonies.core.network.messages.server.colony.InteractionResponse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.TICKS_HOUR;
import static com.minecolonies.api.util.constant.ExpeditionConstants.*;

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
    private static final Component acceptInquiry   = Component.translatable(EXPEDITION_INTERACTION_INQUIRY_ACCEPT);
    private static final Component prepareInquiry  = Component.translatable(EXPEDITION_INTERACTION_INQUIRY_PREPARE);
    private static final Component finishedInquiry = Component.translatable(EXPEDITION_INTERACTION_INQUIRY_FINISHED);

    /**
     * All possible answer fields.
     */
    private static final Component acceptOkAnswer        = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_ACCEPT);
    private static final Component acceptCancelAnswer    = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_NOT_INTERESTED);
    private static final Component prepareFinishAnswer   = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_ACCEPT);
    private static final Component prepareGetSheetAnswer = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_GET_SHEET);
    private static final Component prepareLaterAnswer    = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_NOT_NOW);
    private static final Component prepareCancelAnswer   = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_NOT_INTERESTED);
    private static final Component finishedViewAnswer    = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_NOT_INTERESTED);
    private static final Component finishedLaterAnswer   = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_NOT_NOW);
    private static final Component finishedCancelAnswer  = Component.translatable(EXPEDITION_INTERACTION_RESPONSE_NOT_INTERESTED);

    /**
     * Default constructor.
     */
    public ExpeditionInteraction()
    {
        super(Component.empty(),
          true,
          ChatPriority.IMPORTANT,
          data -> data instanceof IVisitorData visitorData && visitorData.getVisitorType().equals(ModVisitorTypes.expeditionary.get()),
          null);
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
            case CREATED -> acceptInquiry;
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
        return handleResponse(responseId, player, data);
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
            data.getColony().getExpeditionManager().acceptExpedition(expeditionId);
        }

        if (response.equals(acceptOkAnswer) || response.equals(prepareGetSheetAnswer))
        {
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
            if (!player.level.isClientSide && data instanceof IVisitorData visitorData)
            {
                tryStartExpedition(visitorData, player);
            }
        }

        if (response.equals(acceptCancelAnswer) || response.equals(prepareCancelAnswer))
        {
            if (!player.level.isClientSide && data instanceof IVisitorData visitorData)
            {
                data.getColony().getVisitorManager().removeCivilian(visitorData);
            }
            data.getColony().getExpeditionManager().removeCreatedExpedition(expeditionId);
        }

        return true;
    }

    private void tryStartExpedition(final IVisitorData data, final Player player)
    {
        // Try to find the first expedition sheet in the player their inventory that meets the requirements.
        final Optional<ItemStack> expeditionSheet = InventoryUtils.filterItemHandler(new InvWrapper(player.getInventory()), stack -> {
            if (!stack.is(ModItems.expeditionSheet))
            {
                return false;
            }

            final CreatedExpedition createdExpedition = data.getColony().getExpeditionManager().getCreatedExpedition(data.getId());
            if (createdExpedition == null)
            {
                return false;
            }

            return data.getColony().getExpeditionManager().meetsRequirements(createdExpedition.expeditionTypeId(), new ExpeditionSheetContainerManager(stack));
        }).stream().findFirst();

        if (expeditionSheet.isEmpty())
        {
            return;
        }

        // Create all the data needed for creating an expedition
        final ExpeditionSheetContainerManager expeditionSheetContainerManager = new ExpeditionSheetContainerManager(expeditionSheet.get());
        final IExpeditionMember<?> leader = new ExpeditionVisitorMember(data);
        final List<IExpeditionMember<?>> members = new ArrayList<>(List.of(leader));
        for (final int id : expeditionSheetContainerManager.getMembers())
        {
            members.add(new ExpeditionCitizenMember(data.getColony().getCitizenManager().getCivilian(id)));
        }
        final List<ItemStack> equipment = InventoryUtils.getItemHandlerAsList(new InvWrapper(expeditionSheetContainerManager));

        // Attempt to start the expedition
        if (!data.getColony().getExpeditionManager().startExpedition(data.getId(), members, equipment))
        {
            return;
        }

        final ColonyExpedition expedition = Objects.requireNonNull(data.getColony().getExpeditionManager().getActiveExpedition(data.getId()));

        MessageUtils.format(EXPEDITION_START_MESSAGE, leader.getName())
          .withPriority(MessagePriority.IMPORTANT)
          .sendTo(data.getColony())
          .forManagers();

        // Create the event related to this expedition.
        data.getColony().getEventManager().addEvent(new ColonyExpeditionEvent(data.getColony(), expedition));

        // Add all members to the travelling manager and de-spawn them.
        final BlockPos townHallReturnPosition = BlockPosUtil.findSpawnPosAround(data.getColony().getWorld(), data.getColony().getBuildingManager().getTownHall().getPosition());
        for (final IExpeditionMember<?> member : expedition.getMembers())
        {
            data.getColony().getTravelingManager().startTravellingTo(member.getId(), townHallReturnPosition, TICKS_HOUR, false);

            final ICivilianData memberData = member.resolveCivilianData(data.getColony());
            if (memberData != null)
            {
                memberData.getEntity().ifPresent(entity -> entity.remove(RemovalReason.DISCARDED));
            }
        }
    }
}