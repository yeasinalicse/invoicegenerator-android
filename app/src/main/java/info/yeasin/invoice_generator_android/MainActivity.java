package info.yeasin.invoice_generator_android;

import androidx.annotation.RequiresApi;
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
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintJob;
import android.print.PrintManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "PdfCreatorActivity";
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;
    ImageView tvPrintImage;
    ArrayList<Item> itemArrayList;
    WebView myWebView;

    Context context;
    Item name;
    Item price;
    Item type;
    Item date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvPrintImage = findViewById(R.id.iv_printer);
        context = this;

        loadData();

        myWebView = new WebView(this);



        tvPrintImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

    private void loadData() {
        Item item1 = new Item();
        Item item2 = new Item();
        Item item3 = new Item();
        itemArrayList = new ArrayList<Item>();

        item1.setName("HP 15s-du0090TU");
        item1.setDescription("HP 15s-du0090TU 8th Gen Intel Core i5 8265U (1.60GHz-3.90GHz, 4GB DDR4, 1TB HDD) 15.6 Inch FHD (1920x1080) Display, Win 10, Gold Notebook");
        item1.setQuantity("2");
        item1.setPer_price("500");
        item1.setTotal("1000");
        itemArrayList.add(item1);

        item2.setName("BenQ SW240");
        item2.setDescription("BenQ SW240 PhotoVue 24 inch WUXGA Color Accuracy IPS Monitor for Photography (DVI, HDMI, Displayport, 1 x USB Upstream, 2 x USB Downstream, Card Reader)");
        item2.setQuantity("3");
        item2.setPer_price("500");
        item2.setTotal("1500");
        itemArrayList.add(item2);

        item3.setName("Gaming PC-R718X");
        item3.setDescription("Gaming PC-R718X Ryzen 7 1800X 4.0GHz, X370 Chipset, RX590 8GB Gr, 16GB DDR4 3200MHz, 2TB HDD + 256GB SSD, 21.5in Monitor, Gaming Headphone, Gaming KB And Mou");
        item3.setQuantity("3");
        item3.setPer_price("1000");
        item3.setTotal("3000");
        itemArrayList.add(item2);

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createPdf() throws FileNotFoundException, DocumentException {

        String pdfname = "invoices.pdf";

        //Create Folder
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File folder = new File(extStorageDirectory, "/SynergyData");
        folder.mkdir();

        //Delete File
        String myPath = Environment.getExternalStorageDirectory() + "/SynergyData/" + pdfname;
        File f = new File(myPath);
        Boolean deleted = f.delete();
        if (deleted) {
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
        // Document document = new Document(PageSize.A4, 36, 36, 90, 36);
        // Document document = new Document(PageSize.LETTER);
        Rectangle pagesize = new Rectangle(288, 720);
        Document document = new Document(pagesize);
        PdfPTable table = new PdfPTable(new float[]{1, 2, 1, 1, 1});

        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setFixedHeight(50);
        table.setTotalWidth(PageSize.A4.getWidth());
        table.setWidthPercentage(100);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
        String BILL = "                   XXXX MART    \n"
                + "                   XX.AA.BB.CC.     \n " +
                "                 NO 25 ABC ABCDE    \n" +
                "                  XXXXX YYYYYY      \n" +
                "                   MMM 590019091      \n";
        BILL = BILL
                + "-----------------------------------------------\n";
        table.addCell("Name");
        table.addCell("Description");
        table.addCell("Quantity");
        table.addCell("Price Per");
        table.addCell("Total Price");
        table.setHeaderRows(2);
        table.setFooterRows(1);

        PdfPCell[] cells = table.getRow(0).getCells();
        for (int j = 0; j < cells.length; j++) {
            cells[j].setBackgroundColor(BaseColor.LIGHT_GRAY);
        }
        for (int i = 0; i < itemArrayList.size(); i++) {
            name = itemArrayList.get(i);
            String names = name.getName();
            String des = name.getDescription();
            String qty = name.getQuantity();
            String per_price = name.getPer_price();
            String totals = name.getTotal();

            table.addCell(String.valueOf(names));
            table.addCell(String.valueOf(des));
            table.addCell(String.valueOf(qty));
            table.addCell(String.valueOf(per_price));
            table.addCell(totals);
        }
        PdfWriter.getInstance(document, output);
        document.open();
        Font fs = new Font(Font.FontFamily.TIMES_ROMAN, 10.0f, Font.UNDEFINED, BaseColor.BLUE);
        document.add(new Paragraph(BILL, fs));
        document.add(new Paragraph("\n", fs));
        document.add(table);
        document.close();
        Log.e("yeasin", itemArrayList.toString());

        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = new PdfDocumentAdapter(this, myPath);
        printManager.print("Document", printAdapter, new PrintAttributes.Builder().build());

        //printContent();
    }
//
//    public void printContent() {
//        WebView webView = new WebView(this);
//        webView.setWebViewClient(new WebViewClient() {
//
//            public boolean shouldOverrideUrlLoading(WebView view,
//                                                    String url)
//            {
//                return false;
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                createWebPrintJob(view);
//                myWebView = null;
//            }
//        });
//
//        String htmlDocument =
//                "<html><body><h1>Android Print Test</h1><p>"
//                        + "This is some sample content.</p></body></html>";
//
//
//        webView.loadDataWithBaseURL(null, htmlDocument,
//                "text/HTML", "UTF-8", null);
//
//        myWebView = webView;
//    }
//
//
//    private void createWebPrintJob(WebView webView) {
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            PrintManager printManager = (PrintManager) this
//                    .getSystemService(Context.PRINT_SERVICE);
//
//            PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter("MyDocument");
//            String jobName = getString(R.string.app_name) + " Document";
//
//            PrintJob printJob = printManager.print(jobName, printAdapter,
//                    new PrintAttributes.Builder().build());
//        }
//
//    }
//
//
//    private void previewInvoices() {
//        File pdfFile = new File(Environment.getExternalStorageDirectory() + "/SynergyData/" + "invoices.pdf");
//
//        Uri path;
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
//            path = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", pdfFile);
//        } else {
//            path = Uri.fromFile(pdfFile);
//        }
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.setDataAndType(path, "application/pdf");
//        try {
//            PackageManager pm = getPackageManager();
//            if (intent.resolveActivity(pm) != null) {
//                startActivity(intent);
//            }
//        } catch (Exception e) {
//            Toast.makeText(this, "No Application available ", Toast.LENGTH_SHORT).show();
//        }
//
//    }







}