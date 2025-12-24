package com.glassbox.os

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// TREE NODE - Core data structure
data class TreeNode(
    val id: String,
    val name: String,
    val type: NodeType,
    var parentId: String? = null, // null means this is root
    var children: SnapshotStateList<TreeNode> = mutableStateListOf(),
    var isExpanded: Boolean = false,
    var data: Map<String, Any> = emptyMap()
) {
    // Add a child node
    fun addChild(child: TreeNode) {
        // Create a copy with updated parentId
        val updatedChild = child.copy(
            children = child.children,
            data = child.data
        )
        updatedChild.children.forEach { it.parentId = updatedChild.id }
        children.add(updatedChild)
    }

    // Remove a child node
    fun removeChild(childId: String): Boolean {
        return children.removeAll { it.id == childId }
    }

    // Check if this node has children
    fun hasChildren(): Boolean = children.isNotEmpty()

    // Get node path (e.g., "root/control_panel/cpu")
    fun getPath(treeManager: TreeManager): String {
        val path = mutableListOf(name)
        var currentParentId = parentId

        while (currentParentId != null) {
            val parent = treeManager.findNode(currentParentId)
            parent?.let {
                path.add(0, it.name)
                currentParentId = it.parentId
            } ?: break
        }

        return path.joinToString("/")
    }
}

enum class NodeType {
    ROOT,      // OS itself
    BRANCH,    // Major feature
    LEAF,      // Individual component
    PROCESS,   // Running process
    FILE,      // File/directory
    USER,      // User account
    NETWORK,   // Network interface
    DEVICE     // Hardware device
}

// TREE MANAGER - Manages the entire OS tree
class TreeManager {
    // ROOT of our entire OS tree - USING var INSTEAD OF val
    var root = TreeNode(
        id = "glassbox_root",
        name = "🌳 GlassBox OS",
        type = NodeType.ROOT,
        data = mapOf(
            "version" to "1.0.0",
            "status" to "running",
            "uptime" to "2h 15m"
        )
    )

    private val _treeUpdates = MutableStateFlow(0)
    val treeUpdates: StateFlow<Int> = _treeUpdates

    init {
        // Build initial tree structure
        rebuildTree()
    }

    // PUBLIC METHOD for rebuilding tree - FIXED: creates new root instead of reassigning
    fun rebuildTree() {
        // Create new root instead of reassigning the same val
        val newRoot = TreeNode(
            id = "glassbox_root",
            name = "🌳 GlassBox OS",
            type = NodeType.ROOT,
            data = mapOf(
                "version" to "1.0.0",
                "status" to "running",
                "uptime" to "2h 15m"
            )
        )

        // Build tree structure
        buildTreeStructure(newRoot)

        // Update the root reference
        root = newRoot
        _treeUpdates.value++
    }

