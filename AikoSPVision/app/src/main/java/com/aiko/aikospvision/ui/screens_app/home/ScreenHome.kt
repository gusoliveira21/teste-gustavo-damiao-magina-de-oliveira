package com.aiko.aikospvision.ui.screens_app.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.aiko.aikospvision.ui.componentes.IndeterminateCircularIndicator
import com.aiko.aikospvision.ui.componentes.NavigationBar
import com.aiko.aikospvision.ui.componentes.Scaffold
import com.aiko.aikospvision.ui.componentes.SearchInputField
import com.aiko.aikospvision.ui.theme.AikoSPVisionTheme
import com.aiko.aikospvision.util.paradaAdapter
import com.aiko.domain.model.Parada
import com.aiko.domain.usecase.GetStopsBySearchTermUseCase
import com.aiko.domain.usecase.PostAuthUseCase
import com.alabia.manager.ui.state.TaskState
import com.alabia.manager.ui.state.ValidationEvent
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

//todo: Abstrair
//  - viewModel
//  - data class
//  - data class

sealed class HomeFormEvent {
    data class SearchChanged(val searchText: String) : HomeFormEvent()
    data object Submit : HomeFormEvent()
}

data class HomeState(
    val searchText: String = "",
    val listStopped: List<Parada> = emptyList(),
    val onSearchClick: () -> Unit = {},
    val listFavorites: List<String> = emptyList(),
)

abstract class ViewModelHome : ViewModel() {
    protected abstract val _state: MutableStateFlow<HomeState>
    open val state: StateFlow<HomeState> get() = _state.asStateFlow()
    abstract val taskState: StateFlow<TaskState>
    abstract val validationEventChannel: Channel<ValidationEvent>
    open val validationEvents: Flow<ValidationEvent>
        get() = validationEventChannel.receiveAsFlow()
    abstract val message: StateFlow<String>
    abstract fun failed(exception: Throwable?)
    abstract fun auth()
    abstract fun onEvent(event: HomeFormEvent)
}

class MockViewModelHome() : ViewModelHome() {
    override val _state: MutableStateFlow<HomeState> = MutableStateFlow(HomeState())
    override val state = _state.asStateFlow()

    override val validationEventChannel: Channel<ValidationEvent> = Channel()
    override val message: StateFlow<String> = MutableStateFlow("")

    private val _taskState: MutableStateFlow<TaskState> = MutableStateFlow(TaskState.Idle)
    override val taskState: StateFlow<TaskState> = _taskState

    override fun failed(exception: Throwable?) {
        println("Failed with exception: ${exception?.message}")
    }

    override fun auth() {
        println("Fetching data...")
    }

    override fun onEvent(event: HomeFormEvent) {
        TODO("Not yet implemented")
    }
}

class ViewModelHomeImpl(
    val getStopsBySearchTermUseCase: GetStopsBySearchTermUseCase,
    val postAuthUseCase: PostAuthUseCase,
) : ViewModelHome() {
    override var _state: MutableStateFlow<HomeState> = MutableStateFlow(HomeState())
    override val state = _state.asStateFlow()

    private val _taskState: MutableStateFlow<TaskState> = MutableStateFlow(TaskState.Idle)
    override val taskState: StateFlow<TaskState> get() = _taskState

    init {
        auth()
    }

    override val validationEventChannel = Channel<ValidationEvent>()

    override val message: StateFlow<String> get() = _message
    private val _message = MutableStateFlow("")

    private fun updateMessage(newMessage: String) {
        _message.value = newMessage
    }

    override fun auth() {
        _taskState.value = TaskState.Loading
        viewModelScope.launch {
            val result = postAuthUseCase.execute(Unit)
            result.handleResult({
                Log.i(TAG, "Success AUTH: $it")
            }, {
                Log.e(TAG, "FALHA AUTH: $it")
            })
        }
        _taskState.value = TaskState.Idle
    }

    private fun change(searchText: String? = null) {
        if (searchText != null) {
            _state.value = state.value.copy(searchText = searchText)
        }
    }

    override fun onEvent(event: HomeFormEvent) {
        when (event) {
            is HomeFormEvent.SearchChanged -> change(searchText = event.searchText)
            is HomeFormEvent.Submit -> onSubmit()
        }
    }

    override fun failed(exception: Throwable?) {
        updateMessage(exception?.message ?: "Unknown error occurred!")
        viewModelScope.launch { validationEventChannel.send(ValidationEvent.Failed) }
    }

    private fun onSubmit() {
        _taskState.value = TaskState.Loading
        viewModelScope.launch {
            getStopsBySearchTermUseCase.execute(state.value.searchText).handleResult({
                _state.value = _state.value.copy(listStopped = it)
            }, {
                Log.e(TAG, "FALHA onSubmit: $it")
            })
        }
        _taskState.value = TaskState.Idle
    }
}

