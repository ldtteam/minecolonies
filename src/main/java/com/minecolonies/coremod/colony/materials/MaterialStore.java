package com.minecolonies.coremod.colony.materials;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A material store is a node in the material network, keeping track of materials in a specific inventory.
 * Created: December 14, 2015
 *
 * @author Colton
 */
public class MaterialStore
{
    private static final String                 TAG_MATERIAL_STORE = "MaterialStore";
    private static final String                 TAG_DONT_NEED      = "HaveDontNeed";
    private static final String                 TAG_HAVE_NEED      = "HaveNeed";
    private static final String                 TAG_NEED           = "NeedDontHave";
    private static final String                 TAG_ID             = "ID";
    private static final String                 TAG_QUANTITY       = "quantity";
    /**
     * These are Materials we have that we don't need right now. So they could be used for something else.
     */
    @NotNull
    private final        Map<Material, Integer> dontNeed           = new HashMap<>();
    /**
     * These are Materials we have that we currently need. So we don't tell anyone else about them.
     */
    @NotNull
    private final        Map<Material, Integer> haveNeed           = new HashMap<>();
    /**
     * These are Materials that we don't have, but we need. The deliveryman try to keep this list empty.
     */
    @NotNull
    private final        Map<Material, Integer> need               = new HashMap<>();
    private final Type           type;
    private final MaterialSystem system;

    /**
     * Constructor for MaterialStore.
     *
     * @param type   What kind of inventory, Entity(INVENTORY) or AbstractBuilding(CHEST).
     * @param system The MaterialSystem associated with the colony.
     */
    public MaterialStore(final Type type, @NotNull final MaterialSystem system)
    {
        this.type = type;
        this.system = system;

        system.addStore(this);
    }

    /**
     * @return What kind of inventory is this? Entity Inventory, or Chest Inventory.
     */
    public Type getType()
    {
        return type;
    }

    /**
     * These are the Materials that we need, this function will provide a list and quantity for someone like the deliveryman..
     *
     * @return An unmodifiable version of the need map.
     */
    @NotNull
    public Map<Material, Integer> getNeed()
    {
        return Collections.unmodifiableMap(need);
    }

    /**
     * These are the Materials that we have, but we don't need. So someone else could come and take them if they wanted.
     *
     * @return An unmodifiable version of the dontNeed map.
     */
    @NotNull
    public Map<Material, Integer> getHave()
    {
        return Collections.unmodifiableMap(dontNeed);
    }

    /**
     * This returns how many of a Material that we have and don't need.
     *
     * @param material Material that we are checking.
     * @return How many of material that we have.
     */
    public int getMaterialCount(final Material material)
    {
        final Integer count = dontNeed.get(material);

        return count == null ? 0 : count;
    }

    /**
     * Add a Material to this inventory. First check if we need that item and act accordingly. Then put any extra
     * in the dontNeed map.
     *
     * @param item     Item that is being added to the MaterialStore.
     * @param quantity How much of item is being added.
     */
    public void addMaterial(final Item item, final int quantity)
    {
        addMaterial(system.getMaterial(item), quantity);
    }

    private void addMaterial(@Nullable final Material material, final int quantity)
    {
        if (quantity <= 0 || material == null)
        {
            return;
        }

        final Integer count = dontNeed.get(material);
        if (count == null)
        {
            final Integer needCount = need.get(material);
            if (needCount == null)
            {
                dontNeed.put(material, quantity);

                system.addMaterial(material, quantity);
                material.add(this, quantity);
            }
            else
            {
                if (quantity < needCount)
                {
                    haveNeed.put(material, quantity);
                    need.put(material, needCount - quantity);
                }
                else if (quantity == needCount)
                {
                    haveNeed.put(material, quantity);
                    need.remove(material);
                }
                else
                {
                    haveNeed.put(material, needCount);
                    need.remove(material);
                    dontNeed.put(material, quantity - needCount);

                    system.addMaterial(material, quantity - needCount);
                    material.add(this, quantity - needCount);
                }
            }
        }
        else
        {
            dontNeed.put(material, count + quantity);

            system.addMaterial(material, quantity);
            material.add(this, quantity);
        }
    }

    /**
     * Add a Material to this inventory. First check if we need that item and act accordingly. Then put any extra
     * in the dontNeed map.
     *
     * @param block    Block that is being added to the MaterialStore.
     * @param quantity How much of block is being added.
     */
    public void addMaterial(final Block block, final int quantity)
    {
        addMaterial(system.getMaterial(block), quantity);
    }

