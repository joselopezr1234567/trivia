package cl.jlopezr.trivia.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.jlopezr.trivia.splash.domain.model.SplashAction
import cl.jlopezr.trivia.splash.domain.usecase.GetSplashActionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel (

    private val getSplashActionUseCase: GetSplashActionUseCase

): ViewModel(){

    private val _action = MutableStateFlow<SplashAction?>(null)
    val action = _action.asStateFlow()

    init {
        checkAppStatus()
    }

    private fun checkAppStatus() {
        viewModelScope.launch {
            val destination = getSplashActionUseCase()
            _action.value = destination
        }
    }


}