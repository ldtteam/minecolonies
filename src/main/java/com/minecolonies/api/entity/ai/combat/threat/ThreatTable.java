package com.minecolonies.api.entity.ai.combat.threat;

import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * Threat table class, basically a list of entities with an associated threat value
 */
public class ThreatTable<T extends LivingEntity & IThreatTableEntity>
{
    /**
     * Melee range sq
     */
    private static final int MELEE_RANGE = 8;

    /**
     * Max tracking time
     */
    private static final int MAX_TRACKING_TICKS = TICKS_SECOND * 120;

    /**
     * Max distance bonus threat
     */
    private static final int MAX_DIST_THREAT = 7;

    /**
     * Max health bonus threat
     */
    private static final int MAX_HEALTH_THREAT = 2;

    /**
     * List holding the threat entries
     */
    private List<ThreatTableEntry> threatList = new ArrayList<>();

    /**
     * Currently targeted entity
     */
    private int currentTargetIndex = 0;

    /**
     * The owner entity of this table
     */
    private final T owner;

    public ThreatTable(final T owner)
    {
        this.owner = owner;
    }

    /**
     * Adds X threat to the given entity
     *
     * @param attacker         entity to add the value to
     * @param additionalThreat threat value to add
     */
    public void addThreat(final LivingEntity attacker, final int additionalThreat)
    {
        ThreatTableEntry threatTableEntry = null;
        int index = threatList.size();

        for (int i = 0; i < index; i++)
        {
            final ThreatTableEntry entry = threatList.get(i);
            if (entry.getEntity() == attacker)
            {
                threatTableEntry = entry;
                index = i;
                break;
            }
        }

        if (threatTableEntry == null)
        {
            threatTableEntry = new ThreatTableEntry(attacker);
            threatList.add(threatTableEntry);
            threatTableEntry.addThreat(Math.max(0, MAX_DIST_THREAT - (owner.blockPosition().distManhattan(attacker.blockPosition()) / 4)));
            threatTableEntry.addThreat((int) Math.max(0, MAX_HEALTH_THREAT - (3 * (attacker.getHealth() / attacker.getMaxHealth()))));
        }

        threatTableEntry.addThreat(additionalThreat);
        adaptTableToThreat(index);
    }

    /**
     * Get the threat value for the given entity
     *
     * @param attacker entity
     * @return threat value
     */
    public int getThreatFor(final LivingEntity attacker)
    {
        ThreatTableEntry threatTableEntry = null;
        int index = threatList.size();

        for (int i = 0; i < index; i++)
        {
            final ThreatTableEntry entry = threatList.get(i);
            if (entry.getEntity() == attacker)
            {
                return entry.getThreat();
            }
        }

        return 0;
    }

    /**
     * Resorts the given index after its threat value changed
     *
     * @param index
     */
    private void adaptTableToThreat(final int index)
    {
        for (int i = index; i > 0; i--)
        {
            final ThreatTableEntry current = threatList.get(i);
            final ThreatTableEntry above = threatList.get(i - 1);

            if (current.getThreat() > above.getThreat())
            {
                if (currentTargetIndex == (i - 1))
                {
                    currentTargetIndex = i;
                }

                threatList.set(i, above);
                threatList.set(i - 1, current);
            }
            else
            {
                break;
            }
        }
    }

    /**
     * Get the current target
     *
     * @return target or null
     */
    public ThreatTableEntry getTarget()
    {
        if (threatList.isEmpty())
        {
            return null;
        }

        // Threat value target change thresholds
        ThreatTableEntry current = threatList.get(currentTargetIndex);
        final ThreatTableEntry top = threatList.get(0);
        if (top.getThreat() > current.getThreat())
        {
            if (top.getEntity().distanceToSqr(owner) > MELEE_RANGE)
            {
                // Targets not in meleerange need 30% more threat than current to cause a target change
                if (top.getThreat() > (current.getThreat() * 1.3))
                {
                    currentTargetIndex = 0;
                    current = top;
                }
            }
            else
            {
                // Targets in meleerange need 10% more threat than current to cause a target change
                if (top.getThreat() > (current.getThreat() * 1.1))
                {
                    currentTargetIndex = 0;
                    current = top;
                }
            }
        }

        if (Math.abs(owner.level().getGameTime() - current.getLastSeen()) > MAX_TRACKING_TICKS || !current.getEntity().isAlive())
        {
            removeCurrentTarget();
            return getTarget();
        }

        if (current.getThreat() < 0)
        {
            return null;
        }

        if (current instanceof IThreatTableEntity threatTableEntity && threatTableEntity.getThreatTable().threatList.isEmpty())
        {
            threatTableEntity.getThreatTable().addThreat(owner, 0);
        }

        return current;
    }

    /**
     * Only gets the currently targeted mob
     *
     * @return
     */
    public LivingEntity getTargetMob()
    {
        final ThreatTableEntry entry = getTarget();
        if (entry == null)
        {
            return null;
        }
        return entry.getEntity();
    }

    /**
     * Reset current target threat to 0
     */
    public void resetCurrentTargetThreat()
    {
        final ThreatTableEntry entry = threatList.get(currentTargetIndex);
        if (entry.getThreat() > 0)
        {
            entry.setThreat(0);
            threatList.remove(currentTargetIndex);
            currentTargetIndex = 0;
            threatList.add(entry);
        }
    }

    /**
     * Sets the current target to be invalid until it does a threatening action again
     */
    public void markInvalidTarget()
    {
        if (threatList.isEmpty())
        {
            return;
        }

        final ThreatTableEntry entry = threatList.get(currentTargetIndex);
        if (!entry.getEntity().isAlive())
        {
            removeCurrentTarget();
            return;
        }

        if (entry.getThreat() != -1)
        {
            entry.setThreat(-1);
            threatList.remove(currentTargetIndex);
            currentTargetIndex = 0;
            threatList.add(entry);
        }
    }

    /**
     * Reset current target threat to 0
     */
    public void removeCurrentTarget()
    {
        if (threatList.isEmpty())
        {
            return;
        }

        threatList.remove(currentTargetIndex);
        currentTargetIndex = 0;
    }

    /**
     * Reset all entries in the table
     */
    public void resetTable()
    {
        threatList = new ArrayList<>();
        currentTargetIndex = 0;
    }
}
