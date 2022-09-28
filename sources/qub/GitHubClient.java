package qub;

/**
 * A client interface for interacting with a GitHub endpoint.
 */
public interface GitHubClient
{
    public static BasicGitHubClient create(Network network)
    {
        return BasicGitHubClient.create(network);
    }

    public static BasicGitHubClient create(HttpClient httpClient)
    {
        return BasicGitHubClient.create(httpClient);
    }

    /**
     * Get the base {@link URL} that this {@link GitHubClient} will target.
     */
    public URL getBaseUrl();

    /**
     * Set the base {@link URL} {@link String} that this {@link GitHubClient} will target.
     * @param baseUrl The base {@link URL} {@link String} that this {@link GitHubClient} will
     *                target.
     * @return This object for method chaining.
     */
    public default Result<GitHubClient> setBaseUrl(String baseUrl)
    {
        PreCondition.assertNotNullAndNotEmpty(baseUrl, "baseUrl");

        return Result.create(() ->
        {
            return this.setBaseUrl(URL.parse(baseUrl).await());
        });
    }

    /**
     * Set the base {@link URL} that this {@link GitHubClient} will target.
     * @param baseUrl The base {@link URL} that this {@link GitHubClient} will target.
     * @return This object for method chaining.
     */
    public GitHubClient setBaseUrl(URL baseUrl);

    /**
     * Set the access token {@link String} that will be used to authenticate this
     * {@link GitHubClient}'s requests.
     * @param accessToken The access token {@link String} that will be used to authenticate this
     *                    {@link GitHubClient}'s requests.
     * @return This object for method chaining.
     */
    public GitHubClient setAccessToken(String accessToken);

    /**
     * Get whether this {@link GitHubClient} has an access token.
     */
    public boolean hasAccessToken();

    /**
     * Send a generic {@link GitHubRequest}. If this {@link GitHubClient} is authenticated, then the
     * personal access token will be added to the request before it is sent.
     * @param request The {@link GitHubRequest} to send.
     * @return The {@link GitHubResponse} for the {@link GitHubRequest}.
     */
    public Result<GitHubResponse> sendRequest(GitHubRequest request);

