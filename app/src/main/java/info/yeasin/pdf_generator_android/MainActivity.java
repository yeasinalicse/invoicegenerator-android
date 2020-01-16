package info.yeasin.pdf_generator_android;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PdfCreatorActivity";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    ImageView tvPrintImage;
    ArrayList<Data> itemArrayList;
    Data data;
    Context context;
    Data name;
    Data price;
    Data type;
    Data date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvPrintImage = findViewById(R.id.iv_printer);
        context = this;
        data = new Data();
        itemArrayList = new ArrayList<Data>();
        for (int i = 0; i < 5; i++) {
            data.setItem_name("Computer");
            data.setItem_price("6");
            data.setItem_type_code("sdfsd");
            itemArrayList.add(data);
        }
        tvPrintImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    createPdfWrapper();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void createPdfWrapper() throws FileNotFoundException, DocumentException {
        //Storage Permission
        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    showMessageOKCancel("You need to allow access to Storage",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                REQUEST_CODE_ASK_PERMISSIONS);
                                    }
                                }
                            });
                    return;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        } else {
            createPdf();
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void createPdf() throws FileNotFoundException, DocumentException {

        String pdfname = "invoices.pdf";

        //Create Folder
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File folder = new File(extStorageDirectory, "/SynergyData");
        folder.mkdir();

        //Delete File
        String myPath = Environment.getExternalStorageDirectory()+"/SynergyData/"+pdfname;
        File f = new File(myPath);
        Boolean deleted = f.delete();
        if (deleted){
            Toast.makeText(context, "Sucess", Toast.LENGTH_SHORT).show();
        }

        //Create Pdf
        File pdfFile = new File(folder, pdfname);

        try {
            pdfFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document(PageSize.A4);
        PdfPTable table = new PdfPTable(new float[]{3, 3, 3, 3});

        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setFixedHeight(50);
        table.setTotalWidth(PageSize.A4.getWidth());
        table.setWidthPercentage(100);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell("Name");
        table.addCell("Price");
        table.addCell("Type");
        table.addCell("Date");
        table.setHeaderRows(1);
        PdfPCell[] cells = table.getRow(0).getCells();
        for (int j = 0; j < cells.length; j++) {
            cells[j].setBackgroundColor(BaseColor.GRAY);
        }
        for (int i = 0; i < itemArrayList.size(); i++) {
            name = itemArrayList.get(i);
            type = itemArrayList.get(i);
            date = itemArrayList.get(i);
            price = itemArrayList.get(i);
            String namen = name.getItem_name();
            String pricen = price.getItem_price();
            String typen = type.getItem_type_code();

            table.addCell(String.valueOf(namen));
            table.addCell(String.valueOf(pricen));
            table.addCell(String.valueOf(typen));
            table.addCell("16-01-2020");
        }
        PdfWriter.getInstance(document, output);
        document.open();
        Font fs = new Font(Font.FontFamily.TIMES_ROMAN, 30.0f, Font.UNDERLINE, BaseColor.BLUE);
        Font g = new Font(Font.FontFamily.TIMES_ROMAN, 20.0f, Font.NORMAL, BaseColor.BLUE);
        document.add(new Chapter(1));
        document.add(new Paragraph("Dysin Product invoices  \n", fs));
        document.add(new Paragraph("\n", g));
        document.add(table);
        document.close();
        Log.e("yeasin", itemArrayList.toString());
        previewInvoices();
    }

    private void previewInvoices() {
        File pdfFile = new File(Environment.getExternalStorageDirectory() + "/SynergyData/" + "invoices.pdf");

        Uri path;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", pdfFile);
        } else {
            path = Uri.fromFile(pdfFile);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(path, "application/pdf");
        try {
            PackageManager pm = getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "No Application available ", Toast.LENGTH_SHORT).show();
        }

    }


}