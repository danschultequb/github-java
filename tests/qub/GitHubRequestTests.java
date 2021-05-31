package qub;

public interface GitHubRequestTests
{
    static void test(TestRunner runner)
    {
        runner.testGroup(GitHubRequest.class, () ->
        {
            runner.test("create()", (Test test) ->
            {
                final GitHubRequest request = GitHubRequest.create();
                test.assertNotNull(request);
                test.assertNull(request.getHttpMethod());
                test.assertNull(request.getBaseUrl());
                test.assertNull(request.getUrlPath());
                test.assertEqual(HttpHeaders.create(), request.getHeaders());
                test.assertEqual(0, request.getBodyLength());
                test.assertNull(request.getBody());
            });

            runner.testGroup("clone()", () ->
            {
                runner.test("with empty request", (Test test) ->
                {
                    final GitHubRequest request = GitHubRequest.create();
                    final GitHubRequest clonedRequest = request.clone();
                    test.assertNotNull(clonedRequest);
                    test.assertNotSame(request, clonedRequest);
                    test.assertEqual(request.getHttpMethod(), clonedRequest.getHttpMethod());
                    test.assertEqual(request.getBaseUrl(), clonedRequest.getBaseUrl());
                    test.assertEqual(request.getUrlPath(), clonedRequest.getUrlPath());
                    test.assertEqual(request.getHeaders(), clonedRequest.getHeaders());
                    test.assertSame(request.getBody(), clonedRequest.getBody());
                });

                runner.test("with full request", (Test test) ->
                {
                    final GitHubRequest request = GitHubRequest.create()
                        .setHttpMethod("apples")
                        .setBaseUrl("my.github.endpoint.com").await()
                        .setUrlPath("/path/stuff")
                        .setHeader("hello", "there")
                        .setBody("I'm a body!").await();
                    final GitHubRequest clonedRequest = request.clone();
                    test.assertNotNull(clonedRequest);
                    test.assertNotSame(request, clonedRequest);
                    test.assertEqual(request.getHttpMethod(), clonedRequest.getHttpMethod());
                    test.assertEqual(request.getBaseUrl(), clonedRequest.getBaseUrl());
                    test.assertEqual(request.getUrlPath(), clonedRequest.getUrlPath());
                    test.assertEqual(request.getHeaders(), clonedRequest.getHeaders());
                    test.assertNotSame(request.getHeaders(), clonedRequest.getHeaders());
                    test.assertSame(request.getBody(), clonedRequest.getBody());
                });
            });

            runner.testGroup("setHttpMethod(String)", () ->
            {
                final Action2<String,Throwable> setHttpMethodErrorTest = (String httpMethod, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(httpMethod), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        test.assertThrows(() -> request.setHttpMethod(httpMethod),
                            expected);
                        test.assertNull(request.getHttpMethod());
                    });
                };

                setHttpMethodErrorTest.run(null, new PreConditionFailure("httpMethod cannot be null."));
                setHttpMethodErrorTest.run("", new PreConditionFailure("httpMethod cannot be empty."));

                final Action1<String> setHttpMethodTest = (String httpMethod) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(httpMethod), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        final GitHubRequest setHttpMethodResult = request.setHttpMethod(httpMethod);
                        test.assertSame(request, setHttpMethodResult);
                        test.assertEqual(httpMethod, request.getHttpMethod());
                    });
                };

