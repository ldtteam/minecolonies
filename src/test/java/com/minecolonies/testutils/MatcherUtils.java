package com.minecolonies.testutils;

import com.google.common.collect.Multimap;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.Matchers;

import java.util.Map;

public final class MatcherUtils
{

    private MatcherUtils()
    {
        throw new IllegalStateException("Tried to initialize: MatcherUtils but this is a Utility class.");
    }

    public static final <T> Matcher<T> mapContainsKey(Map<?, ?> map)
    {
        return Matchers.argThat(new BaseMatcher<Matcher<T>>() {
            @Override
            public boolean matches(final Object item)
            {
                return map.containsKey(item);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Matcher that checks if the given map: ").appendValue(map).appendText(" contains the parameter as key.");
            }
        });
    }

    public static final <T> Matcher<T> mapDoesNotContainsKey(Map<?, ?> map)
    {
        return Matchers.argThat(new BaseMatcher<Matcher<T>>() {
            @Override
            public boolean matches(final Object item)
            {
                return !map.containsKey(item);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Matcher that checks if the map: ").appendValue(map).appendText(" does not contain the parameter as key.");
            }
        });
    }

    public static final <T> Matcher<T> mapContainsValue(Map<?, ?> map)
    {
        return Matchers.argThat(new BaseMatcher<Matcher<T>>() {
            @Override
            public boolean matches(final Object item)
            {
                return map.containsValue(item);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Matcher that checks if the map: ").appendValue(map).appendText(" contains the parameter as value");
            }
        });
    }

    public static final <T> Matcher<T> mapDoesNotContainsValue(Map<?, ?> map)
    {
        return Matchers.argThat(new BaseMatcher<Matcher<T>>() {
            @Override
            public boolean matches(final Object item)
            {
                return !map.containsValue(item);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Matcher that checks if the map: ").appendValue(map).appendText(" does not contain the parameter as value");
            }
        });
    }

    public static final <T> Matcher<T> multiMapContainsKey(Multimap<?, ?> multiMap)
    {
        return Matchers.argThat(new BaseMatcher<Matcher<T>>() {
            @Override
            public boolean matches(final Object item)
            {
                return multiMap.containsKey(item);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Matcher that checks if the given multiMap: ").appendValue(multiMap).appendText(" contains the parameter as key.");
            }
        });
    }

    public static final <T> Matcher<T> multiMapDoesNotContainsKey(Multimap<?, ?> multiMap)
    {
        return Matchers.argThat(new BaseMatcher<Matcher<T>>() {
            @Override
            public boolean matches(final Object item)
            {
                return !multiMap.containsKey(item);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Matcher that checks if the multiMap: ").appendValue(multiMap).appendText(" does not contain the parameter as key.");
            }
        });
    }

    public static final <T> Matcher<T> multiMapContainsValue(Multimap<?, ?> multiMap)
    {
        return Matchers.argThat(new BaseMatcher<Matcher<T>>() {
            @Override
            public boolean matches(final Object item)
            {
                return multiMap.containsValue(item);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Matcher that checks if the multiMap: ").appendValue(multiMap).appendText(" contains the parameter as value");
            }
        });
    }

    public static final <T> Matcher<T> multiMapDoesNotContainsValue(Multimap<?, ?> multiMap)
    {
        return Matchers.argThat(new BaseMatcher<Matcher<T>>() {
            @Override
            public boolean matches(final Object item)
            {
                return !multiMap.containsValue(item);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Matcher that checks if the multiMap: ").appendValue(multiMap).appendText(" does not contain the parameter as value");
            }
        });
    }
}
