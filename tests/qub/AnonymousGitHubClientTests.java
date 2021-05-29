package qub;

public interface AnonymousGitHubClientTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(AnonymousGitHubClient.class,
            (TestResources resources) -> Tuple.create(resources.getNetwork()),
            (Network network) ->
        {
            runner.testGroup("create(Network)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> AnonymousGitHubClient.create((Network)null),
                        new PreConditionFailure("network cannot be null."));
                });
            });

            runner.testGroup("create(HttpClient)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> AnonymousGitHubClient.create((HttpClient)null),
                        new PreConditionFailure("httpClient cannot be null."));
                });
            });
        });
    }
}
