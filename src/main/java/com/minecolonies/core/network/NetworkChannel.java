package com.minecolonies.core.network;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.crafting.CustomRecipeManagerMessage;
import com.minecolonies.core.network.messages.PermissionsMessage;
import com.minecolonies.core.network.messages.client.*;
import com.minecolonies.core.network.messages.client.colony.*;
import com.minecolonies.core.network.messages.server.*;
import com.minecolonies.core.network.messages.server.colony.*;
import com.minecolonies.core.network.messages.server.colony.building.*;
import com.minecolonies.core.network.messages.server.colony.building.builder.BuilderSelectWorkOrderMessage;
import com.minecolonies.core.network.messages.server.colony.building.enchanter.EnchanterWorkerSetMessage;
import com.minecolonies.core.network.messages.server.colony.building.fields.*;
import com.minecolonies.core.network.messages.server.colony.building.guard.GuardSetMinePosMessage;
import com.minecolonies.core.network.messages.server.colony.building.home.AssignUnassignMessage;
import com.minecolonies.core.network.messages.server.colony.building.miner.MinerRepairLevelMessage;
import com.minecolonies.core.network.messages.server.colony.building.miner.MinerSetLevelMessage;
import com.minecolonies.core.network.messages.server.colony.building.postbox.PostBoxRequestMessage;
import com.minecolonies.core.network.messages.server.colony.building.university.TryResearchMessage;
import com.minecolonies.core.network.messages.server.colony.building.warehouse.SortWarehouseMessage;
import com.minecolonies.core.network.messages.server.colony.building.warehouse.UpgradeWarehouseMessage;
import com.minecolonies.core.network.messages.server.colony.building.worker.*;
import com.minecolonies.core.network.messages.server.colony.citizen.*;
import com.minecolonies.core.network.messages.splitting.SplitPacketMessage;
import com.minecolonies.core.research.GlobalResearchTreeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

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
    private final SimpleChannel rawChannel;

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
     * Next available free index for network messages.
     * Index 0 is reserved
     */
    private int nextMessageId = 1;

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

        //  ColonyView messages
        registerMessage(ColonyViewMessage.class, ColonyViewMessage::new);
        registerMessage(ColonyViewCitizenViewMessage.class, ColonyViewCitizenViewMessage::new);
        registerMessage(ColonyViewRemoveCitizenMessage.class, ColonyViewRemoveCitizenMessage::new);
        registerMessage(ColonyViewBuildingViewMessage.class, ColonyViewBuildingViewMessage::new);
        registerMessage(ColonyViewRemoveBuildingMessage.class, ColonyViewRemoveBuildingMessage::new);
        registerMessage(ColonyViewFieldsUpdateMessage.class, ColonyViewFieldsUpdateMessage::new);
        registerMessage(PermissionsMessage.View.class, PermissionsMessage.View::new);
        registerMessage(ColonyViewWorkOrderMessage.class, ColonyViewWorkOrderMessage::new);
        registerMessage(ColonyViewRemoveWorkOrderMessage.class, ColonyViewRemoveWorkOrderMessage::new);
        registerMessage(UpdateChunkCapabilityMessage.class, UpdateChunkCapabilityMessage::new);
        registerMessage(ColonyViewResearchManagerViewMessage.class, ColonyViewResearchManagerViewMessage::new);

        //  Permission Request messages
        registerMessage(PermissionsMessage.Permission.class, PermissionsMessage.Permission::new);
        registerMessage(PermissionsMessage.AddPlayer.class, PermissionsMessage.AddPlayer::new);
        registerMessage(PermissionsMessage.RemovePlayer.class, PermissionsMessage.RemovePlayer::new);
        registerMessage(PermissionsMessage.ChangePlayerRank.class, PermissionsMessage.ChangePlayerRank::new);
        registerMessage(PermissionsMessage.AddPlayerOrFakePlayer.class, PermissionsMessage.AddPlayerOrFakePlayer::new);
        registerMessage(PermissionsMessage.AddRank.class, PermissionsMessage.AddRank::new);
        registerMessage(PermissionsMessage.RemoveRank.class, PermissionsMessage.RemoveRank::new);
        registerMessage(PermissionsMessage.EditRankType.class, PermissionsMessage.EditRankType::new);
        registerMessage(PermissionsMessage.SetSubscriber.class, PermissionsMessage.SetSubscriber::new);

        //  Colony Request messages
        registerMessage(BuildRequestMessage.class, BuildRequestMessage::new);
        registerMessage(OpenInventoryMessage.class, OpenInventoryMessage::new);
        registerMessage(TownHallRenameMessage.class, TownHallRenameMessage::new);
        registerMessage(MinerSetLevelMessage.class, MinerSetLevelMessage::new);
        registerMessage(RecallCitizenMessage.class, RecallCitizenMessage::new);
        registerMessage(HireFireMessage.class, HireFireMessage::new);
        registerMessage(WorkOrderChangeMessage.class, WorkOrderChangeMessage::new);
        registerMessage(AssignFieldMessage.class, AssignFieldMessage::new);
        registerMessage(AssignmentModeMessage.class, AssignmentModeMessage::new);
        registerMessage(GuardSetMinePosMessage.class, GuardSetMinePosMessage::new);
        registerMessage(RecallCitizenHutMessage.class, RecallCitizenHutMessage::new);
        registerMessage(TransferItemsRequestMessage.class, TransferItemsRequestMessage::new);
        registerMessage(MarkBuildingDirtyMessage.class, MarkBuildingDirtyMessage::new);
        registerMessage(ChangeFreeToInteractBlockMessage.class, ChangeFreeToInteractBlockMessage::new);
        registerMessage(CreateColonyMessage.class, CreateColonyMessage::new);
        registerMessage(ColonyDeleteOwnMessage.class, ColonyDeleteOwnMessage::new);
        registerMessage(ColonyViewRemoveMessage.class, ColonyViewRemoveMessage::new);
        registerMessage(GiveToolMessage.class, GiveToolMessage::new);
        registerMessage(ColonyAbandonOwnMessage.class, ColonyAbandonOwnMessage::new);

        registerMessage(AssignUnassignMessage.class, AssignUnassignMessage::new);
        registerMessage(OpenCraftingGUIMessage.class, OpenCraftingGUIMessage::new);
        registerMessage(AddRemoveRecipeMessage.class, AddRemoveRecipeMessage::new);
        registerMessage(ChangeRecipePriorityMessage.class, ChangeRecipePriorityMessage::new);
        registerMessage(ChangeDeliveryPriorityMessage.class, ChangeDeliveryPriorityMessage::new);
        registerMessage(ForcePickupMessage.class, ForcePickupMessage::new);
        registerMessage(UpgradeWarehouseMessage.class, UpgradeWarehouseMessage::new);
        registerMessage(TransferItemsToCitizenRequestMessage.class, TransferItemsToCitizenRequestMessage::new);
        registerMessage(UpdateRequestStateMessage.class, UpdateRequestStateMessage::new);
        registerMessage(BuildingSetStyleMessage.class, BuildingSetStyleMessage::new);
        registerMessage(RecallSingleCitizenMessage.class, RecallSingleCitizenMessage::new);
        registerMessage(AssignFilterableItemMessage.class, AssignFilterableItemMessage::new);
        registerMessage(TeamColonyColorChangeMessage.class, TeamColonyColorChangeMessage::new);
        registerMessage(ColonyFlagChangeMessage.class, ColonyFlagChangeMessage::new);
        registerMessage(ColonyStructureStyleMessage.class, ColonyStructureStyleMessage::new);
        registerMessage(PauseCitizenMessage.class, PauseCitizenMessage::new);
        registerMessage(RestartCitizenMessage.class, RestartCitizenMessage::new);
        registerMessage(SortWarehouseMessage.class, SortWarehouseMessage::new);
        registerMessage(PostBoxRequestMessage.class, PostBoxRequestMessage::new);
        registerMessage(HireMercenaryMessage.class, HireMercenaryMessage::new);
        registerMessage(HutRenameMessage.class, HutRenameMessage::new);
        registerMessage(BuildingHiringModeMessage.class, BuildingHiringModeMessage::new);
        registerMessage(DecorationBuildRequestMessage.class, DecorationBuildRequestMessage::new);
        registerMessage(DirectPlaceMessage.class, DirectPlaceMessage::new);
        registerMessage(TeleportToColonyMessage.class, TeleportToColonyMessage::new);
        registerMessage(EnchanterWorkerSetMessage.class, EnchanterWorkerSetMessage::new);
        registerMessage(InteractionResponse.class, InteractionResponse::new);
        registerMessage(TryResearchMessage.class, TryResearchMessage::new);
        registerMessage(HireSpiesMessage.class, HireSpiesMessage::new);
        registerMessage(AddMinimumStockToBuildingModuleMessage.class, AddMinimumStockToBuildingModuleMessage::new);
        registerMessage(RemoveMinimumStockFromBuildingModuleMessage.class, RemoveMinimumStockFromBuildingModuleMessage::new);
        registerMessage(FarmFieldPlotResizeMessage.class, FarmFieldPlotResizeMessage::new);
        registerMessage(FarmFieldRegistrationMessage.class, FarmFieldRegistrationMessage::new);
        registerMessage(FarmFieldUpdateSeedMessage.class, FarmFieldUpdateSeedMessage::new);
        registerMessage(AdjustSkillCitizenMessage.class, AdjustSkillCitizenMessage::new);
        registerMessage(BuilderSelectWorkOrderMessage.class, BuilderSelectWorkOrderMessage::new);
        registerMessage(TriggerSettingMessage.class, TriggerSettingMessage::new);
        registerMessage(AssignFilterableEntityMessage.class, AssignFilterableEntityMessage::new);
        registerMessage(BuildPickUpMessage.class, BuildPickUpMessage::new);
        registerMessage(SwitchBuildingWithToolMessage.class, SwitchBuildingWithToolMessage::new);
        registerMessage(ColonyTextureStyleMessage.class, ColonyTextureStyleMessage::new);
        registerMessage(MinerRepairLevelMessage.class, MinerRepairLevelMessage::new);
        registerMessage(PlantationFieldBuildRequestMessage.class, PlantationFieldBuildRequestMessage::new);
        registerMessage(ResetFilterableItemMessage.class, ResetFilterableItemMessage::new);
        registerMessage(CourierHiringModeMessage.class, CourierHiringModeMessage::new);
        registerMessage(QuarryHiringModeMessage.class, QuarryHiringModeMessage::new);
        registerMessage(ToggleRecipeMessage.class, ToggleRecipeMessage::new);
        registerMessage(ColonyNameStyleMessage.class, ColonyNameStyleMessage::new);
        registerMessage(InteractionClose.class, InteractionClose::new);
        registerMessage(GetColonyInfoMessage.class, GetColonyInfoMessage::new);
        registerMessage(PickupBlockMessage.class, PickupBlockMessage::new);
        registerMessage(MarkStoryReadOnItem.class, MarkStoryReadOnItem::new);

        //Client side only
        registerMessage(BlockParticleEffectMessage.class, BlockParticleEffectMessage::new);
        registerMessage(CompostParticleMessage.class, CompostParticleMessage::new);
        registerMessage(ItemParticleEffectMessage.class, ItemParticleEffectMessage::new);
        registerMessage(LocalizedParticleEffectMessage.class, LocalizedParticleEffectMessage::new);
        registerMessage(UpdateChunkRangeCapabilityMessage.class, UpdateChunkRangeCapabilityMessage::new);
        registerMessage(OpenSuggestionWindowMessage.class, OpenSuggestionWindowMessage::new);
        registerMessage(UpdateClientWithCompatibilityMessage.class, UpdateClientWithCompatibilityMessage::new);
        registerMessage(CircleParticleEffectMessage.class, CircleParticleEffectMessage::new);
        registerMessage(StreamParticleEffectMessage.class, StreamParticleEffectMessage::new);
        registerMessage(SleepingParticleMessage.class, SleepingParticleMessage::new);
        registerMessage(VanillaParticleMessage.class, VanillaParticleMessage::new);
        registerMessage(StopMusicMessage.class, StopMusicMessage::new);
        registerMessage(PlayAudioMessage.class, PlayAudioMessage::new);
        registerMessage(PlayMusicAtPosMessage.class, PlayMusicAtPosMessage::new);
        registerMessage(ColonyVisitorViewDataMessage.class, ColonyVisitorViewDataMessage::new);
        registerMessage(SyncPathMessage.class, SyncPathMessage::new);
        registerMessage(SyncPathReachedMessage.class, SyncPathReachedMessage::new);
        registerMessage(ReactivateBuildingMessage.class, ReactivateBuildingMessage::new);
        registerMessage(PlaySoundForCitizenMessage.class, PlaySoundForCitizenMessage::new);
        registerMessage(OpenDecoBuildWindowMessage.class, OpenDecoBuildWindowMessage::new);
        registerMessage(OpenPlantationFieldBuildWindowMessage.class, OpenPlantationFieldBuildWindowMessage::new);
        registerMessage(SaveStructureNBTMessage.class, SaveStructureNBTMessage::new);
        registerMessage(GlobalQuestSyncMessage.class, GlobalQuestSyncMessage::new);
        registerMessage(OpenColonyFoundingCovenantMessage.class, OpenColonyFoundingCovenantMessage::new);
        registerMessage(OpenBuildingUIMessage.class, OpenBuildingUIMessage::new);
        registerMessage(OpenCantFoundColonyWarningMessage.class, OpenCantFoundColonyWarningMessage::new);
        registerMessage(OpenDeleteAbandonColonyMessage.class, OpenDeleteAbandonColonyMessage::new);
        registerMessage(OpenReactivateColonyMessage.class, OpenReactivateColonyMessage::new);

        //JEI Messages
        registerMessage(TransferRecipeCraftingTeachingMessage.class, TransferRecipeCraftingTeachingMessage::new);

        //Advancement Messages
        registerMessage(OpenGuiWindowTriggerMessage.class, OpenGuiWindowTriggerMessage::new);
        registerMessage(ClickGuiButtonTriggerMessage.class, ClickGuiButtonTriggerMessage::new);

        // Colony-Independent items
        registerMessage(RemoveFromRallyingListMessage.class, RemoveFromRallyingListMessage::new);
        registerMessage(ToggleBannerRallyGuardsMessage.class, ToggleBannerRallyGuardsMessage::new);

        // Research-related messages.
        registerMessage(GlobalResearchTreeMessage.class, GlobalResearchTreeMessage::new);

        // Crafter Recipe-related messages
        registerMessage(CustomRecipeManagerMessage.class, CustomRecipeManagerMessage::new);

        registerMessage(ColonyListMessage.class, ColonyListMessage::new);

        // Resource scroll NBT share message
        registerMessage(ResourceScrollSaveWarehouseSnapshotMessage.class, ResourceScrollSaveWarehouseSnapshotMessage::new);

        // Crafting GUI
        registerMessage(SwitchRecipeCraftingTeachingMessage.class, SwitchRecipeCraftingTeachingMessage::new);
    }

    private void setupInternalMessages()
    {
        rawChannel.registerMessage(0, SplitPacketMessage.class, IMessage::toBytes, (buf) -> {
            final SplitPacketMessage msg = new SplitPacketMessage();
            msg.fromBytes(buf);
            return msg;
        }, (msg, ctxIn) -> {
            final net.minecraftforge.network.NetworkEvent.Context ctx = ctxIn.get();
            final LogicalSide packetOrigin = ctx.getDirection().getOriginationSide();
            ctx.setPacketHandled(true);
            msg.onExecute(ctx, packetOrigin.equals(LogicalSide.CLIENT));
        });
    }

    /**
     * Register a message into rawChannel.
     *
     * @param <MSG>      message class type
     * @param msgClazz   message class
     * @param msgCreator supplier with new instance of msgClazz
     */
    public <MSG extends IMessage> void registerMessage(final Class<MSG> msgClazz, final Supplier<MSG> msgCreator)
    {
        this.messagesTypes.put(nextMessageId, new NetworkingMessageEntry<>(msgCreator));
        this.messageTypeToIdMap.put(msgClazz, nextMessageId);

        ++nextMessageId;
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
    public void sendToPlayer(final IMessage msg, final ServerPlayer player)
    {
        handleSplitting(msg, s -> rawChannel.send(PacketDistributor.PLAYER.with(() -> player), s));
    }

    /**
     * Sends the message to the origin of a different message based on the networking context given.
     *
     * @param msg message to send
     * @param ctx network context
     */
    public void sendToOrigin(final IMessage msg, final NetworkEvent.Context ctx)
    {
        final ServerPlayer player = ctx.getSender();
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
        rawChannel.send(PacketDistributor.DIMENSION.with(() -> ResourceKey.create(Registries.DIMENSION, dim)), msg);
    }

    /**
     * Sends to everyone in circle made using given target point.
     *
     * @param msg message to send
     * @param pos target position and radius
     * @see PacketDistributor.TargetPoint
     */
    public void sendToPosition(final IMessage msg, final net.minecraftforge.network.PacketDistributor.TargetPoint pos)
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
    public void sendToTrackingChunk(final IMessage msg, final LevelChunk chunk)
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
        final FriendlyByteBuf innerFriendlyByteBuf = new FriendlyByteBuf(buffer);
        msg.toBytes(innerFriendlyByteBuf);
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
