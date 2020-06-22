package com.minecolonies.coremod.network;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.network.messages.PermissionsMessage;
import com.minecolonies.coremod.network.messages.client.*;
import com.minecolonies.coremod.network.messages.client.colony.*;
import com.minecolonies.coremod.network.messages.client.colony.building.guard.GuardMobAttackListMessage;
import com.minecolonies.coremod.network.messages.server.*;
import com.minecolonies.coremod.network.messages.server.colony.*;
import com.minecolonies.coremod.network.messages.server.colony.building.*;
import com.minecolonies.coremod.network.messages.server.colony.building.beekeeper.BeekeeperScepterMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.beekeeper.BeekeeperSetHarvestHoneycombsMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.composter.ComposterRetrievalMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.cowboy.CowboySetMilkCowsMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.crusher.CrusherSetModeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.enchanter.EnchanterWorkerSetMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.farmer.AssignFieldMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.farmer.AssignmentModeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.guard.GuardRecalculateMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.guard.GuardTaskMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.guard.MobEntryChangeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.home.AssignUnassignMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.lumberjack.LumberjackReplantSaplingToggleMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.lumberjack.LumberjackRestrictionToggleMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.lumberjack.LumberjackScepterMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.miner.MinerSetLevelMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.plantation.PlantationSetPhaseMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.postbox.PostBoxRequestMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.shepherd.ShepherdSetDyeSheepsMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.sifter.SifterSettingsMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.university.TryResearchMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.warehouse.SortWarehouseMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.warehouse.UpgradeWarehouseMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.AddRemoveRecipeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.BuildingHiringModeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.ChangeRecipePriorityMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.RecallCitizenMessage;
import com.minecolonies.coremod.network.messages.server.colony.citizen.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * Our wrapper for Forge network layer
 */
public class NetworkChannel
{
    private static final String        LATEST_PROTO_VER    = "1.0";
    private static final String        ACCEPTED_PROTO_VERS = LATEST_PROTO_VER;
    /**
     * Forge network channel
     */
    private final        SimpleChannel rawChannel;

    /**
     * Creates a new instance of network channel.
     *
     * @param channelName unique channel name
     * @throws IllegalArgumentException if channelName already exists
     */
    public NetworkChannel(final String channelName)
    {
        rawChannel =
          NetworkRegistry.newSimpleChannel(new ResourceLocation(Constants.MOD_ID, channelName), () -> LATEST_PROTO_VER, ACCEPTED_PROTO_VERS::equals, ACCEPTED_PROTO_VERS::equals);
    }

