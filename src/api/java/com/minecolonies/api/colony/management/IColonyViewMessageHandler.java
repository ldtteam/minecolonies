package com.minecolonies.api.colony.management;

import com.minecolonies.api.colony.requestsystem.token.IToken;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Objects that implement this interface handle messages with regards to Colonies when they come from the
 * server.
 * <p>
 * If any of the given IDs do not match a known colony on this client, each method will return null.
 */
public interface IColonyViewMessageHandler
{
    /**
     * Sends view message to the right view.
     *
     * @param colonyId          ID of the colony.
     * @param colonyData        {@link ByteBuf} with colony data.
     * @param isNewSubscription whether this is a new subscription or not.
     * @return the response message.
     */
    @Nullable
    IMessage handleColonyViewMessage(int colonyId, @NotNull ByteBuf colonyData, boolean isNewSubscription);

    /**
     * Returns result of {@code ColonyView#handlePermissionsViewMessage(ByteBuf)}
     * if {@link IColonyManager#getColony(IToken)}. gives a not-null result
     *
     * @param colonyID ID of the colony.
     * @param data     {@link ByteBuf} with colony data.
     * @return result of {@Code ColonyView#handlePermissionsViewMessage(ByteBuf)}
     * or null.
     */
    @Nullable
    IMessage handlePermissionsViewMessage(int colonyID, @NotNull ByteBuf data);

    /**
     * Returns result of {@code ColonyView#handleColonyViewCitizensMessage(int,
     * ByteBuf)} if {@link IColonyManager#getColony(IToken)} gives a not-null result. If
     * {@link IColonyManager#getColony(IToken)} is null, returns null.
     *
     * @param colonyId  ID of the colony.
     * @param citizenId ID of the citizen.
     * @param buf       {@link ByteBuf} with colony data.
     * @return result of {@code ColonyView#handleColonyViewCitizensMessage(int,
     * ByteBuf)} or null.
     */
    @Nullable
    IMessage handleColonyViewCitizensMessage(int colonyId, int citizenId, ByteBuf buf);

    /**
     * Returns result of {@code ColonyView#handleColonyViewWorkOrderMessage(ByteBuf)}
     * (int, ByteBuf)} if {@link IColonyManager#getColony(IToken)} gives a not-null result.
     * If {@link IColonyManager#getColony(IToken)} is null, returns null.
     *
     * @param colonyId ID of the colony.
     * @param buf      {@link ByteBuf} with colony data.
     * @return result of {@code ColonyView#handleColonyViewWorkOrderMessage(ByteBuf)}
     * or null.
     */
    @Nullable
    IMessage handleColonyViewWorkOrderMessage(int colonyId, ByteBuf buf);

    /**
     * Returns result of {@code ColonyView#handleColonyViewRemoveCitizenMessage(int)}
     * if {@link IColonyManager#getColony(IToken)} gives a not-null result. If {@link
     * IColonyManager#getColony(IToken)} is null, returns null.
     *
     * @param colonyId  ID of the colony.
     * @param citizenId ID of the citizen.
     * @return result of {@code ColonyView#handleColonyViewRemoveCitizenMessage(int)}
     * or null.
     */
    @Nullable
    IMessage handleColonyViewRemoveCitizenMessage(int colonyId, int citizenId);

    /**
     * Returns result of {@code ColonyView#handleColonyBuildingViewMessage(BlockPos, IToken, ByteBuf)} if {@link IColonyManager#getColony(IToken)} gives a not-null result.
     * If {@link IColonyManager#getColony(IToken)} is null, returns null.
     *
     * @param colonyId         ID of the colony.
     * @param buildingLocation The location of the building.
     * @param buildingId       ID of the building.
     * @param buf              {@link ByteBuf} with colony data.
     * @return result of {@code ColonyView#handleColonyBuildingViewMessage(BlockPos, IToken, ByteBuf)} or null.
     */
    @Nullable
    IMessage handleColonyBuildingViewMessage(
                                              int colonyId,
                                              BlockPos buildingLocation,
                                              IToken buildingId,
                                              @NotNull ByteBuf buf);

    /**
     * Returns result of {@code ColonyView#handleColonyViewRemoveBuildingMessage(BlockPos)}
     * if {@link IColonyManager#getColony(IToken)} gives a not-null result. If {@link
     * IColonyManager#getColony(IToken)} is null, returns null.
     *
     * @param colonyId   ID of the colony.
     * @param buildingId ID of the building.
     * @return result of {@code ColonyView#handleColonyViewRemoveBuildingMessage(BlockPos)}
     * or null.
     */
    @Nullable
    IMessage handleColonyViewRemoveBuildingMessage(int colonyId, BlockPos buildingId);

    /**
     * Returns result of {@code ColonyView#handleColonyViewRemoveWorkOrderMessage(int)}
     * if {@link IColonyManager#getColony(IToken)} gives a not-null result. If {@link
     * IColonyManager#getColony(IToken)} is null, returns null.
     *
     * @param colonyId    ID of the colony.
     * @param workOrderId ID of the workOrder.
     * @return result of {@code ColonyView#handleColonyViewRemoveWorkOrderMessage(int)}
     * or null.
     */
    @Nullable
    IMessage handleColonyViewRemoveWorkOrderMessage(int colonyId, int workOrderId);
}
