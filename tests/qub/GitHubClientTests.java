package qub;

public interface GitHubClientTests
{
    IntegerValue fakeRepositoryCount = IntegerValue.create(0);
    static String getFakeRepositoryName()
    {
        return "fake-repo-name-" + fakeRepositoryCount.incrementAndGetAsInt();
    }

    static void test(TestRunner runner)
    {
        runner.testGroup(GitHubClient.class, () ->
        {
            runner.testGroup("create(Network)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final Network network = null;
                    test.assertThrows(() -> GitHubClient.create(network),
                        new PreConditionFailure("network cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final Network network = process.getNetwork();
                    final GitHubClient gitHubClient = GitHubClient.create(network);
                    test.assertNotNull(gitHubClient);
                });
            });

            runner.testGroup("create(HttpClient)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final HttpClient httpClient = null;
                    test.assertThrows(() -> GitHubClient.create(httpClient),
                        new PreConditionFailure("httpClient cannot be null."));
                });

                runner.test("with non-null",
                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
                    (Test test, FakeDesktopProcess process) ->
                {
                    final HttpClient httpClient = HttpClient.create(process.getNetwork());
                    final GitHubClient gitHubClient = GitHubClient.create(httpClient);
                    test.assertNotNull(gitHubClient);
                });
            });
        });
    }

    static void test(TestRunner runner, Function1<AccessTokenType,? extends GitHubClient> creator)
    {
        PreCondition.assertNotNull(runner, "runner");
        PreCondition.assertNotNull(creator, "creator");

        runner.testGroup(GitHubClient.class,
            (TestResources resources) -> Tuple.create(resources.getClock()),
            (Clock clock) ->
        {
            runner.testGroup("setAccessToken(String)", () ->
            {
                final Action2<String,Throwable> setAccessTokenErrorTest = (String personalAccessToken, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(personalAccessToken), (Test test) ->
                    {
                        final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                        test.assertFalse(gitHubClient.hasAccessToken());

                        test.assertThrows(() -> gitHubClient.setAccessToken(personalAccessToken),
                            expected);
                        test.assertFalse(gitHubClient.hasAccessToken());
                    });
                };

                setAccessTokenErrorTest.run(null, new PreConditionFailure("accessToken cannot be null."));
                setAccessTokenErrorTest.run("", new PreConditionFailure("accessToken cannot be empty."));

                final Action1<String> setAccessTokenTest = (String personalAccessToken) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(personalAccessToken), (Test test) ->
                    {
                        final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                        test.assertFalse(gitHubClient.hasAccessToken());

                        final GitHubClient setAccessTokenResult = gitHubClient.setAccessToken(personalAccessToken);
                        test.assertSame(gitHubClient, setAccessTokenResult);
                        test.assertTrue(gitHubClient.hasAccessToken());
                    });
                };

                setAccessTokenTest.run("fakeGitHubToken");
            });

            runner.testGroup("setBaseUrl(String)", () ->
            {
                final Action2<String,Throwable> setBaseUrlErrorTest = (String baseUrl, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(baseUrl), (Test test) ->
                    {
                        final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                        test.assertEqual(URL.parse("https://api.github.com").await(), gitHubClient.getBaseUrl());

                        test.assertThrows(() -> gitHubClient.setBaseUrl(baseUrl).await(),
                            expected);
                        test.assertEqual(URL.parse("https://api.github.com").await(), gitHubClient.getBaseUrl());
                    });
                };

                setBaseUrlErrorTest.run(null, new PreConditionFailure("baseUrl cannot be null."));
                setBaseUrlErrorTest.run("", new PreConditionFailure("baseUrl cannot be empty."));
                setBaseUrlErrorTest.run("hello there", new java.lang.IllegalArgumentException("A URL must begin with either a scheme (such as \"http\") or a host (such as \"www.example.com\"), not \" \"."));

                final Action1<String> setBaseUrlTest = (String baseUrl) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(baseUrl), (Test test) ->
                    {
                        final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                        test.assertEqual(URL.parse("https://api.github.com").await(), gitHubClient.getBaseUrl());

                        final GitHubClient setBaseUrlResult = gitHubClient.setBaseUrl(baseUrl).await();
                        test.assertSame(gitHubClient, setBaseUrlResult);
                        test.assertEqual(baseUrl, gitHubClient.getBaseUrl().toString());
                    });
                };

                setBaseUrlTest.run("https://api.github.com");
                setBaseUrlTest.run("api.github.com");
                setBaseUrlTest.run("https://my.github.base.url");
                setBaseUrlTest.run("my.github.base.url");
            });

            runner.testGroup("sendRequest(GitHubRequest)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    test.assertThrows(() -> gitHubClient.sendRequest(null),
                        new PreConditionFailure("request cannot be null."));
                });

                runner.test("with empty request", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GitHubRequest request = GitHubRequest.create();
                    test.assertThrows(() -> gitHubClient.sendRequest(request),
                        new PreConditionFailure("request.getHttpMethod() cannot be null."));
                });

                runner.test("with no URL path", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GitHubRequest request = GitHubRequest.create()
                        .setHttpMethod(HttpMethod.GET);
                    test.assertThrows(() -> gitHubClient.sendRequest(request),
                        new PreConditionFailure("request.getUrlPath() cannot be null."));
                });

                runner.test("with URL path but no token or base url", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GitHubRequest request = GitHubRequest.create()
                        .setHttpMethod(HttpMethod.GET)
                        .setUrlPath("/user");
                    try (final GitHubResponse response = gitHubClient.sendRequest(request).await())
                    {
                        GitHubClientTests.assertErrorResponse(test, response,
                            401, (GitHubErrorResponse errorResponse) ->
                            {
                                test.assertEqual("Requires authentication", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/users#get-the-authenticated-user", errorResponse.getDocumentationUrl());
                                test.assertEqual(Iterable.create(), errorResponse.getErrors());
                            });
                    }
                });

                runner.test("with base url", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GitHubRequest request = GitHubRequest.create()
                        .setHttpMethod(HttpMethod.GET)
                        .setBaseUrl(URL.parse("https://api.github.com").await())
                        .setUrlPath("/user");
                    try (final GitHubResponse response = gitHubClient.sendRequest(request).await())
                    {
                        GitHubClientTests.assertErrorResponse(test, response,
                            401, (GitHubErrorResponse errorResponse) ->
                            {
                                test.assertEqual("Requires authentication", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/users#get-the-authenticated-user", errorResponse.getDocumentationUrl());
                                test.assertEqual(Iterable.create(), errorResponse.getErrors());
                            });
                    }
                });
            });

            runner.testGroup("sendGetAuthenticatedUserRequest()", () ->
            {
                runner.test("when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    try (final GetAuthenticatedUserResponse response = gitHubClient.sendGetAuthenticatedUserRequest().await())
                    {
                        GitHubClientTests.assertErrorResponse(test, response,
                            401, (GitHubErrorResponse errorResponse) ->
                            {
                                test.assertEqual("Requires authentication", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/users#get-the-authenticated-user", errorResponse.getDocumentationUrl());
                                test.assertEqual(Iterable.create(), errorResponse.getErrors());
                            });
                    }
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    try (final GetAuthenticatedUserResponse response = gitHubClient.sendGetAuthenticatedUserRequest().await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(401, response.getStatusCode());
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        test.assertEqual("Bad credentials", errorResponse.getMessage());
                        test.assertEqual("https://docs.github.com/rest", errorResponse.getDocumentationUrl());
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    try (final GetAuthenticatedUserResponse response = gitHubClient.sendGetAuthenticatedUserRequest().await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(200, response.getStatusCode());
                        test.assertFalse(response.isErrorResponse());
                        final GitHubUser user = response.getAuthenticatedUser().await();
                        test.assertNotNull(user);
                        test.assertNotNullAndNotEmpty(user.getLogin());
                    }
                });
            });

            runner.testGroup("getAuthenticatedUser()", () ->
            {
                runner.test("when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GitHubException exception = test.assertThrows(GitHubException.class, () -> gitHubClient.getAuthenticatedUser().await());
                    GitHubClientTests.assertException(test, exception,
                        401, () ->
                        {
                            test.assertEqual("Requires authentication", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/reference/users#get-the-authenticated-user", exception.getDocumentationUrl());
                            test.assertEqual(Iterable.create(), exception.getErrors());
                        });
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final GitHubException exception = test.assertThrows(GitHubException.class, () -> gitHubClient.getAuthenticatedUser().await());
                    test.assertEqual(401, exception.getStatusCode());
                    test.assertEqual("Bad credentials", exception.getMessage());
                    test.assertEqual("https://docs.github.com/rest", exception.getDocumentationUrl());
                    test.assertEqual(Iterable.create(), exception.getErrors());
                });

                runner.test("when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final GitHubUser user = gitHubClient.getAuthenticatedUser().await();
                    test.assertNotNull(user);
                    test.assertNotNullAndNotEmpty(user.getLogin());
                });
            });

            runner.testGroup("sendGetRepositoryRequest(GetRepositoryParameters)", () ->
            {
                final Action2<GetRepositoryParameters,Throwable> getRepositoryErrorTest = (GetRepositoryParameters parameters, Throwable expected) ->
                {
                    runner.test("with " + parameters, (Test test) ->
                    {
                        final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                        test.assertThrows(() -> gitHubClient.getRepository(parameters).await(), expected);
                    });
                };

                getRepositoryErrorTest.run(null, new PreConditionFailure("parameters cannot be null."));
                getRepositoryErrorTest.run(GetRepositoryParameters.create(), new PreConditionFailure("parameters.getOwner() cannot be null."));
                getRepositoryErrorTest.run(GetRepositoryParameters.create().setOwner("fake-owner"), new PreConditionFailure("parameters.getName() cannot be null."));
                getRepositoryErrorTest.run(GetRepositoryParameters.create().setName("fake-name"), new PreConditionFailure("parameters.getOwner() cannot be null."));

                runner.test("with non-existing repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GetRepositoryParameters parameters = GetRepositoryParameters.create()
                        .setOwner("fake-owner")
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    try (final GetRepositoryResponse response = gitHubClient.getRepository(parameters).await())
                    {
                        GitHubClientTests.assertErrorResponse(test, response,
                            404, (GitHubErrorResponse errorResponse) ->
                            {
                                test.assertEqual(404, response.getStatusCode());
                                test.assertEqual("Not Found", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/repos#get-a-repository", errorResponse.getDocumentationUrl());
                                test.assertEqual(Iterable.create(), errorResponse.getErrors());
                            });
                    }
                });

                runner.test("with existing public repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GetRepositoryParameters parameters = GetRepositoryParameters.create()
                        .setOwner("octokit")
                        .setName("octokit.net");
                    try (final GetRepositoryResponse response = gitHubClient.getRepository(parameters).await())
                    {
                        GitHubClientTests.assertResponse(test, response,
                            200, () ->
                            {
                                test.assertFalse(response.isErrorResponse());
                                final GitHubRepository repository = response.getRepository().await();
                                test.assertNotNull(repository);
                                test.assertEqual("octokit", repository.getOwner().getLogin());
                                test.assertEqual("octokit.net", repository.getName());
                                test.assertEqual("octokit/octokit.net", repository.getFullName());
                            });
                    }
                });

                runner.test("with non-existing repository and invalid token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final GetRepositoryParameters parameters = GetRepositoryParameters.create()
                        .setOwner("fake-owner")
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    try (final GetRepositoryResponse response = gitHubClient.getRepository(parameters).await())
                    {
                        test.assertEqual(401, response.getStatusCode());
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        test.assertEqual("Bad credentials", errorResponse.getMessage());
                        test.assertEqual("https://docs.github.com/rest", errorResponse.getDocumentationUrl());
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("with existing public repository and invalid token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final GetRepositoryParameters parameters = GetRepositoryParameters.create()
                        .setOwner("octokit")
                        .setName("octokit.net");
                    try (final GetRepositoryResponse response = gitHubClient.getRepository(parameters).await())
                    {
                        test.assertEqual(401, response.getStatusCode());
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        test.assertEqual("Bad credentials", errorResponse.getMessage());
                        test.assertEqual("https://docs.github.com/rest", errorResponse.getDocumentationUrl());
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("with non-existing repository when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final GetRepositoryParameters parameters = GetRepositoryParameters.create()
                        .setOwner("fake-owner")
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    try (final GetRepositoryResponse response = gitHubClient.getRepository(parameters).await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(404, response.getStatusCode());
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        test.assertEqual("Not Found", errorResponse.getMessage());
                        test.assertEqual("https://docs.github.com/rest/reference/repos#get-a-repository", errorResponse.getDocumentationUrl());
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("with existing public repository when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final GetRepositoryParameters parameters = GetRepositoryParameters.create()
                        .setOwner("octokit")
                        .setName("octokit.net");
                    try (final GetRepositoryResponse response = gitHubClient.getRepository(parameters).await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(200, response.getStatusCode());
                        test.assertFalse(response.isErrorResponse());
                        final GitHubRepository repository = response.getRepository().await();
                        test.assertNotNull(repository);
                        test.assertEqual("octokit", repository.getOwner().getLogin());
                        test.assertEqual("octokit.net", repository.getName());
                        test.assertEqual("octokit/octokit.net", repository.getFullName());
                    }
                });
            });

            runner.testGroup("sendGetRepositoriesForAuthenticatedUserRequest()", () ->
            {
                runner.test("when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    try (final GetRepositoriesForAuthenticatedUserResponse response = gitHubClient.sendGetRepositoriesForAuthenticatedUserRequest().await())
                    {
                        GitHubClientTests.assertErrorResponse(test, response,
                            401, (GitHubErrorResponse errorResponse) ->
                            {
                                test.assertEqual("Requires authentication", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/repos#list-repositories-for-the-authenticated-user", errorResponse.getDocumentationUrl());
                                test.assertEqual(Iterable.create(), errorResponse.getErrors());
                            });
                    }
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    try (final GetRepositoriesForAuthenticatedUserResponse response = gitHubClient.sendGetRepositoriesForAuthenticatedUserRequest().await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(401, response.getStatusCode());
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        test.assertEqual("Bad credentials", errorResponse.getMessage());
                        test.assertEqual("https://docs.github.com/rest", errorResponse.getDocumentationUrl());
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    try (final GetRepositoriesForAuthenticatedUserResponse response = gitHubClient.sendGetRepositoriesForAuthenticatedUserRequest().await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(200, response.getStatusCode());
                        test.assertFalse(response.isErrorResponse());
                        final Iterable<GitHubRepository> repositories = response.getRepositories().await();
                        test.assertNotNull(repositories);
                    }
                });
            });

            runner.testGroup("getRepositoriesForAuthenticatedUser()", () ->
            {
                runner.test("when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.getRepositoriesForAuthenticatedUser().await(), GitHubException.class);
                    GitHubClientTests.assertException(test, exception,
                        401, () ->
                        {
                            test.assertEqual("Requires authentication", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/reference/repos#list-repositories-for-the-authenticated-user", exception.getDocumentationUrl());
                            test.assertEqual(Iterable.create(), exception.getErrors());
                        });
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.getRepositoriesForAuthenticatedUser().await(), GitHubException.class);
                    test.assertEqual(401, exception.getStatusCode());
                    test.assertEqual("Bad credentials", exception.getMessage());
                    test.assertEqual("https://docs.github.com/rest", exception.getDocumentationUrl());
                    test.assertEqual(Iterable.create(), exception.getErrors());
                });

                runner.test("when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final Iterable<GitHubRepository> repositories = gitHubClient.getRepositoriesForAuthenticatedUser().await();
                    test.assertNotNull(repositories);
                });
            });

            runner.testGroup("sendCreateRepositoryRequest(CreateRepositoryParameters)", () ->
            {
                final Action2<CreateRepositoryParameters,Throwable> createRepositoryErrorTest = (CreateRepositoryParameters parameters, Throwable expected) ->
                {
                    runner.test("with " + parameters, (Test test) ->
                    {
                        final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                        test.assertThrows(() -> gitHubClient.sendCreateRepositoryRequest(parameters),
                            expected);
                    });
                };

                createRepositoryErrorTest.run(null, new PreConditionFailure("parameters cannot be null."));
                createRepositoryErrorTest.run(CreateRepositoryParameters.create(), new PreConditionFailure("parameters.getName() cannot be null."));

                runner.test("when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    try (final CreateRepositoryResponse response = gitHubClient.sendCreateRepositoryRequest(parameters).await())
                    {
                        GitHubClientTests.assertErrorResponse(test, response,
                            401, (GitHubErrorResponse errorResponse) ->
                            {
                                test.assertEqual("Requires authentication", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/repos#create-a-repository-for-the-authenticated-user", errorResponse.getDocumentationUrl());
                                test.assertEqual(Iterable.create(), errorResponse.getErrors());
                            });
                    }
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    try (final CreateRepositoryResponse response = gitHubClient.sendCreateRepositoryRequest(parameters).await())
                    {
                        test.assertEqual(401, response.getStatusCode());
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        test.assertEqual("Bad credentials", errorResponse.getMessage());
                        test.assertEqual("https://docs.github.com/rest", errorResponse.getDocumentationUrl());
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("with repository that already exists", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
                        .setName("github-java");
                    try (final CreateRepositoryResponse response = gitHubClient.sendCreateRepositoryRequest(parameters).await())
                    {
                        test.assertEqual(422, response.getStatusCode());
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        test.assertEqual("Repository creation failed.", errorResponse.getMessage());
                        test.assertEqual("https://docs.github.com/rest/reference/repos#create-a-repository-for-the-authenticated-user", errorResponse.getDocumentationUrl());
                        test.assertEqual(
                            Iterable.create(
                                GitHubError.create()
                                    .setResource("Repository")
                                    .setCode("custom")
                                    .setField("name")
                                    .setMessage("name already exists on this account")
                            ),
                            errorResponse.getErrors());
                    }
                });

                runner.test("with repository name that doesn't exist", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final String repositoryOwner = gitHubClient.getAuthenticatedUser().await().getLogin();
                    final String repositoryName = GitHubClientTests.getFakeRepositoryName();
                    final DeleteRepositoryParameters deleteParameters = DeleteRepositoryParameters.create()
                        .setOwner(repositoryOwner)
                        .setName(repositoryName);
                    gitHubClient.deleteRepository(deleteParameters)
                        .catchError()
                        .await();
                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
                        .setName(repositoryName);
                    try (final CreateRepositoryResponse response = gitHubClient.sendCreateRepositoryRequest(parameters).await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(201, response.getStatusCode());
                        test.assertFalse(response.isErrorResponse());
                        final GitHubRepository repository = response.getRepository().await();
                        test.assertEqual(repositoryName, repository.getName());
                        final GitHubUser owner = repository.getOwner();
                        test.assertNotNull(owner);
                        test.assertEqual(repositoryOwner, owner.getLogin());
                        test.assertEqual(repositoryOwner + "/" + repositoryName, repository.getFullName());

                        try (final GetRepositoriesForAuthenticatedUserResponse repositoriesResponse = gitHubClient.sendGetRepositoriesForAuthenticatedUserRequest().await())
                        {
                            final Iterable<GitHubRepository> authenticatedUserRepositories = repositoriesResponse.getRepositories().await();
                            test.assertTrue(authenticatedUserRepositories.contains((GitHubRepository existingRepository) ->
                            {
                                return Strings.equal(existingRepository.getFullName(), repository.getFullName());
                            }));
                        }
                    }
                    finally
                    {
                        GitHubClientTests.retry(clock, () ->
                        {
                            gitHubClient.deleteRepository(deleteParameters).await();
                        });
                    }
                });



                runner.test("with repository name that doesn't exist", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final String repositoryOwner = gitHubClient.getAuthenticatedUser().await().getLogin();
                    final String repositoryName = GitHubClientTests.getFakeRepositoryName();
                    final DeleteRepositoryParameters deleteParameters = DeleteRepositoryParameters.create()
                        .setOwner(repositoryOwner)
                        .setName(repositoryName);
                    gitHubClient.deleteRepository(deleteParameters)
                        .catchError()
                        .await();
                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
                        .setName(repositoryName);
                    try (final CreateRepositoryResponse response = gitHubClient.sendCreateRepositoryRequest(parameters).await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(201, response.getStatusCode());
                        test.assertFalse(response.isErrorResponse());
                        final GitHubRepository repository = response.getRepository().await();
                        test.assertEqual(repositoryName, repository.getName());
                        final GitHubUser owner = repository.getOwner();
                        test.assertNotNull(owner);
                        test.assertEqual(repositoryOwner, owner.getLogin());
                        test.assertEqual(repositoryOwner + "/" + repositoryName, repository.getFullName());

                        try (final GetRepositoriesForAuthenticatedUserResponse repositoriesResponse = gitHubClient.sendGetRepositoriesForAuthenticatedUserRequest().await())
                        {
                            final Iterable<GitHubRepository> authenticatedUserRepositories = repositoriesResponse.getRepositories().await();
                            test.assertTrue(authenticatedUserRepositories.contains((GitHubRepository existingRepository) ->
                            {
                                return Strings.equal(existingRepository.getFullName(), repository.getFullName());
                            }));
                        }
                    }
                    finally
                    {
                        GitHubClientTests.retry(clock, () ->
                        {
                            gitHubClient.deleteRepository(deleteParameters).await();
                        });
                    }
                });
            });

            runner.testGroup("createRepository(CreateRepositoryParameters)", () ->
            {
                final Action2<CreateRepositoryParameters,Throwable> createRepositoryErrorTest = (CreateRepositoryParameters parameters, Throwable expected) ->
                {
                    runner.test("with " + parameters, (Test test) ->
                    {
                        final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                        test.assertThrows(() -> gitHubClient.createRepository(parameters),
                            expected);
                    });
                };

                createRepositoryErrorTest.run(null, new PreConditionFailure("parameters cannot be null."));
                createRepositoryErrorTest.run(CreateRepositoryParameters.create(), new PreConditionFailure("parameters.getName() cannot be null."));

                runner.test("when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.createRepository(parameters).await(), GitHubException.class);
                    GitHubClientTests.assertException(test, exception,
                        401, () ->
                        {
                            test.assertEqual("Requires authentication", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/reference/repos#create-a-repository-for-the-authenticated-user", exception.getDocumentationUrl());
                            test.assertEqual(Iterable.create(), exception.getErrors());
                        });
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.createRepository(parameters).await(), GitHubException.class);
                    test.assertEqual(401, exception.getStatusCode());
                    test.assertEqual("Bad credentials", exception.getMessage());
                    test.assertEqual("https://docs.github.com/rest", exception.getDocumentationUrl());
                    test.assertEqual(Iterable.create(), exception.getErrors());
                });

                runner.test("with repository that already exists", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
                        .setName("github-java");
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.createRepository(parameters).await(), GitHubException.class);
                    test.assertEqual(422, exception.getStatusCode());
                    test.assertEqual("Repository creation failed.", exception.getMessage());
                    test.assertEqual("https://docs.github.com/rest/reference/repos#create-a-repository-for-the-authenticated-user", exception.getDocumentationUrl());
                    test.assertEqual(
                        Iterable.create(
                            GitHubError.create()
                                .setResource("Repository")
                                .setCode("custom")
                                .setField("name")
                                .setMessage("name already exists on this account")
                        ),
                        exception.getErrors());
                });

                runner.test("with repository name that doesn't exist", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final String repositoryName = GitHubClientTests.getFakeRepositoryName();
                    gitHubClient.deleteRepository(DeleteRepositoryParameters.create()
                        .setOwner(gitHubClient.getAuthenticatedUser().await().getLogin())
                        .setName(repositoryName))
                        .catchError()
                        .await();
                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
                        .setName(repositoryName);
                    final GitHubRepository repository = gitHubClient.createRepository(parameters).await();
                    try
                    {
                        test.assertEqual(repositoryName, repository.getName());
                        final GitHubUser owner = repository.getOwner();
                        test.assertNotNull(owner);
                        final String repositoryOwner = owner.getLogin();
                        test.assertNotNullAndNotEmpty(repositoryOwner);
                        test.assertEqual(repositoryOwner + "/" + repositoryName, repository.getFullName());
                        test.assertEqual(URL.parse("git://github.com/" + repositoryOwner + "/" + repositoryName + ".git").await(), repository.getGitUrl());
                        test.assertEqual(URL.parse("https://github.com/" + repositoryOwner + "/" + repositoryName + ".git").await(), repository.getCloneUrl());

                        final Iterable<GitHubRepository> authenticatedUserRepositories = gitHubClient.getRepositoriesForAuthenticatedUser().await();
                        test.assertTrue(authenticatedUserRepositories.contains((GitHubRepository existingRepository) ->
                        {
                            return Strings.equal(existingRepository.getFullName(), repository.getFullName());
                        }));
                    }
                    finally
                    {
                        gitHubClient.deleteRepository(repository).catchError().await();
                    }
                });
            });

            runner.testGroup("sendDeleteRepositoryRequest(DeleteRepositoryParameters)", () ->
            {
                final Action2<DeleteRepositoryParameters,Throwable> sendDeleteRepositoryRequestErrorTest = (DeleteRepositoryParameters parameters, Throwable expected) ->
                {
                    runner.test("with " + parameters, (Test test) ->
                    {
                        final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                        test.assertThrows(() -> gitHubClient.sendDeleteRepositoryRequest(parameters),
                            expected);
                    });
                };

                sendDeleteRepositoryRequestErrorTest.run(null, new PreConditionFailure("parameters cannot be null."));
                sendDeleteRepositoryRequestErrorTest.run(DeleteRepositoryParameters.create(), new PreConditionFailure("parameters.getOwner() cannot be null."));
                sendDeleteRepositoryRequestErrorTest.run(DeleteRepositoryParameters.create().setOwner("fake-owner"), new PreConditionFailure("parameters.getName() cannot be null."));
                sendDeleteRepositoryRequestErrorTest.run(DeleteRepositoryParameters.create().setName("fake-name"), new PreConditionFailure("parameters.getOwner() cannot be null."));

                runner.test("with non-existing repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner("fake-owner")
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(parameters).await())
                    {
                        GitHubClientTests.assertErrorResponse(test, response,
                            404, (GitHubErrorResponse errorResponse) ->
                            {
                                test.assertEqual("Not Found", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", errorResponse.getDocumentationUrl());
                                test.assertEqual(Iterable.create(), errorResponse.getErrors());
                            });
                    }
                });

                runner.test("with existing repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner("danschultequb")
                        .setName("github-java");
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(parameters).await())
                    {
                        GitHubClientTests.assertErrorResponse(test, response,
                            403, (GitHubErrorResponse errorResponse) ->
                            {
                                test.assertEqual("Must have admin rights to Repository.", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", errorResponse.getDocumentationUrl());
                                test.assertEqual(Iterable.create(), errorResponse.getErrors());
                            });
                    }
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner("fake-owner")
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(parameters).await())
                    {
                        test.assertEqual(401, response.getStatusCode());
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        test.assertEqual("Bad credentials", errorResponse.getMessage());
                        test.assertEqual("https://docs.github.com/rest", errorResponse.getDocumentationUrl());
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("with non-existing repository when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final String authenticatedUserLogin = gitHubClient.getAuthenticatedUser().await().getLogin();
                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner(authenticatedUserLogin)
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(parameters).await())
                    {
                        test.assertNotNull(response);
                        final int statusCode = response.getStatusCode();
                        test.assertOneOf(Iterable.create(204, 404), statusCode);
                        switch (statusCode)
                        {
                            case 404:
                                test.assertTrue(response.isErrorResponse());
                                final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                                test.assertEqual("Not Found", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", errorResponse.getDocumentationUrl());
                                test.assertEqual(Iterable.create(), errorResponse.getErrors());
                                break;

                            default:
                                test.fail("Unexpected status code: " + statusCode);
                                break;
                        }
                    }
                });

                runner.test("with existing repository when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final GitHubRepository repository = GitHubClientTests.getExistingRepository(gitHubClient, GitHubClientTests.getFakeRepositoryName(), clock);

                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner(repository.getOwner().getLogin())
                        .setName(repository.getName());

                    GitHubClientTests.retry(clock, () ->
                    {
                        try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(parameters).await())
                        {
                            test.assertNotNull(response);
                            test.assertEqual(204, response.getStatusCode());
                            test.assertFalse(response.isErrorResponse());
                        }
                    });
                });
            });

            runner.testGroup("sendDeleteRepositoryRequest(GitHubRepository)", () ->
            {
                final Action2<GitHubRepository,Throwable> sendDeleteRepositoryRequestErrorTest = (GitHubRepository repository, Throwable expected) ->
                {
                    runner.test("with " + repository, (Test test) ->
                    {
                        final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                        test.assertThrows(() -> gitHubClient.sendDeleteRepositoryRequest(repository),
                            expected);
                    });
                };

                sendDeleteRepositoryRequestErrorTest.run(
                    null,
                    new PreConditionFailure("repository cannot be null."));
                sendDeleteRepositoryRequestErrorTest.run(
                    GitHubRepository.create(),
                    new PreConditionFailure("repository.getOwner() cannot be null."));
                sendDeleteRepositoryRequestErrorTest.run(
                    GitHubRepository.create()
                        .setOwner(GitHubUser.create()),
                    new PreConditionFailure("repository.getOwner().getLogin() cannot be null."));
                sendDeleteRepositoryRequestErrorTest.run(
                    GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin("fake-login")),
                    new PreConditionFailure("repository.getName() cannot be null."));
                sendDeleteRepositoryRequestErrorTest.run(
                    GitHubRepository.create()
                        .setName("fake-name"),
                    new PreConditionFailure("repository.getOwner() cannot be null."));
                sendDeleteRepositoryRequestErrorTest.run(
                    GitHubRepository.create()
                        .setName("fake-name")
                        .setOwner(GitHubUser.create()),
                    new PreConditionFailure("repository.getOwner().getLogin() cannot be null."));

                runner.test("with non-existing repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GitHubRepository repository = GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin("fake-owner"))
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(repository).await())
                    {
                        GitHubClientTests.assertResponse(test, response,
                            404, () ->
                            {
                                test.assertTrue(response.isErrorResponse());
                                final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                                test.assertEqual("Not Found", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", errorResponse.getDocumentationUrl());
                                test.assertEqual(Iterable.create(), errorResponse.getErrors());
                            });
                    }
                });

                runner.test("with existing repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GitHubRepository repository = GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin("fake-owner"))
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(repository).await())
                    {
                        GitHubClientTests.assertResponse(test, response,
                            404, () ->
                            {
                                test.assertTrue(response.isErrorResponse());
                                final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                                test.assertEqual("Not Found", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", errorResponse.getDocumentationUrl());
                                test.assertEqual(Iterable.create(), errorResponse.getErrors());
                            });
                    }
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final GitHubRepository repository = GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin("fake-owner"))
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(repository).await())
                    {
                        test.assertEqual(401, response.getStatusCode());
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        test.assertEqual("Bad credentials", errorResponse.getMessage());
                        test.assertEqual("https://docs.github.com/rest", errorResponse.getDocumentationUrl());
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("with non-existing repository when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final String authenticatedUserLogin = gitHubClient.getAuthenticatedUser().await().getLogin();
                    final GitHubRepository repository = GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin(authenticatedUserLogin))
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(repository).await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(404, response.getStatusCode());
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        test.assertEqual("Not Found", errorResponse.getMessage());
                        test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", errorResponse.getDocumentationUrl());
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("with existing repository when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final GitHubRepository repository = GitHubClientTests.getExistingRepository(gitHubClient, GitHubClientTests.getFakeRepositoryName(), clock);
                    GitHubClientTests.retry(clock, () ->
                    {
                        try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(repository).await())
                        {
                            test.assertNotNull(response);
                            test.assertEqual(204, response.getStatusCode());
                            test.assertFalse(response.isErrorResponse());
                        }
                    });
                });
            });

            runner.testGroup("deleteRepository(DeleteRepositoryParameters)", () ->
            {
                final Action2<DeleteRepositoryParameters,Throwable> deleteRepositoryErrorTest = (DeleteRepositoryParameters parameters, Throwable expected) ->
                {
                    runner.test("with " + parameters, (Test test) ->
                    {
                        final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                        test.assertThrows(() -> gitHubClient.deleteRepository(parameters),
                            expected);
                    });
                };

                deleteRepositoryErrorTest.run(null, new PreConditionFailure("parameters cannot be null."));
                deleteRepositoryErrorTest.run(DeleteRepositoryParameters.create(), new PreConditionFailure("parameters.getOwner() cannot be null."));
                deleteRepositoryErrorTest.run(DeleteRepositoryParameters.create().setOwner("fake-owner"), new PreConditionFailure("parameters.getName() cannot be null."));
                deleteRepositoryErrorTest.run(DeleteRepositoryParameters.create().setName("fake-name"), new PreConditionFailure("parameters.getOwner() cannot be null."));

                runner.test("with non-existing repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner("fake-owner")
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(parameters).await(), GitHubException.class);
                    GitHubClientTests.assertException(test, exception,
                        404, () ->
                        {
                            test.assertEqual("Not Found", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", exception.getDocumentationUrl());
                            test.assertEqual(Iterable.create(), exception.getErrors());
                        });
                });

                runner.test("with existing repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner("danschultequb")
                        .setName("github-java");
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(parameters).await(), GitHubException.class);
                    GitHubClientTests.assertException(test, exception,
                        403, () ->
                        {
                            test.assertEqual("Must have admin rights to Repository.", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", exception.getDocumentationUrl());
                            test.assertEqual(Iterable.create(), exception.getErrors());
                        });
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner("fake-owner")
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(parameters).await(), GitHubException.class);
                    test.assertEqual(401, exception.getStatusCode());
                    test.assertEqual("Bad credentials", exception.getMessage());
                    test.assertEqual("https://docs.github.com/rest", exception.getDocumentationUrl());
                    test.assertEqual(Iterable.create(), exception.getErrors());
                });

                runner.test("with non-existing repository when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final String authenticatedUserLogin = gitHubClient.getAuthenticatedUser().await().getLogin();
                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner(authenticatedUserLogin)
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(parameters).await(), GitHubException.class);
                    test.assertEqual(404, exception.getStatusCode());
                    test.assertEqual("Not Found", exception.getMessage());
                    test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", exception.getDocumentationUrl());
                    test.assertEqual(Iterable.create(), exception.getErrors());
                });

                runner.test("with existing repository when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final String repositoryName = GitHubClientTests.getFakeRepositoryName();
                    final GitHubRepository existingRepository = GitHubClientTests.getExistingRepository(gitHubClient, repositoryName, clock);

                    GitHubClientTests.retry(clock, () ->
                    {
                        final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                            .setOwner(existingRepository.getOwner().getLogin())
                            .setName(existingRepository.getName());
                        gitHubClient.deleteRepository(parameters).await();
                    });

                    GitHubClientTests.retry(clock, () ->
                    {
                        final Iterable<GitHubRepository> repositoriesAfterDelete = gitHubClient.getRepositoriesForAuthenticatedUser().await();
                        test.assertFalse(repositoriesAfterDelete.contains(existingRepository));
                    });
                });
            });

            runner.testGroup("deleteRepository(GitHubRepository)", () ->
            {
                final Action2<GitHubRepository,Throwable> deleteRepositoryErrorTest = (GitHubRepository repository, Throwable expected) ->
                {
                    runner.test("with " + repository, (Test test) ->
                    {
                        final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                        test.assertThrows(() -> gitHubClient.deleteRepository(repository),
                            expected);
                    });
                };

                deleteRepositoryErrorTest.run(
                    null,
                    new PreConditionFailure("repository cannot be null."));
                deleteRepositoryErrorTest.run(
                    GitHubRepository.create(),
                    new PreConditionFailure("repository.getOwner() cannot be null."));
                deleteRepositoryErrorTest.run(
                    GitHubRepository.create()
                        .setOwner(GitHubUser.create()),
                    new PreConditionFailure("repository.getOwner().getLogin() cannot be null."));
                deleteRepositoryErrorTest.run(
                    GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin("fake-owner")),
                    new PreConditionFailure("repository.getName() cannot be null."));
                deleteRepositoryErrorTest.run(
                    GitHubRepository.create()
                        .setName("fake-name"),
                    new PreConditionFailure("repository.getOwner() cannot be null."));
                deleteRepositoryErrorTest.run(
                    GitHubRepository.create()
                        .setOwner(GitHubUser.create())
                        .setName("fake-name"),
                    new PreConditionFailure("repository.getOwner().getLogin() cannot be null."));

                runner.test("with non-existing repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GitHubRepository repository = GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin("fake-owner"))
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(repository).await(), GitHubException.class);
                    GitHubClientTests.assertException(test, exception,
                        404, () ->
                        {
                            test.assertEqual("Not Found", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", exception.getDocumentationUrl());
                            test.assertEqual(Iterable.create(), exception.getErrors());
                        });
                });

                runner.test("with existing repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GitHubRepository repository = GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin("fake-owner"))
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(repository).await(), GitHubException.class);
                    GitHubClientTests.assertException(test, exception,
                        404, () ->
                        {
                            test.assertEqual("Not Found", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", exception.getDocumentationUrl());
                            test.assertEqual(Iterable.create(), exception.getErrors());
                        });
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final GitHubRepository repository = GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin("fake-owner"))
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(repository).await(), GitHubException.class);
                    test.assertEqual(401, exception.getStatusCode());
                    test.assertEqual("Bad credentials", exception.getMessage());
                    test.assertEqual("https://docs.github.com/rest", exception.getDocumentationUrl());
                    test.assertEqual(Iterable.create(), exception.getErrors());
                });

                runner.test("with non-existing repository when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final String authenticatedUserLogin = gitHubClient.getAuthenticatedUser().await().getLogin();
                    final GitHubRepository repository = GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin(authenticatedUserLogin))
                        .setName(GitHubClientTests.getFakeRepositoryName());
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(repository).await(), GitHubException.class);
                    test.assertEqual(404, exception.getStatusCode());
                    test.assertEqual("Not Found", exception.getMessage());
                    test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", exception.getDocumentationUrl());
                    test.assertEqual(Iterable.create(), exception.getErrors());
                });

                runner.test("with existing repository when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final String repositoryName = GitHubClientTests.getFakeRepositoryName();
                    final GitHubRepository existingRepository = GitHubClientTests.getExistingRepository(gitHubClient, repositoryName, clock);

                    GitHubClientTests.retry(clock, () ->
                    {
                        gitHubClient.deleteRepository(existingRepository).await();
                    });

                    GitHubClientTests.retry(clock, () ->
                    {
                        final Iterable<GitHubRepository> repositoriesAfterDelete = gitHubClient.getRepositoriesForAuthenticatedUser().await();
                        test.assertFalse(repositoriesAfterDelete.contains(existingRepository));
                    });
                });
            });
        });
    }

    static GitHubRepository getExistingRepository(GitHubClient githubClient, String repositoryName, Clock clock)
    {
        PreCondition.assertNotNull(githubClient, "githubClient");
        PreCondition.assertNotNullAndNotEmpty(repositoryName, "repositoryName");
        PreCondition.assertNotNull(clock, "clock");

        final GitHubUser authenticatedUser = githubClient.getAuthenticatedUser().await();
        final String owner = authenticatedUser.getLogin();

        final GetRepositoryParameters getRepositoryParameters = GetRepositoryParameters.create()
            .setOwner(owner)
            .setName(repositoryName);
        final GetRepositoryResponse getRepositoryResponse = githubClient.getRepository(getRepositoryParameters).await();
        GitHubRepository repository = getRepositoryResponse.getRepository().catchError().await();
        if (repository == null)
        {
            final CreateRepositoryParameters createParameters = CreateRepositoryParameters.create()
                .setName(repositoryName);
            repository = GitHubClientTests.retry(clock, () -> githubClient.createRepository(createParameters).await());
        }

        PostCondition.assertNotNull(repository, "repository");

        return repository;
    }

    static void retry(Clock clock, Action0 action)
    {
        PreCondition.assertNotNull(clock, "clock");
        PreCondition.assertNotNull(action, "action");

        GitHubClientTests.retry(clock, () ->
        {
            action.run();
            return null;
        });
    }

    static <T> T retry(Clock clock, Function0<T> function)
    {
        PreCondition.assertNotNull(clock, "clock");
        PreCondition.assertNotNull(function, "function");

        return Retry.create()
            .setShouldRetryFunction(3, () -> clock.delay(Duration.seconds(1)).await())
            .run(function)
            .await();
    }

    static void assertResponse(Test test, GitHubResponse response, int expectedStatusCode, Action0 expectedAssertions)
    {
        PreCondition.assertNotNull(expectedAssertions, "expectedAssertions");

        test.assertNotNull(response);

        final int statusCode = response.getStatusCode();
        if (statusCode == expectedStatusCode)
        {
            try
            {
                expectedAssertions.run();
            }
            catch (TestError e)
            {
                if (GitHubClientTests.isRateLimitExceeded(response))
                {
                    GitHubClientTests.assertRateLimitExceeded(test, response);
                }
                else
                {
                    throw e;
                }
            }
        }
        else if (statusCode == 403)
        {
            GitHubClientTests.assertRateLimitExceeded(test, response);
        }
        else
        {
            test.fail("Unexpected status code: " + statusCode);
        }
    }

    static void assertErrorResponse(Test test, GitHubResponse response, int expectedStatusCode, Action1<GitHubErrorResponse> expectedAssertions)
    {
        PreCondition.assertNotNull(test, "test");
        PreCondition.assertNotNull(expectedAssertions, "expectedAssertions");

        GitHubClientTests.assertResponse(test, response,
            expectedStatusCode, () ->
            {
                test.assertTrue(response.isErrorResponse());
                expectedAssertions.run(response.getErrorResponse().await());
            });
    }

    static void assertException(Test test, GitHubException exception, int expectedStatusCode, Action0 expectedAssertions)
    {
        PreCondition.assertNotNull(expectedAssertions, "expectedAssertions");

        test.assertNotNull(exception);

        final int statusCode = exception.getStatusCode();
        if (statusCode == expectedStatusCode)
        {
            try
            {
                expectedAssertions.run();
            }
            catch (TestError e)
            {
                if (GitHubClientTests.isRateLimitExceeded(exception))
                {
                    GitHubClientTests.assertRateLimitExceeded(test, exception);
                }
                else
                {
                    throw e;
                }
            }
        }
        else if (statusCode == 403)
        {
            GitHubClientTests.assertRateLimitExceeded(test, exception);
        }
        else
        {
            test.fail("Unexpected status code: " + statusCode);
        }
    }

    static boolean isRateLimitExceeded(GitHubException exception)
    {
        PreCondition.assertNotNull(exception, "exception");

        return GitHubClientTests.isRateLimitExceeded(exception.getStatusCode(), exception.getMessage());
    }

    static boolean isRateLimitExceeded(GitHubResponse response)
    {
        PreCondition.assertNotNull(response, "response");

        boolean result = false;
        if (response.isErrorResponse())
        {
            final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
            result = GitHubClientTests.isRateLimitExceeded(response.getStatusCode(), errorResponse.getMessage());
        }
        return result;
    }

    static boolean isRateLimitExceeded(int statusCode, String message)
    {
        PreCondition.assertNotNullAndNotEmpty(message, "message");

        return statusCode == 403 &&
            Strings.startsWith(message, "API rate limit exceeded for ") &&
            Strings.endsWith(message, ". (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)");
    }

    static void assertRateLimitExceeded(Test test, GitHubException exception)
    {
        PreCondition.assertNotNull(test, "test");
        PreCondition.assertNotNull(exception, "exception");
        PreCondition.assertEqual(403, exception.getStatusCode(), "exception.getStatusCode()");

        final String message = exception.getMessage();
        test.assertStartsWith(message, "API rate limit exceeded for ");
        test.assertEndsWith(message, ". (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)");
        test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", exception.getDocumentationUrl());
        test.assertEqual(Iterable.create(), exception.getErrors());
    }

    static void assertRateLimitExceeded(Test test, GitHubResponse response)
    {
        PreCondition.assertNotNull(test, "test");
        PreCondition.assertNotNull(response, "response");
        PreCondition.assertEqual(403, response.getStatusCode(), "response.getStatusCode()");
        PreCondition.assertTrue(response.isErrorResponse(), "response.isErrorResponse()");

        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
        final String message = errorResponse.getMessage();
        test.assertStartsWith(message, "API rate limit exceeded for ");
        test.assertEndsWith(message, ". (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)");
        test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", errorResponse.getDocumentationUrl());
        test.assertEqual(Iterable.create(), errorResponse.getErrors());
    }
}