    /**
     * Remove a material from this inventory. First remove from dontNeed, then remove from haveNeed. If material was removed
     * from haveNeed, then request that quantity in need.
     *
     * @param item     Item that is being removed from the MaterialStore.
     * @param quantity How much of item is being removed.
     */
    public void removeMaterial(final Item item, final int quantity)
    {
        removeMaterial(system.getMaterial(item), quantity);
    }

    private void removeMaterial(@Nullable final Material material, final int quantity)
    {
        if (quantity <= 0 || material == null)
        {
            return;
        }

        final Integer count = dontNeed.get(material);
        if (count == null || count < quantity)
        {
            final Integer countNeed = haveNeed.get(material);
            if (count == null)
            {
                if (countNeed == null || countNeed < quantity)
                {
                    throw new QuantityNotFound("MaterialStore (haveNeed)", material.getID(), countNeed == null ? 0 : countNeed, quantity);
                }
                else
                {
                    if (countNeed == quantity)
                    {
                        haveNeed.remove(material);
                    }
                    else
                    {
                        haveNeed.put(material, countNeed - quantity);
                    }
                }
            }
            else
            {
                final int countToRemove = quantity - count;
                if (countNeed == null || countNeed < countToRemove)
                {
                    throw new QuantityNotFound("MaterialStore (dontNeed+haveNeed)", material.getID(), countNeed == null ? count : (countNeed + count), quantity);
                }
                else
                {
                    removeMaterial(material);

                    if (countNeed == countToRemove)
                    {
                        haveNeed.remove(material);
                    }
                    else
                    {
                        haveNeed.put(material, countNeed - countToRemove);
                    }
                }
            }
        }
        else if (count == quantity)
        {
            removeMaterial(material);
        }
        else
        {
            dontNeed.put(material, count - quantity);

            system.removeMaterial(material, quantity);
            material.remove(this, quantity);
        }
    }

    private void removeMaterial(@NotNull final Material material)
    {
        final int count = dontNeed.get(material);

        dontNeed.remove(material);

        removeMaterialFromExternal(material, count);
    }

    private void removeMaterialFromExternal(@NotNull final Material material, final int count)
    {
        system.removeMaterial(material, count);
        material.remove(this);
    }

    /**
     * Remove a material from this inventory. First remove from dontNeed, then remove from haveNeed. If material was removed
     * from haveNeed, then request that quantity in need.
     *
     * @param block    Block that is being removed from the MaterialStore.
     * @param quantity How much of block is being removed.
     */
    public void removeMaterial(final Block block, final int quantity)
    {
        removeMaterial(system.getMaterial(block), quantity);
    }

    /**
     * Call this method when you need something.
     *
     * @param item     Item that you need.
     * @param quantity How much you need.
     */
    public void addNeededMaterial(final Item item, final int quantity)
    {
        addNeededMaterial(system.getMaterial(item), quantity);
    }

    /**
     * Call this method when you need something.
     *
     * @param block    Block that you need.
     * @param quantity How much you need.
     */
    public void addNeededMaterial(final Block block, final int quantity)
    {
        addNeededMaterial(system.getMaterial(block), quantity);
    }

    /**
     * Call this method when you don't need something anymore.
     *
     * @param item     Item that you need.
     * @param quantity How much you need.
     */
    public void removeNeededMaterial(final Item item, final int quantity)
    {
        removeNeededMaterial(system.getMaterial(item), quantity);
    }

    private void removeNeededMaterial(@Nullable final Material material, final int quantity)
    {
        if (quantity <= 0 || material == null)
        {
            return;
        }

        final Integer count = haveNeed.get(material);
        if (count != null)
        {
            if (count > quantity)
            {
                haveNeed.put(material, count - quantity);
                addMaterial(material, count - quantity);
            }
            else
            {
                if (count < quantity)
                {
                    removeFromNeededMap(material, quantity - count);
                }

                haveNeed.remove(material);
                addMaterial(material, count);
            }
        }
        else
        {
            removeFromNeededMap(material, quantity);
        }
    }

    private void removeFromNeededMap(@NotNull final Material material, final int quantity)
    {
        final Integer count = need.get(material);
        if (count == null || count < quantity)
        {
            throw new QuantityNotFound("MaterialStore (need)", material.getID(), count == null ? 0 : count, quantity);
        }
        else if (count == quantity)
        {
            need.remove(material);
        }
        else
        {
            need.put(material, count - quantity);
        }
    }

