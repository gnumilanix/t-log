# T-Log
[Hyperlog](https://github.com/hypertrack/hyperlog-android) rewritten in Kotlin with Room and Coroutines

# Initialization
```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        TLog.initialize(this, getDeviceId())
        TLog.setLogLevel(Log.VERBOSE)
    }
}
```

# Dispatch Logs
Since most of us use different libraries and methods to make network requests. T-Log does not include dispatching log to network. However, it provides a utility function that you can provide an implementation which handles the rest:
```kotlin
TLog.dispatchLogs { page, logs ->
    val requestFile: RequestBody = gson.toJson(logs).toRequestBody(contentType)
    val body: MultipartBody.Part = MultipartBody.Part.createFormData("logs", "logs.json", requestFile)

    logService.dispatchLogs(headers, body).isSuccessful
}
```

## Dispatch periodically with WorkManager
#### Schedule periodic work
```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ...
        scheduleLogDispatchWork()
    }

    private fun scheduleLogDispatchWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val work = PeriodicWorkRequestBuilder<LogDispatchWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(LogDispatchWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, work)
    }
}
```

#### Dispatch logs in batches
```kotlin
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.ignitetech.tlog.TLog
import com.logictechno.updater.data.log.LogService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class LogDispatchWorker(
    val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters), KoinComponent {
    private val logService: LogService by inject()
    private val gson: Gson by inject()

    override suspend fun doWork(): Result {
        TLog.i("Worker started")

        val headers = hashMapOf(
            "App-Id" to context.packageName
        )

        var retry = false

        TLog.dispatchLogs { page, logs ->
            val requestFile: RequestBody = gson.toJson(logs).toRequestBody(contentType)
            val body: MultipartBody.Part =
                MultipartBody.Part.createFormData("logs", "logs.json", requestFile)

            logService.dispatchLogs(headers, body).isSuccessful.also {
                if (!it) {
                    retry = true
                }
            }
        }

        return when (retry) {
            true -> Result.retry()
            else -> Result.success()
        }
    }

    companion object {
        private val contentType = "text/plain".toMediaType()
    }
}
```

### Installing on top of Timber
```kotlin
import android.util.Log
import com.ignitetech.tlog.TLog
import timber.log.Timber

class TLogTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val updatedTag = tag ?: ""

        when (priority) {
            Log.ERROR -> TLog.e(updatedTag, message)
            Log.WARN -> TLog.w(updatedTag, message)
        }
    }
}
```