                for (final HttpMethod httpMethod : HttpMethod.values())
                {
                    setHttpMethodTest.run(httpMethod.toString());
                }
                setHttpMethodTest.run("apples");
                setHttpMethodTest.run("get");
            });

            runner.testGroup("setHttpMethod(HttpMethod)", () ->
            {
                runner.test("with null", (Test test) ->
                {
                    final GitHubRequest request = GitHubRequest.create();
                    test.assertThrows(() -> request.setHttpMethod((HttpMethod)null),
                        new PreConditionFailure("httpMethod cannot be null."));
                    test.assertNull(request.getHttpMethod());
                });

                for (final HttpMethod httpMethod : HttpMethod.values())
                {
                    runner.test("with " + httpMethod, (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        final GitHubRequest setHttpMethodResult = request.setHttpMethod(httpMethod);
                        test.assertSame(request, setHttpMethodResult);
                        test.assertEqual(httpMethod.toString(), request.getHttpMethod());
                    });
                }
            });

            runner.testGroup("setBaseUrl(String)", () ->
            {
                final Action2<String,Throwable> setBaseUrlErrorTest = (String baseUrl, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(baseUrl), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        test.assertThrows(() -> request.setBaseUrl(baseUrl).await(),
                            expected);
                        test.assertNull(request.getBaseUrl());
                    });
                };

                setBaseUrlErrorTest.run(null, new PreConditionFailure("baseUrl cannot be null."));
                setBaseUrlErrorTest.run("", new PreConditionFailure("baseUrl cannot be empty."));
                setBaseUrlErrorTest.run("hello there", new java.lang.IllegalArgumentException("A URL must begin with either a scheme (such as \"http\") or a host (such as \"www.example.com\"), not \" \"."));

                final Action1<String> setBaseUrlTest = (String baseUrl) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(baseUrl), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        final GitHubRequest setBaseUrlResult = request.setBaseUrl(baseUrl).await();
                        test.assertSame(request, setBaseUrlResult);
                        test.assertEqual(baseUrl, request.getBaseUrl().toString());
                    });
                };

                setBaseUrlTest.run("api.github.com");
                setBaseUrlTest.run("www.github.com");
                setBaseUrlTest.run("github.com");
                setBaseUrlTest.run("my.github.server.net");
                setBaseUrlTest.run("http://api.github.com");
                setBaseUrlTest.run("http://www.github.com");
                setBaseUrlTest.run("http://github.com");
                setBaseUrlTest.run("http://my.github.server.net");
                setBaseUrlTest.run("https://api.github.com");
                setBaseUrlTest.run("https://www.github.com");
                setBaseUrlTest.run("https://github.com");
                setBaseUrlTest.run("https://my.github.server.net");
                setBaseUrlTest.run("ftp://api.github.com");
                setBaseUrlTest.run("ftp://www.github.com");
                setBaseUrlTest.run("ftp://github.com");
                setBaseUrlTest.run("ftp://my.github.server.net");
            });

            runner.testGroup("setBaseUrl(URL)", () ->
            {
                final Action2<URL,Throwable> setBaseUrlErrorTest = (URL baseUrl, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(baseUrl), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        test.assertThrows(() -> request.setBaseUrl(baseUrl),
                            expected);
                        test.assertNull(request.getBaseUrl());
                    });
                };

                setBaseUrlErrorTest.run(null, new PreConditionFailure("baseUrl cannot be null."));

                final Action1<URL> setBaseUrlTest = (URL baseUrl) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(baseUrl), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        final GitHubRequest setBaseUrlResult = request.setBaseUrl(baseUrl);
                        test.assertSame(request, setBaseUrlResult);
                        test.assertEqual(baseUrl, request.getBaseUrl());
                    });
                };

                setBaseUrlTest.run(URL.parse("api.github.com").await());
                setBaseUrlTest.run(URL.parse("www.github.com").await());
                setBaseUrlTest.run(URL.parse("github.com").await());
                setBaseUrlTest.run(URL.parse("my.github.server.net").await());
                setBaseUrlTest.run(URL.parse("http://api.github.com").await());
                setBaseUrlTest.run(URL.parse("http://www.github.com").await());
                setBaseUrlTest.run(URL.parse("http://github.com").await());
                setBaseUrlTest.run(URL.parse("http://my.github.server.net").await());
                setBaseUrlTest.run(URL.parse("https://api.github.com").await());
                setBaseUrlTest.run(URL.parse("https://www.github.com").await());
                setBaseUrlTest.run(URL.parse("https://github.com").await());
                setBaseUrlTest.run(URL.parse("https://my.github.server.net").await());
                setBaseUrlTest.run(URL.parse("ftp://api.github.com").await());
                setBaseUrlTest.run(URL.parse("ftp://www.github.com").await());
                setBaseUrlTest.run(URL.parse("ftp://github.com").await());
                setBaseUrlTest.run(URL.parse("ftp://my.github.server.net").await());
            });

            runner.testGroup("setUrlPath(String)", () ->
            {
                final Action2<String,Throwable> setUrlPathErrorTest = (String urlPath, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(urlPath), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        test.assertThrows(() -> request.setUrlPath(urlPath),
                            expected);
                        test.assertNull(request.getUrlPath());
                    });
                };

                setUrlPathErrorTest.run(null, new PreConditionFailure("urlPath cannot be null."));
                setUrlPathErrorTest.run("", new PreConditionFailure("urlPath cannot be empty."));

                final Action1<String> setUrlPathTest = (String urlPath) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(urlPath), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        final GitHubRequest setUrlPathResult = request.setUrlPath(urlPath);
                        test.assertSame(request, setUrlPathResult);
                        test.assertEqual(urlPath, request.getUrlPath());
                    });
                };

                setUrlPathTest.run("/");
                setUrlPathTest.run("/hello");
                setUrlPathTest.run("hello");
                setUrlPathTest.run("hello there");
            });

            runner.testGroup("getHeaderValue(String)", () ->
            {
                final Action2<String,Throwable> getHeaderValueErrorTest = (String headerName, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Iterable.create(headerName).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        test.assertThrows(() -> request.getHeaderValue(headerName).await(),
                            expected);
                        test.assertEqual(HttpHeaders.create(), request.getHeaders());
                    });
                };

                getHeaderValueErrorTest.run(null, new PreConditionFailure("headerName cannot be null."));
                getHeaderValueErrorTest.run("", new PreConditionFailure("headerName cannot be empty."));
                getHeaderValueErrorTest.run("hello", new NotFoundException("hello"));

                final Action3<HttpHeaders,String,String> getHeaderValueTest = (HttpHeaders headers, String headerName, String expected) ->
                {
                    runner.test("with " + English.andList(headers, Strings.escapeAndQuote(headerName)), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create()
                            .setHeaders(headers);
                        test.assertEqual(expected, request.getHeaderValue(headerName).await());
                    });
                };

                getHeaderValueTest.run(
                    HttpHeaders.create()
                        .set("hello", ""),
                    "hello",
                    "");
                getHeaderValueTest.run(
                    HttpHeaders.create()
                        .set("hello", ""),
                    "HELLO",
                    "");
                getHeaderValueTest.run(
                    HttpHeaders.create()
                        .set("hello", "there"),
                    "hello",
                    "there");
                getHeaderValueTest.run(
                    HttpHeaders.create()
                        .set("hello", "there"),
                    "HELLO",
                    "there");
            });

            runner.testGroup("setHeader(String,String)", () ->
            {
                final Action3<String,String,Throwable> setHeaderErrorTest = (String headerName, String headerValue, Throwable expected) ->
                {
                    runner.test("with " + English.andList(Iterable.create(headerName, headerValue).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        test.assertThrows(() -> request.setHeader(headerName, headerValue),
                            expected);
                        test.assertEqual(HttpHeaders.create(), request.getHeaders());
                    });
                };

                setHeaderErrorTest.run(null, "there", new PreConditionFailure("headerName cannot be null."));
                setHeaderErrorTest.run("", "there", new PreConditionFailure("headerName cannot be empty."));
                setHeaderErrorTest.run("hello", null, new PreConditionFailure("headerValue cannot be null."));

                final Action2<String,String> setHeaderTest = (String headerName, String headerValue) ->
                {
                    runner.test("with " + English.andList(Iterable.create(headerName, headerValue).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        final GitHubRequest setHeaderResult = request.setHeader(headerName, headerValue);
                        test.assertSame(request, setHeaderResult);
                        test.assertEqual(headerValue, request.getHeaderValue(headerName).await());
                    });
                };

                setHeaderTest.run("hello", "");
                setHeaderTest.run("hello", "there");
            });

            runner.testGroup("setHeader(String,int)", () ->
            {
                final Action2<String,Integer> setHeaderTest = (String headerName, Integer headerValue) ->
                {
                    runner.test("with " + English.andList(Iterable.create(headerName, headerValue).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        final GitHubRequest setHeaderResult = request.setHeader(headerName, headerValue.intValue());
                        test.assertSame(request, setHeaderResult);
                        test.assertEqual(headerValue.toString(), request.getHeaderValue(headerName).await());
                    });
                };

                setHeaderTest.run("hello", 0);
                setHeaderTest.run("hello", 1);
                setHeaderTest.run("hello", 123512341);
            });

            runner.testGroup("setHeader(String,long)", () ->
            {
                final Action2<String,Long> setHeaderTest = (String headerName, Long headerValue) ->
                {
                    runner.test("with " + English.andList(Iterable.create(headerName, headerValue).map(Strings::escapeAndQuote)), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        final GitHubRequest setHeaderResult = request.setHeader(headerName, headerValue.longValue());
                        test.assertSame(request, setHeaderResult);
                        test.assertEqual(headerValue.toString(), request.getHeaderValue(headerName).await());
                    });
                };

                setHeaderTest.run("hello", 0L);
                setHeaderTest.run("hello", 1L);
                setHeaderTest.run("hello", 123512341L);
            });

            runner.testGroup("setAuthorizationHeader(String)", () ->
            {
                final Action2<String,Throwable> setHeaderErrorTest = (String token, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(token), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        test.assertThrows(() -> request.setAuthorizationHeader(token),
                            expected);
                        test.assertEqual(HttpHeaders.create(), request.getHeaders());
                    });
                };

                setHeaderErrorTest.run(null, new PreConditionFailure("token cannot be null."));
                setHeaderErrorTest.run("", new PreConditionFailure("token cannot be empty."));

                final Action1<String> setHeaderTest = (String token) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(token), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        final GitHubRequest setHeaderResult = request.setAuthorizationHeader(token);
                        test.assertSame(request, setHeaderResult);
                        test.assertEqual("token " + token, request.getHeaderValue("Authorization").await());
                    });
                };

                setHeaderTest.run("hello");
                setHeaderTest.run("there");
            });

            runner.testGroup("setBody(long,ByteReadStream)", () ->
            {
                final Action4<String,Long,ByteReadStream,Throwable> setBodyErrorTest = (String testName, Long bodyLength, ByteReadStream body, Throwable expected) ->
                {
                    runner.test(testName, (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        test.assertThrows(() -> request.setBody(bodyLength, body),
                            expected);
                        test.assertEqual(0, request.getBodyLength());
                        test.assertNull(request.getBody());
                    });
                };

                setBodyErrorTest.run("with negative bodyLength",
                    -1L,
                    InMemoryByteStream.create(),
                    new PreConditionFailure("bodyLength (-1) must be greater than or equal to 0."));
                setBodyErrorTest.run("with null body",
                    0L,
                    null,
                    new PreConditionFailure("body cannot be null."));

                final Action3<String,Long,ByteReadStream> setBodyTest = (String testName, Long bodyLength, ByteReadStream body) ->
                {
                    runner.test(testName, (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        final GitHubRequest setBodyResult = request.setBody(bodyLength, body);
                        test.assertSame(request, setBodyResult);
                        test.assertEqual(bodyLength, request.getBodyLength());
                        test.assertSame(body, request.getBody());
                    });
                };

                setBodyTest.run("with 0 bodyLength", 0L, InMemoryByteStream.create());
                setBodyTest.run("with non-zero bodyLength", 10L, InMemoryByteStream.create());
            });

            runner.testGroup("setBody(String)", () ->
            {
                final Action2<String,Throwable> setBodyErrorTest = (String body, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(body), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        test.assertThrows(() -> request.setBody(body).await(),
                            expected);
                        test.assertEqual(0, request.getBodyLength());
                    });
                };

                setBodyErrorTest.run(null, new PreConditionFailure("body cannot be null."));

                final Action1<String> setBodyTest = (String body) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(body), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        final GitHubRequest setBodyResult = request.setBody(body).await();
                        test.assertSame(request, setBodyResult);
                        test.assertEqual(CharacterEncoding.UTF_8.encodeCharacters(body).await().length, request.getBodyLength());
                        test.assertEqual(body, CharacterReadStream.create(request.getBody()).readEntireString().await());
                    });
                };

                setBodyTest.run("");
                setBodyTest.run("hello");
                setBodyTest.run("{}");
            });

            runner.testGroup("setBody(JSONObject)", () ->
            {
                final Action2<JSONObject,Throwable> setBodyErrorTest = (JSONObject body, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(body), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        test.assertThrows(() -> request.setBody(body).await(),
                            expected);
                        test.assertEqual(0, request.getBodyLength());
                    });
                };

                setBodyErrorTest.run(null, new PreConditionFailure("bodyJson cannot be null."));

                final Action1<JSONObject> setBodyTest = (JSONObject body) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(body), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        final GitHubRequest setBodyResult = request.setBody(body).await();
                        test.assertSame(request, setBodyResult);
                        test.assertEqual(CharacterEncoding.UTF_8.encodeCharacters(body.toString()).await().length, request.getBodyLength());
                        test.assertEqual(body.toString(), CharacterReadStream.create(request.getBody()).readEntireString().await());
                    });
                };

                setBodyTest.run(JSONObject.create());
                setBodyTest.run(JSONObject.create().setString("hello", "there"));
            });

            runner.testGroup("setBody(JSONArray)", () ->
            {
                final Action2<JSONArray,Throwable> setBodyErrorTest = (JSONArray body, Throwable expected) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(body), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        test.assertThrows(() -> request.setBody(body).await(),
                            expected);
                        test.assertEqual(0, request.getBodyLength());
                    });
                };

                setBodyErrorTest.run(null, new PreConditionFailure("bodyJson cannot be null."));

                final Action1<JSONArray> setBodyTest = (JSONArray body) ->
                {
                    runner.test("with " + Strings.escapeAndQuote(body), (Test test) ->
                    {
                        final GitHubRequest request = GitHubRequest.create();
                        final GitHubRequest setBodyResult = request.setBody(body).await();
                        test.assertSame(request, setBodyResult);
                        test.assertEqual(CharacterEncoding.UTF_8.encodeCharacters(body.toString()).await().length, request.getBodyLength());
                        test.assertEqual(body.toString(), CharacterReadStream.create(request.getBody()).readEntireString().await());
                    });
                };

                setBodyTest.run(JSONArray.create());
                setBodyTest.run(JSONArray.create().addString("hello").addString("there"));
            });
        });
    }
}
