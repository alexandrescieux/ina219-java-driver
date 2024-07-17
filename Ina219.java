/**
 * Library for interfacing an INA219 High DC Current Sensor with a Raspberry Pi
 * @author Alexandre Scieux
 * @version 1.0
 *
 * Caution : Be sure to include /opt/pi4j/lib/'*' in your classpath (http://pi4j.com/)
 * 
 */

package sensor;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import java.nio.ByteBuffer;

public class Ina219
{

	private I2CBus bus;
	private I2CDevice ina219;

	/* Default configuration values */
	private BusVoltageRange busVoltageRange       = BusVoltageRange.INA219_CONFIG_BVOLTAGERANGE_32V;
	private Gain gain                             = Gain.INA219_CONFIG_GAIN_8_320MV;
	private BusADCResolution busADCResolution     = BusADCResolution.INA219_CONFIG_BADCRES_12BIT;
	private ShuntADCResolution shuntADCResolution = ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_1S_532US;
	private OperatingMode operatingMode           = OperatingMode.INA219_CONFIG_MODE_SANDBVOLT_CONTINUOUS;

	/**
	 * INA219 constants
	 */
	public enum Constants
	{
		BUS_MAX_V										(32.0),
		SHUNT_MAX_V										(0.32),
		R_SHUNT_OHM 									(5.0),
		MAX_POSSIBLE_CURRENT_A 							(0.064);

		private double value;

		private Constants (double value)
		{
			this.value = value;
		}

		public double getValue() 
		{
			return value;
		}

		public void setValue(double value)
		{
			this.value = value;
		}
	}

	/**
	 * Addresses of the INA219 registers
	 */
	public enum Registers
	{
		INA219_I2C_BUS									(1),
		INA219_I2C_ADDRESS								(0x40),
		
		INA219_CONFIG_RESET 							(0x8000),
		INA219_CONFIG_DEFAULT							(0x399F),
		INA219_CALIBRATION_DEFAULT						(0x1000),

		INA219_REG_CONFIG 								(0x00),
		INA219_REG_SHUNTVOLTAGE 						(0x01),
		INA219_REG_BUSVOLTAGE 							(0x02),
		INA219_REG_POWER 								(0x03),
		INA219_REG_CURRENT 								(0x04),
		INA219_REG_CALIBRATION 							(0x05);

		private int value;

		private Registers (int value)
		{
			this.value = value;
		}
		
		public int getValue() 
		{
			return value;
		}

		public void setValue(int value)
		{
			this.value = value; 
		}
	}

	/**
	 * INA219 Bus Voltage Range configuration values
	 */
	public enum BusVoltageRange
	{
		INA219_CONFIG_BVOLTAGERANGE_MASK 				(0x2000),
		INA219_CONFIG_BVOLTAGERANGE_16V 				(0x0000),
		INA219_CONFIG_BVOLTAGERANGE_32V 				(0x2000);

		private int value;

		private BusVoltageRange()
		{
			this.value = (0x2000);
		}

		private BusVoltageRange (int value)
		{
			this.value = value;
		}

		public int getValue() 
		{
			return value;
		}

		public void setValue(int value)
		{
			this.value = value; 
		}
	}

	/**
	 * INA219 Gain configuration values
	 */
	public enum Gain
	{
		INA219_CONFIG_GAIN_MASK							(0x1800),
		INA219_CONFIG_GAIN_1_40MV 						(0x0000),
		INA219_CONFIG_GAIN_2_80MV 						(0x0800),
		INA219_CONFIG_GAIN_4_160MV 						(0x1000),	
		INA219_CONFIG_GAIN_8_320MV 						(0x1800);

		private int value;

		private Gain()
		{
			this.value = (0x1800);
		}

		private Gain (int value)
		{
			this.value = value;
		}

		public int getValue() 
		{
			return value;
		}

		public void setValue(int value)
		{
			this.value = value; 
		}
	}

	/**
	 * INA219 Bus ADC Resolution configuration values
	 */
	public enum BusADCResolution
	{
		INA219_CONFIG_BADCRES_MASK 						(0x0780),	
		INA219_CONFIG_BADCRES_9BIT 						(0x0080),
		INA219_CONFIG_BADCRES_10BIT 					(0x0100),
		INA219_CONFIG_BADCRES_11BIT 					(0x0200),	
		INA219_CONFIG_BADCRES_12BIT 					(0x0400);

		private int value;

		private BusADCResolution()
		{
			this.value = (0x0400);
		}

		private BusADCResolution (int value)
		{
			this.value = value;
		}

		public int getValue() 
		{
			return value;
		}

		public void setValue(int value)
		{
			this.value = value; 
		}
	}

	/**
	 * INA219 Shunt ADC resolution configuration values
	 */
	public enum ShuntADCResolution
	{
		INA219_CONFIG_SADCRES_MASK						(0x0078),
		INA219_CONFIG_SADCRES_9BIT_1S_84US				(0x0000),
		INA219_CONFIG_SADCRES_10BIT_1S_148US 			(0x0008),	
		INA219_CONFIG_SADCRES_11BIT_1S_276US			(0x0010),	
		INA219_CONFIG_SADCRES_12BIT_1S_532US			(0x0018),	
		INA219_CONFIG_SADCRES_12BIT_2S_1060US 			(0x0048),	
		INA219_CONFIG_SADCRES_12BIT_4S_2130US			(0x0050),	
		INA219_CONFIG_SADCRES_12BIT_8S_4260US 			(0x0058),	
		INA219_CONFIG_SADCRES_12BIT_16S_8510US			(0x0060),	
		INA219_CONFIG_SADCRES_12BIT_32S_17MS			(0x0068),	
		INA219_CONFIG_SADCRES_12BIT_64S_34MS			(0x0070),	
		INA219_CONFIG_SADCRES_12BIT_128S_69MS 			(0x0078);

		private int value;

		private ShuntADCResolution()
		{
			this.value = (0x0018);
		}

		private ShuntADCResolution (int value)
		{
			this.value = value;
		}

		public int getValue() 
		{
			return value;
		}

		public void setValue(int value)
		{
			this.value = value; 
		}
	}

	/**
	 * INA219 Operating mode configuration values
	 */
	public enum OperatingMode
	{
		INA219_CONFIG_MODE_MASK 						(0x0007),	
		INA219_CONFIG_MODE_POWERDOWN 					(0x0000),	
		INA219_CONFIG_MODE_SVOLT_TRIGGERED  			(0x0001),	
		INA219_CONFIG_MODE_BVOLT_TRIGGERED  			(0x0002),	
		INA219_CONFIG_MODE_SANDBVOLT_TRIGGERED  		(0x0003),	
		INA219_CONFIG_MODE_ADCOFF 						(0x0004),	
		INA219_CONFIG_MODE_SVOLT_CONTINUOUS  			(0x0005),	
		INA219_CONFIG_MODE_BVOLT_CONTINUOUS  			(0x0006),	
		INA219_CONFIG_MODE_SANDBVOLT_CONTINUOUS  		(0x0007);

		private int value;

		private OperatingMode()
		{
			this.value = (0x0007);
		}

		private OperatingMode (int value)
		{
			this.value = value;
		}

		public int getValue() 
		{
			return value;
		}

		// Use global variable instead of enum
		public void setValue(int value)
		{
			this.value = value; 
		}
	}
	
	/* I2C */
	private static final int i2cbus                                  = Registers.INA219_I2C_BUS.getValue();											// Raspberry Pi's I2C Bus (0 on Raspberry Pi Rev A, 1 on Raspberry Pi Rev B)
	private static final int address                                 = Registers.INA219_I2C_ADDRESS.getValue();										// INA219 Device address one the I2C Bus. Run i2cdetect -y 1 to see the address. Default is 0x40.
	
	/* Registers */

