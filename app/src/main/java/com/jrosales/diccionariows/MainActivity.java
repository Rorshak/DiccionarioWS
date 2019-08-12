package com.jrosales.diccionariows;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;

import android.os.AsyncTask;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
public class MainActivity extends AppCompatActivity {
    private EditText txtPalabra;
    private TextView txtTexto;
    private Button btnBuscar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //inFlate
        txtPalabra = (EditText)findViewById(R.id.txt_palabra);
        txtTexto = (TextView)findViewById(R.id.txt_texto);
        btnBuscar = (Button)findViewById(R.id.btn_buscar);

        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String palabra="";
                palabra = txtPalabra.getText().toString();
                new AccesswebServiceTask().execute(palabra);
            }
        });
    }

    private String wordDefinition(String word){
        InputStream in = null;
        String strDefinition="";
        try{
            in = openHttpConnection("http://services.aonaware.com/DictService/DictService.asmx/Define?word="+word);
            Document doc = null;
            DocumentBuilderFactory dbf;
            dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db;
            try{
                db = dbf.newDocumentBuilder();
                doc=db.parse(in);

            }catch (ParserConfigurationException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
            doc.getDocumentElement().normalize();
            NodeList definitionElements = doc.getElementsByTagName("Definition");
            for(int i=0; i<definitionElements.getLength();i++){
                Node itemNode= definitionElements.item(i);
                if(itemNode.getNodeType()==Node.ELEMENT_NODE){
                    Element definitionElement = (Element) itemNode;
                    NodeList wordDefinitionElements =
                            (definitionElement).getElementsByTagName("WordDefinition");
                    strDefinition="";
                    for(int j=0;j<wordDefinitionElements.getLength();j++){
                        Element wordDefinitionElement = (Element)wordDefinitionElements.item(j);
                        NodeList textNodes;
                        textNodes= ((Node)wordDefinitionElement).getChildNodes();
                        strDefinition+= ((Node)textNodes.item(0)).getNodeValue()+"\n";
                    }
                }
            }
        }catch (IOException e){
            Log.d("MainActivity", e.getLocalizedMessage());
        }
        return strDefinition;
    }
    private InputStream openHttpConnection(String urlString)throws IOException {
        InputStream in = null;
        int response = -1;
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        if (!(conn instanceof HttpURLConnection)) {
            throw new IOException("No es una conexion HTTP");
        }
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if(response==HttpURLConnection.HTTP_OK){
                in= httpConn.getInputStream();
            }
        }catch (Exception e){
            Log.d("Networking", e.getLocalizedMessage());
            throw new IOException("Error en la conexion");
        }
        return in;
    }

    private class AccesswebServiceTask extends AsyncTask<String, Void, String>{
        protected String doInBackground(String... urls){
            return wordDefinition(urls[0]);
        }
        protected void onPostExecute(String result){
            txtTexto.setText(result);
        }
    }
}