    /**
     * Call this method when you don't need something anymore.
     *
     * @param block    Block that you need.
     * @param quantity How much you need.
     */
    public void removeNeededMaterial(final Block block, final int quantity)
    {
        removeNeededMaterial(system.getMaterial(block), quantity);
    }

    private void addNeededMaterial(@Nullable final Material material, final int quantity)
    {
        if (quantity <= 0 || material == null)
        {
            return;
        }

        final Integer count = dontNeed.get(material);
        if (count != null)
        {
            if (count >= quantity)
            {
                haveNeed.put(material, quantity);
                removeMaterial(material, quantity);
            }
            else
            {
                haveNeed.put(material, count);
                removeMaterial(material);
                need.put(material, quantity - count);
            }
        }
        else
        {
            need.put(material, quantity);
        }
    }

    /**
     * Removes all Materials from the system before the MaterialStore is destroyed.
     */
    public void destroy()
    {
        this.clear();

        system.removeStore(this);
    }

    /**
     * Removes all the Materials from the store.
     */
    public void clear()
    {
        for (@NotNull final Map.Entry<Material, Integer> entry : dontNeed.entrySet())
        {
            removeMaterialFromExternal(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Read a state from nbt.
     *
     * @param nbtTagCompound the compound used.
     */
    public void readFromNBT(@NotNull final NBTTagCompound nbtTagCompound)
    {
        final NBTTagCompound compound = nbtTagCompound.getCompoundTag(TAG_MATERIAL_STORE);

        final NBTTagList list = compound.getTagList(TAG_DONT_NEED, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++)
        {
            final NBTTagCompound tag = list.getCompoundTagAt(i);

            @NotNull final Material material = new Material(tag.getInteger(TAG_ID));

            addMaterial(material, tag.getInteger(TAG_QUANTITY));
        }


        final NBTTagList listHaveNeed = compound.getTagList(TAG_HAVE_NEED, Constants.NBT.TAG_COMPOUND);
        readMapFromNBT(listHaveNeed, haveNeed);

        final NBTTagList listNeed = compound.getTagList(TAG_NEED, Constants.NBT.TAG_COMPOUND);
        readMapFromNBT(listNeed, need);
    }

    private static void readMapFromNBT(@NotNull final NBTTagList list, @NotNull final Map<Material, Integer> map)
    {
        for (int i = 0; i < list.tagCount(); i++)
        {
            final NBTTagCompound tag = list.getCompoundTagAt(i);

            final Integer id = tag.getInteger(TAG_ID);

            map.put(new Material(id), tag.getInteger(TAG_QUANTITY));
        }
    }

    /**
     * Store data to NBT.
     *
     * @param nbtTagCompound compound used to store.
     */
    public void writeToNBT(@NotNull final NBTTagCompound nbtTagCompound)
    {
        @NotNull final NBTTagCompound compound = new NBTTagCompound();

        @NotNull final NBTTagList dontNeedList = new NBTTagList();
        writeMapToNBT(dontNeedList, dontNeed);
        compound.setTag(TAG_DONT_NEED, dontNeedList);

        @NotNull final NBTTagList haveNeedList = new NBTTagList();
        writeMapToNBT(haveNeedList, haveNeed);
        compound.setTag(TAG_HAVE_NEED, haveNeedList);

        @NotNull final NBTTagList needList = new NBTTagList();
        writeMapToNBT(needList, need);
        compound.setTag(TAG_NEED, needList);

        nbtTagCompound.setTag(TAG_MATERIAL_STORE, compound);
    }

    private static void writeMapToNBT(@NotNull final NBTTagList compound, @NotNull final Map<Material, Integer> map)
    {
        for (@NotNull final Map.Entry<Material, Integer> entry : map.entrySet())
        {
            @NotNull final NBTTagCompound tag = new NBTTagCompound();

            //hashCode is item ID
            tag.setInteger(TAG_ID, entry.getKey().hashCode());
            tag.setInteger(TAG_QUANTITY, entry.getValue());

            compound.appendTag(tag);
        }
    }

    /**
     * INVENTORY is Entity.
     * CHEST is AbstractBuilding.
     */
    public enum Type
    {
        INVENTORY,
        CHEST
    }
}