	/* Shunt Resistor Register (Read only) */
	private static final int ina219_reg_shuntvoltage                 =	Registers.INA219_REG_SHUNTVOLTAGE.getValue();								// Address of the register containing the shunt voltage value. Structure at full-scale range : SIGN SD14_8 SD13_8 SD12_8 SD11_8 SD10_8 SD9_8 SD8_8 SD7_8 SD6_8 SD5_8 SD4_8 SD3_8 SD2_8 SD1_8 SD0_8 (16 bits)
	
	/* Bus Voltage Register (Read only) */
	private static final int ina219_reg_busvoltage                   =	Registers.INA219_REG_BUSVOLTAGE.getValue();									// Address of the register containing the bus voltage value. Structure : BD12 BD11 BD10 BD9 BD8 BD7 BD6 BD5 BD4 BD3 BD2 BD1 BD0 - CNVR OVF (16 bits)
	
	/* Power Register (Read only) */
	private static final int ina219_reg_power                        =	Registers.INA219_REG_POWER.getValue();										// Address of the register containing the power value. Structure : PD15 PD14 PD13 PD12 PD11 PD10 PD9 PD8 PD7 PD6 PD5 PD4 PD3 PD2 PD1 PD0 (16 bits)
	
	/* Current Register (Read only) */
	private static final int ina219_reg_current                      =	Registers.INA219_REG_CURRENT.getValue();									// Address of the register containing the current value. Structure : CSIGN CD14 CD13 CD12 CD11 CD10 CD9 CD8 CD7 CD6 CD5 CD4 CD3 CD2 CD1 CD (16 bits)
	
	/* Calibration Register (Read / Write) */
	private static final int ina219_reg_calibration                  =	Registers.INA219_REG_CALIBRATION.getValue();								// Address of the calibration register. Structure :  FS15 FS14 FS13 FS12 FS11 FS10 FS9 FS8 FS7 FS6 FS5 FS4 FS3 FS2 FS1 FS0 (16 bits). FS0 is a void bit and will always be 0. It is not possible to write a 1 to FS0. Calibration is the value stored in FS15:FS1.
	private static final int ina219_calibration_default				 =  Registers.INA219_CALIBRATION_DEFAULT.getValue();							// Default calibration value. No overflow, maximum range.
	
	/* Configuration register (Read / Write) */
	private static final int ina219_reg_config                       =	Registers.INA219_I2C_ADDRESS.getValue(); 									// Adress of the configuration register. Structure :  RST - BRNG PG1 PG0 BADC4 BADC3 BADC2 BADC1 SADC4 SADC3 SADC2 SADC1 MODE3 MODE2 MODE1
	private static final int ina219_config_reset                     =	Registers.INA219_CONFIG_RESET.getValue(); 									// Reset Bit. Setting this bit to '1' generates a system reset that is the same as power-on reset. Resets all registers to default values. This bit self-clears.
	private static final int ina219_config_default 					 =  Registers.INA219_CONFIG_DEFAULT.getValue();									// Default configuration value.
	
	/* Configuration */

	/* Bus Voltage Range */
	private static final int ina219_config_bvoltagerange_mask        =	BusVoltageRange.INA219_CONFIG_BVOLTAGERANGE_MASK.getValue();				// Bus Voltage Range Mask
	private static final int ina219_config_bvoltagerange_16v         =	BusVoltageRange.INA219_CONFIG_BVOLTAGERANGE_16V.getValue();					// 0-16V Range
	private static final int ina219_config_bvoltagerange_32v         =	BusVoltageRange.INA219_CONFIG_BVOLTAGERANGE_32V.getValue();					// 0-32V Range
	
	/* Gain */
	private static final int ina219_config_gain_mask                 =	Gain.INA219_CONFIG_GAIN_MASK.getValue();									// Gain Mask
	private static final int ina219_config_gain_1_40mv               =	Gain.INA219_CONFIG_GAIN_1_40MV.getValue();									// Gain 1, 40mV Range
	private static final int ina219_config_gain_2_80mv               =	Gain.INA219_CONFIG_GAIN_2_80MV.getValue();									// Gain 2, 80mV Range
	private static final int ina219_config_gain_4_160mv              =	Gain.INA219_CONFIG_GAIN_4_160MV.getValue();									// Gain 4, 160mV Range
	private static final int ina219_config_gain_8_320mv              =	Gain.INA219_CONFIG_GAIN_8_320MV.getValue();									// Gain 8, 320mV Range
	
	/* Bus ADC Resolution */
	private static final int ina219_config_badcres_mask              =	BusADCResolution.INA219_CONFIG_BADCRES_MASK.getValue();						// Bus ADC resolution mask
	private static final int ina219_config_badcres_9bit              =	BusADCResolution.INA219_CONFIG_BADCRES_9BIT.getValue();						// 9-bit bus resolution = 0..511
	private static final int ina219_config_badcres_10bit             =	BusADCResolution.INA219_CONFIG_BADCRES_10BIT.getValue();					// 10-bit bus resolution = 0..1023
	private static final int ina219_config_badcres_11bit             =	BusADCResolution.INA219_CONFIG_BADCRES_11BIT.getValue();					// 11-bit bus res = 0..2047
	private static final int ina219_config_badcres_12bit             =	BusADCResolution.INA219_CONFIG_BADCRES_12BIT.getValue();					// 12-bit bus res = 0..4097
	
	/* Shunt ADC Resolution */
	private static final int ina219_config_sadcres_mask              =	ShuntADCResolution.INA219_CONFIG_SADCRES_MASK.getValue();					// Shunt ADC resolution and averaging mask
	private static final int ina219_config_sadcres_9bit_1s_84us      =	ShuntADCResolution.INA219_CONFIG_SADCRES_9BIT_1S_84US.getValue();			// 1 x 9-bit shunt sample
	private static final int ina219_config_sadcres_10bit_1s_148us    =	ShuntADCResolution.INA219_CONFIG_SADCRES_10BIT_1S_148US.getValue();			// 1 x 10-bit shunt sample
	private static final int ina219_config_sadcres_11bit_1s_276us    =	ShuntADCResolution.INA219_CONFIG_SADCRES_11BIT_1S_276US.getValue();			// 1 x 11-bit shunt sample
	private static final int ina219_config_sadcres_12bit_1s_532us    =	ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_1S_532US.getValue();			// 1 x 12-bit shunt sample
	private static final int ina219_config_sadcres_12bit_2s_1060us   =	ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_2S_1060US.getValue();		// 2 x 12-bit shunt samples averaged together
	private static final int ina219_config_sadcres_12bit_4s_2130us   =	ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_4S_2130US.getValue();		// 4 x 12-bit shunt samples averaged together
	private static final int ina219_config_sadcres_12bit_8s_4260us   =	ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_8S_4260US.getValue();		// 8 x 12-bit shunt samples averaged together
	private static final int ina219_config_sadcres_12bit_16s_8510us  =	ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_16S_8510US.getValue();		// 16 x 12-bit shunt samples averaged together
	private static final int ina219_config_sadcres_12bit_32s_17ms    =	ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_32S_17MS.getValue();			// 32 x 12-bit shunt samples averaged together
	private static final int ina219_config_sadcres_12bit_64s_34ms    =	ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_64S_34MS.getValue();			// 64 x 12-bit shunt samples averaged together
	private static final int ina219_config_sadcres_12bit_128s_69ms   =	ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_128S_69MS.getValue();		// 128 x 12-bit shunt samples averaged together
	
