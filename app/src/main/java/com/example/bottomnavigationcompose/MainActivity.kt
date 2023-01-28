package com.example.bottomnavigationcompose

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bottomnavigationcompose.ui.screens.HomeScreen
import com.example.bottomnavigationcompose.ui.screens.MapScreen
import com.example.bottomnavigationcompose.ui.screens.ProfileScreen
import com.example.bottomnavigationcompose.ui.screens.SettingsScreen
import com.example.bottomnavigationcompose.ui.theme.BottomNavigationComposeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BottomNavigationComposeTheme {
                MainScreen()
            }
        }
    }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    @Composable
    fun MainScreen() {

        /** create a nav controller remember nav host **/
        val navController = rememberNavController()

        /** create a scaffold state, set it to close by default **/
        val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))

        /**
         * Create a coroutine scope. Opening of Drawer
         * and snack bar should happen in background
         * thread without blocking main thread
         *
         * **/
        val coroutineScope = rememberCoroutineScope()

        /** Scaffold **/
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { TopBar(coroutineScope, scaffoldState) },
            content = { ContentArea(navController) },
            bottomBar = { BottomNavigationBar(navController) },
            drawerContent = { Drawer(coroutineScope, scaffoldState) },
            floatingActionButton = { FloatingActionWithSnakbar(coroutineScope, scaffoldState) }
        )

        /** Handle back button action if drawer open first it will close drawer **/
        BackPressHandler(onBackPressed = {
            if (scaffoldState.drawerState.isOpen) {
                coroutineScope.launch {
                    /** to close Drawer help by scaffoldState **/
                    scaffoldState.drawerState.close()
                }
            } else {
                /** to close App **/
                finishAffinity()
            }
        })
    }

    /** Content Area is Center part between TopAppbar & BottomNav bar **/
    @Composable
    fun ContentArea(navController: NavHostController) {
        Navigation(navController = navController)

    }

    /** BottomNavigationBar function is configuration of bottom navigation **/
    @Composable
    fun BottomNavigationBar(navController: NavController) {

        val menuItem = listOf(
            NavigationItems.Home,
            NavigationItems.Map,
            NavigationItems.Profile,
            NavigationItems.Settings
        )

        BottomNavigation(backgroundColor = Color.White, contentColor = Color.Black) {

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val isBottomBarItemDestination = menuItem.any { it.route == currentRoute }
            /** if item from list item used in bottom bar then only show bottom navigation bar unless it will hide **/
            if (isBottomBarItemDestination){
                menuItem.forEach { menuItem ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = menuItem.icon),
                                contentDescription = menuItem.title
                            )
                        },
                        label = { Text(text = menuItem.title) },
                        selectedContentColor = Color.Black,
                        unselectedContentColor = Color.Black.copy(0.4f),
                        alwaysShowLabel = true,
                        selected = currentRoute == menuItem.route,
                        onClick = {
                            navController.navigate(menuItem.route) {
                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route = route) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }

    /** Topbar function is a configuration of toolbar **/
    @Composable
    fun TopBar(coroutineScope: CoroutineScope, scaffoldState: ScaffoldState) {
        TopAppBar(
            title = { Text(text = "Jetpack Compose Navigation", fontSize = 18.sp) },
            backgroundColor = Color.Black,
            contentColor = Color.White,
            navigationIcon = {
                /** Hamburger icon for drawer menu **/
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    modifier = Modifier
                        .padding(10.dp)
                        .clickable {
                            coroutineScope.launch {
                                /** To close drawer use -> scaffoldState.drawerState.close() **/
                                scaffoldState.drawerState.open()
                            }
                        },
                    tint = Color.White
                )
            }
        )
    }

    /** Drawer Menu function is a configuration of toolbar **/
    @Composable
    fun Drawer(coroutineScope: CoroutineScope, scaffoldState: ScaffoldState) {
        Column(
            Modifier
                .background(Color.White)
                .fillMaxSize()
        ) {

            val drawerMenu = listOf("Home", "Bookmark", "Map", "Settings", "Sign Out")
            val listState = rememberLazyListState()

            /** Populate List item from drawer menu array using lazy row **/
            LazyColumn(state = listState) {
                items(drawerMenu) { item ->
                    Text(
                        text = item,
                        color = Color.Black,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .clickable {
                                /** To close Drawer help by scaffoldState when click on item **/
                                coroutineScope.launch {
                                    scaffoldState.drawerState.close()
                                }
                            }
                    )
                }
            }
        }
    }

    /** Floating action with snackbar action Button **/
    @Composable
    fun FloatingActionWithSnakbar(coroutineScope: CoroutineScope, scaffoldState: ScaffoldState) {
        FloatingActionButton(
            onClick = {
                /** When clicked open Snack bar **/
                coroutineScope.launch {
                    when (scaffoldState.snackbarHostState.showSnackbar(
                        message = "Hello From Jetpack Compose",
                        actionLabel = "Close"
                    )) {
                        SnackbarResult.Dismissed -> {
                            /** do something when snack bar is dismissed **/
                        }

                        SnackbarResult.ActionPerformed -> {
                            /** when it appears **/
                        }
                    }
                }
            }) {

            /** Simple Icon here inside fav icon **/
            Icon(
                painter = painterResource(id = R.drawable.baseline_add_24),
                contentDescription = null
            )
        }
    }

    /** Navigation is a function for configuration Navigation Route **/
    @Composable
    fun Navigation(navController: NavHostController) {
        NavHost(navController, startDestination = NavigationItems.Home.route) {

            composable(NavigationItems.Home.route) {
                HomeScreen()
            }

            composable(NavigationItems.Map.route) {
                MapScreen()
            }

            composable(NavigationItems.Profile.route) {
                ProfileScreen()
            }

            composable(NavigationItems.Settings.route) {
                SettingsScreen()
            }

        }
    }

    /** Back pressed Handle action **/
    @Composable
    fun BackPressHandler(
        backPressedDispatcher: OnBackPressedDispatcher? = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher,
        onBackPressed: () -> Unit
    ) {
        val currentOnBackPressed by rememberUpdatedState(newValue = onBackPressed)
        val backCallback = remember {
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    currentOnBackPressed()
                }
            }
        }

        DisposableEffect(key1 = backPressedDispatcher) {
            backPressedDispatcher?.addCallback(backCallback)
            onDispose {
                backCallback.remove()
            }
        }
    }
}
