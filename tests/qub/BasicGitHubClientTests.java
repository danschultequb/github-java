package qub;

public interface BasicGitHubClientTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(BasicGitHubClient.class,
            (TestResources resources) -> Tuple.create(resources.getNetwork(), resources.getEnvironmentVariables()),
            (Network network, EnvironmentVariables environmentVariables) ->
        {
            GitHubClientTests.test(runner, (AccessTokenType tokenType) ->
            {
                final BasicGitHubClient gitHubClient = BasicGitHubClient.create(network);
                switch (tokenType)
                {
                    case Invalid:
                        gitHubClient.setAccessToken("fake-access-token");
                        break;

                    case Valid:
                        gitHubClient.setAccessToken(environmentVariables.get("GITHUB_TOKEN").await());
                        break;
                }
                return gitHubClient;
            });

            runner.testGroup("create(Network)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> BasicGitHubClient.create((Network)null),
                        new PreConditionFailure("network cannot be null."));
                });
            });

            runner.testGroup("create(HttpClient)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> BasicGitHubClient.create((HttpClient)null),
                        new PreConditionFailure("httpClient cannot be null."));
                });
            });
        });
    }
}
