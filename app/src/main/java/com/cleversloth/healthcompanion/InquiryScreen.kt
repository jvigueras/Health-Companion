package com.cleversloth.healthcompanion

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@Composable
fun InquiryScreen(
    inquiryViewModel: InquiryViewModel = viewModel()
) {
   val inquiryUiState by inquiryViewModel.uiState.collectAsState()
   val listState = rememberLazyListState()
   val coroutineScope = rememberCoroutineScope()
   val context = LocalContext.current

   Scaffold (

       topBar = {
           TitleBar()
       },
       bottomBar = {
           FieldInquiry(
               onSendMessage = { message ->
                   inquiryViewModel.sendInquiryMsg(message)
               },
               resetScroll = {
                   coroutineScope.launch {
                       listState.scrollToItem(0)
                   }
               }
           )
       }
   ) { innerPadding ->
       Column(
           modifier = Modifier
               .padding(innerPadding)
               .fillMaxSize()
       ) {

           InquiryList(
               inquiryMessages = inquiryUiState.msg_list,
               onShare = { message ->
                   inquiryViewModel.shareInquiryMsg(message, context)
               },
               listState = listState
           )
       }
   }
}



@Composable
private fun TitleBar() {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
    }
}

@Composable
fun InquiryList(inquiryMessages: List<InquiryMessage>,
                onShare: (InquiryMessage) -> Unit,
                listState: LazyListState
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = inquiryMessages, key = { message -> message.id }) {  message ->
            InquiryItem(message,onShare = { onShare(message) })
        }
        coroutineScope.launch {
            listState.animateScrollToItem( listState.layoutInfo.totalItemsCount-1 )
        }
    }
}

@Composable
fun InquiryItem(message: InquiryMessage, onShare: () -> Unit) {

    val backgroundColor = when(message.role) {
        Role.USER -> MaterialTheme.colorScheme.primary
        Role.MODEL -> MaterialTheme.colorScheme.tertiary
    }

    val textColor = when(message.role) {
        Role.USER -> MaterialTheme.colorScheme.onPrimary
        Role.MODEL -> MaterialTheme.colorScheme.onSecondary
    }

    val horizontalAlignment = when(message.role) {
        Role.USER -> Alignment.End
        Role.MODEL -> Alignment.Start
    }

    val msgShape = when(message.role) {
        Role.USER -> RoundedCornerShape(4.dp, 4.dp, 4.dp, 20.dp)
        Role.MODEL -> RoundedCornerShape(4.dp, 4.dp, 20.dp, 4.dp)
    }

    val roleText = when(message.role) {
        Role.USER -> stringResource(R.string.patient)
        Role.MODEL -> stringResource(R.string.cobi)
    }

    Column(
        horizontalAlignment = horizontalAlignment,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
            Text(
                text = roleText,
                modifier = Modifier
                    .padding(all = 4.dp),
                style = MaterialTheme.typography.bodySmall
            )

            BoxWithConstraints {
                Card(
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    shape = msgShape,
                    modifier = Modifier.widthIn(0.dp, maxWidth * 0.8f),
                    ) {
                    Row {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .padding(start = 4.dp, top = 4.dp)
                                .size(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = stringResource(R.string.share_message),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Text(
                            text = message.msg,
                            modifier = Modifier.padding(start = 5.dp, end = 10.dp),
                            color = textColor
                        )
                    }
                }
            }
    }
}


@Composable
fun FieldInquiry(
    onSendMessage: (String) -> Unit,
    resetScroll: () -> Unit = {}
) {
    var inquiryMsg by rememberSaveable { mutableStateOf("") }

    ElevatedCard(
       modifier = Modifier
           .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = inquiryMsg,
                label = { Text(stringResource(R.string.enter_your_inquiry)) },
                onValueChange = {
                    inquiryMsg = it
                },
                modifier = Modifier
                    .weight(0.75f)
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(),
            )
            IconButton(
                onClick = {
                    if (inquiryMsg.isNotBlank()) {
                        onSendMessage(inquiryMsg)
                        inquiryMsg = ""
                        resetScroll()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = stringResource(R.string.send_inquiry),
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}