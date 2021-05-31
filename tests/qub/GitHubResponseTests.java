package qub;

public interface GitHubResponseTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(GitHubResponse.class, () ->
        {
            GitHubResponseTests.test(runner, GitHubResponse::create);

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

        runner.testGroup("getBodyJson()", () ->
        {
            final Action2<String,Throwable> getBodyJsonErrorTest = (String body, Throwable expected) ->
            {
                runner.test("with " + Strings.escapeAndQuote(body), (Test test) ->
                {
                    final MutableHttpResponse httpResponse = HttpResponse.create();
                    if (!Strings.isNullOrEmpty(body))
                    {
                        httpResponse.setBody(body);
                    }
                    final GitHubResponse response = creator.run(httpResponse);
                    test.assertThrows(() -> response.getBodyJson().await(),
                        expected);
                });
            };

            getBodyJsonErrorTest.run(null, new ParseException("No JSON tokens found."));
            getBodyJsonErrorTest.run("", new ParseException("No JSON tokens found."));
            getBodyJsonErrorTest.run("hello", new ParseException("Unrecognized JSONToken literal: hello"));

            final Action1<JSONSegment> getBodyJsonTest = (JSONSegment body) ->
            {
                runner.test("with " + Strings.escapeAndQuote(body), (Test test) ->
                {
                    final GitHubResponse response = creator.run(HttpResponse.create()
                        .setBody(body.toString()));
                    final JSONSegment responseBody = response.getBodyJson().await();
                    test.assertEqual(body, responseBody);
                    test.assertNotSame(body, responseBody);
                });
            };

            getBodyJsonTest.run(JSONObject.create());
            getBodyJsonTest.run(JSONArray.create());
        });

        runner.testGroup("getBodyJsonObject()", () ->
        {
            final Action2<String,Throwable> getBodyJsonObjectErrorTest = (String body, Throwable expected) ->
            {
                runner.test("with " + Strings.escapeAndQuote(body), (Test test) ->
                {
                    final MutableHttpResponse httpResponse = HttpResponse.create();
                    if (!Strings.isNullOrEmpty(body))
                    {
                        httpResponse.setBody(body);
                    }
                    final GitHubResponse response = creator.run(httpResponse);
                    test.assertThrows(() -> response.getBodyJsonObject().await(),
                        expected);
                });
            };

            getBodyJsonObjectErrorTest.run(null, new ParseException("Missing object left curly bracket ('{')."));
            getBodyJsonObjectErrorTest.run("", new ParseException("Missing object left curly bracket ('{')."));
            getBodyJsonObjectErrorTest.run("hello", new ParseException("Unrecognized JSONToken literal: hello"));
            getBodyJsonObjectErrorTest.run("[]", new ParseException("Expected object left curly bracket ('{')."));

            final Action1<JSONSegment> getBodyJsonObjectTest = (JSONSegment body) ->
            {
                runner.test("with " + Strings.escapeAndQuote(body), (Test test) ->
                {
                    final GitHubResponse response = creator.run(HttpResponse.create()
                        .setBody(body.toString()));
                    final JSONObject responseBody = response.getBodyJsonObject().await();
                    test.assertEqual(body, responseBody);
                    test.assertNotSame(body, responseBody);
                });
            };

            getBodyJsonObjectTest.run(JSONObject.create());
        });

        runner.testGroup("getBodyJsonArray()", () ->
        {
            final Action2<String,Throwable> getBodyJsonArrayErrorTest = (String body, Throwable expected) ->
            {
                runner.test("with " + Strings.escapeAndQuote(body), (Test test) ->
                {
                    final MutableHttpResponse httpResponse = HttpResponse.create();
                    if (!Strings.isNullOrEmpty(body))
                    {
                        httpResponse.setBody(body);
                    }
                    final GitHubResponse response = creator.run(httpResponse);
                    test.assertThrows(() -> response.getBodyJsonArray().await(),
                        expected);
                });
            };

            getBodyJsonArrayErrorTest.run(null, new ParseException("Missing array left square bracket ('[')."));
            getBodyJsonArrayErrorTest.run("", new ParseException("Missing array left square bracket ('[')."));
            getBodyJsonArrayErrorTest.run("hello", new ParseException("Unrecognized JSONToken literal: hello"));
            getBodyJsonArrayErrorTest.run("{}", new ParseException("Expected array left square bracket ('[')."));

            final Action1<JSONSegment> getBodyJsonArrayTest = (JSONSegment body) ->
            {
                runner.test("with " + Strings.escapeAndQuote(body), (Test test) ->
                {
                    final GitHubResponse response = creator.run(HttpResponse.create()
                        .setBody(body.toString()));
                    final JSONArray responseBody = response.getBodyJsonArray().await();
                    test.assertEqual(body, responseBody);
                    test.assertNotSame(body, responseBody);
                });
            };

            getBodyJsonArrayTest.run(JSONArray.create());
        });

        runner.testGroup("throwIfErrorResponse()", () ->
        {
            final Action1<Integer> throwIfErrorResponseWithSuccessResponse = (Integer statusCode) ->
            {
                runner.test("with " + statusCode + " status code", (Test test) ->
                {
                    final GitHubResponse response = creator.run(HttpResponse.create()
                        .setStatusCode(statusCode));
                    response.throwIfErrorResponse();
                });
            };

            throwIfErrorResponseWithSuccessResponse.run(0);
            throwIfErrorResponseWithSuccessResponse.run(1);
            throwIfErrorResponseWithSuccessResponse.run(200);
            throwIfErrorResponseWithSuccessResponse.run(399);
            throwIfErrorResponseWithSuccessResponse.run(500);
            throwIfErrorResponseWithSuccessResponse.run(600);

            final Action3<Integer,String,Throwable> throwIfErrorResponseWithErrorResponse = (Integer statusCode, String responseBody, Throwable expected) ->
            {
                runner.test("with " + English.andList(statusCode, Strings.escapeAndQuote(responseBody)), (Test test) ->
                {
                    final MutableHttpResponse httpResponse = HttpResponse.create()
                        .setStatusCode(statusCode);
                    if (!Strings.isNullOrEmpty(responseBody))
                    {
                        httpResponse.setBody(responseBody);
                    }
                    final GitHubResponse response = creator.run(httpResponse);
                    test.assertThrows(response::throwIfErrorResponse, expected);
                });
            };

            throwIfErrorResponseWithErrorResponse.run(400, null, new ParseException("Missing object left curly bracket ('{')."));
            throwIfErrorResponseWithErrorResponse.run(401, "", new ParseException("Missing object left curly bracket ('{')."));
            throwIfErrorResponseWithErrorResponse.run(402, "hello", new ParseException("Unrecognized JSONToken literal: hello"));
            throwIfErrorResponseWithErrorResponse.run(403, "[]", new ParseException("Expected object left curly bracket ('{')."));
            throwIfErrorResponseWithErrorResponse.run(404, "{}", new PreConditionFailure("errorResponse.getMessage() cannot be null."));
            throwIfErrorResponseWithErrorResponse.run(
                405,
                JSONObject.create()
                    .setString("message", "fake-message")
                    .toString(),
                new GitHubException(405, GitHubErrorResponse.create()
                    .setMessage("fake-message")));
            throwIfErrorResponseWithErrorResponse.run(
                406,
                JSONObject.create()
                    .setString("message", "fake-message")
                    .setString("documentation_url", "fake-documentation-url")
                    .toString(),
                new GitHubException(406, GitHubErrorResponse.create()
                    .setMessage("fake-message")
                    .setDocumentationUrl("fake-documentation-url")));
        });
    }
}
