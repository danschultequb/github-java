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

            runner.testGroup("sendGetAuthenticatedUserRequest()", () ->
            {
                runner.test("when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    try (final GetAuthenticatedUserResponse response = gitHubClient.sendGetAuthenticatedUserRequest().await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(401, response.getStatusCode());
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        test.assertNotNull(errorResponse);
                        test.assertEqual("Requires authentication", errorResponse.getMessage());
                        test.assertEqual("https://docs.github.com/rest/reference/users#get-the-authenticated-user", errorResponse.getDocumentationUrl());
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
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
                    test.assertEqual(401, exception.getStatusCode());
                    test.assertEqual("Requires authentication", exception.getMessage());
                    test.assertEqual("https://docs.github.com/rest/reference/users#get-the-authenticated-user", exception.getDocumentationUrl());
                    test.assertEqual(Iterable.create(), exception.getErrors());
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

            runner.testGroup("sendGetRepositoriesForAuthenticatedUserRequest()", () ->
            {
                runner.test("when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    try (final GetRepositoriesForAuthenticatedUserResponse response = gitHubClient.sendGetRepositoriesForAuthenticatedUserRequest().await())
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
                    final int statusCode = exception.getStatusCode();
                    test.assertOneOf(Iterable.create(401, 403), statusCode);
                    switch (statusCode)
                    {
                        case 401:
                            test.assertEqual("Requires authentication", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/reference/repos#list-repositories-for-the-authenticated-user", exception.getDocumentationUrl());
                            break;

                        case 403:
                            test.assertEqual("API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", exception.getDocumentationUrl());
                            break;

                        default:
                            test.fail("Unexpected status code");
                            break;
                    }
                    test.assertEqual(Iterable.create(), exception.getErrors());
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
                        .setName("fake-repo-name");
                    try (final CreateRepositoryResponse response = gitHubClient.sendCreateRepositoryRequest(parameters).await())
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
                    String repositoryOwner = null;
                    final String repositoryName = "fake-repo-name";
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
                        repositoryOwner = owner.getLogin();
                        test.assertNotNullAndNotEmpty(repositoryOwner);
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
                        if (!Strings.isNullOrEmpty(repositoryOwner))
                        {
                            gitHubClient.sendDeleteRepositoryRequest(DeleteRepositoryParameters.create()
                                .setOwner(repositoryOwner)
                                .setName(repositoryName))
                                .await()
                                .dispose().await();
                        }
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
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.createRepository(parameters).await(), GitHubException.class);
                    final int statusCode = exception.getStatusCode();
                    test.assertOneOf(Iterable.create(401, 403), statusCode);
                    switch (statusCode)
                    {
                        case 401:
                            test.assertEqual("Requires authentication", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/reference/repos#create-a-repository-for-the-authenticated-user", exception.getDocumentationUrl());
                            test.assertEqual(Iterable.create(), exception.getErrors());
                            break;

                        case 403:
                            test.assertEqual("API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", exception.getDocumentationUrl());
                            break;

                        default:
                            test.fail("Unexpected status code");
                            break;
                    }
                    test.assertEqual(Iterable.create(), exception.getErrors());
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
                        .setName("fake-repo-name");
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
                    String repositoryOwner = null;
                    final String repositoryName = "fake-repo-name";
                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
                        .setName(repositoryName);
                    final GitHubRepository repository = gitHubClient.createRepository(parameters).await();
                    try
                    {
                        test.assertEqual(repositoryName, repository.getName());
                        final GitHubUser owner = repository.getOwner();
                        test.assertNotNull(owner);
                        repositoryOwner = owner.getLogin();
                        test.assertNotNullAndNotEmpty(repositoryOwner);
                        test.assertEqual(repositoryOwner + "/" + repositoryName, repository.getFullName());

                        final Iterable<GitHubRepository> authenticatedUserRepositories = gitHubClient.getRepositoriesForAuthenticatedUser().await();
                        test.assertTrue(authenticatedUserRepositories.contains((GitHubRepository existingRepository) ->
                        {
                            return Strings.equal(existingRepository.getFullName(), repository.getFullName());
                        }));
                    }
                    finally
                    {
                        gitHubClient.deleteRepository(repository).await();
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
                        .setName("fake-repo-name");
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(parameters).await())
                    {
                        final int statusCode = response.getStatusCode();
                        test.assertOneOf(Iterable.create(403, 404), statusCode);
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        switch (statusCode)
                        {
                            case 403:
                                test.assertEqual("API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", errorResponse.getDocumentationUrl());
                                break;

                            case 404:
                                test.assertEqual("Not Found", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", errorResponse.getDocumentationUrl());
                                break;

                            default:
                                test.fail("Unexpected status code");
                                break;
                        }
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
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
                        test.assertEqual(403, response.getStatusCode());
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        test.assertOneOf(
                            Iterable.create(
                                "Must have admin rights to Repository.",
                                "API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)"),
                            errorResponse.getMessage());
                        test.assertOneOf(
                            Iterable.create(
                                "https://docs.github.com/rest/reference/repos#delete-a-repository",
                                "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"),
                            errorResponse.getDocumentationUrl());
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner("fake-owner")
                        .setName("fake-repo-name");
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
                        .setName("fake-repo-name");
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(parameters).await())
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
                    final GitHubRepository repository = gitHubClient.createRepository(CreateRepositoryParameters.create()
                        .setName("fake-repo-name"))
                        .await();

                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner(repository.getOwner().getLogin())
                        .setName(repository.getName());
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(parameters).await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(204, response.getStatusCode());
                        test.assertFalse(response.isErrorResponse());
                    }
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
                        .setName("fake-repo-name");
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(repository).await())
                    {
                        final int statusCode = response.getStatusCode();
                        test.assertOneOf(Iterable.create(403, 404), statusCode);
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        switch (statusCode)
                        {
                            case 403:
                                test.assertEqual("API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", errorResponse.getDocumentationUrl());
                                break;

                            case 404:
                                test.assertEqual("Not Found", errorResponse.getMessage());
                                test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", errorResponse.getDocumentationUrl());
                                break;

                            default:
                                test.fail("Unexpected status code");
                                break;
                        }
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("with existing repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GitHubRepository repository = GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin("fake-owner"))
                        .setName("fake-repo-name");
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(repository).await())
                    {
                        final int statusCode = response.getStatusCode();
                        test.assertOneOf(Iterable.create(403, 404), statusCode);
                        test.assertTrue(response.isErrorResponse());
                        final GitHubErrorResponse errorResponse = response.getErrorResponse().await();
                        switch (statusCode)
                        {
                            case 403:
                                test.assertEqual(
                                    "API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)",
                                    errorResponse.getMessage());
                                test.assertEqual(
                                    "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting",
                                    errorResponse.getDocumentationUrl());
                                break;

                            case 404:
                                test.assertEqual("c", errorResponse.getMessage());
                                test.assertEqual("d", errorResponse.getDocumentationUrl());
                                break;

                            default:
                                test.fail();
                                break;
                        }
                        test.assertEqual(Iterable.create(), errorResponse.getErrors());
                    }
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final GitHubRepository repository = GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin("fake-owner"))
                        .setName("fake-repo-name");
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
                        .setName("fake-repo-name");
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
                    final GitHubRepository repository = gitHubClient.createRepository(CreateRepositoryParameters.create()
                        .setName("fake-repo-name"))
                        .await();
                    try (final GitHubResponse response = gitHubClient.sendDeleteRepositoryRequest(repository).await())
                    {
                        test.assertNotNull(response);
                        test.assertEqual(204, response.getStatusCode());
                        test.assertFalse(response.isErrorResponse());
                    }
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
                        .setName("fake-repo-name");
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(parameters).await(), GitHubException.class);
                    final int statusCode = exception.getStatusCode();
                    test.assertOneOf(Iterable.create(403, 404), statusCode);
                    switch (statusCode)
                    {
                        case 403:
                            test.assertEqual("API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", exception.getDocumentationUrl());
                            break;

                        case 404:
                            test.assertEqual("Not Found", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", exception.getDocumentationUrl());
                            break;

                        default:
                            test.fail("Unexpected status code");
                            break;
                    }
                    test.assertEqual(Iterable.create(), exception.getErrors());
                });

                runner.test("with existing repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner("danschultequb")
                        .setName("github-java");
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(parameters).await(), GitHubException.class);
                    {
                        test.assertEqual(403, exception.getStatusCode());
                        test.assertOneOf(
                            Iterable.create(
                                "Must have admin rights to Repository.",
                                "API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)"),
                            exception.getMessage());
                        test.assertOneOf(
                            Iterable.create(
                                "https://docs.github.com/rest/reference/repos#delete-a-repository",
                                "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"),
                            exception.getDocumentationUrl());
                        test.assertEqual(Iterable.create(), exception.getErrors());
                    }
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner("fake-owner")
                        .setName("fake-repo-name");
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
                        .setName("fake-repo-name");
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(parameters).await(), GitHubException.class);
                    test.assertEqual(404, exception.getStatusCode());
                    test.assertEqual("Not Found", exception.getMessage());
                    test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", exception.getDocumentationUrl());
                    test.assertEqual(Iterable.create(), exception.getErrors());
                });

                runner.test("with existing repository when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final GitHubRepository repository = gitHubClient.createRepository(CreateRepositoryParameters.create()
                        .setName("fake-repo-name"))
                        .await();

                    final DeleteRepositoryParameters parameters = DeleteRepositoryParameters.create()
                        .setOwner(repository.getOwner().getLogin())
                        .setName(repository.getName());
                    gitHubClient.deleteRepository(parameters).await();
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
                        .setName("fake-repo-name");
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(repository).await(), GitHubException.class);
                    final int statusCode = exception.getStatusCode();
                    test.assertOneOf(Iterable.create(403, 404), statusCode);
                    switch (statusCode)
                    {
                        case 403:
                            test.assertEqual("API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", exception.getDocumentationUrl());
                            break;

                        case 404:
                            test.assertEqual("Not Found", exception.getMessage());
                            test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", exception.getDocumentationUrl());
                            break;

                        default:
                            test.fail("Unexpected status code");
                            break;
                    }
                    test.assertEqual(Iterable.create(), exception.getErrors());
                });

                runner.test("with existing repository when not authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.None);
                    final GitHubRepository repository = GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin("fake-owner"))
                        .setName("fake-repo-name");
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(repository).await(), GitHubException.class);
                    {
                        test.assertEqual(403, exception.getStatusCode());
                        test.assertOneOf(
                            Iterable.create(
                                "Must have admin rights to Repository.",
                                "API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)"),
                            exception.getMessage());
                        test.assertOneOf(
                            Iterable.create(
                                "https://docs.github.com/rest/reference/repos#delete-a-repository",
                                "https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"),
                            exception.getDocumentationUrl());
                        test.assertEqual(Iterable.create(), exception.getErrors());
                    }
                });

                runner.test("with invalid personal access token", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Invalid);
                    final GitHubRepository repository = GitHubRepository.create()
                        .setOwner(GitHubUser.create()
                            .setLogin("fake-owner"))
                        .setName("fake-repo-name");
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
                        .setName("fake-repo-name");
                    final GitHubException exception = test.assertThrows(() -> gitHubClient.deleteRepository(repository).await(), GitHubException.class);
                    test.assertEqual(404, exception.getStatusCode());
                    test.assertEqual("Not Found", exception.getMessage());
                    test.assertEqual("https://docs.github.com/rest/reference/repos#delete-a-repository", exception.getDocumentationUrl());
                    test.assertEqual(Iterable.create(), exception.getErrors());
                });

                runner.test("with existing repository when authenticated", (Test test) ->
                {
                    final GitHubClient gitHubClient = creator.run(AccessTokenType.Valid);
                    final GitHubRepository repository = gitHubClient.createRepository(CreateRepositoryParameters.create()
                        .setName("fake-repo-name"))
                        .await();

                    gitHubClient.deleteRepository(repository).await();
                });
            });
        });
    }
}
