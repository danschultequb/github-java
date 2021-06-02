package qub;

public interface GetRepositoryParametersTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(GetRepositoryParameters.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final GetRepositoryParameters parameters = GetRepositoryParameters.create();
                test.assertNotNull(parameters);
                test.assertNull(parameters.getOwner());
                test.assertNull(parameters.getName());
            });
            
            runner.testGroup("setOwner(String)", () ->
            {
                final Action2<String,Throwable> setOwnerErrorTest = (String owner, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(owner), (Test test) ->
                    {
                        final GetRepositoryParameters parameters = GetRepositoryParameters.create();
                        test.assertThrows(() -> parameters.setOwner(owner),
                            expected);
                        test.assertNull(parameters.getOwner());
                    });
                };

                setOwnerErrorTest.run(null, new PreConditionFailure("owner cannot be null."));
                setOwnerErrorTest.run("", new PreConditionFailure("owner cannot be empty."));

                final Action1<String> setOwnerTest = (String owner) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(owner), (Test test) ->
                    {
                        final GetRepositoryParameters parameters = GetRepositoryParameters.create();
                        final GetRepositoryParameters setOwnerResult = parameters.setOwner(owner);
                        test.assertSame(parameters, setOwnerResult);
                        test.assertEqual(owner, parameters.getOwner());
                    });
                };

                setOwnerTest.run("fake-repo-owner");
            });

            runner.testGroup("setName(String)", () ->
            {
                final Action2<String,Throwable> setNameErrorTest = (String name, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(name), (Test test) ->
                    {
                        final GetRepositoryParameters parameters = GetRepositoryParameters.create();
                        test.assertThrows(() -> parameters.setName(name),
                            expected);
                        test.assertNull(parameters.getName());
                    });
                };

                setNameErrorTest.run(null, new PreConditionFailure("name cannot be null."));
                setNameErrorTest.run("", new PreConditionFailure("name cannot be empty."));

                final Action1<String> setNameTest = (String name) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(name), (Test test) ->
                    {
                        final GetRepositoryParameters parameters = GetRepositoryParameters.create();
                        final GetRepositoryParameters setNameResult = parameters.setName(name);
                        test.assertSame(parameters, setNameResult);
                        test.assertEqual(name, parameters.getName());
                    });
                };

                setNameTest.run("fake-repo-name");
            });
        });
    }
}
