package edu.rit.csci759.smartblind;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import edu.rit.csci759.rspi.utils.MCP3008ADCReader;

/**
 * This class basically reads the ADC values from input channels and provides
 * values for temperature and ambient.
 * 
 * @author Kapil Dole
 *
 */
public class GpioReader {
	static GpioController gpio;

	/*
	 * This method initializes the GPIO instance.
	 */
	public static void initialize() {
		gpio = GpioFactory.getInstance();
		MCP3008ADCReader.initSPI(gpio);
	}

	/*
	 * This method returns the temperature readings.
	 */
	public static int getTemp() {
		return MCP3008ADCReader.readAdc(MCP3008ADCReader.MCP3008_input_channels.CH0.ch());
	}

	/*
	 * This method returns the ambient readings.
	 */
	public static int getAmbient() {
		return MCP3008ADCReader.readAdc(MCP3008ADCReader.MCP3008_input_channels.CH1.ch());
	}

	/*
	 * This method shut down the GPIO instance.
	 */
	public static void shutdown() {
		gpio.shutdown();
	}

}
