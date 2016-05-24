package com.minecolonies.entity.ai;

import com.minecolonies.colony.jobs.JobDeliveryman;
import com.minecolonies.colony.materials.Material;
import com.minecolonies.colony.materials.MaterialStore;
import com.minecolonies.colony.materials.MaterialSystem;
import com.minecolonies.util.InventoryUtils;

import java.util.Map;

import static com.minecolonies.entity.ai.AIState.*;

/**
 * Performs deliveryman work
 */
public class EntityAIWorkDeliveryman extends AbstractEntityAIWork<JobDeliveryman>
{
    private MaterialStore supplyStore;
    private MaterialStore targetStore;
    private Material      targetMaterial;
    private int           targetQuantity;
    private int           targetQuantityLeft;

    public EntityAIWorkDeliveryman(JobDeliveryman deliveryman)
    {
        super(deliveryman);
        super.registerTargets(
                new AITarget(IDLE, () -> START_WORKING),
                new AITarget(START_WORKING, this::searchItems),
                new AITarget(DMAN_GET_MATERIAL, this::getMaterial),
                new AITarget(DMAN_FIND_MORE, this::findMore),
                new AITarget(DMAN_DELIVER_MATERIAL, this::deliverMaterial)
        );
    }

    private AIState searchItems()
    {
        MaterialSystem system = this.getOwnBuilding().getColony().getMaterialSystem();

        for (MaterialStore store : system.getStores())
        {
            Map<Material, Integer> need = store.getNeed();
            if(!need.isEmpty())
            {
                for(Material material : need.keySet())
                {
                    Integer count = system.getMaterials().get(material);
                    if(count != null)
                    {
                        int needCount = need.get(material);

                        Map.Entry<MaterialStore, Integer> largest = null;
                        for(Map.Entry<MaterialStore, Integer> entry : material.getLocationsStored().entrySet())
                        {
                            if(entry.getValue() >= needCount)
                            {
                                this.supplyStore = entry.getKey();
                                this.targetStore = store;
                                this.targetMaterial = material;
                                this.targetQuantityLeft = this.targetQuantity = needCount;
                                return DMAN_GET_MATERIAL;
                            }

                            if(largest == null || entry.getValue() > largest.getValue())
                            {
                                largest = entry;
                            }
                        }

                        if(largest != null)
                        {
                            this.supplyStore = largest.getKey();
                            this.targetStore = store;
                            this.targetMaterial = material;
                            this.targetQuantityLeft = this.targetQuantity = needCount;
                            return DMAN_GET_MATERIAL;
                        }
                    }
                }
            }
        }

        return this.getState();
    }

    private AIState getMaterial()
    {
        if(walkToBlock(supplyStore.getLocation()))
        {
            return this.getState();
        }

        int countLeft = InventoryUtils.transfer(supplyStore.getInventory(), this.getInventory(), targetMaterial.getItem(), targetQuantityLeft);

        if(countLeft != 0)
        {
            targetQuantityLeft = countLeft;
            return DMAN_FIND_MORE;
        }

        return DMAN_DELIVER_MATERIAL;
    }

    private AIState findMore()
    {
        MaterialSystem system = this.getOwnBuilding().getColony().getMaterialSystem();

        Integer count = system.getMaterials().get(targetMaterial);
        if(count != null)
        {
            Map.Entry<MaterialStore, Integer> largest = null;
            for(Map.Entry<MaterialStore, Integer> entry : targetMaterial.getLocationsStored().entrySet())
            {
                if(entry.getValue() >= targetQuantityLeft)
                {
                    this.supplyStore = entry.getKey();
                    return DMAN_GET_MATERIAL;
                }

                if(largest == null || entry.getValue() > largest.getValue())
                {
                    largest = entry;
                }
            }

            if(largest != null)
            {
                this.supplyStore = largest.getKey();
                return DMAN_GET_MATERIAL;
            }
        }

        //We don't have any more, deliver what we have for now
        return DMAN_DELIVER_MATERIAL;
    }

    private AIState deliverMaterial()
    {
        if(walkToBlock(targetStore.getLocation()))
        {
            return this.getState();
        }

        int countLeft = InventoryUtils.transfer(this.getInventory(), targetStore.getInventory(), targetMaterial.getItem(), targetQuantity);

        if(countLeft != 0)
        {
            //worker inventory full
            return getState();//wait until it isn't
        }

        return START_WORKING;
    }
}
