# T-Log
[Hyperlog](https://github.com/hypertrack/hyperlog-android) rewritten in Kotlin with Room and Coroutines

Since most of us use different libraries and methods to make network requests. Hence, T-Log does not include dispatching log to network. However, it provides a utility function that you can provide an implementation which handles the rest:
```
TLog.dispatchLogs { page, logs ->
    val requestFile: RequestBody = gson.toJson(logs).toRequestBody(contentType)
    val body: MultipartBody.Part = MultipartBody.Part.createFormData("logs", "logs.json", requestFile)

    logService.dispatchLogs(headers, body).isSuccessful
}
```