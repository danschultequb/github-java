package qub;

public class GitHubErrorResponse extends JSONObjectWrapperBase
{
    private static final String messagePropertyName = "message";
    private static final String documentationUrlPropertyName = "documentation_url";
    private static final String errorsPropertyName = "errors";

    protected GitHubErrorResponse(JSONObject json)
    {
        super(json);
    }

    public static GitHubErrorResponse create()
    {
        return GitHubErrorResponse.create(JSONObject.create());
    }

    public static GitHubErrorResponse create(JSONObject json)
    {
        return new GitHubErrorResponse(json);
    }

    public String getMessage()
    {
        return this.toJson().getString(GitHubErrorResponse.messagePropertyName)
            .catchError()
            .await();
    }

    public GitHubErrorResponse setMessage(String message)
    {
        this.toJson().setString(GitHubErrorResponse.messagePropertyName, message);

        return this;
    }

    public String getDocumentationUrl()
    {
        return this.toJson().getString(GitHubErrorResponse.documentationUrlPropertyName)
            .catchError()
            .await();
    }

    public GitHubErrorResponse setDocumentationUrl(String documentationUrl)
    {
        this.toJson().setString(GitHubErrorResponse.documentationUrlPropertyName, documentationUrl);

        return this;
    }

    /**
     * Get the errors that were reported as a result of the failed request.
     * @return The errors that were reported as a result of the failed request.
     */
    public Iterable<GitHubError> getErrors()
    {
        return this.toJson().getArray(GitHubErrorResponse.errorsPropertyName)
            .then((JSONArray json) -> json.instanceOf(JSONObject.class).map(GitHubError::create))
            .catchError(() -> Iterable.create())
            .await();
    }

    public GitHubErrorResponse setErrors(Iterable<GitHubError> errors)
    {
        PreCondition.assertNotNull(errors, "errors");

        this.toJson().setArray(GitHubErrorResponse.errorsPropertyName, errors.map(GitHubError::toJson));

        return this;
    }
}
