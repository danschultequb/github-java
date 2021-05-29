//package qub;
//
//public interface GitHubClientTests
//{
//    static void test(TestRunner runner)
//    {
//        runner.testGroup(GitHubClient.class, () ->
//        {
//            runner.testGroup("create(Network)", () ->
//            {
//                runner.test("with null", (Test test) ->
//                {
//                    final Network network = null;
//                    test.assertThrows(() -> GitHubClient.create(network),
//                        new PreConditionFailure("network cannot be null."));
//                });
//
//                runner.test("with non-null",
//                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
//                    (Test test, FakeDesktopProcess process) ->
//                {
//                    final Network network = process.getNetwork();
//                    final GitHubClient gitHubClient = GitHubClient.create(network);
//                    test.assertNotNull(gitHubClient);
//                });
//            });
//
//            runner.testGroup("create(HttpClient)", () ->
//            {
//                runner.test("with null", (Test test) ->
//                {
//                    final HttpClient httpClient = null;
//                    test.assertThrows(() -> GitHubClient.create(httpClient),
//                        new PreConditionFailure("httpClient cannot be null."));
//                });
//
//                runner.test("with non-null",
//                    (TestResources resources) -> Tuple.create(resources.createFakeDesktopProcess()),
//                    (Test test, FakeDesktopProcess process) ->
//                {
//                    final HttpClient httpClient = HttpClient.create(process.getNetwork());
//                    final GitHubClient gitHubClient = GitHubClient.create(httpClient);
//                    test.assertNotNull(gitHubClient);
//                });
//            });
//        });
//    }
//
//    static void test(TestRunner runner, Function1<PersonalAccessTokenType,? extends GitHubClient> creator)
//    {
//        PreCondition.assertNotNull(runner, "runner");
//        PreCondition.assertNotNull(creator, "creator");
//
//        runner.testGroup(GitHubClient.class, () ->
//        {
//
//            runner.testGroup("setPersonalAccessToken(String)", () ->
//            {
//                final Action2<String,Throwable> setPersonalAccessTokenErrorTest = (String personalAccessToken, Throwable expected) ->
//                {
//                    runner.test("with " + Strings.escapeAndQuote(personalAccessToken), (Test test) ->
//                    {
//                        final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.None);
//                        test.assertFalse(gitHubClient.hasPersonalAccessToken());
//
//                        test.assertThrows(() -> gitHubClient.setPersonalAccessToken(personalAccessToken),
//                            expected);
//                        test.assertFalse(gitHubClient.hasPersonalAccessToken());
//                    });
//                };
//
//                setPersonalAccessTokenErrorTest.run(null, new PreConditionFailure("personalAccessToken cannot be null."));
//                setPersonalAccessTokenErrorTest.run("", new PreConditionFailure("personalAccessToken cannot be empty."));
//
//                final Action1<String> setPersonalAccessTokenTest = (String personalAccessToken) ->
//                {
//                    runner.test("with " + Strings.escapeAndQuote(personalAccessToken), (Test test) ->
//                    {
//                        final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.None);
//                        test.assertFalse(gitHubClient.hasPersonalAccessToken());
//
//                        final GitHubClient setPersonalAccessTokenResult = gitHubClient.setPersonalAccessToken(personalAccessToken);
//                        test.assertSame(gitHubClient, setPersonalAccessTokenResult);
//                        test.assertTrue(gitHubClient.hasPersonalAccessToken());
//                    });
//                };
//
//                setPersonalAccessTokenTest.run("fakeGitHubToken");
//            });
//
//            runner.testGroup("getAuthenticatedUser()", () ->
//            {
//                runner.test("when not authenticated", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.None);
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.getAuthenticatedUser().await());
//                    test.assertEqual(401, error.getStatusCode());
//                    test.assertEqual("Requires authentication", error.getMessage());
//                    test.assertEqual("https://docs.github.com/rest/reference/users#get-the-authenticated-user", error.getDocumentationUrl());
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("with invalid personal access token", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Invalid);
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.getAuthenticatedUser().await());
//                    test.assertEqual(401, error.getStatusCode());
//                    test.assertEqual("Bad credentials", error.getMessage());
//                    test.assertEqual("https://docs.github.com/rest", error.getDocumentationUrl());
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("when authenticated", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Valid);
//                    final GitHubUser user = gitHubClient.getAuthenticatedUser().await();
//                    test.assertNotNull(user);
//                    test.assertNotNullAndNotEmpty(user.getLogin());
//                });
//            });
//
//            runner.testGroup("getRepository(GetRepositoryParameters)", () ->
//            {
//                final Action2<GetRepositoryParameters,Throwable> getRepositoryErrorTest = (GetRepositoryParameters parameters, Throwable expected) ->
//                {
//                    runner.test("with " + parameters, (Test test) ->
//                    {
//                        final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.None);
//                        test.assertThrows(() -> gitHubClient.getRepository(parameters).await(), expected);
//                    });
//                };
//
//                getRepositoryErrorTest.run(null, new PreConditionFailure("parameters cannot be null."));
//                getRepositoryErrorTest.run(GetRepositoryParameters.create(), new PreConditionFailure("parameters.getOwner() cannot be null."));
//                getRepositoryErrorTest.run(GetRepositoryParameters.create().setOwner("fake-owner"), new PreConditionFailure("parameters.getName() cannot be null."));
//                getRepositoryErrorTest.run(GetRepositoryParameters.create().setName("fake-name"), new PreConditionFailure("parameters.getOwner() cannot be null."));
//
//                runner.test("with non-existing repository when not authenticated", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.None);
//                    final GetRepositoryParameters parameters = GetRepositoryParameters.create()
//                        .setOwner("fake-owner")
//                        .setName("fake-repo");
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.getRepository(parameters).await());
//                    test.assertEqual(404, error.getStatusCode());
//                    test.assertEqual("Not Found", error.getMessage());
//                    test.assertEqual("https://docs.github.com/rest/reference/repos#get-a-repository", error.getDocumentationUrl());
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("with existing public repository when not authenticated", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.None);
//                    final GetRepositoryParameters parameters = GetRepositoryParameters.create()
//                        .setOwner("octokit")
//                        .setName("octokit.net");
//                    try
//                    {
//                        final GitHubRepository repository = gitHubClient.getRepository(parameters).await();
//                        test.assertEqual("octokit", repository.getOwner().getLogin());
//                        test.assertEqual("octokit.net", repository.getName());
//                        test.assertEqual("octokit/octokit.net", repository.getFullName());
//                    }
//                    catch (GitHubException error)
//                    {
//                        test.assertEqual(403, error.getStatusCode());
//                        test.assertEqual("API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)", error.getMessage());
//                        test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", error.getDocumentationUrl());
//                        test.assertEqual(Iterable.create(), error.getErrors());
//                    }
//                });
//
//                runner.test("with non-existing repository and invalid token", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Invalid);
//                    final GetRepositoryParameters parameters = GetRepositoryParameters.create()
//                        .setOwner("fake-owner")
//                        .setName("fake-repo");
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.getRepository(parameters).await());
//                    test.assertEqual(401, error.getStatusCode());
//                    test.assertEqual("Bad credentials", error.getMessage());
//                    test.assertEqual("https://docs.github.com/rest", error.getDocumentationUrl());
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("with existing public repository and invalid token", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Invalid);
//                    final GetRepositoryParameters parameters = GetRepositoryParameters.create()
//                        .setOwner("octokit")
//                        .setName("octokit.net");
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.getRepository(parameters).await());
//                    test.assertEqual(401, error.getStatusCode());
//                    test.assertEqual("Bad credentials", error.getMessage());
//                    test.assertEqual("https://docs.github.com/rest", error.getDocumentationUrl());
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("with non-existing repository when authenticated", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Valid);
//                    final GetRepositoryParameters parameters = GetRepositoryParameters.create()
//                        .setOwner("fake-owner")
//                        .setName("fake-repo");
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.getRepository(parameters).await());
//                    test.assertEqual(404, error.getStatusCode());
//                    test.assertEqual("Not Found", error.getMessage());
//                    test.assertEqual("https://docs.github.com/rest/reference/repos#get-a-repository", error.getDocumentationUrl());
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("with existing public repository when authenticated", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Valid);
//                    final GetRepositoryParameters parameters = GetRepositoryParameters.create()
//                        .setOwner("octokit")
//                        .setName("octokit.net");
//                    final GitHubRepository repository = gitHubClient.getRepository(parameters).await();
//                    test.assertEqual("octokit", repository.getOwner().getLogin());
//                    test.assertEqual("octokit.net", repository.getName());
//                    test.assertEqual("octokit/octokit.net", repository.getFullName());
//                });
//            });
//
//            runner.testGroup("getRepositoriesForAuthenticatedUser()", () ->
//            {
//                runner.test("when not authenticated", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.None);
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.getRepositoriesForAuthenticatedUser().await());
//
//                    final int statusCode = error.getStatusCode();
//                    test.assertOneOf(Iterable.create(401, 403), statusCode);
//                    switch (statusCode)
//                    {
//                        case 401:
//                            test.assertEqual("Requires authentication", error.getMessage());
//                            test.assertEqual("https://docs.github.com/rest/reference/repos#list-repositories-for-the-authenticated-user", error.getDocumentationUrl());
//                            test.assertEqual(Iterable.create(), error.getErrors());
//                            break;
//
//                        case 403:
//                            test.assertEqual("API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)", error.getMessage());
//                            test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", error.getDocumentationUrl());
//                            break;
//
//                        default:
//                            test.fail("Unexpected status code");
//                            break;
//                    }
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("with invalid personal access token", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Invalid);
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.getRepositoriesForAuthenticatedUser().await());
//                    test.assertEqual(401, error.getStatusCode());
//                    test.assertEqual("Bad credentials", error.getMessage());
//                    test.assertEqual("https://docs.github.com/rest", error.getDocumentationUrl());
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("when authenticated", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Valid);
//                    final Iterable<GitHubRepository> repositories = gitHubClient.getRepositoriesForAuthenticatedUser().await();
//                    test.assertNotNull(repositories);
//                });
//            });
//
//            runner.testGroup("createRepository(CreateRepositoryParameters)", () ->
//            {
//                final Action2<CreateRepositoryParameters,Throwable> createRepositoryErrorTest = (CreateRepositoryParameters parameters, Throwable expected) ->
//                {
//                    runner.test("with " + parameters, (Test test) ->
//                    {
//                        final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.None);
//                        test.assertThrows(() -> gitHubClient.createRepository(parameters),
//                            expected);
//                    });
//                };
//
//                createRepositoryErrorTest.run(null, new PreConditionFailure("parameters cannot be null."));
//                createRepositoryErrorTest.run(CreateRepositoryParameters.create(), new PreConditionFailure("parameters.getName() cannot be null."));
//
//                runner.test("when not authenticated", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.None);
//                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
//                        .setName("fake-repo-name");
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.createRepository(parameters).await());
//
//                    final int statusCode = error.getStatusCode();
//                    test.assertOneOf(Iterable.create(401, 403), statusCode);
//                    switch (statusCode)
//                    {
//                        case 401:
//                            test.assertEqual("Requires authentication", error.getMessage());
//                            test.assertEqual("https://docs.github.com/rest/reference/repos#create-a-repository-for-the-authenticated-user", error.getDocumentationUrl());
//                            test.assertEqual(Iterable.create(), error.getErrors());
//                            break;
//
//                        case 403:
//                            test.assertEqual("API rate limit exceeded for 73.181.147.2. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)", error.getMessage());
//                            test.assertEqual("https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting", error.getDocumentationUrl());
//                            break;
//
//                        default:
//                            test.fail("Unexpected status code");
//                            break;
//                    }
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("with invalid personal access token", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Invalid);
//                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
//                        .setName("fake-repo-name");
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.createRepository(parameters).await());
//                    test.assertEqual(401, error.getStatusCode());
//                    test.assertEqual("Bad credentials", error.getMessage());
//                    test.assertEqual("https://docs.github.com/rest", error.getDocumentationUrl());
//                    test.assertEqual(Iterable.create(), error.getErrors());
//                });
//
//                runner.test("with repository that already exists", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Valid);
//                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
//                        .setName("github-java");
//                    final GitHubException error = test.assertThrows(GitHubException.class, () -> gitHubClient.createRepository(parameters).await());
//                    test.assertEqual(422, error.getStatusCode());
//                    test.assertEqual("Repository creation failed.", error.getMessage());
//                    test.assertEqual("https://docs.github.com/rest/reference/repos#create-a-repository-for-the-authenticated-user", error.getDocumentationUrl());
//                    test.assertEqual(
//                        Iterable.create(
//                            GitHubError.create()
//                                .setResource("Repository")
//                                .setCode("custom")
//                                .setField("name")
//                                .setMessage("name already exists on this account")
//                        ),
//                        error.getErrors());
//                });
//
//                runner.test("with repository name that doesn't exist", (Test test) ->
//                {
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Valid);
//                    final CreateRepositoryParameters parameters = CreateRepositoryParameters.create()
//                        .setName("fake-repo-name");
//                    final GitHubRepository repository = gitHubClient.createRepository(parameters).await();
//                    try
//                    {
//                        test.assertNotNull(repository);
//                        test.assertEqual("fake-repo-name", repository.getName());
//                        final GitHubUser owner = repository.getOwner();
//                        test.assertNotNull(owner);
//                        test.assertNotNullAndNotEmpty(owner.getLogin());
//                        test.assertEqual(owner.getLogin() + "/fake-repo-name", repository.getFullName());
//
//                        final Iterable<GitHubRepository> authenticatedUserRepositories = gitHubClient.getRepositoriesForAuthenticatedUser().await();
//                        test.assertTrue(authenticatedUserRepositories.contains((GitHubRepository existingRepository) -> Strings.equal(existingRepository.getFullName(), repository.getFullName())));
//                    }
//                    finally
//                    {
//                        gitHubClient.deleteRepository(DeleteRepositoryParameters.create()
//                            .setOwner(repository.getOwner().getLogin())
//                            .setName(repository.getName()))
//                            .await();
//                    }
//                });
//            });
//
//            runner.testGroup("deleteRepository(DeleteRepositoryParameters)", () ->
//            {
//                final Action2<DeleteRepositoryParameters,Throwable> deleteRepositoryErrorTest = (DeleteRepositoryParameters parameters, Throwable expected) ->
//                {
//                    runner.test("with " + parameters, (Test test) ->
//                    {
//                        final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.None);
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
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.None);
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
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.None);
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
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Invalid);
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
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Valid);
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
//                    final GitHubClient gitHubClient = creator.run(PersonalAccessTokenType.Valid);
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
//        });
//    }
//}
