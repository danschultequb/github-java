package qub;

public class GitHubUser extends JSONObjectWrapperBase
{
    private static final String loginPropertyName = "login";

    protected GitHubUser(JSONObject json)
    {
        super(json);
    }

    public static GitHubUser create()
    {
        return GitHubUser.create(JSONObject.create());
    }

    public static GitHubUser create(JSONObject json)
    {
        return new GitHubUser(json);
    }

    /**
     * Get the login associated with this user.
     * @return The login associated with this user.
     */
    public String getLogin()
    {
        return this.toJson().getString(GitHubUser.loginPropertyName)
            .catchError()
            .await();
    }

    /**
     * Set the login associated with this user.
     * @param login The login associated with this user.
     * @return This object for method chaining.
     */
    public GitHubUser setLogin(String login)
    {
        PreCondition.assertNotNullAndNotEmpty(login, "login");

        this.toJson().setString(GitHubUser.loginPropertyName, login);

        return this;
    }
}
