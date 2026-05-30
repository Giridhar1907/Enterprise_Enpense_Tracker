package com.example.enterpriseenpensetracker.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.enterpriseenpensetracker.domain.model.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    fun generateExpenseReport(context: Context, expenses: List<Expense>) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        // Page Description
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Title
        titlePaint.textSize = 20f
        titlePaint.isFakeBoldText = true
        canvas.drawText("Enterprise Expense Report", 20f, 40f, titlePaint)

        // Date
        paint.textSize = 12f
        canvas.drawText("Generated on: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}", 20f, 65f, paint)

        // Table Header
        paint.isFakeBoldText = true
        canvas.drawText("Title", 20f, 100f, paint)
        canvas.drawText("Employee", 150f, 100f, paint)
        canvas.drawText("Category", 300f, 100f, paint)
        canvas.drawText("Amount", 450f, 100f, paint)
        canvas.drawText("Status", 520f, 100f, paint)

        // Divider
        canvas.drawLine(20f, 110f, 575f, 110f, paint)

        // Items
        paint.isFakeBoldText = false
        var y = 130f
        expenses.forEach { expense ->
            if (y > 800) { // Simple page break check
                // In a real app, you'd start a new page here
            }
            canvas.drawText(expense.title.take(15), 20f, y, paint)
            canvas.drawText(expense.employeeName.take(15), 150f, y, paint)
            canvas.drawText(expense.category, 300f, y, paint)
            canvas.drawText("$${String.format("%.2f", expense.amount)}", 450f, y, paint)
            canvas.drawText(expense.status.name, 520f, y, paint)
            y += 25f
        }

        pdfDocument.finishPage(page)

        // Save file
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, "Expense_Report_${System.currentTimeMillis()}.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "Report exported to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to export PDF", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}