	/* Operating Mode */
	private static final int ina219_config_mode_mask                 =	OperatingMode.INA219_CONFIG_MODE_MASK.getValue();							// Operating mode mask
	private static final int ina219_config_mode_powerdown            =	OperatingMode.INA219_CONFIG_MODE_POWERDOWN.getValue();						// Powerdown
	private static final int ina219_config_mode_svolt_triggered      = 	OperatingMode.INA219_CONFIG_MODE_SVOLT_TRIGGERED.getValue();				// Shunt-Volt Triggered
	private static final int ina219_config_mode_bvolt_triggered      = 	OperatingMode.INA219_CONFIG_MODE_BVOLT_TRIGGERED.getValue();				// Bus-Volt Triggered
	private static final int ina219_config_mode_sandbvolt_triggered  = 	OperatingMode.INA219_CONFIG_MODE_SANDBVOLT_TRIGGERED.getValue();			// Shunt-Volt/Bus-Volt Triggered
	private static final int ina219_config_mode_adcoff               =	OperatingMode.INA219_CONFIG_MODE_ADCOFF.getValue();							// Analog to Digital Converter OFF
	private static final int ina219_config_mode_svolt_continuous     = 	OperatingMode.INA219_CONFIG_MODE_SVOLT_CONTINUOUS.getValue();				// Shunt-Volt Continuous
	private static final int ina219_config_mode_bvolt_continuous     = 	OperatingMode.INA219_CONFIG_MODE_BVOLT_CONTINUOUS.getValue();				// Bus-Volt Continuous
	private static final int ina219_config_mode_sandbvolt_continuous = 	OperatingMode.INA219_CONFIG_MODE_SANDBVOLT_CONTINUOUS.getValue();			// Shunt-Volt/Bus-Volt Continuous

	/* Constructor */
	public Ina219() throws IOException
	{
		try
		{
			// Connection to the I2C Bus
			this.bus = I2CFactory.getInstance(I2CBus.BUS_1);
			System.out.println("Connection to bus OK");

			// Connection to the I2C Device
			this.ina219 = bus.getDevice(address);
			System.out.println("Connection to device OK");
		}

		catch (IOException ioe) 
		{
			System.err.println("Exception during I2C initialization");
			System.err.println("Exception : " + ioe.getMessage());
		}
	}

	/**
	 * Calibrates the sensor by filling the calibration register with default values
	 * Calibrating the sensor with no possibility for overflow
	 * Value (0x1000) = 4096 calibrates INA219 for maximum range (but less resolution).
	 * The power register and current register default to 0 because the calibration register defaults to 0, yielding a zero current value until the calibration register is programmed.
	 */
	public void calibrate_default() throws IOException
	{
		System.out.println("Calibrating device using default values ...");

		try
		{
		
			setCalibration(ina219_calibration_default);

			System.out.println("Device successfully calibrated with default values");

			// show_calibration();

		}
		catch (IOException ioe) 
		{
			System.err.println("Exception during default sensor calibration");
			System.err.println("Exception : " + ioe.getMessage());
		}
	}

	/**
	 * Set the sensor calibration register value
	 * @param  calibration The calibration to be set (decimal value)
	 * @throws IOException Input/Output Exception
	 */
	public void setCalibration(int calibration) throws IOException
	{
		// Buffer/Register overflow check
		if (calibration >= 0 && calibration <= 65535)
		{
			try
			{
				byte[] buffer = ByteBuffer.allocate(4).putInt(calibration).array();
				
				/*
				void write(int address, byte[] buffer, int offset, int size) throws IOException
				address - local address in the i2c device
				buffer  - buffer of data to be written to the i2c device in one go
				offset  - offset in buffer
				size    - number of bytes to be written
				*/
				ina219.write(ina219_reg_calibration, buffer, 2, 2);
				
				// System.out.println("Calibration value : " + calibration);
				// show_calibration();
			}
			catch (IOException ioe) 
			{
				System.err.println("Exception during sensor calibration");
				System.err.println("Exception : " + ioe.getMessage());
			}
		}
		// Overflow handling
		else
		{
			System.err.println("Calibration value out of range");
		}
	}

	/**
	 * Configures the sensor by filling the configuration register with default value
	 * For a full reset of the device (all registers to their default values), please use reset()
	 */
	public void configure_default() throws IOException
	{
		int default_config = (0x399F);
		byte[] buffer = ByteBuffer.allocate(4).putInt(default_config).array();

		System.out.println("Configuring device with default configuration ...");

		try
		{
			ina219.write(ina219_reg_config, buffer, 2, 2);

			System.out.println("Device successfully configured with default configuration");
			show_configuration();
		}
		catch (IOException ioe)
		{
			System.err.println("Exception during default sensor configuration");
			System.err.println("Exception : " + ioe.getMessage());
		}
	}

	/**
	 * Configures the INA219 by filling the configuration register with provided configuration
	 * @param busVoltageRange 		16V range or 32V range (default)
	 * @param gain 					From gain 1 --> 40 mV range to gain 1/8 --> 320 mV range (default)
	 * @param busADCResolution 		9 bit to 12 bit bus resolution
	 * @param shuntADCResolution 	9 bit sample to 12 bit averaged resolution
	 * @param operatingMode 		S-Volt and/or B-Volt triggered/continuous
	 */
	public void configure_custom(BusVoltageRange busVoltageRange, Gain gain, BusADCResolution busADCResolution, ShuntADCResolution shuntADCResolution, OperatingMode operatingMode) throws IOException
	{
		System.out.println("Configuring device with custom configuration ...");

		try
		{

			setBusVoltageRange(busVoltageRange);
			setGain(gain);
			setBusADCResolution(busADCResolution);
			setShuntADCResolution(shuntADCResolution);
			setOperatingMode(operatingMode);

			System.out.println("Device successfully configurated with custom configuration");
			show_configuration();

		}
		catch (IOException ioe)
		{
			System.err.println("Exception during custom sensor configuration");
			System.err.println("Exception : " + ioe.getMessage());
		}
	}

	/**
	 * Shows the INA219 configuration register in a user-friendly way
	 */
	public void show_configuration() throws IOException
	{
		int reg_config = 0;
		String reg_config_string = "";
		
		try 
		{
			reg_config = getConfigurationRegister();
		}
		catch (IOException ioe)
		{
			System.err.println("Exception during configuration showing");
			System.err.println("Exception : " + ioe.getMessage());
		}


		System.out.println("Configuration register : ");
		System.out.println("RST \t - \t BRNG \t PG1 \t PG0 \t BADC4 \t BADC3 \t BADC2 \t BADC1 \t SADC4 \t SADC3 \t SADC2 \t SADC1 \t MODE3 \t MODE2 \t MODE1");
		
		// Formatting
		reg_config_string = String.format("%16s", Integer.toBinaryString(reg_config));
		reg_config_string = reg_config_string.replace(' ', '0');

		// Showing configuration register
		for (int i = 0; i < reg_config_string.length(); i++)
		{
			System.out.print(reg_config_string.charAt(i) + "\t ");
		}
		
		System.out.println();
	}

	/**
	 * Shows the INA219 calibration register in a user-friendly way
	 */
	public void show_calibration() throws IOException
	{
		int reg_calibration = 0;
		String reg_calibration_string = "";

		try
		{
			reg_calibration = getCalibrationRegister();
		}
		catch (IOException ioe)
		{
			System.err.println("Exception during calibration showing");
			System.err.println("Exception : " + ioe.getMessage());
		}

		System.out.println("Calibration register : ");
		System.out.println("FS15 \t FS14 \t FS13 \t FS12 \t FS11 \t FS10 \t FS9 \t FS8 \t FS7 \t FS6 \t FS5 \t FS4 \t FS3 \t FS2 \t FS1 \t FS0");

		// Formatting
		reg_calibration_string = String.format("%16s", Integer.toBinaryString(reg_calibration));
		reg_calibration_string = reg_calibration_string.replace(' ', '0');

		// Showing calibration register
		for (int i = 0; i < reg_calibration_string.length(); i++)
		{
			System.out.print(reg_calibration_string.charAt(i) + "\t ");
		}
		
		System.out.println();
	}

