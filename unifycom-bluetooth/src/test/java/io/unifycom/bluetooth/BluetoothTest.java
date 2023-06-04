package io.unifycom.bluetooth;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class BluetoothTest {

    @Test
    public void testFind() throws IOException, InterruptedException {

        LocalDevice ld = LocalDevice.getLocalDevice();
        System.out.println("#本机蓝牙名称:" + ld.getFriendlyName());
        Vector<RemoteDevice> devicesDiscovered = new Vector<>();

        DiscoveryListener discoveryListener = new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {

                try {
                    System.out.println("deviceDiscovered:" + btDevice.getFriendlyName(false) + ", " + cod + ", " + btDevice.getBluetoothAddress());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                devicesDiscovered.add(btDevice);
            }

            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecords) {

                System.out.println("servicesDiscovered:");

                for (ServiceRecord serviceRecord : servRecords) {

                    System.out.println("serviceRecord = " + serviceRecord.toString() + ", " + serviceRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false));

                }
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {

                System.out.println("serviceSearchCompleted: transID=" + transID + ", respCode=" + respCode);
            }

            @Override
            public void inquiryCompleted(int discType) {

                System.out.println("inquiryCompleted: " + discType);
            }
        };

        ld.getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, discoveryListener);


        Thread.sleep(1000 * 60 * 1);

//        RemoteDevice[] remoteDevices = ld.getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);

        for (RemoteDevice remoteDevice : devicesDiscovered) {

            System.out.println("已匹配: " + remoteDevice.getFriendlyName(false) + ", " + remoteDevice.getBluetoothAddress());

            if (StringUtils.equals(remoteDevice.getBluetoothAddress(), "000878439229")) {

                String serviceUUID = StringUtils.remove("00001101-0000-1000-8000-00805f9b34fb", '-').toUpperCase();
                UUID[] searchUuidSet = new UUID[]{new UUID(serviceUUID, false)};
                int[] attrIDs = new int[]{0x0100};

                ld.getDiscoveryAgent().searchServices(null, searchUuidSet, remoteDevice, discoveryListener);

                String nameUrl = "btspp://000878439229:1;authenticate=false;encrypt=false;master=false";
                StreamConnection streamConnection = (StreamConnection) Connector.open(nameUrl);

                InputStream inputStream = streamConnection.openInputStream();
                while (inputStream.available() > 0) {

                    System.out.println(inputStream.read());
                }

            }
        }


//        String nameUrl = "btgoep://341CF0FD04FB:1;authenticate=false;encrypt=false;master=false";
//        StreamConnection streamConnection = (StreamConnection) Connector.open(nameUrl);
//
//        InputStream inputStream = streamConnection.openInputStream();
//        byte[] buf = IOUtils.readFully(inputStream, 1024 * 1024);
//
//        System.out.println(buf);

        Thread.sleep(1000 * 60 * 10);
    }
}
