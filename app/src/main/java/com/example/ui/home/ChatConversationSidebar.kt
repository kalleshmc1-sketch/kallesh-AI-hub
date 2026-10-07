package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.UiState

/**
 * Compact horizontal strip displayed beneath the chat header so users can always see recent
 * conversation titles, switch between chats in 1 tap, delete an old conversation in 1 tap,
 * or open the full vertical ChatConversationSidebar.
 */
@Composable
fun RecentConversationsQuickSidebarBar(
    conversations: List<Conversation>,
    activeConversationId: String?,
    isSidebarOpen: Boolean,
    onToggleSidebar: () -> Unit,
    onSelectConversation: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onNewChat: () -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recent_conversations_quick_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sidebar Drawer Toggle Pill
            Surface(
                onClick = onToggleSidebar,
                shape = RoundedCornerShape(10.dp),
                color = if (isSidebarOpen) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                modifier = Modifier.testTag("open_chat_sidebar_strip_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Toggle Recent Chats Sidebar",
                        tint = if (isSidebarOpen) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isSidebarOpen) "Hide Sidebar" else "Sidebar (${conversations.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSidebarOpen) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }

            // Recent Conversation Title Pills with 1-tap Switch & Delete
            conversations.take(12).forEach { conv ->
                val isSelected = conv.id == activeConversationId
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                    modifier = Modifier.testTag("quick_switch_conv_${conv.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { onSelectConversation(conv.id) }
                            .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (conv.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = conv.title.take(28) + if (conv.title.length > 28) "…" else "",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1
                        )
                        IconButton(
                            onClick = { onDeleteConversation(conv.id) },
                            modifier = Modifier
                                .size(22.dp)
                                .testTag("quick_delete_conv_${conv.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete conversation ${conv.title}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // Quick + New Chat Chip
            Surface(
                onClick = onNewChat,
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.testTag("quick_strip_new_chat_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "New",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Responsive Sidebar Wrapper that renders `ChatConversationSidebar` as:
 * - A docked left-hand sidebar column alongside the main chat transcript on wide/tablet layouts
 * - A slide-in left sidebar drawer with a translucent scrim on compact handheld layouts
 */
@Composable
fun ResponsiveChatSidebarLayout(
    isSidebarOpen: Boolean,
    conversationsState: UiState<List<Conversation>>,
    allPersistedMessages: List<ChatMessage>,
    activeConversationId: String?,
    searchQuery: String,
    selectedFolder: String,
    onSearchChange: (String) -> Unit,
    onSelectFolder: (String) -> Unit,
    onSelectConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    onPinToggle: (Conversation) -> Unit,
    onRename: (Conversation) -> Unit,
    onShareToggle: (Conversation) -> Unit,
    onDelete: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier,
    chatContent: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 680.dp

        if (isExpandedScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = isSidebarOpen,
                    enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                ) {
                    Row(modifier = Modifier.fillMaxHeight()) {
                        ChatConversationSidebar(
                            conversationsState = conversationsState,
                            allPersistedMessages = allPersistedMessages,
                            activeConversationId = activeConversationId,
                            searchQuery = searchQuery,
                            selectedFolder = selectedFolder,
                            onSearchChange = onSearchChange,
                            onSelectFolder = onSelectFolder,
                            onSelectConversation = onSelectConversation,
                            onNewChat = onNewChat,
                            onPinToggle = onPinToggle,
                            onRename = onRename,
                            onShareToggle = onShareToggle,
                            onDelete = onDelete,
                            onClearAllHistory = onClearAllHistory,
                            onCloseSidebar = onCloseSidebar,
                            modifier = Modifier
                                .width(300.dp)
                                .fillMaxHeight()
                        )
                        VerticalDivider()
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    chatContent()
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                chatContent()

                // Backdrop Scrim when Sidebar is open on compact screens
                AnimatedVisibility(
                    visible = isSidebarOpen,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.48f))
                            .clickable(onClick = onCloseSidebar)
                            .testTag("chat_sidebar_scrim")
                    )
                }

                // Left Slide-In Conversation Sidebar Drawer
                AnimatedVisibility(
                    visible = isSidebarOpen,
                    enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    ChatConversationSidebar(
                        conversationsState = conversationsState,
                        allPersistedMessages = allPersistedMessages,
                        activeConversationId = activeConversationId,
                        searchQuery = searchQuery,
                        selectedFolder = selectedFolder,
                        onSearchChange = onSearchChange,
                        onSelectFolder = onSelectFolder,
                        onSelectConversation = { id ->
                            onSelectConversation(id)
                            onCloseSidebar()
                        },
                        onNewChat = {
                            onNewChat()
                            onCloseSidebar()
                        },
                        onPinToggle = onPinToggle,
                        onRename = onRename,
                        onShareToggle = onShareToggle,
                        onDelete = onDelete,
                        onClearAllHistory = onClearAllHistory,
                        onCloseSidebar = onCloseSidebar,
                        modifier = Modifier
                            .width(305.dp)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Vertical Chat Conversation Sidebar Component.
 * Displays recent conversation titles, search, folders, pin/rename actions,
 * 1-tap switching between conversations, and deleting individual or all old conversations.
 */
@Composable
fun ChatConversationSidebar(
    conversationsState: UiState<List<Conversation>>,
    allPersistedMessages: List<ChatMessage>,
    activeConversationId: String?,
    searchQuery: String,
    selectedFolder: String,
    onSearchChange: (String) -> Unit,
    onSelectFolder: (String) -> Unit,
    onSelectConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    onPinToggle: (Conversation) -> Unit,
    onRename: (Conversation) -> Unit,
    onShareToggle: (Conversation) -> Unit,
    onDelete: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onCloseSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allConversations = (conversationsState as? UiState.Success)?.data.orEmpty()
    val matchingMessagesByConvId = remember(allPersistedMessages, searchQuery) {
        if (searchQuery.isBlank()) emptyMap()
        else {
            allPersistedMessages
                .filter { it.content.contains(searchQuery, ignoreCase = true) }
                .groupBy { it.conversationId }
        }
    }
    val filtered = remember(allConversations, searchQuery, selectedFolder, matchingMessagesByConvId) {
        allConversations
            .filter { conv ->
                val folderMatch = when {
                    selectedFolder.equals("All", ignoreCase = true) -> true
                    selectedFolder.equals("Pinned", ignoreCase = true) -> conv.isPinned
                    else -> conv.folder.equals(selectedFolder, ignoreCase = true)
                }
                folderMatch &&
                    (searchQuery.isBlank() ||
                        conv.title.contains(searchQuery, ignoreCase = true) ||
                        conv.lastMessagePreview.contains(searchQuery, ignoreCase = true) ||
                        matchingMessagesByConvId.containsKey(conv.id))
            }
            .sortedWith(
                compareByDescending<Conversation> { it.isPinned }
                    .thenByDescending { it.updatedAt?.seconds ?: 0L }
            )
    }

    val pinnedConversations = remember(filtered) { filtered.filter { it.isPinned } }
    val recentUnpinnedConversations = remember(filtered) { filtered.filterNot { it.isPinned } }

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .testTag("chat_conversation_sidebar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .testTag("chat_history_panel")
        ) {
            // Sidebar Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(7.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Recent Conversations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${allConversations.size} saved • ${allPersistedMessages.size} msgs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onCloseSidebar,
                    modifier = Modifier.testTag("sidebar_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Sidebar"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent + New Conversation Button
            Button(
                onClick = onNewChat,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sidebar_new_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Conversation", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Conversations Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Filter recent conversation titles...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_conversations_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Folder Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Pinned", "General", "Coding", "Study", "Business", "Creative").forEach { folder ->
                    FilterChip(
                        selected = selectedFolder.equals(folder, ignoreCase = true),
                        onClick = { onSelectFolder(folder) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (folder == "Pinned") Icons.Default.PushPin else Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        label = { Text(folder, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable Conversation Titles List
            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching conversations." else "No saved conversations yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Start a new chat to automatically save your conversation history.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("sidebar_conversations_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    if (pinnedConversations.isNotEmpty()) {
                        item {
                            Text(
                                text = "PINNED CONVERSATIONS (${pinnedConversations.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        items(pinnedConversations, key = { "pinned_${it.id}" }) { conv ->
                            SidebarConversationItemCard(
                                conv = conv,
                                isActive = conv.id == activeConversationId,
                                matchedMessageSnippet = matchingMessagesByConvId[conv.id]?.firstOrNull()?.content,
                                searchQuery = searchQuery,
                                onSelect = { onSelectConversation(conv.id) },
                                onPinToggle = { onPinToggle(conv) },
                                onRename = { onRename(conv) },
                                onShareToggle = { onShareToggle(conv) },
                                onDelete = { onDelete(conv.id) }
                            )
                        }
                    }

                    if (recentUnpinnedConversations.isNotEmpty()) {
                        item {
                            Text(
                                text = "RECENT CONVERSATIONS (${recentUnpinnedConversations.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            )
                        }
                        items(recentUnpinnedConversations, key = { it.id }) { conv ->
                            SidebarConversationItemCard(
                                conv = conv,
                                isActive = conv.id == activeConversationId,
                                matchedMessageSnippet = matchingMessagesByConvId[conv.id]?.firstOrNull()?.content,
                                searchQuery = searchQuery,
                                onSelect = { onSelectConversation(conv.id) },
                                onPinToggle = { onPinToggle(conv) },
                                onRename = { onRename(conv) },
                                onShareToggle = { onShareToggle(conv) },
                                onDelete = { onDelete(conv.id) }
                            )
                        }
                    }
                }
            }

            // Sidebar Footer: Clear All Old Conversations
            if (allConversations.isNotEmpty()) {
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onClearAllHistory,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clear_all_chat_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Clear All Conversations",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarConversationItemCard(
    conv: Conversation,
    isActive: Boolean,
    matchedMessageSnippet: String?,
    searchQuery: String,
    onSelect: () -> Unit,
    onPinToggle: () -> Unit,
    onRename: () -> Unit,
    onShareToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            }
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                },
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("history_conversation_item_${conv.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    } else if (conv.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = conv.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            val previewLine = if (!matchedMessageSnippet.isNullOrBlank() && searchQuery.isNotBlank()) {
                "Match: \"${matchedMessageSnippet.replace("\n", " ").take(80)}\""
            } else {
                conv.lastMessagePreview.ifBlank { "Tap to switch to this conversation" }
            }

            Text(
                text = previewLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${conv.folder} • ${conv.messageCount} msgs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onPinToggle,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("sidebar_pin_chat_${conv.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (conv.isPinned) "Unpin Conversation" else "Pin Conversation",
                            tint = if (conv.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = onRename,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("sidebar_rename_chat_${conv.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename Conversation",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = onShareToggle,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("sidebar_share_chat_${conv.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Conversation",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("sidebar_delete_chat_${conv.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Conversation ${conv.title}",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
