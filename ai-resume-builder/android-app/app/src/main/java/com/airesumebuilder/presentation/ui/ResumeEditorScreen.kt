package com.airesumebuilder.presentation.ui

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import com.airesumebuilder.presentation.viewmodel.ResumeViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeEditorScreen(
    resumeId: String,
    onNavigateBack: () -> Unit,
    viewModel: ResumeViewModel = koinViewModel()
) {
    val currentResume by viewModel.currentResume.collectAsState()
    val context = LocalContext.current

    var content by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var hasChanges by remember { mutableStateOf(false) }

    LaunchedEffect(resumeId) {
        viewModel.loadResume(resumeId)
    }

    LaunchedEffect(currentResume) {
        currentResume?.let {
            if (content.isEmpty()) {
                content = it.content
                title = it.title
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Edit Resume",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                hasChanges = true
            },
            label = { Text("Resume Title") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = content,
            onValueChange = {
                content = it
                hasChanges = true
            },
            label = { Text("Resume Content") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.updateResume(resumeId, title, content, "modern")
                        hasChanges = false
                        Toast.makeText(context, "Resume saved", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = hasChanges && title.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save")
            }

            OutlinedButton(
                onClick = {
                    generatePdf(context, title, content)
                    Toast.makeText(context, "PDF exported", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export PDF")
            }
        }
    }
}

private fun generatePdf(context: Context, title: String, content: String) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val titlePaint = Paint().apply {
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = Color.BLACK
    }

    val contentPaint = Paint().apply {
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        color = Color.BLACK
    }

    canvas.drawText(title, 40f, 50f, titlePaint)

    val lines = content.split("\n")
    var y = 90f
    val pageHeight = 800f
    val leftMargin = 40f
    val maxWidth = 515f

    for (line in lines) {
        if (y > pageHeight) break

        if (line.trim().startsWith("#") || line.trim().all { it.isUpperCase() || it.isWhitespace() }) {
            // Section header
            val headerPaint = Paint().apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.rgb(0, 51, 102)
            }
            canvas.drawText(line.replace("#", "").trim(), leftMargin, y, headerPaint)
            y += 20f
        } else {
            // Wrap text
            val words = line.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (contentPaint.measureText(testLine) > maxWidth) {
                    canvas.drawText(currentLine, leftMargin, y, contentPaint)
                    y += 15f
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, leftMargin, y, contentPaint)
                y += 15f
            }
        }
    }

    pdfDocument.finishPage(page)

    val fileName = "${title.replace(" ", "_")}_resume.pdf"
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()
}
