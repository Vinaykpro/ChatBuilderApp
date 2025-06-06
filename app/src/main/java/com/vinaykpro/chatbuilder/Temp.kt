package com.vinaykpro.chatbuilder

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


var months = arrayOf(
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December"
)

fun openFile(uri: Uri?, context : Context){
    if (uri == null) {
        Toast.makeText(context, "File not found.", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        // Open the file stream from the URI
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {
            // Check if it's a ZIP file
            val zipInputStream = ZipInputStream(inputStream)
            val fileList = mutableListOf<String>()

            // Read the entries in the ZIP file
            var zipEntry: ZipEntry? = zipInputStream.nextEntry
            while (zipEntry != null) {
                val fileName = zipEntry.name
                val fileSize = zipEntry.size

                // Add file details to the list
                fileList.add("Name: $fileName, Size: ${if (fileSize != -1L) "$fileSize bytes" else "Unknown"}")


                //f = new File(uri.toString());
                val messageList = ArrayList<String>();
//                datesList.clear()
//                ss = ""
//                temp = ""
//                temp0 = ""

                if (fileName.endsWith(".txt", ignoreCase = true)) {
                    val fileContents = StringBuilder()

                    // Read the .txt file line by line
                    val reader = BufferedReader(InputStreamReader(zipInputStream))
                    var line: String? = reader.readLine()
                    while (line != null) {
                        fileContents.append(line).append("\n")
                        line = reader.readLine()
                    }

                    // Display the contents of the .txt file
                    Toast.makeText(context, "Contents of $fileName:\n$fileContents", Toast.LENGTH_LONG).show()
                }

                // Move to the next entry
                zipEntry = zipInputStream.nextEntry
            }

            zipInputStream.close()

            // Display the file list
            if (fileList.isNotEmpty()) {
                Toast.makeText(context, fileList.joinToString("\n"), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "No files found in the ZIP.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Unable to open the file.", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error processing the file.", Toast.LENGTH_SHORT).show()
    }

    /*val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, context.contentResolver.getType(uri))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    // Check if there is an app to handle the intent
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "No application found to open this file.", Toast.LENGTH_SHORT).show()
    }*/
}

@Composable
fun FileProcessorScreen(uri: Uri, zipFileName: String, context: Context) {
    val messageList = remember { mutableStateListOf<String>() }
    val datesList = remember { mutableStateListOf<String>() }
    var chatName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        processFile(uri, zipFileName, context, messageList, datesList) { name, error ->
            chatName = name
            errorMessage = error
            isLoading = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Chat Name: $chatName", style = MaterialTheme.typography.bodySmall)

            LazyColumn {
                items(messageList) { message ->
                    Text(message)
                }
            }

            if (!errorMessage.isNullOrEmpty()) {
                Snackbar {
                    Text(errorMessage!!)
                }
            }
        }
    }
}

suspend fun processFile(
    uri: Uri,
    zipFileName: String,
    context: Context,
    messageList: MutableList<String>,
    datesList: MutableList<String>,
    onResult: (chatName: String, error: String?) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream!!))

            var isChatFromIPhone = false
            var isFileValid = false
            var tempMessage = ""
            var currentDate = ""
            val names = mutableListOf<String>()

            while (reader.ready()) {
                val line = reader.readLine()
                if (line.isNullOrBlank()) continue

                if (line.startsWith("[") && !isChatFromIPhone) {
                    isChatFromIPhone = true
                }

                if (canGetMessage(line) && canGetName(line) && canGetTime(line)) {
                    val name = getName(line)
                    if (!names.contains(name)) name?.let { names.add(it) }

                    if (canGetDate(tempMessage)) {
                        currentDate = getDate(tempMessage)
                        datesList.add(currentDate)
                    }

                    messageList.add(tempMessage)
                    tempMessage = line
                    isFileValid = true
                } else {
                    tempMessage += "\n$line"
                }
            }

            if (!isFileValid) {
                onResult("", "Invalid file format")
            } else {
                val chatName = generateChatName(zipFileName, names)
                onResult(chatName, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult("", "Error: ${e.message}")
        }
    }
}

private fun canGetMessage(s: String): Boolean {
    val a = s.indexOf("-")
    val b = s.indexOf(":", a)
    return a != -1 && b != -1
}

private fun canGetName(s: String): Boolean {
    val a = s.indexOf("-")
    val b = s.indexOf(":", a)
    return a != -1 && b != -1
}

private fun canGetTime(s: String): Boolean {
    val a = s.indexOf(",")
    val b = s.indexOf("-", a)
    return a != -1 && b != -1
}

private fun canGetDate(s: String): Boolean {
    val commaindex = s.indexOf(",")
    var condition = false
    if (commaindex > 0 && commaindex <= 10 && s.indexOf('/') <= 9) {
        val date = s.substring(0, commaindex)
        val test = date.split("/".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        if (test.size == 3) {
            condition = true
        }
    }
    return condition
}

private fun getDate(s: String): String {
    return s.substring(0, s.indexOf(","))
}

private fun getReadableDate(s: String): String? {
    var s = s
    var day = s.substring(0, s.indexOf("/")).toInt()
    s = s.substring(s.indexOf("/") + 1)
    var month = s.substring(0, s.indexOf("/")).toInt()
    if (month > 12 && day <= 12) {
        /*useseconddateformat = true;
            changedatestillnow = true;*/
        val temp = month
        month = day
        day = temp
    } /* else if (useseconddateformat && day <= 12) {
            int temp = month;
            month = day;
            day = temp;
        }*/
    s = s.substring(s.indexOf("/") + 1)
    val year = s.toInt()
    return if (year < 100) {
        /*if(!changedatestillnow) {
                    readabledates.add(day + " " + months[month - 1] + " 20" + year);
                }*/
        day.toString() + " " + months.get(month - 1) + " 20" + year
    } else {
        /*if(!changedatestillnow) {
                    readabledates.add(day + " " + months[month - 1] + " " + year);
                }*/
        day.toString() + " " + months.get(month - 1) + " " + year
    }
}

fun getFlippedDate(s: String): String? {
    val date = s.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val day = date[0].toInt()
    val month = getMonthIndex(date[1])
    return getReadableDate(month.toString() + "/" + day + "/" + date[2])
}

fun getMonthIndex(s: String): Int {
    var index = 1
    for (i in months.indices) {
        if (s == months.get(i)) {
            index = i + 1
        }
    }
    return index
}

private fun getMessage(s: String): String? {
    val a = s.indexOf("-")
    val b = s.indexOf(":", a)
    return s.substring(b + 2)
}

private fun getName(s: String): String? {
    val a = s.indexOf("-")
    val b = s.indexOf(":", a)
    return s.substring(a + 2, b)
}

fun generateRandomTableName(len: Int): String? {
    // ASCII range – alphanumeric (0-9, a-z, A-Z)
    val chars = "abcdefghijklmnopqrstuvwxyz"
    val random = SecureRandom()
    val sb = java.lang.StringBuilder()

    // each iteration of the loop randomly chooses a character from the given
    // ASCII range and appends it to the `StringBuilder` instance
    for (i in 0 until len) {
        val randomIndex = random.nextInt(chars.length)
        sb.append(chars[randomIndex])
    }
    return sb.toString()
}

fun generateChatName(zipFileName: String, names: List<String>): String {
    return if (names.size == 1) names[0] else zipFileName
}

@Composable
fun ListItem(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ChangeStatusBarColor(color: Color, darkIcons: Boolean) {
    val context = LocalContext.current
    val window = (context as? ComponentActivity)?.window
    val view = LocalView.current

    // Convert Compose Color to Android ColorInt
    val colorInt = android.graphics.Color.argb(
        (color.alpha * 255).toInt(),
        (color.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt()
    )

    window?.statusBarColor = colorInt

    val windowInsetsController = ViewCompat.getWindowInsetsController(view)
    windowInsetsController?.isAppearanceLightStatusBars = darkIcons
}