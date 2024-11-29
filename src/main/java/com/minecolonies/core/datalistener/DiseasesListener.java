package com.minecolonies.core.datalistener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.core.Network;
import com.minecolonies.core.datalistener.model.Disease;
import com.minecolonies.core.network.messages.client.colony.GlobalDiseaseSyncMessage;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.random.WeightedRandomList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads and listens to diseases data.
 */
public class DiseasesListener extends SimpleJsonResourceReloadListener
{
    /**
     * Gson instance
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Json constants
     */
    private static final String KEY_NAME   = "name";
    private static final String KEY_RARITY = "rarity";
    private static final String KEY_ITEMS  = "items";

    /**
     * The map of diseases.
     */
    private static WeightedRandomList<Disease> DISEASES = WeightedRandomList.create();

    /**
     * Default constructor.
     */
    public DiseasesListener()
    {
        super(GSON, "colony/diseases");
    }

    /**
     * Sync to client.
     *
     * @param player to send it to.
     */
    public static void sendGlobalDiseasesPackets(final ServerPlayer player)
    {
        final FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        byteBuf.writeInt(DISEASES.unwrap().size());
        for (final Disease disease : DISEASES.unwrap())
        {
            byteBuf.writeResourceLocation(disease.id());
            byteBuf.writeComponent(disease.name());
            byteBuf.writeInt(disease.rarity());
            for (final ItemStorage cureItem : disease.cureItems())
            {
                StandardFactoryController.getInstance().serialize(byteBuf, cureItem);
            }
        }
        Network.getNetwork().sendToPlayer(new GlobalDiseaseSyncMessage(byteBuf), player);
    }

    /**
     * Read the data from the packet and parse it.
     *
     * @param byteBuf pck.
     */
    public static void readGlobalDiseasesPackets(final FriendlyByteBuf byteBuf)
    {
        final List<Disease> newDiseases = new ArrayList<>();
        final int size = byteBuf.readInt();
        for (int i = 0; i < size; i++)
        {
            final ResourceLocation id = byteBuf.readResourceLocation();
            final Component name = byteBuf.readComponent();
            final int rarity = byteBuf.readInt();

            final List<ItemStorage> cureItems = new ArrayList<>();
            final int itemCount = byteBuf.readInt();
            for (int j = 0; j < itemCount; j++)
            {
                cureItems.add(StandardFactoryController.getInstance().deserialize(byteBuf));
            }

            newDiseases.add(new Disease(id, name, rarity, cureItems));
        }
        DISEASES = WeightedRandomList.create(newDiseases);
    }

    /**
     * Get a collection of all possible diseases.
     *
     * @return the collection of diseases.
     */
    @NotNull
    public static List<Disease> getDiseases()
    {
        return DISEASES.unwrap();
    }

    /**
     * Get a specific disease by id.
     *
     * @param id the disease id.
     * @return the disease instance or null if it does not exist.
     */
    @Nullable
    public static Disease getDisease(final ResourceLocation id)
    {
        for (final Disease disease : DISEASES.unwrap())
        {
            if (disease.id().equals(id))
            {
                return disease;
            }
        }
        return null;
    }

    /**
     * Get a random disease from the list of diseases.
     *
     * @param random the input random source.
     * @return the random disease instance or null if no diseases exist.
     */
    @Nullable
    public static Disease getRandomDisease(final RandomSource random)
    {
        return DISEASES.getRandom(random).orElse(null);
    }

    @Override
    protected void apply(
      final @NotNull Map<ResourceLocation, JsonElement> jsonElementMap,
      final @NotNull ResourceManager resourceManager,
      final @NotNull ProfilerFiller profiler)
    {
        final List<Disease> diseases = new ArrayList<>();
        for (final Map.Entry<ResourceLocation, JsonElement> entry : jsonElementMap.entrySet())
        {
            if (!entry.getValue().isJsonObject())
            {
                return;
            }

            final JsonObject object = entry.getValue().getAsJsonObject();
            final Component name = Component.translatable(GsonHelper.getAsString(object, KEY_NAME));
            final int rarity = GsonHelper.getAsInt(object, KEY_RARITY);
            final List<ItemStorage> cureItems = new ArrayList<>();
            for (final JsonElement jsonElement : object.getAsJsonArray(KEY_ITEMS))
            {
                if (!jsonElement.isJsonObject())
                {
                    continue;
                }

                final ItemStorage cureItem = new ItemStorage(jsonElement.getAsJsonObject());
                // TODO: Apparently the healing doesn't fully work yet with multi-count cure items
                // for the sake of compatibility for now, revert the count to 1 despite what's in the JSON.
                cureItem.setAmount(1);
                cureItems.add(cureItem);
            }

            diseases.add(new Disease(entry.getKey(), name, rarity, cureItems));
        }
        DISEASES = WeightedRandomList.create(diseases);
    }
}
