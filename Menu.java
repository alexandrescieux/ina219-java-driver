/**
 * User-friendly menu for INA219 configuration/calibration/register reading with INA219 Raspberry Pi Library
 * Useful for test purposes
 *
 * @author Alexandre Scieux
 * @version 1.0
 */

package sensor;

import java.io.*;
import java.util.*;


public class Menu
{
	private static Ina219 ina219;

	public Menu() throws IOException
	{
		 Menu.ina219 = new Ina219();
	}

	/**
	 * Clears the console screen
	 */
	public final static void clearConsole()
	{
		for (int i=0; i<50; i++)
		{
			System.out.println();
		}
	}

	/**
	 * Shows the root menu
	 */
	public static void showRootMenu() throws IOException
	{
		int choice = 0;
		
		try(Scanner scanner = new Scanner(System.in))
		{
			clearConsole();
	
			System.out.println("INA219 Interface");
	
			System.out.println("1 - Configuration");
			System.out.println("2 - Calibration");
			System.out.println("3 - Measures");
			System.out.println("0 - Quit");
	
			try
			{
				choice = scanner.nextInt();
			}
			
			catch (InputMismatchException ime)
			{
				showRootMenu();
			}
					
			switch (choice)
			{
	
				case 0:
					System.exit(0);
				break;
	
				case 1:
					showConfigurationMenu();
				break;
	
				case 2:
					showCalibrationMenu();
				break;
	
				case 3:
					showMeasuresMenu();
				break;
			
				default:
					showRootMenu(); 
				break;            
			}
		}
	}

	/**
	 * Shows the configuration menu
	 */
	private static void showConfigurationMenu() throws IOException
	{
		int choice = 0;
		
		try(Scanner scanner = new Scanner(System.in))
		{
			clearConsole();
	
			System.out.println("INA219 Configuration Menu");
	
			System.out.println("1 - Bus Voltage Range");
			System.out.println("2 - Gain");
			System.out.println("3 - Bus ADC Resolution");
			System.out.println("4 - Shunt ADC Resolution");
			System.out.println("5 - Operating Mode");
			System.out.println("6 - Reset device");
			System.out.println("0 - Previous");
	
			try
			{	
				choice = scanner.nextInt();
			}
			catch (InputMismatchException ime)
			{
				showConfigurationMenu();
			}

			switch (choice)
			{
				case 0:
					showRootMenu();
				break;
	
				case 1:
					showBusVoltageRangeMenu();
				break;
	
				case 2:
					showGainMenu();
				break;
	
				case 3:
					showBusADCResolutionMenu();
				break;
	
				case 4:
					showShuntADCResolutionMenu();
				break;
	
				case 5:
					showOperatingModeMenu();
				break;
	
				case 6:
					showResetMenu();
				break;
			
				default:
					showRootMenu();
				break;            
			}
		}
	}

	/**
	 * Shows the Bus Voltage Range Menu
	 */
	private static void showBusVoltageRangeMenu() throws IOException
	{
		int busVoltageRangeSetting = ina219.getVoltageRangeSetting();

		Ina219.BusVoltageRange busVoltageRangeSetting16V = Ina219.BusVoltageRange.INA219_CONFIG_BVOLTAGERANGE_16V;
		Ina219.BusVoltageRange busVoltageRangeSetting32V = Ina219.BusVoltageRange.INA219_CONFIG_BVOLTAGERANGE_32V;

		int choice = 0;
		
		try(Scanner scanner = new Scanner(System.in))
		{
			clearConsole();
	
			System.out.println("Current Bus Voltage Range setting : " + busVoltageRangeSetting);
	
			System.out.println("1 - 16V Range");
			System.out.println("2 - 32V Range (default)");
			System.out.println("0 - Previous");
	
			try
			{
				choice = scanner.nextInt();	
			}
			catch (InputMismatchException ime)
			{
				showBusVoltageRangeMenu();
			}
	
			switch (choice)
			{
				case 0:
					showConfigurationMenu();
				break;
	
				case 1:
					ina219.setBusVoltageRange(busVoltageRangeSetting16V);
					showConfigurationMenu();
				break;
	
				case 2:
					ina219.setBusVoltageRange(busVoltageRangeSetting32V);
					showConfigurationMenu();
				break;
			
				default:
					showConfigurationMenu(); 
				break;            
			}
		}
	}

