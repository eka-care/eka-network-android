package com.eka.network.client

import java.io.IOException

class HttpException(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
) : IOException("HTTP error: $statusCode")