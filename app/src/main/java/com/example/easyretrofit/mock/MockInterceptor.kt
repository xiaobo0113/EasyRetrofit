package com.example.easyretrofit.mock

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody

class MockInterceptor : Interceptor {

    companion object {
        private const val DATA_JSON = """
                {
                    "code": 200,
                    "data": [
                        0,
                        1
                    ],
                    "message": "success"
                }
            """
        private const val NULL_JSON = """
                {
                    "code": 200,
                    "data": null,
                    "message": "success"
                }
            """
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        when (chain.request().url().pathSegments().last()) {
            "list" -> {
                Thread.sleep(1500L)

                val body = ResponseBody.create(MediaType.parse("application/json"), DATA_JSON)
                return Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .body(body)
                    .message("OK")
                    .build()
            }

            else -> {
                Thread.sleep(3000L)
                return chain.proceed(chain.request())
            }
        }
    }

}