	/**
	 * Shows the INA219 current register in a user-friendly way
	 */
	public void show_current_register() throws IOException
	{
		int reg_current = 0;
		String reg_current_string = "";

		try
		{
			reg_current = getCurrentRegister();
		}

		catch (IOException ioe)
		{
			System.err.println("Exception during current register showing");
			System.err.println("Exception : " + ioe.getMessage());
		}

		System.out.println("Current register : ");
		System.out.println("CSIGN \t CD14 \t CD13 \t CD12 \t CD11 \t CD10 \t CD9 \t CD8 \t CD7 \t CD6 \t CD5 \t CD4 \t CD3 \t CD2 \t CD1 \t CD ");

		// Formatting
		reg_current_string = String.format("%16s", Integer.toBinaryString(reg_current));
		reg_current_string = reg_current_string.replace(' ', '0');

		// Showing current register
		for (int i = 0; i < reg_current_string.length(); i++)
		{
			System.out.print(reg_current_string.charAt(i) + "\t ");
		}
		
		System.out.println();
	}

	/**
	 * Shows the INA219 power register in a user-friendly way
	 */
	public void show_power_register() throws IOException
	{
		int reg_power = 0;
		String reg_power_string = "";

		try
		{
			reg_power = getPowerRegister();
		}
		catch (IOException ioe)
		{
			System.err.println("Exception during power register showing");
			System.err.println("Exception : " + ioe.getMessage());
		}

		System.out.println("Power register : ");
		System.out.println("PD15 \t PD14 \t PD13 \t PD12 \t PD11 \t PD10 \t PD9 \t PD8 \t PD7 \t PD6 \t PD5 \t PD4 \t PD3 \t PD2 \t PD1 \t PD0");

		// Formatting
		reg_power_string = String.format("%16s", Integer.toBinaryString(reg_power));
		reg_power_string = reg_power_string.replace(' ', '0');

		// Showing power register
		for (int i = 0; i < reg_power_string.length(); i++)
		{
			System.out.print(reg_power_string.charAt(i) + "\t ");
		}
		
		System.out.println();
	}

	/**
	 * Shows the INA219 shunt voltage register in a user-friendly way
	 */	
	public void show_shunt_voltage_register() throws IOException
	{
		int reg_shuntvoltage = 0;
		int gain_setting = getGainSetting();
		
		String reg_shuntvoltage_string = "";

		try
		{
			reg_shuntvoltage = getShuntVoltageRegister();
		}
		catch (IOException ioe)
		{
			System.err.println("Exception during shunt voltage register showing");
			System.err.println("Exception : " + ioe.getMessage());
		}

		System.out.println("Shunt voltage register : ");

		// Adpatation to the PGA range selected
		switch(gain_setting)
		{
			// PGA = 1/8
			case 4:
				System.out.println("SIGN \t SD14 \t SD13 \t SD12 \t SD11 \t SD10 \t SD9 \t SD8 \t SD7 \t SD6 \t SD5 \t SD4 \t SD3 \t SD2 \t SD1 \t SD0");
			break;
			// PGA = 1/4
			case 3:
				System.out.println("SIGN \t SIGN \t SD13 \t SD12 \t SD11 \t SD10 \t SD9 \t SD8 \t SD7 \t SD6 \t SD5 \t SD4 \t SD3 \t SD2 \t SD1 \t SD0");
			break;
			// PGA = 1/2
			case 2:
				System.out.println("SIGN \t SIGN \t SIGN \t SD12 \t SD11 \t SD10 \t SD9 \t SD8 \t SD7 \t SD6 \t SD5 \t SD4 \t SD3 \t SD2 \t SD1 \t SD0");
			break;
			// PGA = 1
			case 1:
				System.out.println("SIGN \t SIGN \t SIGN \t SIGN \t SD11 \t SD10 \t SD9 \t SD8 \t SD7 \t SD6 \t SD5 \t SD4 \t SD3 \t SD2 \t SD1 \t SD0");
			break;
			default:
				System.out.println("Error getting Gain setting : value out of range");
			break;
		}

		// Formatting
		reg_shuntvoltage_string = String.format("%16s", Integer.toBinaryString(reg_shuntvoltage));
		reg_shuntvoltage_string = reg_shuntvoltage_string.replace(' ', '0');

		// Showing shunt voltage register
		for (int i = 0; i < reg_shuntvoltage_string.length(); i++)
		{
			System.out.print(reg_shuntvoltage_string.charAt(i) + "\t ");
		}

		System.out.println();
	}

	/**
	 * Shows the INA219 bus voltage register in a user-friendly way
	 */
	public void show_bus_voltage_register() throws IOException
	{
		int reg_busvoltage = 0;
		String reg_busvoltage_string = "";

		try
		{
			reg_busvoltage = getBusVoltageRegister();
		}
		catch (IOException ioe)
		{
			System.err.println("Exception during bus voltage register showing");
			System.err.println("Exception : " + ioe.getMessage());
		}

		System.out.println("Bus voltage register : ");
		System.out.println("BD12 \t BD11 \t BD10 \t BD9 \t BD8 \t BD7 \t BD6 \t BD5 \t BD4 \t BD3 \t BD2 \t BD1 \t BD0 \t - \t CNVR \t OVF");

		// Formatting
		reg_busvoltage_string = String.format("%16s", Integer.toBinaryString(reg_busvoltage));
		reg_busvoltage_string = reg_busvoltage_string.replace(' ', '0');

		// Showing bus voltage register
		for (int i = 0; i < reg_busvoltage_string.length(); i++)
		{
			System.out.print(reg_busvoltage_string.charAt(i) + "\t ");
		}
		
		System.out.println();
	}

	/**
	 * Shows all the INA219 registers in a user-friendly way
	 */
	public void show_all_registers() throws IOException
	{
		show_configuration();
		show_calibration();
		show_shunt_voltage_register();
		show_bus_voltage_register();
		show_current_register();
		show_power_register();

	}

	/**
	 * Reads the shunt resistor voltage (Vin+ - Vin-) in V
	 * @return Shunt resistor voltage in V
	 */
	public double read_shunt_voltage() throws IOException
	{
		short shunt_voltage = 0;
		int register = 0;
		double shunt_voltage_V = 0;
		
		byte[] buffer = new byte[2];
		DataInputStream Ina219In;

		try
		{
			// int read(int address, byte[] buffer, int offset, int size) throws IOException
			// Reading 2 bytes (16 bits) from the register
			register = ina219.read(ina219_reg_shuntvoltage, buffer, 0, 2);

			Ina219In = new DataInputStream(new ByteArrayInputStream(buffer));
			// Short = 2 bytes = 16 bits
			shunt_voltage = Ina219In.readShort();

			// Conversion in Volts
			shunt_voltage_V = shunt_voltage;

			// TODO : Adapt divider to configuration / calibration values in accordance to resolution
			shunt_voltage_V = twosComplement(shunt_voltage) / 10.0;

			System.out.println("Shunt voltage : " + shunt_voltage_V + " V");
		}

		catch (IOException ioe) 
		{
			System.err.println("Exception during shunt voltage reading");
			System.err.println("Exception : " + ioe.getMessage());
		}

		return shunt_voltage_V;
	}