    /**
     * Registers all common messages.
     */
    public void registerCommonMessages()
    {
        int idx = 0;
        registerMessage(++idx, ServerUUIDMessage.class, ServerUUIDMessage::new);

        //  ColonyView messages
        registerMessage(++idx, ColonyViewMessage.class, ColonyViewMessage::new);
        registerMessage(++idx, ColonyViewCitizenViewMessage.class, ColonyViewCitizenViewMessage::new);
        registerMessage(++idx, ColonyViewRemoveCitizenMessage.class, ColonyViewRemoveCitizenMessage::new);
        registerMessage(++idx, ColonyViewBuildingViewMessage.class, ColonyViewBuildingViewMessage::new);
        registerMessage(++idx, ColonyViewRemoveBuildingMessage.class, ColonyViewRemoveBuildingMessage::new);
        registerMessage(++idx, PermissionsMessage.View.class, PermissionsMessage.View::new);
        registerMessage(++idx, ColonyStylesMessage.class, ColonyStylesMessage::new);
        registerMessage(++idx, ColonyViewWorkOrderMessage.class, ColonyViewWorkOrderMessage::new);
        registerMessage(++idx, ColonyViewRemoveWorkOrderMessage.class, ColonyViewRemoveWorkOrderMessage::new);
        registerMessage(++idx, UpdateChunkCapabilityMessage.class, UpdateChunkCapabilityMessage::new);
        registerMessage(++idx, GuardMobAttackListMessage.class, GuardMobAttackListMessage::new);
        registerMessage(++idx, BeekeeperSetHarvestHoneycombsMessage.class, BeekeeperSetHarvestHoneycombsMessage::new);

        //  Permission Request messages
        registerMessage(++idx, PermissionsMessage.Permission.class, PermissionsMessage.Permission::new);
        registerMessage(++idx, PermissionsMessage.AddPlayer.class, PermissionsMessage.AddPlayer::new);
        registerMessage(++idx, PermissionsMessage.RemovePlayer.class, PermissionsMessage.RemovePlayer::new);
        registerMessage(++idx, PermissionsMessage.ChangePlayerRank.class, PermissionsMessage.ChangePlayerRank::new);
        registerMessage(++idx, PermissionsMessage.AddPlayerOrFakePlayer.class, PermissionsMessage.AddPlayerOrFakePlayer::new);

        //  Colony Request messages
        registerMessage(++idx, BuildRequestMessage.class, BuildRequestMessage::new);
        registerMessage(++idx, OpenInventoryMessage.class, OpenInventoryMessage::new);
        registerMessage(++idx, TownHallRenameMessage.class, TownHallRenameMessage::new);
        registerMessage(++idx, MinerSetLevelMessage.class, MinerSetLevelMessage::new);
        registerMessage(++idx, RecallCitizenMessage.class, RecallCitizenMessage::new);
        registerMessage(++idx, BuildToolPlaceMessage.class, BuildToolPlaceMessage::new);
        registerMessage(++idx, ToggleJobMessage.class, ToggleJobMessage::new);
        registerMessage(++idx, HireFireMessage.class, HireFireMessage::new);
        registerMessage(++idx, WorkOrderChangeMessage.class, WorkOrderChangeMessage::new);
        registerMessage(++idx, AssignFieldMessage.class, AssignFieldMessage::new);
        registerMessage(++idx, AssignmentModeMessage.class, AssignmentModeMessage::new);
        registerMessage(++idx, GuardTaskMessage.class, GuardTaskMessage::new);
        registerMessage(++idx, GuardRecalculateMessage.class, GuardRecalculateMessage::new);
        registerMessage(++idx, MobEntryChangeMessage.class, MobEntryChangeMessage::new);
        registerMessage(++idx, GuardScepterMessage.class, GuardScepterMessage::new);
        registerMessage(++idx, RecallTownhallMessage.class, RecallTownhallMessage::new);
        registerMessage(++idx, TransferItemsRequestMessage.class, TransferItemsRequestMessage::new);
        registerMessage(++idx, MarkBuildingDirtyMessage.class, MarkBuildingDirtyMessage::new);
        registerMessage(++idx, ChangeFreeToInteractBlockMessage.class, ChangeFreeToInteractBlockMessage::new);
        registerMessage(++idx, LumberjackReplantSaplingToggleMessage.class, LumberjackReplantSaplingToggleMessage::new);
        registerMessage(++idx, LumberjackRestrictionToggleMessage.class, LumberjackRestrictionToggleMessage::new);
        registerMessage(++idx, LumberjackScepterMessage.class, LumberjackScepterMessage::new);
        registerMessage(++idx, CreateColonyMessage.class, CreateColonyMessage::new);
        registerMessage(++idx, ColonyDeleteOwnMessage.class, ColonyDeleteOwnMessage::new);
        registerMessage(++idx, ColonyViewRemoveMessage.class, ColonyViewRemoveMessage::new);
        registerMessage(++idx, BeekeeperScepterMessage.class, BeekeeperScepterMessage::new);

        registerMessage(++idx, ToggleHousingMessage.class, ToggleHousingMessage::new);
        registerMessage(++idx, ToggleMoveInMessage.class, ToggleMoveInMessage::new);
        registerMessage(++idx, AssignUnassignMessage.class, AssignUnassignMessage::new);
        registerMessage(++idx, OpenCraftingGUIMessage.class, OpenCraftingGUIMessage::new);
        registerMessage(++idx, AddRemoveRecipeMessage.class, AddRemoveRecipeMessage::new);
        registerMessage(++idx, ChangeRecipePriorityMessage.class, ChangeRecipePriorityMessage::new);
        registerMessage(++idx, ChangeDeliveryPriorityMessage.class, ChangeDeliveryPriorityMessage::new);
        registerMessage(++idx, ForcePickupMessage.class, ForcePickupMessage::new);
        registerMessage(++idx, UpgradeWarehouseMessage.class, UpgradeWarehouseMessage::new);
        registerMessage(++idx, BuildToolPasteMessage.class, BuildToolPasteMessage::new);
        registerMessage(++idx, TransferItemsToCitizenRequestMessage.class, TransferItemsToCitizenRequestMessage::new);
        registerMessage(++idx, UpdateRequestStateMessage.class, UpdateRequestStateMessage::new);
        registerMessage(++idx, BuildingSetStyleMessage.class, BuildingSetStyleMessage::new);
        registerMessage(++idx, CowboySetMilkCowsMessage.class, CowboySetMilkCowsMessage::new);
        registerMessage(++idx, RecallSingleCitizenMessage.class, RecallSingleCitizenMessage::new);
        registerMessage(++idx, AssignFilterableItemMessage.class, AssignFilterableItemMessage::new);
        registerMessage(++idx, TeamColonyColorChangeMessage.class, TeamColonyColorChangeMessage::new);
        registerMessage(++idx, ToggleHelpMessage.class, ToggleHelpMessage::new);
        registerMessage(++idx, PauseCitizenMessage.class, PauseCitizenMessage::new);
        registerMessage(++idx, RestartCitizenMessage.class, RestartCitizenMessage::new);
        registerMessage(++idx, SortWarehouseMessage.class, SortWarehouseMessage::new);
        registerMessage(++idx, PostBoxRequestMessage.class, PostBoxRequestMessage::new);
        registerMessage(++idx, ComposterRetrievalMessage.class, ComposterRetrievalMessage::new);
        registerMessage(++idx, CrusherSetModeMessage.class, CrusherSetModeMessage::new);
        registerMessage(++idx, BuyCitizenMessage.class, BuyCitizenMessage::new);
        registerMessage(++idx, HireMercenaryMessage.class, HireMercenaryMessage::new);
        registerMessage(++idx, ShepherdSetDyeSheepsMessage.class, ShepherdSetDyeSheepsMessage::new);
        registerMessage(++idx, SifterSettingsMessage.class, SifterSettingsMessage::new);
        registerMessage(++idx, HutRenameMessage.class, HutRenameMessage::new);
        registerMessage(++idx, BuildingHiringModeMessage.class, BuildingHiringModeMessage::new);
        registerMessage(++idx, DecorationBuildRequestMessage.class, DecorationBuildRequestMessage::new);
        registerMessage(++idx, DecorationControllerUpdateMessage.class, DecorationControllerUpdateMessage::new);
        registerMessage(++idx, DirectPlaceMessage.class, DirectPlaceMessage::new);
        registerMessage(++idx, TeleportToColonyMessage.class, TeleportToColonyMessage::new);
        registerMessage(++idx, EnchanterWorkerSetMessage.class, EnchanterWorkerSetMessage::new);
        registerMessage(++idx, TriggerServerResponseHandlerMessage.class, TriggerServerResponseHandlerMessage::new);
        registerMessage(++idx, TryResearchMessage.class, TryResearchMessage::new);
        registerMessage(++idx, HireSpiesMessage.class, HireSpiesMessage::new);
        registerMessage(++idx, AddMinimumStockToBuildingMessage.class, AddMinimumStockToBuildingMessage::new);
        registerMessage(++idx, RemoveMinimumStockFromBuildingMessage.class, RemoveMinimumStockFromBuildingMessage::new);
        registerMessage(++idx, PlantationSetPhaseMessage.class, PlantationSetPhaseMessage::new);

        //Client side only
        registerMessage(++idx, BlockParticleEffectMessage.class, BlockParticleEffectMessage::new);
        registerMessage(++idx, CompostParticleMessage.class, CompostParticleMessage::new);
        registerMessage(++idx, ItemParticleEffectMessage.class, ItemParticleEffectMessage::new);
        registerMessage(++idx, LocalizedParticleEffectMessage.class, LocalizedParticleEffectMessage::new);
        registerMessage(++idx, UpdateChunkRangeCapabilityMessage.class, UpdateChunkRangeCapabilityMessage::new);
        registerMessage(++idx, OpenSuggestionWindowMessage.class, OpenSuggestionWindowMessage::new);
        registerMessage(++idx, UpdateClientWithRecipesMessage.class, UpdateClientWithRecipesMessage::new);
        registerMessage(++idx, CircleParticleEffectMessage.class, CircleParticleEffectMessage::new);
        registerMessage(++idx, StreamParticleEffectMessage.class, StreamParticleEffectMessage::new);
        registerMessage(++idx, SleepingParticleMessage.class, SleepingParticleMessage::new);
        registerMessage(++idx, VanillaParticleMessage.class, VanillaParticleMessage::new);
        registerMessage(++idx, StopMusicMessage.class, StopMusicMessage::new);
        registerMessage(++idx, PlayMusicMessage.class, PlayMusicMessage::new);

        //JEI Messages
        registerMessage(++idx, TransferRecipeCrafingTeachingMessage.class, TransferRecipeCrafingTeachingMessage::new);

        //Advancement Messages
        registerMessage(++idx, OpenGuiWindowTriggerMessage.class, OpenGuiWindowTriggerMessage::new);
        registerMessage(++idx, ClickGuiButtonTriggerMessage.class, ClickGuiButtonTriggerMessage::new);

        // Colony-Independent items
        registerMessage(++idx, RemoveFromRallyingListMessage.class, RemoveFromRallyingListMessage::new);
        registerMessage(++idx, ToggleBannerRallyGuardsMessage.class, ToggleBannerRallyGuardsMessage::new);

    }