	/**
	 * Shows the Gain menu
	 */
	private static void showGainMenu() throws IOException
	{
		int gainSetting = ina219.getGainSetting();
		int choice = 0;
		
		try(Scanner scanner = new Scanner(System.in))
		{
			clearConsole();
	
			System.out.println("Current Gain setting : " + gainSetting);
	
			System.out.println("1 - Gain 1 40 mV Range");
			System.out.println("2 - Gain 2 80 mV Range");
			System.out.println("3 - Gain 4 160 mV Range");
			System.out.println("4 - Gain 8 320 mV Range (default)");
			System.out.println("0 - Previous");
	
			try
			{
				choice = scanner.nextInt();
			}
			catch (InputMismatchException ime)
			{
				showGainMenu();
			}

			switch (choice)
			{
				case 0:
					showConfigurationMenu();
				break;
				case 1:
					ina219.setGain(Ina219.Gain.INA219_CONFIG_GAIN_1_40MV);
					showConfigurationMenu();
				break;
				case 2:
					ina219.setGain(Ina219.Gain.INA219_CONFIG_GAIN_2_80MV);
					showConfigurationMenu();
				break;
				case 3:
					ina219.setGain(Ina219.Gain.INA219_CONFIG_GAIN_4_160MV);
					showConfigurationMenu(); 
				break;
				case 4:
					ina219.setGain(Ina219.Gain.INA219_CONFIG_GAIN_8_320MV);
					showConfigurationMenu(); 
				break;
				default:
					showConfigurationMenu(); 
				break;            
			}
		}
	}

	/**
	 * Shows the Bus ADC Resolution menu
	 */
	private static void showBusADCResolutionMenu() throws IOException
	{
		int busADCResolution = ina219.getBusADCResolutionSetting();
		int choice = 0;
		
		try(Scanner scanner = new Scanner(System.in))
		{
			clearConsole();

			System.out.println("Current Bus ADC Resolution setting : " + busADCResolution);

			System.out.println("1 - 9 Bit Resolution");
			System.out.println("2 - 10 Bit Resolution");
			System.out.println("3 - 11 Bit Resolution");
			System.out.println("4 - 12 Bit Resolution (default)");
			System.out.println("0 - Previous");

			try
			{
				choice = scanner.nextInt();	
			}
			catch (InputMismatchException ime)
			{
				showBusADCResolutionMenu();
			}

			switch (choice)
			{
				case 0:
					showConfigurationMenu();
				break;
				case 1:
					ina219.setBusADCResolution(Ina219.BusADCResolution.INA219_CONFIG_BADCRES_9BIT);
					showConfigurationMenu();
				break;
				case 2:
					ina219.setBusADCResolution(Ina219.BusADCResolution.INA219_CONFIG_BADCRES_10BIT);
					showConfigurationMenu();
				break;
				case 3:
					ina219.setBusADCResolution(Ina219.BusADCResolution.INA219_CONFIG_BADCRES_11BIT);
					showConfigurationMenu();
				break;
				case 4:
					ina219.setBusADCResolution(Ina219.BusADCResolution.INA219_CONFIG_BADCRES_12BIT);
					showConfigurationMenu();
				break;
				default:
					showConfigurationMenu(); 
				break;            
			}
		}
	}

