package qub;

public class DeleteRepositoryParameters extends JSONObjectWrapperBase
{
    private static final String ownerPropertyName = "owner";
    private static final String namePropertyName = "name";

    protected DeleteRepositoryParameters(JSONObject json)
    {
        super(json);
    }

    public static DeleteRepositoryParameters create()
    {
        return DeleteRepositoryParameters.create(JSONObject.create());
    }

    public static DeleteRepositoryParameters create(JSONObject json)
    {
        return new DeleteRepositoryParameters(json);
    }

    public String getOwner()
    {
        return this.toJson().getString(DeleteRepositoryParameters.ownerPropertyName)
            .catchError()
            .await();
    }

    public DeleteRepositoryParameters setOwner(String owner)
    {
        PreCondition.assertNotNullAndNotEmpty(owner, "owner");

        this.toJson().setString(DeleteRepositoryParameters.ownerPropertyName, owner);

        return this;
    }

    public String getName()
    {
        return this.toJson().getString(DeleteRepositoryParameters.namePropertyName)
            .catchError()
            .await();
    }

    public DeleteRepositoryParameters setName(String name)
    {
        PreCondition.assertNotNullAndNotEmpty(name, "name");

        this.toJson().setString(DeleteRepositoryParameters.namePropertyName, name);

        return this;
    }
}
