package com.minecolonies.api.util;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Simple class for containing reusable player messaging logic.
 */
public class MessageUtils
{
    /**
     * No operation builders for failed/empty returns
     */
    private static MessageBuilder NOOP = new MessageBuilder(Component.literal(""))
    {
        @Override
        public void sendTo(final Player... players)
        {
            // Noop
        }

        @Override
        public void sendTo(final Collection<Player> players)
        {
            // Noop
        }

        @Override
        public MessageBuilderColonyPlayerSelector sendTo(final IColony colony)
        {
            return NOOPColony;
        }

        @Override
        public MessageBuilderColonyPlayerSelector sendTo(final IColony colony, final boolean alwaysShowColony)
        {
            return NOOPColony;
        }
    };

    private static MessageBuilderColonyPlayerSelector NOOPColony = new MessageBuilderColonyPlayerSelector(null, null, false)
    {
        public void forAllPlayers()
        {
            // Noop
        }

        public void forManagers()
        {
            // Noop
        }
    };

    /**
     * Appends the citizen name to a message and returns the messagebuilder
     *
     * @param citizen
     * @param keyIn
     * @param msg
     * @return
     */
    public static MessageBuilder forCitizen(final AbstractEntityCitizen citizen, final String keyIn, final Object... msg)
    {
        return forCitizen(citizen, Component.translatable(keyIn, msg));
    }

    /**
     * Appends the citizen name to a message and returns the messagebuilder
     *
     * @param citizen
     * @return
     */
    public static MessageBuilder forCitizen(final AbstractEntityCitizen citizen, Component component)
    {
        if (citizen.getCitizenColonyHandler().getColonyOrRegister() != null)
        {
            final IJob<?> job = citizen.getCitizenJobHandler().getColonyJob();

            MessageUtils.MessageBuilder builder;
            if (job != null)
            {
                builder = MessageUtils.format(job.getJobRegistryEntry().getTranslationKey())
                  .append(Component.literal(" "))
                  .append(citizen.getCustomName())
                  .append(Component.literal(": "))
                  .append(component);
            }
            else
            {
                builder = MessageUtils.format(citizen.getCustomName())
                  .append(Component.literal(": "))
                  .append(component);
            }

            return builder;
        }

        return NOOP;
    }

    /**
     * Starts a new message builder.
     *
     * @param key  the translation key.
     * @param args the arguments for the translation component.
     * @return the message builder instance.
     */
    public static MessageBuilder format(final String key, final Object... args)
    {
        return format(Component.translatable(key, args));
    }

    /**
     * Starts a new message builder.
     *
     * @param component the component to send.
     * @return the message builder instance.
     */
    public static MessageBuilder format(final Component component)
    {
        return new MessageBuilder(component);
    }

    /**
     * Message priority types handle different types of message displays.
     */
    public enum MessagePriority
    {
        /**
         * Normal priority messages, these are not important messages sent to the colony, shown in a dimmed color (gray).
         */
        NORMAL(ChatFormatting.GRAY),
        /**
         * Important priority messages, these are important events that require player attention, shown in an outstanding color (gold).
         */
        IMPORTANT(ChatFormatting.GOLD),
        /**
         * Danger priority messages, these are shown for anything involving in serious dangerous events in the colony (ex. raids), shown in the danger color (red).
         */
        DANGER(ChatFormatting.RED);

        /**
         * The color for the message priority.
         */
        private final ChatFormatting color;

        MessagePriority(final ChatFormatting color)
        {
            this.color = color;
        }
    }

    /**
     * Starting class for the message building. Contains primary logic for sending the messages.
     */
    public static class MessageBuilder
    {
        /**
         * The current working component.
         */
        private final MutableComponent fullComponent;

        /**
         * The priority for the message.
         */
        @NotNull
        private MessagePriority priority = MessagePriority.NORMAL;

        /**
         * The click event for this message.
         */
        @Nullable
        private ClickEvent clickEvent;

        /**
         * Default constructor.
         *
         * @param component the component to begin with.
         */
        MessageBuilder(final Component component)
        {
            this.fullComponent = getFormattableComponent(component);
        }

        /**
         * Set the priority of this message, defaults to {@link MessagePriority#NORMAL}.
         *
         * @param priority the new priority.
         * @return the new message builder object.
         */
        @NotNull
        public MessageBuilder withPriority(final MessagePriority priority)
        {
            this.priority = priority;
            return this;
        }

        /**
         * Set a click event on this message, defaults to null.
         *
         * @param clickEvent the click event instance.
         * @return the new message builder object.
         */
        public MessageBuilder withClickEvent(final @NotNull ClickEvent clickEvent)
        {
            this.clickEvent = clickEvent;
            return this;
        }