    private fun buildTreeStructure(currentRoot: TreeNode) {
        // Add BRANCHES to root
        val controlBranch = TreeNode(
            id = "branch_control",
            name = "⚙️ Control Panel",
            type = NodeType.BRANCH,
            data = mapOf("icon" to "dashboard", "enabled" to true)
        )

        val terminalBranch = TreeNode(
            id = "branch_terminal",
            name = "💻 Terminal",
            type = NodeType.BRANCH,
            data = mapOf("icon" to "terminal", "session_count" to 1)
        )

        val filesystemBranch = TreeNode(
            id = "branch_filesystem",
            name = "📁 File System",
            type = NodeType.BRANCH,
            data = mapOf("total_size" to "4GB", "used" to "1.2GB")
        )

        val processBranch = TreeNode(
            id = "branch_processes",
            name = "🔄 Process Tree",
            type = NodeType.BRANCH,
            data = mapOf("process_count" to 23, "cpu_usage" to "12%")
        )

        val networkBranch = TreeNode(
            id = "branch_network",
            name = "🌐 Network",
            type = NodeType.BRANCH,
            data = mapOf("interfaces" to 2, "connected" to true)
        )

        val usersBranch = TreeNode(
            id = "branch_users",
            name = "👥 Users",
            type = NodeType.BRANCH,
            data = mapOf("count" to 1, "active" to 1)
        )

        // Add branches to root
        currentRoot.addChild(controlBranch)
        currentRoot.addChild(terminalBranch)
        currentRoot.addChild(filesystemBranch)
        currentRoot.addChild(processBranch)
        currentRoot.addChild(networkBranch)
        currentRoot.addChild(usersBranch)

        // Add LEAVES to Control Panel branch
        controlBranch.addChild(TreeNode("leaf_cpu", "📊 CPU Monitor", NodeType.LEAF,
            data = mapOf("usage" to "12%", "cores" to 4)))
        controlBranch.addChild(TreeNode("leaf_ram", "💾 RAM Manager", NodeType.LEAF,
            data = mapOf("total" to "8GB", "used" to "2.4GB")))
        controlBranch.addChild(TreeNode("leaf_python", "🐍 Python 3.11", NodeType.LEAF,
            data = mapOf("version" to "3.11.0", "enabled" to true)))
        controlBranch.addChild(TreeNode("leaf_gui", "🖥️ GUI Desktop", NodeType.LEAF,
            data = mapOf("enabled" to false, "type" to "XFCE")))
        controlBranch.addChild(TreeNode("leaf_docker", "🐳 Docker Support", NodeType.LEAF,
            data = mapOf("enabled" to true, "containers" to 3)))

        // Add LEAVES to Terminal branch
        terminalBranch.addChild(TreeNode("leaf_bash", "💲 Bash Shell", NodeType.LEAF))
        terminalBranch.addChild(TreeNode("leaf_python_repl", "🐍 Python REPL", NodeType.LEAF))
        terminalBranch.addChild(TreeNode("leaf_commands", "📜 Command History", NodeType.LEAF))

        // Add LEAVES to File System branch
        val homeDir = TreeNode("dir_home", "🏠 /home", NodeType.FILE,
            data = mapOf("type" to "directory", "size" to "1.2GB"))
        filesystemBranch.addChild(homeDir)
        filesystemBranch.addChild(TreeNode("dir_etc", "⚙️ /etc", NodeType.FILE,
            data = mapOf("type" to "directory", "size" to "48MB")))
        filesystemBranch.addChild(TreeNode("dir_var", "📦 /var", NodeType.FILE,
            data = mapOf("type" to "directory", "size" to "256MB")))

        // Add subdirectories to home
        homeDir.addChild(TreeNode("dir_user", "👤 glassbox", NodeType.FILE))
        homeDir.children[0].addChild(TreeNode("dir_desktop", "🖥️ Desktop", NodeType.FILE))
        homeDir.children[0].addChild(TreeNode("dir_documents", "📄 Documents", NodeType.FILE))
        homeDir.children[0].addChild(TreeNode("dir_downloads", "⬇️ Downloads", NodeType.FILE))

        // Add LEAVES to Process Tree branch
        val initProcess = TreeNode("process_init", "init (PID: 1)", NodeType.PROCESS,
            data = mapOf("pid" to 1, "status" to "running", "user" to "root"))
        processBranch.addChild(initProcess)

        val systemdProcess = TreeNode("process_systemd", "systemd (PID: 100)", NodeType.PROCESS,
            data = mapOf("pid" to 100, "status" to "running", "user" to "root"))
        initProcess.addChild(systemdProcess)

        systemdProcess.addChild(TreeNode("process_bash", "bash (PID: 101)", NodeType.PROCESS,
            data = mapOf("pid" to 101, "status" to "running", "user" to "glassbox")))
        systemdProcess.addChild(TreeNode("process_python", "python3 (PID: 102)", NodeType.PROCESS,
            data = mapOf("pid" to 102, "status" to "running", "user" to "glassbox")))

        // Add LEAVES to Network branch
        networkBranch.addChild(TreeNode("interface_eth0", "🔌 eth0", NodeType.NETWORK,
            data = mapOf("ip" to "192.168.1.100", "status" to "up")))
        networkBranch.addChild(TreeNode("interface_wlan0", "📶 wlan0", NodeType.NETWORK,
            data = mapOf("ip" to "192.168.1.101", "status" to "up")))

        // Add LEAVES to Users branch
        usersBranch.addChild(TreeNode("user_glassbox", "👤 glassbox", NodeType.USER,
            data = mapOf("home" to "/home/glassbox", "shell" to "/bin/bash")))
    }

    // Find a node by ID (Depth-First Search)
    fun findNode(id: String, startNode: TreeNode = root): TreeNode? {
        if (startNode.id == id) return startNode

        for (child in startNode.children) {
            val found = findNode(id, child)
            if (found != null) return found
        }

        return null
    }

