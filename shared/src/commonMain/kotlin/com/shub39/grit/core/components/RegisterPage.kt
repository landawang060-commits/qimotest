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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import grit.shared.generated.resources.register
import grit.shared.generated.resources.username
import grit.shared.generated.resources.email
import grit.shared.generated.resources.password
import grit.shared.generated.resources.confirm_password
import grit.shared.generated.resources.sign_up
import grit.shared.generated.resources.already_have_account
import grit.shared.generated.resources.sign_in
import grit.shared.generated.resources.hide
import grit.shared.generated.resources.show

@Composable
fun RegisterPage(
    onRegister: (username: String, email: String, password: String) -> Unit,
    onSignIn: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val isFormValid = username.isNotEmpty() && 
                      email.isNotEmpty() && 
                      password.isNotEmpty() && 
                      password == confirmPassword

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
            text = stringResource(Res.string.register),
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
                    Icon(Icons.Outlined.Person, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(Res.string.email)) },
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
                    imeAction = ImeAction.Next,
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(Res.string.confirm_password)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = null)
                },
                trailingIcon = {
                    TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Text(stringResource(if (confirmPasswordVisible) Res.string.hide else Res.string.show))
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (isFormValid) onRegister(username, email, password)
                    }
                ),
                isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                supportingText = {
                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text("密码不匹配")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
            )

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
                    onRegister(username, email, password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid && !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(Res.string.sign_up))
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(Res.string.already_have_account))
                TextButton(
                    onClick = onSignIn,
                    enabled = !isLoading,
                ) {
                    Text(stringResource(Res.string.sign_in))
                }
            }
        }
    }
}