        /**
         * Starts a new builder object to append an additional component to the original one.
         *
         * @param key  the translation key.
         * @param args the arguments for the translation component.
         * @return the new message builder object.
         */
        public MessageBuilder append(final String key, final Object... args)
        {
            return append(Component.translatable(key, args));
        }

        /**
         * Appends a new component to the
         *
         * @param component the component to send.
         * @return the new message builder object.
         */
        public MessageBuilder append(final Component component)
        {
            fullComponent.append(getFormattableComponent(component));
            return this;
        }

        /**
         * Creates a text component that can be used as an argument to other components.
         *
         * @return the text component.
         */
        public MutableComponent create()
        {
            final Style newStyle = Style.EMPTY
              .withColor(priority.color)
              .withClickEvent(clickEvent);

            fullComponent.withStyle(newStyle);
            fullComponent.getSiblings().stream()
              .map(this::getFormattableComponent)
              .forEach(comp -> comp.withStyle(newStyle));
            return fullComponent;
        }

        /**
         * Send the message to one (or more) players.
         *
         * @param players the players to send the message to.
         */
        public void sendTo(final Player... players)
        {
            sendTo(Arrays.asList(players));
        }

        /**
         * Send the message to a collection of players.
         *
         * @param players the players to send the message to.
         */
        public void sendTo(final Collection<Player> players)
        {
            for (Player player : players)
            {
                player.displayClientMessage(create(), false);
            }
        }

        /**
         * Send the message to a collection of players close to a location.
         *
         * @param players the players to send the message to.
         */
        public void sendToClose(final BlockPos pos, final int range, final List<Player> players)
        {
            for (Player player : players)
            {
                if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < range * range)
                {
                    player.displayClientMessage(create(), false);
                }
            }
        }

        /**
         * Send a message to a given colony, this method returns a class from which you will need
         * to select which kind of members of the colony to send the message to.
         *
         * @param colony the reference to the colony.
         * @return the message builder colony player selector.
         */
        public MessageBuilderColonyPlayerSelector sendTo(final IColony colony)
        {
            return sendTo(colony, false);
        }

        /**
         * Send a message to a given colony, this method returns a class from which you will need
         * to select which kind of members of the colony to send the message to.
         *
         * @param colony           the reference to the colony.
         * @param alwaysShowColony whether we always want to include the colony name in front of the message.
         * @return the message builder colony player selector.
         */
        public MessageBuilderColonyPlayerSelector sendTo(final IColony colony, final boolean alwaysShowColony)
        {
            return new MessageBuilderColonyPlayerSelector(create(), colony, alwaysShowColony);
        }

        /**
         * Turns any possible text component into a formattable component.
         *
         * @param component the input component.
         * @return the formattable component.
         */
        private MutableComponent getFormattableComponent(final Component component)
        {
            return component.copy();
        }
    }

    public static class MessageBuilderColonyPlayerSelector
    {
        /**
         * The stored text component to use when sending the message.
         */
        private final MutableComponent rootComponent;

        /**
         * The colony this message originated from.
         */
        private final IColony colony;

        /**
         * Determines whether we always want to include the colony name in front of the message.
         */
        private final boolean alwaysShowColony;

        /**
         * Default constructor.
         *
         * @param rootComponent    the completed component to send.
         * @param colony           the reference to the colony.
         * @param alwaysShowColony whether we always want to include the colony name in front of the message.
         */
        public MessageBuilderColonyPlayerSelector(final MutableComponent rootComponent, final IColony colony, final boolean alwaysShowColony)
        {
            this.rootComponent = rootComponent;
            this.colony = colony;
            this.alwaysShowColony = alwaysShowColony;
        }

        /**
         * Sends the message to all players inside the colony.
         */
        public void forAllPlayers()
        {
            sendInternal(colony.getMessagePlayerEntities());
        }

        /**
         * Sends the message to all colony manager.
         */
        public void forManagers()
        {
            sendInternal(colony.getImportantMessageEntityPlayers());
        }

        /**
         * Internal helper method to send the message correctly.
         *
         * @param players the collection of players to send the message to.
         */
        private void sendInternal(final Collection<Player> players)
        {
            for (Player player : players)
            {
                MutableComponent fullComponent = rootComponent.copy();
                if (alwaysShowColony || !colony.isCoordInColony(player.level(), player.blockPosition()))
                {
                    fullComponent = Component.literal("[" + colony.getName() + "] ").append(rootComponent);
                }

                player.displayClientMessage(fullComponent, false);
            }
        }
    }
}
