package qub;

public class GetAuthenticatedUserResponse extends GitHubResponse
{
    private GetAuthenticatedUserResponse(HttpResponse httpResponse)
    {
        super(httpResponse);
    }

    public static GetAuthenticatedUserResponse create(HttpResponse httpResponse)
    {
        return new GetAuthenticatedUserResponse(httpResponse);
    }

    /**
     * Get the authenticated user in the response.
     * @return The authenticated user in the response.
     */
    public Result<GitHubUser> getAuthenticatedUser()
    {
        return Result.create(() ->
        {
            this.throwIfErrorResponse();

            return GitHubUser.create(this.getBodyJsonObject().await());
        });
    }
}
