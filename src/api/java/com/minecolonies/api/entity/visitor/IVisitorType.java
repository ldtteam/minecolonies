package com.minecolonies.api.entity.visitor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
     * Get the entity type for this visitor type.
     *
     * @return the entity type.
     */
    EntityType<? extends AbstractEntityVisitor> getEntityType();

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
        // Nothing should happen by default.
        return InteractionResult.PASS;
    }
}