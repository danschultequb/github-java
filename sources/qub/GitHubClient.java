package qub;

/**
 * An client interface for interacting with a GitHub endpoint.
 */
public interface GitHubClient
{
    static AnonymousGitHubClient create(Network network)
    {
        return AnonymousGitHubClient.create(network);
    }

    static AnonymousGitHubClient create(HttpClient httpClient)
    {
        return AnonymousGitHubClient.create(httpClient);
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
    // Result<GitHubRepository> getRepository(GetRepositoryParameters parameters);

    /**
     * Get the repositories the authenticated user has permission to access.
     * @return The repositories the authenticated user has permission to access.
     */
    // Result<Iterable<GitHubRepository>> getRepositoriesForAuthenticatedUser();

    /**
     * Create a new repository.
     * @param parameters The parameters for the request.
     * @return The newly created repository.
     */
    // Result<GitHubRepository> createRepository(CreateRepositoryParameters parameters);

    /**
     * Delete an existing repository.
     * @param parameters The parameters for the request.
     * @return The result of deleting the repository.
     */
    // Result<Void> deleteRepository(DeleteRepositoryParameters parameters);

    /**
     * Delete an existing repository.
     * @param repository The repository to delete.
     * @return The result of deleting the repository.
     */
//    default Result<Void> deleteRepository(GitHubRepository repository)
//    {
//        PreCondition.assertNotNull(repository, "repository");
//        PreCondition.assertNotNull(repository.getOwner(), "repository.getOwner()");
//        PreCondition.assertNotNullAndNotEmpty(repository.getOwner().getLogin(), "repository.getOwner().getLogin()");
//        PreCondition.assertNotNullAndNotEmpty(repository.getName(), "repository.getName()");
//
//        return this.deleteRepository(DeleteRepositoryParameters.create()
//            .setOwner(repository.getOwner().getLogin())
//            .setName(repository.getName()));
//    }
}