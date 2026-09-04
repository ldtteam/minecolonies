package com.minecolonies.core.economy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * One global saved-data file for all colonies on a server. Keeping this in the
 * overworld data storage means a colony keeps the same account when players
 * travel between dimensions.
 */
public final class EconomySavedData extends SavedData
{
    public static final Codec<EconomySavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Codec.STRING, EconomyLedger.CODEC)
            .optionalFieldOf("colonies", Map.of())
            .forGetter(EconomySavedData::colonies)
    ).apply(instance, EconomySavedData::new));

    public static final SavedDataType<EconomySavedData> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("minecolonies", "economy"), EconomySavedData::new, CODEC
    );

    private final Map<String, EconomyLedger> colonies;

    public EconomySavedData()
    {
        this(Map.of());
    }

    public EconomySavedData(final Map<String, EconomyLedger> colonies)
    {
        this.colonies = new HashMap<>(colonies);
    }

    public Map<String, EconomyLedger> colonies()
    {
        return Map.copyOf(colonies);
    }

    public EconomyLedger getOrCreate(final String key)
    {
        return colonies.computeIfAbsent(key, ignored -> {
            setDirty();
            return new EconomyLedger();
        });
    }

    public EconomyLedger get(final String key)
    {
        return colonies.get(key);
    }

    public void remove(final String key)
    {
        if (colonies.remove(key) != null)
        {
            setDirty();
        }
    }
}
