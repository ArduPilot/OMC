/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.desktop.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MACaddrSource {
	
	public static Pattern patternMACadress = Pattern
			.compile(".*((:?[0-9a-f]{2}[-:]){5}[0-9a-f]{2}).*",Pattern.CASE_INSENSITIVE);

	static Logger log = Logger.getLogger("eu.mavinci");
	
//	public static void main(String[] args) {
//		String line = "line:tap1      Link encap:Ethernet  Hardware Adresse 9A:dB-7A:07:84:AD  ";
//		System.out.println("line To Parse"+line);
//		Matcher mm = patternMACadress.matcher(line);
//		if (mm.matches()){
//			System.out.println("found!");
//			System.out.println(mm);
//			System.out.println(mm.group(1).toUpperCase().replace("-", ":"));
//		}
//	}
	
static Set<String> macs = null;
	
	public static Set<String> getMACs() {
		if (macs != null) return macs;
		
		macs = new TreeSet<String>();
		
		//try to add MACs using direct system calls
		macs.addAll(getFilteredMACsNative());
		
		//if empty try to add MACs using Java utils 
		if(macs.isEmpty()){
			macs.addAll(getFilteredMACsJava());
		}
		
		return macs;
	}
	
	private static Set<String> getFilteredMACsNative(){		
		Set<String> macs = getMACsNative();		
		filterAddresses(macs);
		return macs;
	}
	private static Set<String> getFilteredMACsJava(){		
		Set<String> macs = getMACsPureJava();		
		filterAddresses(macs);
		return macs;
	}
	
	/**
	 * filter all broadcast and not global mac adddresses!
	 */
	private static void filterAddresses(Set<String> macs) {
		Set<String> macsClone = new TreeSet<String>(macs);
		for (String mac : macsClone){
			if (mac.isEmpty()){
				macs.remove(mac);
				continue;
			}
			String secHex = mac.substring(1, 2);
			
			//last bit 0 of first byte    -> non broadcast address
			//prelast bit 0 of first byte -> global unique address
			if (!secHex.equals("0") && !secHex.equals("4") && !secHex.equals("8") && !secHex.equals("C")) macs.remove(mac);
			if (mac.startsWith("00:00:00:00:00:")) macs.remove(mac);
			if (mac.startsWith("00:50:56:")) macs.remove(mac); //VMware server network card
		}
	}
	
	private static Set<String> getMACsNative() {
		Set<String> macs = new TreeSet<String>();
		Process p = null;
		
		try {
			String osname = System.getProperty("os.name", "").toLowerCase();

			if (osname.startsWith("windows")) {
				p = Runtime.getRuntime().exec(
						new String[] { "ipconfig", "/all" }, null);
			}
			// Solaris code must appear before the generic code
			else if (osname.startsWith("solaris") || osname.startsWith("sunos")) {
				String hostName = getFirstLineOfCommand("uname", "-n");
				if (hostName != null) {
					p = Runtime.getRuntime().exec(
							new String[] { "/usr/sbin/arp", hostName }, null);
				}
			} else if (new File("/usr/sbin/lanscan").exists()) {
				p = Runtime.getRuntime().exec(
						new String[] { "/usr/sbin/lanscan" }, null);
			} else if (new File("/sbin/ifconfig").exists()) {
				p = Runtime.getRuntime().exec(
						new String[] { "/sbin/ifconfig", "-a" }, null);
			}

			if (p != null) {
				try(BufferedReader in = new BufferedReader(new InputStreamReader(
						p.getInputStream()), 128)) {
				String line = null;
				while ((line = in.readLine()) != null) {
//					log.fine("getMac:"+ line);
//					System.out.println("line:"+line);
					Matcher mm = patternMACadress.matcher(line);
					if (mm.matches()){
//						System.out.println(mm);
						String macstr =mm.group(1).replace("-", ":").toUpperCase();
						log.fine("getNonJavaMac:"+ line + " -> " + macstr);
						macs.add(macstr);
						}
					}
				}
			}

		} catch (SecurityException ex) {
			// Ignore it.
		} catch (IOException ex) {
			// Ignore it.
		} finally {
			if (p != null) {		
				try {
					p.getErrorStream().close();
				} catch (IOException ex) {
					// Ignore it.
				}
				try {
					p.getOutputStream().close();
				} catch (IOException ex) {
					// Ignore it.
				}
				p.destroy();
			}
		}
		return macs;
	}

	private static Set<String> getMACsPureJava() {
		Set<String> macs = new TreeSet<String>();
		try {

			/*
			 * Get NetworkInterface for the current host and then read the
			 * hardware address.
			 */
			Enumeration<NetworkInterface> nis = NetworkInterface
					.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				NetworkInterface ni = nis.nextElement();
				if (ni != null) {
					//to not call getHardwareAddress if we are not supposed to add the result to the set
					if (ni.isVirtual() || ni.isLoopback()
							|| ni.isPointToPoint())
						continue;
					///////////////
					byte[] mac = ni.getHardwareAddress();

					if (mac != null) {
						String macstr = "";
						/*
						 * Extract each array of mac address and convert it to
						 * hexa with the following format 08:00:27:DC:4A:9E.
						 */
						for (int i = 0; i < mac.length; i++) {
							macstr += String.format("%02X%s", mac[i],
									(i < mac.length - 1) ? ":" : "");
						}
						log.fine("javaGetMAC:" +ni.getDisplayName()+ " " +macstr);
						
							
						macs.add(macstr);
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
			// Ignore it.
		}
		return macs;
	}

	/**
	 * Returns the first line of the shell command.
	 * 
	 * @param commands
	 *            the commands to run
	 * @return the first line of the command
	 * @throws IOException
	 */
	static String getFirstLineOfCommand(String... commands) throws IOException {
		Process p = null;

		try {
			p = Runtime.getRuntime().exec(commands);
			try (BufferedReader bReader = 
					new BufferedReader(new InputStreamReader(
							p.getInputStream()), 128)) {
				return bReader.readLine();				
			}
		} finally {
			if (p != null) {
				try {				
					p.getErrorStream().close();				
				} catch (IOException ex) {
					// Ignore it.
				}
				try {
					p.getOutputStream().close();
				} catch (IOException ex) {
					// Ignore it.
				}
				p.destroy();
			}
		}
	}


	
	public static String getMACString(){
		String macString = "";
		for (String mac:MACaddrSource.getMACs()){
			if (!macString .isEmpty()) macString  += ",\n";
			macString += mac;
		}
		return macString;

	}
	
	public static void main(String[] args) {
		for (String s:getMACs()){
			System.out.println(s);
		}
	}
}
