package com.minecolonies.coremod.network;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
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
import com.minecolonies.coremod.network.messages.server.colony.building.builder.BuilderSelectWorkOrderMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.builder.BuilderSetManualModeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.composter.ComposterRetrievalMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.cowboy.CowboySetMilkCowsMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.crusher.CrusherSetModeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.enchanter.EnchanterWorkerSetMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.farmer.AssignFieldMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.farmer.AssignmentModeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.farmer.RequestFertilizerMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.guard.GuardRecalculateMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.guard.GuardTaskMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.guard.MobEntryChangeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.herder.HerderSetBreedingMessage;
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
import com.minecolonies.coremod.network.messages.splitting.SplitPacketMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Our wrapper for Forge network layer
 */
public class NetworkChannel
{
    /**
     * Forge network channel
     */
    private final        SimpleChannel rawChannel;

    /**
     * The messages that this channel can process, as viewed from a message id.
     */
    private final Map<Integer, NetworkingMessageEntry<?>> messagesTypes = Maps.newHashMap();

    /**
     * The message that this channel can process, as viewed from a message type.
     */
    private final Map<Class<? extends IMessage>, Integer> messageTypeToIdMap = Maps.newHashMap();

    /**
     * Cache of partially received messages, this holds the data untill it is processed.
     */
    private final Cache<Integer, Map<Integer, byte[]>> messageCache = CacheBuilder.newBuilder()
                                                                        .expireAfterAccess(1, TimeUnit.MINUTES)
                                                                        .concurrencyLevel(8)
                                                                        .build();

    /**
     * An atomic counter which keeps track of the split messages that have been send to somewhere from this network node.
     */
    private final AtomicInteger messageCounter = new AtomicInteger();

    /**
     * Creates a new instance of network channel.
     *
     * @param channelName unique channel name
     * @throws IllegalArgumentException if channelName already exists
     */
    public NetworkChannel(final String channelName)
    {
        final String modVersion = ModList.get().getModContainerById(Constants.MOD_ID).get().getModInfo().getVersion().toString();
        rawChannel = NetworkRegistry.newSimpleChannel(new ResourceLocation(Constants.MOD_ID, channelName), () -> modVersion, str -> str.equals(modVersion), str -> str.equals(modVersion));
    }

