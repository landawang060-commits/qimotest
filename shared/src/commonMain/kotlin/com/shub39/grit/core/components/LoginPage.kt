package com.shub39.grit.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import grit.shared.generated.resources.Res
import grit.shared.generated.resources.login
import grit.shared.generated.resources.password
import grit.shared.generated.resources.username
import grit.shared.generated.resources.remember_password
import grit.shared.generated.resources.sign_up
import grit.shared.generated.resources.sign_in
import grit.shared.generated.resources.or
import grit.shared.generated.resources.hide
import grit.shared.generated.resources.show


@Composable
fun LoginPage(
    onLogin: (username: String, password: String, rememberPassword: Boolean) -> Unit,
    onSignUp: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    savedUsername: String = "",
    savedPassword: String = "",
    rememberPasswordChecked: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var username by remember { mutableStateOf(savedUsername) }
    var password by remember { mutableStateOf(savedPassword) }
    var rememberPassword by remember { mutableStateOf(rememberPasswordChecked) }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            onClearError()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.login),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(Res.string.username)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Email, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(Res.string.password)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = null)
                },
                trailingIcon = {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(stringResource(if (passwordVisible) Res.string.hide else Res.string.show))
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onLogin(username, password, rememberPassword)
                    }
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Checkbox(
                    checked = rememberPassword,
                    onCheckedChange = { rememberPassword = it },
                    enabled = !isLoading,
                )
                Text(stringResource(Res.string.remember_password))
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            Button(
                onClick = {
                    focusManager.clearFocus()
                    onLogin(username, password, rememberPassword)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = username.isNotEmpty() && password.isNotEmpty() && !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(Res.string.sign_in))
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(Res.string.or))
                TextButton(
                    onClick = onSignUp,
                    enabled = !isLoading,
                ) {
                    Text(stringResource(Res.string.sign_up))
                }
            }
        }
    }
}