	/**
	 * Reads the bus voltage (Vin- - GND) voltage in V
	 * @return Bus voltage in V
	 */
	public double read_bus_voltage() throws IOException
	{
		int register = 0;
		int bus_voltage = 0;
		double bus_voltage_V = 0;

		String bus_voltage_string_complete = "";
		String bus_voltage_string_data = "";
		
		byte[] buffer = new byte[2];
		DataInputStream Ina219In;

		try
		{
			// Reading 2 bytes (16 bits) from the register
			register = ina219.read(ina219_reg_busvoltage, buffer, 0, 2);
			
			Ina219In = new DataInputStream(new ByteArrayInputStream(buffer));
			// Short = 2 bytes = 16 bits
			bus_voltage = Ina219In.readShort();

			bus_voltage_string_complete = Integer.toString(bus_voltage);

			// Truncate the last 3 bits (CNV, OVF)
			for (int i = 0; i < bus_voltage_string_complete.length() - 3; i++)
			{
				bus_voltage_string_data += bus_voltage_string_complete.charAt(i);
			}

			if (bus_voltage_string_data != "")
			{
				bus_voltage = Integer.parseInt(bus_voltage_string_data);
			}

			else 
			{
				bus_voltage = 0;
			}

			// Conversion in Volts
			bus_voltage_V = bus_voltage / 100.0;

			System.out.println("Bus voltage : " + bus_voltage_V + " V");

		}

		catch (IOException ioe) 
		{
			System.err.println("Exception during bus voltage reading");
			System.err.println("Exception : " + ioe.getMessage());
		}

		return bus_voltage_V;
	}

	/**
	 * Reads the current flowing through the shunt resistor in A
	 * Current register defaults to 0 because the calibration register defaults to 0, yielding a zero current value until the calibration register is programmed.
	 * Call a calibration function before calling read_current()
	 * @return Current flowing through the shunt resistor in A
	 */
	public double read_current() throws IOException
	{
		double current = 0;
		byte[] buffer = new byte[2];
		DataInputStream Ina219In;

		try
		{

			current = ina219.read(ina219_reg_current, buffer, 0, 2);

			Ina219In = new DataInputStream(new ByteArrayInputStream(buffer));
			// Short = 2 bytes = 16 bits
			current = Ina219In.readShort() / 100.0;

			System.out.println("Current : " + current + " A");

		}
		catch (IOException ioe) 
		{
			System.err.println("Exception during current reading");
			System.err.println("Exception : " + ioe.getMessage());
		}

		return current;
	}

	/**
	 * Reads the power ((current x bus voltage) / 5000) in W
	 * Power register defaults to 0 because the calibration register defaults to 0, yielding a zero power value until the calibration register is programmed.
	 * Call a calibration function before calling read_power()
	 * @return Power in W
	 */
	public double read_power() throws IOException
	{
		double power = 0;
		byte[] buffer = new byte[2];
		DataInputStream Ina219In;

		try
		{

			power = ina219.read(ina219_reg_power, buffer, 0, 2);

			Ina219In = new DataInputStream(new ByteArrayInputStream(buffer));
			// Short = 2 bytes = 16 bits
			power = Ina219In.readShort();

			System.out.println("Power : " + power + " W");

		}
		catch (IOException ioe)
		{
			System.err.println("Exception during power reading");
			System.err.println("Exception : " + ioe.getMessage());
		}

		return power;
	}

	/**
	 * Gets the content of the Configuration register
	 */
	public int getConfigurationRegister() throws IOException
	{
		int reg_config = 0;
		byte[] buffer = new byte[2];
		DataInputStream Ina219In;

		try
		{
			reg_config = ina219.read(ina219_reg_config, buffer, 0, 2);

			Ina219In = new DataInputStream(new ByteArrayInputStream(buffer));
			// Short = 2 bytes = 16 bits
			reg_config = Ina219In.readShort();
		}
		catch (IOException ioe)
		{
			System.err.println("Exception during configuration register reading");
			System.err.println("Exception : " + ioe.getMessage());
		}

		return reg_config;
	}

	/**
	 * Gets the content of the Calibration register
	 */
	public int getCalibrationRegister() throws IOException
	{
		int reg_calibration = 0;
		byte[] buffer = new byte[2];
		DataInputStream Ina219In;

		try
		{
			reg_calibration = ina219.read(ina219_reg_calibration, buffer, 0, 2);

			Ina219In = new DataInputStream(new ByteArrayInputStream(buffer));
			// Short = 2 bytes = 16 bits
			reg_calibration = Ina219In.readShort();
		}

		catch (IOException ioe)
		{
			System.err.println("Exception during calibration register reading");
			System.err.println("Exception : " + ioe.getMessage());
		}

		return reg_calibration;
	}

	/**
	 * Gets the content of the Shunt Voltage register
	 */
	public int getShuntVoltageRegister() throws IOException
	{
		int reg_shuntvoltage = 0;
		byte[] buffer = new byte[2];
		DataInputStream Ina219In;

		try
		{
			ina219.read(ina219_reg_shuntvoltage, buffer, 0, 2);

			Ina219In = new DataInputStream(new ByteArrayInputStream(buffer));
			// Short = 2 bytes = 16 bits
			reg_shuntvoltage = Ina219In.readShort();
		}

		catch (IOException ioe)
		{
			System.err.println("Exception during shunt voltage register reading");
			System.err.println("Exception : " + ioe.getMessage());
		}

		return reg_shuntvoltage;
	}

	/**
	 * Gets the content of the Bus Voltage register
	 */
	public int getBusVoltageRegister() throws IOException
	{
		int reg_busvoltage = 0;
		byte[] buffer = new byte[2];
		DataInputStream Ina219In;

		try
		{
			ina219.read(ina219_reg_busvoltage, buffer, 0, 2);

			Ina219In = new DataInputStream(new ByteArrayInputStream(buffer));
			// Short = 2 bytes = 16 bits
			reg_busvoltage = Ina219In.readShort();
		}
		catch (IOException ioe)
		{
			System.err.println("Exception during bus voltage register reading");
			System.err.println("Exception : " + ioe.getMessage());
		}

		return reg_busvoltage;
	}

	/**
	 * Gets the content of the current register
	 */
	public int getCurrentRegister() throws IOException
	{
		int register;
		int reg_current = 0;
		byte[] buffer = new byte[2];
		DataInputStream Ina219In;

		try
		{
			register = ina219.read(ina219_reg_current, buffer, 0, 2);

			Ina219In = new DataInputStream(new ByteArrayInputStream(buffer));
			// Short = 2 bytes = 16 bits
			reg_current = Ina219In.readShort();
		}
		catch (IOException ioe)
		{
			System.err.println("Exception during current register reading");
			System.err.println("Exception : " + ioe.getMessage());
		}
		
		return reg_current;
	}

	/**
	 * Gets the content of the power register
	 */
	public int getPowerRegister() throws IOException
	{
		int reg_power = 0;
		byte[] buffer = new byte[2];
		DataInputStream Ina219In;

		try
		{
			ina219.read(ina219_reg_power, buffer, 0, 2);

			Ina219In = new DataInputStream(new ByteArrayInputStream(buffer));
			// Short = 2 bytes = 16 bits
			reg_power = Ina219In.readShort();
		}
		catch (IOException ioe)
		{
			System.err.println("Exception during power register reading");
			System.err.println("Exception : " + ioe.getMessage());
		}

		return reg_power;
	}

	/**
	 * Gets the Bus Voltage Range setting
	 * @return Bus Voltage Range setting
	 */
	public int getVoltageRangeSetting() throws IOException
	{
		int voltageRangeSetting = 0;
		int configurationRegister = 0;
		String configurationRegisterString = "";
		String voltageRangeSettingString = "";

		configurationRegister = getConfigurationRegister();
		configurationRegisterString = String.format("%16s", Integer.toBinaryString(configurationRegister)).replace(' ', '0');

		// Truncate bit 13 (BRNG)
		voltageRangeSettingString = configurationRegisterString.substring(2,3);
		
		if (!voltageRangeSettingString.equals(""))
		{
			voltageRangeSetting = Integer.parseInt(voltageRangeSettingString);
		}
		else
		{
			voltageRangeSetting = 0;
		}

		switch (voltageRangeSetting)
		{
			// 16V Range
			case 0:
				voltageRangeSetting = 1;
			break;
			// 32V Range
			case 1:
				voltageRangeSetting = 2;
			break;
			// Error handling
			default:
				System.err.println("Error reading Voltage Range setting");
			return -1;
		}

		return voltageRangeSetting;
	}

