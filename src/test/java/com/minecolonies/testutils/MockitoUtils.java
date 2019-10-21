package com.minecolonies.testutils;

import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.expectation.PowerMockitoStubber;

import java.util.function.Consumer;

import static org.powermock.api.mockito.PowerMockito.doAnswer;

public final class MockitoUtils
{

    private MockitoUtils()
    {
        throw new IllegalStateException("Tried to initialize: MockitoUtils but this is a Utility class.");
    }

    public static final PowerMockitoStubber doVoidAnswer(Consumer<InvocationOnMock> callback)
    {
        return doAnswer(invocation -> {
            callback.accept(invocation);

            return null;
        });
    }
}