	/**
	 * Shows the Shunt ADC Resolution menu
	 */
	private static void showShuntADCResolutionMenu() throws IOException
	{
		int shuntADCResolution = ina219.getShuntADCResolutionSetting();
		int choice = 0;
		
		try(Scanner scanner = new Scanner(System.in))
		{
			clearConsole();

			System.out.println("Current Shunt ADC Resolution setting : " + shuntADCResolution);

			System.out.println("1 - 9 Bit Resolution 1 Sample");
			System.out.println("2 - 10 Bit Resolution 1 Sample");
			System.out.println("3 - 11 Bit Resolution 1 Sample (default)");
			System.out.println("4 - 12 Bit Resolution 1 Sample");
			System.out.println("5 - 12 Bit Resolution 2 Samples");
			System.out.println("6 - 12 Bit Resolution 4 Samples");
			System.out.println("7 - 12 Bit Resolution 8 Samples");
			System.out.println("8 - 12 Bit Resolution 16 Samples");
			System.out.println("9 - 12 Bit Resolution 32 Samples");
			System.out.println("10 - 12 Bit Resolution 64 Samples");
			System.out.println("11 - 12 Bit Resolution 128 Samples");
			System.out.println("0 - Previous");

			try
			{
				choice = scanner.nextInt();	
			}
			catch (InputMismatchException ime)
			{
				showShuntADCResolutionMenu();
			}

			switch (choice)
			{

				case 0:
					showConfigurationMenu();
				break;
				case 1:
					ina219.setShuntADCResolution(Ina219.ShuntADCResolution.INA219_CONFIG_SADCRES_9BIT_1S_84US);
					showConfigurationMenu();
				break;
				case 2:
					ina219.setShuntADCResolution(Ina219.ShuntADCResolution.INA219_CONFIG_SADCRES_10BIT_1S_148US);
					showConfigurationMenu();
				break;
				case 3:
					ina219.setShuntADCResolution(Ina219.ShuntADCResolution.INA219_CONFIG_SADCRES_11BIT_1S_276US);
					showConfigurationMenu();
				break;
				case 4:
					ina219.setShuntADCResolution(Ina219.ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_1S_532US);
					showConfigurationMenu();
				break;
				case 5:
					ina219.setShuntADCResolution(Ina219.ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_2S_1060US);
					showConfigurationMenu();
				break;
				case 6:
					ina219.setShuntADCResolution(Ina219.ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_4S_2130US);
					showConfigurationMenu();
				break;
				case 7:
					ina219.setShuntADCResolution(Ina219.ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_8S_4260US);
					showConfigurationMenu();
				break;
				case 8:
					ina219.setShuntADCResolution(Ina219.ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_16S_8510US);
					showConfigurationMenu();
				break;
				case 9:
					ina219.setShuntADCResolution(Ina219.ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_32S_17MS);
					showConfigurationMenu();
				break;
				case 10:
					ina219.setShuntADCResolution(Ina219.ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_64S_34MS);
					showConfigurationMenu();
				break;
				case 11:
					ina219.setShuntADCResolution(Ina219.ShuntADCResolution.INA219_CONFIG_SADCRES_12BIT_128S_69MS);
					showConfigurationMenu();
				break;
				default:
					showConfigurationMenu();
				break;  
			}
		}
	}

