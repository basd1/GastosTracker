package bas.orellana.gastostracker.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import bas.orellana.gastostracker.presentation.navigation.NavRoutes
import bas.orellana.gastostracker.presentation.resources.Res
import bas.orellana.gastostracker.presentation.resources.nav_ahorro
import bas.orellana.gastostracker.presentation.resources.nav_balance
import bas.orellana.gastostracker.presentation.resources.nav_gastos
import bas.orellana.gastostracker.presentation.resources.nav_grafico
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private data class NavTab(
    val route: String,
    val icon: ImageVector,
    val label: StringResource
)

private val tabs = listOf(
    NavTab(NavRoutes.BALANCE, Icons.Default.AccountBalanceWallet, Res.string.nav_balance),
    NavTab(NavRoutes.HOME, Icons.AutoMirrored.Filled.ReceiptLong, Res.string.nav_gastos),
    NavTab(NavRoutes.GRAPH_SCREEN, Icons.Default.PieChart, Res.string.nav_grafico),
    NavTab(NavRoutes.METAS, Icons.Default.Savings, Res.string.nav_ahorro)
)

@Composable
fun BottomNavBar(navController: NavController) {
    val currentRoute = navController.currentDestination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        tabs.forEach { tab ->
            val etiqueta = stringResource(tab.label)
            NavigationBarItem(
                icon = { Icon(tab.icon, contentDescription = etiqueta) },
                label = { Text(etiqueta) },
                selected = currentRoute == tab.route,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(NavRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