    /**
     * Registers all common messages.
     */
    public void registerCommonMessages()
    {
        setupInternalMessages();

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
        registerMessage(++idx, RequestFertilizerMessage.class, RequestFertilizerMessage::new);
        registerMessage(++idx, GuardTaskMessage.class, GuardTaskMessage::new);
        registerMessage(++idx, GuardRecalculateMessage.class, GuardRecalculateMessage::new);
        registerMessage(++idx, MobEntryChangeMessage.class, MobEntryChangeMessage::new);
        registerMessage(++idx, GuardScepterMessage.class, GuardScepterMessage::new);
        registerMessage(++idx, RecallCitizenHutMessage.class, RecallCitizenHutMessage::new);
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
        registerMessage(++idx, BuilderSetManualModeMessage.class, BuilderSetManualModeMessage::new);
        registerMessage(++idx, CowboySetMilkCowsMessage.class, CowboySetMilkCowsMessage::new);
        registerMessage(++idx, HerderSetBreedingMessage.class, HerderSetBreedingMessage::new);
        registerMessage(++idx, RecallSingleCitizenMessage.class, RecallSingleCitizenMessage::new);
        registerMessage(++idx, AssignFilterableItemMessage.class, AssignFilterableItemMessage::new);
        registerMessage(++idx, TeamColonyColorChangeMessage.class, TeamColonyColorChangeMessage::new);
        registerMessage(++idx, ColonyFlagChangeMessage.class, ColonyFlagChangeMessage::new);
        registerMessage(++idx, ToggleHelpMessage.class, ToggleHelpMessage::new);
        registerMessage(++idx, PauseCitizenMessage.class, PauseCitizenMessage::new);
        registerMessage(++idx, RestartCitizenMessage.class, RestartCitizenMessage::new);
        registerMessage(++idx, SortWarehouseMessage.class, SortWarehouseMessage::new);
        registerMessage(++idx, PostBoxRequestMessage.class, PostBoxRequestMessage::new);
        registerMessage(++idx, ComposterRetrievalMessage.class, ComposterRetrievalMessage::new);
        registerMessage(++idx, CrusherSetModeMessage.class, CrusherSetModeMessage::new);
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
        registerMessage(++idx, InteractionResponse.class, InteractionResponse::new);
        registerMessage(++idx, TryResearchMessage.class, TryResearchMessage::new);
        registerMessage(++idx, HireSpiesMessage.class, HireSpiesMessage::new);
        registerMessage(++idx, AddMinimumStockToBuildingMessage.class, AddMinimumStockToBuildingMessage::new);
        registerMessage(++idx, RemoveMinimumStockFromBuildingMessage.class, RemoveMinimumStockFromBuildingMessage::new);
        registerMessage(++idx, PlantationSetPhaseMessage.class, PlantationSetPhaseMessage::new);
        registerMessage(++idx, FieldPlotResizeMessage.class, FieldPlotResizeMessage::new);
        registerMessage(++idx, AdjustSkillCitizenMessage.class, AdjustSkillCitizenMessage::new);
        registerMessage(++idx, BuilderSelectWorkOrderMessage.class, BuilderSelectWorkOrderMessage::new);

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
        registerMessage(++idx, PlayAudioMessage.class, PlayAudioMessage::new);
        registerMessage(++idx, PlayMusicAtPosMessage.class, PlayMusicAtPosMessage::new);
        registerMessage(++idx, ColonyVisitorViewDataMessage.class, ColonyVisitorViewDataMessage::new);

        //JEI Messages
        registerMessage(++idx, TransferRecipeCrafingTeachingMessage.class, TransferRecipeCrafingTeachingMessage::new);

        //Advancement Messages
        registerMessage(++idx, OpenGuiWindowTriggerMessage.class, OpenGuiWindowTriggerMessage::new);
        registerMessage(++idx, ClickGuiButtonTriggerMessage.class, ClickGuiButtonTriggerMessage::new);

        // Colony-Independent items
        registerMessage(++idx, RemoveFromRallyingListMessage.class, RemoveFromRallyingListMessage::new);
        registerMessage(++idx, ToggleBannerRallyGuardsMessage.class, ToggleBannerRallyGuardsMessage::new);
    }

    private void setupInternalMessages()
    {
        rawChannel.registerMessage(0, SplitPacketMessage.class, IMessage::toBytes, (buf) -> {
            final SplitPacketMessage msg = new SplitPacketMessage();
            msg.fromBytes(buf);
            return msg;
        }, (msg, ctxIn) -> {
            final Context ctx = ctxIn.get();
            final LogicalSide packetOrigin = ctx.getDirection().getOriginationSide();
            ctx.setPacketHandled(true);
            msg.onExecute(ctx, packetOrigin.equals(LogicalSide.CLIENT));
        });
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
        this.messagesTypes.put(id, new NetworkingMessageEntry<>(msgCreator));
        this.messageTypeToIdMap.put(msgClazz, id);
    }

    /**
     * Sends to server.
     *
     * @param msg message to send
     */
    public void sendToServer(final IMessage msg)
    {
        handleSplitting(msg, rawChannel::sendToServer);
    }

    /**
     * Sends to player.
     *
     * @param msg    message to send
     * @param player target player
     */
    public void sendToPlayer(final IMessage msg, final ServerPlayerEntity player)
    {
        handleSplitting(msg, s -> rawChannel.send(PacketDistributor.PLAYER.with(() -> player), s));
    }

