package at.shockbytes.plugin.view

import io.reactivex.disposables.CompositeDisposable

abstract class WorkerView<T> {

    private var disposables: CompositeDisposable? = null

    val view: T by lazy {
        val v = initializeView()
        disposables = observeWorker()
        v
    }

    fun unbindView() {
        disposables?.dispose()
    }

    abstract fun initializeView(): T

    abstract fun observeWorker(): CompositeDisposable?
}