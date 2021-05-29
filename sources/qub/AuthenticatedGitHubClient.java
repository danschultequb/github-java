package qub;

/**
 * A GitHubClient that attaches an "Authorization" HTTP header to each request it sends.
 */
public class AuthenticatedGitHubClient extends GitHubClientDecorator<AuthenticatedGitHubClient>
{
    private String personalAccessToken;

    private AuthenticatedGitHubClient(GitHubClient innerGitHubClient, String personalAccessToken)
    {
        super(innerGitHubClient);

        PreCondition.assertNotNullAndNotEmpty(personalAccessToken, "personalAccessToken");

        this.personalAccessToken = personalAccessToken;
    }

    public static AuthenticatedGitHubClient create(GitHubClient innerGitHubClient, String personalAccessToken)
    {
        return new AuthenticatedGitHubClient(innerGitHubClient, personalAccessToken);
    }

    public AuthenticatedGitHubClient setPersonalAccessToken(String personalAccessToken)
    {
        PreCondition.assertNotNullAndNotEmpty(personalAccessToken, "personalAccessToken");

        this.personalAccessToken = personalAccessToken;

        return this;
    }

    @Override
    public Result<GitHubResponse> send(GitHubRequest request)
    {
        PreCondition.assertNotNull(request, "request");

        final GitHubRequest authenticatedRequest = request.clone()
            .setAuthorizationHeader(this.personalAccessToken);
        return super.send(authenticatedRequest);
    }
}
