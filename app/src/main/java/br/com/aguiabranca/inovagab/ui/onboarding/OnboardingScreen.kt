package br.com.aguiabranca.inovagab.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import br.com.aguiabranca.inovagab.navigation.Screen
import br.com.aguiabranca.inovagab.ui.theme.InovaColors

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val description: String
)

private val pages = listOf(
    OnboardingPage(
        emoji = "☀️",
        title = "Sua ideia move o Grupo",
        subtitle = "Do chão de fábrica à diretoria",
        description = "Registra na rota\nProblema em 30 segundos, sem parar"
    ),
    OnboardingPage(
        emoji = "📊",
        title = "Gestor avalia e aprova",
        subtitle = "Com apoio dos votos da equipe",
        description = "Cada ideia é avaliada por gestores\ne apoiada pelos votos dos colegas"
    ),
    OnboardingPage(
        emoji = "🎯",
        title = "Líder vê o impacto",
        subtitle = "ROI real no dashboard executivo",
        description = "Acompanhe em tempo real\no impacto financeiro das inovações"
    )
)

@Composable
fun OnboardingScreen(navController: NavController) {
    var currentPage by remember { mutableStateOf(0) }
    val page = pages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InovaColors.NavyPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emoji grande
            Text(
                text = page.emoji,
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Título
            Text(
                text = page.title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo
            Text(
                text = page.subtitle,
                color = InovaColors.BlueAccent,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Passos numerados
            pages.forEachIndexed { index, p ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Número
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = if (index == currentPage) InovaColors.BlueAccent
                                else Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = pages[index].title,
                            color = if (index == currentPage) Color.White
                            else Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = pages[index].subtitle,
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Indicadores de página
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pages.indices.forEach { index ->
                    val color by animateColorAsState(
                        targetValue = if (index == currentPage) InovaColors.BlueAccent
                        else Color.White.copy(alpha = 0.3f),
                        label = "dot_color"
                    )
                    Box(
                        modifier = Modifier
                            .size(if (index == currentPage) 10.dp else 8.dp)
                            .background(color = color, shape = CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botão principal
            Button(
                onClick = {
                    if (currentPage < pages.lastIndex) {
                        currentPage++
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = InovaColors.BlueAccent
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (currentPage < pages.lastIndex) "Próximo" else "Começar agora",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Skip (apenas nas páginas 1 e 2)
            if (currentPage < pages.lastIndex) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                ) {
                    Text(
                        text = "Pular",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}