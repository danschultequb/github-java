package qub;

public interface GetAuthenticatedUserResponseTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(GetAuthenticatedUserResponse.class, () ->
        {
            runner.testGroup("create(HttpResponse)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    test.assertThrows(() -> GetAuthenticatedUserResponse.create(null),
                        new PreConditionFailure("httpResponse cannot be null."));
                });

                runner.test("with non-null", (Test test) ->
                {
                    final GetAuthenticatedUserResponse response = GetAuthenticatedUserResponse.create(MutableHttpResponse.create());
                    test.assertNotNull(response);
                });
            });
        });
    }
}
