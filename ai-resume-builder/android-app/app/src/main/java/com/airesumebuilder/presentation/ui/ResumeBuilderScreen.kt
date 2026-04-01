package com.airesumebuilder.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airesumebuilder.domain.model.*
import com.airesumebuilder.presentation.viewmodel.ResumeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeBuilderScreen(
    onNavigateBack: () -> Unit,
    onGenerated: (String) -> Unit,
    viewModel: ResumeViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var projects by remember { mutableStateOf("") }
    var certifications by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(ResumeTemplate.MODERN) }
    var showTemplateDropdown by remember { mutableStateOf(false) }

    val resumeState by viewModel.resumeState.collectAsState()

    LaunchedEffect(resumeState) {
        if (resumeState is Resource.Success) {
            val response = (resumeState as Resource.Success).data
            onGenerated(response.resumeId)
            viewModel.clearResumeState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Build Your Resume",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Template Selector
        ExposedDropdownMenuBox(
            expanded = showTemplateDropdown,
            onExpandedChange = { showTemplateDropdown = it }
        ) {
            OutlinedTextField(
                value = selectedTemplate.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Template") },
                leadingIcon = { Icon(Icons.Default.Style, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTemplateDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
                expanded = showTemplateDropdown,
                onDismissRequest = { showTemplateDropdown = false }
            ) {
                ResumeTemplate.entries.forEach { template ->
                    DropdownMenuItem(
                        text = { Text(template.displayName) },
                        onClick = {
                            selectedTemplate = template
                            showTemplateDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FormField(value = name, onValueChange = { name = it }, label = "Full Name", icon = Icons.Default.Person)

        Spacer(modifier = Modifier.height(12.dp))

        FormField(value = education, onValueChange = { education = it }, label = "Education", icon = Icons.Default.School, minLines = 2)

        Spacer(modifier = Modifier.height(12.dp))

        FormField(value = experience, onValueChange = { experience = it }, label = "Experience", icon = Icons.Default.Work, minLines = 3)

        Spacer(modifier = Modifier.height(12.dp))

        FormField(value = skills, onValueChange = { skills = it }, label = "Skills", icon = Icons.Default.Star, minLines = 2)

        Spacer(modifier = Modifier.height(12.dp))

        FormField(value = projects, onValueChange = { projects = it }, label = "Projects (optional)", icon = Icons.Default.Code, minLines = 2)

        Spacer(modifier = Modifier.height(12.dp))

        FormField(value = certifications, onValueChange = { certifications = it }, label = "Certifications (optional)", icon = Icons.Default.Certificate, minLines = 2)

        if (resumeState is Resource.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (resumeState as Resource.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.generateResume(
                    GenerateResumeRequest(name, education, experience, skills, projects, certifications, selectedTemplate.name.lowercase())
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = name.isNotBlank() && education.isNotBlank() && experience.isNotBlank() && skills.isNotBlank() && resumeState !is Resource.Loading
        ) {
            if (resumeState is Resource.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Resume", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        minLines = minLines
    )
}
