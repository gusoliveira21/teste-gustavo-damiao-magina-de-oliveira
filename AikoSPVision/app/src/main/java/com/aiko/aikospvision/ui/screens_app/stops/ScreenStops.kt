package com.aiko.aikospvision.ui.screens_app.stops

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
import com.aiko.domain.model.Parada
import com.aiko.domain.model.Previsao
import com.aiko.domain.usecase.GetForecastUseCase
import com.aiko.domain.usecase.GetStopsBySearchTermUseCase
import com.aiko.domain.usecase.PostAuthUseCase
import com.alabia.manager.ui.state.TaskState
import com.alabia.manager.ui.state.ValidationEvent
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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

sealed class StopsFormEvent {
    data class SearchChanged(val searchText: String) : StopsFormEvent()
    data object Submit : StopsFormEvent()
}

data class StopsState(
    val stopped : Parada? = null,
    val forecast : Previsao? = null,
    val searchText: String = "",
    val listStopped: List<Parada> = emptyList(),
    val onSearchClick: () -> Unit = {},
    val listFavorites: List<String> = emptyList(),
)

abstract class ViewModelStops : ViewModel() {
    protected abstract val _state: MutableStateFlow<StopsState>
    open val state: StateFlow<StopsState> get() = _state.asStateFlow()
    abstract val taskState: StateFlow<TaskState>
    abstract val validationEventChannel: Channel<ValidationEvent>
    open val validationEvents: Flow<ValidationEvent>
        get() = validationEventChannel.receiveAsFlow()
    abstract val message: StateFlow<String>
    abstract fun failed(exception: Throwable?)
    abstract fun auth()
    abstract fun onEvent(event: StopsFormEvent)
    abstract fun onGetForecast(paradaId: Long)
}

class MockViewModelStops() : ViewModelStops() {
    override val _state: MutableStateFlow<StopsState> = MutableStateFlow(StopsState())
    override val state = _state.asStateFlow()

    override val validationEventChannel: Channel<ValidationEvent> = Channel()
    override val message: StateFlow<String> = MutableStateFlow("")

    private val _taskState: MutableStateFlow<TaskState> = MutableStateFlow(TaskState.Idle)
    override val taskState: StateFlow<TaskState> = _taskState
    override fun onGetForecast(paradaId: Long) {
        TODO("Not yet implemented")
    }
    override fun failed(exception: Throwable?) {
        println("Failed with exception: ${exception?.message}")
    }

    override fun auth() {
        println("Fetching data...")
    }

    override fun onEvent(event: StopsFormEvent) {
        TODO("Not yet implemented")
    }
}

class ViewModelStopsImpl(
    private val getForecastUseCase: GetForecastUseCase,
    private val postAuthUseCase: PostAuthUseCase,
) : ViewModelStops()
{
    override var _state: MutableStateFlow<StopsState> = MutableStateFlow(StopsState())
    override val state = _state.asStateFlow()

    private val _taskState: MutableStateFlow<TaskState> = MutableStateFlow(TaskState.Idle)
    override val taskState: StateFlow<TaskState> get() = _taskState

    init {auth()}

    override fun onGetForecast(paradaId: Long) {
        _taskState.value = TaskState.Loading
        viewModelScope.launch {
            paradaId?.let{
                val result = getForecastUseCase.execute(paradaId)
                result.handleResult({ forecast ->
                    Log.e(TAG, "Forecast: $forecast")
                    _state.value = _state.value.copy(forecast = forecast)
                }, { error ->
                    failed(error)
                }
                )
            }
        }
        _taskState.value = TaskState.Idle
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

    override fun onEvent(event: StopsFormEvent) {
        when (event) {
            is StopsFormEvent.SearchChanged -> change(searchText = event.searchText)
            is StopsFormEvent.Submit -> onSubmit()
        }
    }

    override fun failed(exception: Throwable?) {
        updateMessage(exception?.message ?: "Unknown error occurred!")
        viewModelScope.launch { validationEventChannel.send(ValidationEvent.Failed) }
    }

    private fun onSubmit(){}
}

@Composable
private fun getModel(): ViewModelStops {
    return if (LocalInspectionMode.current) {
        MockViewModelStops()
    } else {
        getViewModel<ViewModelStopsImpl>()
    }
}

@Composable
fun ScreenStops(
    navController: NavController,
    viewModel: ViewModelStops = getViewModel(),
    paradaId: Long? = null
) {
    val context = LocalContext.current.applicationContext
    val state by viewModel.state.collectAsState()
    val taskState by viewModel.taskState.collectAsState()
    if(paradaId != null){
        Log.e(TAG, "paradaId $paradaId")
        viewModel.onGetForecast(paradaId)
    }
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = Color.Transparent, darkIcons = true)
    systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.surfaceVariant)

    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        Scaffold(
            titleTopBar = "STOPS",
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
                                        StopsFormEvent.SearchChanged(
                                            value
                                        )
                                    )
                                },
                                onSearchClick = {
                                    viewModel.onEvent(StopsFormEvent.Submit)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Row(modifier = Modifier.padding(16.dp)) {
                            SearchInputField(
                                value = state.searchText ?: "",
                                onValueChange = { value ->
                                    viewModel.onEvent(
                                        StopsFormEvent.SearchChanged(
                                            value
                                        )
                                    )
                                },
                                onSearchClick = {
                                    viewModel.onEvent(StopsFormEvent.Submit)
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
                                                            onItemClick = { },
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
        ScreenStops(
            navController = NavController(LocalContext.current),
            viewModel = MockViewModelStops()
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