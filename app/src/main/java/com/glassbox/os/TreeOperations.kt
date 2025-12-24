package com.glassbox.os

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable  // ADDED THIS IMPORT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeOperationsPanel(treeManager: TreeManager) {
    var newNodeName by remember { mutableStateOf("") }
    var selectedParentId by remember { mutableStateOf("glassbox_root") }
    var selectedNodeType by remember { mutableStateOf(NodeType.LEAF) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tree Statistics Card
        TreeStatisticsCard(treeManager)

        // Add Node Card
        AddNodeCard(
            newNodeName = newNodeName,
            onNewNodeNameChange = { newNodeName = it },
            selectedNodeType = selectedNodeType,
            onNodeTypeSelected = { selectedNodeType = it },
            selectedParentId = selectedParentId,
            onParentSelected = { selectedParentId = it },
            treeManager = treeManager,
            onAddNode = {
                if (newNodeName.isNotEmpty()) {
                    val newNode = TreeNode(
                        id = "node_${System.currentTimeMillis()}_${(Math.random() * 1000).toInt()}",
                        name = newNodeName,
                        type = selectedNodeType,
                        data = when (selectedNodeType) {
                            NodeType.PROCESS -> mapOf("pid" to (1000 + (Math.random() * 100).toInt()))
                            NodeType.FILE -> mapOf("size" to "${(Math.random() * 1000).toInt()}KB")
                            NodeType.USER -> mapOf("home" to "/home/${newNodeName.lowercase()}")
                            else -> emptyMap()
                        }
                    )

                    if (treeManager.addNode(selectedParentId, newNode)) {
                        newNodeName = ""
                    }
                }
            }
        )

        // Tree Actions Card
        TreeActionsCard(treeManager)
    }
}

@Composable
fun TreeStatisticsCard(treeManager: TreeManager) {
    val stats by remember { derivedStateOf { treeManager.getTreeStatistics() } }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF161B22)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 Tree Statistics",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TreeStatItem(
                    label = "Total Nodes",
                    value = stats.totalNodes.toString(),
                    icon = "🌳"
                )

                TreeStatItem(
                    label = "Depth",
                    value = stats.depth.toString(),
                    icon = "📏"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TreeStatItem(
                    label = "Branches",
                    value = stats.branches.toString(),
                    icon = "🌿"
                )

                TreeStatItem(
                    label = "Leaves",
                    value = stats.leaves.toString(),
                    icon = "🍃"
                )
            }
        }
    }
}

@Composable
fun TreeStatItem(label: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            color = Color(0xFF00D4FF)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8B949E)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNodeCard(
    newNodeName: String,
    onNewNodeNameChange: (String) -> Unit,
    selectedNodeType: NodeType,
    onNodeTypeSelected: (NodeType) -> Unit,
    selectedParentId: String,
    onParentSelected: (String) -> Unit,
    treeManager: TreeManager,
    onAddNode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF161B22)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "➕ Add New Node",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            OutlinedTextField(
                value = newNodeName,
                onValueChange = onNewNodeNameChange,
                label = { Text("Node Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Node Type Selection
            Text(
                text = "Node Type:",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF8B949E)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SimpleChip(
                    label = "Branch",
                    selected = selectedNodeType == NodeType.BRANCH,
                    onClick = { onNodeTypeSelected(NodeType.BRANCH) },
                    icon = "🌿"
                )

                SimpleChip(
                    label = "Leaf",
                    selected = selectedNodeType == NodeType.LEAF,
                    onClick = { onNodeTypeSelected(NodeType.LEAF) },
                    icon = "🍃"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SimpleChip(
                    label = "Process",
                    selected = selectedNodeType == NodeType.PROCESS,
                    onClick = { onNodeTypeSelected(NodeType.PROCESS) },
                    icon = "⚙️"
                )

                SimpleChip(
                    label = "File",
                    selected = selectedNodeType == NodeType.FILE,
                    onClick = { onNodeTypeSelected(NodeType.FILE) },
                    icon = "📄"
                )
            }

            // Add Node Button
            Button(
                onClick = onAddNode,
                modifier = Modifier.fillMaxWidth(),
                enabled = newNodeName.isNotEmpty()
            ) {
                Text("➕ Add ${selectedNodeType.name.lowercase().replaceFirstChar { it.uppercase() }}")
            }
        }
    }
}

@Composable
fun SimpleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: String
) {
    val backgroundColor = if (selected) Color(0xFF00D4FF) else Color(0xFF30363D)
    val textColor = if (selected) Color.Black else Color.White

    Card(
        modifier = Modifier
            .clickable { onClick() },  // FIXED: Added clickable modifier
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = textColor, fontSize = 14.sp)
        }
    }
}

@Composable
fun TreeActionsCard(treeManager: TreeManager) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF161B22)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⚡ Tree Actions",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    label = "Expand All",
                    onClick = { treeManager.expandAll() },
                    modifier = Modifier.weight(1f),
                    icon = "⬇️"
                )

                ActionButton(
                    label = "Collapse All",
                    onClick = { treeManager.collapseAll() },
                    modifier = Modifier.weight(1f),
                    icon = "⬆️"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    label = "Clear Tree",
                    onClick = {
                        // Keep only the root node
                        treeManager.root.children.clear()
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFF85149),
                    icon = "🗑️"
                )

                ActionButton(
                    label = "Rebuild",
                    onClick = {
                        // Call public method instead
                        treeManager.rebuildTree()
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFF9400D3),
                    icon = "🔄"
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF00D4FF),
    icon: String = ""
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        )
    ) {
        if (icon.isNotEmpty()) {
            Text(icon)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(label)
    }
}