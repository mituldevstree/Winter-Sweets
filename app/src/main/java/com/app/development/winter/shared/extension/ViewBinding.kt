package com.app.development.winter.shared.extension

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

inline fun <reified T : ViewBinding> Fragment.viewBinding(
    noinline onViewDestroyed: (T) -> Unit = { },
): ViewBindingDelegate<T> {
    return ViewBindingDelegate(this, T::class, onViewDestroyed)
}

class ViewBindingDelegate<T : ViewBinding> @PublishedApi internal constructor(
    private val fragment: Fragment,
    private val viewBindingClass: KClass<T>,
    private val onViewDestroyed: (T) -> Unit,
) : ReadOnlyProperty<Fragment, T> {

    private var binding: T? = null

    init {
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            val viewLifecycleOwnerLiveDataObserver =
                Observer<LifecycleOwner?> { owner ->
                    val viewLifecycleOwner = owner ?: return@Observer

                    viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                        private val mainHandler = Handler(Looper.getMainLooper())

                        @MainThread
                        override fun onDestroy(owner: LifecycleOwner) {
                            owner.lifecycle.removeObserver(this)
                            binding?.let { onViewDestroyed(it) }
                            mainHandler.post { binding = null }
                        }
                    })
                }

            override fun onCreate(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.observeForever(
                    viewLifecycleOwnerLiveDataObserver
                )
            }

            override fun onDestroy(owner: LifecycleOwner) {
                fragment.viewLifecycleOwnerLiveData.removeObserver(
                    viewLifecycleOwnerLiveDataObserver
                )
            }
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val binding = binding
        if (binding != null) {
            return binding
        }

        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException(
                "Should not attempt to get bindings when Fragment views are destroyed."
            )
        }

        return obtainBinding(thisRef.requireView()).also { this.binding = it }
    }

    private fun obtainBinding(view: View): T {
        return viewBindingClass.bind(view)
            .also { binding = it }
    }
}

fun <T : ViewBinding> KClass<T>.bind(rootView: View): T {
    val bindMethod = java.getBindMethod()
    @Suppress("UNCHECKED_CAST")
    return bindMethod.invoke(null, rootView) as T
}

private val bindMethodsCache = mutableMapOf<Class<out ViewBinding>, Method>()

private fun Class<out ViewBinding>.getBindMethod(): Method {
    return bindMethodsCache.getOrPut(this) {
        getDeclaredMethod("bind", View::class.java)
    }
}
