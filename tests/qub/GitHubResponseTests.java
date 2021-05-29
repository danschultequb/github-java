package qub;

public interface GitHubResponseTests
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
                    final GitHubResponse response = GitHubResponse.create(MutableHttpResponse.create());
                    test.assertNotNull(response);
                    test.assertFalse(response.isDisposed());
                    test.assertFalse(response.isErrorResponse());
                });
            });

            GitHubResponseTests.test(runner, GitHubResponse::create);
        });
    }

    static void test(TestRunner runner, Function1<HttpResponse,? extends GitHubResponse> creator)
    {
        PreCondition.assertNotNull(runner, "runner");
        PreCondition.assertNotNull(creator, "creator");

        runner.testGroup("getHttpVersion()", () ->
        {
            final Action1<String> getHttpVersionTest = (String httpVersion) ->
            {
                runner.test("with " + Strings.escape(httpVersion), (Test test) ->
                {
                    try (final GitHubResponse gitHubResponse = creator.run(HttpResponse.create().setHttpVersion(httpVersion)))
                    {
                        test.assertEqual(httpVersion, gitHubResponse.getHttpVersion());
                    }
                });
            };

            getHttpVersionTest.run("HTTP/1.1");
            getHttpVersionTest.run("HTTP/2.0");
            getHttpVersionTest.run("apples");
        });

        runner.testGroup("getStatusCode()", () ->
        {
            final Action1<Integer> getStatusCodeTest = (Integer statusCode) ->
            {
                runner.test("with " + statusCode, (Test test) ->
                {
                    try (final GitHubResponse gitHubResponse = creator.run(HttpResponse.create().setStatusCode(statusCode)))
                    {
                        test.assertEqual(statusCode, gitHubResponse.getStatusCode());
                    }
                });
            };

            getStatusCodeTest.run(0);
            getStatusCodeTest.run(1);
            getStatusCodeTest.run(200);
            getStatusCodeTest.run(404);
        });

        runner.testGroup("getReasonPhrase()", () ->
        {
            final Action1<String> getReasonPhraseTest = (String reasonPhrase) ->
            {
                runner.test("with " + Strings.escape(reasonPhrase), (Test test) ->
                {
                    try (final GitHubResponse gitHubResponse = creator.run(HttpResponse.create().setReasonPhrase(reasonPhrase)))
                    {
                        test.assertEqual(reasonPhrase, gitHubResponse.getReasonPhrase());
                    }
                });
            };

            getReasonPhraseTest.run("OK");
            getReasonPhraseTest.run("Not Found");
            getReasonPhraseTest.run("apples");
        });
    }
}
