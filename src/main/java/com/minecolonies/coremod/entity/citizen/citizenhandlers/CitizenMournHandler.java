package com.minecolonies.coremod.entity.citizen.citizenhandlers;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenMournHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;

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
     * Create a new instance of the citizen happiness handler.
     *
     * @param data the data to handle.
     */
    public CitizenMournHandler(final ICitizenData data)
    {

    }

    @Override
    public void read(final CompoundNBT compound)
    {
        isMourning = compound.getBoolean(TAG_MOURNING);
        final ListNBT tag = compound.getList(TAG_DECEASED, Constants.NBT.TAG_STRING);
        for (int i = 0; i < tag.size(); i++)
        {
            deceasedCitizens.add(tag.getString(i));
        }
    }

    @Override
    public void write(final CompoundNBT compound)
    {
        compound.putBoolean(TAG_MOURNING, isMourning);
        final ListNBT deceasedNbt = new ListNBT();
        for (final String deceased : deceasedCitizens)
        {
            deceasedNbt.add(StringNBT.valueOf(deceased));
        }
        compound.put(TAG_DECEASED, deceasedNbt);
    }

    @Override
    public void addDeceasedCitizen(final String name)
    {
        deceasedCitizens.add(name);
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