    /**
     * Register a message into rawChannel.
     *
     * @param <MSG>      message class type
     * @param id         network id
     * @param msgClazz   message class
     * @param msgCreator supplier with new instance of msgClazz
     */
    private <MSG extends IMessage> void registerMessage(final int id, final Class<MSG> msgClazz, final Supplier<MSG> msgCreator)
    {
        rawChannel.registerMessage(id, msgClazz, (msg, buf) -> msg.toBytes(buf), (buf) -> {
            final MSG msg = msgCreator.get();
            msg.fromBytes(buf);
            return msg;
        }, (msg, ctxIn) -> {
            final Context ctx = ctxIn.get();
            final LogicalSide packetOrigin = ctx.getDirection().getOriginationSide();
            ctx.setPacketHandled(true);
            if (msg.getExecutionSide() != null && packetOrigin.equals(msg.getExecutionSide()))
            {
                Log.getLogger().warn("Receving {} at wrong side!", msg.getClass().getName());
                return;
            }
            // boolean param MUST equals true if packet arrived at logical server
            ctx.enqueueWork(() -> msg.onExecute(ctx, packetOrigin.equals(LogicalSide.CLIENT)));
        });
    }

    /**
     * Sends to server.
     *
     * @param msg message to send
     */
    public void sendToServer(final IMessage msg)
    {
        rawChannel.sendToServer(msg);
    }

