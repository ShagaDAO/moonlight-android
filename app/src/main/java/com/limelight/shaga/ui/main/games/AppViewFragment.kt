package com.limelight.shaga.ui.main.games

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.limelight.AppView.AppObject
import com.limelight.shaga.ui.kit.ShagaTheme

class AppViewFragment : Fragment() {

    private lateinit var viewModel: GameListViewModel

    private var callbacks: AppViewFragmentCallback? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = activity as AppViewFragmentCallback?
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[GameListViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                ShagaTheme {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    GameListScreenCompat(uiState, onItemClick = { callbacks?.onAppClick(it) })
                }
            }
        }
    }
}

interface AppViewFragmentCallback {
    fun onAppClick(app: AppObject)
}