@Composable
private fun getModel(): ViewModelHome {
    return if (LocalInspectionMode.current) {
        MockViewModelHome()
    } else {
        getViewModel<ViewModelHomeImpl>()
    }
}

@Composable
fun ScreenHome(navController: NavController, viewModel: ViewModelHome = getViewModel()) {
    val context = LocalContext.current.applicationContext
    val state by viewModel.state.collectAsState()
    val taskState by viewModel.taskState.collectAsState()

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = Color.Transparent, darkIcons = true)
    systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.surfaceVariant)
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        Scaffold(
            titleTopBar = "HOME",
            navigationUp = navController,
            modifier = Modifier,
            actions = {
                IconButton(onClick = {}, enabled = false) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = Color.Transparent
                    )
                }
            },
            showTopBar = true,
            showLogo = false,
            showBottomBarNavigation = true,
            bottomNavigationBar = { NavigationBar(navController) },
            contentToUse = { paddingValue ->
                Box(modifier = Modifier.padding(paddingValue)) {
                    Column {
                        Row(modifier = Modifier.padding(16.dp)) {
                            SearchInputField(
                                value = state.searchText ?: "",
                                onValueChange = { value ->
                                    viewModel.onEvent(
                                        HomeFormEvent.SearchChanged(
                                            value
                                        )
                                    )
                                },
                                onSearchClick = {
                                    viewModel.onEvent(HomeFormEvent.Submit)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (taskState is TaskState.Loading) {
                            IndeterminateCircularIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            if (viewModel.state.value.listStopped.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.CenterHorizontally)
                                        .padding(16.dp)
                                ) {
                                    Row {
                                        Column {
                                            LazyColumn(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(16.dp))
                                            ) {
                                                if (viewModel.state.value.listStopped.isNotEmpty()) {
                                                    items(
                                                        count = viewModel.state.value.listStopped.size,
                                                        key = { index ->
                                                            viewModel.state.value.listStopped[index].uniqueID
                                                                ?: -1
                                                        }
                                                    ) {
                                                        ParadaItem(
                                                            nomeParada = "${state.listStopped[it].nomeParada}",
                                                            enderecoParada = "${state.listStopped[it].enderecoParada}\n ${state.listStopped[it].codigoParada}",
                                                            modifier = Modifier
                                                                .padding(vertical = 8.dp)
                                                                .background(Color.Red.copy(alpha = 0.1f)),
                                                            onItemClick = {
                                                                try{
                                                                    navController.navigate("stops_screen/${state.listStopped[it].codigoParada}")
                                                                }  catch (e: Exception){
                                                                    Log.e(TAG, "Exception: $e")
                                                                }                                                          },
                                                        )
                                                    }
                                                } else {
                                                    item { Row { Text(text = "Sem itens") } }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}


@Preview(name = "PHONE", showBackground = true, showSystemUi = true, device = Devices.PHONE)
@Composable
fun ScreenHomeManagerPreview() {
    AikoSPVisionTheme {
        ScreenHome(
            navController = NavController(LocalContext.current),
            viewModel = MockViewModelHome()
        )
    }
}


@Composable
fun ParadaItem(
    nomeParada: String,
    enderecoParada: String,
    modifier: Modifier = Modifier,
    onItemClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(16.dp)
    ) {
        Text(
            text = nomeParada,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = enderecoParada,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.Start)
        )
    }
}