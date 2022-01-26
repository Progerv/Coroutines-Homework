package otus.homework.coroutines

import kotlinx.coroutines.*
import java.net.SocketTimeoutException

class CatsPresenter(
    private val catsService: CatsService
) {

    private var _catsView: ICatsView? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main + CoroutineName("CatsCoroutine"))

    fun onInitComplete() {
        presenterScope.launch {
            try {
                val fact = async(Dispatchers.IO) { catsService.getCatFact() }
                val image = async(Dispatchers.IO) { catsService.getCatImage() }
                val cat = Cat(fact.await(), image.await())
                _catsView?.populate(cat)
            } catch (e: Exception) {
                when (e) {
                    is SocketTimeoutException -> {
                        _catsView?.toasts("Не удалось получить ответ от сервера")
                    }
                    else -> {
                        CrashMonitor.trackWarning(e.message!!)
                        _catsView?.toasts(e.message!!)
                    }
                }
            }
        }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        _catsView = null
        presenterScope?.cancel()
    }
}