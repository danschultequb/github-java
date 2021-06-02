package qub;

public interface GitHubClientTests
{
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

        runner.testGroup(GitHubClient.class, () ->
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

            runner.testGroup("getAuthenticatedUser()", () ->
            {
                runner.test("when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GetAuthenticatedUserResponse response = gitHubClient.getAuthenticatedUser().await();
                    test.assertNotNull(response);
                    test.assertEqual(401, response.getStatusCode());
                    test.assertTrue(response.isErrorResponse());
                    final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                    test.assertNotNull(errorResponse);
                    test.assertEqual("Requires authentication", errorResponse.getMessage());
                    test.assertEqual("https://docs.github.com/rest/reference/users#get-the-authenticated-user", errorResponse.getDocumentationUrl());
                    test.assertEqual(Iterable.create(), errorResponse.getErrors());
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final GetAuthenticatedUserResponse response = gitHubClient.getAuthenticatedUser().await();
                    test.assertNotNull(response);
                    test.assertEqual(401, response.getStatusCode());
                    test.assertTrue(response.isErrorResponse());
                    final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                    test.assertEqual("Bad credentials", errorResponse.getMessage());
                    test.assertEqual("https://docs.github.com/rest", errorResponse.getDocumentationUrl());
                    test.assertEqual(Iterable.create(), errorResponse.getErrors());
                });

                runner.test("when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final GetAuthenticatedUserResponse response = gitHubClient.getAuthenticatedUser().await();
                    test.assertNotNull(response);
                    test.assertEqual(200, response.getStatusCode());
                    test.assertFalse(response.isErrorResponse());
                    final GitHubUser user = response.getAuthenticatedUser().await();
                    test.assertNotNull(user);
                    test.assertNotNullAndNotEmpty(user.getLogin());
                });
            });

            runner.testGroup("getRepository(GetRepositoryParameters)", () ->
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
                        .setName("fake-repo");
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

                runner.test("with existing public repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
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

                runner.test("with non-existing repository and invalid token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final GetRepositoryParameters parameters = GetRepositoryParameters.create()
                        .setOwner("fake-owner")
                        .setName("fake-repo");
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
                        .setName("fake-repo");
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

            runner.testGroup("getRepositoriesForAuthenticatedUser()", () ->
            {
                runner.test("when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    try (final GetRepositoriesForAuthenticatedUserResponse response = gitHubClient.getRepositoriesForAuthenticatedUser().await())
                    {
                        test.assertNotNull(response);
                        final int statusCode = response.getStatusCode();
                        test.assertOneOf(Iterable.create(401, 403), statusCode);
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        switch (statusCode)
                        {
                            case 401:
                                test.assertEqual("Requires authentication", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/repos#list-repositories-for-the-authenticated-user", errorResponse.getDocumentationUrl());
                                test.assertEqual(Iterable.create(), errorResponse.getErrors());
                                break;

                            case 403:
                                test.assertEqual("API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", errorResponse.getDocumentationUrl());
                                break;

                            default:
                                test.fail("Unexpected status code");
                                break;
                        }
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    try (final GetRepositoriesForAuthenticatedUserResponse response = gitHubClient.getRepositoriesForAuthenticatedUser().await())
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
                    try (final GetRepositoriesForAuthenticatedUserResponse response = gitHubClient.getRepositoriesForAuthenticatedUser().await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(200, response.getStatusCode());
                        test.assertFalse(response.isErrorResponse());
                        final Iterable<GitHubRepository> repositories = response.getRepositories().await();
                        test.assertNotNull(repositories);
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
                        .setName("fake-repo-name");
                    try (final CreateRepositoryResponse response = gitHubClient.createRepository(parameters).await())
                    {
                        test.assertNotNull(response);
                        final int statusCode = response.getStatusCode();
                        test.assertOneOf(Iterable.create(401, 403), statusCode);
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        switch (statusCode)
                        {
                            case 401:
                                test.assertEqual("Requires authentication", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/repos#create-a-repository-for-the-authenticated-user", errorResponse.getDocumentationUrl());
                                test.assertEqual(Iterable.create(), errorResponse.getErrors());
                                break;

                            case 403:
                                test.assertEqual("API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", errorResponse.getDocumentationUrl());
                                break;

                            default:
                                test.fail("Unexpected status code");
                                break;
                        }
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
                        .setName("fake-repo-name");
                    try (final CreateRepositoryResponse response = gitHubClient.createRepository(parameters).await())
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
                    try (final CreateRepositoryResponse response = gitHubClient.createRepository(parameters).await())
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
                    String repositoryOwner = null;
                    final String repositoryName = "fake-repo-name";
                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
                        .setName(repositoryName);
                    try (final CreateRepositoryResponse response = gitHubClient.createRepository(parameters).await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(201, response.getStatusCode());
                        test.assertFalse(response.isErrorResponse());
                        final GitHubRepository repository = response.getRepository().await();
                        test.assertEqual(repositoryName, repository.getName());
                        final GitHubUser owner = repository.getOwner();
                        test.assertNotNull(owner);
                        repositoryOwner = owner.getLogin();
                        test.assertNotNullAndNotEmpty(repositoryOwner);
                        test.assertEqual(repositoryOwner + "/" + repositoryName, repository.getFullName());

                        try (final GetRepositoriesForAuthenticatedUserResponse repositoriesResponse = gitHubClient.getRepositoriesForAuthenticatedUser().await())
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
                        if (!Strings.isNullOrEmpty(repositoryOwner))
                        {
                            gitHubClient.deleteRepository(DeleteRepositoryParameters.create()
                                .setOwner(repositoryOwner)
                                .setName(repositoryName))
                                .await()
                                .dispose().await();
                        }
                    }
                });
            });

//            runner.testGroup("deleteRepository(DeleteRepositoryParameters)", () ->
//            {
//                final Action2<DeleteRepositoryParameters,Throwable> deleteRepositoryErrorTest = (DeleteRepositoryParameters parameters, Throwable expected) ->
//                {
//                    runner.test("with " + parameters, (Test test) ->
//                    {
//                        final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
//                        test.assertThrows(() -> gitHubClient.deleteRepository(parameters),
//                            expected);
//                    });
//                };
//
//                deleteRepositoryErrorTest.run(null, new PreConditionFailure("parameters cannot be null."));
//                deleteRepositoryErrorTest.run(DeleteRepositoryParameters.create(), new PreConditionFailure("parameters.getOwner() cannot be null."));
//                deleteRepositoryErrorTest.run(DeleteRepositoryParameters.create().setOwner("fake-owner"), new PreConditionFailure("parameters.getName() cannot be null."));
//                deleteRepositoryErrorTest.run(DeleteRepositoryParameters.create().setName("fake-name"), new PreConditionFailure("parameters.getOwner() cannot be null."));
//
//                runner.test("with non-existing repository when not authenticated", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
//                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
//                        .setOwner("fake-owner")
//                        .setName("fake-repo-name");
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.deleteRepository(parameters).await());
//
//                    final int statusCode = error.getStatusCode();
//                    test.assertOneOf(Iterable.create(403, 404), statusCode);
//                    switch (statusCode)
//                    {
//                        case 403:
//                            test.assertEqual("API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)", error.getMessage());
//                            test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", error.getDocumentationUrl());
//                            break;
//
//                        case 404:
//                            test.assertEqual("Not Found", error.getMessage());
//                            test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", error.getDocumentationUrl());
//                            break;
//
//                        default:
//                            test.fail("Unexpected status code");
//                            break;
//                    }
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("with existing repository when not authenticated", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
//                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
//                        .setOwner("danschultequb")
//                        .setName("github-java");
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.deleteRepository(parameters).await());
//
//                    test.assertEqual(403, error.getStatusCode());
//                    test.assertOneOf(
//                        Iterable.create(
//                            "Must have admin rights to Repository.",
//                            "API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)"),
//                        error.getMessage());
//                    test.assertOneOf(
//                        Iterable.create(
//                            "https://docs.github.com/rest/reference/repos#delete-a-repository",
//                            "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"),
//                        error.getDocumentationUrl());
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("with invalid personal access token", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
//                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
//                        .setOwner("fake-owner")
//                        .setName("fake-repo-name");
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.deleteRepository(parameters).await());
//                    test.assertEqual(401, error.getStatusCode());
//                    test.assertEqual("Bad credentials", error.getMessage());
//                    test.assertEqual("https://docs.github.com/rest", error.getDocumentationUrl());
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("with non-existing repository when authenticated", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
//                    final String authenticatedUserLogin = gitHubClient.getAuthenticatedUser().await().getLogin();
//                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
//                        .setOwner(authenticatedUserLogin)
//                        .setName("fake-repo-name");
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.deleteRepository(parameters).await());
//                    test.assertEqual(404, error.getStatusCode());
//                    test.assertEqual("Not Found", error.getMessage());
//                    test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", error.getDocumentationUrl());
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("with existing repository when authenticated", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
//                    final GitHubRepository repository = gitHubClient.createRepository(CreateRepositoryParameters.create()
//                        .setName("fake-repo-name"))
//                        .await();
//
//                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
//                        .setOwner(repository.getOwner().getLogin())
//                        .setName(repository.getName());
//                    final Void result = gitHubClient.deleteRepository(parameters).await();
//                    test.assertNull(result);
//                });
//            });
        });
    }
}
