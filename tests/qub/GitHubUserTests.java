package qub;

public interface GitHubUserTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(GitHubUser.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final GitHubUser user = GitHubUser.create();
                test.assertNotNull(user);
                test.assertNull(user.getLogin());
                test.assertEqual(JSONObject.create(), user.toJson());
            });

            runner.testGroup("create(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> GitHubUser.create(null),
                        new PreConditionFailure("json cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final JSONObject json = JSONObject.create()
                        .setString("login", "fake-login");
                    final GitHubUser user = GitHubUser.create(json);
                    test.assertNotNull(user);
                    test.assertEqual("fake-login", user.getLogin());
                    test.assertSame(json, user.toJson());
                });
            });

            runner.testGroup("setLogin(String)", () ->
            {
                final Action2<String,Throwable> setLoginErrorTest = (String login, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(login), (Test test) ->
                    {
                        final GitHubUser user = GitHubUser.create();
                        test.assertThrows(() -> user.setLogin(login), expected);
                        test.assertNull(user.getLogin());
                        test.assertEqual(JSONObject.create(), user.toJson());
                    });
                };

                setLoginErrorTest.run(null, new PreConditionFailure("login cannot be null."));
                setLoginErrorTest.run("", new PreConditionFailure("login cannot be empty."));

                final Action1<String> setLoginTest = (String login) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(login), (Test test) ->
                    {
                        final GitHubUser user = GitHubUser.create();
                        final GitHubUser setLoginResult = user.setLogin(login);
                        test.assertSame(user, setLoginResult);
                        test.assertEqual(login, user.getLogin());
                        test.assertEqual(
                            JSONObject.create()
                                .setString("login", login),
                            user.toJson());
                    });
                };

                setLoginTest.run("fake-login");
                setLoginTest.run("octocat");
            });
        });
    }
}
