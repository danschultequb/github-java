package qub;

public interface GitHubHttpResponseTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(GitHubResponse.class, () ->
        {
            runner.testGroup("create(HttpResponse)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> GitHubResponse.create(null),
                        new PreConditionFailure("httpResponse cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final HttpResponse httpResponse = HttpResponse.create()
                        .setStatusCode(1)
                        .setReasonPhrase("hello")
                        .setHttpVersion("fake-http-version")
                        .setHeader("a", "b")
                        .setBody("fake-body");
                    final GitHubResponse gitHubResponse = GitHubResponse.create(httpResponse);
                    test.assertNotNull(gitHubResponse);
                    test.assertFalse(gitHubResponse.isDisposed());
                    test.assertEqual(1, gitHubResponse.getStatusCode());
                    test.assertEqual("hello", gitHubResponse.getReasonPhrase());
                    test.assertEqual("fake-http-version", gitHubResponse.getHttpVersion());
                    test.assertEqual("b", gitHubResponse.getHeaderValue("a").await());
                    test.assertEqual("fake-body", CharacterReadStream.create(gitHubResponse.getBody()).readEntireString().await());
                });
            });

            runner.test("dispose()", (Test test) ->
            {
                final HttpResponse httpResponse = HttpResponse.create();
                final GitHubResponse gitHubResponse = GitHubResponse.create(httpResponse);
                test.assertFalse(httpResponse.isDisposed());
                test.assertFalse(gitHubResponse.isDisposed());

                test.assertTrue(gitHubResponse.dispose().await());

                test.assertTrue(httpResponse.isDisposed());
                test.assertTrue(gitHubResponse.isDisposed());

                test.assertFalse(gitHubResponse.dispose().await());

                test.assertTrue(httpResponse.isDisposed());
                test.assertTrue(gitHubResponse.isDisposed());
            });
        });
    }
}