    /**
     * Get the details about the {@link GitHubUser} that this {@link GitHubClient} is authenticated
     * to.
     */
    public default Result<GetAuthenticatedUserResponse> sendGetAuthenticatedUserRequest()
    {
        return Result.create(() ->
        {
            final GitHubRequest gitHubRequest = GitHubRequest.create()
                .setHttpMethod(HttpMethod.GET)
                .setUrlPath("/user");
            final GitHubResponse gitHubResponse = this.sendRequest(gitHubRequest).await();
            final GetAuthenticatedUserResponse result = GetAuthenticatedUserResponse.create(gitHubResponse);

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }

    /**
     * Get the details about the {@link GitHubUser} that this {@link GitHubClient} is authenticated
     * to.
     */
    public default Result<GitHubUser> getAuthenticatedUser()
    {
        return Result.create(() ->
        {
            GitHubUser result;
            try (final GetAuthenticatedUserResponse response = this.sendGetAuthenticatedUserRequest().await())
            {
                result = response.getAuthenticatedUser().await();
            }
            return result;
        });
    }

    /**
     * Get the {@link GitHubRepository} that matches the provided {@link GetRepositoryParameters}.
     * @param parameters The {@link GetRepositoryParameters} that describe the
     * {@link GitHubRepository} to return.
     */
    public default Result<GetRepositoryResponse> getRepository(GetRepositoryParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNullAndNotEmpty(parameters.getOwner(), "parameters.getOwner()");
        PreCondition.assertNotNullAndNotEmpty(parameters.getName(), "parameters.getName()");

        return Result.create(() ->
        {
            final GitHubRequest gitHubRequest = GitHubRequest.create()
                .setHttpMethod(HttpMethod.GET)
                .setUrlPath("/repos/" + parameters.getOwner() + "/" + parameters.getName());
            final GitHubResponse gitHubResponse = this.sendRequest(gitHubRequest).await();
            final GetRepositoryResponse result = GetRepositoryResponse.create(gitHubResponse);

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }

    /**
     * Get the {@link GitHubRepository}s the authenticated {@link GitHubUser} has permission to
     * access.
     */
    public default Result<GetRepositoriesForAuthenticatedUserResponse> sendGetRepositoriesForAuthenticatedUserRequest()
    {
        return Result.create(() ->
        {
            final GitHubRequest gitHubRequest = GitHubRequest.create()
                .setHttpMethod(HttpMethod.GET)
                .setUrlPath("/user/repos");
            final GitHubResponse gitHubResponse = this.sendRequest(gitHubRequest).await();
            final GetRepositoriesForAuthenticatedUserResponse result = GetRepositoriesForAuthenticatedUserResponse.create(gitHubResponse);

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }

    /**
     * Get the {@link GitHubRepository}s the authenticated {@link GitHubUser} has permission to
     * access.
     */
    public default Result<Iterable<GitHubRepository>> getRepositoriesForAuthenticatedUser()
    {
        return Result.create(() ->
        {
            Iterable<GitHubRepository> result;
            try (final GetRepositoriesForAuthenticatedUserResponse response = this.sendGetRepositoriesForAuthenticatedUserRequest().await())
            {
                result = response.getRepositories().await();
            }
            return result;
        });
    }

    /**
     * Create a new {@link GitHubRepository}.
     * @param parameters The {@link CreateRepositoryParameters} for the {@link GitHubRequest}.
     * @return The newly created {@link GitHubRepository}.
     */
    public default Result<CreateRepositoryResponse> sendCreateRepositoryRequest(CreateRepositoryParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNullAndNotEmpty(parameters.getName(), "parameters.getName()");

        return Result.create(() ->
        {
            final GitHubRequest gitHubRequest = GitHubRequest.create()
                .setHttpMethod(HttpMethod.POST)
                .setUrlPath("/user/repos")
                .setBody(parameters.toJson()).await();
            final GitHubResponse gitHubResponse = this.sendRequest(gitHubRequest).await();
            final CreateRepositoryResponse result = CreateRepositoryResponse.create(gitHubResponse);

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }

    /**
     * Create a new {@link GitHubRepository}.
     * @param parameters The {@link CreateRepositoryParameters} for the {@link GitHubRequest}.
     * @return The newly created {@link GitHubRepository}.
     */
    public default Result<GitHubRepository> createRepository(CreateRepositoryParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNullAndNotEmpty(parameters.getName(), "parameters.getName()");

        return Result.create(() ->
        {
            final GitHubRepository result;
            try (final CreateRepositoryResponse response = this.sendCreateRepositoryRequest(parameters).await())
            {
                result = response.getRepository().await();
            }
            return result;
        });
    }

    /**
     * Delete an existing {@link GitHubRepository}.
     * @param parameters The {@link DeleteRepositoryParameters} for the {@link GitHubRequest}.
     */
    public default Result<GitHubResponse> sendDeleteRepositoryRequest(DeleteRepositoryParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNullAndNotEmpty(parameters.getOwner(), "parameters.getOwner()");
        PreCondition.assertNotNullAndNotEmpty(parameters.getName(), "parameters.getName()");

        return Result.create(() ->
        {
            final GitHubRequest gitHubRequest = GitHubRequest.create()
                .setHttpMethod(HttpMethod.DELETE)
                .setUrlPath("/repos/" + parameters.getOwner() + "/" + parameters.getName());
            final GitHubResponse result = this.sendRequest(gitHubRequest).await();

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }

    /**
     * Delete an existing {@link GitHubRepository}.
     * @param repository The {@link GitHubRepository} to delete.
     */
    public default Result<GitHubResponse> sendDeleteRepositoryRequest(GitHubRepository repository)
    {
        PreCondition.assertNotNull(repository, "repository");
        PreCondition.assertNotNull(repository.getOwner(), "repository.getOwner()");
        PreCondition.assertNotNullAndNotEmpty(repository.getOwner().getLogin(), "repository.getOwner().getLogin()");
        PreCondition.assertNotNullAndNotEmpty(repository.getName(), "repository.getName()");

        return this.sendDeleteRepositoryRequest(DeleteRepositoryParameters.create()
            .setOwner(repository.getOwner().getLogin())
            .setName(repository.getName()));
    }

    /**
     * Delete an existing {@link GitHubRepository}.
     * @param parameters The {@link DeleteRepositoryParameters} for the {@link GitHubRequest}.
     */
    public default Result<Void> deleteRepository(DeleteRepositoryParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNullAndNotEmpty(parameters.getOwner(), "parameters.getOwner()");
        PreCondition.assertNotNullAndNotEmpty(parameters.getName(), "parameters.getName()");

        return Result.create(() ->
        {
            try (final GitHubResponse response = this.sendDeleteRepositoryRequest(parameters).await())
            {
                response.throwIfErrorResponse();
            }
        });
    }

    /**
     * Delete an existing {@link GitHubRepository}.
     * @param repository The {@link GitHubRepository} to delete.
     */
    default Result<Void> deleteRepository(GitHubRepository repository)
    {
        PreCondition.assertNotNull(repository, "repository");
        PreCondition.assertNotNull(repository.getOwner(), "repository.getOwner()");
        PreCondition.assertNotNullAndNotEmpty(repository.getOwner().getLogin(), "repository.getOwner().getLogin()");
        PreCondition.assertNotNullAndNotEmpty(repository.getName(), "repository.getName()");

        return Result.create(() ->
        {
            try (final GitHubResponse response = this.sendDeleteRepositoryRequest(repository).await())
            {
                response.throwIfErrorResponse();
            }
        });
    }
}