	/**
	 * Gets the Gain setting
	 * @return Gain setting
	 */
	public int getGainSetting() throws IOException
	{
		int gainSetting = 0;
		int configurationRegister = 0;
		int pg0, pg1;
		String configurationRegisterString = "";

		configurationRegister = getConfigurationRegister();
		configurationRegisterString = String.format("%16s", Integer.toBinaryString(configurationRegister)).replace(' ', '0');

		// Truncate bits PG1 & PG0
		pg1 = Integer.parseInt(configurationRegisterString.substring(3,4));
		pg0 = Integer.parseInt(configurationRegisterString.substring(4,5));

		// Gain 1
		if (pg1 == 0 && pg0 == 0)
		{
			gainSetting = 1;
		}
		// Gain 1/2
		else if (pg1 == 0 && pg0 == 1)
		{
			gainSetting = 2;
		}
		// Gain 1/4
		else if (pg1 == 1 && pg0 == 0)
		{
			gainSetting = 3;
		}
		// Gain 1/8
		else if (pg1 == 1 && pg0 == 1)
		{
			gainSetting = 4;
		}
		// Error handling
		else
		{
			System.err.println("Error reading Gain setting. PG0 and PG1 bits out of range");
			return -1;
		}

		return gainSetting;

	}

	/**
	 * Gets the Bus ADC Resolution setting
	 * @return Bus ADC Resolution Setting
	 */
	public int getBusADCResolutionSetting() throws IOException
	{
		int shuntADCResolutionSetting = 0;
		int configurationRegister= 0;
		int badc4, badc3, badc2, badc1;
		String configurationRegisterString;

		configurationRegister = getConfigurationRegister();
		configurationRegisterString = String.format("%16s", Integer.toBinaryString(configurationRegister)).replace(' ', '0');

		// Truncate bits BADC4, BADC3, BADC2, BADC1
		badc4 = Integer.parseInt(configurationRegisterString.substring(5,6));
		badc3 = Integer.parseInt(configurationRegisterString.substring(6,7));
		badc2 = Integer.parseInt(configurationRegisterString.substring(7,8));
		badc1 = Integer.parseInt(configurationRegisterString.substring(8,9));

		// 9 bits resolution 1 sample
		if (badc4 == 0 && badc2 == 0 && badc1 == 0)
		{
			shuntADCResolutionSetting = 1;
		}
		// 10 bits resolution 1 sample
		else if (badc4 == 0 && badc2 == 0 && badc1 == 1)
		{
			shuntADCResolutionSetting = 2;
		}
		// 11 bits resolution 1 sample
		else if (badc4 == 0 && badc2 == 1 && badc1 == 0)
		{
			shuntADCResolutionSetting = 3;
		}
		// 12 bits resolution 1 sample
		else if (badc4 == 0 && badc2 == 1 && badc1 == 1)
		{
			shuntADCResolutionSetting = 4;
		}
		// 12 bits resolution 2 samples
		else if (badc4 == 1 && badc3 == 0 && badc2 == 0 && badc1 == 0)
		{
			shuntADCResolutionSetting = 5;
		}
		// 12 bits resolution 4 samples
		else if (badc4 == 1 && badc3 == 0 && badc2 == 0 && badc1 == 1)
		{
			shuntADCResolutionSetting = 6;
		}
		// 12 bits resolution 8 samples
		else if (badc4 == 1 && badc3 == 0 && badc2 == 1 && badc1 == 0)
		{
			shuntADCResolutionSetting = 7;
		}
		// 12 bits resolution 16 samples
		else if (badc4 == 1 && badc3 == 0 && badc2 == 1 && badc1 == 1)
		{
			shuntADCResolutionSetting = 8;
		}
		// 12 bits resolution 32 samples
		else if (badc4 == 1 && badc3 == 1 && badc2 == 0 && badc1 == 0)
		{
			shuntADCResolutionSetting = 9;
		}
		// 12 bits resolution 64 samples
		else if (badc4 == 1 && badc3 == 1 && badc2 == 0 && badc1 == 1)
		{
			shuntADCResolutionSetting = 10;
		}
		// 12 bits resolution 128 samples
		else if (badc4 == 1 && badc3 == 1 && badc2 == 1 && badc1 == 1)
		{
			shuntADCResolutionSetting = 11;
		}
		// Error handling
		else 
		{
			System.err.println("Error during Bus ADC Resolution Setting reading : SADC bits out of range");
			return -1;
		}

		return shuntADCResolutionSetting;
	}

	/**
	 * Gets the Shunt ADC Resolution setting
	 * @return Shunt ADC Resolution setting
	 */
	public int getShuntADCResolutionSetting() throws IOException
	{
		int shuntADCResolutionSetting = 0;
		int configurationRegister;
		int sadc4, sadc3, sadc2, sadc1;
		String configurationRegisterString;

		configurationRegister = getConfigurationRegister();
		configurationRegisterString = String.format("%16s", Integer.toBinaryString(configurationRegister)).replace(' ', '0');

		// Truncate bits SADC4, SADC3, SADC2, SADC1
		sadc4 = Integer.parseInt(configurationRegisterString.substring(9,10));
		sadc3 = Integer.parseInt(configurationRegisterString.substring(8,9));
		sadc2 = Integer.parseInt(configurationRegisterString.substring(7,8));
		sadc1 = Integer.parseInt(configurationRegisterString.substring(6,7));

		// 9 bits resolution 1 sample
		if (sadc4 == 0 && sadc2 == 0 && sadc1 == 0)
		{
			shuntADCResolutionSetting = 1;
		}
		// 10 bits resolution 1 sample
		else if (sadc4 == 0 && sadc2 == 0 && sadc1 == 1)
		{
			shuntADCResolutionSetting = 2;
		}
		// 11 bits resolution 1 sample
		else if (sadc4 == 0 && sadc2 == 1 && sadc1 == 0)
		{
			shuntADCResolutionSetting = 3;
		}
		// 12 bits resolution 1 sample
		else if (sadc4 == 0 && sadc2 == 1 && sadc1 == 1)
		{
			shuntADCResolutionSetting = 4;
		}
		// 12 bits resolution 2 samples
		else if (sadc4 == 1 && sadc3 == 0 && sadc2 == 0 && sadc1 == 0)
		{
			shuntADCResolutionSetting = 5;
		}
		// 12 bits resolution 4 samples
		else if (sadc4 == 1 && sadc3 == 0 && sadc2 == 0 && sadc1 == 1)
		{
			shuntADCResolutionSetting = 6;
		}
		// 12 bits resolution 8 samples
		else if (sadc4 == 1 && sadc3 == 0 && sadc2 == 1 && sadc1 == 0)
		{
			shuntADCResolutionSetting = 7;
		}
		// 12 bits resolution 16 samples
		else if (sadc4 == 1 && sadc3 == 0 && sadc2 == 1 && sadc1 == 1)
		{
			shuntADCResolutionSetting = 8;
		}
		// 12 bits resolution 32 samples
		else if (sadc4 == 1 && sadc3 == 1 && sadc2 == 0 && sadc1 == 0)
		{
			shuntADCResolutionSetting = 9;
		}
		// 12 bits resolution 64 samples
		else if (sadc4 == 1 && sadc3 == 1 && sadc2 == 0 && sadc1 == 1)
		{
			shuntADCResolutionSetting = 10;
		}
		// 12 bits resolution 128 samples
		else if (sadc4 == 1 && sadc3 == 1 && sadc2 == 1 && sadc1 == 1)
		{
			shuntADCResolutionSetting = 11;
		}
		// Error handling
		else 
		{
			System.err.println("Error during Shunt ADC Resolution Setting reading : SADC bits out of range");
			return -1;
		}

		return shuntADCResolutionSetting;
	}

