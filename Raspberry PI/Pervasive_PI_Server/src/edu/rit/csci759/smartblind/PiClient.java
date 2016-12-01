package edu.rit.csci759.smartblind;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.RuleBlock;

/**
 * This class basically acts as client to the android server and sends the
 * notification to PI server if temperature changes by 2 degrees.
 * 
 * @author Kapil Dole
 *
 */
public class PiClient extends Thread {
	private static boolean keepRunning = true;
	private static float currentTemperature = 0.0f;
	public static String blindStatus;
	public static float[] temperatureValues;
	public static String temperatureStatus;
	public static int ambientValue;
	public static String ambientStatus;
	private static double blindPositionValue;
	public String androidServerIP = null;
	public static boolean unsetCurrentTemperature = true;
	public static RuleBlock rblock;
	public static FIS fis;
	public static String currentAmbientStatus;

	/*
	 * Constructor - initializes android server ip address.
	 */
	public PiClient(String IPAddress) {
		this.androidServerIP = IPAddress;
	}

	/*
	 * Reading temperature from the TMP36 sensor using the MCP3008 ADC and
	 * returns list of temperature values /100, degree and farenheit.
	 */
	public static float[] getTemperature(int adc_temperature) {
		float[] temperatureReading = new float[3];
		// [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
		// convert in the range of 1-100
		int temperature = (int) (adc_temperature / 10.24);
		temperatureReading[0] = temperature;
		float tmp36_mVolts = (float) (adc_temperature * (3300.0 / 1024.0));
		// 10 MV per degree
		float temp_C = (float) (((tmp36_mVolts - 100.0) / 10.0) - 40.0);
		temperatureReading[1] = temp_C;
		// convert celsius to fahrenheit
		float temp_F = (float) ((temp_C * 9.0 / 5.0) + 32);
		temperatureReading[2] = temp_F;
		return temperatureReading;
	}

	/*
	 * Reading ambient light from the photo cell sensor using the MCP3008 ADC
	 * and returns ambient value /100.
	 */
	public static int getAmbient(int adc_ambient) {
		// [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
		// convert in the range of 1-100
		int ambient = (int) (adc_ambient / 10.24);
		return ambient;
	}

	/*
	 * Returning temperature status based on the temperature value /100.
	 */
	public static String getTemperatureStatus(int temperature) {
		if (temperature >= 0 && temperature <= 12) {
			return "Freezing";
		} else if (temperature > 12 && temperature <= 37) {
			return "Cold";
		} else if (temperature > 37 && temperature <= 62) {
			return "Comfort";
		} else if (temperature > 62 && temperature <= 87) {
			return "Warm";
		} else {
			return "Hot";
		}
	}

	/*
	 * Returning ambient status based on the ambient value /100.
	 */
	public static String getAmbientStatus(int ambient) {
		if (ambient >= 0 && ambient <= 32) {
			return "Dark";
		} else if (ambient > 32 && ambient <= 68) {
			return "Dim";
		} else {
			return "Bright";
		}
	}

	/*
	 * Returning blind status based on the defuzzified value of blind status
	 * /100.
	 */
	public static String getBlindStatus(int blindPositionValue) {
		if (blindPositionValue >= 0 && blindPositionValue <= 37) {
			return "Open";
		} else if (blindPositionValue > 37 && blindPositionValue <= 63) {
			return "Half";
		} else {
			return "Close";
		}
	}

	/*
	 * This method reads rules for fuzzy logic from FCL file and then applies
	 * fuzzy logic on the input variables to get blind position.
	 */
	public static String getBlindPositionStatus(int temperatureValue, int ambientValue) {
		String filename = "FuzzyLogic/smartBlind.fcl";
		fis = FIS.load(filename, true);

		if (fis == null) {
			System.err.println("Can't load file: '" + filename + "'");
			System.exit(1);
		}

		// Get default function block
		FunctionBlock fb = fis.getFunctionBlock(null);

		HashMap<String, RuleBlock> ruleBlocks = fb.getRuleBlocks();
		rblock = ruleBlocks.get("No1");

		// Set inputs
		fb.setVariable("temperature", temperatureValue);
		fb.setVariable("ambient", ambientValue);

		// Evaluate
		fb.evaluate();

		// Show output variable's chart
		fb.getVariable("blind").defuzzify();

		blindPositionValue = fb.getVariable("blind").getValue();
		String blindPosition = getBlindStatus((int) blindPositionValue);

		return blindPosition;
	}

	@Override
	public void run() {
		// The JSON-RPC 2.0 server URL
		URL serverURL = null;
		// response object.
		JSONRPC2Response response = null;

		try {
			serverURL = new URL("http://" + androidServerIP + ":5555");
		} catch (MalformedURLException e) {
			// handle exception.
			System.out.println(e.getMessage());
		}

		// Create new JSON-RPC 2.0 client session
		JSONRPC2Session mySession = new JSONRPC2Session(serverURL);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Shutting down.");
				keepRunning = false;
			}
		});
		// PI keeps sensing the temperature changes and in case of significant change,
		// sends notification to android.
		while (keepRunning) {
			int adc_temperature = GpioReader.getTemp();
			int adc_ambient = GpioReader.getAmbient();
			temperatureValues = getTemperature(adc_temperature);
			ambientValue = getAmbient(adc_ambient);
			temperatureStatus = getTemperatureStatus((int) temperatureValues[0]);
			ambientStatus = getAmbientStatus(ambientValue);
			if (unsetCurrentTemperature) {
				currentTemperature = temperatureValues[1];
				currentAmbientStatus = ambientStatus;
				unsetCurrentTemperature = false;
			}

			// If the temperature varies by 2 degree, send notification to the android server.
			if ((Math.abs(temperatureValues[1] - currentTemperature) >= 2.0f || !currentAmbientStatus.equals(ambientStatus))  && androidServerIP != null) {
				currentAmbientStatus = ambientStatus;
				currentTemperature = temperatureValues[1];
				System.out.println("Sending update to device " + androidServerIP);
				blindStatus = getBlindPositionStatus((int) temperatureValues[0], ambientValue);
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				System.out.println("Sending blind status --> " + blindStatus);
				Map<String, Object> blindStatusMap = new HashMap<String, Object>();
				blindStatusMap.put("BlindStatus", blindStatus);
				blindStatusMap.put("TemperatureStatus", temperatureStatus);
				blindStatusMap.put("TemperatureCelsius", String.valueOf(temperatureValues[1]));
				blindStatusMap.put("TemperatureFarenheit", String.valueOf(temperatureValues[2]));
				blindStatusMap.put("AmbientStatus", PiClient.getAmbientStatus(PiClient.getAmbient(adc_ambient)));
				blindStatusMap.put("time", sdf.format(cal.getTime()));
				JSONRPC2Request request = new JSONRPC2Request("receiveUpdate", blindStatusMap, 2);

				try {
					response = mySession.send(request);

				} catch (Exception e) {

					System.err.println(e.getMessage());
					// handle exception...
				}

				// Print response result / error
				if (response.indicatesSuccess())
					System.out.println(response.getResult());
				else
					System.out.println(response.getError().getMessage());
				try {
					Thread.sleep(500L);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
		System.out.println("Bye...");
		GpioReader.shutdown();
	}
}
