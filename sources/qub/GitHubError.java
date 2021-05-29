package qub;

public class GitHubError extends JSONObjectWrapperBase
{
    private static final String resourcePropertyName = "resource";
    private static final String codePropertyName = "code";
    private static final String fieldPropertyName = "field";
    private static final String messagePropertyName = "message";

    protected GitHubError(JSONObject json)
    {
        super(json);
    }

    public static GitHubError create()
    {
        return GitHubError.create(JSONObject.create());
    }

    public static GitHubError create(JSONObject json)
    {
        return new GitHubError(json);
    }

    public String getResource()
    {
        return this.toJson().getString(GitHubError.resourcePropertyName)
            .catchError()
            .await();
    }

    public GitHubError setResource(String resource)
    {
        PreCondition.assertNotNullAndNotEmpty(resource, "resource");

        this.toJson().setString(GitHubError.resourcePropertyName, resource);

        return this;
    }

    public String getCode()
    {
        return this.toJson().getString(GitHubError.codePropertyName)
            .catchError()
            .await();
    }

    public GitHubError setCode(String code)
    {
        PreCondition.assertNotNullAndNotEmpty(code, "code");

        this.toJson().setString(GitHubError.codePropertyName, code);

        return this;
    }

    public String getField()
    {
        return this.toJson().getString(GitHubError.fieldPropertyName)
            .catchError()
            .await();
    }

    public GitHubError setField(String field)
    {
        PreCondition.assertNotNullAndNotEmpty(field, "field");

        this.toJson().setString(GitHubError.fieldPropertyName, field);

        return this;
    }

    public String getMessage()
    {
        return this.toJson().getString(GitHubError.messagePropertyName)
            .catchError()
            .await();
    }

    public GitHubError setMessage(String message)
    {
        PreCondition.assertNotNullAndNotEmpty(message, "message");

        this.toJson().setString(GitHubError.messagePropertyName, message);

        return this;
    }
}
