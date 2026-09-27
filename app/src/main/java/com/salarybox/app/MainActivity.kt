package com.salarybox.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.salarybox.app.data.local.db.AppDatabase
import com.salarybox.app.data.repository.AttendanceRepository
import com.salarybox.app.data.repository.AuthRepository
import com.salarybox.app.data.repository.StaffRepository
import com.salarybox.app.ui.admin.AddStaffScreen
import com.salarybox.app.ui.admin.AdminHomeScreen
import com.salarybox.app.ui.admin.StaffListScreen
import com.salarybox.app.ui.admin.StaffViewModel
import com.salarybox.app.ui.admin.StaffViewModelFactory
import com.salarybox.app.ui.login.LoginScreen
import com.salarybox.app.ui.staff.StaffHomeScreen
import com.salarybox.app.ui.theme.SalaryBoxTheme

/**
 * Single-activity entry point for the SalaryBox app.
 *
 * Repositories are created here from the [AppDatabase] singleton and passed
 * into composables that need them.  This is a lightweight manual-DI approach;
 * swap for Hilt when the graph grows.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Build repositories once — they are cheap value objects wrapping DAOs.
        val db = AppDatabase.getInstance(applicationContext)
        val authRepository = AuthRepository(db.userDao())
        val staffRepository = StaffRepository(db.staffDao())
        val attendanceRepository = AttendanceRepository(db.attendanceDao())

        setContent {
            SalaryBoxTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SalaryBoxNavGraph(
                        authRepository = authRepository,
                        staffRepository = staffRepository,
                        attendanceRepository = attendanceRepository
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Route constants
// ---------------------------------------------------------------------------

object AppDestinations {
    // --- Top-level ---
    const val LOGIN = "login"

    // --- Admin graph ---
    const val ADMIN_GRAPH    = "admin_graph"
    const val ADMIN_HOME     = "admin_home"
    const val STAFF_LIST     = "staff_list"
    const val ADD_STAFF      = "add_staff"
    const val STAFF_PROFILE  = "staff_profile/{staffId}"
    const val ALL_ATTENDANCE = "all_attendance"
    const val CAMERA_CAPTURE = "camera_capture"

    fun staffProfile(staffId: Long) = "staff_profile/$staffId"

    // --- Staff graph — staffId is baked into the graph route so the ViewModel gets it ---
    const val STAFF_GRAPH        = "staff_graph/{staffId}"
    const val STAFF_HOME         = "staff_home"
    const val MARK_ATTENDANCE    = "mark_attendance/{staffId}"
    const val ATTENDANCE_HISTORY = "attendance_history/{staffId}"

    fun staffGraph(staffId: Long)        = "staff_graph/$staffId"
    fun markAttendance(staffId: Long)    = "mark_attendance/$staffId"
    fun attendanceHistory(staffId: Long) = "attendance_history/$staffId"
}

// ---------------------------------------------------------------------------
// Nav graph
// ---------------------------------------------------------------------------

@Composable
fun SalaryBoxNavGraph(
    authRepository: AuthRepository,
    staffRepository: StaffRepository,
    attendanceRepository: AttendanceRepository
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOGIN
    ) {
        // --- Login ---
        composable(AppDestinations.LOGIN) {
            val loginViewModel: com.salarybox.app.ui.login.LoginViewModel = viewModel(
                factory = com.salarybox.app.ui.login.LoginViewModelFactory(authRepository)
            )
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { role, staffId ->
                    when (role) {
                        com.salarybox.app.data.model.Role.ADMIN ->
                            navController.navigate(AppDestinations.ADMIN_GRAPH) {
                                popUpTo(AppDestinations.LOGIN) { inclusive = true }
                            }
                        com.salarybox.app.data.model.Role.STAFF -> {
                            val id = staffId ?: -1L
                            navController.navigate(AppDestinations.staffGraph(id)) {
                                popUpTo(AppDestinations.LOGIN) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }

        // ---------------------------------------------------------------
        // Admin nested graph — StaffViewModel is scoped here so it is
        // shared between StaffListScreen and AddStaffScreen.
        // ---------------------------------------------------------------
        navigation(
            startDestination = AppDestinations.ADMIN_HOME,
            route = AppDestinations.ADMIN_GRAPH
        ) {
            composable(AppDestinations.ADMIN_HOME) {
                AdminHomeScreen(
                    onNavigateToStaff = { navController.navigate(AppDestinations.STAFF_LIST) },
                    onNavigateToAttendance = { navController.navigate(AppDestinations.ALL_ATTENDANCE) }
                )
            }

            composable(AppDestinations.STAFF_LIST) { entry ->
                val adminEntry = remember(entry) {
                    navController.getBackStackEntry(AppDestinations.ADMIN_GRAPH)
                }
                val viewModel: StaffViewModel = viewModel(
                    viewModelStoreOwner = adminEntry,
                    factory = StaffViewModelFactory(staffRepository, authRepository)
                )
                StaffListScreen(
                    viewModel = viewModel,
                    onNavigateToAddStaff = { navController.navigate(AppDestinations.ADD_STAFF) },
                    onNavigateToProfile = { staffId ->
                        navController.navigate(AppDestinations.staffProfile(staffId))
                    }
                )
            }

            composable(AppDestinations.ADD_STAFF) { entry ->
                val adminEntry = remember(entry) {
                    navController.getBackStackEntry(AppDestinations.ADMIN_GRAPH)
                }
                val viewModel: StaffViewModel = viewModel(
                    viewModelStoreOwner = adminEntry,
                    factory = StaffViewModelFactory(staffRepository, authRepository)
                )
                AddStaffScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(AppDestinations.STAFF_PROFILE) { backStackEntry ->
                val staffId = backStackEntry.arguments
                    ?.getString("staffId")?.toLongOrNull() ?: return@composable

                // Get the captured image path from savedStateHandle if any
                val capturedImagePath = backStackEntry.savedStateHandle.get<String>("capturedImagePath")

                // Once we read it, clear so it isn't reprocessed on rotation
                androidx.compose.runtime.LaunchedEffect(capturedImagePath) {
                    if (!capturedImagePath.isNullOrEmpty()) {
                        backStackEntry.savedStateHandle.remove<String>("capturedImagePath")
                    }
                }

                val viewModel: com.salarybox.app.ui.admin.StaffProfileViewModel = viewModel(
                    factory = com.salarybox.app.ui.admin.StaffProfileViewModelFactory(
                        staffId = staffId,
                        staffRepository = staffRepository,
                        context = androidx.compose.ui.platform.LocalContext.current.applicationContext
                    )
                )

                com.salarybox.app.ui.admin.StaffProfileScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onLaunchCamera = { navController.navigate(AppDestinations.CAMERA_CAPTURE) },
                    capturedImagePath = capturedImagePath
                )
            }

            composable(AppDestinations.ALL_ATTENDANCE) {
                val viewModel: com.salarybox.app.ui.admin.AllAttendanceViewModel = viewModel(
                    factory = com.salarybox.app.ui.admin.AllAttendanceViewModelFactory(
                        staffRepository = staffRepository,
                        attendanceRepository = attendanceRepository,
                        context = androidx.compose.ui.platform.LocalContext.current.applicationContext
                    )
                )
                com.salarybox.app.ui.admin.AllAttendanceScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // ---------------------------------------------------------------
        // Staff nested graph — staffId embedded in parent route
        // ---------------------------------------------------------------
        navigation(
            startDestination = AppDestinations.STAFF_HOME,
            route = AppDestinations.STAFF_GRAPH  // "staff_graph/{staffId}"
        ) {
            composable(AppDestinations.STAFF_HOME) { entry ->
                val graphEntry = remember(entry) {
                    navController.getBackStackEntry(AppDestinations.STAFF_GRAPH)
                }
                val staffId = graphEntry.arguments?.getString("staffId")?.toLongOrNull() ?: -1L

                val viewModel: com.salarybox.app.ui.staff.StaffHomeViewModel = viewModel(
                    factory = com.salarybox.app.ui.staff.StaffHomeViewModelFactory(staffId, staffRepository)
                )
                StaffHomeScreen(
                    viewModel = viewModel,
                    onNavigateToMarkAttendance = { id -> navController.navigate(AppDestinations.markAttendance(id)) },
                    onNavigateToAttendanceHistory = { id -> navController.navigate(AppDestinations.attendanceHistory(id)) }
                )
            }

            composable(AppDestinations.MARK_ATTENDANCE) { backStackEntry ->
                val staffId = backStackEntry.arguments?.getString("staffId")?.toLongOrNull() ?: return@composable
                val capturedImagePath = backStackEntry.savedStateHandle.get<String>("capturedImagePath")

                androidx.compose.runtime.LaunchedEffect(capturedImagePath) {
                    if (!capturedImagePath.isNullOrEmpty()) {
                        backStackEntry.savedStateHandle.remove<String>("capturedImagePath")
                    }
                }

                val viewModel: com.salarybox.app.ui.staff.MarkAttendanceViewModel = viewModel(
                    factory = com.salarybox.app.ui.staff.MarkAttendanceViewModelFactory(
                        staffId = staffId,
                        staffRepository = staffRepository,
                        attendanceRepository = attendanceRepository,
                        context = androidx.compose.ui.platform.LocalContext.current.applicationContext
                    )
                )
                com.salarybox.app.ui.staff.MarkAttendanceScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onLaunchCamera = { navController.navigate(AppDestinations.CAMERA_CAPTURE) },
                    capturedImagePath = capturedImagePath
                )
            }

            composable(AppDestinations.ATTENDANCE_HISTORY) { backStackEntry ->
                val staffId = backStackEntry.arguments?.getString("staffId")?.toLongOrNull() ?: return@composable

                val viewModel: com.salarybox.app.ui.staff.AttendanceHistoryViewModel = viewModel(
                    factory = com.salarybox.app.ui.staff.AttendanceHistoryViewModelFactory(
                        staffId = staffId,
                        attendanceRepository = attendanceRepository,
                        context = androidx.compose.ui.platform.LocalContext.current.applicationContext
                    )
                )
                com.salarybox.app.ui.staff.AttendanceHistoryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // ---------------------------------------------------------------
        // Shared camera screen — accessible from both admin & staff graphs
        // ---------------------------------------------------------------
        composable(AppDestinations.CAMERA_CAPTURE) {
            com.salarybox.app.ui.components.CameraCaptureScreen(
                onImageCaptured = { path ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("capturedImagePath", path)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
