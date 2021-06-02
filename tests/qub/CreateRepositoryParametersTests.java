package qub;

public interface CreateRepositoryParametersTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(CreateRepositoryParameters.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final CreateRepositoryParameters parameters = CreateRepositoryParameters.create();
                test.assertNotNull(parameters);
                test.assertNull(parameters.getName());
                test.assertNull(parameters.getDescription());
                test.assertNull(parameters.getPrivate());
            });

            runner.testGroup("setName(String)", () ->
            {
                final Action2<String,Throwable> setNameErrorTest = (String name, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(name), (Test test) ->
                    {
                        final CreateRepositoryParameters parameters = CreateRepositoryParameters.create();
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
                        final CreateRepositoryParameters parameters = CreateRepositoryParameters.create();
                        final CreateRepositoryParameters setNameResult = parameters.setName(name);
                        test.assertSame(parameters, setNameResult);
                        test.assertEqual(name, parameters.getName());
                    });
                };

                setNameTest.run("fake-repo-name");
            });

            runner.testGroup("setDescription(String)", () ->
            {
                final Action2<String,Throwable> setDescriptionErrorTest = (String description, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(description), (Test test) ->
                    {
                        final CreateRepositoryParameters parameters = CreateRepositoryParameters.create();
                        test.assertThrows(() -> parameters.setDescription(description),
                            expected);
                        test.assertNull(parameters.getDescription());
                    });
                };

                setDescriptionErrorTest.run(null, new PreConditionFailure("description cannot be null."));
                setDescriptionErrorTest.run("", new PreConditionFailure("description cannot be empty."));

                final Action1<String> setDescriptionTest = (String description) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(description), (Test test) ->
                    {
                        final CreateRepositoryParameters parameters = CreateRepositoryParameters.create();
                        final CreateRepositoryParameters setDescriptionResult = parameters.setDescription(description);
                        test.assertSame(parameters, setDescriptionResult);
                        test.assertEqual(description, parameters.getDescription());
                    });
                };

                setDescriptionTest.run("fake-repo-description");
            });

            runner.testGroup("setPrivate(boolean)", () ->
            {
                final Action1<Boolean> setPrivateTest = (Boolean isPrivate) ->
                {
                    runner.test("with " + isPrivate, (Test test) ->
                    {
                        final CreateRepositoryParameters parameters = CreateRepositoryParameters.create();
                        final CreateRepositoryParameters setPrivateResult = parameters.setPrivate(isPrivate);
                        test.assertSame(parameters, setPrivateResult);
                        test.assertEqual(isPrivate, parameters.getPrivate());
                    });
                };

                setPrivateTest.run(false);
                setPrivateTest.run(true);
            });
        });
    }
}
