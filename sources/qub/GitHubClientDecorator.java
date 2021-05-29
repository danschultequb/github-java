package qub;

public abstract class GitHubClientDecorator<T extends GitHubClient> implements GitHubClient
{
    private final GitHubClient innerGitHubClient;

    protected GitHubClientDecorator(GitHubClient innerGitHubClient)
    {
        PreCondition.assertNotNull(innerGitHubClient, "innerGitHubClient");

        this.innerGitHubClient = innerGitHubClient;
    }

    @Override
    public URL getBaseUrl()
    {
        return this.innerGitHubClient.getBaseUrl();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T setBaseUrl(URL baseUrl)
    {
        this.innerGitHubClient.setBaseUrl(baseUrl);

        return (T)this;
    }

    @Override
    public Result<GitHubResponse> send(GitHubRequest request)
    {
        PreCondition.assertNotNull(request, "request");

        return this.innerGitHubClient.send(request);
    }
}
