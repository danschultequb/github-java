package qub;

public interface GetRepositoryResponseTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(GetRepositoryResponse.class, () ->
        {
            GitHubResponseTests.test(runner, GetRepositoryResponse::create);

            runner.testGroup("create(HttpResponse)", () ->
            {
                runner.test("with 0 status code and empty body", (Test test) ->
                {
                    final HttpResponse httpResponse = HttpResponse.create();
                    final GetRepositoryResponse response = GetRepositoryResponse.create(httpResponse);
                    test.assertNotNull(response);
                    test.assertFalse(response.isErrorResponse());
                    test.assertThrows(() -> response.getRepository().await(),
                        new ParseException("No JSON tokens found."));
                    test.assertThrows(() -> response.getErrorResponse().await(),
                        new PreConditionFailure("this.isErrorResponse() cannot be false."));
                });

                runner.test("with 200 status code and empty body", (Test test) ->
                {
                    final HttpResponse httpResponse = HttpResponse.create()
                        .setStatusCode(200);
                    final GetRepositoryResponse response = GetRepositoryResponse.create(httpResponse);
                    test.assertNotNull(response);
                    test.assertFalse(response.isErrorResponse());
                    test.assertThrows(() -> response.getRepository().await(),
                        new ParseException("No JSON tokens found."));
                    test.assertThrows(() -> response.getErrorResponse().await(),
                        new PreConditionFailure("this.isErrorResponse() cannot be false."));
                });

                runner.test("with 200 status code and empty JSON-object body", (Test test) ->
                {
                    final HttpResponse httpResponse = HttpResponse.create()
                        .setStatusCode(200)
                        .setBody(JSONObject.create().toString());
                    final GetRepositoryResponse response = GetRepositoryResponse.create(httpResponse);
                    test.assertNotNull(response);
                    test.assertFalse(response.isErrorResponse());
                    final GitHubRepository repository = response.getRepository().await();
                    test.assertNotNull(repository);
                    test.assertNull(repository.getOwner());
                    test.assertNull(repository.getName());
                    test.assertNull(repository.getFullName());
                    test.assertThrows(() -> response.getErrorResponse().await(),
                        new PreConditionFailure("this.isErrorResponse() cannot be false."));
                });

                runner.test("with 404 status code and empty body", (Test test) ->
                {
                    final HttpResponse httpResponse = HttpResponse.create()
                        .setStatusCode(404);
                    final GetRepositoryResponse response = GetRepositoryResponse.create(httpResponse);
                    test.assertNotNull(response);
                    test.assertTrue(response.isErrorResponse());
                    test.assertThrows(() -> response.getRepository().await(),
                        new ParseException("No JSON tokens found."));
                    test.assertThrows(() -> response.getErrorResponse().await(),
                        new ParseException("No JSON tokens found."));
                });

                runner.test("with 404 status code and empty JSON-object body", (Test test) ->
                {
                    final HttpResponse httpResponse = HttpResponse.create()
                        .setStatusCode(404)
                        .setBody(JSONObject.create().toString());
                    final GetRepositoryResponse response = GetRepositoryResponse.create(httpResponse);
                    test.assertNotNull(response);
                    test.assertTrue(response.isErrorResponse());
                    test.assertThrows(() -> response.getRepository().await(),
                        new PreConditionFailure("errorResponse.getMessage() cannot be null."));
                    test.assertEqual(GitHubErrorResponse.create(), response.getErrorResponse().await());
                });
            });
        });
    }
}