	/**
	 * Shows the Operating Mode menu
	 */
	private static void showOperatingModeMenu() throws IOException
	{
		int operatingMode = ina219.getOperatingModeSetting();
		int choice = 0;
		
		try(Scanner scanner = new Scanner(System.in))
		{
			clearConsole();

			System.out.println("Current Operating Mode setting : " + operatingMode);

			System.out.println("1 - Powerdown");
			System.out.println("2 - Shunt Volt Triggered");
			System.out.println("3 - Bus Volt Triggered");
			System.out.println("4 - Shunt and Bus Volt Triggered");
			System.out.println("5 - ADC OFF");
			System.out.println("6 - Shunt Volt Continuous");
			System.out.println("7 - Bus Volt Continuous");
			System.out.println("8 - Shunt and Bus Volt Continuous (default)");
			System.out.println("0 - Previous");

			try
			{
				choice = scanner.nextInt();
			}
			catch (InputMismatchException ime)
			{
				showOperatingModeMenu();
			}

			switch (choice)
			{
				case 0:
					showConfigurationMenu();
				break;
				case 1:
					ina219.setOperatingMode(Ina219.OperatingMode.INA219_CONFIG_MODE_POWERDOWN);
					showConfigurationMenu();
				break;
				case 2:
					ina219.setOperatingMode(Ina219.OperatingMode.INA219_CONFIG_MODE_SVOLT_TRIGGERED);
					showConfigurationMenu();
				break;
				case 3:
					ina219.setOperatingMode(Ina219.OperatingMode.INA219_CONFIG_MODE_BVOLT_TRIGGERED);
					showConfigurationMenu();
				break;
				case 4:
					ina219.setOperatingMode(Ina219.OperatingMode.INA219_CONFIG_MODE_SANDBVOLT_TRIGGERED);
					showConfigurationMenu();
				break;
				case 5:
					ina219.setOperatingMode(Ina219.OperatingMode.INA219_CONFIG_MODE_ADCOFF);
					showConfigurationMenu();
				break;
				case 6:
					ina219.setOperatingMode(Ina219.OperatingMode.INA219_CONFIG_MODE_SVOLT_CONTINUOUS);
					showConfigurationMenu();
				break;
				case 7:
					ina219.setOperatingMode(Ina219.OperatingMode.INA219_CONFIG_MODE_BVOLT_CONTINUOUS);
					showConfigurationMenu();
				break;
				case 8:
					ina219.setOperatingMode(Ina219.OperatingMode.INA219_CONFIG_MODE_SANDBVOLT_CONTINUOUS);
					showConfigurationMenu();
				break;
				default:
					showConfigurationMenu();
				break;            
			}
		}
	}

	/**
	 * Shows the Reset menu
	 */
	private static void showResetMenu() throws IOException
	{
		int choice = 0;
		
		try(Scanner scanner = new Scanner(System.in))
		{
			clearConsole();

			System.out.println("1 - Confirm Reset");
			System.out.println("0 - Previous");

			try
			{
				choice = scanner.nextInt();
			}
			catch (InputMismatchException ime)
			{
				showResetMenu();
			}

			switch(choice)
			{
				case 0:
					showConfigurationMenu();
				break;
				case 1:
					ina219.configure_default();
					showRootMenu();
				break;
				default:
					showRootMenu();
				break;
			}
		}
	}

	/**
	 * Shows the Calibration menu
	 */
	private static void showCalibrationMenu() throws IOException
	{

		int choice = 0;

		clearConsole();

		System.out.println("INA219 Calibration");
		ina219.show_calibration();
		System.out.println("1 - Change calibration");
		System.out.println("0 - Previous");

		try(Scanner scanner = new Scanner(System.in))
		{
			try
			{
				choice = scanner.nextInt();
			}
			catch (InputMismatchException ime)
			{
				showCalibrationMenu();
			}

			switch(choice)
			{
				case 0:
					showRootMenu();
				break;
				case 1:
					showNewCalibrationMenu();
				break;
				default:
					showRootMenu();
				break;
			}
		}
	}

	/**
	 * Shows the calibration change menu
	 */
	private static void showNewCalibrationMenu() throws IOException
	{
		int calibration = 0;

		System.out.println("Enter new calibration value : ");

		try(Scanner scanner = new Scanner(System.in))
		{
			try
			{
				calibration = scanner.nextInt();
				ina219.setCalibration(calibration);
			}
			catch (InputMismatchException ime)
			{
				showNewCalibrationMenu();
			}
		}

		showCalibrationMenu();
	}

	/**
	 * Shows the measures menu
	 */
	private static void showMeasuresMenu() throws IOException
	{
		int choice = 0;
		
		try(Scanner scanner = new Scanner(System.in))
		{

			clearConsole();

			System.out.println("INA219 Measures");
			System.out.println("Press 0 for main menu, anything else for refresh");

			ina219.show_all_registers();
			ina219.read_shunt_voltage();
			ina219.read_bus_voltage();
			ina219.read_current();
			ina219.read_power();

			try
			{
				choice = scanner.nextInt();
			}
			catch (InputMismatchException ime)
			{
				showMeasuresMenu();
			}

			switch(choice)
			{
				case 0:
					showRootMenu();
				break;
				case 1:
					showMeasuresMenu();
				break;
				default:
					showRootMenu();
				break;
			}
		}
	}
}