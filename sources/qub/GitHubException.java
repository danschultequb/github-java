package qub;

/**
 * An exception that occurs as a result of performing a GitHub request.
 */
public class GitHubException extends RuntimeException
{
    private final int statusCode;
    private final GitHubErrorResponse errorResponse;

    public GitHubException(int statusCode, GitHubErrorResponse errorResponse)
    {
        super(GitHubException.getMessage(errorResponse));

        this.statusCode = statusCode;
        this.errorResponse = errorResponse;
    }

    private static String getMessage(GitHubErrorResponse errorResponse)
    {
        PreCondition.assertNotNull(errorResponse, "errorResponse");
        PreCondition.assertNotNullAndNotEmpty(errorResponse.getMessage(), "errorResponse.getMessage()");

        return errorResponse.getMessage();
    }

    /**
     * Get the status code of the response that created this exception.
     * @return The status code of the response that created this exception.
     */
    public int getStatusCode()
    {
        return this.statusCode;
    }

    /**
     * Get the error response for this exception.
     * @return The error response for this exception.
     */
    public GitHubErrorResponse getErrorResponse()
    {
        return this.errorResponse;
    }

    /**
     * Get the documentation URL provided by GitHub as an explanation for why the request failed.
     * @return The documentation URL provided by GitHub as an explanation for why the request failed.
     */
    public String getDocumentationUrl()
    {
        return this.getErrorResponse().getDocumentationUrl();
    }

    /**
     * Get the errors that were reported as a result of the failed request.
     * @return The errors that were reported as a result of the failed request.
     */
    public Iterable<GitHubError> getErrors()
    {
        return this.getErrorResponse().getErrors();
    }

    /**
     * Get the JSON Object representation of this GitHubException.
     * @return The JSON Object representation of this GitHubException.
     */
    public JSONObject toJson()
    {
        return JSONObject.create()
            .setNumber("statusCode", this.getStatusCode())
            .setObject("errorResponse", this.getErrorResponse().toJson());
    }

    @Override
    public String toString()
    {
        return super.toString() + " " + this.toJson();
    }
}
