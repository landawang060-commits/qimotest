/*
 * Copyright (C) 2026  Shubham Gorai
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.shub39.grit.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shub39.grit.core.LocalWindowSizeClass
import com.shub39.grit.core.components.InitialLoading
import com.shub39.grit.core.components.LoginPage
import com.shub39.grit.core.components.RegisterPage
import com.shub39.grit.core.data.notification.GritNotificationManager.Companion.createNotificationChannel
import com.shub39.grit.core.theme.GritTheme
import com.shub39.grit.domain.BiometricUtils
import com.shub39.grit.domain.ImagePickerServiceImpl
import com.shub39.grit.viewmodel.LoginViewModel
import com.shub39.grit.viewmodel.MainViewModel
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : FragmentActivity() {
    private val mainViewModel: MainViewModel by viewModel()
    private val loginViewModel: LoginViewModel by viewModel()
    private val imagePickerService: ImagePickerServiceImpl by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        FileKit.init(this)

        imagePickerService.registerActivityResultLaunchers(this)

        createNotificationChannel(this)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
                val state by mainViewModel.state.collectAsStateWithLifecycle()
                val isLoggedIn by loginViewModel.isLoggedIn.collectAsStateWithLifecycle()
                val isLoading by loginViewModel.isLoading.collectAsStateWithLifecycle()
                val errorMessage by loginViewModel.errorMessage.collectAsStateWithLifecycle()

                var showContent by remember { mutableStateOf(false) }
                var showLogin by remember { mutableStateOf(true) }
                var showRegister by remember { mutableStateOf(false) }
                var isCheckingLogin by remember { mutableStateOf(true) }
                var savedUsername by remember { mutableStateOf("") }
                var savedPassword by remember { mutableStateOf("") }
                var rememberPasswordChecked by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    val credentials = loginViewModel.getSavedCredentials()
                    savedUsername = credentials.first
                    savedPassword = credentials.second
                    rememberPasswordChecked = loginViewModel.getRememberPassword()
                }

                LaunchedEffect(isLoggedIn) {
                    isCheckingLogin = false
                    if (isLoggedIn) {
                        showLogin = false
                        showRegister = false
                        showContent = true
                    } else {
                        showLogin = true
                        showRegister = false
                    }
                }

                LaunchedEffect(state.isAppUnlocked, state.isBiometricLockOn, showContent) {
                    if (showContent && state.isBiometricLockOn != null) {
                        state.isBiometricLockOn?.let {
                            when {
                                !it || state.isAppUnlocked -> { /* Already showing content */ }
                                else -> {
                                    showBiometricPrompt(
                                        onSuccess = {
                                            mainViewModel.setAppUnlocked(true)
                                        },
                                        onError = { errorCode, errString ->
                                            handleBiometricError(errorCode, errString) {
                                                /* Continue showing content */
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                GritTheme(theme = state.theme) {
                    when {
                        isCheckingLogin -> InitialLoading()
                        showRegister -> RegisterPage(
                            onRegister = { username, email, password ->
                                loginViewModel.register(username, email, password)
                            },
                            onSignIn = {
                                showRegister = false
                                showLogin = true
                            },
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onClearError = { loginViewModel.clearError() },
                        )

                        showLogin -> LoginPage(
                            onLogin = { username, password, rememberPassword ->
                                loginViewModel.login(username, password, rememberPassword)
                            },
                            onSignUp = {
                                showLogin = false
                                showRegister = true
                            },
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onClearError = { loginViewModel.clearError() },
                            savedUsername = savedUsername,
                            savedPassword = savedPassword,
                            rememberPasswordChecked = rememberPasswordChecked,
                        )

                        showContent -> App(
                            state = state,
                            onRefreshSub = { mainViewModel.updateSubscription() },
                            onDismissChangelog = { mainViewModel.dismissChangelog() },
                        )

                        else -> InitialLoading()
                    }
                }
            }
        }
    }

    private fun showBiometricPrompt(onSuccess: () -> Unit, onError: (Int, CharSequence) -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onError(errorCode, errString)
                    }
                },
            )

        val biometricUtils by inject<BiometricUtils>()
        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Lock")
                .setAllowedAuthenticators(biometricUtils.getAuthenticators())
                .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun handleBiometricError(
        errorCode: Int,
        errString: CharSequence,
        onComplete: () -> Unit,
    ) {
        when (errorCode) {
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
            BiometricPrompt.ERROR_CANCELED -> {
                Toast.makeText(this, "Biometric Authentication Failed", Toast.LENGTH_SHORT).show()
                finish()
            }

            BiometricPrompt.ERROR_NO_BIOMETRICS,
            BiometricPrompt.ERROR_HW_NOT_PRESENT,
            BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                mainViewModel.setAppUnlocked(true)
                mainViewModel.setBiometricLock(false)

                Toast.makeText(this, "Biometric Authentication Failed", Toast.LENGTH_LONG).show()
                onComplete()
            }

            else -> {
                Toast.makeText(this, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}