package qub;

public class CreateRepositoryParameters extends JSONObjectWrapperBase
{
    private static final String namePropertyName = "name";
    private static final String descriptionPropertyName = "description";
    private static final String privatePropertyName = "private";

    protected CreateRepositoryParameters(JSONObject json)
    {
        super(json);
    }

    public static CreateRepositoryParameters create()
    {
        return new CreateRepositoryParameters(JSONObject.create());
    }

    /**
     * Get the name of the repository to create.
     * @return The name of the repository to create.
     */
    public String getName()
    {
        return this.toJson().getString(CreateRepositoryParameters.namePropertyName)
            .catchError()
            .await();
    }

    /**
     * Set the name of the repository to create.
     * @param name The name of the repository to create.
     * @return This object for method chaining.
     */
    public CreateRepositoryParameters setName(String name)
    {
        PreCondition.assertNotNullAndNotEmpty(name, "name");

        this.toJson().setString(CreateRepositoryParameters.namePropertyName, name);

        return this;
    }

    /**
     * Get the description of the repository to create.
     * @return The description of the repository to create.
     */
    public String getDescription()
    {
        return this.toJson().getString(CreateRepositoryParameters.descriptionPropertyName)
            .catchError()
            .await();
    }

    /**
     * Set the description of the repository to create.
     * @param description The description of the repository to create.
     * @return This object for method chaining.
     */
    public CreateRepositoryParameters setDescription(String description)
    {
        PreCondition.assertNotNullAndNotEmpty(description, "description");

        this.toJson().setString(CreateRepositoryParameters.descriptionPropertyName, description);

        return this;
    }

    /**
     * Get whether or not the repository to create will be private.
     * @return Whether or not the repository to create will be private.
     */
    public Boolean getPrivate()
    {
        return this.toJson().getBoolean(CreateRepositoryParameters.privatePropertyName)
            .catchError()
            .await();
    }

    /**
     * Set whether or not the repository to create will be private.
     * @param isPrivate Whether or not the repository to create will be private.
     * @return This object for method chaining.
     */
    public CreateRepositoryParameters setPrivate(boolean isPrivate)
    {
        this.toJson().setBoolean(CreateRepositoryParameters.privatePropertyName, isPrivate);

        return this;
    }
}
