package com.minecolonies.api.entity.visitor;

import com.minecolonies.api.colony.IVisitorData;
import com.minecolonies.core.entity.visitor.VisitorCitizen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * Specific handler actions for interacting with different types of visitors.
 */
public interface IVisitorType
{
    /**
     * The id for this visitor type.
     *
     * @return the resloc.
     */
    ResourceLocation getId();

    /**
     * Get the generator function for creating the entity for this given visitor.
     *
     * @return the entity creator.
     */
    Function<Level, AbstractEntityVisitor> getEntityCreator();

    /**
     * Creates the state machine for this specific visitor.
     *
     * @param visitor the current visitor.
     */
    void createStateMachine(final AbstractEntityVisitor visitor);

    /**
     * Get the list of extra data keys to support for this visitor type.
     *
     * @return a list of extra data keys.
     */
    default List<IVisitorExtraData<?>> getExtraDataKeys()
    {
        return List.of();
    }

    /**
     * Direct interaction on right click.
     *
     * @param visitor the visitor that was clicked on.
     * @param player  the player who clicked the visitor.
     * @param level   the level of the visitor.
     * @param hand    the hand which was used to interact.
     */
    @NotNull
    default InteractionResult onPlayerInteraction(final AbstractEntityVisitor visitor, final Player player, final Level level, final InteractionHand hand)
    {
        // Pause the visitor and temporarily look at the player
        visitor.getEntityStateController().setCurrentDelay(TICKS_SECOND * 3);
        visitor.getNavigation().stop();
        visitor.getLookControl().setLookAt(player);
        return InteractionResult.PASS;
    }

    /**
     * Fired upon death of a visitor.
     *
     * @param visitor the visitor that has died.
     * @param cause   the damage source that caused them to die.
     */
    default void onDied(final VisitorCitizen visitor, DamageSource cause) {}

    /**
     * Tick the data to update values.
     *
     * @param visitor the visitor the update call is called for.
     */
    default void update(final IVisitorData visitor) {}
}