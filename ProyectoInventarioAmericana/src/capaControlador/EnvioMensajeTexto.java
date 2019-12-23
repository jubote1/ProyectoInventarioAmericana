package capaControlador;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;


public class EnvioMensajeTexto {

	public static void main(String[] args) throws IOException
	{

        String uri ="http://masivos.colombiared.com.co/Api/rest/message";
        
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        byte[] encodedBytes = Base64.encodeBase64(("apizza" + ":" + "Nailli1").getBytes());
        connection.setRequestProperty("Authorization", "Basic " +  new String(encodedBytes));
        System.out.println("Authorization: Basic " + new String(encodedBytes));
        //connection.setRequestProperty("content-type", "text/xml; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");
        JSONObject datos = new JSONObject();
        datos.put("to", "573148807773");
        datos.put("text", "Esto es un ejemplo");
        datos.put("from", "msg");
//      Aqui me gustaria mandarle el cuerpo pero no es correcto
        connection.setRequestProperty("body", datos.toJSONString());
        connection.setConnectTimeout(10000);
//      connection.setRequestProperty("Content-Length", Integer.toString(archivo.length));
//        connection.getOutputStream().; //suponiendo que archivo sea tipo byte[]
        connection.getOutputStream().flush();
        
        System.out.println("Codigo : " + connection.getResponseCode());
        
        

	}
	
}
