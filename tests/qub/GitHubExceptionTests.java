package qub;

public interface GitHubExceptionTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(GitHubException.class, () ->
        {
            runner.testGroup("constructor(int,String,String,Iterable<GitHubError>)", () ->
            {
                final Action3<Integer,GitHubErrorResponse,Throwable> constructorErrorTest = (Integer statusCode, GitHubErrorResponse errorResponse, Throwable expected) ->
                {
                    runner.test("with " + English.andList(statusCode, errorResponse), (Test test) ->
                    {
                        test.assertThrows(() -> new GitHubException(statusCode, errorResponse),
                            expected);
                    });
                };

                constructorErrorTest.run(401, null, new PreConditionFailure("errorResponse cannot be null."));
                constructorErrorTest.run(401, GitHubErrorResponse.create(), new PreConditionFailure("errorResponse.getMessage() cannot be null."));

                final Action2<Integer,GitHubErrorResponse> constructorTest = (Integer statusCode, GitHubErrorResponse errorResponse) ->
                {
                    runner.test("with " + English.andList(statusCode, errorResponse), (Test test) ->
                    {
                        final GitHubException e = new GitHubException(statusCode, errorResponse);
                        test.assertNotNull(e);
                        test.assertEqual(statusCode, e.getStatusCode());
                        test.assertSame(errorResponse, e.getErrorResponse());
                        test.assertEqual(errorResponse.getErrors(), e.getErrors());
                        test.assertEqual(errorResponse.getMessage(), e.getMessage());
                        test.assertEqual(errorResponse.getDocumentationUrl(), e.getDocumentationUrl());
                        test.assertEqual(errorResponse.getErrors(), e.getErrors());
                    });
                };

                constructorTest.run(0, GitHubErrorResponse.create()
                    .setMessage("hello"));
                constructorTest.run(1, GitHubErrorResponse.create()
                    .setMessage("fake message")
                    .setDocumentationUrl("fake.documentation.url"));
                constructorTest.run(2, GitHubErrorResponse.create()
                    .setMessage("fake message 2")
                    .setErrors(Iterable.create(
                        GitHubError.create()
                            .setMessage("error message")
                            .setField("fake field")
                            .setCode("fake code")
                            .setResource("fake resource"))));
            });

            runner.testGroup("toString()", () ->
            {
                final Action2<GitHubException,String> toStringTest = (GitHubException exception, String expected) ->
                {
                    runner.test("with " + exception, (Test test) ->
                    {
                        test.assertEqual(expected, exception.toString());
                    });
                };

                toStringTest.run(
                    new GitHubException(308, GitHubErrorResponse.create()
                        .setMessage("hello")),
                    "qub.GitHubException: hello {\"statusCode\":308,\"errorResponse\":{\"message\":\"hello\"}}");
                toStringTest.run(
                    new GitHubException(401, GitHubErrorResponse.create()
                        .setMessage("Requires authentication")
                        .setDocumentationUrl("https://github.com")),
                    "qub.GitHubException: Requires authentication {\"statusCode\":401,\"errorResponse\":{\"message\":\"Requires authentication\",\"documentation_url\":\"https://github.com\"}}");
                toStringTest.run(
                    new GitHubException(123, GitHubErrorResponse.create()
                        .setMessage("fake message")
                        .setDocumentationUrl("invalid url string")
                        .setErrors(Iterable.create())),
                    "qub.GitHubException: fake message {\"statusCode\":123,\"errorResponse\":{\"message\":\"fake message\",\"documentation_url\":\"invalid url string\",\"errors\":[]}}");
                toStringTest.run(
                    new GitHubException(422, GitHubErrorResponse.create()
                        .setMessage("Repository creation failed.")
                        .setDocumentationUrl("https://docs.github.com/rest/reference/repos#create-a-repository-for-the-authenticated-user")
                        .setErrors(Iterable.create(
                            GitHubError.create()
                                .setResource("Repository")
                                .setCode("custom")
                                .setField("name")
                                .setMessage("name already exists on this account")))),
                    "qub.GitHubException: Repository creation failed. {\"statusCode\":422,\"errorResponse\":{\"message\":\"Repository creation failed.\",\"documentation_url\":\"https://docs.github.com/rest/reference/repos#create-a-repository-for-the-authenticated-user\",\"errors\":[{\"resource\":\"Repository\",\"code\":\"custom\",\"field\":\"name\",\"message\":\"name already exists on this account\"}]}}");
            });
        });
    }
}
