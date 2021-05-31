package qub;

public interface GetAuthenticatedUserResponseTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(GetAuthenticatedUserResponse.class, () ->
        {
            GitHubResponseTests.test(runner, GetAuthenticatedUserResponse::create);

            runner.testGroup("create(HttpResponse)", () ->
            {
                runner.test("with 0 status code and empty body", (Test test) ->
                {
                    final HttpResponse httpResponse = HttpResponse.create();
                    final GetAuthenticatedUserResponse response = GetAuthenticatedUserResponse.create(httpResponse);
                    test.assertNotNull(response);
                    test.assertFalse(response.isErrorResponse());
                    test.assertThrows(() -> response.getAuthenticatedUser().await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                    test.assertThrows(() -> response.getErrorResponse().await(),
                        new PreConditionFailure("this.isErrorResponse() cannot be false."));
                });

                runner.test("with 200 status code and empty body", (Test test) ->
                {
                    final HttpResponse httpResponse = HttpResponse.create()
                        .setStatusCode(200);
                    final GetAuthenticatedUserResponse response = GetAuthenticatedUserResponse.create(httpResponse);
                    test.assertNotNull(response);
                    test.assertFalse(response.isErrorResponse());
                    test.assertThrows(() -> response.getAuthenticatedUser().await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                    test.assertThrows(() -> response.getErrorResponse().await(),
                        new PreConditionFailure("this.isErrorResponse() cannot be false."));
                });

                runner.test("with 200 status code and empty JSON-object body", (Test test) ->
                {
                    final HttpResponse httpResponse = HttpResponse.create()
                        .setStatusCode(200)
                        .setBody(JSONObject.create().toString());
                    final GetAuthenticatedUserResponse response = GetAuthenticatedUserResponse.create(httpResponse);
                    test.assertNotNull(response);
                    test.assertFalse(response.isErrorResponse());
                    final GitHubUser user = response.getAuthenticatedUser().await();
                    test.assertNotNull(user);
                    test.assertNull(user.getLogin());
                    test.assertThrows(() -> response.getErrorResponse().await(),
                        new PreConditionFailure("this.isErrorResponse() cannot be false."));
                });

                runner.test("with 404 status code and empty body", (Test test) ->
                {
                    final HttpResponse httpResponse = HttpResponse.create()
                        .setStatusCode(404);
                    final GetAuthenticatedUserResponse response = GetAuthenticatedUserResponse.create(httpResponse);
                    test.assertNotNull(response);
                    test.assertTrue(response.isErrorResponse());
                    test.assertThrows(() -> response.getAuthenticatedUser().await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                    test.assertThrows(() -> response.getErrorResponse().await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });

                runner.test("with 404 status code and empty JSON-object body", (Test test) ->
                {
                    final HttpResponse httpResponse = HttpResponse.create()
                        .setStatusCode(404)
                        .setBody(JSONObject.create().toString());
                    final GetAuthenticatedUserResponse response = GetAuthenticatedUserResponse.create(httpResponse);
                    test.assertNotNull(response);
                    test.assertTrue(response.isErrorResponse());
                    test.assertThrows(() -> response.getAuthenticatedUser().await(),
                        new PreConditionFailure("errorResponse.getMessage() cannot be null."));
                    test.assertThrows(() -> response.getErrorResponse().await(),
                        new ParseException("Missing object left curly bracket ('{')."));
                });
            });
        });
    }
}
