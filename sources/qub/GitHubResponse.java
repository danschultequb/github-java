package qub;

public class GitHubResponse implements HttpResponse
{
    private final HttpResponse httpResponse;

    private JSONSegment bodyJson;
    private GitHubErrorResponse errorResponse;

    protected GitHubResponse(HttpResponse httpResponse)
    {
        PreCondition.assertNotNull(httpResponse, "httpResponse");

        this.httpResponse = httpResponse;
    }

    public static GitHubResponse create(HttpResponse httpResponse)
    {
        return new GitHubResponse(httpResponse);
    }

    @Override
    public String getHttpVersion()
    {
        return this.httpResponse.getHttpVersion();
    }

    @Override
    public int getStatusCode()
    {
        return this.httpResponse.getStatusCode();
    }

    @Override
    public String getReasonPhrase()
    {
        return this.httpResponse.getReasonPhrase();
    }

    @Override
    public HttpHeaders getHeaders()
    {
        return this.httpResponse.getHeaders();
    }

    @Override
    public ByteReadStream getBody()
    {
        return this.httpResponse.getBody();
    }

    @Override
    public boolean isDisposed()
    {
        return this.httpResponse.isDisposed();
    }

    @Override
    public Result<Boolean> dispose()
    {
        return this.httpResponse.dispose();
    }

    /**
     * Parse the body of this response into a JSONSegment.
     * @return The JSON-parsed body of this response.
     */
    public Result<JSONSegment> getBodyJson()
    {
        return Result.create(() ->
        {
            if (this.bodyJson == null)
            {
                this.bodyJson = JSON.parse(this.getBody()).await();
            }
            return this.bodyJson;
        });
    }

    /**
     * Parse the body of this response into a JSONObject.
     * @return The JSONObject-parsed body of this response.
     */
    public Result<JSONObject> getBodyJsonObject()
    {
        return this.getBodyJson()
            .then((JSONSegment bodyJson) ->
            {
                final JSONObject result = Types.as(bodyJson, JSONObject.class);
                if (result == null)
                {
                    throw new ParseException("Expected the response's body to be a " + Types.getTypeName(JSONObject.class) + ", but it was a " + Types.getTypeName(bodyJson) + " instead.");
                }
                return result;
            });
    }

    /**
     * Parse the body of this response into a JSONArray.
     * @return The JSONArray-parsed body of this response.
     */
    public Result<JSONArray> getBodyJsonArray()
    {
        return this.getBodyJson()
            .then((JSONSegment bodyJson) ->
            {
                final JSONArray result = Types.as(bodyJson, JSONArray.class);
                if (result == null)
                {
                    throw new ParseException("Expected the response's body to be a " + Types.getTypeName(JSONArray.class) + ", but it was a " + Types.getTypeName(bodyJson) + " instead.");
                }
                return result;
            });
    }

    /**
     * Get whether or not this is an error response.
     * @return Whether or not this is an error response.
     */
    public boolean isErrorResponse()
    {
        return Comparer.between(400, this.getStatusCode(), 499);
    }

    /**
     * Throw a GitHubException if this GitHubResponse is an error response.
     */
    public void throwIfErrorResponse()
    {
        if (this.isErrorResponse())
        {
            throw new GitHubException(this.getStatusCode(), this.getErrorResponse().await());
        }

        PostCondition.assertFalse(this.isErrorResponse(), "this.isErrorResponse()");
    }

    /**
     * Get the body of this response parsed as a GitHubErrorResponse object.
     * @return The body of this response parsed as a GitHubErrorResponse object.
     */
    public Result<GitHubErrorResponse> getErrorResponse()
    {
        PreCondition.assertTrue(this.isErrorResponse(), "this.isErrorResponse()");

        return Result.create(() ->
        {
            if (this.errorResponse == null)
            {
                final JSONSegment bodyJson = this.getBodyJson().await();
                if (bodyJson instanceof JSONObject)
                {
                    this.errorResponse = GitHubErrorResponse.create((JSONObject)bodyJson);
                }
                else
                {
                    throw new ParseException("A " + Types.getTypeName(GitHubErrorResponse.class) + " must be created from a " + Types.getTypeName(JSONObject.class) + ", but found " + Types.getTypeName(bodyJson) + " instead.");
                }
            }
            return this.errorResponse;
        });
    }
}
