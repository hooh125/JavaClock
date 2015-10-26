
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.io.*;
import java.text.DecimalFormat;
import org.json.JSONException;
import org.json.JSONObject;



public class Weather {

	private static final String PYTHON_TEMP_FILENAME = "temperature.py";
	private String api_key;
	private Double temp = 0.;
	private int humidity = 0;
	private Double localTemp = 0.;
	private int localHumidity = 0;
	private DecimalFormat df;

	public Weather(String api_key) {
		this.api_key = api_key;
		df = new DecimalFormat("#.0");
	}

	public String getWeather() {
		return df.format(temp) + ":" + humidity;
	}

	public String getLocalWeather() {
		return df.format(localTemp) + ":" + localHumidity;
	}

	public void updateWeather(String city, String country) throws JSONException, IOException {
			JSONObject json = readJsonFromUrl("http://api.openweathermap.org/data/2.5/weather?q=" + city + "," + country + "&appid=" + api_key);
			temp = json.getJSONObject("main").getDouble("temp") - 273.15;
			humidity = json.getJSONObject("main").getInt("humidity");
			updateLocalWeather();
	}

	private void updateLocalWeather() {
		String data[] = getSensorData().split(":");
		localTemp = Double.parseDouble(data[0]);
		localHumidity = Integer.parseInt(data[1]);
	}

	public String getSensorData() {
		System.out.println("Intentando recoger temperatura y humedad locales...");
		int intento = 0;
		String data = null;
		String s = null;
		while(data == null) {
			intento++;
			System.out.println("Intentando " + intento + " vez");
			try {
			// run the Unix "ps -ef" command
					// using the Runtime exec method:
					Process p = Runtime.getRuntime().exec("python " + PYTHON_TEMP_FILENAME);

					BufferedReader stdInput = new BufferedReader(new
							 InputStreamReader(p.getInputStream()));

					// read the output from the command
					while ((s = stdInput.readLine()) != null) {
							if(!s.equals("error")){
								data = s;
							}
					}
			}
			catch (IOException e) {
					System.out.println("exception happened - here's what I know: ");
					e.printStackTrace();
					System.exit(-1);
			}
		}
		System.out.println("Los datos son: " + data);
		return data;
	}

	private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	    InputStream is = new URL(url).openStream();
	    try {
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	      String jsonText = readAll(rd);
	      JSONObject json = new JSONObject(jsonText);
	      return json;
	    } finally {
	      is.close();
	    }
	  }

	 private static String readAll(Reader rd) throws IOException {
		    StringBuilder sb = new StringBuilder();
		    int cp;
		    while ((cp = rd.read()) != -1) {
		      sb.append((char) cp);
		    }
		    return sb.toString();
		  }

}
