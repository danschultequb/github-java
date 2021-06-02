package qub;

public class CreateRepositoryResponse extends GitHubResponse
{
    private CreateRepositoryResponse(HttpResponse httpResponse)
    {
        super(httpResponse);
    }

    public static CreateRepositoryResponse create(HttpResponse httpResponse)
    {
        return new CreateRepositoryResponse(httpResponse);
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
