package bas.orellana.apppair.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import bas.orellana.apppair.navigation.NavRoutes
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate(NavRoutes.HOME) {
            popUpTo(NavRoutes.SPLASH) { inclusive = true }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "AppPair",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}