    /**
     * Sends to player.
     *
     * @param msg    message to send
     * @param player target player
     */
    public void sendToPlayer(final IMessage msg, final ServerPlayerEntity player)
    {
        rawChannel.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    /**
     * Sends to origin client.
     *
     * @param msg message to send
     * @param ctx network context
     */
    public void sendToOrigin(final IMessage msg, final Context ctx)
    {
        final ServerPlayerEntity player = ctx.getSender();
        if (player != null) // side check
        {
            sendToPlayer(msg, player);
        }
        else
        {
            sendToServer(msg);
        }
    }

    /**
     * Sends to everyone in dimension.
     *
     * @param msg message to send
     * @param dim target dimension
     */
    public void sendToDimension(final IMessage msg, final DimensionType dim)
    {
        rawChannel.send(PacketDistributor.DIMENSION.with(() -> dim), msg);
    }

    /**
     * Sends to everyone in circle made using given target point.
     *
     * @param msg message to send
     * @param pos target position and radius
     * @see TargetPoint
     */
    public void sendToPosition(final IMessage msg, final TargetPoint pos)
    {
        rawChannel.send(PacketDistributor.NEAR.with(() -> pos), msg);
    }

    /**
     * Sends to everyone.
     *
     * @param msg message to send
     */
    public void sendToEveryone(final IMessage msg)
    {
        rawChannel.send(PacketDistributor.ALL.noArg(), msg);
    }

    /**
     * Sends to everyone who is in range from entity's pos using formula below.
     *
     * <pre>
     * Math.min(Entity.getType().getTrackingRange(), ChunkManager.this.viewDistance - 1) * 16;
     * </pre>
     * <p>
     * as of 24-06-2019
     *
     * @param msg    message to send
     * @param entity target entity to look at
     */
    public void sendToTrackingEntity(final IMessage msg, final Entity entity)
    {
        rawChannel.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), msg);
    }

    /**
     * Sends to everyone (including given entity) who is in range from entity's pos using formula below.
     *
     * <pre>
     * Math.min(Entity.getType().getTrackingRange(), ChunkManager.this.viewDistance - 1) * 16;
     * </pre>
     * <p>
     * as of 24-06-2019
     *
     * @param msg    message to send
     * @param entity target entity to look at
     */
    public void sendToTrackingEntityAndSelf(final IMessage msg, final Entity entity)
    {
        rawChannel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), msg);
    }

    /**
     * Sends to everyone in given chunk.
     *
     * @param msg   message to send
     * @param chunk target chunk to look at
     */
    public void sendToTrackingChunk(final IMessage msg, final Chunk chunk)
    {
        rawChannel.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), msg);
    }
}
