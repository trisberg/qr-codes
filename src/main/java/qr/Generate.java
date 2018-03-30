package qr;
 
import java.io.*;
import java.util.Base64;
import java.util.function.Function;

import com.google.gson.JsonObject;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

public class Generate implements Function<String, String> {

	public static String getQrCode(String text) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream b64os = Base64.getEncoder().wrap(baos);
        BitMatrix matrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 300, 300);
        MatrixToImageWriter.writeToStream(matrix, "png", b64os);
        b64os.close();
        return baos.toString("utf-8");
    }

    public String apply(String text) {
        String output = "";
        try {
            output = getQrCode(text);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }
        return output;
	}

	public static JsonObject main(JsonObject args) throws Exception {
		String text = args.getAsJsonPrimitive("text").getAsString();
        String output = getQrCode(text);
        JsonObject response = new JsonObject();
		response.addProperty("qr", output);
		return response;
	}

}
