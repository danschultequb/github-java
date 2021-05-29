package qub;

public interface GitHubRepositoryTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(GitHubRepository.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final GitHubRepository repository = GitHubRepository.create();
                test.assertNotNull(repository);
                test.assertNull(repository.getName());
                test.assertNull(repository.getFullName());
                test.assertEqual(JSONObject.create(), repository.toJson());
            });

            runner.testGroup("create(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> GitHubRepository.create(null),
                        new PreConditionFailure("json cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final JSONObject json = JSONObject.create()
                        .setString("name", "fake-name")
                        .setString("full_name", "fake-full-name");
                    final GitHubRepository repository = GitHubRepository.create(json);
                    test.assertNotNull(repository);
                    test.assertEqual("fake-name", repository.getName());
                    test.assertEqual("fake-full-name", repository.getFullName());
                    test.assertSame(json, repository.toJson());
                });
            });

            runner.testGroup("setName(String)", () ->
            {
                final Action2<String,Throwable> setNameErrorTest = (String name, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(name), (Test test) ->
                    {
                        final GitHubRepository repository = GitHubRepository.create();
                        test.assertNull(repository.getName());

                        test.assertThrows(() -> repository.setName(name), expected);
                        test.assertNull(repository.getName());
                    });
                };

                setNameErrorTest.run(null, new PreConditionFailure("name cannot be null."));
                setNameErrorTest.run("", new PreConditionFailure("name cannot be empty."));

                final Action1<String> setNameTest = (String name) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(name), (Test test) ->
                    {
                        final GitHubRepository repository = GitHubRepository.create();
                        test.assertNull(repository.getName());

                        final GitHubRepository setNameResult = repository.setName(name);
                        test.assertSame(repository, setNameResult);
                        test.assertEqual(name, repository.getName());
                    });
                };

                setNameTest.run("fake-name");
                setNameTest.run("github-java");
            });

            runner.testGroup("setFullName(String)", () ->
            {
                final Action2<String,Throwable> setFullNameErrorTest = (String fullName, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(fullName), (Test test) ->
                    {
                        final GitHubRepository repository = GitHubRepository.create();
                        test.assertNull(repository.getFullName());

                        test.assertThrows(() -> repository.setFullName(fullName), expected);
                        test.assertNull(repository.getFullName());
                    });
                };

                setFullNameErrorTest.run(null, new PreConditionFailure("fullName cannot be null."));
                setFullNameErrorTest.run("", new PreConditionFailure("fullName cannot be empty."));

                final Action1<String> setFullNameTest = (String fullName) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(fullName), (Test test) ->
                    {
                        final GitHubRepository repository = GitHubRepository.create();
                        test.assertNull(repository.getFullName());

                        final GitHubRepository setFullNameResult = repository.setFullName(fullName);
                        test.assertSame(repository, setFullNameResult);
                        test.assertEqual(fullName, repository.getFullName());
                    });
                };

                setFullNameTest.run("fake-full-name");
                setFullNameTest.run("qub/github-java");
            });
        });
    }
}
