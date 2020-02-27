package com.example.addroute;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    static  String TAG = "AddRoute";
    Context mCtx = MainActivity.this;
    ProgressDialog progress_dialog;
    String eth1_name =  null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        if(get_root()){
            add_route(getPackageCodePath());
        }
        */

        try {
            eth1_name = getIpAddress("eth1");
        } catch (SocketException e) {
            e.printStackTrace();
        }

        if(eth1_name != null) {
            Log.d(TAG, "eth1 ip:" + eth1_name );
            add_route(eth1_name);
        }

        finish();
    }

    // 获取ROOT权限
    public boolean get_root(){
        boolean ret;
        if (is_root()){
            Toast.makeText(mCtx, "已经具有ROOT权限!", Toast.LENGTH_LONG).show();
            ret = true;
        }
        else{
            try{
                progress_dialog = ProgressDialog.show(mCtx,"ROOT", "正在获取ROOT权限...", true, false);
                Runtime.getRuntime().exec("su");
                ret = true;
            }
            catch (Exception e){
                Toast.makeText(mCtx, "获取ROOT权限时出错!", Toast.LENGTH_LONG).show();
                ret = false;
            }
        }
        return  ret;
    }

    public static boolean is_root() {
        boolean res = false;
        try {
            if ((!new File("/system/bin/su").exists()) &&
                    (!new File("/system/xbin/su").exists())) {
                res = false;
            } else {
                res = true;
            }
        } catch (Exception e) {
        }
        return res;
    }

    public boolean add_route(String eth1_ip){
        Process process = null;
        DataOutputStream os = null;
        try {
            //String cmd = "chmod 777 " + pkgCodePath;
            //String cmd = "ip route add 192.168.10.0/24 dev eth1 proto kernel scope link src 192.168.10.99 table eth0";
            String[] sourceStrArray = eth1_ip.split("\\.");
            Log.d(TAG, "length is "+sourceStrArray.length);
            for(int i=0; i<sourceStrArray.length;i++){
                Log.d(TAG, sourceStrArray[i]);
            }
            String cmd = "ip route add 192.168." + sourceStrArray[2] + ".0/24" + " dev eth1 proto kernel scope link src " + eth1_ip + " table eth0";
            process = Runtime.getRuntime().exec("su"); // 切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;

    }


    public static String[] getAllNetInterface() {
        ArrayList<String> availableInterface = new ArrayList<>();
        String [] interfaces = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }

                    String ip = ia.getHostAddress();
                    Log.d(TAG,"getAllNetInterface,available interface:"+ni.getName()+",address:"+ip);
                    // 过滤掉127段的ip地址
                    if (!"127.0.0.1".equals(ip)) {
                        availableInterface.add(ni.getName());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"all interface:"+availableInterface.toString());
        int size = availableInterface.size();
        if (size > 0) {
            interfaces = new String[size];
            for(int i = 0; i < size; i++) {
                interfaces[i] = availableInterface.get(i);
            }
        }
        return interfaces;
    }

    /**
     * Get Ip address 自动获取IP地址
     *
     * @throws SocketException
     */
    public static String getIpAddress(String netInterface) throws SocketException {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                //Log.d(TAG,"getIpAddress,interface:"+ni.getName());
                if (ni.getName().equals(netInterface)) {
                    Enumeration<InetAddress> ias = ni.getInetAddresses();
                    while (ias.hasMoreElements()) {
                        ia = ias.nextElement();
                        if (ia instanceof Inet6Address) {
                            continue;// skip ipv6
                        }
                        String ip = ia.getHostAddress();
                        // 过滤掉127段的ip地址
                        if (!"127.0.0.1".equals(ip)) {
                            hostIp = ia.getHostAddress();
                            break;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"getIpAddress,interface:"+netInterface+",ip:"+hostIp);
        return hostIp;
    }

}
