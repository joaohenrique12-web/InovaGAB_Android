// ui/components/Components.kt
package br.com.aguiabranca.inovagab.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.aguiabranca.inovagab.data.model.*
import br.com.aguiabranca.inovagab.ui.theme.InovaColors

// ─────────────────────────────────────────────────────────
// CardSugestaoIA
// ─────────────────────────────────────────────────────────

@Composable
fun CardSugestaoIA(sugestao: String) {
    val estaProcessando = sugestao.contains("Processando") || sugestao.isBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = InovaColors.PurpleAI.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, InovaColors.PurpleAI.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = InovaColors.PurpleAI,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ANÁLISE DA IA",
                    color = InovaColors.PurpleAI,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (estaProcessando) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = InovaColors.PurpleAI,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Gerando recomendações técnicas...",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            } else {
                Text(
                    text = sugestao,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────
// StatusBadge
// ─────────────────────────────────────────────────────────

@Composable
fun StatusBadge(status: StatusIdeia) {
    val (label, bg, text) = when (status) {
        StatusIdeia.ENVIADA     -> Triple("Enviada",     InovaColors.BlueAccent.copy(alpha = 0.1f), InovaColors.BlueAccent)
        StatusIdeia.EM_ANALISE  -> Triple("Em análise",  InovaColors.AmberWarning.copy(alpha = 0.1f), InovaColors.AmberWarning)
        StatusIdeia.APROVADA    -> Triple("Aprovada",    InovaColors.GreenSuccess.copy(alpha = 0.1f), InovaColors.GreenSuccess)
        StatusIdeia.REPROVADA   -> Triple("Reprovada",   InovaColors.RedDanger.copy(alpha = 0.1f), InovaColors.RedDanger)
    }
    Surface(
        color    = bg,
        shape    = RoundedCornerShape(6.dp),
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text       = label,
            color      = text,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val enum = runCatching { StatusIdeia.valueOf(status) }.getOrDefault(StatusIdeia.ENVIADA)
    StatusBadge(enum)
}

@Composable
fun StatusProjetoBadge(status: StatusProjeto) {
    val (label, bg, text) = when (status) {
        StatusProjeto.PLANEJADO          -> Triple("Planejado", InovaColors.AmberWarning.copy(alpha = 0.1f), InovaColors.AmberWarning)
        StatusProjeto.EM_DESENVOLVIMENTO -> Triple("Em desenvolvimento", InovaColors.BlueAccent.copy(alpha = 0.1f), InovaColors.BlueAccent)
        StatusProjeto.CONCLUIDO          -> Triple("Concluído", InovaColors.GreenSuccess.copy(alpha = 0.1f), InovaColors.GreenSuccess)
    }
    Surface(
        color    = bg,
        shape    = RoundedCornerShape(6.dp),
        modifier = Modifier.wrapContentSize()
    ) {
        Text(
            text       = label,
            color      = text,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun UrgentBadge() {
    Surface(
        color = InovaColors.RedDanger.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, InovaColors.RedDanger.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Bolt, null, tint = InovaColors.RedDanger, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(2.dp))
            Text("URGENTE", color = InovaColors.RedDanger, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}

// ─────────────────────────────────────────────────────────
// CardIdeia
// ─────────────────────────────────────────────────────────

@Composable
fun CardIdeia(
    ideia: Ideia,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.07f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ideia.titulo,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(status = ideia.status)
                        if (ideia.urgente) UrgentBadge()
                    }
                }
                if (ideia.impactoEstimado > 0) {
                    Text(
                        text = "+R$${formatarMoeda(ideia.impactoEstimado)}/ano",
                        color = InovaColors.GreenSuccess,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

private fun formatarMoeda(valor: Double): String {
    return when {
        valor >= 1_000_000 -> "${(valor / 1_000_000).toInt()}M"
        valor >= 1_000     -> "${(valor / 1_000).toInt()}K"
        else               -> valor.toInt().toString()
    }
}

// ─────────────────────────────────────────────────────────
// LabeledProgressBar
// ─────────────────────────────────────────────────────────

@Composable
fun LabeledProgressBar(
    label: String,
    value: String,
    progress: Float,
    barColor: Color = InovaColors.GreenSuccess,
    labelColor: Color = Color.White,
    valueColor: Color = Color.White
) {
    Column {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = label,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = labelColor
            )
            Text(
                text       = value,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Medium,
                color      = valueColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress   = { progress.coerceIn(0f, 1f) },
            modifier   = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color      = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap  = StrokeCap.Round
        )
    }
}

// Helper para definir cor por nome da divisão
fun getCorPorDivisao(divisao: String): Color {
    val d = divisao.lowercase()
    return when {
        d.contains("passageiros") || d.contains("embarque") -> InovaColors.GreenSuccess
        d.contains("logística")   || d.contains("logistica") -> InovaColors.BlueAccent
        d.contains("comércio")    || d.contains("comercio")  -> InovaColors.AmberWarning
        d.contains("terminal")    -> InovaColors.PurpleAI
        else                     -> InovaColors.GrayMuted
    }
}

// ─────────────────────────────────────────────────────────
// InovaHeader
// ─────────────────────────────────────────────────────────

@Composable
fun InovaHeader(
    subtitulo: String,
    nome: String,
    iniciais: String,
    onAvatarClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Surface(
        color    = InovaColors.NavyPrimary,
        modifier = Modifier.fillMaxWidth().height(120.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = subtitulo, color = InovaColors.TextOnDarkMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text       = "Olá, ${nome.split(" ").first()}!",
                        color      = InovaColors.TextOnDark,
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Box(
                    modifier = Modifier.size(45.dp).clip(CircleShape).background(InovaColors.BlueAccent)
                        .then(
                            if (onAvatarClick != null) {
                                Modifier.clickable { onAvatarClick() }
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = iniciais, color = Color.White, fontWeight = FontWeight.Medium)

                }
            }
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────
// BottomNav & TopBar
// ─────────────────────────────────────────────────────────

data class NavItem(val label: String, val icon: ImageVector, val route: String)

val liderNavItems = listOf(
    NavItem("Dashboard",  Icons.Outlined.BarChart,      "home_diretoria"),
    NavItem("Ranking",    Icons.Outlined.EmojiEvents,   "ranking"),
    NavItem("Projetos",   Icons.Outlined.Work,          "projetos_lider"),
    NavItem("Estratégia", Icons.Outlined.Bookmark,      "estrategia_lider"),
    NavItem("Notif.",     Icons.Outlined.Notifications, "notificacoes"),
    NavItem("Perfil",     Icons.Outlined.Person,        "perfil")
)

val gestorNavItems = listOf(
    NavItem("Início",     Icons.Outlined.Home,          "home_gestor"),
    NavItem("Ideias",     Icons.Outlined.Lightbulb,      "ideias_gestor"),
    NavItem("Estratégia", Icons.Outlined.Flag,          "estrategia_gestor"),
    NavItem("Projetos",   Icons.Outlined.FolderOpen,    "projetos"),
    NavItem("Notif.",     Icons.Outlined.Notifications, "notificacoes"),
    NavItem("Perfil",     Icons.Outlined.Person,        "perfil")
)

@Composable
fun InovaBottomNav(
    items: List<NavItem>,
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    NavigationBar(
        containerColor = InovaColors.NavyPrimary,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick  = { onItemClick(item.route) },

                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },

                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                },

                alwaysShowLabel = true,

                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = InovaColors.BlueAccent,
                    selectedTextColor   = InovaColors.BlueAccent,
                    indicatorColor      = InovaColors.BlueAccent.copy(alpha = 0.15f),
                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                    unselectedTextColor = Color.White.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
fun BottomNavOperador(rotaAtual: String, onNavigate: (String) -> Unit) {
    val itens = listOf(
        NavItem("Início", Icons.Outlined.Home, "home_motorista"),
        NavItem("Ideias", Icons.Outlined.Lightbulb, "minhas_ideias"),
        NavItem("Estratégia", Icons.Outlined.Flag, "estrategia"),
        NavItem("Notif.", Icons.Outlined.Notifications, "notificacoes"),
        NavItem("Perfil", Icons.Outlined.Person, "perfil")
    )
    InovaBottomNav(items = itens, currentRoute = rotaAtual, onItemClick = onNavigate)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InovaTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    TopAppBar(
        title = { Text(text = title, fontWeight = FontWeight.Medium, fontSize = 15.sp) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Voltar")
                }
            }
        },
        actions = { action?.invoke() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = InovaColors.NavyPrimary,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}
