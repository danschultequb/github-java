package qub;

public class GetRepositoryParameters extends JSONObjectWrapperBase
{
    private static final String ownerPropertyName = "owner";
    private static final String namePropertyName = "name";

    protected GetRepositoryParameters(JSONObject json)
    {
        super(json);
    }

    public static GetRepositoryParameters create()
    {
        return new GetRepositoryParameters(JSONObject.create());
    }

    public String getOwner()
    {
        return this.toJson().getString(GetRepositoryParameters.ownerPropertyName)
            .catchError()
            .await();
    }

    public GetRepositoryParameters setOwner(String owner)
    {
        PreCondition.assertNotNullAndNotEmpty(owner, "owner");

        this.toJson().setString(GetRepositoryParameters.ownerPropertyName, owner);

        return this;
    }

    public String getName()
    {
        return this.toJson().getString(GetRepositoryParameters.namePropertyName)
            .catchError()
            .await();
    }

    public GetRepositoryParameters setName(String name)
    {
        PreCondition.assertNotNullAndNotEmpty(name, "name");

        this.toJson().setString(GetRepositoryParameters.namePropertyName, name);

        return this;
    }
}
