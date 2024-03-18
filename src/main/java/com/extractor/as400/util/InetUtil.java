package com.extractor.as400.util;

import com.extractor.as400.exceptions.InetUtilException;
import com.utmstack.grpc.util.StringUtil;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class InetUtil {
    // Used to store ip address
    private static String ip;
    // Used to store hostname of the ip address
    private static String hostname;

    private static final String CLASSNAME = "InetUtil";

    public static String getIp() {
        return ip;
    }

    public static String getHostname() {
        return hostname;
    }

    /**
     * Method used to scan local network adapters and return the local ip addr
     * */
    public static void searchForLocalIP() throws InetUtilException {
        final String ctx = CLASSNAME + ".getLocalIP";
        try {
            // Getting IP Addr of localhost, this can return a loopback addr if has security manager checks
            // (SecurityManager.checkConnect)
            // so, we need to check that before returning the IP.
            InetAddress localhost = InetAddress.getLocalHost();

            // Getting the host name
            String host_name = localhost.getHostName();

            // Getting real IP using the hostname
            InetAddress address = InetAddress.getByName(host_name);

            // Checking if it's a loopback addr
            if (address.isSiteLocalAddress() && !address.isLoopbackAddress()) {
                ip = address.getHostAddress();
                hostname = host_name;
            } else {
                // Deep search to find the first local and not loopback ip addr
                Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
                // Used to break if already found
                boolean found = false;
                while( networkInterfaceEnumeration.hasMoreElements() && !found){
                    for ( InterfaceAddress interfaceAddress : networkInterfaceEnumeration.nextElement().getInterfaceAddresses())
                        if ( interfaceAddress.getAddress().isSiteLocalAddress() && !interfaceAddress.getAddress().isLoopbackAddress()) {
                            ip = interfaceAddress.getAddress().getHostAddress();
                            hostname = interfaceAddress.getAddress().getHostName();
                            found = true;
                            break;
                        }
                }
            }

        } catch (Exception e) {
            throw new InetUtilException(ctx + ": Unable to define your local IP address, please " +
                    "check your network configuration, try to have a single network. -> "+e.getMessage());
        }
        if (!StringUtil.hasText(ip) || !StringUtil.hasText(hostname)) {
            throw new InetUtilException(ctx + ": Unable to define your local IP address, please " +
                    "check your network configuration, try to have a single network. -> ");
        }
    }
}
