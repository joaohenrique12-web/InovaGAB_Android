package br.com.aguiabranca.inovagab.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import br.com.aguiabranca.inovagab.data.model.PerfilUsuario
import br.com.aguiabranca.inovagab.navigation.Screen
import br.com.aguiabranca.inovagab.ui.theme.InovaColors
import br.com.aguiabranca.inovagab.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel()
) {
    val uiState by loginViewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }
    var perfilSelecionado by remember { mutableStateOf<PerfilUsuario?>(null) }

    // Navega após login bem-sucedido
    LaunchedEffect(uiState.rotaDestino) {
        uiState.rotaDestino?.let { rota ->
            navController.navigate(rota) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            loginViewModel.rotaConsumida()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InovaColors.NavyPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Cabeçalho
            Text(
                text = "Bem vindo(a)!",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Faça login para continuar",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Campo e-mail
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail corporativo") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = InovaColors.BlueAccent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = InovaColors.BlueAccent,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = InovaColors.BlueAccent
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo senha
            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Senha") },
                singleLine = true,
                visualTransformation = if (senhaVisivel) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(
                            imageVector = if (senhaVisivel) Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff,
                            contentDescription = "Mostrar senha",
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = InovaColors.BlueAccent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedLabelColor = InovaColors.BlueAccent,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = InovaColors.BlueAccent
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Seleção de perfil
            Text(
                text = "Selecione seu perfil",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Grid de perfis 2+1
            val perfis = listOf(
                Triple(PerfilUsuario.OPERACIONAL, "🚌", "Operador\nRegistrar ideias"),
                Triple(PerfilUsuario.GESTOR, "📊", "Gestor\nAvaliar, priorizar"),
                Triple(PerfilUsuario.LIDER, "🎯", "Líder\nEstratégia")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                perfis.take(2).forEach { (perfil, emoji, label) ->
                    PerfilChip(
                        emoji = emoji,
                        label = label,
                        selecionado = perfilSelecionado == perfil,
                        modifier = Modifier.weight(1f),
                        onClick = { perfilSelecionado = perfil }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Líder centralizado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                perfis.last().let { (perfil, emoji, label) ->
                    PerfilChip(
                        emoji = emoji,
                        label = label,
                        selecionado = perfilSelecionado == perfil,
                        modifier = Modifier.width(140.dp),
                        onClick = { perfilSelecionado = perfil }
                    )
                }
            }

            // Mensagem de erro
            if (uiState.erro != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = uiState.erro!!,
                    color = InovaColors.RedDanger,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Entrar
            Button(
                onClick = {
                    loginViewModel.login(email.trim(), senha, perfilSelecionado)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.carregando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = InovaColors.BlueAccent,
                    disabledContainerColor = InovaColors.BlueAccent.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.carregando) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Entrar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PerfilChip(
    emoji: String,
    label: String,
    selecionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(80.dp)
            .background(
                color = if (selecionado) InovaColors.BlueAccent.copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (selecionado) 2.dp else 1.dp,
                color = if (selecionado) InovaColors.BlueAccent
                else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = if (selecionado) Color.White else Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}