    /**
     * Sends the message to the origin of a different message based on the networking context given.
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
    public void sendToDimension(final IMessage msg, final ResourceLocation dim)
    {
        rawChannel.send(PacketDistributor.DIMENSION.with(() -> RegistryKey.getOrCreateKey(Registry.WORLD_KEY, dim)), msg);
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
        handleSplitting(msg, s -> rawChannel.send(PacketDistributor.NEAR.with(() -> pos), s));
    }

    /**
     * Sends to everyone.
     *
     * @param msg message to send
     */
    public void sendToEveryone(final IMessage msg)
    {
        handleSplitting(msg, s -> rawChannel.send(PacketDistributor.ALL.noArg(), s));
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
        handleSplitting(msg, s -> rawChannel.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), s));
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
        handleSplitting(msg, s -> rawChannel.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), s));
    }

    /**
     * Sends to everyone in given chunk.
     *
     * @param msg   message to send
     * @param chunk target chunk to look at
     */
    public void sendToTrackingChunk(final IMessage msg, final Chunk chunk)
    {
        handleSplitting(msg, s -> rawChannel.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), s));
    }

    /**
     * Method that handles the splitting of the message into chunks if need be.
     *
     * @param msg                  The message to split in question.
     * @param splitMessageConsumer The consumer that sends away the split parts of the message.
     */
    private void handleSplitting(final IMessage msg, final Consumer<IMessage> splitMessageConsumer)
    {
        //Get the inner message id and check if it is known.
        final int messageId = this.messageTypeToIdMap.getOrDefault(msg.getClass(), -1);
        if (messageId == -1)
        {
            throw new IllegalArgumentException("The message is unknown to this channel!");
        }

        //Write the message into a buffer and copy that buffer into a byte array for processing.
        final ByteBuf buffer = Unpooled.buffer();
        final PacketBuffer innerPacketBuffer = new PacketBuffer(buffer);
        msg.toBytes(innerPacketBuffer);
        final byte[] data = buffer.array();
        buffer.release();

        //Some tracking variables.
        //Max packet size: 90% of maximum.
        final int max_packet_size = 943718; //This is 90% of max packet size.
        //The current index in the data array.
        int currentIndex = 0;
        //The current index for the split packets.
        int packetIndex = 0;
        //The communication id.
        final int comId = messageCounter.getAndIncrement();

        //Loop while data is available.
        while (currentIndex < data.length)
        {
            //Tell the network message entry that we are splitting a packet.
            this.getMessagesTypes().get(messageId).onSplitting(packetIndex);

            final int extra = Math.min(max_packet_size, data.length - currentIndex);
            //Extract the sub data array.
            final byte[] subPacketData = Arrays.copyOfRange(data, currentIndex, currentIndex + extra);

            //Construct the wrapping packet.
            final SplitPacketMessage splitPacketMessage = new SplitPacketMessage(comId, packetIndex++, (currentIndex + extra) >= data.length, messageId, subPacketData);

            //Send the wrapping packet.
            splitMessageConsumer.accept(splitPacketMessage);

            //Move our working index.
            currentIndex += extra;
        }
    }

    /**
     * Gives access to the cache of messages that are being received.
     *
     * @return The message cache.
     */
    public Cache<Integer, Map<Integer, byte[]>> getMessageCache()
    {
        return messageCache;
    }

    /**
     * Gives access to the internal index codec.
     *
     * @return The internal index codec map.
     */
    public Map<Integer, NetworkingMessageEntry<?>> getMessagesTypes()
    {
        return messagesTypes;
    }

    /**
     * A class that handles the data wrapping for our inner index codec.
     *
     * @param <MSG> The message type.
     */
    public static final class NetworkingMessageEntry<MSG extends IMessage>
    {
        /**
         * Atomic boolean that tracks if a splitting warning has been written to the log for a given packet type.
         */
        private final AtomicBoolean hasWarned = new AtomicBoolean(true);

        /**
         * A callback to create a new message instance.
         */
        private final Supplier<MSG> creator;

        private NetworkingMessageEntry(final Supplier<MSG> creator) {this.creator = creator;}

        /**
         * Gives access to the callback that creates a new message instance.
         *
         * @return The callback.
         */
        public Supplier<MSG> getCreator()
        {
            return creator;
        }

        /**
         * Invoked to indicate that a packet is being split.
         *
         * @param packetIndex The index of the split packet that is being send.
         */
        public void onSplitting(int packetIndex)
        {
            //We only log when the SECOND packet, so with index 1, is processed.
            if (packetIndex != 1)
            {
                return;
            }

            //Ensure we only log once for a given packet.
            if (hasWarned.getAndSet(false))
            {
                Log.getLogger().warn("Splitting message: " + creator.get().getClass() + " it is too big to send normally. This message is only printed once");
            }
        }
    }
}