	/**
	 * Gets the Operating Mode setting
	 * @return Operating Mode setting
	 */
	public int getOperatingModeSetting() throws IOException
	{
		int operatingModeSetting = 0;
		int configurationRegister;
		int mode3, mode2, mode1;
		String configurationRegisterString;

		configurationRegister = getConfigurationRegister();
		configurationRegisterString = String.format("%16s", Integer.toBinaryString(configurationRegister)).replace(' ', '0');

		mode3 = Integer.parseInt(configurationRegisterString.substring(13,14));
		mode2 = Integer.parseInt(configurationRegisterString.substring(14,15));
		mode1 = Integer.parseInt(configurationRegisterString.substring(15,16));

		// Powerdown mode
		if (mode3 == 0 && mode2 == 0 && mode1 == 0)
		{
			operatingModeSetting = 1;
		}
		// Shunt Voltage Triggered mode
		else if (mode3 == 0 && mode2 == 0 && mode1 == 1)
		{
			operatingModeSetting = 2;
		}
		// Bus Voltage Triggered mode
		else if (mode3 == 0 && mode2 == 1 && mode1 == 0)
		{
			operatingModeSetting = 3;
		}
		// Shunt and Bus Voltage Triggered mode
		else if (mode3 == 0 && mode2 == 1 && mode1 == 1)
		{
			operatingModeSetting = 4;
		}
		// ADC OFF mode
		else if (mode3 == 1 && mode2 == 0 && mode1 == 0)
		{
			operatingModeSetting = 5;
		}
		// Shunt Voltage Continuous mode
		else if (mode3 == 1 && mode2 == 0 && mode1 == 1)
		{
			operatingModeSetting = 6;
		}
		// Bus Voltage Continuous mode
		else if (mode3 == 1 && mode2 == 1 && mode1 == 0)
		{
			operatingModeSetting = 7;
		}
		// Shunt and Bus Voltage Continuous mode
		else if (mode3 == 1 && mode2 == 1 && mode1 == 1)
		{
			operatingModeSetting = 8;
		}
		// Error handling
		else
		{
			System.err.println("Error during Operating Mode reading : MODE bits out of range");
			return -1;
		}

		return operatingModeSetting;
	}

	/**
	 * Sets the Bus Voltage Range to the provided mode (16V / 32V) in the configuration register
	 * @param busVoltageRange Bus Voltage Range
	 */
	public void setBusVoltageRange(BusVoltageRange busVoltageRange) throws IOException
	{
		try
		{
			// Current state of the configuration register
			int configurationRegister = getConfigurationRegister();
			String configurationRegisterString = String.format("%16s", Integer.toBinaryString(configurationRegister)).replace(' ', '0');

			// State to change in the configuration register
			int value = busVoltageRange.getValue();
			String valueString = String.format("%16s", Integer.toBinaryString(value)).replace(' ', '0');

			// New state of the configuration register
			int newConfigurationRegister = 0;
			String newConfigurationRegisterString = "";

			for (int i = 0; i < configurationRegisterString.length(); i++)
			{
				// BRNG bit
				if (i == 2)
				{
					newConfigurationRegisterString += valueString.charAt(i);
				}
				// Other bits
				else
				{
					newConfigurationRegisterString += configurationRegisterString.charAt(i);
				}	
			}

			// Formatting
			newConfigurationRegisterString = newConfigurationRegisterString.replace(' ', '0');
			newConfigurationRegister = Integer.parseInt(newConfigurationRegisterString, 2);

			// Writing new bus voltage range setting into the sensor configuration register
			byte[] buffer = ByteBuffer.allocate(4).putInt(newConfigurationRegister).array();
			ina219.write(ina219_reg_config, buffer, 2, 2);

			// Setting current bus voltage range setting
			this.busVoltageRange.setValue(busVoltageRange.getValue());
		}

		catch (IOException ioe)
		{
			System.err.println("Exception during Bus Voltage setting");
			System.err.println("Exception : " + ioe.getMessage());
		}
	}

	/**
	 * Sets the Gain to the provided mode (1, 1/2, 1/4, 1/8) in the configuration register
	 * @param gain Gain
	 */
	public void setGain(Gain gain) throws IOException
	{
		try
		{
			// Current state of the configuration register
			int configurationRegister = getConfigurationRegister();
			String configurationRegisterString = String.format("%16s", Integer.toBinaryString(configurationRegister)).replace(' ', '0');

			// State to change in the configuration register
			int value = gain.getValue();
			String valueString = String.format("%16s", Integer.toBinaryString(value)).replace(' ', '0');

			// New state of the configuration register
			int newConfigurationRegister = 0;
			String newConfigurationRegisterString = "";

			for (int i = 0; i < configurationRegisterString.length(); i++)
			{
				// PG1 & PG0 bits
				if (i == 3 || i == 4)
				{
					newConfigurationRegisterString += valueString.charAt(i);
				}
				// Other bits
				else
				{
					newConfigurationRegisterString += configurationRegisterString.charAt(i);
				}	
			}

			// Formatting
			newConfigurationRegisterString = newConfigurationRegisterString.replace(' ', '0');
			newConfigurationRegister = Integer.parseInt(newConfigurationRegisterString, 2);

			// Writing new gain setting into the sensor configuration register
			byte[] buffer = ByteBuffer.allocate(4).putInt(newConfigurationRegister).array();
			ina219.write(ina219_reg_config, buffer, 2, 2);

			// Setting current gain setting
			this.gain.setValue(gain.getValue());
		}
		catch (IOException ioe) 
		{
			System.err.println("Exception during Gain setting");
			System.err.println("Exception : " + ioe.getMessage());
		}

	}

	/**
	 * Sets the Bus ADC Resolution in the configuration register
	 * @param busADCResolution Bus ADC Resolution
	 */
	public void setBusADCResolution(BusADCResolution busADCResolution) throws IOException
	{
		try
		{
			// Current state of the configuration register
			int configurationRegister = getConfigurationRegister();
			String configurationRegisterString = String.format("%16s", Integer.toBinaryString(configurationRegister)).replace(' ', '0');

			// State to change in the configuration register
			int value = busADCResolution.getValue();
			String valueString = String.format("%16s", Integer.toBinaryString(value)).replace(' ', '0');

			// New state of the configuration register
			int newConfigurationRegister = 0;
			String newConfigurationRegisterString = "";

			for (int i = 0; i < configurationRegisterString.length(); i++)
			{
				// BADC4, BADC3, BADC2, BADC1 bits
				if (i == 5 || i == 6 || i == 7 || i == 8)
				{
					newConfigurationRegisterString += valueString.charAt(i);
				}
				// Other bits
				else
				{
					newConfigurationRegisterString += configurationRegisterString.charAt(i);
				}	
			}

			// Formatting
			newConfigurationRegisterString = newConfigurationRegisterString.replace(' ', '0');
			newConfigurationRegister = Integer.parseInt(newConfigurationRegisterString, 2);

			// Writing new bus ADC resolution setting into the sensor configuration register
			byte[] buffer = ByteBuffer.allocate(4).putInt(newConfigurationRegister).array();
			ina219.write(ina219_reg_config, buffer, 2, 2);

			// Setting current bus ADC resolution setting
			this.busADCResolution.setValue(busADCResolution.getValue());
		}

		catch (IOException ioe)
		{
			System.err.println("Exception during Bus Voltage setting");
			System.err.println("Exception : " + ioe.getMessage());
		}
	}

