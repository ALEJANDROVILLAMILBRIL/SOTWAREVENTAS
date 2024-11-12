package com.example.softwareventas.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import com.example.softwareventas.models.Cart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class InvoiceGenerator {

    public static File generateInvoice(Context context, List<Cart> cartList, String userName, String userEmail) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(1);

        int yPosition = 30;
        paint.setTextSize(18);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Factura de Compra", pageInfo.getPageWidth() / 2, yPosition, paint);

        paint.setTextSize(12);
        paint.setTextAlign(Paint.Align.LEFT);
        yPosition += 40;
        canvas.drawText("Cliente: " + userName, 20, yPosition, paint);
        yPosition += 20;
        canvas.drawText("Correo: " + userEmail, 20, yPosition, paint);

        yPosition += 30;

        // Encabezados de tabla
        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        int startX = 20;
        int column1 = startX;
        int column2 = startX + 100;
        int column3 = startX + 170;
        int column4 = startX + 240;

        canvas.drawLine(startX, yPosition - 10, pageInfo.getPageWidth() - startX, yPosition - 10, linePaint);
        canvas.drawText("Producto", column1, yPosition, paint);
        canvas.drawText("Cantidad", column2, yPosition, paint);
        canvas.drawText("Precio", column3, yPosition, paint);
        canvas.drawText("Total", column4, yPosition, paint);
        yPosition += 10;
        canvas.drawLine(startX, yPosition, pageInfo.getPageWidth() - startX, yPosition, linePaint);

        // Detalles de productos
        double grandTotal = 0;
        for (Cart item : cartList) {
            yPosition += 20;
            canvas.drawText(item.getProductName(), column1, yPosition, paint);
            canvas.drawText(String.valueOf(item.getQuantity()), column2, yPosition, paint);
            canvas.drawText(String.format("$%.2f", item.getPrice()), column3, yPosition, paint);

            double totalPrice = item.getPrice() * item.getQuantity();
            grandTotal += totalPrice;
            canvas.drawText(String.format("$%.2f", totalPrice), column4, yPosition, paint);

            yPosition += 5;
            canvas.drawLine(startX, yPosition, pageInfo.getPageWidth() - startX, yPosition, linePaint);
        }

        // Total general
        yPosition += 30;
        paint.setTextSize(14);
        paint.setColor(Color.BLACK);
        canvas.drawText("Total a Pagar: $" + String.format("%.2f", grandTotal), column4, yPosition, paint);

        pdfDocument.finishPage(page);

        // Guardar PDF en almacenamiento
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Invoices");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, "factura_" + System.currentTimeMillis() + ".pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(context, "Factura generada en " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al guardar la factura", Toast.LENGTH_SHORT).show();
        }
        pdfDocument.close();

        return file;
    }
}