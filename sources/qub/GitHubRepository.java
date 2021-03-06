package qub;

/**
 * An object that describes a GitHub repository.
 */
public class GitHubRepository extends JSONObjectWrapperBase
{
    private static final String namePropertyName = "name";
    private static final String fullNamePropertyName = "full_name";
    private static final String ownerPropertyName = "owner";
    private static final String gitUrlPropertyName = "git_url";
    private static final String cloneUrlPropertyName = "clone_url";

    protected GitHubRepository(JSONObject json)
    {
        super(json);
    }

    /**
     * Create a new GitHubRepository object.
     * @return The new GitHubRepository object.
     */
    public static GitHubRepository create()
    {
        return GitHubRepository.create(JSONObject.create());
    }

    /**
     * Wrap a GitHubRepository object around the provided JSON object.
     * @param json The JSONObject to wrap with a GitHubRepository object.
     * @return The new GitHubRepository object.
     */
    public static GitHubRepository create(JSONObject json)
    {
        return new GitHubRepository(json);
    }

    /**
     * Get the name of this repository.
     * @return The name of this repository.
     */
    public String getName()
    {
        return this.toJson().getString(GitHubRepository.namePropertyName)
            .catchError()
            .await();
    }

    /**
     * Set the name of this repository.
     * @param name The name of this repository.
     * @return This object for method chaining.
     */
    public GitHubRepository setName(String name)
    {
        PreCondition.assertNotNullAndNotEmpty(name, "name");

        this.toJson().setString(GitHubRepository.namePropertyName, name);

        return this;
    }

    /**
     * Get the full name of this repository (owner and repository name).
     * @return The full name of this repository (owner and repository name).
     */
    public String getFullName()
    {
        return this.toJson().getString(GitHubRepository.fullNamePropertyName)
            .catchError()
            .await();
    }

    /**
     * Set the full name of this repository (owner and repository name).
     * @param fullName The full name of this repository (owner and repository name).
     * @return This object for method chaining.
     */
    public GitHubRepository setFullName(String fullName)
    {
        PreCondition.assertNotNullAndNotEmpty(fullName, "fullName");

        this.toJson().setString(GitHubRepository.fullNamePropertyName, fullName);

        return this;
    }

    /**
     * Get the owner of this repository.
     * @return The owner of this repository.
     */
    public GitHubUser getOwner()
    {
        return this.toJson().getObject(GitHubRepository.ownerPropertyName)
            .then((JSONObject ownerJson) -> GitHubUser.create(ownerJson))
            .catchError()
            .await();
    }

    /**
     * Set the owner of this repository.
     * @param owner The owner of this repository.
     * @return This object for method chaining.
     */
    public GitHubRepository setOwner(GitHubUser owner)
    {
        PreCondition.assertNotNull(owner, "owner");

        this.toJson().setObject(GitHubRepository.ownerPropertyName, owner.toJson());

        return this;
    }

    /**
     * Get the git-specific URL for this repository.
     * @return The git-specific URL for this repository.
     */
    public URL getGitUrl()
    {
        return this.toJson().getString(GitHubRepository.gitUrlPropertyName)
            .then((String gitUrlString) -> URL.parse(gitUrlString).await())
            .catchError()
            .await();
    }

    /**
     * Set the git-specific URL for this repository.
     * @param gitUrl The git-specific URL for this repository.
     * @return This object for method chaining.
     */
    public GitHubRepository setGitUrl(URL gitUrl)
    {
        PreCondition.assertNotNull(gitUrl, "gitUrl");

        this.toJson().setString(GitHubRepository.gitUrlPropertyName, gitUrl.toString(true));

        return this;
    }

    public URL getCloneUrl()
    {
        return this.toJson().getString(GitHubRepository.cloneUrlPropertyName)
            .then((String gitUrlString) -> URL.parse(gitUrlString).await())
            .catchError()
            .await();
    }

    public GitHubRepository setCloneUrl(URL cloneUrl)
    {
        PreCondition.assertNotNull(cloneUrl, "cloneUrl");

        this.toJson().setString(GitHubRepository.cloneUrlPropertyName, cloneUrl.toString(true));

        return this;
    }
}