	/**
	 * Sets the Shunt ADC Resolution in the configuration register
	 * @param shuntADCResolution Shunt ADC Resolution
	 */
	public void setShuntADCResolution(ShuntADCResolution shuntADCResolution) throws IOException
	{
		try
		{
			// Current state of the configuration register
			int configurationRegister = getConfigurationRegister();
			String configurationRegisterString = String.format("%16s", Integer.toBinaryString(configurationRegister)).replace(' ', '0');

			// State to change in the configuration register
			int value = shuntADCResolution.getValue();
			String valueString = String.format("%16s", Integer.toBinaryString(value)).replace(' ', '0');

			// New state of the configuration register
			int newConfigurationRegister = 0;
			String newConfigurationRegisterString = "";

			for (int i = 0; i < configurationRegisterString.length(); i++)
			{
				// SADC4, SADC3, SADC2, SADC1 bits
				if (i == 9 || i == 10 || i == 11 || i == 12)
				{
					newConfigurationRegisterString += valueString.charAt(i);
				}
				// Other bits
				else
				{
					newConfigurationRegisterString += configurationRegisterString.charAt(i);
				}	
			}

			// Formatting
			newConfigurationRegisterString = newConfigurationRegisterString.replace(' ', '0');
			newConfigurationRegister = Integer.parseInt(newConfigurationRegisterString, 2);

			// Writing new shunt ADC resolution setting into the sensor configuration register
			byte[] buffer = ByteBuffer.allocate(4).putInt(newConfigurationRegister).array();
			ina219.write(ina219_reg_config, buffer, 2, 2);

			// Setting current shunt ADC resolution setting
			this.shuntADCResolution.setValue(shuntADCResolution.getValue());
		}
		catch (IOException ioe) 
		{
			System.err.println("Exception during Shunt ADC Resolution setting");
			System.err.println("Exception : " + ioe.getMessage());
		}
	} 

	/**
	 * Sets the Operating Mode in the configuration register
	 * @param operatingMode Operating Mode
	 */
	public void setOperatingMode(OperatingMode operatingMode) throws IOException
	{
		try
		{
			// Current state of the configuration register
			int configurationRegister = getConfigurationRegister();
			String configurationRegisterString = String.format("%16s", Integer.toBinaryString(configurationRegister)).replace(' ', '0');

			// State to change in the configuration register
			int value = operatingMode.getValue();
			String valueString = String.format("%16s", Integer.toBinaryString(value)).replace(' ', '0');

			// New state of the configuration register
			int newConfigurationRegister = 0;
			String newConfigurationRegisterString = "";

			for (int i = 0; i < configurationRegisterString.length(); i++)
			{
				// SADC4, SADC3, SADC2, SADC1 bits
				if (i == 9 || i == 10 || i == 11 || i == 12)
				{
					newConfigurationRegisterString += valueString.charAt(i);
				}
				// Other bits
				else
				{
					newConfigurationRegisterString += configurationRegisterString.charAt(i);
				}	
			}

			// Formatting
			newConfigurationRegisterString = newConfigurationRegisterString.replace(' ', '0');
			newConfigurationRegister = Integer.parseInt(newConfigurationRegisterString, 2);

			// Writing new operating mode setting into the sensor configuration register	
			byte[] buffer = ByteBuffer.allocate(4).putInt(newConfigurationRegister).array();
			ina219.write(ina219_reg_config, buffer, 2, 2);

			// Setting current operating mode setting
			this.operatingMode.setValue(operatingMode.getValue());
		}
		catch (IOException ioe) 
		{
			System.err.println("Exception during Operating Mode setting");
			System.err.println("Exception : " + ioe.getMessage());
		}
	}

	/**
	 * Generates a system reset that is the same as power-on reset. Resets all registers to default values.
	 */
	public void reset() throws IOException
	{
		System.out.println("Resetting device ...");
		
		try
		{
			// This bit self-clears.
			ina219.write(ina219_reg_config, (byte) ina219_config_reset);

			/*
			Runtime runtime = Runtime.getRuntime();
			String[] args = { "/bin/sh", "-c", "i2cset -y 1 0x40 0x00 0x8000 w" };
			final Process process = runtime.exec(args);
			*/

			System.out.println("Successful device reset");
		}
		catch (IOException ioe)
		{
			System.err.println("Exception during device reset");
			System.err.println("Exception : " + ioe.getMessage());
		}
	}

	/**
	 * Converts the twos-complement decimal representation to the absolute value represented by the twos-complement
	 * 0000 1111 1010 0000 = 4000 in binary (and twos-complement) becomes 4000
	 * 1111 0000 0110 0000 = 61536 in binary and -4000 in twos-complement becomes 4000
	 * @param  complement The twos-complement decimal representation to convert
	 * @return            The absolute value of the twos-complement representated number
	 */
	public int twosComplement(int complement)
	{
		int uncomplemented = 0;

		String complemented_binary_string = "";
		String uncomplemented_string      = "";

		// Avoiding collisions with the two ways of writing 0 in 16-bit twos-complement format (0000000000000000 and 10000000000000000) because 0 can be +0 or -0
		if (complement < 0 || complement == 0 || complement == 1)
		{
			return 0;
		}

		// Formatting 16-bit twos-complement binary String
		complemented_binary_string = String.format("%16s", Integer.toBinaryString(complement));
		complemented_binary_string = complemented_binary_string.replace(' ', '0');

		// Truncates sign bit
		complemented_binary_string = complemented_binary_string.substring(1, complemented_binary_string.length());

		// Negative number
		if (isNegative(complement))
		{

			// Substracting one added by the twos-complement conversion
			complement = Integer.parseInt(complemented_binary_string, 2);
			complement--;

			// Formatting 16-bit twos-complement binary String
			complemented_binary_string = String.format("%15s", Integer.toBinaryString(complement));
			complemented_binary_string = complemented_binary_string.replace(' ', '0');

			// Complementing binary string
			uncomplemented_string = complement(complemented_binary_string);

		}
		// Positive number
		else
		{
			uncomplemented_string = complemented_binary_string;
		}

		uncomplemented = Integer.parseInt(uncomplemented_string, 2);

		return uncomplemented;
	}

	/**
	 * Tells if a twos-complement formated decimal representation of a number is a negative number by checking its sign-bit
	 * Warning : 0 has 2 representations (0000000000000000 and 100000000000000) in twos-complement format and can be either flagged positive or negative
	 * @param  complement The twos-complement decimal number to sign-check
	 * @return            True if the number is negative, false if the number is positive 
	 */
	public boolean isNegative(int complement)
	{
		String sign = "";
		String complemented_binary_string = "";

		// Formatting 16-bit twos-complement binary String
		complemented_binary_string = String.format("%16s", Integer.toBinaryString(complement));
		complemented_binary_string = complemented_binary_string.replace(' ', '0');

		sign = complemented_binary_string.substring(0, 1);

		// Negative number
		if (sign.equals("1"))
		{
			return true;
		}
		// Positive number
		else if (sign.equals("0"))
		{
			return false;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Complements (0 --> 1 / 1 --> 0) a String-formatted binary number
	 * @param  toComplement The binary number to complement
	 * @return              The complemented binary number
	 */
	public String complement(String toComplement)
	{
		String complemented = "";

		// Complementing binary string
		for (int i = 0; i < toComplement.length(); i++)
		{

			if (toComplement.charAt(i) == '0')
			{
				complemented += '1';
			}

			else if (toComplement.charAt(i) == '1')
			{
				complemented += '0';
			}

			else
			{
				System.err.println("Error in complementation conversion");
			}

		}

		return complemented;
	}

	@SuppressWarnings("static-access")
	public static void main (String[] args) throws IOException, InterruptedException
	{
		Ina219 ina219 = new Ina219();
		Menu menu = new Menu();
		
		// ina219.reset();
		ina219.calibrate_default();					// Calibrating the INA219 to default values
		
		ina219.show_all_registers();				// Showing all registers on startup

		ina219.read_shunt_voltage();				// Reading shunt voltage
		ina219.read_bus_voltage();					// Reading bus boltage
		ina219.read_current();						// Reading current flowing through
		ina219.read_power();						// Reading power flowing through

		menu.showRootMenu();						// Showing menu
	}
}