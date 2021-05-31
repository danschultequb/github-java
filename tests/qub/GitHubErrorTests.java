package qub;

public interface GitHubErrorTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(GitHubError.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final GitHubError error = GitHubError.create();
                test.assertNotNull(error);
                test.assertNull(error.getMessage());
                test.assertNull(error.getCode());
                test.assertNull(error.getField());
                test.assertNull(error.getResource());
            });

            runner.testGroup("create(JSONObject)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> GitHubError.create(null),
                        new PreConditionFailure("json cannot be null."));
                });

                runner.test("with empty", (Test test) ->
                {
                    final GitHubError error = GitHubError.create(JSONObject.create());
                    test.assertNotNull(error);
                    test.assertNull(error.getMessage());
                    test.assertNull(error.getCode());
                    test.assertNull(error.getField());
                    test.assertNull(error.getResource());
                });

                runner.test("with non-empty", (Test test) ->
                {
                    final GitHubError error = GitHubError.create(JSONObject.create()
                        .setString("message", "fake-message")
                        .setString("code", "fake-code")
                        .setString("field", "fake-field")
                        .setString("resource", "fake-resource"));
                    test.assertNotNull(error);
                    test.assertEqual("fake-message", error.getMessage());
                    test.assertEqual("fake-code", error.getCode());
                    test.assertEqual("fake-field", error.getField());
                    test.assertEqual("fake-resource", error.getResource());
                });
            });
        });
    }
}
