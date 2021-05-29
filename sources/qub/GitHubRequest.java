package qub;

public class GitHubRequest
{
    private String httpMethod;
    private URL baseUrl;
    private String urlPath;
    private final MutableHttpHeaders httpHeaders;
    private long bodyLength;
    private ByteReadStream body;

    protected GitHubRequest()
    {
        this.httpHeaders = HttpHeaders.create();
    }

    public static GitHubRequest create()
    {
        return new GitHubRequest();
    }

    @Override
    public GitHubRequest clone()
    {
        final GitHubRequest result = GitHubRequest.create();
        if (!Strings.isNullOrEmpty(this.httpMethod))
        {
            result.setHttpMethod(this.httpMethod);
        }
        if (this.baseUrl != null)
        {
            result.setBaseUrl(this.baseUrl);
        }
        if (!Strings.isNullOrEmpty(this.urlPath))
        {
            result.setUrlPath(this.urlPath);
        }
        result.setHeaders(this.httpHeaders);
        if (this.body != null)
        {
            result.setBody(this.bodyLength, body);
        }

        PostCondition.assertNotNull(result, "result");

        return result;
    }

    public String getHttpMethod()
    {
        return this.httpMethod;
    }

    public GitHubRequest setHttpMethod(String httpMethod)
    {
        PreCondition.assertNotNullAndNotEmpty(httpMethod, "httpMethod");

        this.httpMethod = httpMethod;

        return this;
    }

    public GitHubRequest setHttpMethod(HttpMethod httpMethod)
    {
        PreCondition.assertNotNull(httpMethod, "httpMethod");

        return this.setHttpMethod(httpMethod.toString());
    }

    public URL getBaseUrl()
    {
        return this.baseUrl;
    }

    public Result<GitHubRequest> setBaseUrl(String baseUrl)
    {
        PreCondition.assertNotNullAndNotEmpty(baseUrl, "baseUrl");

        return Result.create(() ->
        {
            return this.setBaseUrl(URL.parse(baseUrl).await());
        });
    }

    public GitHubRequest setBaseUrl(URL baseUrl)
    {
        PreCondition.assertNotNull(baseUrl, "baseUrl");

        this.baseUrl = baseUrl;

        return this;
    }

    public String getUrlPath()
    {
        return this.urlPath;
    }

    public GitHubRequest setUrlPath(String urlPath)
    {
        PreCondition.assertNotNullAndNotEmpty(urlPath, "urlPath");

        this.urlPath = urlPath;

        return this;
    }

    public HttpHeaders getHeaders()
    {
        return this.httpHeaders;
    }

    public GitHubRequest setHeader(String headerName, String headerValue)
    {
        this.httpHeaders.set(headerName, headerValue);

        return this;
    }

    public GitHubRequest setHeader(String headerName, int headerValue)
    {
        this.httpHeaders.set(headerName, headerValue);

        return this;
    }

    public GitHubRequest setHeader(String headerName, long headerValue)
    {
        this.httpHeaders.set(headerName, headerValue);

        return this;
    }

    public GitHubRequest setAuthorizationHeader(String token)
    {
        PreCondition.assertNotNullAndNotEmpty(token, "token");

        return this.setHeader("Authorization", "token " + token);
    }

    public GitHubRequest setHeaders(Iterable<HttpHeader> headers)
    {
        this.httpHeaders.setAll(headers);

        return this;
    }

    public long getBodyLength()
    {
        return this.bodyLength;
    }

    public ByteReadStream getBody()
    {
        return this.body;
    }

    public GitHubRequest setBody(long bodyLength, ByteReadStream body)
    {
        PreCondition.assertGreaterThanOrEqualTo(0, bodyLength, "bodyLength");
        PreCondition.assertNotNull(body, "body");
        PreCondition.assertNotDisposed(body, "body");

        this.bodyLength = bodyLength;
        this.body = body;

        return this;
    }

    public Result<? extends GitHubRequest> setBody(String body)
    {
        PreCondition.assertNotNull(body, "body");

        return Result.create(() ->
        {
            final InMemoryByteStream bodyStream = InMemoryByteStream.create();
            final int bodyLength = CharacterEncoding.UTF_8.encodeCharacters(body, bodyStream).await();
            return this.setBody(bodyLength, bodyStream);
        });
    }

    public Result<? extends GitHubRequest> setBody(JSONObject bodyJson)
    {
        PreCondition.assertNotNull(bodyJson, "bodyJson");

        return this.setBody(bodyJson.toString());
    }

    public Result<? extends GitHubRequest> setBody(JSONArray bodyJson)
    {
        PreCondition.assertNotNull(bodyJson, "bodyJson");

        return this.setBody(bodyJson.toString());
    }
}
