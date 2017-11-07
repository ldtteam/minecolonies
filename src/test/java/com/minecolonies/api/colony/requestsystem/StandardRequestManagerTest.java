package com.minecolonies.api.colony.requestsystem;

public class StandardRequestManagerTest
{

   /* private StandardRequestManager requestManager;
    private IRequestResolverProvider provider;

    @Before
    public void setUp() throws Exception
    {
        Configurations.requestSystem.enableDebugLogging = true;
        requestManager = new StandardRequestManager(null);

        StandardFactoryControllerInitializer.onPreInit();

        StandardFactoryController.getInstance().registerNewFactory(new StringRequestFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StringResolverFactory());
        StandardFactoryController.getInstance().registerNewFactory(new TestRequesterFactory());

        StandardRequestManager.registerRequestableTypeMapping(String.class, StringRequest.class);

        provider = new TestResolvingProvider();
    }

    @After
    public void tearDown() throws Exception
    {
        requestManager = null;
        StandardFactoryController.reset();
    }

    @Test
    public void serializeNBT() throws Exception
    {
        requestManager.onProviderAddedToColony(provider);

        requestManager.createRequest(TestRequester.INSTANCE, "Hello");
        requestManager.createRequest(TestRequester.INSTANCE, "Test 2");

        NBTTagCompound compound = requestManager.serializeNBT();

        assertNotNull(compound);
    }

    @Test
    public void deserializeNBT() throws Exception
    {
        requestManager.onProviderAddedToColony(provider);

        requestManager.createRequest(TestRequester.INSTANCE, "Hello");
        requestManager.createAndAssignRequest(TestRequester.INSTANCE, "Test 2");

        NBTTagCompound compound = requestManager.serializeNBT();

        StandardRequestManager deserializedVariant = new StandardRequestManager(null);
        deserializedVariant.onProviderAddedToColony(provider);
        deserializedVariant.deserializeNBT(compound);
    }

    @Test
    public void getFactoryController() throws Exception
    {
        assertEquals(StandardFactoryController.getInstance(), requestManager.getFactoryController());
    }

    @Test
    public void createAndAssignRequest() throws Exception
    {
        requestManager.onProviderAddedToColony(provider);

        IToken token = requestManager.createAndAssignRequest(TestRequester.INSTANCE, "Hello");
        assertNotNull(token);

        IRequest<? extends String> request = requestManager.getRequestForToken(token);
        assertNotNull(request);
        assertEquals("Hello", request.getRequest());

        requestManager.onProviderRemovedFromColony(provider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateRequestState() throws Exception
    {
        requestManager.onProviderAddedToColony(provider);

        IToken token = requestManager.createAndAssignRequest(TestRequester.INSTANCE, "Hello");

        RequestState originalState = requestManager.getRequestForToken(token).getState();
        assertEquals(RequestState.COMPLETED, originalState);

        requestManager.updateRequestState(token, RequestState.RECEIVED);
        requestManager.getRequestForToken(token);
    }

    @Test
    public void onProviderModificationTest() throws Exception
    {
        requestManager.onProviderAddedToColony(provider);
        requestManager.onProviderRemovedFromColony(provider);
    }

    private static class TestResolvingProvider implements IRequestResolverProvider
    {

        private final IToken token;
        private final ImmutableCollection<IRequestResolver> resolvers;

        private TestResolvingProvider() {token = StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN);
            resolvers = ImmutableList.of(new StringResolver());
        }

        @Override
        public IToken getToken()
        {
            return token;
        }

        @Override
        public ImmutableCollection<IRequestResolver> getResolvers()
        {
            return resolvers;
        }
    }

    private static class StringRequest extends AbstractRequest<String>
    {

        StringRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final String requested)
        {
            super(requester, token, requested);
        }

        StringRequest(@NotNull final IRequester requester, @NotNull final IToken token, @NotNull final RequestState state, @NotNull final String requested)
        {
            super(requester, token, state, requested);
        }

        @NotNull
        @Override
        public ITextComponent getDisplayString()
        {
            return null;
        }

        @Override
        public List<ItemStack> getDisplayStacks()
        {
            return null;
        }
    }

    private static class StringRequestFactory implements IRequestFactory<String, StringRequest>
    {
        *//**
         * Method to get a new instance of a request given the input and token.
         *
         * @param input        The input to build a new request for.
         * @param location     The location of the requester.
         * @param token        The token to build the request from.
         * @param initialState The initial state of the request request.
         * @return The new output instance for a given input.
         *//*
        @Override
        public StringRequest getNewInstance(
                                                                 @NotNull final String input,
                                                                 @NotNull final IRequester location,
                                                                 @NotNull final IToken token,
                                                                 @NotNull final RequestState initialState)
        {
            return new StringRequest(location, token, initialState, input);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<StringRequest> getFactoryOutputType()
        {
            return TypeToken.of(StringRequest.class);
        }

        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public TypeToken<String> getFactoryInputType()
        {
            return TypeToken.of(String.class);
        }

        *//**
         * Method to serialize a given Request.
         *
         * @param controller The controller that can be used to serialize complicated types.
         * @param request    The request to serialize.
         * @return The serialized data of the given requets.
         *//*
        @NotNull
        @Override
        public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final StringRequest request)
        {
            return StandardRequestFactories.serializeToNBT(controller, request, (controller1, object) -> {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setString("String", object);
                return compound;
            });
        }

        *//**
         * Method to deserialize a given Request.
         *
         * @param controller The controller that can be used to deserialize complicated types.
         * @param nbt        The data of the request that should be deserialized.
         * @return The request that corresponds with the given data in the nbt
         *//*
        @NotNull
        @Override
        @SuppressWarnings(Suppression.LEFT_CURLY_BRACE)
        public StringRequest deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
        {
            return StandardRequestFactories.deserializeFromNBT(controller, nbt, ((controller1, compound) -> compound.getString("String")),
              (requested, token, requester, requestState) -> controller.getNewInstance(TypeToken.of(StringRequest.class), requested, token, requester, requestState));
        }
    }

    private static class StringResolver implements IRequestResolver<String>
    {

        @Override
        public TypeToken<? extends String> getRequestType()
        {
            return TypeToken.of(String.class);
        }

        @Override
        public boolean canResolve(@NotNull final IRequestManager manager, final IRequest<? extends String> requestToCheck)
        {
            return true;
        }

        @Nullable
        @Override
        public List<IToken> attemptResolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends String> request)
        {
            if (request.getRequest().length() == 1)
                return Lists.newArrayList();
            else
                return Lists.newArrayList(manager.createRequest(TestRequester.INSTANCE, request.getRequest().substring(1)));
        }

        @Nullable
        @Override
        public void resolve(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends String> request) throws RuntimeException
        {
            System.out.println(request.getRequest());
            manager.updateRequestState(request.getToken(), RequestState.COMPLETED);
        }

        @Nullable
        @Override
        public IRequest getFollowupRequestForCompletion(
                                                         @NotNull final IRequestManager manager, @NotNull final IRequest<? extends String> completedRequest)
        {
            return null;
        }

        @Nullable
        @Override
        public IRequest onParentCancelled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends String> request) throws IllegalArgumentException
        {
            return null;
        }

        @Nullable
        @Override
        public void onResolvingOverruled(@NotNull final IRequestManager manager, @NotNull final IRequest<? extends String> request) throws IllegalArgumentException
        {

        }

        @Override
        public int getPriority()
        {
            return 0;
        }

        @Override
        public IToken getRequesterId()
        {
            return TestRequester.INSTANCE.token;
        }

        @NotNull
        @Override
        public ILocation getRequesterLocation()
        {
            return TestRequester.INSTANCE.getRequesterLocation();
        }

        @NotNull
        @Override
        public void onRequestComplete(@NotNull final IToken token)
        {
            //NOOP
        }
    }

    private static class StringResolverFactory implements IRequestResolverFactory<StringResolver>
    {

        @NotNull
        @Override
        public TypeToken<? extends StringResolver> getFactoryOutputType()
        {
            return TypeToken.of(StringResolver.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends ILocation> getFactoryInputType()
        {
            return TypeConstants.ILOCATION;
        }

        @NotNull
        @Override
        public StringResolver getNewInstance(@NotNull final IFactoryController factoryController, @NotNull final ILocation iLocation, @NotNull final Object... context)
          throws IllegalArgumentException
        {
            return new StringResolver();
        }

        @NotNull
        @Override
        public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final StringResolver stackResolver)
        {
            return new NBTTagCompound();
        }

        @NotNull
        @Override
        public StringResolver deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
        {
            return new StringResolver();
        }
    }

    private static class TestRequester implements IRequester {

        static final TestRequester INSTANCE= new TestRequester();

        private final IToken token;

        private TestRequester() {
            this(StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN));
        }

        private TestRequester(final IToken token) {this.token = token;}

        @Override
        public IToken getRequesterId()
        {
            return token;
        }

        @NotNull
        @Override
        public ILocation getRequesterLocation()
        {
            return null;
        }

        @NotNull
        @Override
        public void onRequestComplete(@NotNull final IToken token)
        {
            return;
        }
    }

    private static class TestRequesterFactory implements IRequesterFactory<FactoryVoidInput, TestRequester>
    {

        @NotNull
        @Override
        public TypeToken<? extends TestRequester> getFactoryOutputType()
        {
            return TypeToken.of(TestRequester.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public TestRequester getNewInstance(
                                             @NotNull final IFactoryController factoryController,
                                             @NotNull final FactoryVoidInput factoryVoidInput,
                                             @NotNull final Object... context) throws IllegalArgumentException
        {
            return new TestRequester();
        }

        @NotNull
        @Override
        public NBTTagCompound serialize(@NotNull final IFactoryController controller, @NotNull final TestRequester testRequester)
        {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setTag("Token", controller.serialize(testRequester.token));
            return compound;
        }

        @NotNull
        @Override
        public TestRequester deserialize(@NotNull final IFactoryController controller, @NotNull final NBTTagCompound nbt)
        {
            IToken token = controller.deserialize(nbt.getCompoundTag("Token"));
            return new TestRequester(token);
        }
    }*/
}