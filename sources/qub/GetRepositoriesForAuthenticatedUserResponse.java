package qub;

public class GetRepositoriesForAuthenticatedUserResponse extends GitHubResponse
{
    private GetRepositoriesForAuthenticatedUserResponse(HttpResponse httpResponse)
    {
        super(httpResponse);
    }

    public static GetRepositoriesForAuthenticatedUserResponse create(HttpResponse httpResponse)
    {
        return new GetRepositoriesForAuthenticatedUserResponse(httpResponse);
    }

    /**
     * Get the repositories in the response.
     * @return The repositories in the response.
     */
    public Result<Iterable<GitHubRepository>> getRepositories()
    {
        return Result.create(() ->
        {
            this.throwIfErrorResponse();

            final JSONArray bodyJson = this.getBodyJsonArray().await();
            final Iterable<GitHubRepository> result = bodyJson.instanceOf(JSONObject.class)
                .map((JSONObject repositoryJson) -> GitHubRepository.create(repositoryJson));

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }
}
