package com.minecolonies.coremod.network;

import com.ldtteam.structurize.Structurize;
import com.ldtteam.structurize.api.util.constant.Constants;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.network.messages.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor.TargetPoint;
import net.minecraftforge.fml.network.simple.SimpleChannel;

/**
 * Our wrapper for Forge network layer
 */
public class NetworkChannel
{
    private static final String LATEST_PROTO_VER = "1.0";
    private static final String ACCEPTED_PROTO_VERS = LATEST_PROTO_VER;
    /**
     * Forge network channel
     */
    private final SimpleChannel rawChannel;

    /**
     * Creates a new instance of network channel.
     *
     * @param channelName unique channel name
     * @throws IllegalArgumentException if channelName already exists
     */
    public NetworkChannel(final String channelName)
    {
        rawChannel = NetworkRegistry.newSimpleChannel(new ResourceLocation(Constants.MOD_ID, channelName), () -> LATEST_PROTO_VER, ACCEPTED_PROTO_VERS::equals, ACCEPTED_PROTO_VERS::equals);
    }

    /**
     * Registers all common messages.
     */
    public void registerCommonMessages()
    {
        int idx = 0;
        registerMessage(++idx, ServerUUIDMessage.class);

        //  ColonyView messages
        registerMessage(++idx, ColonyViewMessage.class);
        registerMessage(++idx, ColonyViewCitizenViewMessage.class);
        registerMessage(++idx, ColonyViewRemoveCitizenMessage.class);
        registerMessage(++idx, ColonyViewBuildingViewMessage.class);
        registerMessage(++idx, ColonyViewRemoveBuildingMessage.class);
        registerMessage(++idx, PermissionsMessage.View.class);
        registerMessage(++idx, ColonyStylesMessage.class);
        registerMessage(++idx, ColonyViewWorkOrderMessage.class);
        registerMessage(++idx, ColonyViewRemoveWorkOrderMessage.class);
        registerMessage(++idx, UpdateChunkCapabilityMessage.class);
        registerMessage(++idx, GuardMobAttackListMessage.class);
        registerMessage(++idx, HappinessDataMessage.class);

        //  Permission Request messages
        registerMessage(++idx, PermissionsMessage.Permission.class);
        registerMessage(++idx, PermissionsMessage.AddPlayer.class);
        registerMessage(++idx, PermissionsMessage.RemovePlayer.class);
        registerMessage(++idx, PermissionsMessage.ChangePlayerRank.class);
        registerMessage(++idx, PermissionsMessage.AddPlayerOrFakePlayer.class);

        //  Colony Request messages
        registerMessage(++idx, BuildRequestMessage.class);
        registerMessage(++idx, OpenInventoryMessage.class);
        registerMessage(++idx, TownHallRenameMessage.class);
        registerMessage(++idx, MinerSetLevelMessage.class);
        registerMessage(++idx, RecallCitizenMessage.class);
        registerMessage(++idx, BuildToolPlaceMessage.class);
        registerMessage(++idx, ToggleJobMessage.class);
        registerMessage(++idx, HireFireMessage.class);
        registerMessage(++idx, WorkOrderChangeMessage.class);
        registerMessage(++idx, AssignFieldMessage.class);
        registerMessage(++idx, AssignmentModeMessage.class);
        registerMessage(++idx, GuardTaskMessage.class);
        registerMessage(++idx, GuardRecalculateMessage.class);
        registerMessage(++idx, MobEntryChangeMessage.class);
        registerMessage(++idx, GuardScepterMessage.class);
        registerMessage(++idx, RecallTownhallMessage.class);
        registerMessage(++idx, TransferItemsRequestMessage.class);
        registerMessage(++idx, MarkBuildingDirtyMessage.class);
        registerMessage(++idx, ChangeFreeToInteractBlockMessage.class);
        registerMessage(++idx, LumberjackReplantSaplingToggleMessage.class);
        registerMessage(++idx, ToggleHousingMessage.class);
        registerMessage(++idx, ToggleMoveInMessage.class);
        registerMessage(++idx, AssignUnassignMessage.class);
        registerMessage(++idx, OpenCraftingGUIMessage.class);
        registerMessage(++idx, AddRemoveRecipeMessage.class);
        registerMessage(++idx, ChangeRecipePriorityMessage.class);
        registerMessage(++idx, ChangeDeliveryPriorityMessage.class);
        registerMessage(++idx, ChangeDeliveryPriorityStateMessage.class);
        registerMessage(++idx, UpgradeWarehouseMessage.class);
        registerMessage(++idx, BuildToolPasteMessage.class);
        registerMessage(++idx, TransferItemsToCitizenRequestMessage.class);
        registerMessage(++idx, UpdateRequestStateMessage.class);
        registerMessage(++idx, BuildingSetStyleMessage.class);
        registerMessage(++idx, CowboySetMilkCowsMessage.class);
        registerMessage(++idx, BuildingMoveMessage.class);
        registerMessage(++idx, RecallSingleCitizenMessage.class);
        registerMessage(++idx, RemoveEntityMessage.class);
        registerMessage(++idx, AssignFilterableItemMessage.class);
        registerMessage(++idx, TeamColonyColorChangeMessage.class);
        registerMessage(++idx, ToggleHelpMessage.class);
        registerMessage(++idx, PauseCitizenMessage.class);
        registerMessage(++idx, RestartCitizenMessage.class);
        registerMessage(++idx, SortWarehouseMessage.class);
        registerMessage(++idx, PostBoxRequestMessage.class);
        registerMessage(++idx, ComposterRetrievalMessage.class);
        registerMessage(++idx, CrusherSetModeMessage.class);
        registerMessage(++idx, BuyCitizenMessage.class);
        registerMessage(++idx, HireMercenaryMessage.class);
        registerMessage(++idx, ShepherdSetDyeSheepsMessage.class);
        registerMessage(++idx, SifterSettingsMessage.class);
        registerMessage(++idx, HutRenameMessage.class);
        registerMessage(++idx, BuildingHiringModeMessage.class);
        registerMessage(++idx, DecorationBuildRequestMessage.class);
        registerMessage(++idx, DecorationControllUpdateMessage.class);
        registerMessage(++idx, DirectPlaceMessage.class);

        //Client side only
        registerMessage(++idx, BlockParticleEffectMessage.class);
        registerMessage(++idx, CompostParticleMessage.class);
        registerMessage(++idx, ItemParticleEffectMessage.class);
        registerMessage(++idx, LocalizedParticleEffectMessage.class);
        registerMessage(++idx, UpdateChunkRangeCapabilityMessage.class);
        registerMessage(++idx, OpenSuggestionWindowMessage.class);

        //JEI Messages
        registerMessage(++idx, TransferRecipeCrafingTeachingMessage.class);
    }

    /**
     * Register a message into rawChannel.
     *
     * @param <MSG>    message class type
     * @param id       network id
     * @param msgClazz message class
     */
    private <MSG extends IMessage> void registerMessage(final int id, final Class<MSG> msgClazz)
    {
        rawChannel.registerMessage(id, msgClazz, (msg, buf) -> msg.toBytes(buf), (buf) -> {
            try
            {
                final MSG msg = msgClazz.newInstance();
                msg.fromBytes(buf);
                return msg;
            }
            catch (final InstantiationException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
            return null;
        }, (msg, ctxIn) -> {
            final Context ctx = ctxIn.get();
            final LogicalSide packetOrigin = ctx.getDirection().getOriginationSide();
            ctx.setPacketHandled(true);
            if (msg.getExecutionSide() != null && packetOrigin.equals(msg.getExecutionSide()))
            {
                Structurize.getLogger().warn("Receving {} at wrong side!", msg.getClass().getName());
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
     *
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
     *
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
