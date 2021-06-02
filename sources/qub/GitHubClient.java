package qub;

/**
 * An client interface for interacting with a GitHub endpoint.
 */
public interface GitHubClient
{
    static BasicGitHubClient create(Network network)
    {
        return BasicGitHubClient.create(network);
    }

    static BasicGitHubClient create(HttpClient httpClient)
    {
        return BasicGitHubClient.create(httpClient);
    }

    /**
     * Get the base URL that this GitHubClient will target.
     * @return The base URL that this GitHubClient will target.
     */
    URL getBaseUrl();

    /**
     * Set the base URL that this GitHubClient will target.
     * @param baseUrl The base URL that this GitHubClient will target.
     * @return This object for method chaining.
     */
    default Result<GitHubClient> setBaseUrl(String baseUrl)
    {
        PreCondition.assertNotNullAndNotEmpty(baseUrl, "baseUrl");

        return Result.create(() ->
        {
            return this.setBaseUrl(URL.parse(baseUrl).await());
        });
    }

    /**
     * Set the base URL that this GitHubClient will target.
     * @param baseUrl The base URL that this GitHubClient will target.
     * @return This object for method chaining.
     */
    GitHubClient setBaseUrl(URL baseUrl);

    /**
     * Set the access token that will be used to authenticate the client's requests.
     * @param accessToken The access token that will be used to authenticate the client's requests.
     * @return This object for method chaining.
     */
    GitHubClient setAccessToken(String accessToken);

    /**
     * Get whether or not this client has an access token.
     * @return Whether or not this client has an access token.
     */
    boolean hasAccessToken();

    /**
     * Send a generic GitHubRequest. If this client is authenticated, then the personal access
     * token will be added to the request before it is sent.
     * @param request The request to send.
     * @return The response for the request.
     */
    Result<GitHubResponse> send(GitHubRequest request);

    /**
     * Get the details about the user that this client is authenticated to.
     * @return The details about the user that this client is authenticated to.
     */
    default Result<GetAuthenticatedUserResponse> getAuthenticatedUser()
    {
        return Result.create(() ->
        {
            final GitHubRequest gitHubRequest = GitHubRequest.create()
                .setHttpMethod(HttpMethod.GET)
                .setUrlPath("/user");
            final GitHubResponse gitHubResponse = this.send(gitHubRequest).await();
            final GetAuthenticatedUserResponse result = GetAuthenticatedUserResponse.create(gitHubResponse);

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }

    /**
     * Get the repository that matches the provided parameters.
     * @param parameters The parameters that describe the repository to return.
     * @return The matching repository.
     */
    default Result<GetRepositoryResponse> getRepository(GetRepositoryParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNullAndNotEmpty(parameters.getOwner(), "parameters.getOwner()");
        PreCondition.assertNotNullAndNotEmpty(parameters.getName(), "parameters.getName()");

        return Result.create(() ->
        {
            final GitHubRequest gitHubRequest = GitHubRequest.create()
                .setHttpMethod(HttpMethod.GET)
                .setUrlPath("/repos/" + parameters.getOwner() + "/" + parameters.getName());
            final GitHubResponse gitHubResponse = this.send(gitHubRequest).await();
            final GetRepositoryResponse result = GetRepositoryResponse.create(gitHubResponse);

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }

    /**
     * Get the repositories the authenticated user has permission to access.
     * @return The repositories the authenticated user has permission to access.
     */
    default Result<GetRepositoriesForAuthenticatedUserResponse> getRepositoriesForAuthenticatedUser()
    {
        return Result.create(() ->
        {
            final GitHubRequest gitHubRequest = GitHubRequest.create()
                .setHttpMethod(HttpMethod.GET)
                .setUrlPath("/user/repos");
            final GitHubResponse gitHubResponse = this.send(gitHubRequest).await();
            final GetRepositoriesForAuthenticatedUserResponse result = GetRepositoriesForAuthenticatedUserResponse.create(gitHubResponse);

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }

    /**
     * Create a new repository.
     * @param parameters The parameters for the request.
     * @return The newly created repository.
     */
    default Result<CreateRepositoryResponse> createRepository(CreateRepositoryParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNullAndNotEmpty(parameters.getName(), "parameters.getName()");

        return Result.create(() ->
        {
            final GitHubRequest gitHubRequest = GitHubRequest.create()
                .setHttpMethod(HttpMethod.POST)
                .setUrlPath("/user/repos")
                .setBody(parameters.toJson()).await();
            final GitHubResponse gitHubResponse = this.send(gitHubRequest).await();
            final CreateRepositoryResponse result = CreateRepositoryResponse.create(gitHubResponse);

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }

    /**
     * Delete an existing repository.
     * @param parameters The parameters for the request.
     * @return The result of deleting the repository.
     */
    default Result<GitHubResponse> deleteRepository(DeleteRepositoryParameters parameters)
    {
        PreCondition.assertNotNull(parameters, "parameters");
        PreCondition.assertNotNullAndNotEmpty(parameters.getOwner(), "parameters.getOwner()");
        PreCondition.assertNotNullAndNotEmpty(parameters.getName(), "parameters.getName()");

        return Result.create(() ->
        {
            final GitHubRequest gitHubRequest = GitHubRequest.create()
                .setHttpMethod(HttpMethod.DELETE)
                .setUrlPath("/repos/" + parameters.getOwner() + "/" + parameters.getName());
            final GitHubResponse result = this.send(gitHubRequest).await();

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }

    /**
     * Delete an existing repository.
     * @param repository The repository to delete.
     * @return The result of deleting the repository.
     */
    default Result<GitHubResponse> deleteRepository(GitHubRepository repository)
    {
        PreCondition.assertNotNull(repository, "repository");
        PreCondition.assertNotNull(repository.getOwner(), "repository.getOwner()");
        PreCondition.assertNotNullAndNotEmpty(repository.getOwner().getLogin(), "repository.getOwner().getLogin()");
        PreCondition.assertNotNullAndNotEmpty(repository.getName(), "repository.getName()");

        return this.deleteRepository(DeleteRepositoryParameters.create()
            .setOwner(repository.getOwner().getLogin())
            .setName(repository.getName()));
    }
}