    // Find nodes by name (supports partial matches)
    fun findNodesByName(name: String): List<TreeNode> {
        val results = mutableListOf<TreeNode>()
        searchNodesByName(root, name.lowercase(), results)
        return results
    }

    private fun searchNodesByName(node: TreeNode, query: String, results: MutableList<TreeNode>) {
        if (node.name.lowercase().contains(query)) {
            results.add(node)
        }

        for (child in node.children) {
            searchNodesByName(child, query, results)
        }
    }

    // Add a new node
    fun addNode(parentId: String, newNode: TreeNode): Boolean {
        val parent = findNode(parentId) ?: return false
        parent.addChild(newNode)
        _treeUpdates.value++
        return true
    }

    // Remove a node
    fun removeNode(id: String): Boolean {
        if (id == root.id) return false // Cannot remove root

        val node = findNode(id) ?: return false
        val parent = findNode(node.parentId ?: "") ?: return false

        val removed = parent.removeChild(id)
        if (removed) {
            _treeUpdates.value++
        }
        return removed
    }

    // Expand/Collapse a branch
    fun toggleNode(id: String) {
        val node = findNode(id)
        node?.isExpanded = !(node?.isExpanded ?: false)
        _treeUpdates.value++
    }

    // Expand all nodes
    fun expandAll(node: TreeNode = root) {
        node.isExpanded = true
        node.children.forEach { expandAll(it) }
        _treeUpdates.value++
    }

    // Collapse all nodes
    fun collapseAll(node: TreeNode = root) {
        node.isExpanded = false
        node.children.forEach { collapseAll(it) }
        _treeUpdates.value++
    }

    // Get tree depth
    fun getDepth(node: TreeNode = root): Int {
        if (node.children.isEmpty()) return 1
        return 1 + (node.children.maxOfOrNull { getDepth(it) } ?: 0)
    }

    // Get total node count
    fun getTotalNodes(node: TreeNode = root): Int {
        var count = 1
        for (child in node.children) {
            count += getTotalNodes(child)
        }
        return count
    }

    // Get leaf count
    fun getLeafCount(node: TreeNode = root): Int {
        if (node.children.isEmpty()) return 1

        var count = 0
        for (child in node.children) {
            count += getLeafCount(child)
        }
        return count
    }

    // Get branch count
    fun getBranchCount(node: TreeNode = root): Int {
        var count = if (node.type == NodeType.BRANCH) 1 else 0

        for (child in node.children) {
            count += getBranchCount(child)
        }
        return count
    }

    // Get all nodes at a specific level
    fun getNodesAtLevel(level: Int): List<TreeNode> {
        val result = mutableListOf<TreeNode>()
        collectNodesAtLevel(root, 0, level, result)
        return result
    }

    private fun collectNodesAtLevel(
        node: TreeNode,
        currentLevel: Int,
        targetLevel: Int,
        result: MutableList<TreeNode>
    ) {
        if (currentLevel == targetLevel) {
            result.add(node)
            return
        }

        for (child in node.children) {
            collectNodesAtLevel(child, currentLevel + 1, targetLevel, result)
        }
    }

    // Get tree statistics
    fun getTreeStatistics(): TreeStatistics {
        return TreeStatistics(
            totalNodes = getTotalNodes(),
            depth = getDepth(),
            branches = getBranchCount(),
            leaves = getLeafCount(),
            rootChildren = root.children.size
        )
    }

    // Move node to new parent
    fun moveNode(nodeId: String, newParentId: String): Boolean {
        val node = findNode(nodeId) ?: return false
        val newParent = findNode(newParentId) ?: return false

        // Cannot move node to its own descendant
        if (isDescendant(newParentId, nodeId)) return false

        // Remove from old parent
        val oldParent = findNode(node.parentId ?: "") ?: return false
        oldParent.removeChild(nodeId)

        // Add to new parent
        // Create a copy with updated parentId
        val updatedNode = node.copy(
            parentId = newParentId,
            children = node.children,
            data = node.data
        )
        newParent.addChild(updatedNode)

        _treeUpdates.value++
        return true
    }

    // Check if node is descendant of another
    private fun isDescendant(parentId: String, childId: String): Boolean {
        val parent = findNode(parentId) ?: return false
        return findNode(childId, parent) != null
    }
}

// Tree Statistics Data Class
data class TreeStatistics(
    val totalNodes: Int,
    val depth: Int,
    val branches: Int,
    val leaves: Int,
    val rootChildren: Int
)