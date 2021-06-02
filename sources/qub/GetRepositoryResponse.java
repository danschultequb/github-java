package qub;

public class GetRepositoryResponse extends GitHubResponse
{
    private GetRepositoryResponse(HttpResponse httpResponse)
    {
        super(httpResponse);
    }

    public static GetRepositoryResponse create(HttpResponse httpResponse)
    {
        return new GetRepositoryResponse(httpResponse);
    }

    /**
     * Get the repository in the response.
     * @return The repository in the response.
     */
    public Result<GitHubRepository> getRepository()
    {
        return Result.create(() ->
        {
            this.throwIfErrorResponse();

            return GitHubRepository.create(this.getBodyJsonObject().await());
        });
    }
}
