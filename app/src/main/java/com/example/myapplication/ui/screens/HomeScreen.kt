package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(innerPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding
    ) {
        items(40) { index ->
            ListItem(
                headlineContent = { Text("主页动态流 #$index") },
                supportingContent = { Text("滑动看顶栏折叠") }
            )
        }
    }
}
