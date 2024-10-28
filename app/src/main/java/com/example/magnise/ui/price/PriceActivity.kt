package com.example.magnise.ui.price

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.magnise.R
import com.example.magnise.util.Loading
import com.example.magnise.data.price.PriceCache
import com.example.magnise.ui.theme.MagniseTheme
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.extensions.format
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import java.text.SimpleDateFormat
import java.util.Locale

class PriceActivity : ComponentActivity() {

    private lateinit var viewModel: PriceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        PriceCache.init(this)
        val factory = PriceViewModel.Factory(this)
        viewModel = ViewModelProvider(this, factory)[PriceViewModel::class.java]

        setContent {
            MagniseTheme {
                Scaffold(modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)) { padding ->
                    Root(viewModel, padding)
                }
            }
        }

        viewModel.init()
    }

}

@Composable
fun Root(viewModel: PriceViewModel, padding: PaddingValues) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(padding)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            InstrumentList(viewModel)
            Price(viewModel)
            Chart(viewModel)
        }

        ProgressBar(viewModel)
    }
}

@Composable
fun InstrumentList(viewModel: PriceViewModel) {
    val instrumentListLoading by viewModel.instrumentListState.collectAsState()
    val instrument by viewModel.instrumentState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, start = 20.dp, end = 20.dp, bottom = 50.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        var expanded by remember { mutableStateOf(false) }

        val options = if (instrumentListLoading is Loading.Done) {
            (instrumentListLoading as Loading.Done).value
        } else {
            listOf()
        }

        Box(modifier = Modifier.width(120.dp)) {
            Text(
                text = instrument?.toString() ?: stringResource(R.string.select),
                color = Color.Black,
                modifier = Modifier
                    .clickable { expanded = true }
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.toString()) },
                        onClick = {
                            expanded = false
                            viewModel.setCurrentInstrument(option)
                        }
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.subscribe),
            color = Color.White,
            modifier = Modifier
                .clickable { viewModel.subscribeToCurrentInstrument() }
                .background(Color.Blue, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun Price(viewModel: PriceViewModel) {
    val price by viewModel.priceState.collectAsState()

    price?.let {
        val dateFormat = SimpleDateFormat(stringResource(R.string.price_date_format), Locale.getDefault())

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Symbol Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.symbol), style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it.instrument.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Price Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.price), style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.getDefault(), "%,.2f", it.value),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Time Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.time), style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(it.timestamp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (price == null) {
        Text(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp), text = stringResource(R.string.no_data_for_current_price)
        )
    }
}

@Composable
fun Chart(viewModel: PriceViewModel) {
    val dateFormat = SimpleDateFormat(stringResource(R.string.chart_label_date_format), Locale.getDefault())
    val priceHistoryLoading by viewModel.priceHistoryState.collectAsState()

    val history = if (priceHistoryLoading is Loading.Done) {
        (priceHistoryLoading as Loading.Done).value
    } else {
        listOf()
    }

    if (history.isNotEmpty()) {
        val data = mutableListOf<Bars>()
        for (i in history.indices) {
            val it = history[i]
            data.add(
                Bars(
                    if (i % 2 == 0) dateFormat.format(it.timestamp) else " ",
                    listOf(Bars.Data(value = it.value.toDouble(), color = SolidColor(Color.Magenta)))
                )
            )
        }

        ColumnChart(
            data = data,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            labelProperties = LabelProperties(enabled = true, textStyle = TextStyle(color = Color.White)),
            indicatorProperties = HorizontalIndicatorProperties(
                enabled = true,
                textStyle = TextStyle(color = Color.White),
                contentBuilder = {
                    it.format(2)
                }
            ),
            labelHelperProperties = LabelHelperProperties(enabled = false)
        )
    } else {
        Text(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp), text = stringResource(R.string.no_data_for_history)
        )
    }
}

@Composable
fun ProgressBar(viewModel: PriceViewModel) {
    val instrumentListLoading by viewModel.instrumentListState.collectAsState()
    val priceHistoryLoading by viewModel.priceHistoryState.collectAsState()

    val isLoading = instrumentListLoading is Loading.InProgress || priceHistoryLoading is Loading.InProgress

    if (isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0f, 0f, 0f, alpha = 0.4f)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = Color.Magenta,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}