package com.minecolonies.ai;

import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.ai.util.ChatSpamFilter;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

public class ChatSpamFilterTest
{

    @Test
    public void testFilterFirst()
    {
        final EntityCitizen mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter = new ChatSpamFilter(mockedCitizen);
        filter.requestWithoutSpam("Whatever");
        verify(mockedCitizen).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterFiltering()
    {
        final EntityCitizen mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter = new ChatSpamFilter(mockedCitizen);
        filter.requestWithoutSpam("Whatever");
        filter.requestWithoutSpam("Whatever");
        verify(mockedCitizen).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterRelease()
    {
        final EntityCitizen mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter = new ChatSpamFilter(mockedCitizen);
        filter.requestWithoutSpam("Whatever");
        filter.requestWithoutSpam("Whatever2");
        verify(mockedCitizen, times(2)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterReleaseAndFilter()
    {
        final EntityCitizen mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter = new ChatSpamFilter(mockedCitizen);
        filter.requestWithoutSpam("Whatever");
        filter.requestWithoutSpam("Whatever2");
        filter.requestWithoutSpam("Whatever2");
        filter.requestWithoutSpam("Whatever");
        verify(mockedCitizen, times(3)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterTimeout()
    {
        final EntityCitizen mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter = new ChatSpamFilter(mockedCitizen);
        for (int i = 0; i < 30 * 20 + 2; i++)
        {
            filter.requestWithoutSpam("Whatever");
        }

        verify(mockedCitizen, times(2)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterTimeoutLonger()
    {
        final EntityCitizen mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter = new ChatSpamFilter(mockedCitizen);
        for (int i = 0; i < 30 * 20 * 7 +8; i++)
        {
            filter.requestWithoutSpam("Whatever");
        }

        verify(mockedCitizen, times(4)).sendLocalizedChat(any(), any());
    }

    @Test
    public void testFilterTimeoutMax()
    {
        final EntityCitizen mockedCitizen = mock(EntityCitizen.class);
        final ChatSpamFilter filter = new ChatSpamFilter(mockedCitizen);
        for (int i = 0; i < 30 * 20 * 127 +80; i++)
        {
            filter.requestWithoutSpam("Whatever");
        }
        verify(mockedCitizen, times(8)).sendLocalizedChat(any(), any());
        for (int i = 0; i < 20 * 60 * 60 *2 +8; i++)
        {
            filter.requestWithoutSpam("Whatever");
        }
        verify(mockedCitizen, times(10)).sendLocalizedChat(any(), any());
    }

}
