package com.minecolonies.entity.ai.util;

import com.minecolonies.entity.EntityCitizen;
import org.junit.Test;

import static com.minecolonies.entity.ai.util.ChatSpamFilter.BASE_TIMEOUT;
import static com.minecolonies.entity.ai.util.ChatSpamFilter.MAX_TIMEOUT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ChatSpamFilterTest
{

    @Test
    public void testFilterFirst()
    {
        final EntityCitizen  mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter        = new ChatSpamFilter(mockedCitizen);
        filter.requestWithoutSpam("Whatever");
        verify(mockedCitizen).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterFiltering()
    {
        final EntityCitizen  mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter        = new ChatSpamFilter(mockedCitizen);
        filter.requestWithoutSpam("Whatever");
        filter.requestWithoutSpam("Whatever");
        verify(mockedCitizen).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterRelease()
    {
        final EntityCitizen  mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter        = new ChatSpamFilter(mockedCitizen);
        filter.requestWithoutSpam("Whatever");
        filter.requestWithoutSpam("Whatever2");
        verify(mockedCitizen, times(2)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterReleaseAndFilter()
    {
        final EntityCitizen  mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter        = new ChatSpamFilter(mockedCitizen);
        filter.requestWithoutSpam("Whatever");
        filter.requestWithoutSpam("Whatever2");
        filter.requestWithoutSpam("Whatever2");
        filter.requestWithoutSpam("Whatever");
        verify(mockedCitizen, times(3)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterTimeout()
    {
        final EntityCitizen  mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter        = new ChatSpamFilter(mockedCitizen);
        for (int i = 0; i < BASE_TIMEOUT + 2; i++)
        {
            filter.requestWithoutSpam("Whatever");
        }

        verify(mockedCitizen, times(2)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterTimeoutLonger()
    {
        final EntityCitizen  mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter        = new ChatSpamFilter(mockedCitizen);
        for (int i = 0; i < BASE_TIMEOUT * (1 + 2 + 4) + 4; i++)
        {
            filter.requestWithoutSpam("Whatever");
        }

        verify(mockedCitizen, times(4)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterTimeoutMax()
    {
        final EntityCitizen  mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter        = new ChatSpamFilter(mockedCitizen);
        for (int i = 0; i < BASE_TIMEOUT * (1 + 2 + 4 + 8) + 5; i++)
        {
            filter.requestWithoutSpam("Whatever");
        }
        verify(mockedCitizen, times(5)).sendLocalizedChat(any(), any());
        for (int i = 0; i < MAX_TIMEOUT * 2 + 2; i++)
        {
            filter.requestWithoutSpam("Whatever");
        }
        verify(mockedCitizen, times(7)).sendLocalizedChat(any(), any());
    }

}
