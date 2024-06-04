package com.minecolonies.core.entity.citizen.citizenhandlers;

import com.minecolonies.core.MineColonies;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenMournHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.*;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_DECEASED;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_MOURNING;

/**
 * The new happiness handler for the citizen.
 */
public class CitizenMournHandler implements ICitizenMournHandler
{
    /**
     * Citizens that have recently died and were somehow related to this citizen.
     */
    private Set<String> deceasedCitizens = new HashSet<>();

    /**
     * If the citizen is currently mourning.
     */
    private boolean isMourning;

    /**
     * If mourning is enabled
     */
    private boolean isMourningEnabled;
    
    /**
     * Create a new instance of the citizen happiness handler.
     *
     * @param data the data to handle.
     */
    public CitizenMournHandler(final ICitizenData data)
    {
        isMourningEnabled = MineColonies.getConfig().getServer().mourningEnabled.get();
    }

    @Override
    public void read(final CompoundTag compound)
    {
        isMourning = compound.getBoolean(TAG_MOURNING);
        final ListTag tag = compound.getList(TAG_DECEASED, Tag.TAG_STRING);
        for (int i = 0; i < tag.size(); i++)
        {
            deceasedCitizens.add(tag.getString(i));
        }
    }

    @Override
    public void write(final CompoundTag compound)
    {
        compound.putBoolean(TAG_MOURNING, isMourning);
        final ListTag deceasedNbt = new ListTag();
        for (final String deceased : deceasedCitizens)
        {
            deceasedNbt.add(StringTag.valueOf(deceased));
        }
        compound.put(TAG_DECEASED, deceasedNbt);
    }

    @Override
    public void addDeceasedCitizen(final String name)
    {
        if(isMourningEnabled)
        {
            deceasedCitizens.add(name);
        }
    }

    @Override
    public Set<String> getDeceasedCitizens()
    {
    	return deceasedCitizens;
    }

    @Override
    public void removeDeceasedCitizen(final String name)
    {
        deceasedCitizens.remove(name);
    }

    @Override
    public void clearDeceasedCitizen()
    {
        deceasedCitizens.clear();
    }

    @Override
    public boolean shouldMourn()
    {
        return !deceasedCitizens.isEmpty();
    }

    @Override
    public boolean isMourning()
    {
        return isMourning;
    }

    @Override
    public void setMourning(final boolean mourn)
    {
        this.isMourning = mourn;
    }
}
