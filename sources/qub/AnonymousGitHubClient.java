package qub;

/**
 * An implementation of the GitHubClient interface that makes HTTP requests to a GitHub endpoint.
 */
public class AnonymousGitHubClient implements GitHubClient
{
    private final HttpClient httpClient;
    private URL baseUrl;

    private AnonymousGitHubClient(HttpClient httpClient)
    {
        PreCondition.assertNotNull(httpClient, "httpClient");

        this.httpClient = httpClient;
        this.setBaseUrl(URL.parse("https://api.github.com").await());
    }

    static AnonymousGitHubClient create(Network network)
    {
        PreCondition.assertNotNull(network, "network");

        return AnonymousGitHubClient.create(HttpClient.create(network));
    }

    public static AnonymousGitHubClient create(HttpClient httpClient)
    {
        PreCondition.assertNotNull(httpClient, "httpClient");

        return new AnonymousGitHubClient(httpClient);
    }

    @Override
    public URL getBaseUrl()
    {
        return this.baseUrl;
    }

    @Override
    public GitHubClient setBaseUrl(URL baseUrl)
    {
        PreCondition.assertNotNull(baseUrl, "baseUrl");

        this.baseUrl = baseUrl;

        return this;
    }

    @Override
    public Result<GitHubResponse> send(GitHubRequest request)
    {
        PreCondition.assertNotNull(request, "request");
        PreCondition.assertNotNullAndNotEmpty(request.getHttpMethod(), "request.getHttpMethod()");
        PreCondition.assertNotNullAndNotEmpty(request.getUrlPath(), "request.getUrlPath()");

        return Result.create(() ->
        {
            final String httpMethod = request.getHttpMethod();
            URL baseUrl = request.getBaseUrl();
            if (baseUrl == null)
            {
                baseUrl = this.getBaseUrl();
            }
            final String urlPath = request.getUrlPath();
            final URL url = baseUrl.clone()
                .setPath(urlPath);
            final HttpHeaders requestHeaders = request.getHeaders();
            final ByteReadStream requestBody = request.getBody();
            final long requestBodyLength = request.getBodyLength();

            final MutableHttpRequest httpRequest = HttpRequest.create()
                .setMethod(httpMethod)
                .setUrl(url)
                .setHeaders(requestHeaders);
            if (requestBody != null)
            {
                httpRequest.setBody(requestBodyLength, requestBody);
            }

            final HttpResponse httpResponse = this.httpClient.send(httpRequest).await();

            final GitHubResponse result = GitHubResponse.create(httpResponse);

            PostCondition.assertNotNull(result, "result");

            return result;
        });
    }
}
